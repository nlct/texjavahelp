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

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.URL;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import java.awt.event.KeyEvent;

import org.xml.sax.SAXException;

import com.dickimawbooks.texparserlib.TeXApp;
import com.dickimawbooks.texparserlib.TeXParser;

public class TeXJavaHelpLib
{
   public TeXJavaHelpLib(TeXJavaHelpLibApp application) throws IOException
   {
      this(application, Locale.getDefault(), Locale.getDefault());
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
       Locale messagesLocale, Locale helpsetLocale)
     throws IOException
   {
      this(application, application.getApplicationName(),
         application.getApplicationName().toLowerCase().replaceAll(" ", ""),
         "/resources", messagesLocale, helpsetLocale);
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String dictPrefix, String resourcebase)
    throws IOException
   {
      this(application, applicationName, dictPrefix, resourcebase,
        Locale.getDefault(), Locale.getDefault());
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String dictPrefix, String resourcebase,
      Locale messagesLocale, Locale helpsetLocale)
    throws IOException
   {
      this.applicationName = applicationName;
      this.resourcebase = resourcebase;
      this.resourceIconBase = resourcebase + "/icons";
      this.helpsetLocale = helpsetLocale;

      messages = new MessageSystem(getResourcePath(), "texjavahelplib", messagesLocale);

      if (dictPrefix != null)
      {
         messages.loadDictionary(dictPrefix);
      }
   }

   public static String encodeHTML(String str, boolean encodeQuotes)
   {
      if (str.isEmpty()) return str;

      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < str.length(); )
      {
         int cp = str.codePointAt(i);
         i += Character.charCount(cp);

         if (cp == '&')
         {
            builder.append("&amp;");
         }
         else if (cp == '<')
         {
            builder.append("&lt;");
         }
         else if (cp == '>')
         {
            builder.append("&gt;");
         }
         else if (encodeQuotes && (cp == '"' || cp == '\''))
         {
            builder.append(String.format("&#x%x;", cp));
         }
         else
         {
            builder.appendCodePoint(cp);
         }
      }

