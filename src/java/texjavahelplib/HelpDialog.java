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
import java.io.FileNotFoundException;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.tree.TreePath;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

/**
 * Dialog box showing a page from the manual.
 * Modal dialogs will open in front of the main HelpFrame and won't
 * allow the user to interact with it. In which case, a help button
 * on modal dialogs will need to own a dialog that shows the
 * applicable help page.
 */
public class HelpDialog extends JDialog
 implements HelpPageContainer,HelpFontChangeListener
{
   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode, Dialog owner)
   throws IOException
   {
      this(helpLib, pageNode, owner, pageNode.getTitle(), false);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      TargetRef targetRef, Dialog owner)
   throws IOException
   {
      this(helpLib, targetRef.getNode(), owner,
       targetRef.getNode().getTitle(), false, targetRef);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode,
      Dialog owner, boolean modal)
   throws IOException
   {
      this(helpLib, pageNode, owner, pageNode.getTitle(), modal, (TargetRef)null);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode,
      Dialog owner, boolean modal, TargetRef targetRef)
   throws IOException
   {
      this(helpLib, pageNode, owner, pageNode.getTitle(), modal, targetRef);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode,
      Dialog owner, String title, boolean modal)
   throws IOException
   {
      this(helpLib, pageNode, owner, title, modal, (TargetRef)null);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode,
      Dialog owner, String title, boolean modal, TargetRef targetRef)
   throws IOException
   {
      super(owner, title, modal);
      this.helpLib = helpLib;
      this.pageNode = pageNode;
      this.pageTargetRef = targetRef;

      updateAndCloseHelpFrame = modal
         || owner.getModalityType() != Dialog.ModalityType.MODELESS;

      init();

      helpLib.addHelpFontChangeListener(this);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode, Frame owner)
   throws IOException
   {
      this(helpLib, pageNode, owner, pageNode.getTitle(), false, (TargetRef)null);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      TargetRef targetRef, Frame owner)
   throws IOException
   {
      this(helpLib, targetRef.getNode(), owner,
       targetRef.getNode().getTitle(), false,
       targetRef);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode,
      Frame owner, boolean modal)
   throws IOException
   {
      this(helpLib, pageNode, owner, pageNode.getTitle(), modal, (TargetRef)null);
   }

   public HelpDialog(TeXJavaHelpLib helpLib,
      NavigationNode pageNode,
      Frame owner, String title, boolean modal, TargetRef targetRef)
   throws IOException
   {
      super(owner, title, modal);
      this.helpLib = helpLib;
      this.pageNode = pageNode;
      this.pageTargetRef = targetRef;
      updateAndCloseHelpFrame = modal;

      init();

      helpLib.addHelpFontChangeListener(this);
   }

   private void init() throws IOException
   {
      boolean hasChildren = (pageNode.getChildCount() > 0);

      helpPage = new HelpPage(helpLib, this, pageNode);

      JComponent toolBar = new JPanel(new BorderLayout());
      getContentPane().add(toolBar, BorderLayout.NORTH);

      JComponent navComp = new JPanel();
      toolBar.add(navComp, BorderLayout.WEST);

      JComponent historyNavComp = new JPanel();
      toolBar.add(historyNavComp, BorderLayout.EAST);

      JMenuBar mBar = new JMenuBar();
      setJMenuBar(mBar);

      // Minimal navigation

      JMenu navMenu = helpLib.createJMenu("menu.helpframe.navigation");
      mBar.add(navMenu);

      resetAction = new TJHAbstractAction(helpLib,
        "menu.helpdialog.navigation", "reset")
       {
         @Override
         public void doAction()
         {
            reset();
         }
       };

      resetAction.setToolTipText(
        helpLib.getMessage("menu.helpdialog.navigation.reset.tooltip",
          pageNode.getTitle()));

      navComp.add(createActionComponent(resetAction));
      navMenu.add(resetAction);

      historyBackAction = new TJHAbstractAction(helpLib,
        "menu.helpdialog.navigation", "historyback")
       {
         @Override
         public void doAction()
         {
            historyBack();
         }
       };

      historyNavComp.add(createActionComponent(historyBackAction));
      navMenu.add(historyBackAction);

      historyForwardAction = new TJHAbstractAction(helpLib,
        "menu.helpdialog.navigation", "historyforward")
       {
         @Override
         public void doAction()
         {
            historyForward();
         }
       };

      historyNavComp.add(createActionComponent(historyForwardAction));
      navMenu.add(historyForwardAction);

      if (hasChildren)
      {
         navMenu.addSeparator();

         previousAction = new TJHAbstractAction(helpLib,
           "menu.helpdialog.navigation", "previous")
         {
            @Override
            public void doAction()
            {
               prevPage();
            }
         };

         navComp.add(createActionComponent(previousAction));
         navMenu.add(previousAction);

         upAction = new TJHAbstractAction(helpLib,
           "menu.helpdialog.navigation", "up")
         {
            @Override
            public void doAction()
            {
               upPage();
            }
         };

         navComp.add(createActionComponent(upAction));
         navMenu.add(upAction);

         nextAction = new TJHAbstractAction(helpLib,
           "menu.helpdialog.navigation", "next")
         {
            @Override
            public void doAction()
            {
               nextPage();
            }
         };

         navComp.add(createActionComponent(nextAction));
         navMenu.add(nextAction);

         navTree = new JTree(pageNode);
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
                      setPage(node);
                   }
                }
             }
          });

         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
           new JScrollPane(navTree), new JScrollPane(helpPage));

         splitPane.setResizeWeight(0.1);
         splitPane.setOneTouchExpandable(true);

         getContentPane().add(splitPane, BorderLayout.CENTER);
      }
      else
      {
         getContentPane().add(new JScrollPane(helpPage), BorderLayout.CENTER);
      }

      navMenu.addSeparator();
      navMenu.add(new TJHAbstractAction(helpLib, "button", "close")
        {
           @Override
           public void doAction()
           {
              setVisible(false);
           }
        });

      if (pageTargetRef != null)
      {
         setPage(pageTargetRef, false);
      }

      updateNavWidgets();

      setSize(helpLib.getHelpWindowInitialSize());
   }

   protected JButton createActionComponent(Action action)
   {
      return helpLib.createToolBarButton(action);
   }

   public void display()
   {
      if (updateAndCloseHelpFrame)
      {
         HelpFrame helpFrame = helpLib.getHelpFrame();

         if (helpFrame.isVisible())
         {
            // Since the user can't interact with the main help frame
            // with modal dialogs close it to avoid confusion.
            helpFrame.setVisible(false);
         }

         try
         {
            // Add this page to the main help frame's history

            helpFrame.setPage(getCurrentNode());
         }
         catch (IOException e)
         {
            helpLib.debug(e);
         }
      }

      setVisible(true);
   }

   @Override
   public void fontChanged(HelpFontChangeEvent event)
   {
      if (navTree != null)
      {
         HelpFontSettings fontSettings = event.getSettings();
         navTree.setFont(fontSettings.getBodyFont());
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

   public void reset()
   {
      try
      {
         if (pageTargetRef == null)
         {
            helpPage.setPage(pageNode);
         }
         else
         {
            helpPage.setPage(pageNode, pageTargetRef.getRef());
         }

         updateNavWidgets();
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

   public boolean isInNavigationTree(NavigationNode node)
   {
      return node != null && (node.equals(pageNode) || pageNode.isAncestorOf(node));
   }

   public void upPage()
   {
      NavigationNode currentNode = helpPage.getCurrentNode();
      setPage(currentNode.getParentNode());
   }

   public void prevPage()
   {
      NavigationNode currentNode = helpPage.getCurrentNode();
      setPage(currentNode.getPreviousNode());
   }

   public void nextPage()
   {
      NavigationNode currentNode = helpPage.getCurrentNode();
      setPage(currentNode.getNextNode());
   }

   public void setPage(String ref) throws IOException
   {
      NavigationNode node = helpLib.getNavigationNodeById(ref);

      if (node == null)
      {
         TargetRef targetRef = helpLib.getTargetRef(ref);

         if (targetRef == null)
         {
            throw new FileNotFoundException(helpLib.getMessage(
               "error.node_id_not_found", ref));
         }
         else
         {
            setPage(targetRef);
         }
      }
      else
      {
         setPage(node);
      }
   }

   public void setPage(NavigationNode node)
   {
      setPage(node, updateAndCloseHelpFrame);
   }

   public void setPage(NavigationNode node, boolean updateHelpFrame)
   {
      try
      {
         if (isInNavigationTree(node))
         {
            helpPage.setPage(node);

            if (updateHelpFrame)
            {
               helpLib.getHelpFrame().setPage(node);
            }
         }
      }
      catch (IOException e)
      {
         helpLib.debug(e);
      }
   }

   public void setPage(TargetRef targetRef) throws IOException
   {
      setPage(targetRef, updateAndCloseHelpFrame);
   }

   public void setPage(TargetRef targetRef, boolean updateHelpFrame)
   throws IOException
   {
      try
      {
         if (isInNavigationTree(targetRef.getNode()))
         {
            helpPage.setPage(targetRef);

            if (updateHelpFrame)
            {
               helpLib.getHelpFrame().setPage(targetRef);
            }
         }
      }
      catch (IOException e)
      {
         helpLib.debug(e);
      }
   }

   public NavigationNode getCurrentNode()
   {
      return helpPage.getCurrentNode();
   }

   @Override
   public void requestHelpPageFocus()
   {
      helpPage.requestFocusInWindow();
   }

   @Override
   public void updateNavWidgets()
   {
      NavigationNode currentNode = helpPage.getCurrentNode();

      historyBackAction.setEnabled(helpPage.hasBackHistory());
      historyForwardAction.setEnabled(helpPage.hasForwardHistory());
      resetAction.setEnabled(!currentNode.equals(pageNode));

      popupHistoryBackAction.setEnabled(historyBackAction.isEnabled());
      popupHistoryForwardAction.setEnabled(historyForwardAction.isEnabled());
      popupResetAction.setEnabled(resetAction.isEnabled());

      if (pageNode.getChildCount() > 0)
      {
         NavigationNode prevPage = currentNode.getPreviousNode();
         previousAction.setEnabled(isInNavigationTree(prevPage));
         popupPreviousAction.setEnabled(previousAction.isEnabled());

         NavigationNode upPage = currentNode.getParentNode();
         upAction.setEnabled(isInNavigationTree(upPage));
         popupUpAction.setEnabled(upAction.isEnabled());

         NavigationNode nextPage = currentNode.getNextNode();
         nextAction.setEnabled(isInNavigationTree(nextPage));
         popupNextAction.setEnabled(nextAction.isEnabled());

         if (isInNavigationTree(currentNode))
         {
            TreePath treePath = navTree.getSelectionPath();
   
            if (treePath == null
              || !currentNode.equals((NavigationNode)treePath.getLastPathComponent()))
            {
               navTree.setSelectionPath(currentNode.getTreePath());
            }
         }
         else
         {
            navTree.clearSelection();
         }
      }
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   @Override
   public void addActions(JPopupMenu popupMenu)
   { 
      popupResetAction = new TJHAbstractAction(helpLib,
        "menu.helppage", "reset")
       {
         @Override
         public void doAction()
         {
            reset();
         }
       };

      popupMenu.add(popupResetAction);

      if (pageNode.getChildCount() > 0)
      {
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

         popupUpAction = new TJHAbstractAction(helpLib,
           "menu.helppage", "up")
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
      }

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

   protected void createImageViewer()
   {
      imageViewer = new ImageViewer(helpLib, this, 
        helpLib.getMessage("imageviewer.title"));
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

   protected HelpPage helpPage;
   protected TeXJavaHelpLib helpLib;
   protected NavigationNode pageNode;
   protected TargetRef pageTargetRef;
   protected TJHAbstractAction resetAction,
     historyBackAction, historyForwardAction,
     popupResetAction, popupHistoryBackAction, popupHistoryForwardAction;

   protected boolean updateAndCloseHelpFrame = true;

   protected ImageViewer imageViewer;

   // null if node has no children
   protected JSplitPane splitPane;
   protected JTree navTree;
   protected TJHAbstractAction nextAction, previousAction, upAction,
    popupNextAction, popupPreviousAction, popupUpAction;
}
