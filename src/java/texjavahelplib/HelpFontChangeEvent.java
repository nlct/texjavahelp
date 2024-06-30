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

public class HelpFontChangeEvent
{
   public HelpFontChangeEvent(Object source, HelpFontSettings settings)
   {
      this(source, settings, Integer.MAX_VALUE);
   }

   public HelpFontChangeEvent(Object source, HelpFontSettings fontSettings,
      int modifiers)
   {
      this.source = source;
      this.fontSettings = fontSettings;
      this.modifiers = modifiers;
   }

   public HelpFontSettings getSettings()
   {
      return fontSettings;
   }

   public boolean isConsumed()
   {
      return consumed;
   }

   public Object getSource()
   {
      return source;
   }

   public int getModifiers()
   {
      return modifiers;
   }

   protected Object source;
   protected HelpFontSettings fontSettings;
   protected boolean consumed=false;
   protected int modifiers;

   public static final int BODY_SIZE=1;
   public static final int BODY_FONT=2;
   public static final int ICON_FONT=4;
   public static final int KEYSTROKE_FONT=8;
   public static final int MONO_FONT=16;
}
