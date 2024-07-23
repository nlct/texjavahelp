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

import java.awt.BorderLayout; 

import java.awt.event.MouseEvent; 
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class HelpPageNavPanel extends JPanel
  implements LowerNavSettingsChangeListener
{
   public HelpPageNavPanel(TeXJavaHelpLib helpLib,
     TJHAbstractAction previousAction,
     TJHAbstractAction upAction, TJHAbstractAction nextAction)
   {
      super(new BorderLayout());

      this.helpLib = helpLib;

      this.lowerNavLabelLimit = helpLib.getDefaultLowerNavLabelLimit();
      this.lowerNavLabelText = helpLib.isDefaultLowerNavLabelTextOn();

      helpLib.addLowerNavSettingsChangeListener(this);

      previousLabel = createNavLabel(previousAction,
        SwingConstants.RIGHT, SwingConstants.LEFT);
   
      add(previousLabel, "West");
   
      upLabel = createNavLabel(upAction, 
         SwingConstants.RIGHT, SwingConstants.CENTER);
      add(upLabel, "Center");

      nextLabel = createNavLabel(nextAction,
         SwingConstants.LEFT, SwingConstants.RIGHT);
      add(nextLabel, "East");
   }

   public void updatePreviousLabel(boolean enabled, String text)
   {
      previousLabel.setEnabled(enabled);
      previousLabel.setText(text);
   }

   public void updateNextLabel(boolean enabled, String text)
   {
      nextLabel.setEnabled(enabled);
      nextLabel.setText(text);
   }

   public void updateUpLabel(boolean enabled, String text)
   {
      upLabel.setEnabled(enabled);
      upLabel.setText(text);
   }

   protected LowerNavLabel createNavLabel(TJHAbstractAction action, int textPos, int hPos)
   {
      return new LowerNavLabel(this, action, textPos, hPos);
   }

   @Override
   public void lowerNavSettingsChange(LowerNavSettingsChangeEvent e)
   {
      this.lowerNavLabelText = e.isShowTextOn();
      this.lowerNavLabelLimit = e.getLimit();

      previousLabel.update();
      upLabel.update();
      nextLabel.update();
   }

   public boolean isLowerNavLabelTextOn()
   {
      return lowerNavLabelText;
   }

   public int getLowerNavLabelLimit()
   {
      return lowerNavLabelLimit;
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   protected LowerNavLabel previousLabel, upLabel, nextLabel;

   protected int lowerNavLabelLimit = 20;
   protected boolean lowerNavLabelText = true;
   protected TeXJavaHelpLib helpLib;
}

class LowerNavLabel extends JLabel
{
   public LowerNavLabel(HelpPageNavPanel navPanel, TJHAbstractAction action,
     int textPos, int hPos)
   {
      super((ImageIcon)action.getValue(Action.SMALL_ICON));

      this.navPanel = navPanel;

      setHorizontalTextPosition(textPos);
      setHorizontalAlignment(hPos);

      addMouseListener(new NavLabelMouseListener(action));

      shortDesc = (String)action.getValue(Action.SHORT_DESCRIPTION);

      if (shortDesc != null)
      {
         setToolTipText(shortDesc);
      }
   }

   public void update()
   {
      setText(originalText);
   }

   @Override
   public void setText(String text)
   {
      originalText = text;
      String label = text;

      if (navPanel == null)
      {
      }
      else if (navPanel.isLowerNavLabelTextOn())
      {
         int limit = navPanel.getLowerNavLabelLimit();

         if (text.length() > limit)
         {
            int idx = text.lastIndexOf(' ', limit);

            if (idx < 1)
            {
               idx = limit;
            }

            label = text.substring(0, idx)+"...";
         }
      }
      else
      {
         label = "";
      }

      super.setText(label);

      String desc = shortDesc;

      if (text != null && !text.isEmpty())
      {
         desc += ": " + text;
      }

      setToolTipText(desc);
   }

   protected String originalText, shortDesc;
   protected HelpPageNavPanel navPanel;
}

class NavLabelMouseListener implements MouseListener
{
   public NavLabelMouseListener(TJHAbstractAction action)
   {
      this.action = action;
   }

   @Override
   public void mouseClicked(MouseEvent e)
   {
      action.doAction();
   }

   @Override
   public void mouseEntered(MouseEvent e)
   {
   }

   @Override
   public void mouseExited(MouseEvent e)
   {
   }

   @Override
   public void mousePressed(MouseEvent e)
   {
   }

   @Override
   public void mouseReleased(MouseEvent e)
   {
   }

   TJHAbstractAction action;
}
