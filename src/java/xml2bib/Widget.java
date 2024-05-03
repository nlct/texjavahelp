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

import java.io.PrintWriter;
import java.io.IOException;

public class Widget extends Entry
{
   public Widget(String key)
   {
      this(key, null);
   }

   public Widget(String key, String value)
   {
      this(key, value, null);
   }

   public Widget(String key, String value, EntryType type)
   {
      super(key, value, type);
   }

   public void setMnemonic(String mnemonic)
   {
      this.mnemonic = mnemonic;
   }

   public String getMnemonic()
   {
      return mnemonic;
   }

   public void setKeyStroke(String keystroke)
   {
      this.keystroke = keystroke;
   }

   public String getKeyStroke()
   {
      return keystroke;
   }

   public void setToolTip(String tooltip)
   {
      this.tooltip = tooltip;
   }

   public String getToolTip()
   {
      return tooltip;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getDescription()
   {
      return description;
   }

   @Override
   public void writeExtraFields(PrintWriter out) throws IOException
   {
      if (description != null)
      {
         out.println(",");
         out.print("  description={");
         out.print(encode(description));
         out.print("}");
      }

      if (mnemonic != null)
      {
         out.println(",");
         out.print("  mnemonic={");
         out.print(encode(mnemonic));
         out.print("}");
      }

      if (keystroke != null)
      {
         out.println(",");
         out.print("  keystroke={");
         out.print(encode(keystroke));
         out.print("}");
      }

      if (tooltip != null)
      {
         out.println(",");
         out.print("  tooltip={");
         out.print(encode(tooltip));
         out.print("}");
      }

      out.println();
   }

   public String mnemonic, keystroke, tooltip, description;
}
