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
package com.dickimawbooks.xml2bib;

import java.io.*;

import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.charset.Charset;

import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.MessageFormat;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibAppAdapter;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;

public class Xml2Bib
{
   public Xml2Bib()
   {
      inFileNames = new Vector<String>();
   }

   protected void initHelpLibrary() throws IOException
   {
      helpLibApp = new TeXJavaHelpLibAppAdapter()
       {
          @Override
          public boolean isGUI() { return false; }

          @Override
          public String getApplicationName()
          {
             return NAME;
          }

          @Override
          public boolean isDebuggingOn()
          {
             return debugMode;
          }

       };
      helpLib = new TeXJavaHelpLib(helpLibApp);
      helpLibApp.setHelpLib(helpLib);
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      if (helpLib == null)
      {
         MessageFormat fmt = new MessageFormat(fallbackFormat);

         return fmt.format(params);
      }

      return helpLib.getMessageWithFallback(label, fallbackFormat, params);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      if (helpLib == null) return null;

      return helpLib.getMessageIfExists(label, args);
   }

   public String getMessage(String label, Object... params)
   {
      if (helpLib == null)
      {// message system hasn't been initialised

         String param = (params.length == 0 ? "" : params[0].toString());

         for (int i = 1; i < params.length; i++)
         {
            param += ","+params[0].toString();
         }

         return String.format("%s[%s]", label, param);
      }

      return helpLib.getMessage(label, params);
   }

   public void error(String message, Throwable e)
   {
      if (helpLibApp != null)
      {
         helpLibApp.error(message, e);
      }
      else
      {
         message("error", message, e);
      }
   }

   public void message(String msgTag, String message, Throwable e)
   {
      if (message == null)
      {
         if (e != null)
         {
            message = e.getMessage();
         }

         if (message == null)
         {
            message = e.getClass().getSimpleName();
         }
      }

      System.err.format("%s: %s: %s%n", getApplicationName(), msgTag, message);

      if (debugMode && e != null)
      {
         e.printStackTrace();
      }
   }

   public void debug(String message, Throwable e)
   {
      if (debugMode)
      {
         error(message, e);
      }
   }

