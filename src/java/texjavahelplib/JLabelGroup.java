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

import java.util.Vector;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Group of labels that should have the same preferred width that
 * matches the default preferred width of the widest label.
 * (To help with vertical alignment.) It's faster to have auto
 * update off, but that requires remembering to call
 * updateLabelWidths() after all labels have been added.
 */

public class JLabelGroup extends Vector<JLabel>
{
   public JLabelGroup()
   {
      this(true);
   }

   public JLabelGroup(boolean autoUpdate)
   {
      super();
      this.autoUpdate = autoUpdate;
   }

   public JLabelGroup(int capacity)
   {
      super(capacity);
   }

   public JLabel createJLabel(String text)
   {
      return createJLabel(text, -1, (String)null, (JComponent)null);
   }

   public JLabel createJLabel(String text, int mnemonic)
   {
      return createJLabel(text, mnemonic, (String)null, (JComponent)null);
   }

   public JLabel createJLabel(String text, int mnemonic, JComponent comp)
   {
      return createJLabel(text, mnemonic, (String)null, comp);
   }

   public JLabel createJLabel(String text, int mnemonic, 
     String tooltip, JComponent comp)
   {
      JLabel label = new JLabel(text);

      if (mnemonic > -1)
      {
         label.setDisplayedMnemonic(mnemonic);
      }

      if (tooltip != null)
      {
         label.setToolTipText(tooltip);
      }

      if (comp != null)
      {
         label.setLabelFor(comp);
      }

      add(label);

      return label;
   }

   public JLabel createJLabel(TeXJavaHelpLib helpLib,
     String tag, JComponent comp)
   {
      JLabel jlabel = helpLib.createJLabel(tag, comp);
      add(jlabel);
      return jlabel;
   }

   @Override
   public boolean add(JLabel label)
   {
      Dimension dim = label.getPreferredSize();

      if (dim.width > maxWidth)
      {
         maxWidth = dim.width;
         widestLabel = label;

         if (autoUpdate)
         {
            updateLabelWidths();
         }
      }
      else if (autoUpdate)
      {
         dim.width = maxWidth;
         label.setPreferredSize(dim);
      }

      return super.add(label);
   }

   public boolean isAutoUpdateOn()
   {
      return autoUpdate;
   }

   /**
    * Force update of label preferred sizes.
    * If auto update not set, this method needs to be called after
    * all labels have been added to the group.
    */ 
   public void updateLabelWidths()
   {
      for (JLabel label : this)
      {
         Dimension dim = label.getPreferredSize();
         dim.width = maxWidth;
         label.setPreferredSize(dim);
      }
   }

   public JLabel getWidestLabel()
   {
      return widestLabel;
   }

   private int maxWidth=0;
   private JLabel widestLabel;
   private boolean autoUpdate=true;
}
