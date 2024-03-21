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

import java.nio.charset.Charset;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.tree.TreeNode;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderAdapter;

import com.dickimawbooks.texparserlib.html.DivisionNode;

/**
 * Navigation node that represents the table of contents. 
 */
public class NavigationNode implements TreeNode
{
   protected NavigationNode(String key, String ref)
   {
      this(key, ref, null);
   }

   protected NavigationNode(String key, String ref, NavigationNode parentNode)
   {
      this.key = key;
      this.ref = ref;
      this.parent = parentNode;

      if (parent != null)
      {
         if (parent.children == null)
         {
            parent.children = new Vector<NavigationNode>();
         }

         parent.children.add(this);
      }
   }

   public String getKey()
   {
      return key;
   }

   public String getRef()
   {
      return ref;
   }

   public String getPrefix()
   {
      return prefix;
   }

   public String getTitle()
   {
      return title;
   }

   public String getFileName()
   {
      return filename;
   }

   @Override
   public String toString()
   {
      if (prefix != null)
      {
         return prefix + " " + title;
      }
      else
      {
         return title;
      }
   }

   @Override
   public Enumeration children()
   {
      return children == null ? null : children.elements();
   }

   @Override
   public TreeNode getParent()
   {
      return parent;
   }

   @Override
   public boolean isLeaf()
   {
      return children == null || children.isEmpty();
   }

   @Override
   public boolean getAllowsChildren()
   {
      return !isLeaf();
   }

   @Override
   public int getChildCount()
   {
      return children == null ? 0 : children.size();
   }

   @Override
   public TreeNode getChildAt(int childIndex)
   {
      if (childIndex >= 0 && childIndex < getChildCount())
      {
         return children.get(childIndex);
      }

      throw new ArrayIndexOutOfBoundsException(childIndex);
   }

   @Override
   public int getIndex(TreeNode node)
   {
      if (children != null)
      {
         for (int i = 0; i < children.size(); i++)
         {
            if (children.get(i).equals(node))
            {
               return i;
            }
         }
      }

      return -1;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof NavigationNode))
      {
         return false;
      }

      return key.equals(((NavigationNode)obj).getKey());
   }

   protected void addChildren(DivisionNode divNode)
   {
      int n = divNode.getChildCount();

      if (n > 0)
      {
         children = new Vector<NavigationNode>(n);

         for (Iterator<DivisionNode> it = divNode.getChildIterator(); it.hasNext(); )
         {
            DivisionNode childDivNode = it.next();

            NavigationNode childNavNode = new NavigationNode(
              childDivNode.getId(), childDivNode.getRef(), this);

            childNavNode.prefix = childDivNode.getPrefix();
            childNavNode.title = childDivNode.getTitle();
            childNavNode.filename = childDivNode.getFile().getName();

            childNavNode.addChildren(childDivNode);
         }
      }
   }

   public static NavigationNode createTree(DivisionNode divNode)
   {
      NavigationNode navNode = new NavigationNode(
        divNode.getId(), divNode.getRef());

      navNode.prefix = divNode.getPrefix();
      navNode.title = divNode.getTitle();
      navNode.filename = divNode.getFile().getName();

      navNode.addChildren(divNode);

      return navNode;
   }

   /**
    * Saves the tree with this node as the root. Note that the TJHListener
    * should already have encoded the HTML entities, but they
    * will need to be double-encoded to ensure that when the file is read back,
    * the entities will be preserved.
    */ 
   public void saveTree(PrintWriter out, Charset charset) throws IOException
   {
      out.print("<?xml version=\"1.0\" encoding=\"");
      out.print(charset.name());
      out.println("\" standalone=\"no\"?>");

      out.println("<navigation>");

      saveSubTree(out);

      out.println("</navigation>");
   }

   protected void saveSubTree(PrintWriter out) throws IOException
   {
      // The key shouldn't have any awkward characters, but just in case

      out.printf("<node key=\"%s\" ref=\"%s\">%n",
        TeXJavaHelpLib.encodeHTML(key, true),
        TeXJavaHelpLib.encodeHTML(ref, true)
       );

      if (prefix != null)
      {
         out.printf(" <prefix>%s</prefix>%n", TeXJavaHelpLib.encodeHTML(prefix, false));
      }

      out.printf(" <title>%s</title>%n", TeXJavaHelpLib.encodeHTML(title, false));

      out.printf(" <filename>%s</filename>%n", TeXJavaHelpLib.encodeHTML(filename, false));

      if (children != null)
      {
         for (NavigationNode child : children)
         {
            child.saveSubTree(out);
         }
      }

      out.println("</node>");
   }

   public static NavigationNode readTree(MessageSystem messageSystem, Reader in)
      throws IOException,SAXException
   {
      NavigationTreeReader reader = new NavigationTreeReader(messageSystem);

      reader.parse(new InputSource(in));

      return reader.getRootNode();
   }

   protected final String key, ref;
   protected String prefix;
   protected String title;
   protected String filename;

   protected Vector<NavigationNode> children;
   protected NavigationNode parent;
}