   public void help()
   {
      version();
      System.out.println();
      System.out.println(getMessage("syntax.title"));
      System.out.println();
      System.out.println(getMessage("syntax.options", getApplicationName()));
      System.out.println();
      System.out.println(getMessage("syntax.in", "--in", "-i"));
      System.out.println(getMessage("syntax.prop", "--prop"));
      System.out.println(getMessage("syntax.out", "--output", "-o"));
      System.out.println(getMessage("syntax.out.charset", "--out-charset"));
      System.out.println(getMessage("syntax.provide-xml",
         "--provide-xml", getApplicationName()));
      System.out.println(getMessage("syntax.copy-overwrite-xml",
          "--copy-overwrite-xml", getApplicationName()));

      System.out.println(getMessage("syntax.encapless-field", "--encapless-field"));
      System.out.println();
      System.out.println(getMessage("syntax.debug", "--debug"));
      System.out.println(getMessage("syntax.nodebug", "--nodebug"));
      System.out.println(getMessage("syntax.version", "--version", "-v"));
      System.out.println(getMessage("syntax.help", "--help", "-h"));
      System.out.println();
      System.out.println(getMessage("syntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   public void version()
   {
      if (!shownVersion)
      {
         System.out.println(getMessageWithFallback("about.version",
           "{0} version {1} ({2})", getApplicationName(), 
           TeXJavaHelpLib.VERSION, TeXJavaHelpLib.VERSION_DATE));
         shownVersion = true;
      }
   }

   public void license()
   {
      System.out.println();
      System.out.format("Copyright %s Nicola Talbot%n",
       getCopyrightDate());
      System.out.println(getMessage("about.license"));
      System.out.println("https://github.com/nlct/texjavahelp");
   }

   public String getCopyrightStartYear()
   {
      return "2024";
   }

   public String getCopyrightDate()
   {
      String startYr = getCopyrightStartYear();
      String endYr = TeXJavaHelpLib.VERSION_DATE.substring(0, 4);

      if (startYr.equals(endYr))
      {
         return endYr;
      }
      else
      {
         return String.format("%s-%s", startYr, endYr);
      }
   }

   public String getApplicationName()
   {
      return NAME;
   }

   public String getApplicationVersion()
   {
      return TeXJavaHelpLib.VERSION;
   }

   protected boolean isNewer(File file1, File file2)
   {
      try
      {
         return file1.lastModified() > file2.lastModified();
      }
      catch (SecurityException e)
      {
         debug(null, e);
         return false;
      }
   }

   protected File getXmlFile(String filename)
   {
      File file = new File(filename);

      if (copyXml)
      {
         URL url = getClass().getResource(
           helpLib.getResourcePath()+"/"+file.getName());

         if (url != null)
         {
            try
            {
               File resourceFile = new File(url.toURI());

               if (!file.exists() || (replaceXml && isNewer(resourceFile, file)))
               {
                  helpLibApp.message(getMessageWithFallback("message.copying",
                    "Copying {0} -> {1}", resourceFile, file));

                  Files.copy(resourceFile.toPath(), file.toPath(),
                   StandardCopyOption.REPLACE_EXISTING);
               }
            }
            catch (Exception e)
            {
               debug(null, e);
            }
         }
      }

      return file;
   }

   public String getNoEncapField()
   {
      return noEncapField;
   }

   protected void run() throws IOException
   {
      Properties props = new Properties();

      InputStream in = null;
      PrintWriter out = null;

      try
      {
         /*
          The order of files is important as duplicate keys will be overridden
          in a subsequent file. This allows texjavahelplib-*.xml to be specified
          first and the application's resource file next, which can override
          default values.
          */
         for (String filename : inFileNames)
         {
            in = new FileInputStream(getXmlFile(filename));
            props.loadFromXML(in);
            in.close();
            in = null;
         }

         // Also support property files with key=value entries
         if (propFileNames != null)
         {
            for (String filename : propFileNames)
            {
               in = new FileInputStream(new File(filename));
               props.load(in);
               in.close();
               in = null;
            }
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
            in = null;
         }
      }

      HashMap<String,Entry> entries = new HashMap<String,Entry>();

      for (Enumeration en = props.propertyNames(); en.hasMoreElements(); )
      {
         String key = (String)en.nextElement();

         if (entries.containsKey(key)) continue;

         int idx = key.lastIndexOf('.');

         if (idx > -1)
         {
            String prefix = key.substring(0, idx);
            String suffix = key.substring(idx+1);

            Entry entry = null;
            String parent = prefix;

            if (KEY_SUFFIX_PATTERN.matcher(suffix).matches())
            {
               String fieldValue = (String)props.getProperty(key);
               key = prefix;

               idx = key.lastIndexOf('.');

               if (idx > 0)
               {
                  parent = key.substring(0, idx);
               }
               else
               {
                  parent = null;
               }

               entry = entries.get(key);

               if (entry == null)
               {
                  entry = entries.get(key+".title");
               }

               if (entry == null)
               {
                  String value = (String)props.getProperty(key);
                  entry = new Entry(this, key, value);
                  entries.put(key, entry);
               }

               boolean encode
                  = !(key.startsWith("index.") || key.startsWith("manual.")
                       || TEX_SUFFIX_PATTERN.matcher(suffix).matches());

               if (suffix.equals("keystroke"))
               {
                  String keyStrokeVal = getKeyStrokeValue(fieldValue, props);

                  if (keyStrokeVal != null)
                  {
                     fieldValue = keyStrokeVal;
                     encode = false;
                  }
               }

               entry.put(suffix, fieldValue, encode);
            }

            if (entry == null)
            {
               entry = new Entry(this, key, (String)props.getProperty(key));
               entries.put(key, entry);
            }

            if (parent != null
                 && entry.getParent() == null
                 && !suffix.equals("title"))
            {
               Entry parentEntry = entries.get(parent);

               if (parentEntry == null)
               {
                  parentEntry = entries.get(parent+".title");
               }

               if (parentEntry == null)
               {
                  parentEntry = entries.get("index."+parent);

                  if (parentEntry == null)
                  {
                     String val = (String)props.get(parent);

                     if (val == null)
                     {
                        val = (String)props.get(parent+".title");

                        if (val != null)
                        {
                           parent += ".title";
                        }
                     }

                     if (val == null)
                     {
                        val = (String)props.get("index."+parent);

                        if (val != null)
                        {
                           parent = "index."+parent;
                        }
                     }

                     if (val != null)
                     {
                        parentEntry = new Entry(this, parent, val);
                        entries.put(parent, parentEntry);
                     }
                  }
               }

               if (parentEntry != null && !parentEntry.equals(entry))
               {
                  entry.setParent(parentEntry);
               }
            }
         }
         else
         {
            entries.put(key,
              new Entry(this, key, (String)props.getProperty(key)));
         }
      }

      try
      {
         out = new PrintWriter(Files.newBufferedWriter(outFile.toPath(), outCharset));

         out.print("% Encoding: ");
         out.println(outCharset.name());

         for (String key : entries.keySet())
         {
            Entry entry = entries.get(key);
            entry.write(out);
         }
      }
      finally
      {
         if (out != null)
         {
            out.close();
            out = null;
         }
      }
   }

   protected String getKeyStrokeValue(String fieldValue, Properties props)
   {
      String[] split = fieldValue.split("\\s+");

      StringBuilder builder = new StringBuilder();

      for (String s : split)
      {
         String keyref = "manual.keystroke." + s.toLowerCase();
         String propVal = props.getProperty(keyref);

         if (propVal == null && !s.equals("_") && s.contains("_"))
         {
            keyref = "manual.keystroke."
                + s.replaceAll("_", "").toLowerCase();
            propVal = props.getProperty(keyref);
         }

         if (builder.length() > 0)
         {
            builder.append('+');
         }

         if (propVal != null)
         {
            builder.append("\\keyref{");
            builder.append(keyref);
            builder.append("}");
         }
         else if (!(s.equals("typed")
                 || s.equals("released")
                 || s.equals("pressed")))
         {
            KeyStroke ks = KeyStroke.getKeyStroke(s);

            boolean encode = true;

            if (ks != null)
            {
               switch (ks.getKeyCode())
               {
                  case KeyEvent.VK_AT:
                    s = "@";
                    break;
                  case KeyEvent.VK_BACK_QUOTE:
                    s = "\\textasciigrave ";
                    encode = false;
                    break;
                  case KeyEvent.VK_BACK_SLASH:
                    s = "\\";
                    break;
                  case KeyEvent.VK_BRACELEFT:
                    s = "{";
                    break;
                  case KeyEvent.VK_BRACERIGHT:
                    s = "}";
                    break;
                  case KeyEvent.VK_CIRCUMFLEX:
                    s = "^";
                    break;
                  case KeyEvent.VK_CLOSE_BRACKET:
                    s = "]";
                    break;
                  case KeyEvent.VK_COLON:
                    s = ":";
                    break;
                  case KeyEvent.VK_COMMA:
                    s = ",";
                    break;
                  case KeyEvent.VK_DOLLAR:
                    s = "$";
                    break;
                  case KeyEvent.VK_EQUALS:
                    s = "$";
                    break;
                  case KeyEvent.VK_EURO_SIGN:
                    s = "\\texteuro ";
                    encode = false;
                    break;
                  case KeyEvent.VK_EXCLAMATION_MARK:
                    s = "!";
                    break;
                  case KeyEvent.VK_GREATER:
                    s = ">";
                    break;
                  case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
                    s = "\\textexclamdown ";
                    encode = false;
                    break;
                  case KeyEvent.VK_LEFT_PARENTHESIS:
                    s = "(";
                    break;
                  case KeyEvent.VK_LESS:
                    s = "<";
                    break;
                  case KeyEvent.VK_MINUS:
                    s = "-";
                    break;
                  case KeyEvent.VK_NUMBER_SIGN:
                    s = "#";
                    break;
                  case KeyEvent.VK_OPEN_BRACKET:
                    s = "[";
                    break;
                  case KeyEvent.VK_PERIOD:
                    s = ".";
                    break;
                  case KeyEvent.VK_PLUS:
                    s = "+";
                    break;
                  case KeyEvent.VK_QUOTE:
                    s = "\\textquotesingle ";
                    encode = false;
                    break;
                  case KeyEvent.VK_QUOTEDBL:
                    s = "\\textquotedouble ";
                    encode = false;
                    break;
                  case KeyEvent.VK_RIGHT_PARENTHESIS:
                    s = "+";
                    break;
                  case KeyEvent.VK_SEMICOLON:
                    s = "+";
                    break;
                  case KeyEvent.VK_SLASH:
                    s = "/";
                    break;
                  case KeyEvent.VK_UNDERSCORE:
                    s = "_";
                    break;
               }
            }

            builder.append("\\actualkey{");

            if (encode)
            {
               Entry.encodeTeXChars(builder, s);
            }
            else
            {
               builder.append(s);
            }

            builder.append("}");
         }
      }

      if (builder.length() > 0)
      {
         return "\\keys{" + builder.toString() + "}";
      }
      else
      {
         return null;
      }
   }

   private void parseArgs(String[] args) throws InvalidSyntaxException
   {
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("--version") || args[i].equals("-v"))
         {
            version();
            System.exit(0);
         }
         else if (args[i].equals("--help") || args[i].equals("-h"))
         {
            help();
            System.exit(0);
         }
         else if (args[i].equals("--debug"))
         {
            debugMode = true;
         }
         else if (args[i].equals("--nodebug"))
         {
            debugMode = false;
         }
         else if (args[i].equals("--verbose"))
         {
            verboseLevel = 1;
         }
         else if (args[i].equals("--provide-xml"))
         {
            copyXml = true;
            replaceXml = false;
         }
         else if (args[i].equals("--copy-overwrite-xml"))
         {
            copyXml = true;
            replaceXml = true;
         }
         else if (args[i].equals("--nocopy-xml"))
         {
            copyXml = false;
         }
         else if (args[i].equals("--encapless-field"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_value",
                   args[i-1]));
            }

            noEncapField = args[i];
         }
         else if (args[i].equals("--in") || args[i].equals("-i"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_input",
                   args[i-1]));
            }

