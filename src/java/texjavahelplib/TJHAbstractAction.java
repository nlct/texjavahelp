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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.JComponent;

public abstract class TJHAbstractAction extends AbstractAction
{
   public TJHAbstractAction(TeXJavaHelpLib helpLib, String action)
   {
      this(helpLib, null, action);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action)
   {
      this(helpLib, parentTag, action, (Boolean)null, (JComponent)null);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action, JComponent comp)
   {
      this(helpLib, parentTag, action, (Boolean)null, comp);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action, Boolean selectedState)
   {
      this(helpLib, parentTag, action, selectedState, (JComponent)null);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action, Boolean selectedState, JComponent comp)
   {
      this(helpLib, parentTag, action, 
       helpLib.getKeyStroke(action == null ? parentTag : parentTag+"."+action),
       selectedState, comp);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke)
   {
      this(helpLib, parentTag, action, keyStroke, null, null);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, JComponent comp)
   {
      this(helpLib, parentTag, action, keyStroke, null, comp);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, Boolean selectedState, JComponent comp)
   {
      this(helpLib, parentTag, action, action, action, keyStroke, selectedState, comp);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, String iconPrefix,
     KeyStroke keyStroke, Boolean selectedState, JComponent comp)
   {
      super();

      this.helpLib = helpLib;

      if (actionName != null)
      {
         putValue(ACTION_COMMAND_KEY, actionName);
      }

      if (keyStroke != null)
      {
         putValue(ACCELERATOR_KEY, keyStroke);

         if (actionName != null && comp != null)
         {
            comp.getInputMap().put(keyStroke, actionName);
            comp.getActionMap().put(actionName, this);
         }
      }

      if (selectedState != null)
      {
         putValue(SELECTED_KEY, selectedState);
      }

      String tag = parentTag == null ? childTag : parentTag+"."+childTag;

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

      ImageIcon ic = helpLib.getLargeIcon(iconPrefix);

      if (ic != null)
      {
         putValue(LARGE_ICON_KEY, ic);
      }

      ic = helpLib.getSmallIcon(iconPrefix);

      if (ic != null)
      {
         putValue(SMALL_ICON, ic);
      }

   }

   public abstract void doAction();

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      doAction();
   }

   protected TeXJavaHelpLib helpLib;
}
