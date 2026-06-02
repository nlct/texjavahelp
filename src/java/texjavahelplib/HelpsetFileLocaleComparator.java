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
 * A comparator for ordering HelpsetFile lists by locale.
 */

public class HelpsetFileLocaleComparator implements Comparator<HelpsetFile>
{
   public HelpsetFileLocaleComparator()
   {
      comparator = new LocaleComparator();
   }

   @Override
   public int compare(HelpsetFile hsf1, HelpsetFile hsf2)
   {
      Locale l1 = hsf1.getLocale();
      Locale l2 = hsf2.getLocale();

      if (l1 == null && l2 == null) return 0;

      if (l1 == null) return 1;

      if (l2 == null) return -1;

      return comparator.compare(l1, l2);
   }


   LocaleComparator comparator;
}
