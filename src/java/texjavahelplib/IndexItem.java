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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.InputStream;

import java.net.URL;

import java.nio.charset.Charset;

import java.util.Vector;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderAdapter;

/**
 * An object representing an entry from the index.xml file. 
 */
public class IndexItem
{
   public IndexItem(MessageSystem messageSystem, String key)
   {
      this(messageSystem, key, null, null);
   }

   public IndexItem(MessageSystem messageSystem, String key, String target, String filename)
   {
      this.messageSystem = messageSystem;
      this.key = key;
      this.target = target;
      this.filename = filename;
   }

   public String getKey()
   {
      return key;
   }

   public void setTarget(String target)
   {
      this.target = target;
   }

   public String getTarget()
   {
      return target;
   }

   public void setName(String name)
   {
      if ("".equals(name))
      {
         this.name = null;
      }
      else
      {
         this.name = name;
      }
   }

   public String getName()
   {
      return name;
   }

   public void setDescription(String description)
   {
      if ("".equals(description))
      {
         this.description = null;
      }
      else
      {
         this.description = description;
      }
   }

   public String getDescription()
   {
      return description;
   }

   public void setLongValue(String longValue)
   {
      if ("".equals(longValue))
      {
         this.longValue = null;
      }
      else
      {
         this.longValue = longValue;
      }
   }

   public String getLongValue()
   {
      return longValue;
   }

   public void setShortValue(String shortValue)
   {
      if ("".equals(shortValue))
      {
         this.shortValue = null;
      }
      else
      {
         this.shortValue = shortValue;
      }
   }

   public String getShortValue()
   {
      return shortValue;
   }

   public void setFileName(String filename)
   {
      this.filename = filename;
   }

   public String getFileName()
   {
      return filename;
   }

   protected void save(PrintWriter out)
     throws IOException
   {
      out.format("<entry key=\"%s\"", TeXJavaHelpLib.encodeHTML(key, true));

      if (target != null)
      {
         out.format(" target=\"%s\"", TeXJavaHelpLib.encodeHTML(target, true));
      }

      if (filename != null)
      {
         out.format(" filename=\"%s\"", TeXJavaHelpLib.encodeHTML(filename, true));
      }

      out.println(">");

      if (name != null)
      {
         out.print("<name>");
         out.print(TeXJavaHelpLib.encodeHTML(name, false));
         out.println("</name>");
      }

      if (description != null)
      {
         out.print("<description>");
         out.print(TeXJavaHelpLib.encodeHTML(description, false));
         out.println("</description>");
      }

      if (shortValue != null)
      {
         out.print("<short>");
         out.print(TeXJavaHelpLib.encodeHTML(shortValue, false));
         out.println("</short>");
      }

      if (longValue != null)
      {
         out.print("<long>");
         out.print(TeXJavaHelpLib.encodeHTML(longValue, false));
         out.println("</long>");
      }

      out.println("</entry>");
   }

   public static void saveIndex(Vector<IndexItem> indexData,
       PrintWriter out, Charset charset)
     throws IOException
   {
      out.print("<?xml version=\"1.0\" encoding=\"");
      out.print(charset.name());
      out.println("\" standalone=\"no\"?>");

      out.println("<index>");

      for (IndexItem item : indexData)
      {
         item.save(out);
      }

      out.println("</index>");
   }

   public static void saveIndex(HashMap<String,IndexItem> indexData,
       PrintWriter out, Charset charset)
     throws IOException
   {
      out.print("<?xml version=\"1.0\" encoding=\"");
      out.print(charset.name());
      out.println("\" standalone=\"no\"?>");

      out.println("<index>");

      for (String mapKey : indexData.keySet())
      {
         IndexItem item = indexData.get(mapKey);
         item.save(out);
      }

      out.println("</index>");
   }

