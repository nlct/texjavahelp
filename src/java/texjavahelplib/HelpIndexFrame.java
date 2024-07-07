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

import java.util.TreeSet;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;


/**
 * Frame for showing search results.
 */
public class HelpIndexFrame extends JFrame
 implements HyperlinkListener,HelpFontChangeListener
{
   public HelpIndexFrame(final HelpFrame helpFrame,
     TreeSet<IndexItem> indexGroupData, URL indexURL)
    throws IOException
   {
      super(helpFrame.getHelpLib().getMessage("help.navigation.index.title"));

      this.helpFrame = helpFrame;
      this.indexURL = indexURL;

      init(indexGroupData);

      helpFrame.getHelpLib().addHelpFontChangeListener(this);
   }

   protected void init(TreeSet<IndexItem> indexGroupData)
    throws IOException
   {
      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();
      HelpFontSettings fontSettings = helpLib.getHelpFontSettings();

      StringBuilder builder = new StringBuilder();

      builder.append("<html><style>h2 { margin-left: 5pt; margin-top: 2.5pt; margin-bottom: 2.5pt; } ");
      fontSettings.appendRules(builder);
      builder.append("</style><body>");

      for (IndexItem item : indexGroupData)
      {
         builder.append("<h2><a href=\"");

         builder.append(item.getTarget());

         builder.append("\">");

         builder.append(item.getName());

         builder.append("</a></h2>");
      }

      builder.append("</body></html>");

      groupPane = new TJHEditorPane("text/html", builder.toString());
      groupPane.addHyperlinkListener(this);

      editorPane = new TJHEditorPane(indexURL);
      editorPane.addHyperlinkListener(this);

      JSplitPane splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        new JScrollPane(groupPane), new JScrollPane(editorPane));

      splitPane.setOneTouchExpandable(true);
      splitPane.setResizeWeight(0.25);

      getContentPane().add(splitPane, "Center");

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width/3, dim.height/2);
   }

   public void open()
   {
      if (isVisible())
      {
         toFront();
      }
      else
      {
         setLocationRelativeTo(helpFrame);

         setVisible(true);
      }
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpFrame.getHelpLib();
   }

   public void goToIndexRef(String ref)
   {
      editorPane.scrollToReference(ref);
   }

   protected void open(URL url) throws IOException
   {
      String ref = url.getRef();

      if (indexURL.sameFile(url) && ref != null && !ref.isEmpty())
      {
         // likely a cross-reference
         goToIndexRef(ref);
      }
      else
      {
         helpFrame.open(url);
         helpFrame.requestHelpPageFocus();
      }
   }

   @Override
   public void hyperlinkUpdate(HyperlinkEvent evt)
   {
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
         if (evt.getSource() == groupPane)
         {
            goToIndexRef(evt.getDescription());
         }
         else
         {
            try
            {
               open(evt.getURL());
            }
            catch (IOException e)
            {
               getHelpLib().error(e);
            }
         }
      }
      else if (evt.getSource() == editorPane)
      {
         if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED)
         {
            editorPane.setToolTipText(evt.getDescription());  
         }
         else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED)
         {     
            editorPane.setToolTipText(null);
         }  
      }
   }

   @Override
   public void fontChanged(HelpFontChangeEvent evt)
   {
      HelpFontSettings fontSettings = evt.getSettings();

      HTMLDocument doc = (HTMLDocument)editorPane.getDocument();
      StyleSheet styles = doc.getStyleSheet();

      fontSettings.addFontRulesToStyleSheet(styles, evt.getModifiers());

      doc = (HTMLDocument)groupPane.getDocument();
      styles = doc.getStyleSheet();

      fontSettings.addFontRulesToStyleSheet(styles, evt.getModifiers());
   }


   protected HelpFrame helpFrame;
   protected TJHEditorPane editorPane, groupPane;
   protected URL indexURL;
}