      return builder.toString();
   }

   public MessageSystem getMessageSystem()
   {
      return messages;
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      return messages.getMessageWithFallback(label, fallbackFormat, params);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      return messages.getMessageIfExists(label, args);
   }

   public String getMessage(String label, Object... params)
   {
      String msg = messages.getMessage(label, params);

      if (msg == null)
      {
         warning("Can't find message for label: "+label);

         return label;
      }

      return msg;
   }

   public String getChoiceMessage(String label, int argIdx,
      String choiceLabel, int numChoices, Object... args)
   {
      return messages.getChoiceMessage(label, argIdx, choiceLabel, numChoices, args);
   }

   public void warning(String message)
   {
      if (application == null)
      {
         System.err.println(message);
      }
      else
      {
         application.warning(message);
      }
   }

   public void warning(String message, Throwable t)
   {
      if (application == null)
      {
         System.err.println(message);
         t.printStackTrace();
      }
      else
      {
         application.warning(message, t);
      }
   }

   public void error(String message)
   {
      if (application == null)
      {
         System.err.println(message);
      }
      else
      {
         application.error(message);
      }
   }

   public void error(Exception e)
   {
      if (application == null)
      {
         String msg = e.getMessage();

         if (msg == null)
         {
            msg = e.toString();
         }

         System.err.println(msg);

         e.printStackTrace();
      }
      else
      {
         application.error(e);
      }
   }

   public void error(String message, Exception e)
   {
      if (application == null)
      {
         System.err.println(message);

         e.printStackTrace();
      }
      else
      {
         application.error(message, e);
      }
   }

   public String getApplicationName()
   {
      return applicationName;
   }

   public void setTeXJavaHelpLibApp(TeXJavaHelpLibApp application)
   {
      this.application = application;
   }

   public TeXJavaHelpLibApp getApplication()
   {
      return application;
   }

   public String getResourcePath()
   {
      return resourcebase;
   }

   public String getIconPath()
   {
      return resourceIconBase;
   }

   public void setIconPath(String relpath)
   {
      resourceIconBase = relpath;
   }

   public String getSmallIconSuffix()
   {
      return smallIconSuffix;
   }

   public void setSmallIconSuffix(String suffix)
   {
      smallIconSuffix = suffix;
   }

   public ImageIcon getSmallIcon(String base)
   {
      return getSmallIcon(base, "png", "jpg", "jpeg", "gif");
   }

   public ImageIcon getSmallIcon(String base, String... extensions)
   {
      String basename = resourceIconBase + "/" + base + smallIconSuffix;

      for (String ext : extensions)
      {
         URL url = getClass().getResource(basename + "." + ext);

         if (url != null)
         {
            return new ImageIcon(url);
         }
      }

      return null;
   }

   public String getLargeIconSuffix()
   {
      return largeIconSuffix;
   }

   public void setLargeIconSuffix(String suffix)
   {
      largeIconSuffix = suffix;
   }

   public ImageIcon getLargeIcon(String base)
   {
      return getLargeIcon(base, "png", "jpg", "jpeg", "gif");
   }

   public ImageIcon getLargeIcon(String base, String... extensions)
   {
      String basename = resourceIconBase + "/" + base + largeIconSuffix;

      for (String ext : extensions)
      {
         URL url = getClass().getResource(basename + "." + ext);

         if (url != null)
         {
            return new ImageIcon(url);
         }
      }

      return null;
   }

   public String getHelpSetResourcePath()
   {
      if (helpsetsubdir == null || helpsetsubdir.isEmpty())
      {
         return resourcebase + "/" + helpsetdir;
      }
      else
      {
         return resourcebase + "/" + helpsetdir + "/" + helpsetsubdir;
      }
   }

   public URL getHelpSetResource(String filename)
    throws FileNotFoundException
   {
      String path = getHelpSetResourcePath() + "/" + filename;

      URL url = getClass().getResource(path);

      if (url == null)
      {
         throw new FileNotFoundException(
           getMessage("error.resource_not_found", path));
      }

      return url;
   }

   public InputStream getNavigationXMLInputStream()
     throws FileNotFoundException
   {
      String path;
      InputStream stream = null;

      if (helpsetLocale == null || helpsetsubdir != null)
      {
         path = getHelpSetResourcePath() + "/" + navxmlfilename;
         stream = getClass().getResourceAsStream(path);
      }
      else
      {
         String base = resourcebase + "/" + helpsetdir;

         helpsetsubdir = helpsetLocale.toLanguageTag();

         path = base + "/" + helpsetsubdir + "/" + navxmlfilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            String lang = helpsetLocale.getLanguage();
            String country = helpsetLocale.getCountry();
            String tag = lang + "-" + country;

            if (country == null || country.isEmpty() || helpsetsubdir.equals(tag))
            {
               helpsetsubdir = lang;

               path = base + "/" + helpsetsubdir + "/" + navxmlfilename;
            }
            else
            {
               helpsetsubdir = tag;

               path = base + "/" + helpsetsubdir + "/" + navxmlfilename;

               stream = getClass().getResourceAsStream(path);

               if (stream == null)
               {
                  helpsetsubdir = lang;

                  path = base + "/" + helpsetsubdir + "/" + navxmlfilename;
               }
            }

            stream = getClass().getResourceAsStream(path);

            if (stream == null)
            {
               String script = helpsetLocale.getScript();

               if (script != null && !script.isEmpty())
               {
                  helpsetsubdir = lang + "-" + script;

                  path = base + "/" + helpsetsubdir
                     + "/" + navxmlfilename;

                  stream = getClass().getResourceAsStream(path);
               }

               if (stream == null)
               {
                  path = base + "/" + navxmlfilename;
                  helpsetsubdir = "";

                  stream = getClass().getResourceAsStream(path);
               }
            }
         }
      }

      if (stream == null)
      {
         throw new FileNotFoundException(
           getMessage("error.resource_not_found", path));
      }

      return stream;
   }

   public void initHelpSet()
    throws IOException,SAXException
   {
      initHelpSet("helpset", "navigation");
   }

   public void initHelpSet(String helpsetdir, String navBaseName)
    throws IOException,SAXException
   {
      initHelpSet(helpsetdir, navBaseName,
        getMessageWithFallback("manual.title", "Manual"));
   }

   public void initHelpSet(String helpsetdir, String navBaseName, String title)
    throws IOException,SAXException
   {
      navhtmlfilename = navBaseName+"."+htmlsuffix;
      navxmlfilename = navBaseName+".xml";

      navigationTree = NavigationTree.load(this);

      helpFrame = new HelpFrame(this, title);
   }

   public NavigationTree getNavigationTree()
   {
      return navigationTree;
   }

   // GUI components

   public HelpFrame getHelpFrame()
   {
      return helpFrame;
   }

   public void openHelp() throws HelpSetNotInitialisedException
   {
      if (helpFrame == null)
      {
         throw new HelpSetNotInitialisedException(
           getMessageWithFallback(
           "error.no_helpset", "Helpset has not been initialised"));
      }

      helpFrame.setVisible(true);
      helpFrame.toFront();
   }

   public void openHelpForId(String id)
    throws UnknownNodeException,IOException,HelpSetNotInitialisedException
   {
      if (helpFrame == null)
      {
         throw new HelpSetNotInitialisedException(
           getMessageWithFallback(
           "error.no_helpset", "Helpset has not been initialised"));
      }

      NavigationNode node = navigationTree.getNodeById(id);

      if (node == null)
      {
         throw new UnknownNodeException(getMessageWithFallback(
           "error.node_id_not_found", "Node with ID ''{0}'' not found", id));
      }

      helpFrame.setPage(node);
      openHelp();
   }

   public TJHAbstractAction createHelpAction()
   {
      return new TJHAbstractAction(this,
        "menu.help", "manual", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))
        {
           @Override
           public void doAction()
           {
              helpLib.openHelp();
           }
        };
   }

   public TJHAbstractAction createHelpAction(String helpID)
   {
      return createHelpAction(helpID, "action", "help", 
       "help", "help", null, null);
   }

   public TJHAbstractAction createHelpAction(String helpID, JComponent comp)
   {
      return createHelpAction(helpID, "action", "help", "help", "help", 
       KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.SHIFT_MASK), comp);
   }

   public TJHAbstractAction createHelpAction(String helpID,
      KeyStroke keyStroke, JComponent comp)
   {
      return createHelpAction(helpID, "action", "help", 
       "manual."+helpID, "help", keyStroke, comp);
   }

   public TJHAbstractAction createHelpAction(String helpID,
      String msgParentTag, String childTag, String action, String iconPrefix)
   {
      return createHelpAction(helpID, msgParentTag, childTag, action, iconPrefix,
        null, null);
   }

   public TJHAbstractAction createHelpAction(final String helpID,
      String msgParentTag, String childTag, String action,
      String iconPrefix, KeyStroke keyStroke, JComponent comp)
   {
      return  new TJHAbstractAction(this,
        msgParentTag, childTag, action, iconPrefix, keyStroke, null, comp)
        {
           @Override
           public void doAction()
           {
              try
              {
                 helpLib.openHelpForId(helpID);
              }
              catch (Exception e)
              {
                 helpLib.getApplication().error(e);
              }
           }
        };
   }

   public int getMnemonic(String label)
   {
      String text = getMessageIfExists(label);

      if (text == null || text.isEmpty()) return -1;

      return text.codePointAt(0);
   }

   public JMenu createJMenu(String tag)
   {
      JMenu jmenu = new JMenu(getMessage(tag));

      int mnemonic = getMnemonic(tag+".mnemonic");

      if (mnemonic > 0)
      {
         jmenu.setMnemonic(mnemonic);
      }

      String tooltip = getMessageIfExists(tag+".tooltip");

      if (tooltip != null)
      {
         jmenu.setToolTipText(tooltip);
      }

      String desc = getMessageIfExists(tag+".description");

      if (desc != null)
      {
         jmenu.getAccessibleContext().setAccessibleDescription(desc);
      }

      return jmenu;
   }

   public JMenuItem createJMenuItem(String tag)
   {
      return createJMenuItem(tag, null, null);
   }

   public JMenuItem createJMenuItem(String parentTag, String action,
     ActionListener actionListener)
   {
      return createJMenuItem(parentTag, action, actionListener, null);
   }

   public JMenuItem createJMenuItem(String parentTag, String action,
     ActionListener actionListener, KeyStroke accelerator)
   {
      String tag = action == null ? parentTag : parentTag+"."+action;

      JMenuItem item = new JMenuItem(getMessage(tag));

      if (action != null)
      {
         item.setActionCommand(action);
      }

      if (actionListener != null)
      {
         item.addActionListener(actionListener);
      }

      if (accelerator != null)
      {
         item.setAccelerator(accelerator);
      }

      int mnemonic = getMnemonic(tag+".mnemonic");

      if (mnemonic > 0)
      {
         item.setMnemonic(mnemonic);
      }

      String tooltip = getMessageIfExists(tag+".tooltip");

      if (tooltip != null)
      {
         item.setToolTipText(tooltip);
      }

      String desc = getMessageIfExists(tag+".description");

      if (desc != null)
      {
         item.getAccessibleContext().setAccessibleDescription(desc);
      }

      return item;
   }

   public JLabel createJLabel(String tag)
   {
      JLabel jlabel = new JLabel(getMessage(tag));

      int mnemonic = getMnemonic(tag+".mnemonic");

      if (mnemonic > 0)
      {
         jlabel.setDisplayedMnemonic(mnemonic);
      }

      String tooltip = getMessageIfExists(tag+".tooltip");

      if (tooltip != null)
      {
         jlabel.setToolTipText(tooltip);
      }

      String desc = getMessageIfExists(tag+".description");

      if (desc != null)
      {
         jlabel.getAccessibleContext().setAccessibleDescription(desc);
      }

      return jlabel;
   }

   public JButton createJButton(String tag)
   {
      return createJButton(tag, null, null);
   }

   public JButton createJButton(String parentTag, String action,
     ActionListener actionListener)
   {
      String tag = action == null ? parentTag : parentTag+"."+action;

      JButton button = new JButton(getMessage(tag));

      if (action != null)
      {
         button.setActionCommand(action);
      }

      if (actionListener != null)
      {
         button.addActionListener(actionListener);
      }

      int mnemonic = getMnemonic(tag+".mnemonic");

      if (mnemonic > 0)
      {
         button.setMnemonic(mnemonic);
      }

      String tooltip = getMessageIfExists(tag+".tooltip");

      if (tooltip != null)
      {
         button.setToolTipText(tooltip);
      }

      String desc = getMessageIfExists(tag+".description");

      if (desc != null)
      {
         button.getAccessibleContext().setAccessibleDescription(desc);
      }

      return button;
   }

   protected String resourcebase = "/resources";
   protected String resourceIconBase = "/resources/icons";
   protected String smallIconSuffix = "-16x16";
   protected String largeIconSuffix = "-32x32";

   protected String helpsetdir = "helpset";
   protected String helpsetsubdir = null;
   protected Locale helpsetLocale;
   protected NavigationTree navigationTree;
   protected String navhtmlfilename, navxmlfilename;
   protected String htmlsuffix = "html";

   protected HelpFrame helpFrame;

   protected MessageSystem messages;
   protected String applicationName;
   protected TeXJavaHelpLibApp application;
}
