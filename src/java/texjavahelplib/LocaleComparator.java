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

public class LocaleComparator implements Comparator<Locale>
{
   public LocaleComparator()
   {
   }

   @Override
   public int compare(Locale l1, Locale l2)
   {
      if (l1.equals(l2)) return 0;
                
      int result = -l1.getLanguage().compareTo(l2.getLanguage());

      if (result != 0)
      {
         return result;
      }

      int n1 = 0;
      int n2 = 0;

      if (!l1.getCountry().isEmpty()) n1++;
      if (!l2.getCountry().isEmpty()) n2++;

      if (!l1.getScript().isEmpty()) n1++;
      if (!l2.getScript().isEmpty()) n2++;

      if (!l1.getVariant().isEmpty()) n1++;
      if (!l2.getVariant().isEmpty()) n2++;

      if (n1 > n2) return -1;

      if (n1 < n2) return 1;

      return -l1.toLanguageTag().compareTo(l2.toLanguageTag());
   }

}
