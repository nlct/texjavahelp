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

import java.util.ArrayDeque;

// adapted from com.dickimawbooks.bib2gls.common.BibGlsTeXApp

public abstract class CLISyntaxParser
{
   public CLISyntaxParser(TeXJavaHelpLib helpLib, String[] args)
   {
      this(helpLib, args, null, null);
   }

   public CLISyntaxParser(TeXJavaHelpLib helpLib, String[] args,
    String shortHelpSwitch, String shortVersionSwitch)
   {
      this.helpLib = helpLib;
      deque = new ArrayDeque<String>(args.length);
      originalArgList = args;
      this.shortHelp = shortHelpSwitch;
      this.shortVersion = shortVersionSwitch;
   }

   public void process()
   throws InvalidSyntaxException
   {
      preparse();
      parseArgs();
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   /**
    * Gets the number of required arguments for a command line
    * switch. This should return the number or -1 for a single
    * optional argument (which should not start with "-").
    * @param arg switch
    * @return number of required arguments or -1 for a single
    * optional argument
    */
   protected abstract int argCount(String arg);

   /**
    * Gets the maximum number of arguments that any command line
    * switch will allow.
    */
   protected int maxArgParams()
   {
      return 1;
   }

   protected abstract void help();
   protected abstract void version();

   /**
    * Parse command line word that doesn't starts with "-" if recognised.
    * @throws InvalidSyntaxException if the argument is invalid
    */
   protected abstract void parseArg(String arg)
    throws InvalidSyntaxException;

   /**
    * Parse command line argument that starts with "-" if recognised.
    * @param arg option name including leading hyphen(s)
    * @param returnVals scratch variable for storing the
    * argument values (by methods such as isArg(), isIntArg() or isListArg())
    * @return true if recognised switch
    * @throws InvalidSyntaxException if the argument is a valid
    * switch but has been used incorrectly
    */
   protected abstract boolean parseArg(String arg, CLIArgValue[] returnVals)
    throws InvalidSyntaxException;

   /**
    * For use after processing, returns true if any arguments were
    * found.
    */
   public boolean optionsWereFound()
   {
      return argsFound;
   }

   /**
    * This method is called in preprocessor if the argument is
    * "--debug". Default implementation returns false.
    * Override this method to enable the switch. This option needs
    * to be processed first to ensure debugging is available for
    * other option actions. Designed for switch that may optionally
    * be followed by an integer.
    * @param option the option name ("--debug")
    * @param value the option value or null if argCount returns 0
    * or if the argument count is 1 but the value isn't an integer
    * @return true if argument is supported
    */
   public boolean setDebugOption(String option, Integer value)
   throws InvalidSyntaxException
   {
      return false;
   }

   /**
    * This method is called in preprocessor if the argument is
    * "--debug-mode". Default implementation returns false.
    * Override this method and ensure that argCount() returns 1
    * to enable the switch. This option needs
    * to be processed first to ensure debugging is available for
    * other option actions. Designed for switch that must be
    * followed by a string identifier.
    * @param option the option name ("--debug-mode")
    * @param value the option value (not null)
    * @return true if argument is supported
    */
   public boolean setDebugModeOption(String option, String value)
   throws InvalidSyntaxException
   {
      return false;
   }

   /**
    * May be used for special cases that need to be picked up before
    * the main processing. The preparseIndex may be incremented if
    * the special case has an argument.
    * This method may add items to the deque, in which case it
    * should return true to prevent duplication.
    * @return true if argument should not be added to deque
    */
   protected boolean preparseCheckArg()
   throws InvalidSyntaxException
   {
      if (originalArgList[preparseIndex].equals("--help")
       || originalArgList[preparseIndex].equals(shortHelp))
      {
         help();

         // help() will typically exit but in case it doesn't
         // (and to keep compiler happy):
         return true;
      }
      else if (originalArgList[preparseIndex].equals("--version")
            || originalArgList[preparseIndex].equals(shortVersion))
      {
         version();

         // version() will typically exit but in case it doesn't:
         return true;
      }
      else if (originalArgList[preparseIndex].equals("--debug"))
      {
         String option = originalArgList[preparseIndex];
         Integer value = null;

         if (argCount(option) == 1)
         {
            if (preparseIndex < originalArgList.length - 1)
            {
               try
               {
                  value = Integer.valueOf(originalArgList[preparseIndex+1]);
                  preparseIndex++;
               }
               catch (NumberFormatException e)
               {
                  value = null;
               }
            }
         }

         return setDebugOption(option, value);
      }
      else if (originalArgList[preparseIndex].startsWith("--debug="))
      {
         String[] split = originalArgList[preparseIndex].split("=", 2);
         String option = split[0];
         Integer value = null;

         if (argCount(option) == 1)
         {
            try
            {
               value = Integer.valueOf(split[1]);
               preparseIndex++;
            }
            catch (NumberFormatException e)
            {
               value = null;
            }
         }

         return setDebugOption(option, value);
      }
      else if (originalArgList[preparseIndex].equals("--debug-mode"))
      {
         // similar to the above by the argument should be a string

         String option = originalArgList[preparseIndex];

         if (argCount(option) == 1)
         {
            if (preparseIndex < originalArgList.length - 1)
            {
               preparseIndex++;

               return setDebugModeOption(option, originalArgList[preparseIndex]);
            }
            else
            {
               throw new InvalidSyntaxException(helpLib.getMessage(
                 "error.clisyntax.missing.value", option));
            }
         }
      }
      else if (originalArgList[preparseIndex].startsWith("--debug-mode="))
      {
         String[] split = originalArgList[preparseIndex].split("=", 2);
         String option = split[0];

         if (argCount(option) == 1)
         {
            return setDebugModeOption(option, split[1]);
         }
      }

      return false;
   }

   public void preparse()
   throws InvalidSyntaxException
   {
      for (preparseIndex = 0;
           preparseIndex < originalArgList.length;
           preparseIndex++)
      {
         if (!preparseCheckArg())
         {
            deque.add(originalArgList[preparseIndex]);
   
            if (originalArgList[preparseIndex].startsWith("-")
             && originalArgList[preparseIndex].length() > 1)
            {
               String[] split = originalArgList[preparseIndex].split("=", 2);

               int n = argCount(split[0]);

               if (n == -1)
               {
                  if (preparseIndex + 1 < originalArgList.length
                  && !originalArgList[preparseIndex+1].startsWith("-"))
                  {
                     n = 1;
                  }
                  else
                  {
                     n = 0;
                  }
               }

               if (split.length == 2)
               {
                  n--;
               }

               for (int j = 0; j < n; j++)
               {
                  preparseIndex++;
   
                  if (preparseIndex < originalArgList.length)
                  {
                     deque.add(originalArgList[preparseIndex]);
                  }
               }
            }
         }
      }

   }

   /**
    * Parse the arguments. Note that the queue must first be created
    * with preparse().
    */
   public void parseArgs()
     throws InvalidSyntaxException
   {
      String arg;
      CLIArgValue[] returnVals = new CLIArgValue[maxArgParams()];

      while ((arg = deque.poll()) != null)
      {
         if (arg.startsWith("-"))
         {
            // reset

            for (int i = 0; i < returnVals.length; i++)
            {
               returnVals[i] = null;
            }

            if (!parseArg(arg, returnVals))
            {
               throw new InvalidSyntaxException(
                  helpLib.getMessage("error.clisyntax.unknown.arg",
                  arg, "--help"));
            }
         }
         else
         {
            parseArg(arg);
         }
      }
   }

   protected boolean isArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return isArg(arg, null, longName, returnVals, CLIArgValueType.STRING);
   }

