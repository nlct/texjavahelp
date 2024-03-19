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

package com.dickimawbooks.texjavahelpmk;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.MessageFormat;
import java.text.ChoiceFormat;

import java.io.*;

import java.nio.file.*;
import java.nio.charset.Charset;

import java.net.URL;

import java.awt.Dimension;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.html.*;

public class TeXJavaHelpMk implements TeXApp
{

   // Localisation and messages

   protected String getLanguageFileName(String tag)
   {
      return String.format("/resources/texjavahelpmk-%s.xml", tag);
   }

   protected URL getLanguageFile() throws FileNotFoundException
   {
      Locale locale = Locale.getDefault();
      String tag = locale.toLanguageTag();

      String dict = getLanguageFileName(tag);

      URL url = getClass().getResource(dict);

      if (url != null) return url;

      String lang = locale.getLanguage();
      String region = locale.getCountry();

      if (!region.isEmpty())
      {
         tag = String.format("%s-%s", lang, region);

         dict = getLanguageFileName(tag);
         url = getClass().getResource(dict);

         if (url != null) return url;
      }

      dict = getLanguageFileName(lang);
      url = getClass().getResource(dict);

      if (url != null) return url;

      dict = getLanguageFileName("en");
      url = getClass().getResource(dict);

      if (url == null)
      {
         throw new FileNotFoundException
         (
            "Can't find dictionary resource file matching locale "+locale+" or fallback \"en\""
         );
      }

      return url;
   }

