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
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.text.html.StyleSheet;

import java.nio.file.Path;
import java.nio.file.Files;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.Locale;
import java.util.Vector;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.dickimawbooks.texparserlib.html.HtmlTag;

public class HelpsetFile
{
   public HelpsetFile(TeXJavaHelpLib helpLib, String ref, String type)
   {
      this(helpLib, ref, type, null);
   }

   public HelpsetFile(TeXJavaHelpLib helpLib, String ref, String type, Locale locale)
   {
      if (ref == null || type == null)
      {
         throw new NullPointerException();
      }

      this.helpLib = helpLib;
      this.ref = ref;
      this.type = type;
      this.locale = locale;
   }

   public String getRef()
   {
      return ref;
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

   public void setNameFrom(Path path)
   {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < path.getNameCount(); i++)
      {
         if (i > 0) builder.append('/');

         builder.append(path.getName(i).toString());
      }

      setName(builder.toString());
   }

   public String getName()
   {
      return name;
   }

   public void setEncoding(String encodingName)
   {
      encoding = encodingName;
   }

   public String getEncoding()
   {
      return encoding;
   }

   public void setEncodingFromPath() throws IOException
   {
      if (type.equals(TYPE_XML))
      {
         FileInputStream in = null;

         try
         {
            in = new FileInputStream(path.toFile());

            byte[] bytes = new byte[256];

            int len = in.read(bytes);

            if (len > 0)
            {
               Matcher m = Helpset.XML_HEADER_ENCODING_PATTERN.matcher(new String(bytes));

               if (m.lookingAt())
               {
                  encoding = m.group(1);
               }
            }
         }
         finally
         {
            if (in != null)
            {
               in.close();
            }
         }
      }
      else if (type.equals(TYPE_HTML))
      {
         BufferedReader in = null;

         try
         {
            in = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1);

            String line;

            while ((line = in.readLine()) != null)
            {
               Matcher m = HTML_META_CHARSET_PATTERN.matcher(line);

               if (m.find())
               {
                  encoding = m.group(1);
                  break;
               }

               m = HTML_CONTENT_CHARSET_PATTERN.matcher(line);

               if (m.find())
               {
                  encoding = m.group(1);
                  break;
               }
            }

            if (encoding == null)
            {
               encoding = "UTF-8";
            }
         }
         finally
         {
            if (in != null)
            {
               in.close();
            }
         }
      }
      else if (type.equals(TYPE_CSS))
      {
         BufferedReader in = null;

         try
         {
            in = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1);

            String line;

            while ((line = in.readLine()) != null)
            {
               Matcher m = CSS_CHARSET_PATTERN.matcher(line);

               if (m.find())
               {
                  encoding = m.group(1);
                  break;
               }
            }

            if (encoding == null)
            {
               encoding = "UTF-8";
            }
         }
         finally
         {
            if (in != null)
            {
               in.close();
            }
         }
      }
   }

   public void setByteContent(byte[] content)
   {
      byteContent = content;
   }

   public boolean hasContent()
   {
      return byteContent != null;
   }

   public boolean isTextContent()
   {
      return type.startsWith("text/");
   }

   public boolean isImageContent()
   {
      return type.startsWith("image/");
   }

   public String getStringContent()
     throws InvalidContentTypeException,UnsupportedEncodingException
   {
      if (isTextContent())
      {
         return byteContent == null ? "" :
           new String(byteContent, encoding == null ? "UTF-8" : encoding);
      }
      else
      {
         throw new InvalidContentTypeException(
           helpLib.getMessageWithFallback(
            "error.content_type_not_textual",
            "{0}: Content type {1} is not textual",
            ref, type));
      }
   }

   public BufferedImage getImage() throws IOException
   {
      if (image != null || !hasContent()) return image;

      if (isImageContent())
      {
         image = ImageIO.read(getInputStream());

         return image;
      }
      else
      {
         throw new InvalidContentTypeException(
           helpLib.getMessageWithFallback(
            "error.content_type_not_image",
            "{0}: Content type {1} is not an image",
            ref, type));
      }
   }

   public StyleSheet getStyleSheet() throws IOException
   {
      if (stylesheet != null || !hasContent()) return stylesheet;

      if (type.equals(TYPE_CSS))
      {
         stylesheet = new StyleSheet();

         stylesheet.loadRules(getStringReader(), null);

         return stylesheet;
      }
      else
      {
         throw new InvalidContentTypeException(
           helpLib.getMessageWithFallback(
            "error.content_type_not_stylesheet",
            "{0}: Content type {1} is not a style sheet",
            ref, type));
      }
   }

   public StringReader getStringReader()
     throws InvalidContentTypeException,UnsupportedEncodingException
   {
      return new StringReader(getStringContent());
   }

   public InputStream getInputStream()
   {
      return new ByteArrayInputStream(byteContent);
   }

   public static boolean isSupportedType(String mimetype)
   {
      return mimetype.equals(TYPE_HTML)
          || mimetype.equals(TYPE_CSS)
          || mimetype.equals(TYPE_XML)
          || mimetype.equals(TYPE_PNG)
          || mimetype.equals(TYPE_JPEG);
   }

   public void writeManifestEntry(OutputStream out)
   throws IOException
   {
      String str = String.format("<%s ref=\"%s\" type=\"%s\" ",
          ELEMENT_NAME, ref, type);

      if (locale != null)
      {
         str += String.format("locale=\"%s\" ", 
                 HtmlTag.encodeAttributeValue(locale.toLanguageTag(), false, false));
      }

      if (encoding != null)
      {
         str += String.format("encoding=\"%s\" ", 
                 HtmlTag.encodeAttributeValue(encoding, false, false));
      }

      str += "/>";

      byte[] byteArray = str.getBytes();
      out.write(byteArray, 0, byteArray.length);
   }

   @Override
   public String toString()
   {
      return ref.toString();
   }

   String ref;
   String type, name, encoding;
   Locale locale;

   Path path;
   byte[] byteContent;

   BufferedImage image = null;
   StyleSheet stylesheet = null;

   TeXJavaHelpLib helpLib;

   public static final String ELEMENT_NAME = "entry";

   public static final String TYPE_HTML="text/html",
    TYPE_CSS="text/css", TYPE_XML="text/xml",
    TYPE_PNG="image/png", TYPE_JPEG="image/jpeg";

   public static final Pattern CSS_CHARSET_PATTERN = 
     Pattern.compile("\\@charset\\s+\"([^\"]+)\"\\s+;");

   public static final Pattern HTML_META_CHARSET_PATTERN =
     Pattern.compile("<meta\\s+charset=\"([^\"]+)\"\\s*>");

   public static final Pattern HTML_CONTENT_CHARSET_PATTERN =
     Pattern.compile("<meta\\s+.*?content\\s*=\\s*\".+?;\\s+charset=([^\"]+)\"\\s*/?>");
}
