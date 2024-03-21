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

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.xml.sax.SAXException;

import com.dickimawbooks.texparserlib.TeXApp;
import com.dickimawbooks.texparserlib.TeXParser;

public class TeXJavaHelpLib
{
   public TeXJavaHelpLib(TeXApp texApp) throws IOException
   {
      this(texApp, texApp.getApplicationName(),
         texApp.getApplicationName().toLowerCase(),
         "/resources");
   }

   public TeXJavaHelpLib(TeXApp texApp,
      String applicationName, String dictPrefix, String resourcebase)
    throws IOException
   {
      this.applicationName = applicationName;
      this.resourcebase = resourcebase;

      messages = new MessageSystem(getResourcePath(), "texjavahelplib");

      if (dictPrefix != null)
      {
         messages.loadDictionary(dictPrefix);
      }
   }

   public static String encodeHTML(String str, boolean encodeQuotes)
   {
      if (str.isEmpty()) return str;

      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < str.length(); )
      {
         int cp = str.codePointAt(i);
         i += Character.charCount(cp);

         if (cp == '&')
         {
            builder.append("&amp;");
         }
         else if (cp == '<')
         {
            builder.append("&lt;");
         }
         else if (cp == '>')
         {
            builder.append("&gt;");
         }
         else if (encodeQuotes && (cp == '"' || cp == '\''))
         {
            builder.append(String.format("&#x%x;", cp));
         }
         else
         {
            builder.appendCodePoint(cp);
         }
      }

      return builder.toString();
   }

   public MessageSystem getMessageSystem()
   {
      return messages;
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      return messages.getMessageWithFallback(label, fallbackFormat, params);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      return messages.getMessageIfExists(label, args);
   }

   public String getMessage(String label, Object... params)
   {
      String msg = messages.getMessage(label, params);

      if (msg == null)
      {
         warning("Can't find message for label: "+label);

         return label;
      }

      return msg;
   }

   public String getChoiceMessage(String label, int argIdx,
      String choiceLabel, int numChoices, Object... args)
   {
      return messages.getChoiceMessage(label, argIdx, choiceLabel, numChoices, args);
   }

   public void warning(String message)
   {
      if (texApp == null)
      {
         System.err.println(String.format("%s: %s", getApplicationName(), message));
      }
      else
      {
         texApp.warning(null, message);
      }
   }

   public void warning(TeXParser parser, String message)
   {
      if (texApp == null)
      {
         System.err.println(String.format("%s: %s", getApplicationName(), message));
      }
      else
      {
         texApp.warning(parser, message);
      }
   }

   public String getApplicationName()
   {
      return applicationName;
   }

   public void setTeXApp(TeXApp texApp)
   {
      this.texApp = texApp;
   }

   public TeXApp getTeXApp()
   {
      return texApp;
   }

   public String getResourcePath()
   {
      return resourcebase;
   }

   public String getHelpSetResourcePath()
   {
      return resourcebase + "/" + helpsetdir;
   }

   public InputStream getNavigationXMLInputStream()
     throws FileNotFoundException
   {
      String path = getHelpSetResourcePath() + "/" + navxmlfilename;

      InputStream stream = getClass().getResourceAsStream(path);

      if (stream == null)
      {
         throw new FileNotFoundException(
           getMessage("error.resource_not_found", path));
      }

      return stream;
   }

   public void initHelpSet()
    throws IOException,SAXException
   {
      initHelpSet("helpset", "navigation");
   }

   public void initHelpSet(String helpsetdir, String navBaseName)
    throws IOException,SAXException
   {
      navhtmlfilename = navBaseName+"."+htmlsuffix;
      navxmlfilename = navBaseName+".xml";

      navigationTree = NavigationTree.load(this);
   }

   protected String resourcebase = "/resources";

   protected String helpsetdir = "helpset";
   protected NavigationTree navigationTree;
   protected String navhtmlfilename, navxmlfilename;
   protected String htmlsuffix = "html";

   protected MessageSystem messages;
   protected String applicationName;
   protected TeXApp texApp;
}