   public void loadDictionary()
      throws IOException
   {
      URL url = getLanguageFile();

      InputStream in = null;

      try
      {
         in = url.openStream();

         Properties dictionary = new Properties();
         dictionary.loadFromXML(in);

         messages = new Hashtable<String,MessageFormat>();

         for (Object key : dictionary.keySet())
         {
            messages.put((String)key, new MessageFormat((String)dictionary.get(key)));
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      MessageFormat fmt = null;

      if (messages == null)
      {
         fmt = messages.get(label);
      }

      if (fmt == null)
      {
         fmt = new MessageFormat(fallbackFormat);
         warning("Can't find message for label "+label);
      }

      return fmt.format(params);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      MessageFormat msg = messages.get(label);

      if (msg == null)
      {
         return null;
      }

      return msg.format(args);
   }

   public String getMessage(String label, Object... params)
   {
      if (messages == null)
      {// message system hasn't been initialised

         String param = (params.length == 0 ? "" : params[0].toString());

         for (int i = 1; i < params.length; i++)
         {
            param += ","+params[0].toString();
         }

         return String.format("%s[%s]", label, param);
      }

      MessageFormat msg = messages.get(label);

      if (msg == null)
      {
         warning("Can't find message for label: "+label);

         return label;
      }

      return msg.format(params);
   }

   public String getChoiceMessage(String label, int argIdx,
      String choiceLabel, int numChoices, Object... args)
   {
      String[] part = new String[numChoices];

      double[] limits = new double[numChoices];

      for (int i = 0; i < numChoices; i++)
      {
         String tag = String.format("message.%d.%s", i, choiceLabel);

         MessageFormat fmt = messages.get(tag);

         if (fmt == null)
         {
            throw new IllegalArgumentException(
             "Invalid message label: "+tag);
         }

         part[i] = fmt.toPattern();
         limits[i] = i;
      }

      MessageFormat fmt = messages.get(label);

      if (fmt == null)
      {
         throw new IllegalArgumentException(
          "Invalid message label: "+label);
      }

      ChoiceFormat choiceFmt = new ChoiceFormat(limits, part);

      fmt.setFormatByArgumentIndex(argIdx, choiceFmt);

      return fmt.format(args);
   }

   public void warning(String message)
   {
      logAndStdErrMessage(String.format("%s: %s", getApplicationName(), message));
   }

   public void warning(String message, Throwable e)
   {
      logAndStdErrMessage(e, String.format("%s: %s", getApplicationName(), message));
   }

   public void warning(TeXParser parser, String message)
   {     
      File file = parser.getCurrentFile();
      int lineNum = parser.getLineNumber();

      if (file != null && lineNum > 0)
      {  
         message = String.format("%s:%d: %s%n", file.getName(), lineNum, message);
      }  

      logAndStdErrMessage(message);
   }

   public void error(String message)
   {
      logAndStdErrMessage(String.format("%s: %s", getApplicationName(), message));
   }

   public void error(Exception e)
   {
      if (e instanceof TeXSyntaxException)
      {
         error(((TeXSyntaxException)e).getMessage(this));
      }
      else
      {
         error(e.getMessage());
      }

      if (debugMode > 0)
      {
         e.printStackTrace();
      }
   }

   public void logAndStdErrMessage(String message)
   {
      logAndStdErrMessage(null, message);
   }

   public void logAndStdErrMessage(Throwable e)
   {
      logAndStdErrMessage(e, null);
   }

   public void logAndStdErrMessage(Throwable e, String message)
   {
      if (message != null)
      {
         System.err.println(message);
      }

      if (e != null)
      {
         e.printStackTrace();
      }

      if (logWriter != null)
      {
         if (message != null)
         {
            logWriter.println(message);
         }

         if (e != null)
         {
            e.printStackTrace(logWriter);
         }
      }
   }

   public void logAndPrintMessage(String message)
   {
      System.out.println(message);

      if (logWriter != null)
      {
         logWriter.println(message);
      }
   }

   public void logMessage(String message)
   {
      if (logWriter != null)
      {
         logWriter.println(message);
      }
   }

   public void debug(String message)
   {
      if (debugMode > 0)
      {
         logAndPrintMessage(String.format("%s: %s", getApplicationName(), message));
      }
   }

   public void debug(Exception e)
   {
      if (debugMode > 0)
      {
         logAndStdErrMessage(e, String.format("%s:", getApplicationName()));
      }
   }

   public boolean isDebuggingOn()
   {
      return debugMode > 0;
   }

   @Override
   public void progress(int percentage)
   {
   }

   @Override
   public void message(String text)
   {
      logAndPrintMessage(text);
   }

   public String requestUserInput(String message)
     throws IOException
   {
      return javax.swing.JOptionPane.showInputDialog(null, message);
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
         else if (args[i].equals("--log"))
         {
            if (logFile != null)
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

            logFile = new File(args[i]);
         }
         else if (args[i].equals("--nolog"))
         {
            logFile = null;
         }
         else if (args[i].equals("--nomathjax"))
         {
            mathJax = false;
         }
         else if (args[i].equals("--mathjax"))
         {
            mathJax = true;
         }
         else if (args[i].equals("--noentities"))
         {
            useHtmlEntities = false;
         }
         else if (args[i].equals("--entities"))
         {
            useHtmlEntities = true;
         }
         else if (args[i].equals("--split"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_value", args[i-1]));
            }

            try
            {
               splitLevel = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.number_expected", args[i-1], args[i]), e);
            }
         }
         else if (args[i].equals("--prefix-split"))
         {
           splitUseBaseNamePrefix = true;
         }
         else if (args[i].equals("--noprefix-split"))
         {
           splitUseBaseNamePrefix = false;
         }
         else if (args[i].equals("--head"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_filename", args[i-1]));
            }

            extraHead = args[i];
         }
         else if (args[i].equals("--debug"))
         {
            debugMode = Integer.MAX_VALUE;

            if (i < args.length - 1)
            {
               try
               {
                  int val = Integer.parseInt(args[i+1]);

                  if (val >= 0)
                  {
                     debugMode = val;
                     i++;
                  }
               }
               catch (NumberFormatException e)
               {
               }
            }
         }
         else if (args[i].equals("--debug-mode"))
         {
            i++;

            if (i == args.length)
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.missing_mode", args[i-1]));
            }

            try
            {
               int val = Integer.parseInt(args[i]);

               if (val >= 0)
               {
                  debugMode = val;
               }
            }
            catch (NumberFormatException e)
            {
               try
               {
                  debugMode = TeXParser.getDebugLevelFromModeList(args[i].split(","));
               }
               catch (TeXSyntaxException e2)
               {
                  throw new InvalidSyntaxException(e2.getMessage(this), e2);
               }
            }
         }
         else if (args[i].equals("--nodebug"))
         {
            debugMode = 0;
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
            if (outDir != null)
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

            outDir = new File(args[i]);

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
         else if (args[i].equals("--no-rm-tmp-dir"))
         {
            deleteTempDirOnExit = false;
         }
         else if (args[i].equals("--rm-tmp-dir"))
         {
            deleteTempDirOnExit = true;
         }
         else if (args[i].equals("--no-convert-images"))
         {
            convertImages = false;
         }
         else if (args[i].equals("--convert-images"))
         {
            convertImages = true;
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
            else if (outDir == null)
            {
               outDir = new File(args[i]);
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
      
      if (outDir == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_out"));
      }
   }

   protected void run() throws IOException
   {
      L2HConverter listener = new L2HConverter(this, mathJax, outDir,
        outCharset, true, splitLevel)
      {
         public L2HImage toImage(String preamble,
          String content, String mimeType, TeXObject alt, String name,
          boolean crop)
         throws IOException
         {
            if (convertImages)
            {
               try
               {
                  return createImage(getParser(), preamble, content, mimeType, alt,
                    name, crop);
               }
               catch (InterruptedException e)
               {
                  throw new TeXSyntaxException(e, parser,
                    getMessage("error.interrupted"));
               }
            }
            else
            {
               return null;
            }
         }

         public Dimension getImageSize(File file, String mimetype)
         {
            try
            {
               return getImageFileDimensions(getParser(), file, mimetype);
            }
            catch (IOException | InterruptedException e)
            {
               return null;
            }
         }
      };

      listener.setSplitUseBaseNamePrefix(splitUseBaseNamePrefix);

      listener.setUseEntities(useHtmlEntities);

      if (extraHead != null)
      {
         listener.addToHead(extraHead);
      }

      TeXParser parser = new TeXParser(listener);

      logWriter = null;

      if (logFile != null)
      {
         logWriter = new PrintWriter(logFile);
         parser.setDebugMode(debugMode, logWriter);
      }

      try
      {
         parser.parse(inFile);
      }
      finally
      {
         if (deleteTempDirOnExit)
         {
            deleteTempDir();
         }

         if (logWriter != null)
         {
            logWriter.close();
            logWriter = null;
         }
      }
   }

   private void deleteTempDir() throws IOException
   {
      if (tmpDir == null) return;

      File[] files = tmpDir.listFiles();

      for (File f : files)
      {
         f.delete();
      }

      tmpDir.delete();
   }

   public L2HImage createImage(TeXParser parser, String preamble,
    String content, String mimetype, TeXObject alt, String name,
    boolean crop)
   throws IOException,InterruptedException
   {
      L2HConverter listener = (L2HConverter)parser.getListener();

      if (name == null)
      {
         nameIdx++;
         name = String.format("img%06d", nameIdx);
      }

      Charset charset = listener.getCharSet();
      L2HImage image = null;
      PrintWriter writer = null;

      try
      {
         if (tmpDir == null)
         {
            tmpDir = Files.createTempDirectory("texparserlib").toFile();
         }

         File file = new File(tmpDir, name+".tex");

         if (charset == null)
         {
            writer = new PrintWriter(file);
         }
         else
         {
            writer = new PrintWriter(file, charset.name());
         }

         writer.println("\\batchmode");
         writer.println(preamble);
         writer.println("\\begin{document}");
         writer.println(content);
         writer.println("\\end{document}");

         writer.close();
         writer = null;

         String invoker;

         if (listener.isStyLoaded("fontspec"))
         {
            invoker = "lualatex";
         }
         else
         {
            invoker = "pdflatex";
         }

         File dir = inFile.getParentFile();

         if (dir == null)
         {
            dir = new File(".");
         }

         debug(getMessageWithFallback("message.running",
           "Running {0}", String.format("%s \"%s\"", invoker, name)));
         
         ProcessBuilder pb = new ProcessBuilder(invoker, name);

         pb.directory(tmpDir);

         Map<String,String> env = pb.environment();
         env.put("TEXINPUTS", String.format("%s%c",
              dir.getAbsolutePath(), File.pathSeparatorChar));

         Process process = pb.start();
         int exitCode = process.waitFor();

         if (exitCode != 0)
         {
            throw new IOException(getMessage("error.app_failed",
              String.format("%s \"%s\"", invoker, name), exitCode));
         }

         if (mimetype == null)
         {
            mimetype = L2HConverter.MIME_TYPE_PNG;
         }

         File pdfFile = new File(tmpDir, name+".pdf");
         Path destPath;

         if (crop)
         {
            invoker = "pdfcrop";

            String croppedPdfName = name+"-crop.pdf";

            exitCode = execCommandAndWaitFor(tmpDir,
               invoker, pdfFile.getName(), croppedPdfName);

            if (exitCode == 0)
            {
               pdfFile = new File(tmpDir, croppedPdfName);
            }
            else
            {
               warning(parser, getMessage("error.app_failed",
                 String.format("%s \"%s\"", invoker,
                    pdfFile.getName(), croppedPdfName),
                 exitCode));
            }
         }

         Dimension imageDim = null;

         if (mimetype.equals(L2HConverter.MIME_TYPE_PDF))
         {
            destPath = (new File(outDir, name+".pdf")).toPath();

            Files.copy(pdfFile.toPath(), destPath);
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_PNG))
         {
            File pngFile = pdfToImage(pdfFile, name, "png");

            imageDim = getImageFileDimensions(parser, pngFile, mimetype);

            destPath = (new File(outDir, pngFile.getName())).toPath();

            Files.copy(pngFile.toPath(), destPath);
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_JPEG))
         {
            File jpegFile = pdfToImage(pdfFile, name, "jpeg");

            imageDim = getImageFileDimensions(parser, jpegFile, mimetype);

            destPath = (new File(outDir, jpegFile.getName())).toPath();

            Files.copy(jpegFile.toPath(), destPath);
         }
         else
         {
            warning(parser, getMessage("warning.unsupported.image.type",
             mimetype));

            mimetype=L2HConverter.MIME_TYPE_PDF;
            destPath = (new File(outDir, name+".pdf")).toPath();
            Files.copy(pdfFile.toPath(), destPath);
         }

         int width=0;
         int height=0;

         if (imageDim != null)
         {
            width = imageDim.width;
            height = imageDim.height;
         }

         image = new L2HImage(outDir.toPath().relativize(destPath),
          mimetype, width, height, name, alt);
      }
      finally
      {
         if (writer != null)
         {
            writer.close();
         }
      }

      return image;
   }

   public Dimension getImageFileDimensions(TeXParser parser, File file,
     String type)
     throws IOException,InterruptedException
   {
      String invoker = "file";

      boolean isPdf = L2HConverter.MIME_TYPE_PDF.equals(type);

      if (isPdf)
      {
         invoker = "pdfinfo";
      }

      String line = null;
      StringBuilder result = new StringBuilder();

      int exitCode = execCommandAndWaitFor(result, invoker, file.getAbsolutePath());

      Pattern pat = null;

      if (L2HConverter.MIME_TYPE_PNG.equals(type))
      {
         pat = PNG_INFO;
      }
      else if (L2HConverter.MIME_TYPE_JPEG.equals(type))
      {
         pat = JPEG_INFO;
      }
      else if (isPdf)
      {
         pat = PDF_INFO;
      }
      else
      {
         return null;
      }

      if (exitCode == 0)
      {
         line = result.toString();

         if (line == null || line.isEmpty())
         {
            return null;
         }

         Matcher m = pat.matcher(line);

         if (m.matches())
         {
            try
            {
               int width, height;

               if (isPdf)
               {
                  width = (int)Math.round(Float.parseFloat(m.group(1)));
                  height = (int)Math.round(Float.parseFloat(m.group(2)));
               }
               else
               {
                  width = Integer.parseInt(m.group(1));
                  height = Integer.parseInt(m.group(2));
               }

               return new Dimension(width, height);
            }
            catch (NumberFormatException e)
            {// shouldn't happen, pattern ensures format correct
               debug(e);
            }
         }
      }
      else
      {
         warning(parser, getMessage("error.app_failed",
           String.format("%s \"%s\"", invoker, file.getName()),
           exitCode));
      }

      return null;
   }

   protected File pdfToImage(File pdfFile, String basename, String format)
     throws IOException,InterruptedException
   {
      String invoker = "pdftoppm";

      int exitCode = execCommandAndWaitFor(tmpDir,
          invoker, "-singlefile", "-"+format,
          pdfFile.getAbsolutePath().toString(), basename);

      if (exitCode != 0)
      {
         throw new IOException(getMessage("error.app_failed",
           String.format("%s -singlefile -png \"%s\" \"%s\"", invoker,
             pdfFile.getAbsolutePath(), basename), exitCode));
      }

      return new File(tmpDir, basename+"."+format);
   }

   @Override
   public void substituting(TeXParser parser, String original, String replacement)    
   {  
      File file = parser.getCurrentFile();
      int lineNum = parser.getLineNumber();
      String message;
      
      if (replacement.isEmpty())
      {
         message = getMessage("warning.removing", original);
      }
      else
      {
         message = getMessage("warning.substituting",
              original, replacement);
      }

      if (file == null)
      {
         warning(message);
      }
      else if (lineNum > 0)
      {
         warning(String.format("%s:%d: %s", file.getName(),
           lineNum, message));
      }
      else
      {
         warning(String.format("%s: %s", file.getName(), message));
      }
   }

   public String cmdListToString(String... params)
   {
      StringBuilder builder = new StringBuilder();

      builder.append(params[0]);

      for (int i = 1; i < params.length; i++)
      {
         builder.append(" \"");
         builder.append(params[i]);
         builder.append("\"");
      }

      return builder.toString();
   }

   protected int execCommandAndWaitFor(String... cmd)
     throws IOException,InterruptedException
   {
      return execCommandAndWaitFor(null, null, 0, cmd);
   }

   protected int execCommandAndWaitFor(StringBuilder result, int maxLines, String... cmd)
     throws IOException,InterruptedException
   {
      return execCommandAndWaitFor(null, result, maxLines, cmd);
   }

   protected int execCommandAndWaitFor(StringBuilder result, String... cmd)
     throws IOException,InterruptedException
   {
      return execCommandAndWaitFor(null, result, Integer.MAX_VALUE, cmd);
   }

   protected int execCommandAndWaitFor(File processDir, String... cmd)
     throws IOException,InterruptedException
   {
      return execCommandAndWaitFor(processDir, null, 0, cmd);
   }

   protected int execCommandAndWaitFor(File processDir,
      StringBuilder result, int maxLines, String... cmd)
     throws IOException,InterruptedException
   {
      debug(getMessageWithFallback("message.running",
        "Running {0}", cmdListToString(cmd)));
         
      Process process = null;
      int exitCode = -1;

      try
      {
         ProcessBuilder pb = new ProcessBuilder(cmd);

         if (processDir != null)
         {
            pb.directory(processDir);
         }

         process = pb.start();
         exitCode = process.waitFor();
      }
      catch (Exception e)
      {
         debug(e);

         return exitCode;
      }

      if (result != null)
      {
         String line = null;
         int lineNum = 0;

         if (exitCode == 0)
         {
            InputStream stream = process.getInputStream();

            if (stream == null)
            {
               throw new IOException(
                getMessageWithFallback("error.cant.open.process.stream",
                "Unable to open input stream from process: {0}",
                cmdListToString(cmd)));
            }

            BufferedReader reader = null;

            try
            {
               reader = new BufferedReader(new InputStreamReader(stream));

               while (lineNum < maxLines)
               {
                  line = reader.readLine();

                  if (line == null) break;

                  lineNum++;

                  if (lineNum > 1)
                  {
                     result.append(String.format("%n"));

                     result.append(line);
                  }
               }
            }
            finally
            {
               if (reader != null)
               {
                  reader.close();
               }
            }
         }
         else if (isDebuggingOn())
         {
            logAndStdErrMessage(getMessageWithFallback("error.app_failed",
              "{0} failed with exit code {1}",
              cmd,  exitCode));
         }

         debug(getMessageWithFallback("message.process.result",
           "Processed returned: {0}", result));
      }

      return exitCode;
   }

   @Override
   public String kpsewhich(String name)
     throws IOException,InterruptedException
   {
      if (name.indexOf("\\") != -1)
      {
         throw new IOException(getMessage("error.bksl_in_kpsewhich", name));
      }

      String arg = "--var-value="+name;

      if (kpsewhichResults == null)
      {
         kpsewhichResults = new Hashtable<String,String>();
      }
      else
      {
         // has kpsewhich already been called with this argument? 

         String result = kpsewhichResults.get(arg);
         
         if (result != null)
         {
            return result;
         }
      }

      String line = null;
      StringBuilder result = new StringBuilder();

      int exitCode = execCommandAndWaitFor(result, 1, "kpsewhich", arg);

      if (result.length() > 0)
      {
         line = result.toString();
      }

      kpsewhichResults.put(arg, line);

      return line;
   }

   public File getTeXMF()
      throws IOException
   {
      if (texmf != null)
      {
         return texmf;
      }

      // Try to use kpsewhich -var-value=TEXMFHOME to find target
      // directory

      try
      {
         texmf = new File(kpsewhich("-var-value=TEXMFHOME"));
      }
      catch (InterruptedException e)
      {
         error(e);
      }

      return texmf;
   }

   @Override
   public void epstopdf(File file, File pdfFile)
     throws IOException,InterruptedException
   {
      epstopdf(file, pdfFile, "epstopdf");
   }

   public void epstopdf(File file, File pdfFile, String app)
     throws IOException,InterruptedException
   {
      int exitCode = -1;

      if (!isReadAccessAllowed(file))
      {
         throw new IOException(getMessage("message.no.read", file));
      }

      if (!isWriteAccessAllowed(pdfFile))
      {
         throw new IOException(getMessage("message.no.write", pdfFile));
      }

      String fileName = file.getAbsolutePath();

      exitCode = execCommandAndWaitFor(app,
         "--outfile="+pdfFile.getAbsolutePath(), fileName);

      if (exitCode != 0)
      {
         throw new IOException(getMessage("error.app_failed",
           String.format("%s \"%s\"", app, fileName), exitCode));
      }
   }

   @Override
   public void wmftoeps(File file, File epsFile)
     throws IOException,InterruptedException
   {
      wmftoeps(file, epsFile, "wmf2eps");
   }

   public void wmftoeps(File file, File epsFile, String app)
     throws IOException,InterruptedException
   {
      int exitCode = -1;

      if (!isReadAccessAllowed(file))
      {
         throw new IOException(getMessage("message.no.read", file));
      }

      if (!isWriteAccessAllowed(epsFile))
      {
         throw new IOException(getMessage("message.no.write", epsFile));
      }

      String fileName = file.getAbsolutePath();

      exitCode = execCommandAndWaitFor(app,
         "-o", epsFile.getAbsolutePath(),
         fileName);

      if (exitCode != 0)
      {
         throw new IOException(getMessage("error.app_failed",
           String.format("%s \"%s\"", app, fileName), exitCode));
      }
   }

   @Override
   public void convertimage(int inPage, String[] inOptions, File inFile,
     String[] outOptions, File outFile)
     throws IOException,InterruptedException
   {
      convertimage(inPage, inOptions, inFile, outOptions, outFile, "convert");
   }


   public void convertimage(int inPage, String[] inOptions, File inFile,
     String[] outOptions, File outFile, String app)
     throws IOException,InterruptedException
   {
      int exitCode = -1;

      if (!isReadAccessAllowed(inFile))
      {
         throw new IOException(getMessage("message.no.read", inFile));
      }

      if (!isWriteAccessAllowed(outFile))
      {
         throw new IOException(getMessage("message.no.write", outFile));
      }

      int numInOpts = (inOptions == null ? 0 : inOptions.length);
      int numOutOpts = (outOptions == null ? 0 : outOptions.length);

      String[] args = new String[3+numInOpts+numOutOpts];

      int idx = 0;
      args[idx++] = app;

      for (int i = 0; i < numInOpts; i++)
      {
         args[idx++] = inOptions[i];
      }

      if (inPage > 0)
      {
         args[idx++] = String.format("%s[%d]", inFile.getAbsolutePath(), inPage-1);
      }
      else
      {
         args[idx++] = inFile.getAbsolutePath();
      }

      for (int i = 0; i < numOutOpts; i++)
      {
         args[idx++] = outOptions[i];
      }

      args[idx++] = outFile.getAbsolutePath();

      exitCode = execCommandAndWaitFor(args);

      if (exitCode != 0)
      {
         throw new IOException(getMessage("error.app_failed",
           String.format("%s \"%s\" \"%s\"", app, inFile, outFile), exitCode));
      }
   }

   @Override
   public boolean isReadAccessAllowed(TeXPath path)
   {
      return isReadAccessAllowed(path.getFile());
   }

   @Override
   public boolean isReadAccessAllowed(File file)
   {
      return file.canRead();
   }

   @Override
   public boolean isWriteAccessAllowed(TeXPath path)
   {
      return isWriteAccessAllowed(path.getFile());
   }

   @Override
   public boolean isWriteAccessAllowed(File file)
   {
      if (file.exists())
      {
         return file.canWrite();
      }

      File dir = file.getParentFile();

      if (dir != null)
      {
         return dir.canWrite();
      }

      return (new File(System.getProperty("user.dir"))).canWrite();
   }

   @Override
   public Charset getDefaultCharset()
   {
      return Charset.defaultCharset();
   }

   @Override
   public void copyFile(File src, File dest)
   throws IOException
   {
      if (!isReadAccessAllowed(src))
      {
         throw new IOException(getMessage("message.no.read", src));
      }

      File destDirFile = dest.getParentFile();

      if (!destDirFile.exists())
      {
         if (!isWriteAccessAllowed(destDirFile))
         {
            throw new IOException(getMessage("message.no.write", destDirFile));
         }

          debug(String.format("mkdir %s", destDirFile));
          Files.createDirectories(destDirFile.toPath());
      }

      if (!isWriteAccessAllowed(dest))
      {
         throw new IOException(getMessage("message.no.write", dest));
      }

      debug(String.format("%s -> %s", src, dest));
      Files.copy(src.toPath(), dest.toPath());
   }

   public void copyFile(File src, String destDir, String destName)
   throws IOException
   {
      File destDirFile = new File(destDir);

      if (!destDirFile.exists())
      {
          Files.createDirectories(destDirFile.toPath());
      }

      Files.copy(src.toPath(), (new File(destDirFile, destName)).toPath());
   }

   public void copyFile(String srcDir, String srcName,
      String destDir, String destName)
   throws IOException
   {
      File destDirFile = new File(destDir);

      if (!destDirFile.exists())
      {
          Files.createDirectories(destDirFile.toPath());
      }

      Path source = FileSystems.getDefault().getPath(
         srcDir, srcName);
      Path target = FileSystems.getDefault().getPath(
         destDir, destName);

      Files.copy(source, target);
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
      System.out.println(getMessage("syntax.debug-mode", "--debug-mode"));
      System.out.println(getMessage("syntax.log", "--log"));
      System.out.println(getMessage("syntax.nolog", "--nolog"));
      System.out.println();
      System.out.println(getMessage("syntax.version", "--version", "-v"));
      System.out.println(getMessage("syntax.help", "--help", "-h"));
      System.out.println();
      System.out.println(getMessage("syntax.output.options"));
      System.out.println();
      System.out.println(getMessage("syntax.out", "--output", "-o"));
      System.out.println(getMessage("syntax.out.charset", "--out-charset"));
      System.out.println();
      System.out.println(getMessage("syntax.html.options"));
      System.out.println();
      System.out.println(getMessage("syntax.head", "--head"));
      System.out.println(getMessage("syntax.mathjax", "--[no]mathjax"));
      System.out.println(getMessage("syntax.entities", "--entities"));
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

   public void libraryVersion()
   {
      System.out.println();
      System.out.println(getMessageWithFallback("about.library.version",
        "Bundled with {0} version {1} ({2})",
        "texparserlib.jar", TeXParser.VERSION, TeXParser.VERSION_DATE));
      System.out.println("https://github.com/nlct/texparser");
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

   @Override
   public String getApplicationVersion()
   {
      return VERSION;
   }

   public static void main(String[] args)
   {
      final TeXJavaHelpMk app = new TeXJavaHelpMk();

      try
      {
         app.loadDictionary();
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

   private Hashtable<String,MessageFormat> messages;
   private Hashtable<String,String> kpsewhichResults;
   private File texmf;
   private boolean shownVersion = false;

   private boolean useHtmlEntities = true;
   private boolean mathJax = false;

   private File inFile, outDir;
   private int splitLevel=8;
   private Charset outCharset;

   private File tmpDir = null;
   private File logFile = null;
   private PrintWriter logWriter = null;

   private int debugMode = 0;

   private boolean deleteTempDirOnExit = true;
   private boolean convertImages = true;
   private boolean splitUseBaseNamePrefix = false;

   private String outputFormat = "latex";

   private int nameIdx=0;
   
   private String extraHead=null;

   public static final Pattern PNG_INFO =
    Pattern.compile(".*: PNG image data, (\\d+) x (\\d+),.*");
   public static final Pattern JPEG_INFO =
    Pattern.compile(".*: JPEG image data, .*, (\\d+)x(\\d+),.*");
   public static final Pattern PDF_INFO =
    Pattern.compile(".*Page size:\\s+(\\d*\\.?\\d+) x (\\d*\\.?\\d+) pts.*", Pattern.DOTALL);

   public static final String NAME = "texjavahelpmk";
   public static final String VERSION = "0.1a";
   public static final String DATE = "2024-03-18";
}
