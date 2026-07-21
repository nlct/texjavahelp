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

import java.util.Locale;
import java.util.IllformedLocaleException;

import java.awt.Component;

public abstract class AbstractCLI
{
   public void initialiseHelpAndParse(String[] args)
     throws IOException,InvalidSyntaxException
   {
      try
      {
         preInitHelpProcess(args);
      }
      catch (InvalidSyntaxException e)
      {
         setExitCode(TeXJavaHelpLibAppAdapter.EXIT_SYNTAX);
         throw e;
      }

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

   /**
    * Returns long switch to set message locale or null if not supported.
    * This method returns <code>"--msg-locale"</code> but may be
    * overridden to use a different name or to drop support.
    * @return the long switch for message system locale
    */
   public String getMessageSystemLocaleCLILongSwitch()
   {
      return "--msg-locale";
   }

   /**
    * Returns short switch to set message locale or null if not supported.
    * This method returns null but may be overridden to add support.
    * @return the short switch for message system locale
    */
   public String getMessageSystemLocaleCLIShortSwitch()
   {
      return null;
   }

   /**
    * Returns long switch to set helpset locale or null if not supported.
    * This method returns <code>"--hs-locale"</code> but may be
    * overridden to use a different name or to drop support.
    * @return the long switch for helpset locale
    */
   public String getHelpSetLocaleCLILongSwitch()
   {
      return "--hs-locale";
   }

   /**
    * Returns short switch to set helpset locale or null if not supported.
    * This method returns null but may be overridden to add support.
    * @return the short switch for helpset locale
    */
   public String getHelpSetLocaleCLIShortSwitch()
   {
      return null;
   }

   /**
    * Parses command line args for message locale switch before
    * message system is initialised. It's too late to set the
    * message system locale when the CLISyntaxParser is parsing the
    * arguments.
    * @param args the command line arguments
    * @throws InvalidSyntaxException if the syntax is not valid
    */
   protected void preInitHelpProcess(String[] args)
     throws InvalidSyntaxException
   {
      String localeLongSwitch = getMessageSystemLocaleCLILongSwitch();
      String localeShortSwitch = getMessageSystemLocaleCLIShortSwitch();

      if (localeLongSwitch != null || localeShortSwitch != null)
      {
         for (int i = 0; i < args.length; i++)
         {
            if (
                 ( localeLongSwitch != null && args[i].equals(localeLongSwitch) )
               || 
                 ( localeShortSwitch != null && args[i].equals(localeShortSwitch) )
              )
            {
               String option = args[i];

               i++;

               if (i < args.length)
               {
                  String value = args[i];

                  if (value.isEmpty())
                  {
                     setMessageLocale((HelpSetLocale)null);
                  }
                  else
                  {
                     try
                     {
                        setMessageLocale(new HelpSetLocale(value));
                     }
                     catch (IllformedLocaleException e)
                     {
                        throw new InvalidSyntaxException(
                         String.format("invalid %s value %s", option, value));
                     }
                  }
               }
               else
               {
                  throw new InvalidSyntaxException(
                   String.format("missing %s value", option));
               }

               break;
            }
            else if (
                  localeLongSwitch != null && args[i].startsWith(localeLongSwitch+"=") 
               )
            {
               String[] split = args[i].split("=", 2);
               String option = split[0];
               String value = split[1];

               if (value.isEmpty())
               {
                  setMessageLocale((HelpSetLocale)null);
               }
               else
               {
                  try
                  {
                     setMessageLocale(new HelpSetLocale(value));
                  }
                  catch (IllformedLocaleException e)
                  {
                     throw new InvalidSyntaxException(
                      String.format("invalid %s value %s", option, value));
                  }
               }

               break;
            }
            else if (args[i].startsWith("-"))
            {
               i += getCLIArgCount(args[i]);
            }
         }
      }
   }

   protected int getCLIArgCount(String arg)
   {
      String msgSysLocaleLongSwitch = getMessageSystemLocaleCLILongSwitch();
      String msgSysLocaleShortSwitch = getMessageSystemLocaleCLIShortSwitch();

      String hsLocaleLongSwitch = getHelpSetLocaleCLILongSwitch();
      String hsLocaleShortSwitch = getHelpSetLocaleCLIShortSwitch();

      if (
          ( msgSysLocaleLongSwitch != null && arg.equals(msgSysLocaleLongSwitch) )
         || 
          ( msgSysLocaleShortSwitch != null && arg.equals(msgSysLocaleShortSwitch) )
         || 
          ( hsLocaleLongSwitch != null && arg.equals(hsLocaleLongSwitch) )
         || 
          ( hsLocaleShortSwitch != null && arg.equals(hsLocaleShortSwitch) )
         )
      {
         return 1;
      }

      return 0;
   }

   protected int getCLIMaxArgParams()
   {
      return 1;
   }

   protected abstract void parseNoSwitchCLIArg(String arg)
      throws InvalidSyntaxException;

   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      String msgSysLocaleLongSwitch = getMessageSystemLocaleCLILongSwitch();
      String msgSysLocaleShortSwitch = getMessageSystemLocaleCLIShortSwitch();

      String hsLocaleLongSwitch = getHelpSetLocaleCLILongSwitch();
      String hsLocaleShortSwitch = getHelpSetLocaleCLIShortSwitch();

      if (
           msgSysLocaleLongSwitch != null
            && cliParser.isArg(arg, 
                 msgSysLocaleShortSwitch, msgSysLocaleLongSwitch, returnVals)
         )
      {
         // ignore, already parsed
      }
      else if (
           hsLocaleLongSwitch != null
            && cliParser.isArg(arg, hsLocaleShortSwitch, hsLocaleLongSwitch, returnVals)
         )
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         String tag = returnVals[0].toString();

         if (tag.isEmpty())
         {
            setHelpSetLocale((HelpSetLocale)null);
         }
         else
         {
            try
            {
               setHelpSetLocale(new HelpSetLocale(tag));
            }
            catch (IllformedLocaleException e)
            {
               throw new InvalidSyntaxException(
                  getMessage("error.clisyntax.invalid.locale_tag", arg), e);
            }
         }
      }
      else
      {
         return false;
      }

