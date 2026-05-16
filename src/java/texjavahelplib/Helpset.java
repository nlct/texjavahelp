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

import java.io.*;

import java.nio.file.Path;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import java.util.zip.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderAdapter;

public class Helpset
{
   public Helpset(TeXJavaHelpLib helpLib)
   {
      this.helpLib = helpLib;

      map = new HashMap<String,HelpsetFile>();
      manifest = new Vector<String>();
   }

   public void add(HelpsetFile hsf, boolean onlyForHelpLocale)
   {
      if (!onlyForHelpLocale || !hsf.hasLocale()
           || helpLib.getHelpSetLocale().getLocale().equals(hsf.getLocale()))
      {
         add(hsf);
      }
   }

   public void add(HelpsetFile hsf)
   {
      add(hsf.getName(), hsf);
   }

   public void add(String name, HelpsetFile hsf)
   {
      if (hsf == null)
      {
         throw new NullPointerException();
      }

      if (name == null)
      {
         name = hsf.getName();

         if (name == null)
         {
            name = helpLib.getResourcePath();

            if (!name.endsWith("/"))
            {
               name += "/";
            }

            name += hsf.getRef();
         }
      }

      if (hsf.getName() == null)
      {
         hsf.setName(name);
      }

      map.put(name, hsf);

      if (!manifest.contains(name))
      {
         manifest.add(name);
      }
   }

   public HelpsetFile get(String name)
   {
      return map.get(name);
   }

   public HelpsetFile remove(String name)
   {
      manifest.remove(name);

      return map.remove(name);
   }

   public void writeManifest(OutputStream out) throws IOException
   {
      byte[] byteArray = MANIFEST_XML_HEADER.getBytes();

      out.write(byteArray, 0, byteArray.length);

      byte[] eol = String.format("%n").getBytes();
      out.write(eol, 0, eol.length);

      byteArray = ("<"+MANIFEST_CONTAINER+">").getBytes();
      out.write(byteArray, 0, byteArray.length);

      out.write(eol, 0, eol.length);

      for (String name : manifest)
      {
         HelpsetFile hsf = map.get(name);

         hsf.writeManifestEntry(out);

         out.write(eol, 0, eol.length);
      }

      byteArray = ("</"+MANIFEST_CONTAINER+">").getBytes();
      out.write(byteArray, 0, byteArray.length);
      out.write(eol, 0, eol.length);
   }

   public void writeHelpset(File zipFile) throws IOException
   {
      helpLib.message(helpLib.getMessageWithFallback(
        "message.writing", "Writing {0}...", zipFile));

      FileOutputStream fout = null;
      ZipOutputStream zipOut = null;
      FileInputStream fin = null;

      try
      {
         fout = new FileOutputStream(zipFile);

         zipOut = new ZipOutputStream(fout);

         ZipEntry zipEntry = new ZipEntry("mimetype");
         zipEntry.setMethod(ZipEntry.STORED);

         byte[] byteArray = ZIP_HELPSET_MIME_TYPE.getBytes();

         CRC32 crc = new CRC32();
         crc.update(byteArray);
         zipEntry.setCrc(crc.getValue());
         zipEntry.setSize(byteArray.length);
         zipEntry.setCompressedSize(byteArray.length);
         zipOut.putNextEntry(zipEntry);

         zipOut.write(byteArray, 0, byteArray.length);

         zipEntry = new ZipEntry(MANIFEST_XML);
         zipOut.putNextEntry(zipEntry);

         writeManifest(zipOut);

         for (String name : manifest)
         {
            HelpsetFile hsf = map.get(name);

            Path path = hsf.getPath();

            fin = new FileInputStream(path.toFile());

            zipEntry = new ZipEntry(hsf.getName());
            zipOut.putNextEntry(zipEntry);

            byteArray = new byte[1024];
            int length;

            while ((length = fin.read(byteArray)) >= 0)
            {
               zipOut.write(byteArray, 0, length);
            }

            fin.close();
            fin = null;
         }
      }
      finally
      {
         if (fin != null)
         {
            fin.close();
         }

         if (zipOut != null)
         {
            zipOut.close();
         }

         if (fout != null)
         {
            fout.close();
         }
      }

   }

