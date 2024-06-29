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

import java.awt.Desktop;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

/**
 * Simple non-editable HTML message dialog. Intended for about
 * dialogs or similar.
 */
public class MessageDialog extends JDialog implements HyperlinkListener
{
   public MessageDialog(JFrame parent, String title, boolean modal,
     TeXJavaHelpLib helpLib, String bodyText)
   {
      super(parent, title, modal);

      this.helpLib = helpLib;

      HelpFontSettings fontSettings = helpLib.getHelpFrame().getHelpFontSettings();

      StringBuilder builder = new StringBuilder();
      builder.append("<html><head><style>");
      fontSettings.appendRules(builder);
      builder.append("</style></head><body>");
      builder.append(bodyText);
      builder.append("</body></html>");

      editorPane = new JEditorPane("text/html", builder.toString());

      init();
      setLocationRelativeTo(parent);
   }

   public MessageDialog(JFrame parent, String title, boolean modal,
     TeXJavaHelpLib helpLib, String contentType, String content)
   {
      super(parent, title, modal);

      this.helpLib = helpLib;

      editorPane = new JEditorPane(contentType, content);
      init();
      setLocationRelativeTo(parent);
   }

   protected void init()
   {
      editorPane.setEditable(false);
      editorPane.addHyperlinkListener(this);

      getContentPane().add(new JScrollPane(editorPane), "Center");

      JComponent buttonPanel = new JPanel(new BorderLayout());
      getContentPane().add(buttonPanel, "South");

      TJHAbstractAction copyAction = new TJHAbstractAction(helpLib, "action", "copy")
       {
          @Override
          public void doAction()
          {
             copyContent();
          }
       };

      buttonPanel.add(createJButton(copyAction), "West");

      TJHAbstractAction closeAction = new TJHAbstractAction(helpLib, "action", "close")
       {
          @Override
          public void doAction()
          {
             setVisible(false);
          }
       };

      JButton closeButton = createJButton(closeAction);
      getRootPane().setDefaultButton(closeButton);
      buttonPanel.add(closeButton, "East");

      pack();
   }

   protected JButton createJButton(Action action)
   {
      JButton btn = new JButton(action);

      Dimension dim = btn.getPreferredSize();
      btn.setMaximumSize(dim);
      btn.setSize(dim);

      return btn;
   }

   public void copyContent()
   {
      int start = editorPane.getSelectionStart();
      int end = editorPane.getSelectionEnd();

      if (start == end)
      {
         editorPane.selectAll();
      }

      editorPane.copy();
   }

   protected void open(URL url) throws IOException
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

   @Override
   public void hyperlinkUpdate(HyperlinkEvent evt)
   {
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
         URL url = evt.getURL();

         try
         {
            open(url);
         }
         catch (IOException e)
         {
            helpLib.error(e);
         }
      }
   }

   private JEditorPane editorPane;
   private TeXJavaHelpLib helpLib;
}
