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

import java.util.Locale;
import java.util.Vector;

import java.io.IOException;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Panel for showing a page of the manual.
 */
public class HelpPage extends TJHEditorPane
  implements HyperlinkListener,HelpFontChangeListener
{
   public HelpPage(TeXJavaHelpLib helpLib, HelpPageContainer helpPageContainer)
     throws IOException
   {
      this(helpLib, helpPageContainer,
         helpLib.getNavigationTree().getRoot(), helpLib.getHelpFontSettings());
   }

   public HelpPage(TeXJavaHelpLib helpLib, HelpPageContainer helpPageContainer,
      NavigationNode initialPage)
     throws IOException
   {
      this(helpLib, helpPageContainer, initialPage,
        helpLib.getHelpFontSettings());
   }

   public HelpPage(TeXJavaHelpLib helpLib, HelpPageContainer helpPageContainer,
      NavigationNode initialPage, HelpFontSettings fontSettings)
     throws IOException
   {
      super();

      this.helpLib = helpLib;
      this.helpPageContainer = helpPageContainer;
      this.fontSettings = fontSettings;
      helpLib.addHelpFontChangeListener(this);

      history = new Vector<HistoryItem>();

      setPage(initialPage);

      addHyperlinkListener(this);

      addMouseListener(new MouseAdapter()
       {
          @Override
          public void mousePressed(MouseEvent evt)
          {
             checkForPopup(evt);
          }
          @Override
          public void mouseReleased(MouseEvent evt)
          {
             checkForPopup(evt);
          }
       });

      addPropertyChangeListener("page", 
        new PropertyChangeListener()
         {
            public void propertyChange(PropertyChangeEvent evt)
            {
               pageChanged(evt);
            }
         }
      );

      popupMenu = new JPopupMenu();
      viewImageAction = new TJHAbstractAction(helpLib,
         "menu.helppage", "view_image", this)
       {
          @Override
          public void doAction()
          {
             viewImage();
          }
       };
      popupMenu.add(viewImageAction);
      helpPageContainer.addActions(popupMenu);

      getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
        "popupmenu");
      getActionMap().put("popupmenu",
         new AbstractAction()
         {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
               popupAt(getCaretPosition());
            }
         });
   }

   public NavigationNode getCurrentNode()
   {
      return currentNode;
   }

   public boolean hasBackHistory()
   {
      return (historyIdx > 0);
   }

   public boolean hasForwardHistory()
   {
      return (historyIdx < history.size() - 1);
   }

   public int getHistoryCount()
   {
      return history.size();
   }

   public int getHistoryIndex()
   {
      return historyIdx;
   }

   public HistoryItem getHistoryItem(int idx)
   {
      return history.get(idx);
   }

   public void history(int idx) throws IOException
   {
      if (idx >= 0 && idx < history.size())
      {
         historyIdx = idx;

         HistoryItem item = history.get(historyIdx);
         currentNode = item.getNode();
         URL url = currentNode.getURL();

         setPage(url);
      }
   }


   public void historyBack() throws IOException
   {
      if (historyIdx > 0)
      {
         historyIdx--;

         HistoryItem item = history.get(historyIdx);
         currentNode = item.getNode();
         URL url = currentNode.getURL();

         setPage(url);
      }
   }

   public void historyForward() throws IOException
   {
      if (historyIdx < history.size() - 1)
      {
         historyIdx++;

         HistoryItem item = history.get(historyIdx);
         currentNode = item.getNode();
         URL url = currentNode.getURL();

         setPage(url);
      }
   }

   public void setPage(NavigationNode node)
     throws IOException
   {
      updateCurrentNode(node, null);

      setPage(node.getURL());
   }

   protected void updateCurrentNode(NavigationNode node, String ref)
     throws IOException
   {
      currentNode = node;
      URL url = node.getURL();

      if (url == null)
      {
         url = helpLib.getHelpSetResource(node.getFileName());
         node.setURL(url);
      }

      if (historyIdx < history.size() - 1)
      {
         history.setSize(historyIdx+1);
      }

      history.add(new HistoryItem(node, ref));
      historyIdx = history.size()-1;
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

   public void setPage(String nodeId, String ref) throws IOException
   {
      NavigationNode node = helpLib.getNavigationTree().getNodeById(nodeId);

      if (node != null)
      {
         setPage(node, ref);
      }
   }

   public void setPage(TargetRef targetRef) throws IOException
   {
      setPage(targetRef.getNode(), targetRef.getRef());
   }

   public void setPage(NavigationNode node, String ref) throws IOException
   {
      updateCurrentNode(node, ref);

      URL url = node.getURL();

      if (ref != null && !ref.isEmpty())
      {
         try
         {
            URI uri = url.toURI();

            uri = new URI(uri.getScheme(), uri.getUserInfo(),
             uri.getHost(), uri.getPort(),
             uri.getPath(), uri.getQuery(), ref);

            url = uri.toURL();
         }
         catch (URISyntaxException e)
         {
            helpLib.debug(e);
         }
      }

      setPage(url);
   }

   protected void pageChanged(PropertyChangeEvent evt)
   {
      Object oldValue = evt.getOldValue();
      Object newValue = evt.getNewValue();

      if (oldValue != null && newValue != null
         && oldValue instanceof URL && newValue instanceof URL)
      {
         URL oldUrl = (URL)oldValue;
         URL newUrl = (URL)newValue;

/*
         String ref = newUrl.getRef();

         if (ref != null)
         {
            scrollToReference(ref);
         }
*/

         helpPageContainer.updateNavWidgets();
      }

      addFontRulesToStyleSheet();
   }

   protected void addFontRulesToStyleSheet()
   {
      HTMLDocument doc = (HTMLDocument)getDocument();
      StyleSheet styles = doc.getStyleSheet();

      fontSettings.addFontRulesToStyleSheet(styles);
   }

   @Override
   public void fontChanged(HelpFontChangeEvent event)
   {
      fontSettings.copyFrom(event);

      HTMLDocument doc = (HTMLDocument)getDocument();
      StyleSheet styles = doc.getStyleSheet();

      fontSettings.addFontRulesToStyleSheet(
        styles, event.getModifiers());
   }

   public HelpFontSettings getFontSettings()
   {
      return fontSettings;
   }

   public void open(URL url) throws IOException
   {
      if ("file".equals(url.getProtocol()))
      {
         NavigationNode node = helpLib.getNavigationTree().getNodeByURL(url);

         String ref = url.getRef();

         if (node == null && ref != null && !ref.isEmpty())
         {
            TargetRef targetRef = helpLib.getTargetRef(ref);

            if (targetRef != null)
            {
               node = targetRef.getNode();
            }
         }

         setPage(url);

         if (node != null)
         {
            updateCurrentNode(node, ref);
         }
      }
      else
      {
         if (Desktop.isDesktopSupported())
         {
            try
            {
               Desktop.getDesktop().browse(url.toURI());
            }
            catch (Exception e)
            {
               throw new IOException(helpLib.getMessage("error.browse_failed", url), e);
            }
         }
         else
         {
            throw new IOException(helpLib.getMessage("error.no_desktop", url));
         }
      }
   }

   @Override
   public void hyperlinkUpdate(HyperlinkEvent evt)
   {
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
         try
         {
            open(evt.getURL());
         }
         catch (IOException e)
         {
            helpLib.error(e);
         }
      }
      else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED)
      {
         URL url = evt.getURL();
         String text = null;
         String ref = url.getRef();

         if (ref != null)
         {
            TargetRef targetRef = helpLib.getTargetRef(ref);

            if (targetRef != null)
            {
               IndexItem item = targetRef.getIndexItem();

               text = item.brief();
            }
         }

         if (text == null || text.isEmpty())
         {
            NavigationNode node = helpLib.getNavigationTree().getNodeByURL(url);

            if (node != null)
            {
               text = node.getTitle();
            }
         }

         if (text == null || text.isEmpty())
         {
            text = url.toString();
         }
         else
         {
            text = "<html>"+text+"</html>";
         }

         setToolTipText(text);
      }
      else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED)
      {
         setToolTipText(null);
      }
   }

   public boolean checkForPopup(MouseEvent evt)
   {     
      if (evt.isPopupTrigger())
      { 
         currentImageAttributeSet = null;

         if (evt.getSource() == this)
         {
            Point pt = new Point(evt.getX(), evt.getY());
            int pos = viewToModel(pt);

            checkForImg(pos);
         }

         viewImageAction.setEnabled(currentImageAttributeSet != null);

         popupMenu.show((Component)evt.getSource(), evt.getX(), evt.getY());

         return true;
      }

      return false;
   }

   public void popupAt(int pos)
   {
      currentImageAttributeSet = null;
      checkForImg(pos);

      viewImageAction.setEnabled(currentImageAttributeSet != null);

      Rectangle view;
      Component parentComp = getParent();
      Point location = null;

      if (parentComp instanceof JViewport)
      {
         view = ((JViewport)parentComp).getViewRect();
      }
      else
      {
         view = getBounds();
      }

      try
      {
         Rectangle rect = modelToView(pos);
         Point p = rect.getLocation();

         if (view.contains(p))
         {
            location = p;
         }
      }
      catch (BadLocationException e)
      {
      }

      if (location == null)
      {
         location = view.getLocation();
      }

      popupMenu.show(this, location.x, location.y);
   }

   protected void checkForImg(int pos)
   {
      HTMLDocument doc = (HTMLDocument)getDocument();
      HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.IMG);

      while (it.isValid())
      {
         // allow a little leeway either side
         if (it.getStartOffset()-2 <= pos && pos <= it.getEndOffset()+2)
         {
            AttributeSet as = it.getAttributes();

            if (as.getAttribute(HTML.Attribute.SRC) != null)
            {
               currentImageAttributeSet = as;
            }

            break;
         }

         it.next();
      }
   }

   protected void viewImage()
   {
      if (currentImageAttributeSet != null)
      {
         helpPageContainer.getImageViewer().display(currentImageAttributeSet);
      }
   }

   protected TeXJavaHelpLib helpLib;
   protected HelpPageContainer helpPageContainer;
   protected NavigationNode currentNode;

   protected JPopupMenu popupMenu;
   protected TJHAbstractAction viewImageAction;
   protected AttributeSet currentImageAttributeSet;

   protected Vector<HistoryItem> history;
   protected int historyIdx = 0;

   protected HelpFontSettings fontSettings;
}
