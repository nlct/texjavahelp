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

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.text.MessageFormat;

public abstract class AbstractCLI
{
   public void initialiseHelpAndParse(String[] args)
     throws IOException,InvalidSyntaxException
   {
      initHelpLibrary();

      try
      {
         parseArgs(args);
      }
      catch (InvalidSyntaxException e)
      {
         setExitCode(TeXJavaHelpLibAppAdapter.EXIT_SYNTAX);
         throw e;
      }
   }

   public abstract String getCLIApplicationName();

   public abstract String getCLIApplicationVersion();

   public abstract void printCLISyntax();

   public abstract String getCLIApplicationVersionDate();

   public String getCLIVersionLine()
   {
      String date = getCLIApplicationVersionDate();

      if (date == null)
      {
          return getMessageWithFallback("about.version",
           "{0} version {1}", getCLIApplicationName(),
           getCLIApplicationVersion());
      }
      else
      {
          return getMessageWithFallback("about.version_date",
           "{0} version {1} ({2})", getCLIApplicationName(),
           getCLIApplicationVersion(), date);
      }
   }

   public abstract void printCLIAbout();

   protected abstract int getCLIArgCount(String arg);

   protected abstract void parseNoSwitchCLIArg(String arg)
      throws InvalidSyntaxException;

   protected abstract boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException;

   protected abstract void postCLIProcess()
     throws InvalidSyntaxException;

   protected String getShortHelpSwitch() { return "-h"; }
   protected String getShortVersionSwitch() { return "-v"; }

   protected boolean setCLIDebugOption(String option, Integer value)
         throws InvalidSyntaxException
   {
      setDebuggingLevel(value == null ? 1 : value.intValue());

      return true;
   }

   public void printSyntaxItem(String msg)
   {
      helpLib.printSyntaxItem(msg);
   }

   public URL getHelpLibResource(File file)
   {
      return getClass().getResource(
           helpLib.getResourcePath()+"/"+file.getName());
   }

   public void printCommonCLISyntax()
   {
      String shortVersion = getShortVersionSwitch();

      if (shortVersion == null)
      {
         helpLib.printSyntaxItem(getMessage("clisyntax.version", "--version"));
      }
      else
      {
         helpLib.printSyntaxItem(getMessage("clisyntax.version2", "--version", 
            shortVersion));
      }

      String shortHelp = getShortHelpSwitch();

      if (shortHelp == null)
      {
         helpLib.printSyntaxItem(getMessage("clisyntax.help", "--help"));
      }
      else
      {
         helpLib.printSyntaxItem(getMessage("clisyntax.help2", "--help", shortHelp));
      }

      helpLib.printSyntaxItem(getMessage("clisyntax.verbose", "--[no]verbose"));
      helpLib.printSyntaxItem(getMessage("clisyntax.debug", "--[no]debug"));

   }

   protected CLISyntaxParser createCLISyntaxParser(String[] args)
   {
      return new CLISyntaxParser(helpLib, args, getShortHelpSwitch(), getShortVersionSwitch())
      {        
         @Override
         protected int argCount(String arg)
         {
            return getCLIArgCount(arg);
         }

         @Override
         public boolean setDebugOption(String option, Integer value)
         throws InvalidSyntaxException
         {
            return setCLIDebugOption(option, value);
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
            printCLISyntax();
            System.exit(0);
         }

         @Override
         protected void version()
         {
            printCLIAbout();
            System.exit(0);
         }

         @Override
         protected void parseArg(String arg)
         throws InvalidSyntaxException
         {
            parseNoSwitchCLIArg(arg);
         }

         @Override
         protected boolean parseArg(String arg, CLIArgValue[] returnVals)
         throws InvalidSyntaxException
         {
            return parseCLIArg(arg, returnVals);
         }
      };
   }

   protected void parseArgs(String[] args) throws InvalidSyntaxException
   {
      cliParser = createCLISyntaxParser(args);

      cliParser.process();

      postCLIProcess();
   }

