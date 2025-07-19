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

import java.util.ArrayDeque;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.ChoiceFormat;
import java.text.MessageFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URISyntaxException;

/**
 * Class for localised messages.
 */
public class MessageSystem extends Hashtable<String,MessageFormat>
{
   public MessageSystem(TeXJavaHelpLib helpLib, String tagPrefix)
       throws IOException
   {
      this(helpLib, tagPrefix, Locale.getDefault());
   }

   public MessageSystem(TeXJavaHelpLib helpLib, String tagPrefix, Locale locale)
     throws IOException
   {
      this(helpLib, tagPrefix, new HelpSetLocale(locale));
   }

   public MessageSystem(TeXJavaHelpLib helpLib, String tagPrefix, 
      HelpSetLocale hsLocale)
     throws IOException
   {
      super();

      this.helpLib = helpLib;
      this.hsLocale = hsLocale;

      loadDictionary(tagPrefix);
   }

   protected void initDictPattern()
   {
      dictionaryPattern = Pattern.compile(
         ".+-([a-z]{2,3}(?:-[A-Z]{2})?(?:-[A-Z][a-z]{3})?)\\.(?:xml|prop)");
   }

   public void setDictionaryPattern(Pattern p)
   {
      availableDictionaries = null;
      dictionaryPattern = p;
   }

   public Pattern getDictionaryPattern()
   {
      if (dictionaryPattern == null)
      {
         initDictPattern();
      }

      return dictionaryPattern;
   }

   public Vector<HelpSetLocale> getDictionaries()
    throws URISyntaxException,FileNotFoundException
   {
      if (availableDictionaries == null)
      {
         String path = helpLib.getDictionaryPath();
         URL url = getClass().getResource(path);

         if (url == null)
         {
            throw new FileNotFoundException(getMessageWithFallback(
             "error.resource_not_found", "Resource file ''{0}'' not found",
             path));
         }

         File dir = new File(url.toURI());

         String[] list = dir.list();

         availableDictionaries = new Vector<HelpSetLocale>();

         for (String filename : list)
         {
            Matcher m = getDictionaryPattern().matcher(filename);

            if (m.matches())
            {
               HelpSetLocale hs = new HelpSetLocale(m.group(1));

               if (!availableDictionaries.contains(hs))
               {
                  availableDictionaries.add(hs);
               }
            }
         }

         availableDictionaries.sort(null);
      }

      return availableDictionaries;
   }

   protected String getLanguageFileName(String tag)
   {
      return String.format("%s/%s-%s.xml",
        helpLib.getDictionaryPath(), tagPrefix, tag);
   }

   /**
     Gets the list of available resource files for the specified locale.
     This returns a list to allow more specific locales to override more general
     locales. For example, prefix-en.xml may have the default for all keys
     and prefix-en-GB.xml overrides just the different keys specific to GB.
     @return list of available language files
    */ 
   protected ArrayDeque<URL> getLanguageFiles()
     throws FileNotFoundException
   {
      String tag, dict;
      URL url;

      Locale locale = hsLocale.getLocale();
      String lang = locale.getLanguage();

      ArrayDeque<URL> deque = new ArrayDeque<URL>();

      // preferred tag
      tag = hsLocale.getTag();
      dict = getLanguageFileName(tag);
      url = getClass().getResource(dict);

      if (url != null)
      {
         deque.addFirst(url);
         url = null;
      }

      tag = locale.toLanguageTag();

      if (!tag.equals(hsLocale.getTag()))
      {
         dict = getLanguageFileName(tag);
         url = getClass().getResource(dict);

         if (url != null)
         {
            if (!deque.contains(url))
            {
               deque.addFirst(url);
            }

            url = null;
         }
      }

      String region = locale.getCountry();

      if (!region.isEmpty())
      {
         tag = String.format("%s-%s", lang, region);

         dict = getLanguageFileName(tag);
         url = getClass().getResource(dict);

         if (url != null)
         {
            if (!deque.contains(url))
            {
               deque.addFirst(url);
            }

            url = null;
         }
      }

      dict = getLanguageFileName(lang);
      url = getClass().getResource(dict);

      if (url != null)
      {
         if (!deque.contains(url))
         {
            deque.addFirst(url);
         }

         url = null;
      }

      return deque;
   }

   public void loadDictionary(String tagPrefix)
      throws IOException
   {
      this.tagPrefix = tagPrefix;

      ArrayDeque<URL> deque = getLanguageFiles();

      if (deque.isEmpty())
      {
         HelpSetLocale orgLocale = hsLocale;
         hsLocale = new HelpSetLocale("en", Locale.ENGLISH);
         deque = getLanguageFiles();

         if (deque.isEmpty())
         {
            throw new FileNotFoundException
            (
               String.format(
               "Can't find dictionary resource file for locale \"%s\" or fallback \"%s\" matching \"%s\"",
                orgLocale.getTag(), hsLocale.getTag(), getLanguageFileName("*")
               )
            );
         }
      }

      InputStream in = null;

      try
      {
         URL url = null;

         Properties dictionary = new Properties();

         while ((url = deque.pollFirst()) != null)
         {
            helpLib.message("Loading "+url);

            in = url.openStream();

            dictionary.loadFromXML(in);

            in.close();
            in = null;

            helpLib.dictionaryLoaded(url);
         }

         for (Object key : dictionary.keySet())
         {
            put((String)key, new MessageFormat((String)dictionary.get(key)));
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

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      MessageFormat fmt = get(label);

      if (fmt == null)
      {
         if (fallbackFormat == null || fallbackFormat.isEmpty())
         {
            return fallbackFormat;
         }

         fmt = new MessageFormat(fallbackFormat);
      }

      return fmt.format(params);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      MessageFormat msg = get(label);

      if (msg == null)
      {
         return null;
      }

      return msg.format(args);
   }

   public String getMessage(String label, Object... args)
   {
      MessageFormat msg = get(label);

      if (msg == null)
      {
         return null;
      }

      return msg.format(args);
   }

   public String getChoiceMessage(String label, int argIdx,
      String choiceLabel, int numChoices, Object... args)
   {
      String[] part = new String[numChoices];

      double[] limits = new double[numChoices];

      for (int i = 0; i < numChoices; i++)
      {
         String tag = String.format("message.%d.%s", i, choiceLabel);

         MessageFormat fmt = get(tag);

         if (fmt == null)
         {
            throw new IllegalArgumentException(
             "Invalid message label: "+tag);
         }

         part[i] = fmt.toPattern();
         limits[i] = i;
      }

      MessageFormat fmt = get(label);

      if (fmt == null)
      {
         throw new IllegalArgumentException(
          "Invalid message label: "+label);
      }

      ChoiceFormat choiceFmt = new ChoiceFormat(limits, part);

      fmt.setFormatByArgumentIndex(argIdx, choiceFmt);

      return fmt.format(args);
   }

   public HelpSetLocale getLocale()
   {
      return hsLocale;
   }

   protected TeXJavaHelpLib helpLib;
   protected HelpSetLocale hsLocale;
   protected String tagPrefix;
   protected Pattern dictionaryPattern;
   protected Vector<HelpSetLocale> availableDictionaries;
}
