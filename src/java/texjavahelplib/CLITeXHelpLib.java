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

import java.io.IOException;

import java.net.URL;

import com.dickimawbooks.texparserlib.TeXParser;
import com.dickimawbooks.texparserlib.TeXSyntaxException;

public class CLITeXHelpLib extends AbstractCLI
{
   public CLITeXHelpLib(CLITeXAppAdapter cliTeXApp)
   {
      this.cliTeXApp = cliTeXApp;
   }

   @Override
   protected void loadDictionaries(MessageSystem msgSys)
     throws IOException
   {
      cliTeXApp.loadDictionaries(msgSys);
   }

   @Override
   public String getCLIApplicationName()
   {
      return cliTeXApp.getCLIApplicationName();
   }

   @Override
   public String getCLIApplicationVersion()
   {
      return cliTeXApp.getCLIApplicationVersion();
   }

   @Override
   public String getCLIApplicationVersionDate()
   {
      return cliTeXApp.getCLIApplicationVersionDate();
   }

   @Override
   public void printCLISyntax()
   {
      cliTeXApp.printCLISyntax();
   }

   @Override
   public void printCLIAbout()
   {
      cliTeXApp.printCLIAbout();
   }

   @Override
   public void postVersionInfo()
   {
      cliTeXApp.postVersionInfo();
   }

   @Override
   protected int getCLIArgCount(String arg)
   {
      return cliTeXApp.getCLIArgCount(arg);
   }

   @Override
   protected void parseNoSwitchCLIArg(String arg)
      throws InvalidSyntaxException
   {
      cliTeXApp.parseNoSwitchCLIArg(arg);
   }

   @Override
   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      return cliTeXApp.parseCLIArg(arg, returnVals);
   }

   @Override
   protected void postCLIProcess()
     throws InvalidSyntaxException
   {
      cliTeXApp.postCLIProcess();
   }

   @Override
   protected String getShortHelpSwitch()
   {
      return cliTeXApp.getShortHelpSwitch();
   }

   @Override
   protected String getShortVersionSwitch()
   {
      return cliTeXApp.getShortVersionSwitch();
   }

   @Override
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
            try
            {
               level = Integer.valueOf(
                 TeXParser.getDebugLevelFromModeList(value.trim().split("\\s*,\\s*")));      
            }
            catch (TeXSyntaxException e2)
            {   
               throw new InvalidSyntaxException(e2.getMessage(cliTeXApp), e2);
            }
         }  
      }     
   
      return setCLIDebugOption(option, level);
   }

   @Override
   protected TeXJavaHelpLib createHelpLib() throws IOException
   {
      TeXJavaHelpLib h = super.createHelpLib();

      URL url = TeXParser.getLanguageResourceUrl(h.getMessagesLocale().getLocale());

      if (url == null)
      {
         h.warning(h.getMessageWithFallback(
            "error.no_texjavaparserlib_lang_support",
            "No language support file found for texjavaparserlib"));
      }
      else
      {
         h.getMessageSystem().loadDictionary(url);
      }

      return h;
   }

   @Override
   public int getExitCode(Throwable e, boolean isFatal)
   {
      if (e instanceof TeXSyntaxException)
      {
         return TeXJavaHelpLibAppAdapter.EXIT_TEX_PARSER;
      }
      else
      {
         return super.getExitCode(e, isFatal);
      }
   }

   @Override
   public boolean isGUIMode()
   {       
      return cliTeXApp.isGUI();
   } 

   CLITeXAppAdapter cliTeXApp;
}
