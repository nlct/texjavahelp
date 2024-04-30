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

import java.util.Vector;

import java.io.IOException;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

import java.awt.Rectangle;
import java.awt.Desktop;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

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

      history = new Vector<HistoryItem>();

      setPage(navTree.getRoot());

      addHyperlinkListener(this);

      addPropertyChangeListener("page", 
        new PropertyChangeListener()
         {
            public void propertyChange(PropertyChangeEvent evt)
            {
               pageChanged(evt);
            }
         }
      );
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

   public void setPage(String nodeId, int pos) throws IOException
   {
      NavigationNode node = helpLib.getNavigationTree().getNodeById(nodeId);

      if (node != null)
      {
         updateCurrentNode(node, null);

         // adjust the relative position

         pos -= node.getBodyStartIndex();

         URL url = node.getURL();

         if (pos > 0)
         {
            try
            {
               URI uri = url.toURI();

               uri = new URI(uri.getScheme(), uri.getUserInfo(),
                uri.getHost(), uri.getPort(),
                uri.getPath(), "pos="+pos, uri.getFragment());

               url = uri.toURL();
            }
            catch (URISyntaxException e)
            {
               helpLib.debug(e);
            }
         }

         setPage(url);
      }
   }

   @Override
   public void scrollToReference(String ref)
   {
      HTMLDocument doc = (HTMLDocument)getDocument();

      Element element = doc.getElement(ref);

      if (element == null)
      {
         super.scrollToReference(ref);
      }
      else
      {
         int pos = element.getStartOffset();

         try
         {
            Rectangle r = modelToView(pos);

            if (r != null)
            {
               Rectangle vis = getVisibleRect();
               r.height = vis.height;
               scrollRectToVisible(r);
               setCaretPosition(pos);
            }
         }
         catch (BadLocationException e)
         {
         }
      }
   }

   public void scrollToPosition(int pos)
   {
      try
      {
         Rectangle r = modelToView(pos);

         if (r != null)
         {
            Rectangle vis = getVisibleRect();
            r.height = vis.height;
            scrollRectToVisible(r);
            setCaretPosition(pos);
         }
      }
      catch (BadLocationException e)
      {
      }
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

         String ref = newUrl.getRef();

         if (ref != null)
         {
            scrollToReference(ref);
         }
         else
         {
            String query = newUrl.getQuery();

            if (query != null)
            {
               int pos = 0;
               int idx = query.indexOf("pos=");

               if (idx > -1)
               {
                  try
                  {
                     pos = Integer.parseInt(query.substring(idx+5));
                  }
                  catch (NumberFormatException e)
                  {// do nothing (shouldn't happen)
                  }
               }

               if (pos > 0)
               {
                  scrollToPosition(pos);
               }
            }
         }

         helpLib.getHelpFrame().updateNavWidgets();
      }

      addBodyFontRuleToStyleSheet();
   }

   protected void addBodyFontRuleToStyleSheet()
   {
      HTMLDocument doc = (HTMLDocument)getDocument();
      StyleSheet styles = doc.getStyleSheet();

      styles.addRule(getBodyFontRule());
   }

   public void setFontStyle(String fontName, int fontSize)
   {
      this.fontSize = fontSize;
      this.fontName = fontName;

      fontNameNeedsQuotes = fontName.matches("[^\\p{IsAlphabetic}\\-]");

      addBodyFontRuleToStyleSheet();
   }

   public void setFontStyle(int fontSize)
   {
      this.fontSize = fontSize;

      addBodyFontRuleToStyleSheet();
   }

   public int getBodyFontSize()
   {
      return fontSize;
   }

   public String getBodyFontCssName()
   {
      return fontName;
   }

   public Font getBodyFont()
   {
      if (fontName.equals(FALLBACK_FONT_KEYWORD))
      {
         return new Font(FALLBACK_FONT_NAME, Font.PLAIN, fontSize);
      }
      else
      {
         return new Font(fontName, Font.PLAIN, fontSize);
      }
   }

   public String getBodyFontRule()
   {
      String rule;

      if (fontName.equals(FALLBACK_FONT_KEYWORD))
      {
         rule = String.format("body { font-family: %s; font-size: %d; }",
           fontName, fontSize);
      }
      else if (fontNameNeedsQuotes)
      {
         rule = String.format("body { font-family: \"%s\", %s; font-size: %d; }",
           fontName, FALLBACK_FONT_KEYWORD, fontSize);
      }
      else
      {
         rule = String.format("body { font-family: %s, %s; font-size: %d; }",
           fontName, FALLBACK_FONT_KEYWORD, fontSize);
      }

      return rule;
   }

   @Override
   public void hyperlinkUpdate(HyperlinkEvent evt)
   {
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
         URL url = evt.getURL();

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

            try
            {
               setPage(url);

               if (node != null)
               {
                  updateCurrentNode(node, ref);
               }
            }
            catch (Throwable t)
            {
               t.printStackTrace();
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
                  helpLib.error(helpLib.getMessage("error.browse_failed", url), e);
               }
            }
            else
            {
               helpLib.error(helpLib.getMessage("error.no_desktop", url));
            }
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

   protected TeXJavaHelpLib helpLib;
   protected NavigationNode currentNode;

   protected Vector<HistoryItem> history;
   protected int historyIdx = 0;

   public static final String FALLBACK_FONT_KEYWORD = "sans-serif";
   public static final String FALLBACK_FONT_NAME = "SansSerif";
   protected int fontSize = 12;
   protected String fontName = FALLBACK_FONT_KEYWORD;
   protected boolean fontNameNeedsQuotes = false;
}
