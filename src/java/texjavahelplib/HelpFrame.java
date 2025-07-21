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

import java.util.List;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import javax.swing.tree.TreePath;

/**
 * Frame for showing pages of the manual.
 */
public class HelpFrame extends JFrame
 implements HelpPageContainer,HelpFontChangeListener
{
   public HelpFrame(TeXJavaHelpLib helpLib, String title)
    throws IOException
   {
      super(title);

      this.helpLib = helpLib;

      init();

      // helpPage needs updating first
      helpLib.addHelpFontChangeListener(this);
   }

   private void init() throws IOException
   {
      JMenuBar mBar = new JMenuBar();
      setJMenuBar(mBar);

      JMenu navMenu = helpLib.createJMenu("menu.helpframe.navigation");
      mBar.add(navMenu);

      JMenu settingsMenu = helpLib.createJMenu("menu.helpframe.settings");
      mBar.add(settingsMenu);

      helpPage = new HelpPage(helpLib, this);

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

      navTree.addMouseListener(new MouseAdapter()
       {
          @Override
          public void mousePressed(MouseEvent evt)
          {
             helpPage.checkForPopup(evt);
          }
          @Override
          public void mouseReleased(MouseEvent evt)
          {
             helpPage.checkForPopup(evt);
          }
       });

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        new JScrollPane(navTree), new JScrollPane(helpPage));

      splitPane.setResizeWeight(0.25);
      splitPane.setOneTouchExpandable(true);

      getContentPane().add(splitPane, "Center");

      JPanel buttonBar = new JPanel(new BorderLayout());
      JPanel docNavPanel = new JPanel();
      buttonBar.add(docNavPanel, "West");

      JPanel middlePanel = new JPanel();
      buttonBar.add(middlePanel, "Center");

      JPanel historyPanel = new JPanel();
      buttonBar.add(historyPanel, "East");

      getContentPane().add(buttonBar, "North");

      TJHAbstractAction homeAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.navigation", "home")
      {
         @Override
         public void doAction()
         {
            homePage();
         }
      };

      docNavPanel.add(createActionComponent(homeAction));
      navMenu.add(homeAction);

      previousAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.navigation", "previous")
      {
         @Override
         public void doAction()
         {
            prevPage();
         }
      };

      docNavPanel.add(createActionComponent(previousAction));
      navMenu.add(previousAction);

      upAction = new TJHAbstractAction(helpLib, "menu.helpframe.navigation", "up")
      {
         @Override
         public void doAction()
         {
            upPage();
         }
      };

      docNavPanel.add(createActionComponent(upAction));
      navMenu.add(upAction);

      nextAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.navigation", "next")
      {
         @Override
         public void doAction()
         {
            nextPage();
         }
      };

      docNavPanel.add(createActionComponent(nextAction));
      navMenu.add(nextAction);

      // search
      navMenu.addSeparator();

      helpSearchFrame = new HelpSearchFrame(this);

      TJHAbstractAction searchAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.navigation", "search")
      {
         @Override
         public void doAction()
         {
            helpSearchFrame.open();
         }
      };

      middlePanel.add(createActionComponent(searchAction));
      navMenu.add(searchAction);

      // index

      NavigationNode indexNode = helpLib.getIndexNode();
      URL indexNodeURL = (indexNode == null ? null : indexNode.getURL());

      if (indexNodeURL != null)
      {
         helpIndexFrame = new HelpIndexFrame(this, 
          helpLib.getIndexGroupData(), indexNodeURL);

         TJHAbstractAction indexAction = new TJHAbstractAction(helpLib,
           "menu.helpframe.navigation", "index")
         {
            @Override
            public void doAction()
            {
               helpIndexFrame.open();
            }
         };

         middlePanel.add(createActionComponent(indexAction));
         navMenu.add(indexAction);
      }
      else
      {
         helpLib.debug("No navigation: indexNode="+indexNode);
      }

      navMenu.addSeparator();
      middlePanel.add(Box.createHorizontalStrut(20));

      // history

      helpHistoryFrame = new HelpHistoryFrame(this);

      historyAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.navigation", "history")
       {
         @Override
         public void doAction()
         {
            showHistoryFrame();
         }
       };

      historyPanel.add(createActionComponent(historyAction));
      navMenu.add(historyAction);

      historyBackAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.navigation", "historyback")
       {
         @Override
         public void doAction()
         {
            historyBack();
         }
       };

      historyPanel.add(createActionComponent(historyBackAction));
      navMenu.add(historyBackAction);

      historyForwardAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.navigation", "historyforward")
       {
         @Override
         public void doAction()
         {
            historyForward();
         }
       };

      historyPanel.add(createActionComponent(historyForwardAction));
      navMenu.add(historyForwardAction);

      navMenu.addSeparator();
      navMenu.add(new TJHAbstractAction(helpLib, "button", "close")
        {
           @Override
           public void doAction()
           {
              setVisible(false);
           }
        });

      // lower navigation panel

      navPanel = new HelpPageNavPanel(helpLib, previousAction, upAction, nextAction);
      getContentPane().add(navPanel, "South");

      updateNavWidgets();

      // font

      TJHAbstractAction fontDecreaseAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.settings", "decrease")
      {
         @Override
         public void doAction()
         {
            int size = helpLib.getHelpFontSettings().getBodyFontSize();

            if (size > 3)
            {
               setHelpFont(size-1);
            }
         }
      };

      middlePanel.add(createActionComponent(fontDecreaseAction));
      settingsMenu.add(fontDecreaseAction);

      helpFontSettingsFrame = new HelpFontSettingsFrame(this);

      TJHAbstractAction fontSelectAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.settings", "font")
      {
         @Override
         public void doAction()
         {
            openFontSettings();
         }
      };

      middlePanel.add(createActionComponent(fontSelectAction));
      settingsMenu.add(fontSelectAction);

      TJHAbstractAction fontIncreaseAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.settings", "increase")
      {
         @Override
         public void doAction()
         {
            setHelpFont(helpLib.getHelpFontSettings().getBodyFontSize()+1);
         }
      };

      middlePanel.add(createActionComponent(fontIncreaseAction));
      settingsMenu.add(fontIncreaseAction);

      // lower navigation panel settings

      lowerNavSettingsDialog = new HelpLowerNavSettingsDialog(this, navPanel);

      TJHAbstractAction lowerNavSettingsAction = new TJHAbstractAction(helpLib,
        "menu.helpframe.settings", "nav")
      {
         @Override
         public void doAction()
         {
            openNavSettings();
         }
      };

      settingsMenu.add(lowerNavSettingsAction);

      setSize(helpLib.getHelpWindowInitialSize());
   }

   public FontMetrics getHelpFontMetrics(Font font)
   {
      return helpPage.getFontMetrics(font);
   }

   public void openFontSettings()
   {
      helpFontSettingsFrame.open();
   }

   public void openNavSettings()
   {
      lowerNavSettingsDialog.open(this);
   }

   @Override
   public void fontChanged(HelpFontChangeEvent evt)
   {
      Font f = evt.getSettings().getBodyFont();
      navTree.setFont(f);
      helpHistoryFrame.update();
   }

   private void setHelpFont(int fontSize)
   {
      HelpFontSettings settings = helpLib.getHelpFontSettings();
      settings.setBodyFontSize(fontSize);

      helpLib.notifyFontChange(new HelpFontChangeEvent(this,
       settings, HelpFontChangeEvent.BODY_SIZE));
   }

   protected JButton createActionComponent(Action action)
   {
      return helpLib.createToolBarButton(action);
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   public void setIconImage(Image image)
   {
      super.setIconImage(image);
      helpHistoryFrame.setIconImage(image);
      helpSearchFrame.setIconImage(image);
      helpFontSettingsFrame.setIconImage(image);

      if (helpIndexFrame != null)
      {
         helpIndexFrame.setIconImage(image);
      }
   }

   protected void createImageViewer()
   {
      imageViewer = new ImageViewer(helpLib, this, 
        helpLib.getMessage("imageviewer.title"), false);
   }

   @Override
   public ImageViewer getImageViewer()
   {
      if (imageViewer == null)
      {
         createImageViewer();
      }

      return imageViewer;
   }

   public void setIconImages(List<? extends Image> icons)
   {
      super.setIconImages(icons);
      helpHistoryFrame.setIconImages(icons);
      helpSearchFrame.setIconImages(icons);
      helpFontSettingsFrame.setIconImages(icons);

      if (helpIndexFrame != null)
      {
         helpIndexFrame.setIconImages(icons);
      }
   }

   public void setPage(NavigationNode node) throws IOException
   {
      helpPage.setPage(node);
   }

   public void setPage(String nodeId, String ref) throws IOException
   {
      helpPage.setPage(nodeId, ref);

      if (!isVisible())
      {
         setVisible(true);
      }
   }

   public void open(URL url) throws IOException
   {
      helpPage.open(url);

      if (!isVisible())
      {
         setVisible(true);
      }
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
         helpHistoryFrame.setLocationRelativeTo(this);
         helpHistoryFrame.setVisible(true);
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

   public void history(int idx)
   {
      try
      {
         helpPage.history(idx);
      }
      catch (IOException e)
      {
         helpLib.debug(e);
      }

      if (!isVisible())
      {
         setVisible(true);
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

   @Override
   public void requestHelpPageFocus()
   {
      toFront();
      helpPage.requestFocusInWindow();
   }

   @Override
   public void updateNavWidgets()
   {
      NavigationNode currentNode = helpPage.getCurrentNode();

      historyBackAction.setEnabled(helpPage.hasBackHistory());
      historyForwardAction.setEnabled(helpPage.hasForwardHistory());

      popupHistoryBackAction.setEnabled(historyBackAction.isEnabled());
      popupHistoryForwardAction.setEnabled(historyForwardAction.isEnabled());

      if (helpHistoryFrame.isVisible())
      {
         helpHistoryFrame.update();
      }

      NavigationNode previousNode = currentNode.getPreviousNode();
      NavigationNode nextNode = currentNode.getNextNode();
      NavigationNode upNode = currentNode.getParentNode();

      if (previousNode == null)
      {
         previousAction.setEnabled(false);
         navPanel.updatePreviousLabel(false, "");
      }
      else
      {
         previousAction.setEnabled(true);
         navPanel.updatePreviousLabel(true, previousNode.getTitle());
      }

      popupPreviousAction.setEnabled(previousAction.isEnabled());

      if (nextNode == null)
      {
         nextAction.setEnabled(false);
         navPanel.updateNextLabel(false, "");
      }
      else
      {
         nextAction.setEnabled(true);
         navPanel.updateNextLabel(true, nextNode.getTitle());
      }

      popupNextAction.setEnabled(nextAction.isEnabled());

      if (upNode == null)
      {
         upAction.setEnabled(false);
         navPanel.updateUpLabel(false, "");
      }
      else
      {
         upAction.setEnabled(true);
         navPanel.updateUpLabel(true, upNode.getTitle());
      }

      popupUpAction.setEnabled(upAction.isEnabled());

      TreePath treePath = navTree.getSelectionPath();

      if (treePath == null
        || !currentNode.equals((NavigationNode)treePath.getLastPathComponent()))
      {
         navTree.setSelectionPath(currentNode.getTreePath());
      }
   }

   public HelpPageNavPanel getLowerNavPanel()
   {
      return navPanel;
   }

   @Override
   public void addActions(JPopupMenu popupMenu)
   {
      popupMenu.add(new TJHAbstractAction(helpLib,
        "menu.helppage", "home")
      {
         @Override
         public void doAction()
         {
            homePage();
         }
      });

      popupPreviousAction = new TJHAbstractAction(helpLib,
        "menu.helppage", "previous")
      {
         @Override
         public void doAction()
         {
            prevPage();
         }
      };

      popupMenu.add(popupPreviousAction);

      popupUpAction = new TJHAbstractAction(helpLib, "menu.helppage", "up")
      {
         @Override
         public void doAction()
         {
            upPage();
         }
      };

      popupMenu.add(popupUpAction);

      popupNextAction = new TJHAbstractAction(helpLib,
        "menu.helppage", "next")
      {
         @Override
         public void doAction()
         {
            nextPage();
         }
      };

      popupMenu.add(popupNextAction);

      popupMenu.addSeparator();

      popupHistoryBackAction = new TJHAbstractAction(helpLib,
        "menu.helppage", "historyback")
       {
         @Override
         public void doAction()
         {
            historyBack();
         }
       };

      popupMenu.add(popupHistoryBackAction);

      popupHistoryForwardAction = new TJHAbstractAction(helpLib,
        "menu.helppage", "historyforward")
       {
         @Override
         public void doAction()
         {
            historyForward();
         }
       };

      popupMenu.add(popupHistoryForwardAction);
   }

   protected TeXJavaHelpLib helpLib;
   protected HelpPage helpPage;
   protected JSplitPane splitPane;
   protected JTree navTree;

   protected HelpHistoryFrame helpHistoryFrame;
   protected HelpFontSettingsFrame helpFontSettingsFrame;
   protected HelpSearchFrame helpSearchFrame;
   protected HelpIndexFrame helpIndexFrame;

   protected ImageViewer imageViewer;

   protected TJHAbstractAction previousAction, upAction, nextAction,
    historyAction, historyForwardAction, historyBackAction,
    fontIncreaseAction, fontDecreaseAction, fontSelectAction,
    popupPreviousAction, popupUpAction, popupNextAction,
    popupHistoryBackAction, popupHistoryForwardAction;

   protected HelpPageNavPanel navPanel;
   protected HelpLowerNavSettingsDialog lowerNavSettingsDialog;
}
