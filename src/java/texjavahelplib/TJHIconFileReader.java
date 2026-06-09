/*    
    Copyright (C) 2026 Nicola L.C. Talbot
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
import java.io.Reader;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderAdapter;

/**
 * Reads XML file created by <code>TJHIconFile.writeXML</code>.
 */
public class TJHIconFileReader extends XMLReaderAdapter
{
   public TJHIconFileReader(TeXJavaHelpLib helpLib) throws SAXException
   {
      super();
      this.helpLib = helpLib;
   }

   public static void load(TeXJavaHelpLib helpLib)
   throws IOException,SAXException
   {
      Reader in = null;

      try
      {
         in = helpLib.getHelpsetIconsXMLReader();

         TJHIconFileReader xmlReader = new TJHIconFileReader(helpLib);

         xmlReader.parse(new InputSource(in));
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }

   @Override 
   public void startElement(String uri, String localName, String qName,
     Attributes attrs)
   throws SAXException
   {
      super.startElement(uri, localName, qName, attrs);

      if ("icons".equals(qName))
      {
         if (iconsFound)
         {
            throw new SAXException(
              helpLib.getMessageWithFallback(
              "error.xml.more_than_one_tag", "more than 1 <{0}> found", qName));
         }

         iconsFound = true;
      }
      else if ("icon".equals(qName))
      {
         if (previousQname != null)
         {
            throw new SAXException(
              helpLib.getMessageWithFallback(
              "error.xml.tag_found_inside",
              "<{0}> found inside <{1}>", qName, previousQname));
         }

         if (!iconsFound || iconsEndTagFound)
         {  
            throw new SAXException(
              helpLib.getMessageWithFallback(
              "error.xml.tag_found_outside",
              "<{0}> found outside <{1}>", qName, "icons"));
         } 

         String name = attrs.getValue("name");

         if (name == null)
         {
            throw new SAXException(helpLib.getMessageWithFallback(
             "error.xml.missing_attr_in_tag", 
             "Missing ''{0}'' attribute in <{1}>", "name", qName));
         }

         String path = attrs.getValue("path");

         if (path == null)
         {
            throw new SAXException(helpLib.getMessageWithFallback(
             "error.xml.missing_attr_in_tag", 
             "Missing ''{0}'' attribute in <{1}>", "path", qName));
         }

         String filename = attrs.getValue("filename");

         if (filename == null)
         {
            throw new SAXException(helpLib.getMessageWithFallback(
             "error.xml.missing_attr_in_tag", 
             "Missing ''{0}'' attribute in <{1}>", "filename", qName));
         }

         String ext = attrs.getValue("ext");

         if (ext == null)
         {
            throw new SAXException(helpLib.getMessageWithFallback(
             "error.xml.missing_attr_in_tag", 
             "Missing ''{0}'' attribute in <{1}>", "ext", qName));
         }

         String size = attrs.getValue("size");

         try
         {
            helpLib.addDocumentationIcon(name, path, filename, ext, size);
         }
         catch (IOException e)
         {
            helpLib.warning(
             helpLib.getMessageWithFallback(
              "error.failed_to_add_doc_image_map",
              "Failed to add documentation image map name={0}, path={1}, filename={2}, ext={3}, size={4}",
              name, path, filename, ext, size),
             e);
         }

         previousQname = qName;
      }
      else
      {
         throw new SAXException(helpLib.getMessageWithFallback(
          "error.xml.unknown_tag", "Unknown tag <{0}>", qName));
      }
   }

   @Override
   public void endElement(String uri, String localName, String qName)
    throws SAXException
   {
      super.endElement(uri, localName, qName);

      if ("icons".equals(qName))
      {
         iconsEndTagFound = true;
      }
      else if ("icon".equals(qName))
      {
         previousQname = null;
      }
      else
      {
         throw new SAXException(helpLib.getMessageWithFallback(
          "error.xml.unknown_end_tag",
          "Unknown end tag </{0}> found", qName));
      }
   }

   TeXJavaHelpLib helpLib;
   boolean iconsFound, iconsEndTagFound;
   String previousQname;
}
