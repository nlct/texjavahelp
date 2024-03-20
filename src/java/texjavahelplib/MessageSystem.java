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

import java.util.Properties;
import java.util.Hashtable;
import java.util.Locale;

import java.text.MessageFormat;
import java.text.ChoiceFormat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

/**
 * Class for localised messages.
 */
public class MessageSystem extends Hashtable<String,MessageFormat>
{
   public MessageSystem(String tagPrefix) throws IOException
   {
      super();

      loadDictionary(tagPrefix);
   }

   protected String getLanguageFileName(String tagPrefix, String tag)
   {
      return String.format("/resources/%s-%s.xml", tagPrefix, tag);
   }

   protected URL getLanguageFile(String tagPrefix) throws FileNotFoundException
   {
      Locale locale = Locale.getDefault();
      String tag = locale.toLanguageTag();

      String dict = getLanguageFileName(tagPrefix, tag);

      URL url = getClass().getResource(dict);

      if (url != null) return url;

      String lang = locale.getLanguage();
      String region = locale.getCountry();

      if (!region.isEmpty())
      {
         tag = String.format("%s-%s", lang, region);

         dict = getLanguageFileName(tagPrefix, tag);
         url = getClass().getResource(dict);

         if (url != null) return url;
      }

      dict = getLanguageFileName(tagPrefix, lang);
      url = getClass().getResource(dict);

      if (url != null) return url;

      dict = getLanguageFileName(tagPrefix, "en");
      url = getClass().getResource(dict);

      if (url == null)
      {
         throw new FileNotFoundException
         (
            "Can't find dictionary resource file matching locale "+locale+" or fallback \"en\""
         );
      }

      return url;
   }

   public void loadDictionary(String tagPrefix)
      throws IOException
   {
      URL url = getLanguageFile(tagPrefix);

      InputStream in = null;

      try
      {
         in = url.openStream();

         Properties dictionary = new Properties();
         dictionary.loadFromXML(in);

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

}