   protected boolean isArg(String arg,
     String longName, CLIArgValue[] returnVals,
     CLIArgValueType type)
    throws InvalidSyntaxException
   {
      return isArg(arg, null, longName, returnVals, type);
   }

   protected boolean isArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return isArg(arg, shortName, longName, returnVals,
         CLIArgValueType.STRING);
   }

   protected boolean isIntArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return isArg(arg, null, longName, returnVals,
         CLIArgValueType.INT);
   }

   protected boolean isIntArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return isArg(arg, shortName, longName, returnVals,
         CLIArgValueType.INT);
   }

   protected boolean isListArg(String arg,
     String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return isArg(arg, null, longName, returnVals,
         CLIArgValueType.LIST);
   }

   protected boolean isListArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals)
    throws InvalidSyntaxException
   {
      return isArg(arg, shortName, longName, returnVals,
         CLIArgValueType.LIST);
   }

   protected boolean isArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals,
     CLIArgValueType type)
    throws InvalidSyntaxException
   {
      return isArg(arg, shortName, longName, null, returnVals, type);
   }

   protected boolean isArg(String arg,
     String shortName, String longName, String altLongName,
     CLIArgValue[] returnVals,
     CLIArgValueType type)
    throws InvalidSyntaxException
   {
      String[] split = arg.split("=", 2);
      String argName = split[0];

      int n = 0;

      if (argName.equals(longName) || argName.equals(altLongName))
      {
         n = argCount(argName);

         if (n == 0)
         {
            returnVals[0] = null;
         }
         else if (split.length == 1)
         {
            if (n == -1)
            {
               String val = deque.peekFirst();

               if (val != null && !val.startsWith("-"))
               {
                  returnVals[0] = CLIArgValue.create(helpLib, argName, deque.poll(), type);
               }
               else
               {
                  returnVals[0] = null;
               }
            }
            else
            {
               returnVals[0] = CLIArgValue.create(helpLib, argName, deque.poll(), type);
            }
         }
         else
         {
            returnVals[0] = CLIArgValue.create(helpLib, argName, split[1], type);
         }
      }
      else if (shortName != null && arg.equals(shortName))
      {
         argName = shortName;

         n = argCount(shortName);

         if (n == 0)
         {
            returnVals[0] = null;
         }
         else if (n == -1)
         {
            String val = deque.peekFirst();

            if (val != null && !val.startsWith("-"))
            {
               returnVals[0] = CLIArgValue.create(helpLib, argName, deque.poll(), type);
            }
            else
            {
               returnVals[0] = null;
            }
         }
         else
         {
            returnVals[0] = CLIArgValue.create(helpLib, argName, deque.poll(), type);
         }
      }
      else
      {
         return false;
      }

      for (int i = 1; i < n; i++)
      {
         returnVals[i] = CLIArgValue.create(helpLib, argName, deque.poll(), type);
      }

      argsFound = true;

      return true;
   }

   protected boolean isIntArg(String arg,
     String longName, CLIArgValue[] returnVals, int defValue)
    throws InvalidSyntaxException
   {
      return isIntArg(arg, null, longName, returnVals, defValue);
   }

   /**
    * Test for switch that takes a single optional integer argument.
    */ 
   protected boolean isIntArg(String arg,
     String shortName, String longName, CLIArgValue[] returnVals,
     int defValue)
    throws InvalidSyntaxException
   {
      String[] split = arg.split("=", 2);

      if (split[0].equals(longName))
      {
         if (split.length == 1)
         {
            String val = deque.peekFirst();

            if (val == null)
            {
               returnVals[0] = null;
            }
            else
            {
               try
               {
                  int i = Integer.parseInt(val);
                  returnVals[0] = new CLIArgValue(deque.poll(), i);
                  argsFound = true;
               }
               catch (NumberFormatException e)
               {
                  returnVals[0] = null;
               }
            }
         }
         else
         {
            argsFound = true;

            returnVals[0] = CLIArgValue.create(helpLib, split[0], split[1], 
              CLIArgValueType.INT);
         }
      }
      else if (shortName != null && arg.equals(shortName))
      {
         String val = deque.peekFirst();

         if (val == null)
         {
            returnVals[0] = null;
         }
         else
         {
            try
            {
               int i = Integer.parseInt(val);
               returnVals[0] = new CLIArgValue(deque.poll(), i);
               argsFound = true;
            }
            catch (NumberFormatException e)
            {
               returnVals[0] = null;
            }
         }
      }
      else
      {
         return false;
      }

      return true;
   }

   private TeXJavaHelpLib helpLib;
   protected ArrayDeque<String> deque;
   protected String[] originalArgList;
   protected int preparseIndex;
   protected String shortHelp = null;
   protected String shortVersion = null;
   protected boolean argsFound = false;
}
