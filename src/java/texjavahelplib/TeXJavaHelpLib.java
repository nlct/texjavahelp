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

public class TeXJavaHelpLib
{

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
}
