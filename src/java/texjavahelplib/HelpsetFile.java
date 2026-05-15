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
import java.io.OutputStream;
import java.io.InputStream;

import java.nio.file.Path;

import java.net.URI;

import java.util.Locale;
import java.util.Vector;

import com.dickimawbooks.texparserlib.html.HtmlTag;

public class HelpsetFile
{
   public HelpsetFile(URI resourcesURI, String type)
   {
      this(resourcesURI, type, null, null);
   }

   public HelpsetFile(URI resourcesURI, String type,
      URI helpsetURI, Locale locale)
   {
      if (resourcesURI == null || type == null)
      {
         throw new NullPointerException();
      }

      this.resourcesURI = resourcesURI;
      this.type = type;
      this.helpsetURI = helpsetURI;
      this.locale = locale;
   }

   public URI getResourcesURI()
   {
      return resourcesURI;
   }

   public URI getHelpsetURI()
   {
      return helpsetURI;
   }

   public String getType()
   {
      return type;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public boolean hasLocale()
   {
      return locale != null;
   }

   public void setPath(Path path)
   {
      this.path = path;
   }

   public Path getPath()
   {
      return path;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public static boolean isSupportedType(String mimetype)
   {
      return mimetype.equals(TYPE_HTML)
          || mimetype.equals(TYPE_CSS)
          || mimetype.equals(TYPE_XML)
          || mimetype.equals(TYPE_PNG)
          || mimetype.equals(TYPE_JPEG);
   }

   public static void writeManifest(OutputStream out, Vector<HelpsetFile> files)
   throws IOException
   {
      byte[] byteArray = MANIFEST_XML_HEADER.getBytes();

      out.write(byteArray, 0, byteArray.length);

      byte[] eol = String.format("%n").getBytes();
      out.write(eol, 0, eol.length);

      byteArray = MANIFEST_HEADER.getBytes();
      out.write(byteArray, 0, byteArray.length);

      out.write(eol, 0, eol.length);

      for (HelpsetFile hs : files)
      {
         String str = String.format("<entry resource=\"%s\" type=\"%s\" ",
          hs.resourcesURI, hs.type);

         if (hs.helpsetURI != null)
         {
            str += String.format("helpset=\"%s\" ", hs.helpsetURI);
         }

         if (hs.locale != null)
         {
            str += String.format("locale=\"%s\" ", 
                    HtmlTag.encodeAttributeValue(hs.locale.toLanguageTag(), false, false));
         }

         if (hs.name != null)
         {
            str += String.format("name=\"%s\" ", 
                    HtmlTag.encodeAttributeValue(hs.name, false, false));
         }

         str += "/>";

         byteArray = str.getBytes();
         out.write(byteArray, 0, byteArray.length);

         out.write(eol, 0, eol.length);
      }

      byteArray = MANIFEST_FOOTER.getBytes();
      out.write(byteArray, 0, byteArray.length);
      out.write(eol, 0, eol.length);
   }

   public static Vector<HelpsetFile> readManifest(InputStream in)
      throws IOException
   {
      Vector<HelpsetFile> helpsetFiles = new Vector<HelpsetFile>();

// TODO
      return helpsetFiles;
   }

   @Override
   public String toString()
   {
      return resourcesURI.toString();
   }

   URI resourcesURI, helpsetURI;
   String type, name;
   Locale locale;

   Path path;

   public static final String TYPE_HTML="text/html",
    TYPE_CSS="text/css", TYPE_XML="text/xml",
    TYPE_PNG="image/png", TYPE_JPEG="image/jpeg";

   public static final String MANIFEST_XML_HEADER="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
   public static final String MANIFEST_HEADER="<helpset>";
   public static final String MANIFEST_FOOTER="</helpset>";
}
