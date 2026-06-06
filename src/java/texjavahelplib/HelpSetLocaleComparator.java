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

import java.util.Comparator;
import java.util.Locale;

/**
 * A comparator that compares by language but ensures that more specific locales come first.
 */

public class HelpSetLocaleComparator implements Comparator<HelpSetLocale>
{
   public HelpSetLocaleComparator()
   {
      localeComparator = new LocaleComparator();
   }

   @Override
   public int compare(HelpSetLocale hsl1, HelpSetLocale hsl2)
   {
      return localeComparator.compare(hsl1.getLocale(), hsl2.getLocale());
   }

   LocaleComparator localeComparator;
}