class NavigationTreeReader extends XMLReaderAdapter
{
   protected NavigationTreeReader(MessageSystem messageSystem) throws SAXException
   {
      super();
      this.messageSystem = messageSystem;
   }

   public NavigationNode getRootNode()
   {
      return rootNode;
   }

   @Override
   public void startElement(String uri, String localName, String qName,
     Attributes attrs)
   throws SAXException
   {
      super.startElement(uri, localName, qName, attrs);

      if ("navigation".equals(qName))
      {
         if (navTagFound)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.more_than_one_tag", "more than 1 <{0}> found", qName));
         }

         navTagFound = true;
      }
      else if ("node".equals(qName))
      {
         if (previousQname != null)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.tag_found_inside",
              "<{0}> found inside <{1}>", qName, previousQname));
         }

         if (!navTagFound || navEndTagFound)
         {
            throw new SAXException(
              messageSystem.getMessageWithFallback(
              "error.xml.tag_found_outside",
              "<{0}> found outside <{1}>", qName, "navigation"));
         }

         String key = attrs.getValue("key");

         if (key == null)
         {
            throw new SAXException(messageSystem.getMessageWithFallback(
             "error.xml.missing_attr_in_tag", 
             "Missing ''{0}'' attribute in <{0}>", "key", "node"));
         }

         String ref = attrs.getValue("ref");

         if (ref == null)
         {
            throw new SAXException(messageSystem.getMessageWithFallback(
             "error.xml.missing_attr_in_tag", 
             "Missing ''{0}'' attribute in <{0}>", "ref", "node"));
         }

         currentParent = currentNode;
         currentNode = new NavigationNode(key, ref, currentParent);

         if (rootNode == null)
         {
            rootNode = currentNode;
         }
      }
      else if ("title".equals(qName) || "prefix".equals(qName)
              || "filename".equals(qName) || "comment".equals(qName))
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

      if ("navigation".equals(qName))
      {
         navEndTagFound = true;
      }
      else if ("node".equals(qName))
      {
         if (currentNode.title == null)
         {
            throw new SAXException(messageSystem.getMessageWithFallback(
              "error.xml.missing_tag_for_node",
              "Missing <{0}> for node ''{1}''",
              "title", currentNode.getKey()));
         }

         if (currentNode.filename == null)
         {
            throw new SAXException(messageSystem.getMessageWithFallback(
              "error.xml.missing_tag_for_node",
              "Missing <{0}> for node ''{1}''",
              "filename", currentNode.getKey()));
         }

         currentNode = currentParent;

         if (currentNode == null)
         {
            currentParent = null;
         }
         else
         {
            currentParent = currentNode.parent;
         }
      }
      else if ("title".equals(qName))
      {
         currentNode.title = currentBuilder.toString();
         currentBuilder = null;
         previousQname = null;
      }
      else if ("prefix".equals(qName))
      {
         currentNode.prefix = currentBuilder.toString();
         currentBuilder = null;
         previousQname = null;
      }
      else if ("filename".equals(qName))
      {
         currentNode.filename = currentBuilder.toString();
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

   private NavigationNode rootNode, currentNode, currentParent;

   private StringBuilder currentBuilder;

   private boolean navTagFound = false, navEndTagFound = false;

   private String previousQname = null;

   private MessageSystem messageSystem;
}

