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
import javax.swing.Icon;
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

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag,
      String action, JComponent comp, String... omitKeys)
   {
      this(helpLib, parentTag, action, (Boolean)null, comp, omitKeys);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag,
      String action, Boolean selectedState, String... omitKeys)
   {
      this(helpLib, parentTag, action, selectedState, (JComponent)null,
      omitKeys);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag,
      String action, Boolean selectedState, JComponent comp,
      String... omitKeys)
   {
      this(helpLib, parentTag, action, 
       helpLib.getKeyStroke(action == null ? parentTag : parentTag+"."+action),
       selectedState, comp, omitKeys);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, String... omitKeys)
   {
      this(helpLib, parentTag, action, keyStroke, null, null, omitKeys);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, JComponent comp, String... omitKeys)
   {
      this(helpLib, parentTag, action, keyStroke, null, comp, omitKeys);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, Boolean selectedState, JComponent comp,
     String... omitKeys)
   {
      this(helpLib, parentTag, action, action, action, keyStroke,
         selectedState, comp, omitKeys);
   }

   public TJHAbstractAction(TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, String iconPrefix,
     KeyStroke keyStroke, Boolean selectedState, JComponent comp,
     String... omitKeys)
   {
      super();

      this.helpLib = helpLib;

      boolean setCommand = true;
      boolean setAccelerator = true;
      boolean setSelected = true;
      boolean setName = true;
      boolean setMnemonic = true;
      boolean setShortDesc = true;
      boolean setLongDesc = true;
      boolean setLargeIcon = true;
      boolean setSmallIcon = true;

      for (String key : omitKeys)
      {
         if (key.equals(ACTION_COMMAND_KEY))
         {
            setCommand = false;
         }
         else if (key.equals(ACCELERATOR_KEY))
         {
            setAccelerator = false;
         }
         else if (key.equals(SELECTED_KEY))
         {
            setSelected = false;
         }
         else if (key.equals(NAME))
         {
            setName = false;
         }
         else if (key.equals(MNEMONIC_KEY))
         {
            setMnemonic = false;
         }
         else if (key.equals(SHORT_DESCRIPTION))
         {
            setShortDesc = false;
         }
         else if (key.equals(LONG_DESCRIPTION))
         {
            setLongDesc = false;
         }
         else if (key.equals(LARGE_ICON_KEY))
         {
            setLargeIcon = false;
         }
         else if (key.equals(SMALL_ICON))
         {
            setSmallIcon = false;
         }
      }

      if (setCommand && actionName != null)
      {
         putValue(ACTION_COMMAND_KEY, actionName);
      }

      if (setAccelerator && keyStroke != null)
      {
         putValue(ACCELERATOR_KEY, keyStroke);

         if (actionName != null && comp != null)
         {
            comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(keyStroke, actionName);
            comp.getActionMap().put(actionName, this);
         }
      }

      if (setSelected && selectedState != null)
      {
         putValue(SELECTED_KEY, selectedState);
      }

      String tag = parentTag == null ? childTag : parentTag+"."+childTag;

      if (setName)
      {
         String text = helpLib.getMessageIfExists(tag);

         if (text != null)
         {
            putValue(NAME, text);
            displayName = text;
         }
      }

      if (setMnemonic)
      {
         int mnemonic = helpLib.getMnemonic(tag+".mnemonic");

         if (mnemonic > 0)
         {
            putValue(MNEMONIC_KEY, Integer.valueOf(mnemonic));
         }
      }

      if (setShortDesc)
      {
         String tooltip = helpLib.getMessageIfExists(tag+".tooltip");

         if (tooltip != null)
         {
            putValue(SHORT_DESCRIPTION, tooltip);

            if (displayName == null)
            {
               displayName = tooltip;
            }
         }
      }

      if (setLongDesc)
      {
         String desc = helpLib.getMessageIfExists(tag+".description");

         if (desc != null)
         {
            putValue(LONG_DESCRIPTION, desc);
         }
      }

      Icon ic;

      if (setLargeIcon)
      {
         largeIconSet = helpLib.getHelpIconSet(iconPrefix, false);

         if (largeIconSet != null)
         {
            ic = largeIconSet.getDefaultIcon();

            if (ic != null)
            {
               putValue(LARGE_ICON_KEY, ic);
            }
         }
      }

      if (setSmallIcon)
      {
         smallIconSet = helpLib.getHelpIconSet(iconPrefix, true);

         if (smallIconSet != null)
         {
            ic = smallIconSet.getDefaultIcon();

            if (ic != null)
            {
               putValue(SMALL_ICON, ic);
            }
         }
      }

      if (displayName == null)
      {
         displayName = (actionName == null ? childTag : actionName);
      }
   }

   public void setToolTipText(String tooltip)
   {
      putValue(SHORT_DESCRIPTION, tooltip);
   }

   public String getDisplayName()
   {
      return displayName;
   }

   public abstract void doAction();

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      doAction();
   }

   public IconSet getIconSet()
   {
      if (largeIconSet == null)
      {
         return smallIconSet;
      }

      return largeIconSet;
   }

   public IconSet getLargeIconSet()
   {
      return largeIconSet;
   }

   public IconSet getSmallIconSet()
   {
      return smallIconSet;
   }

   protected TeXJavaHelpLib helpLib;
   protected String displayName;
   protected IconSet largeIconSet, smallIconSet;
}
