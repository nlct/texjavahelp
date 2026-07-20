/*
    Copyright (C) 2026 Nicola L.C. Talbot
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

import java.io.*;

import java.net.URL;

import java.nio.file.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.text.MessageFormat;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.awt.Component;
import java.awt.Dimension;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.html.L2HConverter;

public abstract class CLITeXAppAdapter extends TeXAppAdapter
{
   public CLITeXAppAdapter()
   {
      this(true);
   }

   public CLITeXAppAdapter(boolean mayRequireTmpDir)
   {
      this.mayRequireTmpDir = mayRequireTmpDir;
   }

   protected void loadDictionaries(MessageSystem msgSys)
     throws IOException
   {
      msgSys.loadDictionary(
         "/com/dickimawbooks/texparserlib/dictionaries/",
         "texjavaparserlib");
   }

   public void initialiseHelpAndParse(String[] args)
     throws IOException,InvalidSyntaxException
   {
      cliTeXHelpLib = new CLITeXHelpLib(this);
      cliTeXHelpLib.initialiseHelpAndParse(args);
   }

   protected String getShortHelpSwitch() { return "-h"; }

   protected String getShortVersionSwitch() { return "-v"; }

   public abstract String getCLIApplicationName();

   @Override
   public String getApplicationName()
   {
      return getCLIApplicationName();
   }

   public abstract String getCLIApplicationVersion();

   @Override
   public String getApplicationVersion()
   {
      return getCLIApplicationVersion();
   }

   public abstract String getCLIApplicationVersionDate();

   public abstract void printCLISyntax();

   public abstract void printCLIAbout();

   public String getCharsetShortSwitch() { return "-c"; }

   protected int getCLIArgCount(String arg)
   {
      if (arg.equals("--debug-mode")
       || arg.equals("--log")
       || arg.equals("--charset") || arg.equals(getCharsetShortSwitch())
         )
      {
         return 1;
      }

      return 0;
   }

   protected abstract void parseNoSwitchCLIArg(String arg)
      throws InvalidSyntaxException;

   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = getSyntaxParser();

      if (mayRequireTmpDir && arg.equals("--no-rm-tmp-dir"))
      {
         deleteTempDirOnExit = false;
      }
      else if (mayRequireTmpDir && arg.equals("--rm-tmp-dir"))
      {
         deleteTempDirOnExit = true;
      }
      else if (arg.equals("--nolog"))
      {
         logFile = null;
      }
      else if (cliParser.isArg(arg, "--log", returnVals))
      {
         if (logFile != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one", arg));
         }
      
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         } 
      
         logFile = new File(returnVals[0].toString());
      }
      else if (cliParser.isArg("--charset", getCharsetShortSwitch(), returnVals))
      {
         defaultCharset = Charset.forName(returnVals[0].toString());
      }
      else
      {
         return false;
      }

      return true;
   }

   protected abstract void postCLIProcess()
     throws InvalidSyntaxException;

   public void postVersionInfo()
   {
   }

   public void versionInfo()
   {
      cliTeXHelpLib.versionInfo();
   }

   public void printSyntaxItem(String msg)
   {
      cliTeXHelpLib.printSyntaxItem(msg);
   }

   public void printCommonCLISyntax()
   {
      cliTeXHelpLib.printCommonCLISyntax();

      printSyntaxItem(getMessage("texparser.syntax.debug-mode", "--debug-mode"));
      
      System.out.println();
      
      printSyntaxItem(getMessage("clisyntax.log", "--log"));
      printSyntaxItem(getMessage("clisyntax.nolog", "--nolog"));
      printSyntaxItem(getMessage("clisyntax.charset", "--charset", getCharsetShortSwitch()));

      if (mayRequireTmpDir)
      {
         printSyntaxItem(getMessage("clisyntax.rmtmpdir", "--[no-]rm-tmp-dir"));
      }
   }

   public URL getHelpLibResource(File file)
   {
      return getClass().getResource(
           getHelpLib().getResourcePath()+"/"+file.getName());
   }

   public TeXJavaHelpLibAppAdapter getHelpLibApp()
   {
      return cliTeXHelpLib.getHelpLibApp();
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return cliTeXHelpLib.getHelpLib();
   }

   public TeXApp getTeXApp()
   {
      return this;
   }

   public boolean isGUI()
   {
      return false;
   }

   public int getExitCode()
   {
      return cliTeXHelpLib == null ? exitCode : cliTeXHelpLib.getExitCode();
   }

   public void setExitCode(int code)
   {
      if (cliTeXHelpLib != null)
      {
         cliTeXHelpLib.setExitCode(code);
      }

      exitCode = code;
   }

   public int getExitCode(Throwable e, boolean isFatal)
   {
      if (cliTeXHelpLib != null)
      {
         return cliTeXHelpLib.getExitCode(e, isFatal);
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

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      if (cliTeXHelpLib == null)
      {
         MessageFormat fmt = new MessageFormat(fallbackFormat);

         return fmt.format(params);
      }

      return cliTeXHelpLib.getMessageWithFallback(label, fallbackFormat, params);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      if (cliTeXHelpLib == null) return null;

      return cliTeXHelpLib.getMessageIfExists(label, args);
   }

   @Override
   public String getMessage(String label, Object... params)
   {
      if (cliTeXHelpLib == null)
      {// message system hasn't been initialised

         String param = (params.length == 0 ? "" : params[0].toString());

         for (int i = 1; i < params.length; i++)
         {
            param += ","+params[0].toString();
         }

         return String.format("%s[%s]", label, param);
      }

      return cliTeXHelpLib.getMessage(label, params);
   }

   public String getChoiceMessage(String label, int argIdx,
      String choiceLabel, int numChoices, Object... args)
   {
      return getHelpLib().getChoiceMessage(label, argIdx, choiceLabel, numChoices, args);
   }

   @Override
   public void warning(TeXParser parser, String message)
   {
      if (parser == null)
      {
         getHelpLibApp().warning(message);
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
      if (cliTeXHelpLib != null)
      {
         cliTeXHelpLib.error(msg, e);
      }
      else
      {
         logAndStdErrMessage(e, msg);
         setExitCode(getExitCode(e, false));
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
      if (cliTeXHelpLib.isDebuggingModeOn()
       || cliTeXHelpLib.isMinimumVerbosity(level))
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

   protected void openLogWriter(TeXParser parser, Charset logCharset)
      throws IOException
   {
      logWriter = null;

      if (logFile != null)
      {  
         logWriter = new PrintWriter(
           createBufferedWriter(logFile.toPath(),
             logCharset == null ? defaultCharset : logCharset));
         
         if (parser != null)
         {
            parser.setDebugMode(cliTeXHelpLib.getDebuggingLevel(), logWriter);
         }
      } 
   }

   protected void closeLogWriter() throws IOException
   {
      if (logWriter != null)
      {
         logWriter.close();
         logWriter = null;
      }
   }

   public boolean isDebuggingOn()
   {
      return cliTeXHelpLib.isDebuggingModeOn();
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
      else
      {
         try
         {
            BufferedImage image = ImageIO.read(file);

            if (image != null)
            {
               return new Dimension(image.getWidth(), image.getHeight());
            }
         }
         catch (Throwable e)
         {
            getHelpLib().debug(e);
         }
      }

      String line = null;
      StringBuilder result = new StringBuilder();

      int exitCode = getHelpLib().execCommandAndWaitFor(result,
       MAX_PROCESS_TIME,
       invoker, file.getAbsolutePath());

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
               getHelpLib().debug(e);
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

   protected File createTempFile(String name, boolean usePrefix)
     throws IOException
   {
      if (tmpDir == null)
      {
         tmpDir = Files.createTempDirectory(getApplicationName()).toFile();
      }

      if (usePrefix)
      {
         name = tmpDir.getName() + name;
      }

      return new File(tmpDir, name);
   }

   protected void deleteTempDir() throws IOException
   {  
      if (tmpDir == null) return;
      
      File[] files = tmpDir.listFiles();
      
      for (File f : files)
      {  
         f.delete();
      }
      
      tmpDir.delete();
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

   public void libraryVersion()
   {
      libraryVersion(true);
   }

   public void libraryVersion(boolean showHelpLibVersion)
   {
      if (showHelpLibVersion)
      {
         System.out.println();
         System.out.println(getMessageWithFallback("about.library.version",
           "{0} version {1} ({2})",
           "texjavahelplib.jar",
           TeXJavaHelpLib.VERSION, TeXJavaHelpLib.VERSION_DATE));

         System.out.println("https://github.com/nlct/texjavahelp");
      }

      System.out.println();
      System.out.println(getMessageWithFallback("about.library.version",
        "{0} version {1} ({2})",
        "texparserlib.jar", TeXParser.VERSION, TeXParser.VERSION_DATE));
      System.out.println("https://github.com/nlct/texparser");
   }

   public CLISyntaxParser getSyntaxParser()
   {
      return cliTeXHelpLib == null ? null : cliTeXHelpLib.getSyntaxParser();
   }

   public CLITeXHelpLib getCLITeXHelpLib()
   {
      return cliTeXHelpLib;
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
         cliTeXHelpLib.warning(message);
      }
      else if (lineNum > 0)
      {
         cliTeXHelpLib.warning(String.format("%s:%d: %s", file.getName(),
           lineNum, message));
      }
      else
      {
         cliTeXHelpLib.warning(String.format("%s: %s", file.getName(), message));
      }
   }

   @Override
   public String kpsewhich(String arg)
     throws IOException,InterruptedException
   {
      return getHelpLib().kpsewhich(arg, MAX_PROCESS_TIME);
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

      exitCode = getHelpLib().execCommandAndWaitFor(MAX_PROCESS_TIME, app,
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

      exitCode = getHelpLib().execCommandAndWaitFor(MAX_PROCESS_TIME, app,
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

      exitCode = getHelpLib().execCommandAndWaitFor(MAX_PROCESS_TIME, args);

      if (exitCode != 0)
      {
         throw new IOException(getMessage("error.app_failed",
           String.format("%s \"%s\" \"%s\"", app, inFile, outFile), exitCode));
      }
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

          cliTeXHelpLib.debug(String.format("mkdir %s", destDirFile));
          Files.createDirectories(destDirFile.toPath());
      }

      if (!isWriteAccessAllowed(dest))
      {
         throw new IOException(getMessage("message.no.write", dest));
      }

      cliTeXHelpLib.debug(String.format("%s -> %s", src, dest));

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

   CLITeXHelpLib cliTeXHelpLib;

   int exitCode = 0;

   protected File logFile = null;
   protected PrintWriter logWriter = null;
   protected boolean deleteTempDirOnExit = true;
   protected boolean mayRequireTmpDir;
   protected File tmpDir;

   public static final long MAX_PROCESS_TIME = 360000L; // millisecs

   protected File texmf;

   public static final Pattern PNG_INFO =
    Pattern.compile(".*: PNG image data, (\\d+) x (\\d+),.*");
   public static final Pattern JPEG_INFO =
    Pattern.compile(".*: JPEG image data, .*, (\\d+)x(\\d+),.*");
   public static final Pattern PDF_INFO =
    Pattern.compile(".*Page size:\\s+(\\d*\\.?\\d+) x (\\d*\\.?\\d+) pts.*", Pattern.DOTALL);

}
