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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

public abstract class TJHAbstractAction extends AbstractAction
{
   public TJHAbstractAction(TeXJavaHelpLib helpLib, String action)
   {
      this(helpLib, null, action);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action)
   {
      this(helpLib, parentTag, action, null);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke)
   {
      this(helpLib, parentTag, action, keyStroke, null);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, Boolean selectedState)
   {
      super();

      putValue(ACTION_COMMAND_KEY, action);

      if (keyStroke != null)
      {
         putValue(ACCELERATOR_KEY, keyStroke);
      }

      if (selectedState != null)
      {
         putValue(SELECTED_KEY, selectedState);
      }

      String tag = parentTag == null ? action : parentTag+"."+action;

      String text = helpLib.getMessageIfExists(tag);

      if (text != null)
      {
         putValue(NAME, text);
      }

      int mnemonic = helpLib.getMnemonic(tag+".mnemonic");

      if (mnemonic > 0)
      {
         putValue(MNEMONIC_KEY, Integer.valueOf(mnemonic));
      }

      String tooltip = helpLib.getMessageIfExists(tag+".tooltip");

      if (tooltip != null)
      {
         putValue(SHORT_DESCRIPTION, tooltip);
      }

      String desc = helpLib.getMessageIfExists(tag+".description");

      if (desc != null)
      {
         putValue(LONG_DESCRIPTION, tooltip);
      }

      ImageIcon ic = helpLib.getLargeIcon(action);

      if (ic != null)
      {
         putValue(LARGE_ICON_KEY, ic);
      }

      ic = helpLib.getSmallIcon(action);

      if (ic != null)
      {
         putValue(SMALL_ICON, ic);
      }

   }

}
