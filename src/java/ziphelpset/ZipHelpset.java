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
package com.dickimawbooks.ziphelpset;

import java.io.*;

import java.net.URL;

import java.util.Vector;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibAppAdapter;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;
import com.dickimawbooks.texjavahelplib.AbstractCLI;
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;

public class ZipHelpset extends AbstractCLI
{
   public ZipHelpset()
   {
   }

   @Override
   public void printCLIAbout()
   {
      System.out.println(getHelpLib().getAboutInfo(false,
        TeXJavaHelpLib.VERSION,
        getCopyrightDate(),
        String.format(
         "Copyright (C) %s Nicola L. C. Talbot (%s)",
          TeXJavaHelpLib.VERSION_DATE.substring(0, 4),
          getHelpLib().getInfoUrl(false, "www.dickimaw-books.com")),
         TeXJavaHelpLib.LICENSE_GPL3,
         true, null
      ));
   }

   public String getCopyrightStartYear()
   {
      return "2026";
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
   public String getCLIApplicationName()
   {
      return NAME;
   }

   @Override
   public String getCLIApplicationVersion()
   {
      return TeXJavaHelpLib.VERSION;
   }

   @Override
   public String getCLIApplicationVersionDate()
   {
      return TeXJavaHelpLib.VERSION_DATE;
   }

   @Override
   public void printCLISyntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("clisyntax.usage",
        getMessage("syntax.options", getCLIApplicationName())));

      System.out.println();

      printSyntaxItem(getMessage("syntax.in", "--in", "-i"));

      printSyntaxItem(getMessage("syntax.prop", "--prop"));

      printSyntaxItem(getMessage("syntax.out", "--output", "-o"));

      printSyntaxItem(getMessage("syntax.out.charset", "--out-charset"));
      printSyntaxItem(getMessage("syntax.provide-xml",
         "--provide-xml", getCLIApplicationName()));

      printSyntaxItem(getMessage("syntax.copy-overwrite-xml",
          "--copy-overwrite-xml", getCLIApplicationName()));

      printSyntaxItem(getMessage("syntax.encapless-field", "--encapless-field"));

      printSyntaxItem(getMessage("syntax.auto-trim", "--[no]auto-trim"));

      System.out.println();

      printCommonCLISyntax();

      System.out.println();

      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   @Override
   protected int getCLIArgCount(String arg)
   {
      if (arg.equals("--in") || arg.equals("-i")
       || arg.equals("--output") || arg.equals("-o")
       || arg.equals("--locales") || arg.equals("-l")
       || arg.equals("--locale-prefix") || arg.equals("-p")
         )
      {
         return 1;
      }

      return 0;
   }

   @Override
   protected void parseNoSwitchCLIArg(String arg) throws InvalidSyntaxException
   {
      if (inDir == null)
      {
         inDir = new File(arg);
      }
      else if (zipFile == null)
      {
         zipFile = new File(arg);
      }
      else
      {
         throw new InvalidSyntaxException(
               getMessage("error.syntax.only_one_inout"));
      }
   }

   @Override
   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      if (isArg(arg, "--in", "-i", returnVals))
      {
         if (inDir != null)
         {
            throw new InvalidSyntaxException(
                  getMessage("error.syntax.only_one_input"));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         inDir = new File(returnVals[0].toString());
      }
      else if (isArg(arg, "--locales", "-l", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         if (localeNames == null)
         {
            localeNames = new Vector<String>();
         }

         String[] list = returnVals[0].listValue();

         for (String name : list)
         {
            localeNames.add(name);
         }
      }
      else if (isArg(arg, "--locale-prefix", "-p", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         localePrefix = returnVals[0].toString();
      }
      else if (isArg(arg, "--output", "-o", returnVals))
      {
         if (zipFile != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.syntax.only_one_output"));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         zipFile = new File(returnVals[0].toString());
      }
      else
      {
         return false;
      }

      return true;
   }

   @Override
   protected void postCLIProcess() throws InvalidSyntaxException
   {
      if (inDir == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_in"));
      }
      
      if (zipFile == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_out"));
      }
   }

   protected void run() throws IOException
   {
   }

   public static void main(String[] args)
   {
      final ZipHelpset ziphelpset = new ZipHelpset();

      try
      {
         ziphelpset.initialiseHelpAndParse(args);
         ziphelpset.run();
      }
      catch (InvalidSyntaxException e)
      {
         ziphelpset.error(e.getMessage(), null);
      }
      catch (Throwable e)
      {
         ziphelpset.error(null, e);
      }

      System.exit(ziphelpset.getExitCode());
   }

   public static final String NAME="tjhziphelpset";

   File inDir = null, zipFile = null;

   Vector<String> localeNames;

   String localePrefix = "";
}