   public static Vector<IndexItem> load(TeXJavaHelpLib helpLib)
    throws IOException,SAXException
   {
      InputStream in = null;
      Vector<IndexItem> data = null;

      try
      {
         in = helpLib.getIndexXMLInputStream();

         data = readIndex(helpLib.getMessageSystem(), in);
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

   public static Vector<IndexItem> readIndex(MessageSystem messageSystem, Reader in)
      throws IOException,SAXException
   {
      IndexReader reader = new IndexReader(messageSystem);

      reader.parse(new InputSource(in));

      return reader.getIndexData();
   }

   public static Vector<IndexItem> readIndex(MessageSystem messageSystem, InputStream in)
      throws IOException,SAXException
   {
      IndexReader reader = new IndexReader(messageSystem);

      reader.parse(new InputSource(in));

      return reader.getIndexData();
   }

   public String brief()
   {
      if (longValue != null) return longValue;
      if (description != null && description.length() < BRIEF_MAX_CHARS) return description;
      if (name != null) return name;
      if (shortValue != null) return shortValue;
      return "";
   }

   public String details()
   {
      if (name == null && description == null && shortValue == null && longValue == null)
      {
         return "";
      }

      if (shortValue != null && (longValue != null || description != null))
      {
         if (longValue == null || longValue.equals(description))
         {
            return messageSystem.getMessage("indexmap.short_description",
              shortValue, description);
         }
         else
         {
            return messageSystem.getMessage("indexmap.long_short_description",
              longValue, shortValue, description);
         }
      }
      else if (name != null)
      {
         if (description == null)
         {
            return name;
         }
         else
         {
            return messageSystem.getMessage("indexmap.name_description",
              name, description);
         }
      }

      return brief();
   }

   @Override
   public String toString()
   {
      return String.format("%s[key=%s,target=%s,filename=%s,name=%s,description=%s,short=%s,long=%s]",
        getClass().getSimpleName(), key, target, filename, name, description,
         shortValue, longValue);
   }

   
   protected final String key;
   protected String target, filename;
   protected String name, description, shortValue, longValue;

   protected MessageSystem messageSystem;

   public static final int BRIEF_MAX_CHARS=60;
}

class IndexReader extends XMLReaderAdapter
{
   protected IndexReader(MessageSystem messageSystem) throws SAXException
   {
      super();
      this.messageSystem = messageSystem;
   }

   public Vector<IndexItem> getIndexData()
   {
      return indexData;
   }

   @Override
   public void startElement(String uri, String localName, String qName,
     Attributes attrs)
   throws SAXException
   {
      super.startElement(uri, localName, qName, attrs);

      if ("index".equals(qName))
      {
         if (indexData != null)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.more_than_one_tag", "more than 1 <{0}> found", qName));
         }

         indexData = new Vector<IndexItem>();
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

         if (currentItem != null)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.tag_found_inside",
              "<{0}> found inside <{1}>", qName, qName));
         }

         if (indexData == null || indexEndTagFound)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.tag_found_outside",
              "<{0}> found outside <{1}>", qName, "index"));
         }

         String key = attrs.getValue("key");

         if (key == null)
         {
            throw new SAXException(messageSystem.getMessageWithFallback(
             "error.xml.missing_attr_in_tag", 
             "Missing ''{0}'' attribute in <{1}>", "key", qName));
         }

         String target = attrs.getValue("target");

         String filename = attrs.getValue("filename");

         currentItem = new IndexItem(messageSystem, key, target, filename);
      }
      else if ("name".equals(qName) || "description".equals(qName)
              || "long".equals(qName) || "short".equals(qName)
              || "comment".equals(qName))
      {
         if (previousQname != null)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.tag_found_inside", "<{0}> found inside <{1}>",
                qName, previousQname));
         }

         previousQname = qName;

         currentBuilder = new StringBuilder();
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

      if ("index".equals(qName))
      {
         indexEndTagFound = true;
      }
      else if ("entry".equals(qName))
      {
         indexData.add(currentItem);
         currentItem = null;
      }
      else if ("name".equals(qName))
      {
         currentItem.setName(currentBuilder.toString());
         currentBuilder = null;
         previousQname = null;
      }
      else if ("description".equals(qName))
      {
         currentItem.setDescription(currentBuilder.toString());
         currentBuilder = null;
         previousQname = null;
      }
      else if ("short".equals(qName))
      {
         currentItem.setShortValue(currentBuilder.toString());
         currentBuilder = null;
         previousQname = null;
      }
      else if ("long".equals(qName))
      {
         currentItem.setLongValue(currentBuilder.toString());
         currentBuilder = null;
         previousQname = null;
      }
      else if ("comment".equals(qName))
      {
         currentBuilder = null;
         previousQname = null;
      }
      else
      {
         throw new SAXException(messageSystem.getMessageWithFallback(
          "error.xml.unknown_end_tag",
          "Unknown end tag </{0}> found", qName));
      }
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

   private Vector<IndexItem> indexData;

   private IndexItem currentItem;

   private StringBuilder currentBuilder;

   private boolean indexEndTagFound = false;

   private String previousQname = null;

   private MessageSystem messageSystem;
}

