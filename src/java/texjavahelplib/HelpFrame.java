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
import java.awt.BorderLayout;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import javax.swing.tree.TreePath;

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

      init();
   }

   private void init() throws IOException
   {
      JMenuBar mBar = new JMenuBar();
      setJMenuBar(mBar);

      JMenu navMenu = helpLib.createJMenu("menu.navigation");
      mBar.add(navMenu);

      helpPage = new HelpPage(helpLib);

      navTree = new JTree(helpLib.getNavigationTree().getRoot());
      navTree.setEditable(false);
      navTree.setDragEnabled(false);
      navTree.addTreeSelectionListener(new TreeSelectionListener()
       {
          @Override
          public void valueChanged(TreeSelectionEvent evt)
          {
             TreePath path = navTree.getSelectionPath();

             if (path != null)
             {
                NavigationNode node = (NavigationNode)path.getLastPathComponent();

                if (!node.equals(helpPage.getCurrentNode()))
                {
                   try
                   {
                      setPage(node);
                   }
                   catch (IOException e)
                   {// shouldn't happen
                   }
                }
             }
          }
       });

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        new JScrollPane(navTree), new JScrollPane(helpPage));

      getContentPane().add(splitPane, "Center");

      JToolBar toolBar = new JToolBar();

      getContentPane().add(toolBar, "North");

      JPanel navPanel = new JPanel(new BorderLayout());
      getContentPane().add(navPanel, "South");

      TJHAbstractAction homeAction = new TJHAbstractAction(helpLib,
        "navigation", "home",
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, ActionEvent.ALT_MASK))
      {
         @Override
         public void doAction()
         {
            homePage();
         }
      };

      toolBar.add(homeAction);
      navMenu.add(homeAction);

      previousAction = new TJHAbstractAction(helpLib,
        "navigation", "previous",
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK))
      {
         @Override
         public void doAction()
         {
            prevPage();
         }
      };

      toolBar.add(previousAction);
      navMenu.add(previousAction);

      previousLabel = createNavLabel(previousAction, 
        SwingConstants.RIGHT, SwingConstants.LEFT);

      navPanel.add(previousLabel, "West");

      upAction = new TJHAbstractAction(helpLib,
        "navigation", "up",
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK))
      {
         @Override
         public void doAction()
         {
            upPage();
         }
      };

      toolBar.add(upAction);
      navMenu.add(upAction);

      upLabel = createNavLabel(upAction,
         SwingConstants.RIGHT, SwingConstants.CENTER);
      navPanel.add(upLabel, "Center");

      nextAction = new TJHAbstractAction(helpLib,
        "navigation", "next",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK))
      {
         @Override
         public void doAction()
         {
            nextPage();
         }
      };

      toolBar.add(nextAction);
      navMenu.add(nextAction);

      toolBar.addSeparator();

      // history

      helpHistoryFrame = new HelpHistoryFrame(this);

      historyAction = new TJHAbstractAction(helpLib,
        "navigation", "history",
        KeyStroke.getKeyStroke(KeyEvent.VK_H,
          ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK))
       {
         @Override
         public void doAction()
         {
            showHistoryFrame();
         }
       };

      toolBar.add(historyAction);
      navMenu.add(historyAction);

      historyBackAction = new TJHAbstractAction(helpLib,
        "navigation", "historyback",
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
          ActionEvent.ALT_MASK | ActionEvent.SHIFT_MASK))
       {
         @Override
         public void doAction()
         {
            historyBack();
         }
       };

      toolBar.add(historyBackAction);
      navMenu.add(historyBackAction);

      historyForwardAction = new TJHAbstractAction(helpLib,
        "navigation", "historyforward",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
          ActionEvent.ALT_MASK | ActionEvent.SHIFT_MASK))
       {
         @Override
         public void doAction()
         {
            historyForward();
         }
       };

      toolBar.add(historyForwardAction);
      navMenu.add(historyForwardAction);

      nextLabel = createNavLabel(nextAction,
         SwingConstants.LEFT, SwingConstants.RIGHT);
      navPanel.add(nextLabel, "East");

      updateNavWidgets();

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width/2, dim.height/2);

   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
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
         helpLib.debug(e);
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
         helpLib.debug(e);
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
         helpLib.debug(e);
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
         helpLib.debug(e);
      }
   }

   public void historyForward()
   {
      try
      {
         helpPage.historyForward();
      }
      catch (IOException e)
      {
         helpLib.debug(e);
      }
   }

   public void showHistoryFrame()
   {
      if (helpHistoryFrame.isVisible())
      {
         helpHistoryFrame.toFront();
      }
      else
      {
         helpHistoryFrame.setVisible(true);
         helpHistoryFrame.revalidateTable();
      }
   }

   public void historyBack()
   {
      try
      {
         helpPage.historyBack();
      }
      catch (IOException e)
      {
         helpLib.debug(e);
      }
   }

   public int getHistoryCount()
   {
      return helpPage.getHistoryCount();
   }

   public HistoryItem getHistoryItem(int idx)
   {
      return helpPage.getHistoryItem(idx);
   }

   public int getHistoryIndex()
   {
      return helpPage.getHistoryIndex();
   }

   protected JLabel createNavLabel(TJHAbstractAction action, int textPos, int hPos)
   {
      JLabel jlabel = new JLabel((ImageIcon)action.getValue(Action.SMALL_ICON));

      jlabel.setHorizontalTextPosition(textPos);
      jlabel.setHorizontalAlignment(hPos);

      jlabel.addMouseListener(new NavLabelMouseListener(action));

      String tooltip = (String)action.getValue(Action.SHORT_DESCRIPTION);

      if (tooltip != null)
      {
         jlabel.setToolTipText(tooltip);
      }

      return jlabel;
   }

   public void updateNavWidgets()
   {
      NavigationNode currentNode = helpPage.getCurrentNode();

      historyBackAction.setEnabled(helpPage.hasBackHistory());
      historyForwardAction.setEnabled(helpPage.hasForwardHistory());

      if (helpHistoryFrame.isVisible())
      {
         helpHistoryFrame.revalidateTable();
      }

      NavigationNode previousNode = currentNode.getPreviousNode();
      NavigationNode nextNode = currentNode.getNextNode();
      NavigationNode upNode = currentNode.getParentNode();

      if (previousNode == null)
      {
         previousAction.setEnabled(false);
         previousLabel.setEnabled(false);
         previousLabel.setText("");
      }
      else
      {
         previousAction.setEnabled(true);
         previousLabel.setEnabled(true);
         previousLabel.setText(previousNode.getTitle());
      }

      if (nextNode == null)
      {
         nextAction.setEnabled(false);
         nextLabel.setEnabled(false);
         nextLabel.setText("");
      }
      else
      {
         nextAction.setEnabled(true);
         nextLabel.setEnabled(true);
         nextLabel.setText(nextNode.getTitle());
      }

      if (upNode == null)
      {
         upAction.setEnabled(false);
         upLabel.setEnabled(false);
         upLabel.setText("");
      }
      else
      {
         upAction.setEnabled(true);
         upLabel.setEnabled(true);
         upLabel.setText(upNode.getTitle());
      }

      TreePath treePath = navTree.getSelectionPath();

      if (treePath == null
        || !currentNode.equals((NavigationNode)treePath.getLastPathComponent()))
      {
         navTree.setSelectionPath(currentNode.getTreePath());
      }
   }

   protected TeXJavaHelpLib helpLib;
   protected HelpPage helpPage;
   protected JSplitPane splitPane;
   protected JTree navTree;
   protected HelpHistoryFrame helpHistoryFrame;

   protected TJHAbstractAction previousAction, upAction, nextAction,
    historyAction, historyForwardAction, historyBackAction;
   protected JLabel previousLabel, upLabel, nextLabel;
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
