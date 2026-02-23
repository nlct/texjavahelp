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

import java.nio.file.*;
import java.nio.charset.Charset;

import java.text.MessageFormat;

import java.awt.Component;

import com.dickimawbooks.texparserlib.*;

public abstract class CLITeXAppAdapter extends TeXAppAdapter
{
   protected void initHelpLibrary() throws IOException
   {
      helpLibApp = new CLITeXJavaHelpLibAdapter(this);

      helpLib = new TeXJavaHelpLib(helpLibApp);
      helpLibApp.setHelpLib(helpLib);
      helpLib.getMessageSystem().loadDictionary("texparserlib");
   }

   public TeXJavaHelpLibAppAdapter getHelpLibApp()
   {
      return helpLibApp;
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
            parser.setDebugMode(debugMode, logWriter);
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

   public abstract void cliSyntax();
   public abstract void cliVersion();

   public void libraryVersion()
   {
      libraryVersion(false);
   }

   public void libraryVersion(boolean showHelpLibVersion)
   {
      if (showHelpLibVersion)
      {
         System.out.println();
         System.out.println(getMessageWithFallback("about.library.version",
           "Bundled with {0} version {1} ({2})",
           "texjavahelplib.jar",
           TeXJavaHelpLib.VERSION, TeXJavaHelpLib.VERSION_DATE));

         System.out.println("https://github.com/nlct/texjavahelp");
      }

      System.out.println();
      System.out.println(getMessageWithFallback("about.library.version",
        "Bundled with {0} version {1} ({2})",
        "texparserlib.jar", TeXParser.VERSION, TeXParser.VERSION_DATE));
      System.out.println("https://github.com/nlct/texparser");
   }

   protected abstract void parseNoSwitchArg(String arg)
     throws InvalidSyntaxException;

   protected abstract int cliArgCount(String arg);

   protected abstract boolean parseCliArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException;

   protected abstract void postParseArgs()
     throws InvalidSyntaxException;

   protected void parseArgs(String[] args)
     throws InvalidSyntaxException
   {
      parseArgs(args, "-h", "-v");
   }

   public CLISyntaxParser getSyntaxParser()
   {
      return cliParser;
   }

   protected void parseArgs(String[] args,
      String shortHelpSwitch, String shortVersionSwitch)
     throws InvalidSyntaxException
   {
      cliParser = new CLITeXAppSyntaxParser(this, args,
         shortHelpSwitch, shortVersionSwitch);

      cliParser.process();

      postParseArgs();
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

   @Override
   public String kpsewhich(String arg)
     throws IOException,InterruptedException
   {
      return helpLib.kpsewhich(arg, MAX_PROCESS_TIME);
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

      exitCode = helpLib.execCommandAndWaitFor(MAX_PROCESS_TIME, app,
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

      exitCode = helpLib.execCommandAndWaitFor(MAX_PROCESS_TIME, app,
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

      exitCode = helpLib.execCommandAndWaitFor(MAX_PROCESS_TIME, args);

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

   CLITeXJavaHelpLibAdapter helpLibApp;
   TeXJavaHelpLib helpLib;

   int exitCode = 0;
   int debugMode = 0;
   protected int verboseLevel = 0;

   protected File logFile = null;
   protected PrintWriter logWriter = null;

   public static final long MAX_PROCESS_TIME = 360000L; // millisecs

   protected File texmf;
   protected Charset defaultCharset = Charset.defaultCharset();

   CLISyntaxParser cliParser;
}

class CLITeXJavaHelpLibAdapter extends TeXJavaHelpLibAppAdapter
{
   CLITeXJavaHelpLibAdapter(CLITeXAppAdapter cliTeXApp)
   {
      this.cliTeXApp = cliTeXApp;
   }

   @Override
   public String getApplicationName()
   {
      return cliTeXApp.getApplicationName();
   }

   @Override
   public boolean isGUI() { return false; }

   @Override
   public boolean isDebuggingOn()
   {
      return cliTeXApp.debugMode > 0;
   }

   @Override
   public void stdOutMessage(String text)
   {
      cliTeXApp.logAndPrintMessage(text);
   }

   @Override
   public void stdErrMessage(Throwable e, String msg)
   {
      cliTeXApp.logAndStdErrMessage(e, msg);
   }

   @Override
   public void error(Component owner, String msg, Throwable e)
   {
      if (msg == null && e instanceof TeXSyntaxException)
      {
         msg = ((TeXSyntaxException)e).getMessage(cliTeXApp.getTeXApp());
      }

      super.error(owner, msg, e);
   }

   @Override
   public int getExitCode(Throwable e, boolean isFatal)
   {
      if (e instanceof TeXSyntaxException)
      {
         return EXIT_TEX_PARSER;
      }
      else
      {
         return super.getExitCode(e, isFatal);
      }
   }

   CLITeXAppAdapter cliTeXApp;
}

class CLITeXAppSyntaxParser extends CLISyntaxParser
{
   CLITeXAppSyntaxParser(CLITeXAppAdapter cliTeXApp, String[] args,
      String shortHelpSwitch, String shortVersionSwitch)
   {
      super(cliTeXApp.getHelpLib(), args, shortHelpSwitch, shortVersionSwitch);
      this.cliTeXApp = cliTeXApp;
   }

   @Override
   public boolean setDebugOption(String option, Integer value)
   throws InvalidSyntaxException
   {
      cliTeXApp.debugMode = value.intValue();

      return true;
   }

   @Override
   public boolean setDebugModeOption(String option, String strValue)
   throws InvalidSyntaxException
   {  
      cliTeXApp.setCLIDebugModeOption(option, strValue);
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
         cliTeXApp.verboseLevel = 1;
      }
      else if (originalArgList[preparseIndex].equals("--noverbose"))
      {
         cliTeXApp.verboseLevel = 0;
      }
      else if (originalArgList[preparseIndex].equals("--nodebug")
             || originalArgList[preparseIndex].equals("--no-debug")
              )
      {
         cliTeXApp.debugMode = 0;
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
      cliTeXApp.cliSyntax();
      System.exit(0);
   }

   @Override
   protected void version()
   {
      cliTeXApp.cliVersion();
      System.exit(0);
   }

   protected void parseArg(String arg)
   throws InvalidSyntaxException
   {
      cliTeXApp.parseNoSwitchArg(arg);
   }

   @Override
   protected int argCount(String arg)
   {
      return cliTeXApp.cliArgCount(arg);
   }

   @Override
   protected boolean parseArg(String arg, CLIArgValue[] returnVals)
   throws InvalidSyntaxException
   {
      return cliTeXApp.parseCliArg(arg, returnVals);
   }

   CLITeXAppAdapter cliTeXApp;
}
