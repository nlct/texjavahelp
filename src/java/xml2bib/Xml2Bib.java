/*
    Copyright (C) 2024-2025 Nicola L.C. Talbot
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
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;

public class Xml2Bib
{
   public Xml2Bib()
   {
      inFileNames = new Vector<String>();
   }

   public int getExitCode()
   {
      return helpLibApp == null ? exitCode : helpLibApp.getExitCode();
   }

   public void setExitCode(int code)
   {
      if (helpLibApp != null)
      {
         helpLibApp.setExitCode(code);
      }

      exitCode = code;
   }

   public int getExitCode(Throwable e, boolean isFatal)
   {
      if (helpLibApp != null)
      {
         return helpLibApp.getExitCode(e, isFatal);
      }
      else if (e instanceof InvalidSyntaxException)
      {
         return TeXJavaHelpLibAppAdapter.EXIT_SYNTAX;
      }
      else if (e instanceof IOException)
      {
         return TeXJavaHelpLibAppAdapter.EXIT_IO;
      }
      else
      {
         return TeXJavaHelpLibAppAdapter.EXIT_OTHER; 
      }
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

          @Override
          public void message(String msg)
          {
             if (verboseLevel > 0)
             {
                stdOutMessage(msg);
             }
          }

       };
      helpLib = new TeXJavaHelpLib(helpLibApp);
      helpLibApp.setHelpLib(helpLib);

      if (exitCode != 0)
      {
         helpLibApp.setExitCode(exitCode);
      }
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
         setExitCode(getExitCode(e, false));
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

      if (e != null)
      {
         System.err.format("%s: %s: %s%n", getApplicationName(), msgTag, message);

         if (debugMode)
         {
            e.printStackTrace();
         }
      }
      else if (msgTag.contains("error"))
      {
         System.err.format("%s: %s: %s%n", getApplicationName(), msgTag, message);
      }
      else
      {
         System.out.format("%s: %s: %s%n", getApplicationName(), msgTag, message);
      }
   }

   public void debug(String message, Throwable e)
   {
      if (debugMode)
      {
         error(message, e);
      }
   }

   public void syntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("clisyntax.usage",
        getMessage("syntax.options", getApplicationName())));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.in", "--in", "-i"));

      helpLib.printSyntaxItem(getMessage("syntax.prop", "--prop"));

      helpLib.printSyntaxItem(getMessage("syntax.out", "--output", "-o"));

      helpLib.printSyntaxItem(getMessage("syntax.out.charset", "--out-charset"));
      helpLib.printSyntaxItem(getMessage("syntax.provide-xml",
         "--provide-xml", getApplicationName()));

      helpLib.printSyntaxItem(getMessage("syntax.copy-overwrite-xml",
          "--copy-overwrite-xml", getApplicationName()));

      helpLib.printSyntaxItem(getMessage("syntax.encapless-field", "--encapless-field"));

      helpLib.printSyntaxItem(getMessage("syntax.auto-trim", "--[no]auto-trim"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.debug", "--[no]debug"));


      helpLib.printSyntaxItem(getMessage("clisyntax.version2", "--version", "-v"));

      helpLib.printSyntaxItem(getMessage("clisyntax.help2", "--help", "-h"));

      System.out.println();

      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   public void versionInfo()
   {
      if (!shownVersion)
      {
         System.out.println(getMessageWithFallback("about.version_date",
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

   public boolean isAutoTrimOn()
   {
      return autoTrim;
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

      Vector<String> titleList = new Vector<String>();
      Vector<String> indexList = new Vector<String>();
      Vector<String> menuList = new Vector<String>();
      Vector<String> otherList = new Vector<String>();
      Vector<String> fieldList = new Vector<String>();

      for (Enumeration en = props.propertyNames(); en.hasMoreElements(); )
      {
         String key = (String)en.nextElement();

         int idx = key.lastIndexOf('.');

         if (key.endsWith(".title"))
         {
            titleList.add(key);
         }
         else if (idx > -1
               && KEY_SUFFIX_PATTERN.matcher(key.substring(idx+1)).matches())
         {
            fieldList.add(key);
         }
         else if (key.startsWith("index.")
               || key.startsWith("manual.")
               || key.startsWith("term.")
                 )
         {
            indexList.add(key);
         }
         else if (key.startsWith("menu."))
         {
            menuList.add(key);
         }
         else
         {
            otherList.add(key);
         }
      }

      titleList.sort(null);
      indexList.sort(null);
      otherList.sort(null);

      HashMap<String,Entry> entries = new HashMap<String,Entry>();

      processEntries(indexList, props, entries);
      processEntries(menuList, props, entries);
      processEntries(titleList, props, entries);
      processEntries(otherList, props, entries);
      processEntries(fieldList, props, entries);

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

   protected void processEntries(Vector<String> keyList, Properties props,
    HashMap<String,Entry> entries)
   {
      for (String key : keyList)
      {
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
                  = !(key.startsWith("index.")
                       || key.startsWith("manual.")
                       || key.startsWith("term.")
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

   }

   protected String getKeyStrokeValue(String fieldValue, Properties props)
   {
      String[] split = fieldValue.split("\\s+");

      StringBuilder builder = new StringBuilder();

      for (String s : split)
      {
         String name = s.toLowerCase();

         if (name.equals("enter"))
         {
            name = "return";
         }
         else if (s.equals("control"))
         {
            name = "ctrl";
         }

         String keyref = KEYREF_PREFIX + name;
         String propVal = props.getProperty(keyref);

         if (propVal == null && !s.equals("_") && s.contains("_"))
         {
            keyref = KEYREF_PREFIX + name.replaceAll("_", "");
            propVal = props.getProperty(keyref);
         }

         if (builder.length() > 0)
         {
            builder.append('+');
         }

         if (propVal != null)
         {
            builder.append("\\keyref{");

            if (keyref.startsWith(KEYREF_PREFIX))
            {
               builder.append(keyref, KEYREF_PREFIX.length(), keyref.length());
            }
            else
            {
               builder.append(keyref);
            }

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
                  case KeyEvent.VK_CONTEXT_MENU:
                    s = "\\contextmenusym ";
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
      CLISyntaxParser cliParser = new CLISyntaxParser(helpLib, args, "-h", "-v")
      {
         @Override
         protected int argCount(String arg)
         {
            if (arg.equals("--encapless-field")
             || arg.equals("--in") || arg.equals("-i")
             || arg.equals("--output") || arg.equals("-o")
             || arg.equals("--prop")
             || arg.equals("--out-charset")
               )
            {
               return 1;
            }

            return 0;
         }

         @Override
         public boolean setDebugOption(String option, Integer value)
         throws InvalidSyntaxException
         {
            debugMode = true;

            return true;
         }

         @Override
         protected boolean preparseCheckArg()
         throws InvalidSyntaxException
         {
            if (super.preparseCheckArg())
            {
               return true;
            }

            if (originalArgList[preparseIndex].equals("--verbose"))
            {
               verboseLevel = 1;
            }
            else if (originalArgList[preparseIndex].equals("--noverbose"))
            {
               verboseLevel = 0;
            }
            else if (originalArgList[preparseIndex].equals("--nodebug")
                   || originalArgList[preparseIndex].equals("--no-debug")
                    )
            {
               debugMode = false;
            }
            else
            {
               return false;
            }

            return true;
         }

         @Override
         protected void help()
         {
            syntax();
            System.exit(0);
         }

         @Override
         protected void version()
         {
            System.out.println(getHelpLib().getAboutInfo(false,
              TeXJavaHelpLib.VERSION,
              TeXJavaHelpLib.VERSION_DATE,
              String.format(
               "Copyright (C) %s Nicola L. C. Talbot (%s)",
                TeXJavaHelpLib.VERSION_DATE.substring(0, 4),
                getHelpLib().getInfoUrl(false, "www.dickimaw-books.com")),
               TeXJavaHelpLib.LICENSE_GPL3,
               true, null
            ));

            System.exit(0);
         }

         @Override
         protected void parseArg(String arg)
         throws InvalidSyntaxException
         {
            inFileNames.add(arg);
         }

         @Override
         protected boolean parseArg(String arg, CLIArgValue[] returnVals)
         throws InvalidSyntaxException
         {
            if (arg.equals("--provide-xml"))
            {
               copyXml = true;
               replaceXml = false;
            }
            else if (arg.equals("--copy-overwrite-xml"))
            {
               copyXml = true;
               replaceXml = true;
            }
            else if (arg.equals("--nocopy-xml"))
            {
               copyXml = false;
            }
            else if (arg.equals("--auto-trim"))
            {
               autoTrim = true;
            }
            else if (arg.equals("--noauto-trim"))
            {
               autoTrim = false;
            }
            else if (isArg(arg, "--encapless-field", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               noEncapField = returnVals[0].toString();
            }
            else if (isArg(arg, "--in", "-i", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               inFileNames.add(returnVals[0].toString());
            }
            else if (isArg(arg, "--prop", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               if (propFileNames == null)
               {
                  propFileNames = new Vector<String>();
               }

               propFileNames.add(returnVals[0].toString());
            }
            else if (isArg(arg, "--output", "-o", returnVals))
            {
               if (outFile != null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.only_one", arg));
               }

               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               outFile = new File(returnVals[0].toString());
            }
            else if (isArg(arg, "--out-charset", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               if (propFileNames == null)
               {
                  propFileNames = new Vector<String>();
               }

               outCharset = Charset.forName(returnVals[0].toString());
            }
            else
            {
               return false;
            }

            return true;
         }
      };

      cliParser.process();

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
         app.setExitCode(TeXJavaHelpLibAppAdapter.EXIT_SYNTAX);
      }
      catch (Throwable e)
      {
         app.error(null, e);
      }

      System.exit(app.getExitCode());
   }

   protected boolean debugMode = false;
   protected boolean shownVersion = false;
   protected File outFile;
   protected Vector<String> inFileNames, propFileNames;
   private Charset outCharset = Charset.defaultCharset();
   protected int verboseLevel = 0;
   protected boolean copyXml = false, replaceXml = false;
   protected String noEncapField=null;// field to save non-encapsulated value
   protected boolean autoTrim = true;
   protected int exitCode = 0;

   private TeXJavaHelpLib helpLib;
   private TeXJavaHelpLibAppAdapter helpLibApp;

   public static final Pattern KEY_SUFFIX_PATTERN
     = Pattern.compile("mnemonic|tooltip|description|keystroke|defaultkeys|plural|symbol|user[1-6]|text|defaultparams|syntax|initvalue|defaultvalue|see(also)?|alias|iconimage");
   public static final Pattern TEX_SUFFIX_PATTERN
     = Pattern.compile("plural|text|symbol|user[1-6]|defaultparams|defaultkeys|syntax|initvalue|defaultvalue");

   public static final String NAME = "tjhxml2bib";

   public static final String KEYREF_PREFIX = "manual.keystroke.";
}
