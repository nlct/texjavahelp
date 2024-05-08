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
package com.dickimawbooks.xml2bib;

public enum EntryType
{
   MENU("menu", "menufmt"),
   WIDGET("widget", "widgetfmt"),
   COMMON_WIDGET("commonwidget", "widgetfmt"),
   WINDOW("dialog", "dialogfmt"),
   MESSAGE("message", null),
   ERROR("error", null),
   WARNING("warning", null),
   SYNTAX("syntax", null),
   SYMBOL("symbol", "symbolfmt"),
   KEYSTROKE("keystroke", null),
   INDEX("index", null);

   EntryType(String type, String cmd)
   {
      this.type = type;
      this.cmd = cmd;
   }

   public static EntryType getTypeFromKey(String key)
   {
      int idx = key.indexOf('.');

      if (idx > 0)
      {
         String prefix = key.substring(0, idx);

         if (prefix.endsWith("menu"))
         {
            return MENU;
         }

         if (prefix.equals("action"))
         {
            String str = key.substring(idx+1);

            if (str.equals("okay")
              ||str.equals("cancel")
              ||str.equals("apply")
              ||str.equals("close")
               )
            {
               return COMMON_WIDGET;
            }
            else
            {
               return WIDGET;
            }
         }

         if (prefix.equals("widget"))
         {
            return WIDGET;
         }

         if (prefix.equals("message"))
         {
            return MESSAGE;
         }

         if (prefix.equals("error"))
         {
            return ERROR;
         }

         if (prefix.equals("warning"))
         {
            return WARNING;
         }

         if (prefix.equals("syntax"))
         {
            return SYNTAX;
         }

         if (key.endsWith("title"))
         {
            return WINDOW;
         }

         if (prefix.equals("symbol"))
         {
            return SYMBOL;
         }

         if (prefix.equals("manual"))
         {
            String str = key.substring(idx+1);

            if (str.startsWith("keystroke"))
            {
               return KEYSTROKE;
            }
            else
            {
               return INDEX;
            }
         }

         if (prefix.equals("text") || prefix.equals("index"))
         {
            return INDEX;
         }

         return WIDGET;
      }
      else
      {
         return INDEX;
      }
   }

   public String getBibType()
   {
      return type;
   }

   public String getEncap()
   {
      return cmd;
   }

   protected final String type, cmd;
}