   public static Helpset load(TeXJavaHelpLib helpLib, String zipName, InputStream in)
   throws IOException
   {
      helpLib.message(helpLib.getMessageWithFallback(
        "message.reading", "Reading {0}...", zipName));

      ZipInputStream zipIn = null;
      ZipEntry zipEntry;
      byte[] byteArray;

      HelpSetLocale locale = helpLib.getHelpSetLocale();

      Helpset hs = null;
      String manifestContent = null;

      try
      {
         zipIn = new ZipInputStream(in);

         while ((zipEntry = zipIn.getNextEntry()) != null)
         {
            String name = zipEntry.getName();

            if (name.equals("mimetype"))
            {
               byteArray = new byte[(int)zipEntry.getSize()];

               zipIn.read(byteArray, 0, byteArray.length);

               String mimetype = new String(byteArray);

               if (!mimetype.equals(ZIP_HELPSET_MIME_TYPE))
               {
                  throw new ZipIOException(helpLib, zipName, name,
                    helpLib.getMessage("error.invalid_mime_type", mimetype));
               }

               hs = new Helpset(helpLib);
            }
            else if (hs == null)
            {
               throw new ZipIOException(helpLib, zipName,
                  helpLib.getMessage("error.missing_entry", "mimetype"));
            }
            else if (name.equals(MANIFEST_XML))
            {
               ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

               byteArray = new byte[1024];
               int len = -1;
               String encoding = null;

               while ((len = zipIn.read(byteArray, 0, byteArray.length)) != -1)
               {
                  byteStream.write(byteArray, 0, len);

                  if (encoding == null)
                  {
                     Matcher m = XML_HEADER_ENCODING_PATTERN.matcher(
                       byteStream.toString("US-ASCII"));

                     if (m.lookingAt())
                     {
                        encoding = m.group(1);
                     }
                     else
                     {
                        encoding = "UTF-8";
                     }
                  }
               }

               if (encoding == null)
               {
                  // Most likely an empty file
                  manifestContent = byteStream.toString();
               }
               else
               {
                  manifestContent = byteStream.toString(encoding);
               }

               zipIn.closeEntry();
               zipIn.close();
               zipIn = null;

               break;
            }

            zipIn.closeEntry();
         }

         if (manifestContent == null)
         {
            throw new ZipIOException(helpLib, zipName,
              helpLib.getMessage("error.zip_missing_entry", MANIFEST_XML));
         }

         try
         {
            helpLib.debug(helpLib.getMessageWithFallback(
              "message.reading", "Reading {0}...", MANIFEST_XML));

            ManifestReader mReader = new ManifestReader(hs);

            mReader.parse(new InputSource(new StringReader(manifestContent)));
         }
         catch (Exception e)
         {
            throw new ZipIOException(helpLib,
              zipName, MANIFEST_XML, e.getLocalizedMessage(), e);
         }

         in.close();

         in = helpLib.getClass().getResourceAsStream(zipName);
         zipIn = new ZipInputStream(in);

         while ((zipEntry = zipIn.getNextEntry()) != null)
         {
            String name = zipEntry.getName();

            if (!name.equals("mimetype") && !name.equals(MANIFEST_XML))
            {
               try
               {
                  hs.readHelpsetFile(zipIn, zipEntry);
               }
               catch (Exception e)
               {
                  throw new ZipIOException(helpLib,
                    zipName, name, e.getLocalizedMessage(), e);
               }
            }

            zipIn.closeEntry();
         }
      }
      finally
      {
         if (zipIn != null)
         {
            zipIn.close();
         }
      }

      return hs;
   }

   protected void readHelpsetFile(ZipInputStream zipIn, ZipEntry zipEntry)
      throws IOException
   {
      String name = zipEntry.getName();

      HelpsetFile hsf = map.get(name);

      if (hsf != null)
      {
         helpLib.debug(helpLib.getMessageWithFallback(
           "message.reading", "Reading {0}...", hsf));

         ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

         byte[] byteArray = new byte[1024];
         int len = -1;

         while ((len = zipIn.read(byteArray, 0, byteArray.length)) != -1)
         {
            byteStream.write(byteArray, 0, len);
         }

         hsf.setByteContent(byteStream.toByteArray());
      }
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   TeXJavaHelpLib helpLib;

   HashMap<String,HelpsetFile> map;
   Vector<String> manifest;

   public static final String MANIFEST_XML = "manifest.xml";

   public static final Pattern XML_HEADER_ENCODING_PATTERN =
     Pattern.compile("<\\?xml .*?encoding=\"([^\"]+)\".*?\\?>");

   public static final String MANIFEST_XML_HEADER="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

   public static final String MANIFEST_CONTAINER="helpset";

   public static final String ZIP_HELPSET_MIME_TYPE = "application/texjavahelp+zip";
}

class ManifestReader extends XMLReaderAdapter
{
   protected ManifestReader(Helpset helpset) throws SAXException
   {
      super();
      this.helpset = helpset;
      helpLib = helpset.getHelpLib();

   }

   @Override
   public void startElement(String uri, String localName, String qName,
     Attributes attrs)
   throws SAXException
   {
      super.startElement(uri, localName, qName, attrs);

      if (Helpset.MANIFEST_CONTAINER.equals(qName))
      {
         if (manifestFound)
         {
            throw new SAXException(
              helpLib.getMessageWithFallback(
              "error.xml.more_than_one_tag", "more than 1 <{0}> found", qName));
         }

         manifestFound = true;
      }
      else if (HelpsetFile.ELEMENT_NAME.equals(qName))
      {
         if (previousQname != null)
         {
            throw new SAXException(
              helpLib.getMessageWithFallback(
              "error.xml.tag_found_inside",
              "<{0}> found inside <{1}>", qName, previousQname));
         }

         if (!manifestFound)
         {
            throw new SAXException(
              helpLib.getMessageWithFallback(
              "error.xml.tag_found_outside",
              "<{0}> found outside <{1}>", qName, Helpset.MANIFEST_CONTAINER));
         }

         String ref = attrs.getValue("ref");

         if (ref == null)
         {
            throw new SAXException(helpLib.getMessageWithFallback(
             "error.xml.missing_attr_in_tag",
             "Missing ''{0}'' attribute in <{1}>", "ref", qName));
         }

         String type = attrs.getValue("type");

         if (type == null)
         {
            throw new SAXException(helpLib.getMessageWithFallback(
             "error.xml.missing_attr_in_tag",
             "Missing ''{0}'' attribute in <{1}>", "type", qName));
         }

         String localeTag = attrs.getValue("locale");

         HelpsetFile hsf = new HelpsetFile(helpLib, ref, type,  
           localeTag == null ? null : Locale.forLanguageTag(localeTag));

         String encoding = attrs.getValue("encoding");

         if (encoding != null)
         {
            hsf.setEncoding(encoding);
         }

         helpset.add(hsf, true);

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

      if (Helpset.MANIFEST_CONTAINER.equals(qName))
      {
         endTagFound = true;
      }
      else if (HelpsetFile.ELEMENT_NAME.equals(qName))
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

   Helpset helpset;
   TeXJavaHelpLib helpLib;
   boolean manifestFound = false, endTagFound=false;
   String previousQname;
}
