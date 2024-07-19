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

import java.util.IllformedLocaleException;
import java.util.Locale;

public class HelpSetLocale implements Comparable<HelpSetLocale>
{
   public HelpSetLocale(String tag)
     throws IllformedLocaleException
   {
      this.tag = tag;

      locale = new Locale.Builder().setLanguageTag(tag).build();
   }

   public HelpSetLocale(Locale locale)
   {
      this(locale.toLanguageTag(), locale);
   }

   public HelpSetLocale(String tag, Locale locale)
   {
      this.tag = tag;
      this.locale = locale;
   }

   public String getTag()
   {
      return tag;
   }

   public Locale getLocale()
   {
      return locale;
   }

   @Override
   public String toString()
   {
      return locale.getDisplayName();
   }

   @Override
   public boolean equals(Object other)
   {
      if (other == null || !(other instanceof HelpSetLocale))
      {
         return false;
      }

      return tag.equals(((HelpSetLocale)other).getTag());
   }

   @Override
   public int compareTo(HelpSetLocale other)
   {
      return toString().compareTo(other.toString());
   }

   String tag;
   Locale locale;
}
