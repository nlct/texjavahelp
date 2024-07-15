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

import javax.swing.AbstractButton;
import javax.swing.Icon;

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
         setPressedIcon(pressedIcon);
      }

      if (disabledIcon != null)
      {
         setDisabledIcon(disabledIcon);
      }

      if (disabledSelectedIcon != null)
      {
         setDisabledSelectedIcon(disabledSelectedIcon);
      }
   }

   protected Icon defaultIcon, selectedIcon,
    rolloverIcon, rolloverSelectedIcon,
    pressedIcon, disabledIcon, disabledSelectedIcon;
}
