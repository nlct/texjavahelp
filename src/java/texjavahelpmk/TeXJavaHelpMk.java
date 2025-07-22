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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.MessageFormat;

import java.io.*;
import java.net.URL;

import java.nio.file.*;
import java.nio.charset.Charset;

import java.awt.Dimension;
import java.awt.Component;
import javax.swing.ImageIcon;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.LaTeXSty;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.color.ColorSty;
import com.dickimawbooks.texparserlib.html.*;
import com.dickimawbooks.texparserlib.latex2latex.LaTeXPreambleListener;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibAppAdapter;
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;

public class TeXJavaHelpMk extends TeXAppAdapter
{
   protected void initHelpLibrary() throws IOException
   {
      helpLibApp = new TeXJavaHelpLibAppAdapter()
       {
          @Override
          public String getApplicationName()
          {
             return NAME;
          }

          @Override
          public boolean isGUI() { return false; }

          @Override
          public boolean isDebuggingOn()
          {
             return debugMode > 0;
          }

          @Override
          public void stdOutMessage(String text)
          {
             logAndPrintMessage(text);
          }

          @Override
          public void stdErrMessage(Throwable e, String msg)
          {
             logAndStdErrMessage(e, msg);
          }

          @Override   
          public void error(Component owner, String msg, Throwable e)
          {  
             if (msg == null && e instanceof TeXSyntaxException)
             {
                msg = ((TeXSyntaxException)e).getMessage(getTeXApp());
             }

             super.error(owner, msg, e);
          }
       };
      helpLib = new TeXJavaHelpLib(helpLibApp);
      helpLibApp.setHelpLib(helpLib);
      helpLib.getMessageSystem().loadDictionary("texparserlib");
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   public TeXApp getTeXApp()
   {
      return this;
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

   @Override
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

   public String getChoiceMessage(String label, int argIdx,
      String choiceLabel, int numChoices, Object... args)
   {
      return helpLib.getChoiceMessage(label, argIdx, choiceLabel, numChoices, args);
   }

   @Override
   public void warning(TeXParser parser, String message)
   {     
      if (parser == null)
      {
         helpLibApp.warning(message);
      }
      else
      {
         File file = parser.getCurrentFile();
         int lineNum = parser.getLineNumber();

         if (file != null && lineNum > 0)
         {  
            message = String.format("%s:%d: %s%n", file.getName(), lineNum, message);
         }  

         logAndStdErrMessage(message);
      }
   }

   @Override
   public void error(Exception e)
   {
      error(null, e);
   }

   public void error(String msg, Throwable e)
   {
      if (helpLibApp != null)
      {
         helpLibApp.error(msg, e);
      }
      else
      {
         logAndStdErrMessage(e, msg);
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
      logAndPrintMessage(message, 1);
   }

   public void logAndPrintMessage(String message, int level)
   {
      if (debugMode > 0 || verboseLevel >= level)
      {
         System.out.println(message);
      }

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

   public boolean isDebuggingOn()
   {
      return debugMode > 0;
   }

   @Override
   public void message(String text)
   {
      logAndPrintMessage(text);
   }

   @Override
   public String requestUserInput(String message)
     throws IOException
   {
      return javax.swing.JOptionPane.showInputDialog(null, message);
   }

   private void parseArgs(String[] args) throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = new CLISyntaxParser(helpLib, args, "-h", "-v")
      {
         @Override
         public boolean setDebugOption(String option, Integer value)
         throws InvalidSyntaxException
         {
            debugMode = value.intValue();

            return true;
         }

         @Override
         public boolean setDebugModeOption(String option, String strValue)
         throws InvalidSyntaxException
         {
            setCLIDebugModeOption(option, strValue);
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
               debugMode = 0;
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

         protected void parseArg(String arg)
         throws InvalidSyntaxException
         {
            // if no option specified, assume --in or --out

            if (inFile == null)
            {
               inFile = new File(arg);
            }
            else if (outDir == null)
            {
               outDir = new File(arg);
            }
            else
            {
               throw new InvalidSyntaxException(
                 getMessage("error.syntax.only_one_inout"));
            }
         }

         @Override
         protected int argCount(String arg)
         {
            if (arg.equals("--log")
             || arg.equals("--split")
             || arg.equals("--head")
             || arg.equals("--in") || arg.equals("-i")
             || arg.equals("--output") || arg.equals("-o")
             || arg.equals("--out-charset")
             || arg.equals("--image-preamble")
             || arg.equals("--debug")
             || arg.equals("--debug-mode")
               )
            {
               return 1;
            }

            return 0;
         }

         @Override
         protected boolean parseArg(String arg, CLIArgValue[] returnVals)
         throws InvalidSyntaxException
         {
            if (arg.equals("--nolog"))
            {
               logFile = null;
            }
            else if (arg.equals("--nomathjax"))
            {
               mathJax = false;
            }
            else if (arg.equals("--mathjax"))
            {
               mathJax = true;
            }
            else if (arg.equals("--noentities"))
            {
               useHtmlEntities = false;
            }
            else if (arg.equals("--entities"))
            {
               useHtmlEntities = true;
            }
            else if (arg.equals("--prefix-split"))
            {
              splitUseBaseNamePrefix = true;
            }
            else if (arg.equals("--noprefix-split"))
            {
              splitUseBaseNamePrefix = false;
            }
            else if (arg.equals("--no-rm-tmp-dir"))
            {
               deleteTempDirOnExit = false;
            }
            else if (arg.equals("--rm-tmp-dir"))
            {
               deleteTempDirOnExit = true;
            }
            else if (arg.equals("--no-convert-images"))
            {
               convertImages = false;
            }
            else if (arg.equals("--convert-images"))
            {
               convertImages = true;
            }
            else if (isArg(arg, "--log", returnVals))
            {
               if (logFile != null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.only_one", arg));
               }

               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               logFile = new File(returnVals[0].toString());
            }
            else if (isIntArg(arg, "--split", returnVals))
            {
               splitLevel = returnVals[0].intValue();
            }
            else if (isArg(arg, "--head", returnVals))
            {
               extraHead = returnVals[0].toString();
            }
            else if (isArg(arg, "--in", "-i", returnVals))
            {
               if (inFile != null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.only_one_input"));
               }

               inFile = new File(returnVals[0].toString());
            }
            else if (isArg(arg, "--output", "-o", returnVals))
            {
               if (outDir != null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.only_one", arg));
               }

               outDir = new File(returnVals[0].toString());
            }
            else if (isArg(arg, "--out-charset", returnVals))
            {
               outCharset = Charset.forName(returnVals[0].toString());
            }
            else if (isArg(arg, "--image-preamble", returnVals))
            {
               imagePreambleFile = new File(returnVals[0].toString());
            }
            else
            {
               return false;
            }

            return true;
         }
      };

      cliParser.process();

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

   protected void setCLIDebugModeOption(String option, String strValue)
   throws InvalidSyntaxException
   {
      try
      {
         int val = Integer.parseInt(strValue);

         if (val >= 0)
         {
            debugMode = val;
         }
      }
      catch (NumberFormatException e)
      {
         try
         {
            debugMode = TeXParser.getDebugLevelFromModeList(strValue.split(","));
         }
         catch (TeXSyntaxException e2)
         {
            throw new InvalidSyntaxException(e2.getMessage(this), e2);
         }
      }
   }

   public boolean isConvertImagesOn()
   {
      return convertImages;
   }

   public int getSplitLevel()
   {
      return splitLevel;
   }

   public boolean isSplitBaseNamePrefixOn()
   {
      return splitUseBaseNamePrefix;
   }

   public boolean isUseHtmlEntitiesOn()
   {
      return useHtmlEntities;
   }

   public boolean isUseMathJaxOn()
   {
      return mathJax;
   }

   public boolean isParsePackagesOn()
   {
      return true;
   }

   public String getExtraHeadCode()
   {
      return extraHead;
   }

   public File getOutDirectory()
   {
      return outDir;
   }

   protected void run() throws IOException
   {
      TJHListener listener = new TJHListener(this, outCharset);

      TeXParser parser = new TeXParser(listener);

      logWriter = null;

      if (logFile != null)
      {
         logWriter = new PrintWriter(
           createBufferedWriter(logFile.toPath(), 
             outCharset == null ? defaultCharset : outCharset));

         parser.setDebugMode(debugMode, logWriter);
      }

      try
      {
         if (convertImages)
         {
            if (imagePreambleFile != null)
            {
               imagePreamble = getFileContents(imagePreambleFile);
            }
            else
            {
               imagePreamble = parsePreamble(inFile);
            }
         }

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

   public String getImagePreamble()
   {
      return imagePreamble;
   }

   public L2HImage createImage(TeXParser parser, String preamble,
    String content, String mimetype, TeXObject alt, String name,
    boolean crop, Path relPath)
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
            tmpDir = Files.createTempDirectory(NAME).toFile();
         }

         File file = new File(tmpDir, tmpDir.getName()+name+".tex");

         if (charset == null)
         {
            writer = new PrintWriter(
              createBufferedWriter(file.toPath(), defaultCharset));
         }
         else
         {
            writer = new PrintWriter(
              createBufferedWriter(file.toPath(), charset));
         }

         writer.println("\\batchmode");
         writer.println(preamble);

         String[] grpaths = listener.getGraphicsPaths();

         if (grpaths != null)
         {
            writer.print("\\graphicspath{");

            Path basePath = inFile.getAbsoluteFile().getParentFile().toPath();
            String basePathStr = basePath.toString();

            if (File.separatorChar != '/')
            {
               basePathStr = basePathStr.replaceAll("/", File.separator);
            }

            if (!basePathStr.endsWith("/"))
            {
               basePathStr += "/";
            }

            for (int i = 0; i < grpaths.length; i++)
            {
               writer.format("{%s%s}", basePathStr, grpaths[i]);
            }

            writer.println("}");
         }

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

         if (isDebuggingOn())
         {
            helpLibApp.debug(getMessageWithFallback("message.running",
              "Running {0}",
               String.format("%s -jobname \"%s\" \"%s\"", invoker, name, file.getName())));
         }
         
         ProcessBuilder pb = new ProcessBuilder(invoker, "-jobname", name, file.getName());

         pb.directory(tmpDir);

         Map<String,String> env = pb.environment();
         env.put("TEXINPUTS", String.format("%s%c",
              dir.getAbsolutePath(), File.pathSeparatorChar));

         Process process = pb.start();
         int exitCode = process.waitFor();

         if (exitCode != 0)
         {
            throw new IOException(getMessage("error.app_failed",
              String.format("%s -jobname \"%s\" \"%s\"", invoker, name, file.getName()),
              exitCode));
         }

         if (mimetype == null)
         {
            mimetype = L2HConverter.MIME_TYPE_PNG;
         }

         File pdfFile = new File(tmpDir, name+".pdf");
         File destFile;

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

         Path outPath = outDir.toPath();

         if (relPath != null)
         {
            outPath = outPath.resolve(relPath);
         }

         if (mimetype.equals(L2HConverter.MIME_TYPE_PDF))
         {
            destFile = (new File(outPath.toFile(), name+".pdf"));
            copyFile(pdfFile, destFile);
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_PNG))
         {
            File pngFile = pdfToImage(pdfFile, name, "png");

            imageDim = getImageFileDimensions(parser, pngFile, mimetype);

            destFile = new File(outPath.toFile(), pngFile.getName());
            copyFile(pngFile, destFile);
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_JPEG))
         {
            File jpegFile = pdfToImage(pdfFile, name, "jpeg");

            imageDim = getImageFileDimensions(parser, jpegFile, mimetype);

            destFile = new File(outPath.toFile(), jpegFile.getName());
            copyFile(jpegFile, destFile);
         }
         else
         {
            warning(parser, getMessage("warning.unsupported.image.type",
             mimetype));

            mimetype=L2HConverter.MIME_TYPE_PDF;

            destFile = new File(outPath.toFile(), name+".pdf");
            copyFile(pdfFile, destFile);
         }

         int width=0;
         int height=0;

         if (imageDim != null)
         {
            width = imageDim.width;
            height = imageDim.height;
         }

         image = new L2HImage(outDir.toPath().relativize(destFile.toPath()),
          mimetype, width, height, name, alt, true);
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
               helpLibApp.debug(e);
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
         helpLibApp.warning(message);
      }
      else if (lineNum > 0)
      {
         helpLibApp.warning(String.format("%s:%d: %s", file.getName(),
           lineNum, message));
      }
      else
      {
         helpLibApp.warning(String.format("%s: %s", file.getName(), message));
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

   protected int execCommandAndWaitFor(boolean warnOnNonZeroExit,
      StringBuilder result, int maxLines, String... cmd)
     throws IOException,InterruptedException
   {
      return execCommandAndWaitFor(null, warnOnNonZeroExit, result, maxLines, cmd);
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
      return execCommandAndWaitFor(processDir, true, result, maxLines, cmd);
   }

   protected int execCommandAndWaitFor(File processDir,
      boolean warnOnNonZeroExit,
      StringBuilder result, int maxLines, String... cmd)
     throws IOException,InterruptedException
   {
      if (isDebuggingOn())
      {
         helpLibApp.debug(getMessageWithFallback("message.running",
           "Running {0}", cmdListToString(cmd)));
      }
         
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
         helpLibApp.debug(e);

         return exitCode;
      }

      if (exitCode != 0 && warnOnNonZeroExit)
      {
         helpLibApp.warning(getMessageWithFallback("error.app_failed",
           "{0} failed with exit code {1}",
           cmdListToString(cmd),  exitCode));
      }

      if (result != null)
      {
         String line = null;
         int lineNum = 0;

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
               }

               result.append(line);
            }
         }
         finally
         {
            if (reader != null)
            {
               reader.close();
            }
         }

         helpLibApp.debug(getMessageWithFallback("message.process.result",
           "Processed returned: {0}", result));
      }

      return exitCode;
   }

