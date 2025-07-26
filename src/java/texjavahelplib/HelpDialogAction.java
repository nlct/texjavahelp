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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

public class HelpDialogAction extends TJHAbstractAction
{
   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, pageNode, helpLib, "button", "help", "manual."+pageNode.getKey(), "help",
       helpLib.getKeyStroke("button.help"), (Boolean)null, omitKeys);
   }

   public HelpDialogAction(JDialog owner, TargetRef targetRef,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, targetRef.getNode(), helpLib, "button", "help",
       "manual."+targetRef.getNode().getKey(), "help",
       helpLib.getKeyStroke("button.help"), (Boolean)null, targetRef, omitKeys);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     Boolean selectedState, String... omitKeys)
   {
      this(owner, pageNode, helpLib, parentTag, action, action, "help", 
       helpLib.getKeyStroke(action == null ? parentTag : parentTag+"."+action),
       selectedState, omitKeys);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, String... omitKeys)
   {
      this(owner, pageNode, helpLib, parentTag, action, action, "help",
        keyStroke, (Boolean)null, omitKeys);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, String iconPrefix,
     KeyStroke keyStroke, Boolean selectedState, String... omitKeys)
   {
      this(owner, pageNode, helpLib, parentTag, childTag, actionName, 
        iconPrefix, keyStroke, selectedState, (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, String iconPrefix,
     KeyStroke keyStroke, Boolean selectedState, TargetRef targetRef,
     String... omitKeys)
   {
      super(helpLib, parentTag, childTag, actionName, iconPrefix, keyStroke,
        selectedState, owner.getRootPane(), omitKeys);

      if (pageNode == null)
      {
         if (targetRef == null)
         {
            throw new NullPointerException();
         }

         pageNode = targetRef.getNode();
      }

      this.owner = owner;
      this.pageNode = pageNode;
      this.targetRef = targetRef;
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
      IconSet largeIconSet, IconSet smallIconSet,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, pageNode, 
       largeIconSet, smallIconSet,
       helpLib, "button", "help", "manual."+pageNode.getKey(), 
       helpLib.getKeyStroke("button.help"), (Boolean)null,
       (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JDialog owner, TargetRef targetRef,
      IconSet largeIconSet, IconSet smallIconSet,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, targetRef.getNode(),
       largeIconSet, smallIconSet,
        helpLib, "button", "help",
       "manual."+targetRef.getNode().getKey(), 
       helpLib.getKeyStroke("button.help"), (Boolean)null, targetRef, omitKeys);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
      IconSet largeIconSet, IconSet smallIconSet,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     Boolean selectedState, String... omitKeys)
   {
      this(owner, pageNode, 
       largeIconSet, smallIconSet,
       helpLib, parentTag, action, action,  
       helpLib.getKeyStroke(action == null ? parentTag : parentTag+"."+action),
       selectedState, (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
      IconSet largeIconSet, IconSet smallIconSet,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, String... omitKeys)
   {
      this(owner, pageNode, 
        largeIconSet, smallIconSet,
        helpLib, parentTag, action, action, 
        keyStroke, (Boolean)null, (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     IconSet largeIconSet, IconSet smallIconSet,
     TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, 
     KeyStroke keyStroke, Boolean selectedState, TargetRef targetRef,
     String... omitKeys)
   {
      super(helpLib, parentTag, childTag, actionName, 
            largeIconSet, smallIconSet,
           keyStroke, selectedState, owner.getRootPane(), omitKeys);

      if (pageNode == null)
      {
         if (targetRef == null)
         {
            throw new NullPointerException();
         }

         pageNode = targetRef.getNode();
      }

      this.owner = owner;
      this.pageNode = pageNode;
      this.targetRef = targetRef;
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, pageNode, helpLib, "button", "help", "manual."+pageNode.getKey(), "help",
       helpLib.getKeyStroke("button.help"), (Boolean)null, omitKeys);
   }

   public HelpDialogAction(JFrame owner, TargetRef targetRef,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, targetRef.getNode(), helpLib, "button", "help", 
      "manual."+targetRef.getNode().getKey(), "help",
       helpLib.getKeyStroke("button.help"), (Boolean)null, targetRef, omitKeys);
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     Boolean selectedState, String... omitKeys)
   {
      this(owner, pageNode, helpLib, parentTag, action, action, "help", 
       helpLib.getKeyStroke(action == null ? parentTag : parentTag+"."+action),
       selectedState, omitKeys);
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, String... omitKeys)
   {
      this(owner, pageNode, helpLib, parentTag, action, action, "help",
        keyStroke, (Boolean)null, omitKeys);
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, String iconPrefix,
     KeyStroke keyStroke, Boolean selectedState, String... omitKeys)
   {
      this(owner, pageNode, helpLib, parentTag, childTag, actionName, 
        iconPrefix, keyStroke, selectedState, (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, String iconPrefix,
     KeyStroke keyStroke, Boolean selectedState, TargetRef targetRef,
     String... omitKeys)
   {
      super(helpLib, parentTag, childTag, actionName, iconPrefix, keyStroke,
        selectedState, owner.getRootPane(), omitKeys);

      if (pageNode == null)
      {
         if (targetRef == null)
         {
            throw new NullPointerException();
         }

         pageNode = targetRef.getNode();
      }

      this.owner = owner;
      this.pageNode = pageNode;
      this.targetRef = targetRef;
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
      IconSet largeIconSet, IconSet smallIconSet,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, pageNode, 
       largeIconSet, smallIconSet,
       helpLib, "button", "help", "manual."+pageNode.getKey(), 
       helpLib.getKeyStroke("button.help"),
       (Boolean)null, (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JFrame owner, TargetRef targetRef,
      IconSet largeIconSet, IconSet smallIconSet,
      TeXJavaHelpLib helpLib, String... omitKeys)
   {
      this(owner, targetRef.getNode(), 
       largeIconSet, smallIconSet,
       helpLib, "button", "help", 
       "manual."+targetRef.getNode().getKey(), 
       helpLib.getKeyStroke("button.help"), (Boolean)null, targetRef, omitKeys);
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
      IconSet largeIconSet, IconSet smallIconSet,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     Boolean selectedState, String... omitKeys)
   {
      this(owner, pageNode, 
       largeIconSet, smallIconSet,
       helpLib, parentTag, action, action,  
       helpLib.getKeyStroke(action == null ? parentTag : parentTag+"."+action),
       selectedState, (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
      IconSet largeIconSet, IconSet smallIconSet,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke, String... omitKeys)
   {
      this(owner, pageNode, 
        largeIconSet, smallIconSet,
        helpLib, parentTag, action, action, 
        keyStroke, (Boolean)null, (TargetRef)null, omitKeys);
   }

   public HelpDialogAction(JFrame owner, NavigationNode pageNode,
     IconSet largeIconSet, IconSet smallIconSet,
     TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, 
     KeyStroke keyStroke, Boolean selectedState, TargetRef targetRef,
     String... omitKeys)
   {
      super(helpLib, parentTag, childTag, actionName, 
        largeIconSet, smallIconSet,
        keyStroke, selectedState, owner.getRootPane(), omitKeys);

      if (pageNode == null)
      {
         if (targetRef == null)
         {
            throw new NullPointerException();
         }

         pageNode = targetRef.getNode();
      }

      this.owner = owner;
      this.pageNode = pageNode;
      this.targetRef = targetRef;
   }

   @Override
   public void doAction()
   {
      if (helpDialog == null)
      {
         try
         {
            if (owner instanceof Dialog)
            {
               if (targetRef == null)
               {
                  helpDialog = new HelpDialog(helpLib, pageNode, (Dialog)owner);
               }
               else
               {
                  helpDialog = new HelpDialog(helpLib, targetRef, (Dialog)owner);
               }
            }
            else if (targetRef == null)
            {
               helpDialog = new HelpDialog(helpLib, pageNode, (Frame)owner);
            }
            else
            {
               helpDialog = new HelpDialog(helpLib, targetRef, (Frame)owner);
            }

            helpDialog.setLocationRelativeTo(owner);
         }
         catch (IOException e)
         {
            helpLib.error(e);
         }
      }
      else
      {
         helpDialog.reset();
      }

      helpDialog.display();
   }

   public Window getOwner()
   {
      return owner;
   }

   public HelpDialog getHelpDialog()
   {
      return helpDialog;
   }

   public NavigationNode getPageNode()
   {
      return pageNode;
   }

   private Window owner;
   private NavigationNode pageNode;
   private TargetRef targetRef;
   private HelpDialog helpDialog;
}
