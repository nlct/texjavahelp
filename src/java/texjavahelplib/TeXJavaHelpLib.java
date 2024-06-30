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
import java.util.HashMap;
import java.util.Vector;
import java.util.TreeSet;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.URL;

import java.awt.Font;
import java.awt.Insets;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBox;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Action;

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
         "/resources", messagesLocale, helpsetLocale,
         application.getApplicationName().toLowerCase().replaceAll(" ", ""));
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String resourcebase,
      Locale messagesLocale, Locale helpsetLocale, String... dictPrefixes)
    throws IOException
   {
      this(application, applicationName, resourcebase,
       resourcebase, messagesLocale, helpsetLocale, dictPrefixes);
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String resourcebase,
      String dictionaryBase,
      Locale messagesLocale, Locale helpsetLocale, String... dictPrefixes)
    throws IOException
   {
      this.application = application;
      this.applicationName = applicationName;

      if (resourcebase.endsWith("/"))
      {
         this.resourcebase = resourcebase.substring(0, resourcebase.length()-1);
      }
      else
      {
         this.resourcebase = resourcebase;
      }

      if (dictionaryBase.endsWith("/"))
      {
         this.dictionaryBase = dictionaryBase.substring(0, dictionaryBase.length()-1);
      }
      else
      {
         this.dictionaryBase = dictionaryBase;
      }

      this.resourceIconBase = resourcebase + "/icons";
      this.helpsetLocale = helpsetLocale;

      messages = new MessageSystem(this, "texjavahelplib", messagesLocale);

      for (String dictPrefix : dictPrefixes)
      {
         if (dictPrefix.endsWith("-"))
         {
            dictPrefix = dictPrefix.substring(0, dictPrefix.length()-1);
         }

         messages.loadDictionary(dictPrefix);
      }

      helpFontSettings = new HelpFontSettings();
   }

   public void dictionaryLoaded(URL url)
   {
      application.dictionaryLoaded(url);
   }

   public Locale getHelpSetLocale()
   {
      return helpsetLocale;
   }

   public Locale getMessagesLocale()
   {
      return messages.getLocale();
   }

   /**
    * Encodes HTML special characters. See also
    * HtmlTag.encodeAttributeValue(String,boolean)
    * @param str HTML string
    * @param encodeQuotes true if quotes should be converted to
    * entities
    *  @return encoded string
    */ 
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

   public void message(String message)
   {
      if (application == null)
      {
         System.out.println(message);
      }
      else
      {
         application.message(message);
      }
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

   public void debug(Throwable e)
   {
      if (application == null)
      {
         e.printStackTrace();
      }
      else
      {
         application.debug(e);
      }
   }

   public void debug(String message)
   {
      if (application == null)
      {
         System.err.println(message);
      }
      else
      {
         application.debug(message);
      }
   }

   public void debug(String message, Throwable e)
   {
      if (application == null)
      {
         System.err.println(message);

         e.printStackTrace();
      }
      else
      {
         application.debug(message, e);
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

   public String getDictionaryPath()
   {
      return dictionaryBase;
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

   public ImageIcon getHelpIcon(String base, boolean small)
   {
      return getHelpIcon(base, small, "png", "jpg", "jpeg", "gif");
   }

   public ImageIcon getHelpIcon(String base, boolean small, String... extensions)
   {
      /* Try the icon path first to allow application icons to take
         precedence.
       */

      ImageIcon ic = small ? getSmallIcon(base) : getLargeIcon(base);

      if (ic == null)
      {
         // Use icon provided in texjavahelplib.jar

         InputStream in = null;

         String suffix = small ? smallIconSuffix : largeIconSuffix;

         in = getClass().getResourceAsStream(
           "/com/dickimawbooks/texjavahelplib/icons/"+base+suffix+".png");

         if (in != null)
         {
            try
            {
               ic = new ImageIcon(ImageIO.read(in));
            }
            catch (IOException e)
            {
            }
         }
      }

      return ic;
   }

   public ImageIcon getSmallIcon(String base)
   {
      return getSmallIcon(base, "png", "jpg", "jpeg", "gif");
   }

   public ImageIcon getSmallIcon(String base, String... extensions)
   {
      ImageIcon ic = application.getSmallIcon(base, extensions);

      if (ic != null)
      {
         return ic;
      }

      String basename = resourceIconBase;

      if (!resourceIconBase.endsWith("/"))
      {
         basename += "/";
      }

      basename += base + smallIconSuffix;

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
      ImageIcon ic = application.getLargeIcon(base, extensions);

      if (ic != null)
      {
         return ic;
      }

      String basename = resourceIconBase;

      if (!resourceIconBase.endsWith("/"))
      {
         basename += "/";
      }

      basename += base + largeIconSuffix;

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

   public void setHelpsetSubDirPrefix(String prefix)
   {
      if (prefix == null)
      {
         throw new NullPointerException();
      }

      helpsetSubdirPrefix = prefix;
   }

   public String getHelpSetResourcePath()
   {
      if (helpsetsubdir == null || helpsetsubdir.isEmpty())
      {
         return resourcebase + "/" + helpsetdir;
      }
      else
      {
         return resourcebase + "/" + helpsetdir + "/"
            + helpsetSubdirPrefix+helpsetsubdir;
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

         path = base + "/" + helpsetSubdirPrefix+helpsetsubdir + "/" + navxmlfilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            String lang = helpsetLocale.getLanguage();
            String country = helpsetLocale.getCountry();
            String tag = lang + "-" + country;

            if (country == null || country.isEmpty() || helpsetsubdir.equals(tag))
            {
               helpsetsubdir = lang;

               path = base + "/" + helpsetSubdirPrefix+helpsetsubdir
                    + "/" + navxmlfilename;
            }
            else
            {
               helpsetsubdir = tag;

               path = base + "/" + helpsetSubdirPrefix+helpsetsubdir
                     + "/" + navxmlfilename;

               stream = getClass().getResourceAsStream(path);

               if (stream == null)
               {
                  helpsetsubdir = lang;

                  path = base + "/" + helpsetSubdirPrefix+helpsetsubdir
                       + "/" + navxmlfilename;
               }
            }

            stream = getClass().getResourceAsStream(path);

            if (stream == null)
            {
               String script = helpsetLocale.getScript();

               if (script != null && !script.isEmpty())
               {
                  helpsetsubdir = lang + "-" + script;

                  path = base + "/" + helpsetSubdirPrefix+helpsetsubdir
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

   public InputStream getIndexXMLInputStream()
     throws FileNotFoundException
   {
      String path;
      InputStream stream = null;

      if (helpsetLocale == null || helpsetsubdir != null)
      {
         path = getHelpSetResourcePath() + "/" + indexXmlFilename;
         stream = getClass().getResourceAsStream(path);
      }
      else
      {
         String base = resourcebase + "/" + helpsetdir;

         helpsetsubdir = helpsetLocale.toLanguageTag();

         path = base + "/" + helpsetsubdir + "/" + indexXmlFilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            String lang = helpsetLocale.getLanguage();
            String country = helpsetLocale.getCountry();
            String tag = lang + "-" + country;

            if (country == null || country.isEmpty() || helpsetsubdir.equals(tag))
            {
               helpsetsubdir = lang;

               path = base + "/" + helpsetsubdir + "/" + indexXmlFilename;
            }
            else
            {
               helpsetsubdir = tag;

               path = base + "/" + helpsetsubdir + "/" + indexXmlFilename;

               stream = getClass().getResourceAsStream(path);

               if (stream == null)
               {
                  helpsetsubdir = lang;

                  path = base + "/" + helpsetsubdir + "/" + indexXmlFilename;
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
                     + "/" + indexXmlFilename;

                  stream = getClass().getResourceAsStream(path);
               }

               if (stream == null)
               {
                  path = base + "/" + indexXmlFilename;
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

   public InputStream getSearchXMLInputStream()
     throws FileNotFoundException
   {
      String path;
      InputStream stream = null;

      if (helpsetLocale == null || helpsetsubdir != null)
      {
         path = getHelpSetResourcePath() + "/" + searchXmlFilename;
         stream = getClass().getResourceAsStream(path);
      }
      else
      {
         String base = resourcebase + "/" + helpsetdir;

         helpsetsubdir = helpsetLocale.toLanguageTag();

         path = base + "/" + helpsetsubdir + "/" + searchXmlFilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            String lang = helpsetLocale.getLanguage();
            String country = helpsetLocale.getCountry();
            String tag = lang + "-" + country;

            if (country == null || country.isEmpty() || helpsetsubdir.equals(tag))
            {
               helpsetsubdir = lang;

               path = base + "/" + helpsetsubdir + "/" + searchXmlFilename;
            }
            else
            {
               helpsetsubdir = tag;

               path = base + "/" + helpsetsubdir + "/" + searchXmlFilename;

               stream = getClass().getResourceAsStream(path);

               if (stream == null)
               {
                  helpsetsubdir = lang;

                  path = base + "/" + helpsetsubdir + "/" + searchXmlFilename;
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
                     + "/" + searchXmlFilename;

                  stream = getClass().getResourceAsStream(path);
               }

               if (stream == null)
               {
                  path = base + "/" + searchXmlFilename;
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

   public void initHelpSet(String helpsetdir)
    throws IOException,SAXException
   {
      initHelpSet(helpsetdir, "navigation");
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
      this.helpsetdir = helpsetdir;

      navhtmlfilename = navBaseName+"."+htmlsuffix;
      navxmlfilename = navBaseName+".xml";

      navigationTree = NavigationTree.load(this);

      indexData = IndexItem.load(this);
      targetMap = new HashMap<String,TargetRef>();

      indexGroupList = new TreeSet<IndexItem>();

      for (IndexItem item : indexData)
      {
         NavigationNode node = null;

         String filename = item.getFileName();

         if (filename != null)
         {
            if (item.getKey().startsWith("docindex."))
            {
               indexGroupList.add(item);
            }

            URL url = getHelpSetResource(filename);

            node = navigationTree.getNodeByURL(url);

            if (node != null)
            {
               String ref = item.getTarget();

               if (ref.matches("wrglossary\\.\\d+"))
               {
                  item.setName(node.getTitle());
               }
               else
               {
                  String text = item.brief();

                  if (text.isEmpty() || text.matches("\\d+(\\.\\d+)*"))
                  {
                     item.setName(text+" "+node.getTitle());
                  }
               }

               TargetRef targetRef = new TargetRef(item, ref, node);
               targetMap.put(ref, targetRef);
            }
         }
      }

      searchData = SearchData.load(this);

      helpFrame = new HelpFrame(this, title);
   }

   public TreeSet<IndexItem> getIndexGroupData()
   {
      return indexGroupList;
   }

   public NavigationNode getIndexNode()
   {
      return getNavigationNodeById("docindex");
   }

   public NavigationNode getNavigationNodeById(String id)
   {
      return navigationTree == null ? null : navigationTree.getNodeById(id);
   }

   public SearchData getSearchData()
   {
      return searchData;
   }

   public TargetRef getTargetRef(String ref)
   {
      return targetMap.get(ref);
   }

   public NavigationTree getNavigationTree()
   {
      return navigationTree;
   }

   public String preProcessSearchWordList(String str)
   {
      return str.replaceAll("\u2019", "'");
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

   public void openHelp(NavigationNode node)
    throws UnknownNodeException,IOException,HelpSetNotInitialisedException
   {
      if (helpFrame == null)
      {
         throw new HelpSetNotInitialisedException(
           getMessageWithFallback(
           "error.no_helpset", "Helpset has not been initialised"));
      }

      helpFrame.setPage(node);
      openHelp();
   }

   public void setKeyStrokeProperty(String propertyName, KeyStroke keyStroke)
   {
      if (resourceProperties == null)
      {
         resourceProperties = new HashMap<String,Object>();
      }

      resourceProperties.put(propertyName, keyStroke);
   }

   public KeyStroke getKeyStroke(String property)
   {
      KeyStroke keyStroke = null;

      if (resourceProperties != null)
      {
         Object value = resourceProperties.get(property);

         if (value instanceof KeyStroke)
         {
            keyStroke = (KeyStroke)value;
         }
      }

      if (keyStroke == null)
      {
         String text = getMessageIfExists(property+".keystroke");

         if (text != null && !text.isEmpty())
         {
            keyStroke = KeyStroke.getKeyStroke(text);
         }
      }

      return keyStroke;
   }

   public void setFontProperty(String propertyName, Font font)
   {
      if (resourceProperties == null)
      {
         resourceProperties = new HashMap<String,Object>();
      }

      resourceProperties.put(propertyName, font);
   }

   public Font getFontStyle(String property)
   {
      Font font = null;

      if (resourceProperties != null)
      {
         Object value = resourceProperties.get(property);

         if (value instanceof Font)
         {
            font = (Font)value;
         }
      }

      if (font == null)
      {
         String text = getMessageIfExists(property+".fontstyle");

         if (text != null && !text.isEmpty())
         {
            font = Font.decode(text);
         }
      }

      return font;
   }

   public void notifyFontChange(HelpFontChangeEvent evt)
   {
      if (listeners != null)
      {
         for (HelpFontChangeListener listener : listeners)
         {
            listener.fontChanged(evt);

            if (evt.isConsumed())
            {
               break;
            }
         }
      }
   }

   public void addHelpFontChangeListener(HelpFontChangeListener listener)
   {
      if (listeners == null)
      {
         listeners = new Vector<HelpFontChangeListener>();
      }

      listeners.add(listener);
   }

   public HelpFontSettings getHelpFontSettings()
   {
      return helpFontSettings;
   }

   public TJHAbstractAction createHelpAction()
   {
      return new TJHAbstractAction(this,
        "menu.help", "manual", getKeyStroke("menu.help.manual"))
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
       getKeyStroke("action.help"), comp);
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

   public TJHAbstractAction createHelpAction(NavigationNode node, JComponent comp)
   {
      return createHelpAction(node, "action", "help", "help", "help", 
       getKeyStroke("action.help"), comp);
   }

   public TJHAbstractAction createHelpAction(final NavigationNode node,
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
                 helpLib.openHelp(node);
              }
              catch (Exception e)
              {
                 helpLib.getApplication().error(e);
              }
           }
        };
   }

   public HelpDialogAction createHelpDialogAction(JDialog owner, String helpId)
    throws IllegalArgumentException
   {
      NavigationNode node = getNavigationNodeById(helpId);

      if (node == null)
      {
         throw new IllegalArgumentException(
            getMessage("error.node_id_not_found", helpId));
      }

      return new HelpDialogAction(owner, node, this);
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
      return createJMenuItem(tag, null, null, null);
   }

   public JMenuItem createJMenuItem(String parentTag, String action,
     ActionListener actionListener)
   {
      return createJMenuItem(parentTag, action, actionListener, 
        getKeyStroke(action == null ? parentTag : parentTag+"."+action));
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

   /**
    * Creates a JButton from an action without showing text or
    * border. Note that this overrides any text specified by the
    * action.
    */
   public JButton createToolBarButton(Action action)
   {
      JButton btn = new JButton(action);
      btn.setText(null);
      btn.setMargin(new Insets(0, 0, 0, 0));
      return btn;
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

   public JCheckBox createJCheckBox(String tag)
   {
      return createJCheckBox(tag, null, false);
   }

   public JCheckBox createJCheckBox(String parentTag, String action,
     boolean selected)
   {
      String tag = action == null ? parentTag : parentTag+"."+action;

      JCheckBox button = new JCheckBox(getMessage(tag), selected);

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
   protected String dictionaryBase = resourcebase;
   protected String resourceIconBase = "/resources/icons";
   protected String smallIconSuffix = "-16x16";
   protected String largeIconSuffix = "-32x32";

   protected String helpsetdir = "helpset";
   protected String helpsetsubdir = null;
   protected String helpsetSubdirPrefix = "";
   protected Locale helpsetLocale;
   protected NavigationTree navigationTree;
   protected String navhtmlfilename, navxmlfilename;
   protected String htmlsuffix = "html";

   protected String indexXmlFilename = "index.xml";
   protected Vector<IndexItem> indexData;
   protected TreeSet<IndexItem> indexGroupList;
   protected HashMap<String,TargetRef> targetMap;

   protected String searchXmlFilename = "search.xml";
   protected SearchData searchData;

   protected HelpFrame helpFrame;

   protected MessageSystem messages;
   protected String applicationName;
   protected TeXJavaHelpLibApp application;

   protected HashMap<String,Object> resourceProperties;

   private Vector<HelpFontChangeListener> listeners; 

   private HelpFontSettings helpFontSettings;

   public static final String KEYSTROKE_CSS
    = ".keystroke { font-family: sans-serif; font-weight: bold; border: 2pt outset gray; background-color: silver; }";

   public static final String ICON_CSS_CLASSES = ".icon, .locationprefix";

   public static final String ICON_CSS = ICON_CSS_CLASSES
    + " { font-family: serif; }"; 

   public static final String MONO_CSS_CLASSES
    = ".code, .cmd, .cmdfmt, .csfmt, .csfmtfont, .csfmtcolourfont, .appfmt, .styfmt, .clsfmt, .envfmt, .optfmt, .csoptfmt, .styoptfmt, .clsoptfmt, .ctrfmt, .filefmt, .extfmt, .cbeg, .cend, .longargfmt, .shortargfmt, .qtt, .xmltagfmt, .varfmt, .terminal, .transcript, .filedef, .codebox, .badcodebox, .unicodebox, .compactcodebox, .sidebysidecode";

   public static final String VERSION = "0.2a";
   public static final String VERSION_DATE = "2024-06-30";
}