            inFileNames.add(args[i]);
         }
         else if (args[i].equals("--prop"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_input",
                   args[i-1]));
            }

            if (propFileNames == null)
            {
               propFileNames = new Vector<String>();
            }

            propFileNames.add(args[i]);
         }
         else if (args[i].equals("--output") || args[i].equals("-o"))
         {
            if (outFile != null)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.only_one", args[i]));
            }

            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_filename", args[i-1]));
            }

            outFile = new File(args[i]);

         }
         else if (args[i].equals("--out-charset"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_input",
                   args[i-1]));
            }

            outCharset = Charset.forName(args[i]);
         }
         else if (args[i].charAt(0) == '-')
         {
            throw new InvalidSyntaxException(
             getMessage("error.syntax.unknown_option", args[i]));
         }
         else
         {
            // if no option specified, assume --in

            inFileNames.add(args[i]);
         }
      }

      if (inFileNames.isEmpty())
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_in"));
      }
      
      if (outFile == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_out"));
      }
   }

   public static void main(String[] args)
   {
      final Xml2Bib app = new Xml2Bib();

      try
      {
         app.initHelpLibrary();
         app.parseArgs(args);
         app.run();
      }
      catch (InvalidSyntaxException e)
      {
         app.error(e.getMessage(), null);

         System.exit(1);
      }
      catch (Throwable e)
      {
         app.error(null, e);

         System.exit(2);
      }
   }

   protected boolean debugMode = false;
   protected boolean shownVersion = false;
   protected File outFile;
   protected Vector<String> inFileNames, propFileNames;
   private Charset outCharset = Charset.defaultCharset();
   protected int verboseLevel = 0;
   protected boolean copyXml = false, replaceXml = false;
   protected String noEncapField=null;// field to save non-encapsulated value

   private TeXJavaHelpLib helpLib;
   private TeXJavaHelpLibAppAdapter helpLibApp;

   public static final Pattern KEY_SUFFIX_PATTERN
     = Pattern.compile("mnemonic|tooltip|description|keystroke|defaultkeys|plural|symbol|user[1-6]|name|text|defaultparams");
   public static final Pattern TEX_SUFFIX_PATTERN
     = Pattern.compile("plural|text|name|symbol|user[1-6]|defaultparams|defaultkeys");

   public static final String NAME = "xml2bib";
}
