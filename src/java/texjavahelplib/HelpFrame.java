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

import java.io.IOException;

import java.net.URL;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

/**
 * Frame for showing pages of the manual.
 */
public class HelpFrame extends JFrame
{
   public HelpFrame(TeXJavaHelpLib helpLib, String title)
     throws IOException
   {
      super(title);

      this.helpLib = helpLib;

      JMenuBar mBar = new JMenuBar();
      setJMenuBar(mBar);

      JMenu navMenu = helpLib.createJMenu("menu.navigation");
      mBar.add(navMenu);

      helpPage = new HelpPage(helpLib);

      getContentPane().add(new JScrollPane(helpPage), "Center");

      toolBar = new JToolBar();

      getContentPane().add(toolBar, "North");

      TJHAbstractAction homeAction = new TJHAbstractAction(helpLib,
        "navigation", "home",
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, ActionEvent.ALT_MASK))
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            homePage();
         }
      };

      toolBar.add(homeAction);
      navMenu.add(homeAction);

      TJHAbstractAction previousAction = new TJHAbstractAction(helpLib,
        "navigation", "previous",
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK))
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            prevPage();
         }
      };

      toolBar.add(previousAction);
      navMenu.add(previousAction);

      TJHAbstractAction upAction = new TJHAbstractAction(helpLib,
        "navigation", "up",
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK))
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            upPage();
         }
      };

      toolBar.add(upAction);
      navMenu.add(upAction);

      TJHAbstractAction nextAction = new TJHAbstractAction(helpLib,
        "navigation", "next",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK))
      {
         @Override
         public void actionPerformed(ActionEvent evt)
         {
            nextPage();
         }
      };

      toolBar.add(nextAction);
      navMenu.add(nextAction);

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width/2, dim.height/2);

   }

   public void setPage(NavigationNode node) throws IOException
   {
      helpPage.setPage(node);
   }

   public void prevPage()
   {
      try
      {
         helpPage.prevPage();
      }
      catch (IOException e)
      {
      }
   }

   public void nextPage()
   {
      try
      {
         helpPage.nextPage();
      }
      catch (IOException e)
      {
      }
   }

   public void upPage()
   {
      try
      {
         helpPage.upPage();
      }
      catch (IOException e)
      {
      }
   }

   public void homePage()
   {
      try
      {
         helpPage.homePage();
      }
      catch (IOException e)
      {
      }
   }

   protected TeXJavaHelpLib helpLib;
   protected HelpPage helpPage;
   protected JSplitPane splitPane;
   protected JToolBar toolBar;
}
