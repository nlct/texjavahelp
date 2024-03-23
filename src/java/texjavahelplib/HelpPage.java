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

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

/**
 * Panel for showing a page of the manual.
 */
public class HelpPage extends JEditorPane implements HyperlinkListener
{
   public HelpPage(TeXJavaHelpLib helpLib)
     throws IOException
   {
      super();

      setContentType("text/html");
      setEditable(false);

      this.helpLib = helpLib;

      NavigationTree navTree = helpLib.getNavigationTree();

      setPage(navTree.getRoot());

      addHyperlinkListener(this);
   }

   public NavigationNode getCurrentNode()
   {
      return currentNode;
   }

   public void setPage(NavigationNode node)
     throws IOException
   {
      currentNode = node;
      URL url = node.getURL();

      if (url == null)
      {
         url = helpLib.getHelpSetResource(node.getFileName());
         node.setURL(url);
      }

      setPage(url);
   }

   public void nextPage() throws IOException
   {
      NavigationNode node = currentNode.getNextNode();

      if (node != null)
      {
         setPage(node);
      }
   }

   public void prevPage() throws IOException
   {
      NavigationNode node = currentNode.getPreviousNode();

      if (node != null)
      {
         setPage(node);
      }
   }

   public void upPage() throws IOException
   {
      NavigationNode node = currentNode.getParentNode();

      if (node != null)
      {
         setPage(node);
      }
   }

   public void homePage() throws IOException
   {
      setPage(helpLib.getNavigationTree().getRoot());
   }

   @Override
   public void hyperlinkUpdate(HyperlinkEvent evt)
   {
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
         URL url = evt.getURL();
         NavigationNode node = helpLib.getNavigationTree().getNodeByURL(url);

         try
         {
            if (node == null)
            {
               setPage(url);
            }
            else
            {
               String ref = url.getRef();
               setPage(node);

               if (ref != null)
               {
                  scrollToReference(ref);
               }

               helpLib.getHelpFrame().updateNavWidgets();
            }
         }
         catch (Throwable t)
         {
            t.printStackTrace();
         }
      }
      else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED)
      {
         URL url = evt.getURL();
         NavigationNode node = helpLib.getNavigationTree().getNodeByURL(url);

         if (node != null)
         {
            setToolTipText(node.getTitle());
         }
         else
         {
            setToolTipText(null);
         }
      }
      else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED)
      {
         setToolTipText(null);
      }
   }

   protected TeXJavaHelpLib helpLib;
   protected NavigationNode currentNode;

}
