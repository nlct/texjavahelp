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

import java.awt.Color;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class IconSet
{
   public IconSet(Icon defaultIcon)
   {
      this.defaultIcon = defaultIcon;
   }

   public Icon getDefaultIcon()
   {
      return defaultIcon;
   }

   public Icon getSelectedIcon()
   {
      return selectedIcon;
   }

   public Icon getRolloverIcon()
   {
      return rolloverIcon;
   }

   public Icon getRolloverSelectedIcon()
   {
      return rolloverSelectedIcon;
   }

   public Icon getPressedIcon()
   {
      return pressedIcon;
   }

   public Icon getDisabledIcon()
   {
      return disabledIcon;
   }

   public Icon getDisabledSelectedIcon()
   {
      return disabledSelectedIcon;
   }

   public void setDefaultIcon(Icon ic)
   {
      defaultIcon = ic;
   }

   public void setSelectedIcon(Icon ic)
   {
      selectedIcon = ic;
   }

   public void setRolloverIcon(Icon ic)
   {
      rolloverIcon = ic;
   }

   public void setRolloverSelectedIcon(Icon ic)
   {
      rolloverSelectedIcon = ic;
   }

   public void setPressedIcon(Icon ic)
   {
      pressedIcon = ic;
   }

   public void setDisabledIcon(Icon ic)
   {
      disabledIcon = ic;
   }

   public void setDisabledSelectedIcon(Icon ic)
   {
      disabledSelectedIcon = ic;
   }

   public JRadioButton createIconRadioButton()
   {
      JRadioButton btn = new JRadioButton(defaultIcon);
      setButtonExtraIcons(btn);

      if (selectedIcon == null)
      {
         btn.addChangeListener(new ChangeListener()
          {
             @Override
             public void stateChanged(ChangeEvent evt)
             {
                JRadioButton btn = (JRadioButton)evt.getSource();

                if (btn.isSelected())
                {
                   if (!btn.isContentAreaFilled())
                   {
                      btn.setContentAreaFilled(true);
                   }
                }
                else if (btn.isContentAreaFilled())
                {
                   btn.setContentAreaFilled(false);
                }
             }
          });
         btn.setBackground(HIGHLIGHT);
         btn.setContentAreaFilled(false);
      }

      return btn;
   }

   public JCheckBox createIconCheckBox()
   {
      JCheckBox btn = new JCheckBox(defaultIcon);
      setButtonExtraIcons(btn);

      if (selectedIcon == null)
      {
         btn.addChangeListener(new ChangeListener()
          {
             @Override
             public void stateChanged(ChangeEvent evt)
             {
                JRadioButton btn = (JRadioButton)evt.getSource();

                if (btn.isSelected())
                {
                   if (!btn.isContentAreaFilled())
                   {
                      btn.setContentAreaFilled(true);
                   }
                }
                else if (btn.isContentAreaFilled())
                {
                   btn.setContentAreaFilled(false);
                }
             }
          });
         btn.setBackground(HIGHLIGHT);
         btn.setContentAreaFilled(false);
      }

      return btn;
   }

   public void setButtonIcons(AbstractButton button)
   {
      if (defaultIcon != null)
      {
         button.setIcon(defaultIcon);
      }

      setButtonExtraIcons(button);
   }

   public void setButtonExtraIcons(AbstractButton button)
   {
      if (selectedIcon != null)
      {
         button.setSelectedIcon(selectedIcon);
      }

      if (rolloverIcon != null)
      {
         button.setRolloverIcon(rolloverIcon);
      }

      if (rolloverSelectedIcon != null)
      {
         button.setRolloverSelectedIcon(rolloverSelectedIcon);
      }

      if (pressedIcon != null)
      {
         button.setPressedIcon(pressedIcon);
      }

      if (disabledIcon != null)
      {
         button.setDisabledIcon(disabledIcon);
      }

      if (disabledSelectedIcon != null)
      {
         button.setDisabledSelectedIcon(disabledSelectedIcon);
      }
   }

   public String toString()
   {
      return String.format(
        "%s[default=%s,selected=%s,pressed=%s,rollover=%s,rolloverSelected=%s,disabled=%s,disabledSelected=%s]",
        getClass().getSimpleName(), defaultIcon, selectedIcon,
        pressedIcon, rolloverIcon, rolloverSelectedIcon,
        disabledIcon, disabledSelectedIcon);
   }

   protected Icon defaultIcon, selectedIcon,
    rolloverIcon, rolloverSelectedIcon,
    pressedIcon, disabledIcon, disabledSelectedIcon;

   public static Color HIGHLIGHT = Color.YELLOW;
}
