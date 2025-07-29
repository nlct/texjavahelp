/*
    Copyright (C) 2025 Nicola L.C. Talbot
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

import java.text.MessageFormat;
import java.text.BreakIterator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;
import java.net.URISyntaxException;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.imageio.ImageIO;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;

import org.xml.sax.SAXException;

import com.dickimawbooks.texparserlib.TeXParser;

public class TeXJavaHelpLib
{
   public TeXJavaHelpLib(TeXJavaHelpLibApp application) throws IOException
   {
      this(application, Locale.getDefault(), Locale.getDefault());
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
       Locale msgLocale, Locale hsLocale)
     throws IOException
   {
      this(application, application.getApplicationName(),
         "/resources", msgLocale, hsLocale,
         application.getApplicationName().toLowerCase().replaceAll(" ", ""));
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String resourcebase,
      Locale msgLocale, Locale hsLocale, String... dictPrefixes)
    throws IOException
   {
      this(application, applicationName, resourcebase,
       resourcebase, msgLocale, hsLocale, dictPrefixes);
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String resourcebase,
      String dictionaryBase,
      Locale msgLocale, Locale hsLocale, String... dictPrefixes)
    throws IOException
   {
      this(application, applicationName, resourcebase, dictionaryBase,
       msgLocale == null ? null : new HelpSetLocale(msgLocale),
       hsLocale == null ? null : new HelpSetLocale(hsLocale),
       dictPrefixes);
   }

   public TeXJavaHelpLib(TeXJavaHelpLibApp application,
      String applicationName, String resourcebase,
      String dictionaryBase,
      HelpSetLocale messagesLocale, HelpSetLocale helpsetLocale,
      String... dictPrefixes)
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

   public HelpSetLocale getHelpSetLocale()
   {
      return helpsetLocale;
   }

   /**
    * Sets the help set locale. If this method is used, ensure
    * this is done before search for the help system files.
    */
   public void setHelpSetLocale(HelpSetLocale hsLocale)
   {
      helpsetLocale = hsLocale;
   }

   public HelpSetLocale getMessagesLocale()
   {
      return messages.getLocale();
   }

   /**
    * Encodes HTML special characters. See also
    * encodeAttributeValue(String,boolean)
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

   /**
    * Encodes string for use in quoted attribute value.
    * The string should have first been processed or expanded if it was
    * obtained from TeX source, so it may already contain entities.
    * (Copied from HtmlTag as TeX Parser Library may not otherwise be required
    * for the application to run.)
    */ 
   public static String encodeAttributeValue(String value, boolean url)
   {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < value.length(); )
      {
         int cp = value.codePointAt(i);
         i += Character.charCount(cp);

         if (cp == '\\' || cp == '"' || cp == '\'' || cp == '<' || cp == '>')
         {
            if (url)
            {
               builder.append('%');
            }
            else
            {
               builder.append("\\x");
            }

            builder.append(String.format("%X", cp));
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

   public boolean isMessageLabelValid(String label)
   {
      return messages.containsKey(label);
   }

   public String getMessage(String label, Object... params)
   {
      String msg = messages.getMessage(label, params);

      if (msg == null)
      {
         try
         {
            throw new IllegalArgumentException("Can't find message for label: "+label);
         }
         catch (IllegalArgumentException e)
         {
            warning(e.getMessage(), e);
         }

         return label;
      }

      return msg;
   }

   public String getChoiceMessage(String label, int argIdx,
      String choiceLabel, int numChoices, Object... args)
   {
      return messages.getChoiceMessage(label, argIdx, choiceLabel, numChoices, args);
   }

   private void printSyntaxItem(String syntax, int syntaxLength, String description)
   {
      String desc = description.replaceAll(" *\\n", " ");

      int descLength = desc.length();
       
      System.out.print("  ");

      if (syntax != null)
      {
         if (syntaxLength <= 0)
         {
            syntaxLength = syntax.length();
         }

         System.out.print(syntax);
      }
      else
      {
         for (int i = 0; i < syntaxLength; i++)
         {
            System.out.print(' ');
         }
      }

      int numSpaces = SYNTAX_ITEM_TAB - syntaxLength - 2;

      if (numSpaces <= 0)
      {
         numSpaces = 2;
      }

      int indent = syntaxLength+2+numSpaces;

      int width = SYNTAX_ITEM_LINEWIDTH-indent;

      for (int i = 0; i < numSpaces; i++)
      {
         System.out.print(' ');
      }

      if (width >= descLength)
      {
         System.out.println(desc);
      }
      else
      {
         BreakIterator boundary = BreakIterator.getLineInstance();
         boundary.setText(desc);

         int start = boundary.first();
         int n = 0;

         int defWidth = SYNTAX_ITEM_LINEWIDTH - SYNTAX_ITEM_TAB;
         numSpaces = SYNTAX_ITEM_TAB;

         for (int end = boundary.next();
            end != BreakIterator.DONE;
            start = end, end = boundary.next())
         {
            int len = end-start;
            n += len;

            if (n >= width)
            {
               System.out.println();

               for (int i = 0; i < numSpaces; i++)
               {
                  System.out.print(' ');
               }

               n = len;
               width = defWidth;
            }

            System.out.print(desc.substring(start,end));
         }

         System.out.println();
      }
   }

   public void printSyntaxItem(String syntax, String desc)
   {
      printSyntaxItem(syntax, 0, desc);
   }

   public void printSyntaxItem(int syntaxLength, String desc)
   {
      printSyntaxItem(null, syntaxLength, desc);
   }

   public void printWordWrapped(String message)
   {
      String desc = message.replaceAll(" *\\n", " ");

      int descLength = desc.length();

      if (descLength <= SYNTAX_ITEM_LINEWIDTH)
      {
         System.out.println(desc);
      }
      else
      {
         BreakIterator boundary = BreakIterator.getLineInstance();
         boundary.setText(desc);
         int start = boundary.first();
         int n = 0;

         for (int end = boundary.next();
            end != BreakIterator.DONE;
            start = end, end = boundary.next())
         {
            int len = end-start;
            n += len;

            if (n >= SYNTAX_ITEM_LINEWIDTH)
            {
               System.out.println();
               n = len;
            }

            System.out.print(desc.substring(start,end));
         }

         System.out.println();
      }
   }

   public void printSyntaxItem(String message)
   {
      String[] messageList = message.split("\n\t");
      int syntaxLength = 0;

      for (int i = 0 ; i < messageList.length; i++)
      {
         String[] split = messageList[i].split("\t", 2);

         if (split.length == 2)
         {
            if (split[0].isEmpty())
            {
               printSyntaxItem(syntaxLength, messageList[i]);
            }
            else
            {
               syntaxLength = split[0].length();
               printSyntaxItem(split[0], split[1]);
            }
         }
         else
         {
            printWordWrapped(messageList[i]);
         }
      }
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

   public String getAboutInfo(boolean html,
     String version, String date,
     String copyright, String legalText,
     boolean incTeXParser, String trailer)
   {
      String par = html ? "<p>" : String.format("%n%n");
      String nl = html ? "<br>" : String.format("%n");

      StringBuilder builder = new StringBuilder();

      if (date == null)
      {
         builder.append(
           getMessageWithFallback(
            "about.version",
            "{0} ({1})",
            getApplicationName(), version));
      }
      else
      {
         builder.append(
           getMessageWithFallback(
            "about.version_date",
            "{0} {1} ({2})",
            getApplicationName(), version, date));
      }

      if (copyright != null)
      {
         builder.append(nl);
         builder.append(copyright);
      }

      if (legalText != null)
      {
         if (html)
         {
            legalText = encodeHTML(legalText, false).replaceAll("\r?\n", nl);
         }

         builder.append(nl);
         builder.append(legalText);
      }

      String translator = getMessageIfExists("about.translator_info");

      if (translator != null && !translator.isEmpty())
      {
         builder.append(par);

         if (html)
         {
            translator = encodeHTML(translator, false);
         }

         builder.append(translator);
      }

      String ack = getMessageIfExists("about.acknowledgements");

      if (ack != null && !ack.isEmpty())
      {
         builder.append(par);

         if (html)
         {
            ack = encodeHTML(ack, false);
         }

         builder.append(ack);
      }

      builder.append(par);
      builder.append(getMessageWithFallback("about.library.version",
        "Bundled with {0} version {1} ({2})",
        "texjavahelplib.jar",
        TeXJavaHelpLib.VERSION, TeXJavaHelpLib.VERSION_DATE));
      builder.append(nl);

      builder.append(getInfoUrl(html, "https://github.com/nlct/texjavahelplib"));

      if (incTeXParser)
      {
         builder.append(par);
         builder.append(getMessageWithFallback("about.library.version",
           "Bundled with {0} version {1} ({2})",
           "texparserlib.jar",
            TeXParser.VERSION, TeXParser.VERSION_DATE));
         builder.append(nl);
         builder.append(getInfoUrl(html, "https://github.com/nlct/texparser"));
      }

      if (trailer != null)
      {
         if (html)
         {
            trailer = encodeHTML(trailer, false).replaceAll("\r?\n", nl);
         }

         builder.append(nl);
         builder.append(trailer);
      }

      return builder.toString();
   }

   public String getInfoUrl(boolean html, String url)
   {
      if (html)
      {
         String href = url;

         if (!url.startsWith("http"))
         {
            href = "https://"+url;
         }

         return String.format("<a href=\"%s\">%s</a>",
           encodeAttributeValue(href, true),
           encodeHTML(url, false));
      }
      else
      {
         return url;
      }
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

   public String getMappedSmallIconSuffix()
   {
      return mappedSmallIconSuffix;
   }

   public void setMappedSmallIconSuffix(String suffix)
   {
      mappedSmallIconSuffix = suffix;
   }

   public void loadImageMap(String resourcePath)
   throws IOException
   {
       InputStream in = null;
       BufferedReader reader = null;

       try
       {
          in = getClass().getResourceAsStream(resourcePath);

          if (in == null)
          {
             throw new FileNotFoundException(
               "Can't find resource "+resourcePath);
          }

          reader = new BufferedReader(new InputStreamReader(in));

          loadImageMap(reader);
       }
       finally
       {
          if (reader != null)
          {
             reader.close();
          }

          if (in != null)
          {
             in.close();
          }
       }
   }

   public void loadImageMap(Reader reader)
   throws IOException
   {
      if (imageMap == null)
      {
         imageMap = new Properties();
      }

      imageMap.load(reader);
   }

   public URL getMappedImageLocation(String action)
   {
      if (imageMap == null) return null;

      String location = imageMap.getProperty(action);

      if (location == null) return null;

      URL imageURL = getClass().getResource(location);

      if (imageURL == null)
      {
         application.debug("Can't find resource '"+location+"'");
         imageMap.remove(action);
      }

      return imageURL;
   }

   public ImageIcon getHelpIcon(String base, boolean small)
   {
      return getHelpIcon(base, small, imageExtensions);
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

         try
         {
            in = getClass().getResourceAsStream(
              HELP_LIB_ICON_PATH+base+suffix+".png");

            if (in != null)
            {
               ic = new ImageIcon(ImageIO.read(in));
            }
         }
         catch (IOException e)
         {
         }
         finally
         {
            if (in != null)
            {
               try
               {
                  in.close();
               }
               catch (IOException e)
               {
                  debug(e);
               }
            }
         }
      }

      return ic;
   }

   /**
    * Gets icon provided in texjavahelplib.jar with the given
    * suffix. This is just a shortcut for fetching the icon
    * named base+suffix+".png" in the jar file.
    */
   public ImageIcon getHelpIcon(String base, String suffix)
   {
      InputStream in = null;
      ImageIcon ic = null;

      try
      {
         in = getClass().getResourceAsStream(
           HELP_LIB_ICON_PATH+base+suffix+".png");

         if (in != null)
         {
            ic = new ImageIcon(ImageIO.read(in));
         }
      }
      catch (IOException e)
      {
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
               debug(e);
            }
         }
      }

      return ic;
   }

   /**
    * Gets icons provided in texjavahelplib.jar with the given
    * suffix.
    */
   public IconSet getHelpIconSet(String base, String suffix)
   {
      ImageIcon ic = getHelpIcon(base, suffix);

      if (ic == null) return null;

      IconSet icSet = new IconSet(base, ic);

      ic = getHelpIcon(base+"_pressed", suffix);

      if (ic != null)
      {
         icSet.setPressedIcon(ic);
      }

      ic = getHelpIcon(base+"_selected", suffix);

      if (ic != null)
      {
         icSet.setSelectedIcon(ic);
      }

      ic = getHelpIcon(base+"_rollover", suffix);

      if (ic != null)
      {
         icSet.setRolloverIcon(ic);
      }

      ic = getHelpIcon(base+"_rollover_selected", suffix);

      if (ic != null)
      {
         icSet.setRolloverSelectedIcon(ic);
      }

      ic = getHelpIcon(base+"_disabled", suffix);

      if (ic != null)
      {
         icSet.setDisabledIcon(ic);
      }

      ic = getHelpIcon(base+"_disabled_selected", suffix);

      if (ic != null)
      {
         icSet.setDisabledSelectedIcon(ic);
      }

      return icSet;
   }

   public IconSet getHelpIconSet(String base, boolean small)
   {
      return getHelpIconSet(base, small, imageExtensions);
   }

   public IconSet getHelpIconSet(String base, boolean small, String... extensions)
   {
      return small ? getSmallIconSet(base, extensions)
                    : getLargeIconSet(base, extensions);
   }

   public ImageIcon getSmallIcon(String base)
   {
      return getSmallIcon(base, imageExtensions);
   }

   public ImageIcon getSmallIcon(String base, String... extensions)
   {
      ImageIcon ic = application.getSmallIcon(base, extensions);

      if (ic != null)
      {
         return ic;
      }

      URL mapped = getMappedImageLocation(base+mappedSmallIconSuffix);

      if (mapped == null)
      {
         mapped = getMappedImageLocation(base);
      }

      if (mapped != null)
      {
         return new ImageIcon(mapped);
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

   public IconSet getSmallIconSet(String base)
   {
      return getSmallIconSet(base, imageExtensions);
   }

   public IconSet getSmallIconSet(String base, String... extensions)
   {
      IconSet icSet = application.getSmallIconSet(base, extensions);

      if (icSet != null)
      {
         return icSet;
      }

      String mappedBase = base+mappedSmallIconSuffix;
      URL mapped = getMappedImageLocation(mappedBase);

      if (mapped != null)
      {
         icSet = new IconSet(base, new ImageIcon(mapped));

         mapped = getMappedImageLocation(mappedBase+"_selected");

         if (mapped != null)
         {
            icSet.setSelectedIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(mappedBase+"_pressed");

         if (mapped != null)
         {
            icSet.setPressedIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(mappedBase+"_rollover");

         if (mapped != null)
         {
            icSet.setRolloverIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(mappedBase+"_rollover_selected");

         if (mapped != null)
         {
            icSet.setRolloverSelectedIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(mappedBase+"_disabled");

         if (mapped != null)
         {
            icSet.setDisabledIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(mappedBase+"_disabled_selected");

         if (mapped != null)
         {
            icSet.setDisabledSelectedIcon(new ImageIcon(mapped));
         }

         return icSet;
      }

      String basename = resourceIconBase;

      if (!resourceIconBase.endsWith("/"))
      {
         basename += "/";
      }

      basename += base;

      for (String ext : extensions)
      {
         String suffix = smallIconSuffix + "." + ext;

         URL url = getClass().getResource(basename + suffix);

         if (url != null)
         {
            icSet = new IconSet(base, new ImageIcon(url));

            url = getClass().getResource(basename + "_selected" + suffix);

            if (url != null)
            {
               icSet.setSelectedIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_pressed" + suffix);

            if (url != null)
            {
               icSet.setPressedIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_rollover" + suffix);

            if (url != null)
            {
               icSet.setRolloverIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_rollover_selected" + suffix);

            if (url != null)
            {
               icSet.setRolloverSelectedIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_disabled" + suffix);

            if (url != null)
            {
               icSet.setDisabledIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_disabled_selected" + suffix);

            if (url != null)
            {
               icSet.setDisabledSelectedIcon(new ImageIcon(url));
            }

            return icSet;
         }
      }

      return getHelpIconSet(base, smallIconSuffix);
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
      return getLargeIcon(base, imageExtensions);
   }

   public ImageIcon getLargeIcon(String base, String... extensions)
   {
      ImageIcon ic = application.getLargeIcon(base, extensions);

      if (ic != null)
      {
         return ic;
      }

      URL mapped = getMappedImageLocation(base);

      if (mapped != null)
      {
         return new ImageIcon(mapped);
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

   public IconSet getLargeIconSet(String base)
   {
      return getLargeIconSet(base, imageExtensions);
   }

   public IconSet getLargeIconSet(String base, String... extensions)
   {
      IconSet icSet = application.getLargeIconSet(base, extensions);

      if (icSet != null)
      {
         return icSet;
      }

      URL mapped = getMappedImageLocation(base);

      if (mapped != null)
      {
         icSet = new IconSet(base, new ImageIcon(mapped));

         mapped = getMappedImageLocation(base+"_selected");

         if (mapped != null)
         {
            icSet.setSelectedIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(base+"_pressed");

         if (mapped != null)
         {
            icSet.setPressedIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(base+"_rollover");

         if (mapped != null)
         {
            icSet.setRolloverIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(base+"_rollover_selected");

         if (mapped != null)
         {
            icSet.setRolloverSelectedIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(base+"_disabled");

         if (mapped != null)
         {
            icSet.setDisabledIcon(new ImageIcon(mapped));
         }

         mapped = getMappedImageLocation(base+"_disabled_selected");

         if (mapped != null)
         {
            icSet.setDisabledSelectedIcon(new ImageIcon(mapped));
         }

         return icSet;
      }

      String basename = resourceIconBase;

      if (!resourceIconBase.endsWith("/"))
      {
         basename += "/";
      }

      basename += base;

      for (String ext : extensions)
      {
         String suffix = largeIconSuffix + "." + ext;

         URL url = getClass().getResource(basename+suffix);

         if (url != null)
         {
            icSet = new IconSet(base, new ImageIcon(url));

            url = getClass().getResource(basename + "_selected" + suffix);

            if (url != null)
            {
               icSet.setSelectedIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_pressed" + suffix);

            if (url != null)
            {
               icSet.setPressedIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_rollover" + suffix);

            if (url != null)
            {
               icSet.setRolloverIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_rollover_selected" + suffix);

            if (url != null)
            {
               icSet.setRolloverSelectedIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_disabled" + suffix);

            if (url != null)
            {
               icSet.setDisabledIcon(new ImageIcon(url));
            }

            url = getClass().getResource(basename + "_disabled_selected" + suffix);

            if (url != null)
            {
               icSet.setDisabledSelectedIcon(new ImageIcon(url));
            }

            return icSet;
         }
      }

      return getHelpIconSet(base, largeIconSuffix);
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

   protected void initHelpsetPattern()
   {
      helpsetPattern = Pattern.compile(
         "(?:.+-|^)([a-z]{2,3}(?:-[A-Z]{2})?(?:-[A-Z][a-z]{3})?)");
   }

   public void setHelpSetPattern(Pattern p)
   {
      availableHelpsets = null;
      helpsetPattern = p;
   }

   public Pattern getHelpSetPattern()
   {
      if (helpsetPattern == null)
      {
         initHelpsetPattern();
      }

      return helpsetPattern;
   }

   /**
    * Gets a sorted list of available helpsets.
    */
   public Vector<HelpSetLocale> getHelpSets()
    throws URISyntaxException,FileNotFoundException
   {
      if (availableHelpsets == null)
      {
         String path = getHelpSetResourcePath();
         URL url = getClass().getResource(path);

         if (url == null)
         {
            throw new FileNotFoundException(getMessageWithFallback(
             "error.resource_not_found", "Resource file ''{0}'' not found",
             path));
         }

         File dir = new File(url.toURI()).getParentFile();

         String[] list = dir.list();

         availableHelpsets = new Vector<HelpSetLocale>();

         for (String filename : list)
         {
            Matcher m = getHelpSetPattern().matcher(filename);

            if (m.matches())
            {
               HelpSetLocale hs = new HelpSetLocale(m.group(1));

               if (!availableHelpsets.contains(hs))
               {
                  availableHelpsets.add(hs);
               }
            }
         }

         availableHelpsets.sort(null);
      }

      return availableHelpsets;
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

         Locale locale = helpsetLocale.getLocale();
         helpsetsubdir = helpsetLocale.getTag();

         path = base + "/" + helpsetSubdirPrefix+helpsetsubdir
               + "/" + navxmlfilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            helpsetsubdir = locale.toLanguageTag();
            path = base + "/" + helpsetSubdirPrefix+helpsetsubdir
                 + "/" + navxmlfilename;
            stream = getClass().getResourceAsStream(path);
         }

         if (stream == null)
         {
            String lang = locale.getLanguage();
            String country = locale.getCountry();
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
               String script = locale.getScript();

               if (script != null && !script.isEmpty())
               {
                  helpsetsubdir = lang + "-" + script;

                  path = base + "/" + helpsetSubdirPrefix+helpsetsubdir
                     + "/" + navxmlfilename;

                  stream = getClass().getResourceAsStream(path);
               }

               if (stream == null)
               {
                  helpsetsubdir = "en";
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

         Locale locale = helpsetLocale.getLocale();
         helpsetsubdir = helpsetLocale.getTag();

         path = base + "/" + helpsetsubdir + "/" + indexXmlFilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            helpsetsubdir = locale.toLanguageTag();
            path = base + "/" + helpsetsubdir + "/" + indexXmlFilename;
            stream = getClass().getResourceAsStream(path);
         }

         if (stream == null)
         {
            String lang = locale.getLanguage();
            String country = locale.getCountry();
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
               String script = locale.getScript();

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

         Locale locale = helpsetLocale.getLocale();
         helpsetsubdir = helpsetLocale.getTag();

         path = base + "/" + helpsetsubdir + "/" + searchXmlFilename;

         stream = getClass().getResourceAsStream(path);

         if (stream == null)
         {
            helpsetsubdir = locale.toLanguageTag();
            path = base + "/" + helpsetsubdir + "/" + searchXmlFilename;
            stream = getClass().getResourceAsStream(path);
         }

         if (stream == null)
         {
            String lang = locale.getLanguage();
            String country = locale.getCountry();
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
               String script = locale.getScript();

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
      initHelpSet(helpsetdir, navBaseName, title, null);
   }

   public void initHelpSet(String helpsetdir, String navBaseName, String title,
    Dimension helpWindowInitSize)
    throws IOException,SAXException
   {
      this.helpsetdir = helpsetdir;
      this.helpWindowInitSize = helpWindowInitSize;

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

   public Dimension getHelpWindowInitialSize()
   {
      if (helpWindowInitSize == null)
      {
         Toolkit tk = Toolkit.getDefaultToolkit();
         Dimension dim = tk.getScreenSize();
         helpWindowInitSize = new Dimension(dim.width/2, dim.height*9/10);
      }

      return helpWindowInitSize;
   }

   public TreeSet<IndexItem> getIndexGroupData()
   {
      return indexGroupList;
   }

   public NavigationNode getIndexNode()
    throws HelpSetNotInitialisedException
   {
      return getNavigationNodeById("docindex");
   }

   public NavigationNode getNavigationNodeById(String id)
    throws HelpSetNotInitialisedException
   {
      if (navigationTree == null)
      {
         throw new HelpSetNotInitialisedException(getMessageWithFallback(
           "error.no_navtree_with_id",
           "No node matching ID {0}: navigation tree has not been created",
           id));
      }

      return navigationTree.getNodeById(id);
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
    throws IOException,HelpSetNotInitialisedException
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

   public void openHelp(TargetRef ref)
    throws IOException,HelpSetNotInitialisedException
   {
      if (helpFrame == null)
      {
         throw new HelpSetNotInitialisedException(
           getMessageWithFallback(
           "error.no_helpset", "Helpset has not been initialised"));
      }

      helpFrame.setPage(ref);
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

   public String getIconPrefix(String property, String fallback)
   {
      String value = getMessageIfExists(property+".iconimage");

      return value == null ? fallback : value;
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
      if (helpFontChangeListeners != null)
      {
         for (HelpFontChangeListener listener : helpFontChangeListeners)
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
      if (helpFontChangeListeners == null)
      {
         helpFontChangeListeners = new Vector<HelpFontChangeListener>();
      }

      helpFontChangeListeners.add(listener);
   }

   public HelpFontSettings getHelpFontSettings()
   {
      return helpFontSettings;
   }

   public int getDefaultLowerNavLabelLimit()
   {
      return helpLowerNavLabelLimit;
   }

   public boolean isDefaultLowerNavLabelTextOn()
   {
      return helpLowerNavLabelShowText;
   }

   public void setDefaultLowerNavSettings(boolean showText, int limit)
   {
      helpLowerNavLabelShowText = showText;
      helpLowerNavLabelLimit = limit;
   }

   public void fireLowerNavSettingUpdate(LowerNavSettingsChangeEvent evt)
   {
      helpLowerNavLabelShowText = evt.isShowTextOn();
      helpLowerNavLabelLimit = evt.getLimit();

      if (lowerNavSettingsChangeListeners != null)
      {
         for (LowerNavSettingsChangeListener listener : lowerNavSettingsChangeListeners)
         {
            listener.lowerNavSettingsChange(evt);

            if (evt.isConsumed())
            {
               break;
            }
         }
      }
   }

   public void addLowerNavSettingsChangeListener(LowerNavSettingsChangeListener listener)
   {
      if (lowerNavSettingsChangeListeners == null)
      {
         lowerNavSettingsChangeListeners = new Vector<LowerNavSettingsChangeListener>();
      }

      lowerNavSettingsChangeListeners.add(listener);
   }

   /**
    * Action to open the main help window. Designed for the
    * "menu.help.manual" menu item with the "manual" icon prefix.
    */
   public TJHAbstractAction createHelpManualAction()
   {
      return new TJHAbstractAction(this,
        "menu.help", "manual", getKeyStroke("menu.help.manual"),
         getDefaultButtonActionOmitKeys())
        {
           @Override
           public void doAction()
           {
              helpLib.openHelp();
           }
        };
   }

   /**
    * Action to open the main help window. Designed for the
    * "menu.help.manual" menu item with the given icon prefix.
    */
   public TJHAbstractAction createHelpManualAction(String iconPrefix)
   {
      return new TJHAbstractAction(this,
        "menu.help", "manual", "manual", iconPrefix,
         getKeyStroke("menu.help.manual"),
         (Boolean)null, (JComponent)null,
         getDefaultButtonActionOmitKeys())
        {
           @Override
           public void doAction()
           {
              helpLib.openHelp();
           }
        };
   }

   /**
    * Action to open the main help window. Designed for the
    * "menu.help.manual" menu item with the given icon set.
    */
   public TJHAbstractAction createHelpManualAction(
       IconSet largeIconSet, IconSet smallIconSet)
   {
      return new TJHAbstractAction(this,
        "menu.help", "manual", "manual", largeIconSet, smallIconSet,
         getKeyStroke("menu.help.manual"),
         (Boolean)null, (JComponent)null,
         getDefaultButtonActionOmitKeys())
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
      return createHelpAction(helpID, (JComponent)null);
   }

   public TJHAbstractAction createHelpAction(String helpID, JComponent comp)
   {
      return createHelpAction(helpID, "button", "help", "manual."+helpID,
       getIconPrefix("button.help", "help"), 
       getKeyStroke("button.help"), comp, getDefaultButtonActionOmitKeys());
   }

   public TJHAbstractAction createHelpAction(String helpID,
      KeyStroke keyStroke, JComponent comp, String... omitKeys)
   {
      return createHelpAction(helpID, "button", "help", 
       "manual."+helpID, 
       getIconPrefix("button.help", "help"), 
       keyStroke, comp, omitKeys);
   }

   public TJHAbstractAction createHelpAction(String helpID,
      String msgParentTag, String childTag, String action, String iconPrefix)
   {
      return createHelpAction(helpID, msgParentTag, childTag, action, iconPrefix,
        null, null);
   }

   public TJHAbstractAction createHelpAction(final String helpID,
      String msgParentTag, String childTag, String action,
      String iconPrefix, KeyStroke keyStroke, JComponent comp, String... omitKeys)
   {
      return new TJHAbstractAction(this,
        msgParentTag, childTag, action, iconPrefix, keyStroke, null, comp, omitKeys)
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
      return createHelpAction(node, "button", "help", "manual."+node.getKey(),
       getIconPrefix("button.help", "help"), 
       getKeyStroke("button.help"), comp);
   }

   public TJHAbstractAction createHelpAction(final NavigationNode node,
      String msgParentTag, String childTag, String action,
      String iconPrefix, KeyStroke keyStroke, JComponent comp,
      String... omitKeys)
   {
      return  new TJHAbstractAction(this,
        msgParentTag, childTag, action, iconPrefix, keyStroke, null, comp, omitKeys)
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
    throws IllegalArgumentException,
           HelpSetNotInitialisedException
   {
      NavigationNode node = getNavigationNodeById(helpId);

      if (node == null)
      {
         TargetRef ref = getTargetRef(helpId);

         if (ref == null)
         {
            throw new IllegalArgumentException(
               getMessage("error.node_id_not_found", helpId));
         }

         return createHelpDialogAction(owner, ref);
      }

      return createHelpDialogAction(owner, node);
   }

   public HelpDialogAction createHelpDialogAction(JDialog owner, NavigationNode node)
   {
      return new HelpDialogAction(owner, node, this, getDefaultButtonActionOmitKeys());
   }

   public HelpDialogAction createHelpDialogAction(JDialog owner, TargetRef ref)
   {
      return new HelpDialogAction(owner, ref, this, getDefaultButtonActionOmitKeys());
   }

   public HelpDialogAction createHelpDialogAction(JDialog owner, String helpId,
      IconSet largeIconSet, IconSet smallIconSet)
    throws IllegalArgumentException,
           HelpSetNotInitialisedException
   {
      NavigationNode node = getNavigationNodeById(helpId);

      if (node == null)
      {
         TargetRef ref = getTargetRef(helpId);

         if (ref == null)
         {
            throw new IllegalArgumentException(
               getMessage("error.node_id_not_found", helpId));
         }

         return createHelpDialogAction(owner, ref, largeIconSet, smallIconSet);
      }

      return createHelpDialogAction(owner, node, largeIconSet, smallIconSet);
   }

   public HelpDialogAction createHelpDialogAction(JDialog owner,
      NavigationNode node, IconSet largeIconSet, IconSet smallIconSet)
   {
      return new HelpDialogAction(owner, node,
        largeIconSet, smallIconSet,
        this, getDefaultButtonActionOmitKeys());
   }

   public HelpDialogAction createHelpDialogAction(JDialog owner, 
      TargetRef ref, IconSet largeIconSet, IconSet smallIconSet)
   {
      return new HelpDialogAction(owner, ref, largeIconSet, smallIconSet,
       this, getDefaultButtonActionOmitKeys());
   }

   public HelpDialogAction createHelpDialogAction(JFrame owner, String helpId)
    throws IllegalArgumentException,
           HelpSetNotInitialisedException
   {
      NavigationNode node = getNavigationNodeById(helpId);

      if (node == null)
      {
         TargetRef ref = getTargetRef(helpId);

         if (ref == null)
         {
            throw new IllegalArgumentException(
               getMessage("error.node_id_not_found", helpId));
         }

         return createHelpDialogAction(owner, ref);
      }

      return createHelpDialogAction(owner, node);
   }

   public HelpDialogAction createHelpDialogAction(JFrame owner, NavigationNode node)
   {
      return new HelpDialogAction(owner, node, this, getDefaultButtonActionOmitKeys());
   }

   public HelpDialogAction createHelpDialogAction(JFrame owner, TargetRef ref)
   {
      return new HelpDialogAction(owner, ref, this, getDefaultButtonActionOmitKeys());
   }

   public HelpDialogAction createHelpDialogAction(JFrame owner, String helpId,
     IconSet largeIconSet, IconSet smallIconSet)
    throws IllegalArgumentException,
           HelpSetNotInitialisedException
   {
      NavigationNode node = getNavigationNodeById(helpId);

      if (node == null)
      {
         TargetRef ref = getTargetRef(helpId);

         if (ref == null)
         {
            throw new IllegalArgumentException(
               getMessage("error.node_id_not_found", helpId));
         }

         return createHelpDialogAction(owner, ref, largeIconSet, smallIconSet);
      }

      return createHelpDialogAction(owner, node, largeIconSet, smallIconSet);
   }

   public HelpDialogAction createHelpDialogAction(JFrame owner,
     NavigationNode node,
     IconSet largeIconSet, IconSet smallIconSet)
   {
      return new HelpDialogAction(owner, node, largeIconSet, smallIconSet,
         this, getDefaultButtonActionOmitKeys());
   }

   public HelpDialogAction createHelpDialogAction(JFrame owner,
     TargetRef ref,
     IconSet largeIconSet, IconSet smallIconSet)
   {
      return new HelpDialogAction(owner, ref, largeIconSet, smallIconSet,
         this, getDefaultButtonActionOmitKeys());
   }

   public JButton createHelpDialogButton(JDialog owner, String helpId)
    throws IllegalArgumentException,
           HelpSetNotInitialisedException
   {
      NavigationNode node = getNavigationNodeById(helpId);

      if (node == null)
      {
         TargetRef ref = targetMap.get(helpId);

         if (ref == null)
         {
            throw new IllegalArgumentException(
               getMessage("error.node_id_not_found", helpId));
         }
         else
         {
            return createHelpDialogButton(owner, ref);
         }
      }

      return createHelpDialogButton(owner, node);
   }

   public JButton createHelpDialogButton(JDialog owner, NavigationNode node)
   {
      return new JButton(createHelpDialogAction(owner, node));
   }

   public JButton createHelpDialogButton(JDialog owner, TargetRef ref)
   {
      return new JButton(createHelpDialogAction(owner, ref));
   }

   /**
    * For secondary frames that behave like modeless dialogs.
    */
   public JButton createHelpDialogButton(JFrame owner, String helpId)
    throws IllegalArgumentException,
           HelpSetNotInitialisedException
   {
      NavigationNode node = getNavigationNodeById(helpId);

      if (node == null)
      {
         TargetRef ref = targetMap.get(helpId);

         if (ref == null)
         {
            throw new IllegalArgumentException(
               getMessage("error.node_id_not_found", helpId));
         }
         else
         {
            return createHelpDialogButton(owner, ref);
         }
      }

      return createHelpDialogButton(owner, node);
   }

   public JButton createHelpDialogButton(JFrame owner, NavigationNode node)
   {
      return new JButton(createHelpDialogAction(owner, node));
   }

   public JButton createHelpDialogButton(JFrame owner, TargetRef ref)
   {
      return new JButton(createHelpDialogAction(owner, ref));
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
      return createJLabel(tag, null);
   }

   public JLabel createJLabel(JLabelGroup labelGrp, String tag, JComponent comp)
   {
      JLabel label = createJLabel(tag, comp);
      labelGrp.add(label);
      return label;
   }

   public JLabel createJLabel(String tag, JComponent comp)
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

      if (comp != null)
      {
         jlabel.setLabelFor(comp);
      }

      return jlabel;
   }

   /**
    * Creates a check box menu item.
    */
   public JCheckBoxMenuItem createJCheckBoxMenuItem(String parentTag, String action,
     boolean selected, ActionListener listener)
   {
      String tag = action == null ? parentTag : parentTag+"."+action;

      JCheckBoxMenuItem item = new JCheckBoxMenuItem(getMessage(tag), selected);

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

      if (listener != null)
      {
         item.setActionCommand(action);
         item.addActionListener(listener);
      }

      return item;
   }

   /**
    * Creates a radio button menu item.
    */
   public JRadioButtonMenuItem createJRadioButtonMenuItem(String parentTag, String action,
     ActionListener listener, ButtonGroup bg)
   {
      return createJRadioButtonMenuItem(parentTag, action, false,
       listener, bg);
   }

   public JRadioButtonMenuItem createJRadioButtonMenuItem(String parentTag, String action,
     boolean selected, ActionListener listener, ButtonGroup bg)
   {
      String tag = action == null ? parentTag : parentTag+"."+action;

      JRadioButtonMenuItem item = new JRadioButtonMenuItem(getMessage(tag), selected);

      if (bg != null)
      {
         bg.add(item);
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

      if (listener != null)
      {
         item.setActionCommand(action);
         item.addActionListener(listener);
      }

      return item;
   }

   public String[] getDefaultButtonActionOmitKeys()
   {
      if (buttonDefaultOmitKeys == null)
      {
         if (buttonDefaultIconSmall)
         {
            if (buttonDefaultOmitTextIfIcon)
            {
               buttonDefaultOmitKeys = new String[]
                { 
                  Action.LARGE_ICON_KEY ,
                  Action.NAME
                } ;
            }
            else
            {
               buttonDefaultOmitKeys = new String[]
                { 
                  Action.LARGE_ICON_KEY 
                } ;
            }
         }
         else if (buttonDefaultOmitTextIfIcon)
         {
            buttonDefaultOmitKeys = new String[]
             { 
               Action.NAME
             } ;
         }
         else
         {
            buttonDefaultOmitKeys = new String[] { };
         }
      }

      return buttonDefaultOmitKeys;
   }

   /**
    * Sets the default behaviour for the smallIcon for methods that
    * omit it. This value should be set before calling the
    * applicable methods.
    */
   public void setDefaultButtonIconSmall(boolean on)
   {
      buttonDefaultIconSmall = on;
      buttonDefaultOmitKeys = null;
   }

   public boolean getDefaultButtonIconSmall()
   {
      return buttonDefaultIconSmall;
   }

   /**
    * Sets the default behaviour for the omitTextIfIcon for methods that
    * omit it. This value should be set before calling the
    * applicable methods.
    */
   public void setDefaultButtonOmitTextIfIcon(boolean on)
   {
      buttonDefaultOmitTextIfIcon = on;
      buttonDefaultOmitKeys = null;
   }

   public boolean getDefaultButtonOmitTextIfIcon()
   {
      return buttonDefaultOmitTextIfIcon;
   }

   /**
    * Sets the default small icon boolean setting for createToolBarButton(Action).
    * This value should be set before calling the
    * applicable methods.
    */
   public void setDefaultToolBarButtonIconSmall(boolean on)
   {
      toolbarButtonDefaultIconSmall = on;
   }

   public boolean getDefaultToolBarButtonIconSmall()
   {
      return toolbarButtonDefaultIconSmall;
   }

   /**
    * Creates a JButton from an action without showing text or
    * border. Note that this overrides any text specified by the
    * action.
    */
   public JButton createToolBarButton(Action action)
   {
      return createToolBarButton(action, toolbarButtonDefaultIconSmall);
   }

   public JButton createToolBarButton(Action action, boolean onlySmallIcon)
   {
      JButton btn = new JButton(action);
      Insets insets = new Insets(1, 1, 1, 1);
      Object value = action.getValue(Action.LARGE_ICON_KEY);

      if (onlySmallIcon)
      {
         Object smallIcon = action.getValue(Action.SMALL_ICON);

         if (smallIcon != null)
         {
            action.putValue(Action.LARGE_ICON_KEY, smallIcon);
            value = smallIcon;
            insets.set(2, 2, 2, 2);
         }
      }

      if (value != null)
      {
         if (value instanceof Icon)
         {
            Icon ic = (Icon)value;

            btn.setPreferredSize(new Dimension(
              ic.getIconWidth() + insets.left + insets.right,
              ic.getIconHeight() + insets.top + insets.bottom
            ));
         }

         btn.setText(null);
         btn.setMargin(insets);
      }

      return btn;
   }

   /**
    * Creates Okay button.
    * @param okayAction action
    * @param comp the component to set up key map (may be null)
    */
   public JButton createOkayButton(final OkayAction okayAction, JComponent comp)
   {
      return createOkayButton(okayAction, comp, getDefaultButtonActionOmitKeys());
   }

   /**
    * Creates Okay button.
    * @param okayAction action
    * @param comp the component to set up key map (may be null)
    */
   public JButton createOkayButton(final OkayAction okayAction,
     JComponent comp, String... omitKeys)
   {
      TJHAbstractAction action = new TJHAbstractAction(this, "button",
         "okay", (Boolean)null, comp, omitKeys)
         {
            @Override
            public void doAction()
            {
               okayAction.okay();
            }
         };

      JButton btn = new JButton(action);

      if (comp != null && comp instanceof JRootPane)
      {
         ((JRootPane)comp).setDefaultButton(btn);
      }

      return btn;
   }

   /**
    * Creates Okay button with action command "okay".
    * NB this doesn't set up any key map.
    * @param listener if not null, listener is added to button
    */
   public JButton createOkayButton(ActionListener listener)
   {
      return createOkayButton(listener, buttonDefaultIconSmall,
        buttonDefaultOmitTextIfIcon);
   }

   /**
    * Creates Okay button with action command "okay".
    * NB this doesn't set up any key map.
    * @param listener if not null, listener is added to button
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createOkayButton(ActionListener listener,  
     boolean smallIcon, boolean omitTextIfIcon)
   {
      return createJButton("button", "okay", listener, smallIcon, omitTextIfIcon);
   }

   /**
    * Creates Okay button.
    * Sets up key mapping for the component if a keystroke is
    * available. If the component is non-null the button will be set
    * as it's default button.
    * @param action the action to perform when this button is
    * pressed
    * @param comp the component to set up key map (may be null)
    */
   public JButton createOkayButton(Action action, JComponent comp)
   {
      return createOkayButton(action, comp, 
        buttonDefaultIconSmall, buttonDefaultOmitTextIfIcon);
   }

   /**
    * Creates Okay button.
    * Sets up key mapping for the component if a keystroke is
    * available. If the component is non-null JRootPane the button will be set
    * as it's default button.
    * @param action the action to perform when this button is
    * pressed
    * @param comp the component to set up key map (may be null)
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createOkayButton(Action action, JComponent comp,  
     boolean smallIcon, boolean omitTextIfIcon)
   {
      JButton btn = createJButton("button", "okay", action, comp, 
        getIconPrefix("button.okay", "okay"), 
        smallIcon, omitTextIfIcon);

      if (comp != null && comp instanceof JRootPane)
      {
         ((JRootPane)comp).setDefaultButton(btn);
      }

      return btn;
   }


   /**
    * Creates Apply button.
    * @param applyAction action
    * @param comp the component to set up key map (may be null)
    */
   public JButton createApplyButton(final ApplyAction applyAction, JComponent comp)
   {
      return createApplyButton(applyAction, comp, getDefaultButtonActionOmitKeys());
   }

   /**
    * Creates Apply button.
    * @param applyAction action
    * @param comp the component to set up key map (may be null)
    */
   public JButton createApplyButton(final ApplyAction applyAction,
     JComponent comp, String... omitKeys)
   {
      TJHAbstractAction action = new TJHAbstractAction(this, "button",
         "apply", (Boolean)null, comp, omitKeys)
         {
            @Override
            public void doAction()
            {
               applyAction.apply();
            }
         };

      return new JButton(action);
   }

   /**
    * Creates Apply button with action command "apply".
    * NB this doesn't set up any key map.
    * @param listener if not null, listener is added to button
    */
   public JButton createApplyButton(ActionListener listener)
   {
      return createApplyButton(listener, buttonDefaultIconSmall,
        buttonDefaultOmitTextIfIcon);
   }

   /**
    * Creates Apply button with action command "apply".
    * NB this doesn't set up any key map.
    * @param listener if not null, listener is added to button
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createApplyButton(ActionListener listener,
     boolean smallIcon, boolean omitTextIfIcon)
   {
      return createJButton("button", "apply", listener,
        smallIcon, omitTextIfIcon);
   }

   /**
    * Creates Cancel button using TJHAbstractAction that closes the
    * given JFrame. Ignores large icon.
    * @param frame the frame that the button should close
    */
   public JButton createCancelButton(JFrame frame)
   {
      return createCancelButton(frame, getDefaultButtonActionOmitKeys());
   }

   /**
    * Creates Cancel button using TJHAbstractAction that closes the
    * given JFrame.
    * @param frame the frame that the button should close
    * @param omitKeys list of keys the action shouldn't set
    */
   public JButton createCancelButton(final JFrame frame, String... omitKeys)
   {
      TJHAbstractAction action = new TJHAbstractAction(this, "button",
      "cancel", (Boolean)null, frame.getRootPane(), omitKeys)
      {
         @Override
         public void doAction()
         {
            frame.setVisible(false);
         }
      };

      return new JButton(action);
   }

   /**
    * Creates Cancel button using TJHAbstractAction that closes the
    * given JDialog.
    */
   public JButton createCancelButton(JDialog dialog)
   {
      return createCancelButton(dialog, getDefaultButtonActionOmitKeys());
   }

   /**
    * Creates Cancel button using TJHAbstractAction that closes the
    * given JDialog.
    * @param dialog the dialog that the button should close
    * @param omitKeys list of keys the action shouldn't set
    */
   public JButton createCancelButton(final JDialog dialog, String... omitKeys)
   {
      TJHAbstractAction action = new TJHAbstractAction(this, "button",
      "cancel", (Boolean)null, dialog.getRootPane(), omitKeys)
      {
         @Override
         public void doAction()
         {
            dialog.setVisible(false);
         }
      };

      return new JButton(action);
   }

   /**
    * Creates Cancel button with the given action.
    * Sets up key mapping for the component if a keystroke is
    * available.
    * @param action the action to perform when this button is
    * pressed
    * @param comp the component to set up key map (may be null)
    */
   public JButton createCancelButton(Action action, JComponent comp)
   {
      return createCancelButton(action, comp, 
       buttonDefaultIconSmall, buttonDefaultOmitTextIfIcon);
   }

   /**
    * Creates Cancel button.
    * Sets up key mapping for the component if a keystroke is
    * available.
    * @param action the action to perform when this button is
    * pressed
    * @param comp the component to set up key map (may be null)
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createCancelButton(Action action, JComponent comp,  
     boolean smallIcon, boolean omitTextIfIcon)
   {
      return createJButton("button", "cancel", action, comp, 
        getIconPrefix("button.cancel", "cancel"), 
        smallIcon, omitTextIfIcon);
   }

   /**
    * Creates Close button using TJHAbstractAction that closes the
    * given JFrame. The new button will be set as the frame's
    * default button. Ignores the large icon.
    * @param frame the frame that the button should close
    */
   public JButton createCloseButton(JFrame frame)
   {
      return createCloseButton(frame, getDefaultButtonActionOmitKeys());
   }

   /**
    * Creates Close button using TJHAbstractAction that closes the
    * given JFrame. The new button will be set as the frame's
    * default button.
    * @param frame the frame that the button should close
    * @param omitKeys list of keys the action shouldn't set
    */
   public JButton createCloseButton(final JFrame frame, String... omitKeys)
   {
      TJHAbstractAction action = new TJHAbstractAction(this, "button",
      "close", (Boolean)null, frame.getRootPane(), omitKeys)
      {
         @Override
         public void doAction()
         {
            frame.setVisible(false);
         }
      };

      JButton btn = new JButton(action);

      frame.getRootPane().setDefaultButton(btn);

      return btn;
   }

   /**
    * Creates Close button using TJHAbstractAction that closes the
    * given JDialog. The new button will be set as the dialog's
    * default button.
    * @param dialog the dialog that the button should close
    */
   public JButton createCloseButton(JDialog dialog)
   {
      return createCloseButton(dialog, getDefaultButtonActionOmitKeys());
   }

   /**
    * Creates Close button using TJHAbstractAction that closes the
    * given JDialog. The new button will be set as the dialog's
    * default button.
    * @param dialog the dialog that the button should close
    * @param omitKeys list of keys the action shouldn't set
    */
   public JButton createCloseButton(final JDialog dialog, String... omitKeys)
   {
      TJHAbstractAction action = new TJHAbstractAction(this, "button",
      "close", (Boolean)null, dialog.getRootPane(), omitKeys)
      {
         @Override
         public void doAction()
         {
            dialog.setVisible(false);
         }
      };

      JButton btn = new JButton(action);

      dialog.getRootPane().setDefaultButton(btn);

      return btn;
   }

   /**
    * Creates Close button with action command "close".
    * NB this doesn't set up any key map.
    * @param listener if not null, listener is added to button
    */
   public JButton createCloseButton(ActionListener listener)
   {
      return createCloseButton(listener, buttonDefaultIconSmall, buttonDefaultOmitTextIfIcon);
   }

   /**
    * Creates Close button with action command "close".
    * NB this doesn't set up any key map.
    * @param listener if not null, listener is added to button
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createCloseButton(ActionListener listener,  
     boolean smallIcon, boolean omitTextIfIcon)
   {
      return createJButton("button", "close", listener,
        smallIcon, omitTextIfIcon);
   }

   /**
    * Creates Close button with action command "close".
    * @param action the button action 
    * @param comp if not null, sets the key mapping for the
    * component
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createCloseButton(Action action, JComponent comp,  
     boolean smallIcon, boolean omitTextIfIcon)
   {
      return createJButton("button", "close", action, comp,
        getIconPrefix("button.close", "close"), 
        smallIcon, omitTextIfIcon);
   }

   /**
    * Creates a labelled button. The text and mnemonic are obtained from the tag.
    * @param tag the language tag
    */
   public JButton createJButton(String tag)
   {
      return createJButton(tag, null, null);
   }

   /**
    * Creates a labelled button. The text and mnemonic are obtained from the tag.
    * @param tag the language tag
    * @param actionName if not null, the button's action command
    * @param actionListener if not null, added to the button's 
    * ActionListener list
    */
   public JButton createJButton(String parentTag, String actionName,
     ActionListener actionListener)
   {
      return createJButton(parentTag, actionName, actionListener, actionName,
        buttonDefaultIconSmall, buttonDefaultOmitTextIfIcon);
   }

   /**
    * Creates a labelled button. The text and mnemonic are obtained from the tag.
    * The icon prefix is assumed to be the same as the action
    * command.
    * @param tag the language tag
    * @param actionName if not null, the button's action command
    * @param actionListener if not null, added to the button's 
    * ActionListener list
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createJButton(String parentTag, String actionName,
     ActionListener actionListener,  
     boolean smallIcon, boolean omitTextIfIcon)
   {
      return createJButton(parentTag, actionName, actionListener, actionName,
        smallIcon, omitTextIfIcon);
   }

   /**
    * Creates a labelled button. The text and mnemonic are obtained from the tag.
    * @param tag the language tag
    * @param actionName if not null, the button's action command
    * @param actionListener if not null, added to the button's 
    * ActionListener list
    * @param iconPrefix the prefix used for the icon filename
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createJButton(String parentTag, String actionName,
     ActionListener actionListener, String iconPrefix, 
     boolean smallIcon, boolean omitTextIfIcon)
   {
      String tag = actionName == null ? parentTag : parentTag+"."+actionName;

      IconSet icSet = null;

      if (iconPrefix != null)
      {
         icSet = getHelpIconSet(iconPrefix, smallIcon);
      }

      JButton button;
      String tooltip = getMessageIfExists(tag+".tooltip");

      if (omitTextIfIcon && icSet != null)
      {
         button = new JButton(icSet.getDefaultIcon());
         button.setMargin(new Insets(0, 0, 0, 0));

         if (tooltip == null)
         {
            tooltip = getMessageIfExists(tag);
         }
      }
      else if (icSet != null)
      {
         button = new JButton(getMessage(tag), icSet.getDefaultIcon());
      }
      else
      {
         button = new JButton(getMessage(tag));
      }

      if (icSet != null)
      {
         icSet.setButtonExtraIcons(button);
      }

      if (actionName != null)
      {
         button.setActionCommand(actionName);
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

   /**
    * Creates a labelled button. The text and mnemonic are obtained from the tag.
    * @param tag the language tag
    * @param actionName if not null, the button's action command
    * @param action if not null, set as the button's action
    * @param iconPrefix the prefix used for the icon filename
    * @param smallIcon if true use small icon if found
    * @param omitTextIfIcon if true don't set text if icon is found
    */
   public JButton createJButton(String parentTag, String actionName,
     Action action, JComponent comp, String iconPrefix, 
     boolean smallIcon, boolean omitTextIfIcon)
   {
      String tag = actionName == null ? parentTag : parentTag+"."+actionName;

      IconSet icSet = null;

      if (iconPrefix != null)
      {
         icSet = getHelpIconSet(iconPrefix, smallIcon);
      }

      JButton button;
      String tooltip = getMessageIfExists(tag+".tooltip");

      if (omitTextIfIcon && icSet != null)
      {
         button = new JButton(icSet.getDefaultIcon());
         button.setMargin(new Insets(0, 0, 0, 0));

         if (tooltip == null)
         {
            tooltip = getMessageIfExists(tag);
         }
      }
      else if (icSet != null)
      {
         button = new JButton(getMessage(tag), icSet.getDefaultIcon());
      }
      else
      {
         button = new JButton(getMessage(tag));
      }

      if (icSet != null)
      {
         icSet.setButtonExtraIcons(button);
      }

      if (actionName != null)
      {
         button.setActionCommand(actionName);
      }

      if (action != null)
      {
         button.setAction(action);

         if (comp != null)
         {
            KeyStroke keyStroke = getKeyStroke(tag);

            if (keyStroke != null)
            {
               comp.getInputMap().put(keyStroke, actionName);
               comp.getActionMap().put(actionName, action);
            }
         }
      }

      int mnemonic = getMnemonic(tag+".mnemonic");

      if (mnemonic > 0)
      {
         button.setMnemonic(mnemonic);
      }

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
      return createJCheckBox(parentTag, action, selected, null);
   }

   public JCheckBox createJCheckBox(String parentTag, String action,
     ActionListener listener)
   {
      return createJCheckBox(parentTag, action, false, listener);
   }

   public JCheckBox createJCheckBox(String parentTag, String action,
     boolean selected, ActionListener listener)
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

      if (listener != null)
      {
         button.setActionCommand(action);
         button.addActionListener(listener);
      }

      return button;
   }

   protected String resourcebase = "/resources";
   protected String dictionaryBase = resourcebase;
   protected String resourceIconBase = "/resources/icons";
   protected String smallIconSuffix = "-16";
   protected String largeIconSuffix = "-32";

   protected Properties imageMap = null;
   protected String mappedSmallIconSuffix = "-small";

   protected boolean buttonDefaultIconSmall = true;
   protected boolean buttonDefaultOmitTextIfIcon = false;
   protected boolean toolbarButtonDefaultIconSmall = false;
   protected String[] buttonDefaultOmitKeys;

   public static final String HELP_LIB_ICON_PATH
   = "/com/dickimawbooks/texjavahelplib/icons/";

   protected Pattern helpsetPattern;
   protected Vector<HelpSetLocale> availableHelpsets;

   protected String helpsetdir = "helpset";
   protected String helpsetsubdir = null;
   protected String helpsetSubdirPrefix = "";
   protected HelpSetLocale helpsetLocale;
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

   private Vector<HelpFontChangeListener> helpFontChangeListeners; 
   private Vector<LowerNavSettingsChangeListener> lowerNavSettingsChangeListeners; 

   private HelpFontSettings helpFontSettings;
   private boolean helpLowerNavLabelShowText = true;
   private int helpLowerNavLabelLimit = 20;

   private Dimension helpWindowInitSize;

   /**
    * List of image extensions to try when searching for icons.
    */
   public String[] imageExtensions = new String[] { "png" };

   public static final String KEYSTROKE_CSS
    = ".keystroke { font-family: sans-serif; font-weight: bold; border: 2pt outset gray; background-color: silver; }";

   public static final String ICON_CSS_CLASSES = ".icon, .locationprefix";

   public static final String ICON_CSS = ICON_CSS_CLASSES
    + " { font-family: serif; }"; 

   public static final String MENU_CSS_CLASSES = ".menu, .menuitem .dialog";

   public static final String MENU_CSS = MENU_CSS_CLASSES
    + " { font-weight: bold; }"; 

   public static final String MONO_CSS_CLASSES
    = ".code, .cmd, .cmdfmt, .csfmt, .csfmtfont, .csfmtcolourfont, .appfmt, .styfmt, .clsfmt, .envfmt, .optfmt, .csoptfmt, .styoptfmt, .clsoptfmt, .ctrfmt, .filefmt, .extfmt, .cbeg, .cend, .longargfmt, .shortargfmt, .qtt, .xmltagfmt, .varfmt, .terminal, .transcript, .filedef, .codebox, .badcodebox, .unicodebox, .compactcodebox, .sidebysidecode";

   public static final int SYNTAX_ITEM_LINEWIDTH=78;
   public static final int SYNTAX_ITEM_TAB=30;

   public static final String LICENSE_GPL3 = String.format(
   "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>%nThis is free software: you are free to change and redistribute it.%nThere is NO WARRANTY, to the extent permitted by law.");

   public static final String VERSION = "0.9a.20250729";
   public static final String VERSION_DATE = "2025-07-29";
}