   public boolean isArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isArg(arg, longName, returnVals);
   }

   public boolean isArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isArg(arg, shortName, longName, returnVals);
   }

   public boolean isIntArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isIntArg(arg, longName, returnVals);
   }

   public boolean isIntArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isIntArg(arg, shortName, longName, returnVals);
   }

   public boolean isIntArg(String arg,
     String longName, CLIArgValue[] returnVals, int defValue)
    throws InvalidSyntaxException
   {
      return cliParser.isIntArg(arg, longName, returnVals, defValue);
   }

   public boolean isIntArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals,
     int defValue)
    throws InvalidSyntaxException
   {
      return cliParser.isIntArg(arg, shortName, longName, returnVals, defValue);
   }

   public boolean isListArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isListArg(arg, longName, returnVals);
   }

   public boolean isListArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isListArg(arg, shortName, longName, returnVals);
   }

   public void postVersionInfo()
   {
   }

   public void versionInfo()
   {
      if (!shownVersion)
      {
         System.out.println(getCLIVersionLine());
         postVersionInfo();
         shownVersion = true;
      }
   }

   public boolean isGUIMode()
   {
      return false;
   }

   public boolean isDebuggingModeOn()
   {
      return debugMode != 0;
   }

   public int getDebuggingLevel()
   {
      return debugMode;
   }

   public void setDebuggingLevel(int level)
   {
      debugMode = level;
   }

   public boolean isMinimumVerbosity(int level)
   {
      return verboseLevel >= level;
   }

   public boolean isVerboseModeOn()
   {
      return verboseLevel > 0;
   }

   public int getVerboseLevel()
   {
      return verboseLevel;
   }

   public void setVerboseLevel(int level)
   {
      verboseLevel = level;
   }

   public void publishMessage(String msg)
   {
      if (isVerboseModeOn())
      {
         helpLibApp.stdOutMessage(msg);
      }
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   public TeXJavaHelpLibAppAdapter getHelpLibApp()
   {
      return helpLibApp;
   }

   public CLISyntaxParser getSyntaxParser()
   {
      return cliParser;
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

   public void warning(String message)
   {
      if (helpLibApp != null)
      {
         helpLibApp.warning(message);
      }
      else
      {
         message("warning", message, null);
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
         System.err.format("%s: %s: %s%n", getCLIApplicationName(), msgTag, message);

         if (isDebuggingModeOn())
         {
            e.printStackTrace();
         }
      }
      else if (msgTag.contains("error"))
      {
         System.err.format("%s: %s: %s%n", getCLIApplicationName(), msgTag, message);
      }
      else
      {
         System.out.format("%s: %s: %s%n", getCLIApplicationName(), msgTag, message);
      }
   }

   public void debug(String message)
   {
      debug(message, null);
   }

   public void debug(String message, Throwable e)
   {
      if (isDebuggingModeOn())
      {
         error(message, e);
      }
   }

   protected TeXJavaHelpLibAppAdapter createHelpLibraryApp()
   {
      return new TeXJavaHelpLibAppAdapter()
       {
          @Override
          public boolean isGUI() { return isGUIMode(); }

          @Override
          public String getApplicationName()
          {
             return getCLIApplicationName();
          }

          @Override
          public boolean isDebuggingOn()
          {
             return isDebuggingModeOn();
          }

          @Override
          public void message(String msg)
          {
             publishMessage(msg);
          }
       };
   }

   protected TeXJavaHelpLib createHelpLib() throws IOException
   {
      helpLib = new TeXJavaHelpLib(helpLibApp);

      helpLibApp.setHelpLib(helpLib);

      return helpLib;
   }

   protected void initHelpLibrary() throws IOException
   {
      helpLibApp = createHelpLibraryApp();

      helpLib = createHelpLib();

      if (exitCode != 0)
      {
         helpLibApp.setExitCode(exitCode);
      }
   }

   protected int debugMode = 0;
   protected boolean shownVersion = false;
   protected int verboseLevel = 0;
   protected int exitCode = 0;
   protected TeXJavaHelpLib helpLib;
   protected TeXJavaHelpLibAppAdapter helpLibApp;
   protected CLISyntaxParser cliParser;
}