      return true;
   }

   protected void preCLIProcess() throws InvalidSyntaxException
   {
   }

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

   protected boolean setCLIDebugModeOption(String option, String value)
   throws InvalidSyntaxException
   {
      Integer level = null;
      
      if (value != null)
      {
         try
         {
            level = Integer.valueOf(value);
         }
         catch (NumberFormatException e)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.invalid.int_value", option, value),
              e);
         }
      }
   
      return setCLIDebugOption(option, level);
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
      String msgSysLocaleLongSwitch = getMessageSystemLocaleCLILongSwitch();
      String msgSysLocaleShortSwitch = getMessageSystemLocaleCLIShortSwitch();

      if (msgSysLocaleLongSwitch != null)
      {
         if (msgSysLocaleShortSwitch == null)
         {
            helpLib.printSyntaxItem(getMessage("clisyntax.msg-locale",
               msgSysLocaleLongSwitch));
         }
         else
         {
            helpLib.printSyntaxItem(getMessage("clisyntax.msg-locale2",
               msgSysLocaleLongSwitch, msgSysLocaleShortSwitch));
         }
      }

      if (isGUIMode())
      {
         String hsLocaleLongSwitch = getHelpSetLocaleCLILongSwitch();
         String hsLocaleShortSwitch = getHelpSetLocaleCLIShortSwitch();

         if (hsLocaleLongSwitch != null)
         {
            if (hsLocaleShortSwitch == null)
            {
               helpLib.printSyntaxItem(getMessage("clisyntax.hs-locale",
                  hsLocaleLongSwitch));
            }
            else
            {
               helpLib.printSyntaxItem(getMessage("clisyntax.hs-locale2",
                  hsLocaleLongSwitch, hsLocaleShortSwitch));
            }
         }
      }

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
            if (arg.equals("--debug"))
            {
               return -1;
            }

            return getCLIArgCount(arg);
         }

         @Override
         protected int maxArgParams()
         {
            return getCLIMaxArgParams();
         }

         @Override
         public boolean setDebugOption(String option, Integer value)
         throws InvalidSyntaxException
         {
            return setCLIDebugOption(option, value);
         }

         @Override
         public boolean setDebugModeOption(String option, String value)
         throws InvalidSyntaxException
         {
            return setCLIDebugModeOption(option, value);
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

      preCLIProcess();

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

   public boolean isLongArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isLongArg(arg, longName, returnVals);
   }

   public boolean isLongArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isLongArg(arg, shortName, longName, returnVals);
   }

   public boolean isFloatArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isFloatArg(arg, longName, returnVals);
   }

   public boolean isFloatArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isFloatArg(arg, shortName, longName, returnVals);
   }

   public boolean isDoubleArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isDoubleArg(arg, longName, returnVals);
   }

   public boolean isDoubleArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return cliParser.isDoubleArg(arg, shortName, longName, returnVals);
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

   public boolean isErrorBufferOn()
   {
      return errorBuffer != null;
   }

   public void setErrorBuffering(boolean enable)
   {
      if (enable)
      {
         errorBuffer = new StringBuilder();
      }
      else
      {
         errorBuffer = null;
      }
   }

   public void clearErrorBuffer()
   {
      if (errorBuffer != null)
      {
         errorBuffer.setLength(0);
      }
   }

   public void bufferError(String message)
   {
      if (errorBuffer != null)
      {
         if (errorBuffer.length() >= 0)
         {
            errorBuffer.append(String.format("%n"));
         }

         errorBuffer.append(message);
      }
   }

   public String getErrorBufferContent()
   {
      return errorBuffer == null ? null : errorBuffer.toString();
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

   public void error(Component comp, String message, Throwable e)
   {
      if (helpLibApp != null)
      {
         helpLibApp.error(comp, message, e);
      }
      else
      {
         message("error", message, e);
         setExitCode(getExitCode(e, false));
      }
   }

   public void error(Component comp, String message)
   {
      if (helpLibApp != null)
      {
         helpLibApp.error(comp, message);
      }
      else
      {
         message("error", message, null);
      }
   }

   public void internalError(Component owner, String message, Throwable e)
   {
      if (helpLibApp != null)
      {
         helpLibApp.internalError(owner, message, e);
      }
      else
      {
         message("internal error", message, e);
         setExitCode(getExitCode(e, false));
      }
   }

   public void fatalError(String message, Throwable e, int exitCode)
   {  
      if (helpLibApp != null)
      {
         fatalError(message, e, exitCode);
      }
      else
      {
         message("fatal error", message, e);
         System.exit(exitCode);
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

   public void warning(Component comp, String message)
   {
      if (helpLibApp != null)
      {
         helpLibApp.warning(comp, message);
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

          @Override
          public void error(String message)
          {
             if (isErrorBufferOn())
             {
                bufferError(message);
             }
             else
             {
                error(null, message, null);
             }
          }
       };
   }

   public void setMessageLocale(Locale locale)
   {
      setMessageLocale(locale == null ? null : new HelpSetLocale(locale));
   }

   public void setMessageLocale(HelpSetLocale locale)
   {
      if (helpLib != null)
      {
         throw new IllegalArgumentException(getMessageWithFallback(
            "error.too_late_to_set_locale",
            "Too late to set message locale {0} (message system already initialised)",
            locale));
      }

      msgLocale = locale;
   }

   public void setHelpSetLocale(Locale locale)
   {
      setHelpSetLocale(locale == null ? null : new HelpSetLocale(locale));
   }

   public void setHelpSetLocale(HelpSetLocale locale)
   {
      hsLocale = locale;

      if (helpLib != null)
      {
         helpLib.setHelpSetLocale(locale);
      }
   }

   protected TeXJavaHelpLib createHelpLib() throws IOException
   {
      if (msgLocale == null)
      {
         msgLocale = new HelpSetLocale(Locale.getDefault());
      }

      if (hsLocale == null)
      {
         hsLocale = msgLocale;
      }

      helpLib = new TeXJavaHelpLib(helpLibApp, msgLocale, hsLocale);

      helpLibApp.setHelpLib(helpLib);

      loadDictionaries(helpLib.getMessageSystem());

      return helpLib;
   }

   protected abstract void loadDictionaries(MessageSystem msgSys)
     throws IOException;

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
   protected StringBuilder errorBuffer;
   protected TeXJavaHelpLib helpLib;
   protected TeXJavaHelpLibAppAdapter helpLibApp;
   protected CLISyntaxParser cliParser;
   protected HelpSetLocale msgLocale, hsLocale;
}

