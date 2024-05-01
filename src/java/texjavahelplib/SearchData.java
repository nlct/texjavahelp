/*
    Copyright (C) 2024 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.texjavahelplib;

import java.util.Vector;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Comparator;

import java.text.BreakIterator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.nio.charset.Charset; 
import java.nio.file.Files;
import java.nio.file.Path;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderAdapter;

public class SearchData
{
   public SearchData()
   {
      items = new Vector<SearchItem>();
      contexts = new HashMap<Integer,SearchContext>();
   }

   public void add(SearchItem item, CharSequence context)
   {
      items.add(item);

      Integer contextId = Integer.valueOf(item.getContextId());
      SearchContext searchContext = contexts.get(contextId);

      if (searchContext == null)
      {
         searchContext = new SearchContext(contextId.intValue(), context);
         contexts.put(contextId, searchContext);
      }

      searchContext.addItem(item);
   }

   protected void add(Vector<SearchItem> itemList, Vector<SearchContext> contextList)
     throws UnknownContextException
   {
      for (SearchContext searchContext : contextList)
      {
         Integer contextId = Integer.valueOf(searchContext.getId());
         contexts.put(contextId, searchContext);
      }

      for (SearchItem item : itemList)
      {
         Integer contextId = Integer.valueOf(item.getContextId());
         SearchContext searchContext = contexts.get(contextId);

         if (searchContext == null)
         {
            throw new UnknownContextException(contextId, item.toString());
         }

         searchContext.addItem(item);
      }

      items.addAll(itemList);
   }

   public HashMap<Integer,SearchContext> getContexts()
   {
      return contexts;
   }

   public SearchContext getContext(int id)
   {
      return contexts.get(Integer.valueOf(id));
   }

   public TreeSet<SearchResult> search(TeXJavaHelpLib helpLib, String wordList,
      boolean caseSensitive, boolean exact)
   {
      wordList = helpLib.preProcessSearchWordList(wordList);

      Vector<String> words = null;

      BreakIterator boundary = BreakIterator.getWordInstance();
      boundary.setText(wordList);

      int idx1 = boundary.first();

      for (int idx2 = boundary.next(); idx2 != BreakIterator.DONE;
           idx1 = idx2, idx2 = boundary.next())
      {
         String word = wordList.substring(idx1, idx2).trim();

         if (words == null)
         {
            words = new Vector<String>();
         }

         if (!word.isEmpty() && !words.contains(word))
         {
            words.add(word);
         }
      }

      if (words == null || words.isEmpty())
      {
         return null;
      }

      if (!exact && words.size() > 1)
      {
         words.sort(new Comparator<String>()
          {
             public int compare(String str1, String str2)
             {
                int n1 = str1.length();
                int n2 = str2.length();

                if (n1 > n2)
                {
                   return -1;
                }
                else if (n1 < n2)
                {
                   return 1;
                }
                else
                {
                   return 0;
                }
             }
          });
      }

      TreeSet<SearchResult> results = null;

      for (Integer key : contexts.keySet())
      {
         SearchContext searchContext = contexts.get(key);

         SearchResult result = searchContext.find(helpLib,
           words, caseSensitive, exact);

         if (result != null)
         {
            if (results == null)
            {
               results = new TreeSet<SearchResult>();
            }

            results.add(result);
         }
      }

      return results;
   }

   public void write(Path path, Charset charset)
   throws IOException
   {
      PrintWriter out = null;

      try
      {
         out = new PrintWriter(Files.newBufferedWriter(path, charset));

         out.print("<?xml version=\"1.0\" encoding=\"");
         out.print(charset.name());
         out.println("\" standalone=\"no\"?>");

         out.println("<search>");

         for (SearchItem item : items)
         {
            out.print("<entry node=\"");

            out.print(item.getNodeLabel());

            out.print("\" ");

            out.format("context-start=\"%d\" ", item.getContextStart());

            out.format("context-end=\"%d\" ", item.getContextEnd());

            out.format("context-id=\"%d\" ", item.getContextId());

            out.print(">");

            out.print(TeXJavaHelpLib.encodeHTML(item.getWord(), false));

            out.println("</entry>");

         }

         for (Integer key : contexts.keySet())
         {
            out.format("<context id=\"%d\">", key);

            out.print(TeXJavaHelpLib.encodeHTML(
               contexts.get(key).getText().toString(), false));

            out.println("</context>");
         }

         out.println("</search>");
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   public static SearchData load(TeXJavaHelpLib helpLib)
    throws IOException,SAXException
   {
      BufferedReader in = null;

      SearchData data = null;

      try
      {
         in = new BufferedReader(new InputStreamReader(
            helpLib.getSearchXMLInputStream()));

         SearchDataReader reader = new SearchDataReader(helpLib.getMessageSystem());

         reader.parse(new InputSource(in));

         data = reader.getData();
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      return data;
   }

   protected Vector<SearchItem> items; 
   protected HashMap<Integer,SearchContext> contexts;
}

class SearchDataReader extends XMLReaderAdapter
{
   protected SearchDataReader(MessageSystem messageSystem)
    throws SAXException
   {
      super();

      this.messageSystem = messageSystem;

      itemList = new Vector<SearchItem>();
      contextList = new Vector<SearchContext>();
   }

   public SearchData getData()
   throws SAXException
   {
      if (data == null)
      {
         throw new SAXException(messageSystem.getMessageWithFallback(
           "error.xml.missing_tag", "Missing tag <{0}>", "search"));
      }

      return data;
   }

   protected int getIntAttributeValue(String qName, Attributes attrs, String attrName)
    throws SAXException
   {
      String val = attrs.getValue(attrName);

      if (val == null)
      {
         throw new SAXException(messageSystem.getMessageWithFallback(
          "error.xml.missing_attr_in_tag",
          "Missing ''{0}'' attribute in <{1}>", attrName, qName));
      }

      try
      {
         return Integer.parseInt(val);
      }
      catch (NumberFormatException e)
      {
         throw new SAXException(messageSystem.getMessageWithFallback(
          "error.xml.int_attr_required_in_tag",
          "Integer ''{0}'' attribute required in <{1}> (found ''{2}'')",
          attrName, qName, val), e);
      }
   }

   @Override
   public void startElement(String uri, String localName, String qName,
     Attributes attrs)
   throws SAXException
   {
      super.startElement(uri, localName, qName, attrs);

      if ("search".equals(qName))
      {
         if (data != null)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.more_than_one_tag", "more than 1 <{0}> found", qName));
         }

         data = new SearchData();
      }
      else if (endFound)
      {
         throw new SAXException(
           messageSystem.getMessageWithFallback(
           "error.xml.xml.tag_found_outside", "<{0}> found outside <{1}>",
              qName, "search"));
      }
      else if ("entry".equals(qName))
      {
         if (previousQname != null)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.tag_found_inside",
              "<{0}> found inside <{1}>", qName, previousQname));
         }

         currentNodeId = attrs.getValue("node");

         if (currentNodeId == null)
         {
            throw new SAXException(messageSystem.getMessageWithFallback(
             "error.xml.missing_attr_in_tag",
             "Missing ''{0}'' attribute in <{1}>", "node", qName));
         }

         currentContextStart = getIntAttributeValue(qName, attrs, "context-start");
         currentContextEnd = getIntAttributeValue(qName, attrs, "context-end");
         currentContextId = getIntAttributeValue(qName, attrs, "context-id");

         currentBuilder = new StringBuilder();

         previousQname = qName;
      }
      else if ("context".equals(qName))
      {
         if (previousQname != null)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.tag_found_inside",
              "<{0}> found inside <{1}>", qName, previousQname));
         }

         currentContextId = getIntAttributeValue(qName, attrs, "id");

         currentBuilder = new StringBuilder();

         previousQname = qName;
      }
      else
      {
         throw new SAXException(messageSystem.getMessageWithFallback(
          "error.xml.unknown_tag", "Unknown tag <{0}>", qName));
      }
   }

   @Override
   public void endElement(String uri, String localName, String qName)
    throws SAXException
   {
      super.endElement(uri, localName, qName);

      previousQname = null;

      if ("search".equals(qName))
      {
         endFound = true;

         try
         {
            data.add(itemList, contextList);
         }
         catch (UnknownContextException e)
         {
            throw new SAXException(messageSystem.getMessageWithFallback(
              "error.xml.unknown_context",
              "No context found for ID {0} referenced by entry ''{1}'",
              e.getId(), e.getRef()));
         }
      }
      else if ("entry".equals(qName))
      {
         SearchItem item = new SearchItem(currentBuilder.toString(),
           currentContextStart, currentContextEnd,
           currentNodeId, currentContextId);

         itemList.add(item);
      }
      else if ("context".equals(qName))
      {
         SearchContext context
           = new SearchContext(currentContextId, currentBuilder);

         contextList.add(context);
      }

      currentBuilder = null;
   }

   @Override       
   public void characters(char[] ch, int start, int length)
    throws SAXException
   {
      super.characters(ch, start, length);

      if (currentBuilder == null)
      {
         for (int i = 0; i < length; i++)
         {
            if (!Character.isWhitespace(ch[start+i]))
            {
               throw new SAXException(
                 messageSystem.getMessageWithFallback("error.xml.unexpected_chars",
                   "Unexpected content ''{0}'' found",
                   new String(ch, start+i, length-i)));
            }
         }
      }
      else
      {
         currentBuilder.append(ch, start, length);
      }
   }

   protected SearchData data;
   protected MessageSystem messageSystem;

   protected String previousQname;
   protected boolean endFound = false;
   protected StringBuilder currentBuilder = null;

   protected String currentNodeId;
   protected int currentContextStart, currentContextEnd;
   protected int currentContextId;

   protected Vector<SearchContext> contextList;
   protected Vector<SearchItem> itemList;
}
