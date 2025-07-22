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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

/**
 * Simple non-editable HTML message dialog. Intended for about
 * dialogs or similar.
 */
public class MessageDialog extends JDialog
 implements HyperlinkListener,HelpFontChangeListener,ActionListener
{
   /**
    * Creates a message dialog with the given HTML content that
    * uses the TeXJavaHelpLib font settings and listens to font
    * changes.
    * @param parent the dialog owner
    * @param title the dialog title
    * @param modal true if modal
    * @param helpLib the TeXJavaHelpLib with the localisation
    * strings and font settings
    * @param bodyText the HTML body code (no head or body tags)
    */
   public MessageDialog(JFrame parent, String title, boolean modal,
     TeXJavaHelpLib helpLib, String bodyText)
   {
      this(parent, title, modal, helpLib, bodyText,
         helpLib.getHelpFontSettings());

      helpLib.addHelpFontChangeListener(this);
   }

   /**
    * Creates a message dialog with the given HTML content that
    * uses the given font settings and does not automatically listen to font
    * changes.
    * @param parent the dialog owner
    * @param title the dialog title
    * @param modal true if modal
    * @param helpLib the TeXJavaHelpLib with the localisation
    * strings and font settings
    * @param bodyText the HTML body code (no head or body tags)
    * @param fontSettings the font settings for the HTML document
    */
   public MessageDialog(JFrame parent, String title, boolean modal,
     TeXJavaHelpLib helpLib, String bodyText, HelpFontSettings fontSettings)
   {
      super(parent, title, modal);

      this.helpLib = helpLib;

      initEditor(bodyText, fontSettings);

      initGUI();
      setLocationRelativeTo(parent);
   }

   /**
    * Creates a message dialog with the given document content. 
    * Does not implement font settings or automatically listen to font changes.
    * Any font styling should be included in the content.
    * @param parent the dialog owner
    * @param title the dialog title
    * @param modal true if modal
    * @param helpLib the TeXJavaHelpLib with the localisation
    * strings
    * @param contentType the content type
    * @param content the document content
    */
   public MessageDialog(JFrame parent, String title, boolean modal,
     TeXJavaHelpLib helpLib, String contentType, String content)
   {
      super(parent, title, modal);

      this.helpLib = helpLib;

      editorPane = new TJHEditorPane(contentType, content);
      initGUI();
      setLocationRelativeTo(parent);
   }

   /**
    * Creates a message dialog with the given HTML content that
    * uses the TeXJavaHelpLib font settings and listens to font
    * changes.
    * @param parent the dialog owner
    * @param title the dialog title
    * @param modalityType the modality setting
    * @param helpLib the TeXJavaHelpLib with the localisation
    * strings and font settings
    * @param bodyText the HTML body code (no head or body tags)
    */
   public MessageDialog(JFrame parent, String title,
     Dialog.ModalityType modalityType,
     TeXJavaHelpLib helpLib, String bodyText)
   {
      this(parent, title, modalityType, helpLib, bodyText,
         helpLib.getHelpFontSettings());

      helpLib.addHelpFontChangeListener(this);
   }

   /**
    * Creates a message dialog with the given HTML content that
    * uses the given font settings and does not automatically listen to font
    * changes.
    * @param parent the dialog owner
    * @param title the dialog title
    * @param modalityType the modality setting
    * @param helpLib the TeXJavaHelpLib with the localisation
    * strings and font settings
    * @param bodyText the HTML body code (no head or body tags)
    * @param fontSettings the font settings for the HTML document
    */
   public MessageDialog(JFrame parent, String title,
     Dialog.ModalityType modalityType,
     TeXJavaHelpLib helpLib, String bodyText,
     HelpFontSettings fontSettings)
   {
      super(parent, title, modalityType);

      this.helpLib = helpLib;

      initEditor(bodyText, fontSettings);

      initGUI();
      setLocationRelativeTo(parent);
   }

   /**
    * Creates a message dialog with the given document content. 
    * Does not implement font settings or automatically listen to font changes.
    * Any font styling should be included in the content.
    * @param parent the dialog owner
    * @param title the dialog title
    * @param modalityType the modality setting
    * @param helpLib the TeXJavaHelpLib with the localisation
    * strings
    * @param contentType the content type
    * @param content the document content
    */
   public MessageDialog(JFrame parent, String title, 
     Dialog.ModalityType modalityType,
     TeXJavaHelpLib helpLib, String contentType, String content)
   {
      super(parent, title, modalityType);

      this.helpLib = helpLib;

      editorPane = new TJHEditorPane(contentType, content);
      initGUI();
      setLocationRelativeTo(parent);
   }

   /**
    * Creates a message dialog with the given HTML content obtained
    * from a URL that uses the TeXJavaHelpLib font settings and listens to font
    * changes.
    * @param parent the dialog owner
    * @param title the dialog title
    * @param modal true if modal
    * @param helpLib the TeXJavaHelpLib with the localisation
    * strings and font settings
    * @param url the source (includes HTML head)
    */
   public MessageDialog(JFrame parent, String title, boolean modal,
     TeXJavaHelpLib helpLib, URL url)
    throws IOException
   {
      super(parent, title, modal);

      this.helpLib = helpLib;

      initEditor(url, helpLib.getHelpFontSettings());

      helpLib.addHelpFontChangeListener(this);

      initGUI();
      setLocationRelativeTo(parent);
   }

   private void initEditor(String bodyText, HelpFontSettings fontSettings)
   {
      StringBuilder builder = new StringBuilder();
      builder.append("<html><head><style>");
      fontSettings.appendRules(builder);
      builder.append("</style></head><body>");
      builder.append(bodyText);
      builder.append("</body></html>");

      editorPane = new TJHEditorPane("text/html", builder.toString());
   }

   private void initEditor(URL url, HelpFontSettings fontSettings)
    throws IOException
   {
      editorPane = new TJHEditorPane(url);

      HTMLDocument htmlDoc = (HTMLDocument)editorPane.getDocument();
      StyleSheet styles = htmlDoc.getStyleSheet();

      fontSettings.addFontRulesToStyleSheet(styles);
   }

   protected void initGUI()
   {
      editorPane.addHyperlinkListener(this);

      getContentPane().add(new JScrollPane(editorPane), "Center");

      JComponent buttonPanel = new JPanel(new BorderLayout());
      getContentPane().add(buttonPanel, "South");

      westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

      buttonPanel.add(westPanel, "West");
      buttonPanel.add(eastPanel, "East");

      westPanel.add(new JButton(new TJHAbstractAction(helpLib, "button",
      "copy_all", (Boolean)null, getRootPane(),
       helpLib.getDefaultButtonActionOmitKeys())
      {
         @Override
         public void doAction()
         {
            copyContent();
         }
      }));

      JButton closeButton = helpLib.createCloseButton((JDialog)this);
      eastPanel.add(closeButton);

      pack();
   }

   public void addToWestPanel(Component comp)
   {
      westPanel.add(comp);
   }

   public void addToEastPanel(Component comp)
   {
      eastPanel.add(comp);
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if ("copy".equals(action))
      {
         copyContent();
      }
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

         if (url == null)
         {
            String desc = evt.getDescription();

            if (desc != null && desc.startsWith("#"))
            {
               desc = desc.substring(1);

               if (!desc.isEmpty())
               {
                  editorPane.scrollToReference(desc);
               }
            }
         }
         else
         {
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
   }

   @Override
   public void fontChanged(HelpFontChangeEvent event)
   {
      Document doc = editorPane.getDocument();

      if (doc instanceof HTMLDocument)
      {
         HTMLDocument htmlDoc = (HTMLDocument)doc;
         StyleSheet styles = htmlDoc.getStyleSheet();

         event.getSettings().addFontRulesToStyleSheet(
           styles, event.getModifiers());
      }
   }

   private TJHEditorPane editorPane;
   private TeXJavaHelpLib helpLib;

   protected JComponent westPanel, eastPanel;
}