   @Override
   public String kpsewhich(String arg)
     throws IOException,InterruptedException
   {
      if (arg.indexOf("\\") != -1)
      {
         throw new IOException(getMessage("error.bksl_in_kpsewhich", arg));
      }

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

      execCommandAndWaitFor(false, result, 1, "kpsewhich", arg);

      if (result.length() > 0)
      {
         line = result.toString();

         kpsewhichResults.put(arg, line);
      }

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
      convertimage(inPage, inOptions, inFile, outOptions, outFile, "magick");
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
   public Charset getDefaultCharset()
   {
      return defaultCharset;
   }

   public String getFileContents(File file)
   throws IOException
   {
      BufferedReader in = null;
      StringWriter writer = new StringWriter();

      try
      {
         in = createBufferedReader(file.toPath(), getDefaultCharset());

         int c = -1;

         while ((c = in.read()) != -1)
         {
            writer.write(c);
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      return writer.toString();
   }

   protected String parsePreamble(File file)
   throws IOException
   {
      StringWriter strWriter = new StringWriter();

      LaTeXPreambleListener preambleListener
         = new LaTeXPreambleListener(this, strWriter);

      preambleListener.enableReplaceJobname(true);

      TeXParser p = new TeXParser(preambleListener);

      preambleListener.putControlSequence(new L2LLoadResources());

      p.parse(file);

      TeXPath inPath = new TeXPath(p, file.getAbsoluteFile());

      strWriter.write(String.format("\\texparserimgfile{%s}",
         inPath.getTeXPath(true)));

      return strWriter.toString();
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

          helpLibApp.debug(String.format("mkdir %s", destDirFile));
          Files.createDirectories(destDirFile.toPath());
      }

      if (!isWriteAccessAllowed(dest))
      {
         throw new IOException(getMessage("message.no.write", dest));
      }

      helpLibApp.debug(String.format("%s -> %s", src, dest));

      Files.copy(src.toPath(), dest.toPath(),
         StandardCopyOption.REPLACE_EXISTING);
   }

   public void copyFile(File src, String destDir, String destName)
   throws IOException
   {
      File destDirFile = new File(destDir);

      if (!destDirFile.exists())
      {
          Files.createDirectories(destDirFile.toPath());
      }

      Files.copy(src.toPath(), (new File(destDirFile, destName)).toPath(),
         StandardCopyOption.REPLACE_EXISTING);
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

      Files.copy(source, target,
         StandardCopyOption.REPLACE_EXISTING);
   }


   public void syntax()
   {
      versionInfo();
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
      System.out.println(getMessage("syntax.rmtmpdir", "--[no-]rm-tmp-dir"));
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
      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   public void versionInfo()
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
      System.out.println(getMessageWithFallback("about.license", "License"));
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

   @Override
   public String getApplicationName()
   {
      return NAME;
   }

   @Override
   public String getApplicationVersion()
   {
      return TeXJavaHelpLib.VERSION;
   }

   public static void main(String[] args)
   {
      final TeXJavaHelpMk app = new TeXJavaHelpMk();

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

   private Hashtable<String,String> kpsewhichResults;
   private File texmf;
   private boolean shownVersion = false;

   private boolean useHtmlEntities = false;
   private boolean mathJax = false;

   private File inFile, outDir;
   private int splitLevel=8;
   private Charset outCharset;
   private Charset defaultCharset = Charset.defaultCharset();

   private File tmpDir = null;
   private File logFile = null;
   private PrintWriter logWriter = null;
   private TeXJavaHelpLib helpLib;
   private TeXJavaHelpLibAppAdapter helpLibApp;

   private int debugMode = 0;
   private int verboseLevel = 0;

   private boolean deleteTempDirOnExit = true;
   private boolean convertImages = true;
   private boolean splitUseBaseNamePrefix = false;

   private String outputFormat = "latex";

   private int nameIdx=0;
   
   private String extraHead=null;

   private File imagePreambleFile = null;
   private String imagePreamble = null;

   public static final Pattern PNG_INFO =
    Pattern.compile(".*: PNG image data, (\\d+) x (\\d+),.*");
   public static final Pattern JPEG_INFO =
    Pattern.compile(".*: JPEG image data, .*, (\\d+)x(\\d+),.*");
   public static final Pattern PDF_INFO =
    Pattern.compile(".*Page size:\\s+(\\d*\\.?\\d+) x (\\d*\\.?\\d+) pts.*", Pattern.DOTALL);

   public static final String NAME = "texjavahelpmk";
}
