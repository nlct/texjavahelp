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

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.Charset;

import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;

import java.text.MessageFormat;

import java.awt.event.KeyEvent;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibApp;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;

public class Xml2Bib implements TeXJavaHelpLibApp
{
   protected void initHelpLibrary() throws IOException
   {
      helpLib = new TeXJavaHelpLib(this);
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

   @Override
   public void error(String message)
   {
      error(message, null);
   }

   @Override
   public void error(Throwable e)
   {
      error(e.getMessage(), e);
   }

   @Override
   public void error(String message, Throwable e)
   {
      message("error", message, e);
   }

   @Override
   public void warning(String message)
   {
      warning(message, null);
   }

   @Override
   public void warning(String message, Throwable e)
   {
      message("warning", message, e);
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

   @Override
   public void debug(Throwable e)
   {
      debug(e.getMessage(), e);
   }

   @Override
   public void debug(String msg)
   {
      debug(msg, null);
   }

   @Override
   public void debug(String message, Throwable e)
   {
      if (debugMode)
      {
         if (message != null)
         {
            System.out.println(message);
         }

         if (e != null)
         {
            e.printStackTrace();
         }
      }
   }

   public void help()
   {
      version();
      System.out.println();
      System.out.println(getMessage("syntax.title"));
      System.out.println();
      System.out.println(getMessage("syntax.opt_in", getApplicationName()));
      System.out.println();
      System.out.println(getMessage("syntax.general"));
      System.out.println(getMessage("syntax.in", "--in", "-i", getApplicationName()));
      System.out.println();
      System.out.println(getMessage("syntax.debug", "--debug"));
      System.out.println(getMessage("syntax.nodebug", "--nodebug"));
      System.out.println();
      System.out.println(getMessage("syntax.version", "--version", "-v"));
      System.out.println(getMessage("syntax.help", "--help", "-h"));
      System.out.println();
      System.out.println(getMessage("syntax.output.options"));
      System.out.println();
      System.out.println(getMessage("syntax.out", "--output", "-o"));
      System.out.println(getMessage("syntax.out.charset", "--out-charset"));
      System.out.println();
      System.out.println(getMessage("syntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   public void version()
   {
      if (!shownVersion)
      {
         System.out.println(getMessageWithFallback("about.version",
           "{0} version {1} ({2})", getApplicationName(), VERSION, DATE));
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
      String endYr = DATE.substring(0, 4);

      if (startYr.equals(endYr))
      {
         return endYr;
      }
      else
      {
         return String.format("%s-%s", startYr, endYr);
      }
   }

   @Override
   public String getApplicationName()
   {
      return NAME;
   }

   public String getApplicationVersion()
   {
      return VERSION;
   }

   protected void run() throws IOException
   {
      Properties props = new Properties();

      InputStream in = null;
      PrintWriter out = null;

      try
      {
         in = new FileInputStream(inFile);

         props.loadFromXML(in);
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

            if (suffix.equals("mnemonic")
              || suffix.equals("tooltip")
              || suffix.equals("description")
              || suffix.equals("keystroke")
              || suffix.equals("defaultkeys")
               )
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
                  String value = (String)props.getProperty(key);
                  entry = new Entry(key, value);
                  entries.put(key, entry);
               }

               boolean encode = true;

               if (suffix.equals("keystroke"))
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
                        int keyCode = 0;

                        try
                        {
                           keyCode = KeyEvent.class.getField(s).getInt(KeyEvent.class);
                        }
                        catch (NoSuchFieldException | IllegalAccessException e)
                        {
                        }

                        if (keyCode >= 0x20 && keyCode < 0x7F)
                        {
                           builder.append("\\actualkey{");
                           Entry.encodeTeXChars(builder, keyCode);
                           builder.append("}");
                        }
                        else
                        {
                           builder.append("\\actualkey{");
                           Entry.encodeTeXChars(builder, s);
                           builder.append("}");
                        }
                     }
                  }

                  if (builder.length() > 0)
                  {
                     fieldValue = "\\keys{" + builder.toString() + "}";
                  }

                  encode = false;
               }

               entry.put(suffix, fieldValue, encode);
            }

            if (entry == null)
            {
               entry = new Entry(key, (String)props.getProperty(key));
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
                        parentEntry = new Entry(parent, val);
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
            entries.put(key, new Entry(key, (String)props.getProperty(key)));
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
         else if (args[i].equals("--in") || args[i].equals("-i"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_input",
                   args[i-1]));
            }

            if (inFile != null)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.only_one_input"));
            }

            inFile = new File(args[i]);
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
            // if no option specified, assume --in or --out

            if (inFile == null)
            {
               inFile = new File(args[i]);
            }
            else if (outFile == null)
            {
               outFile = new File(args[i]);
            }
            else
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.only_one_inout"));
            }

         }
      }

      if (inFile == null)
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
         app.error(e.getMessage());

         System.exit(1);
      }
      catch (Exception e)
      {
         app.error(e);

         System.exit(1);
      }
   }

   protected boolean debugMode = false;
   protected boolean shownVersion = false;
   protected File inFile, outFile;
   private Charset outCharset = Charset.defaultCharset();

   private TeXJavaHelpLib helpLib;

   public static final String NAME = "xml2bib";
   public static final String VERSION = "0.1a";
   public static final String DATE = "2024-05-03";
}
