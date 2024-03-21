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

import java.util.Locale;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.xml.sax.SAXException;

import com.dickimawbooks.texparserlib.TeXApp;
import com.dickimawbooks.texparserlib.TeXParser;

public class TeXJavaHelpLib
{
   public TeXJavaHelpLib(TeXJavaHelpLibApp application) throws IOException
   {
      this(application, Locale.getDefault(), Locale.getDefault());
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
       Locale messagesLocale, Locale helpsetLocale)
     throws IOException
   {
      this(application, application.getApplicationName(),
         application.getApplicationName().toLowerCase().replaceAll(" ", ""),
         "/resources", messagesLocale, helpsetLocale);
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String dictPrefix, String resourcebase)
    throws IOException
   {
      this(application, applicationName, dictPrefix, resourcebase,
        Locale.getDefault(), Locale.getDefault());
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String dictPrefix, String resourcebase,
      Locale messagesLocale, Locale helpsetLocale)
    throws IOException
   {
      this.applicationName = applicationName;
      this.resourcebase = resourcebase;
      this.helpsetLocale = helpsetLocale;

      messages = new MessageSystem(getResourcePath(), "texjavahelplib", messagesLocale);

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
      if (application == null)
      {
         System.err.println(message);
      }
      else
      {
         application.warning(message);
      }
   }

   public void error(String message)
   {
      if (application == null)
      {
         System.err.println(message);
      }
      else
      {
         application.error(message);
      }
   }

   public void error(Exception e)
   {
      if (application == null)
      {
         String msg = e.getMessage();

         if (msg == null)
         {
            msg = e.toString();
         }

         System.err.println(msg);

         e.printStackTrace();
      }
      else
      {
         application.error(e);
      }
   }

   public void error(String message, Exception e)
   {
      if (application == null)
      {
         System.err.println(message);

         e.printStackTrace();
      }
      else
      {
         application.error(message, e);
      }
   }

   public String getApplicationName()
   {
      return applicationName;
   }

   public void setTeXJavaHelpLibApp(TeXJavaHelpLibApp application)
   {
      this.application = application;
   }

   public TeXJavaHelpLibApp getApplication()
   {
      return application;
   }

   public String getResourcePath()
   {
      return resourcebase;
   }

   public String getHelpSetResourcePath()
   {
      if (helpsetsubdir == null || helpsetsubdir.isEmpty())
      {
         return resourcebase + "/" + helpsetdir;
      }
      else
      {
         return resourcebase + "/" + helpsetdir + "/" + helpsetsubdir;
      }
   }

   public InputStream getNavigationXMLInputStream()
     throws FileNotFoundException
   {
      String path;
      InputStream stream = null;

      if (helpsetLocale == null || helpsetsubdir != null)
      {
         path = getHelpSetResourcePath() + "/" + navxmlfilename;
         stream = getClass().getResourceAsStream(path);
      }
      else
      {
         helpsetsubdir = helpsetLocale.toLanguageTag();

         path = getHelpSetResourcePath() + "/" + helpsetsubdir
            + "/" + navxmlfilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            String lang = helpsetLocale.getLanguage();
            String country = helpsetLocale.getCountry();

            if (country == null || country.isEmpty())
            {
               helpsetsubdir = lang;

               path = getHelpSetResourcePath() + "/" + helpsetsubdir
                  + "/" + navxmlfilename;
            }
            else
            {
               helpsetsubdir = lang + "-" + country;

               path = getHelpSetResourcePath() + "/" + helpsetsubdir
                  + "/" + navxmlfilename;

               stream = getClass().getResourceAsStream(path);

               if (stream == null)
               {
                  helpsetsubdir = lang;

                  path = getHelpSetResourcePath() + "/" + helpsetsubdir
                     + "/" + navxmlfilename;
               }
            }

            stream = getClass().getResourceAsStream(path);

            if (stream == null)
            {
               String script = helpsetLocale.getScript();

               if (script != null && !script.isEmpty())
               {
                  helpsetsubdir = lang + "-" + script;

                  path = getHelpSetResourcePath() + "/" + helpsetsubdir
                     + "/" + navxmlfilename;

                  stream = getClass().getResourceAsStream(path);
               }

               if (stream == null)
               {
                  path = getHelpSetResourcePath() + "/" + navxmlfilename;
                  helpsetsubdir = "";

                  stream = getClass().getResourceAsStream(path);
               }
            }
         }
      }

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
   protected String helpsetsubdir = null;
   protected Locale helpsetLocale;
   protected NavigationTree navigationTree;
   protected String navhtmlfilename, navxmlfilename;
   protected String htmlsuffix = "html";

   protected MessageSystem messages;
   protected String applicationName;
   protected TeXJavaHelpLibApp application;
}
