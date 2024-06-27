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

import javax.swing.JDialog;
import javax.swing.KeyStroke;

public class HelpDialogAction extends TJHAbstractAction
{
   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
      TeXJavaHelpLib helpLib)
   {
      this(owner, pageNode, helpLib, "action", "help", "help", "help",
       helpLib.getKeyStroke("menu.help.manual"), (Boolean)null);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     Boolean selectedState)
   {
      this(owner, pageNode, helpLib, parentTag, action, action, "help", 
       helpLib.getKeyStroke(action == null ? parentTag : parentTag+"."+action),
       selectedState);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib, String parentTag, String action,
     KeyStroke keyStroke)
   {
      this(owner, pageNode, helpLib, parentTag, action, action, "help",
        keyStroke, (Boolean)null);
   }

   public HelpDialogAction(JDialog owner, NavigationNode pageNode,
     TeXJavaHelpLib helpLib,
     String parentTag, String childTag, String actionName, String iconPrefix,
     KeyStroke keyStroke, Boolean selectedState)
   {
      super(helpLib, parentTag, childTag, actionName, iconPrefix, keyStroke,
        selectedState, owner.getRootPane());

      if (pageNode == null)
      {
         throw new NullPointerException();
      }

      this.owner = owner;
      this.pageNode = pageNode;
   }

   public void doAction()
   {
      if (helpDialog == null)
      {
         try
         {
            helpDialog = new HelpDialog(helpLib, pageNode, owner);
            helpDialog.setLocationRelativeTo(owner);
         }
         catch (IOException e)
         {
            helpLib.error(e);
         }
      }
      else
      {
         helpDialog.historyReset();
      }

      helpDialog.display();
   }

   public JDialog getOwner()
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

   private JDialog owner;
   private NavigationNode pageNode;
   private HelpDialog helpDialog;
}
