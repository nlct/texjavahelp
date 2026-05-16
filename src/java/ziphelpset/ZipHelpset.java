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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import java.net.*;

import java.util.Locale;
import java.util.Vector;
import java.util.zip.*;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;
import com.dickimawbooks.texjavahelplib.AbstractCLI;
import com.dickimawbooks.texjavahelplib.CLIArgValue;
import com.dickimawbooks.texjavahelplib.HelpsetFile;
import com.dickimawbooks.texjavahelplib.Helpset;

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

      printSyntaxItem(getMessage("syntax.in", "--lib", "-i"));

      printSyntaxItem(getMessage("syntax.helpset", "--helpset"));

      printSyntaxItem(getMessage("syntax.out", "--output", "-o"));

      printSyntaxItem(getMessage("syntax.locales", "--locales", "-l"));

      printSyntaxItem(getMessage("syntax.locale-prefix", "--locale-prefix", "-p"));

      System.out.println();

      printCommonCLISyntax();

      System.out.println();

      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   @Override
   protected int getCLIArgCount(String arg)
   {
      if (arg.equals("--lib") || arg.equals("-i")
       || arg.equals("--output") || arg.equals("-o")
       || arg.equals("--helpset")
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
      if (resourcePathName == null)
      {
         resourcePathName = arg;
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
      if (isArg(arg, "--lib", "-i", returnVals))
      {
         if (resourcePathName != null)
         {
            throw new InvalidSyntaxException(
                  getMessage("error.syntax.only_one_input"));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         resourcePathName = returnVals[0].toString();
      }
      else if (isArg(arg, "--helpset", returnVals))
      {
         helpsetDirName = returnVals[0].toString();
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
      if (resourcePathName == null)
      {
         throw new InvalidSyntaxException(
           getMessage("error.syntax.missing_in"));
      }

      File inFile = new File(resourcePathName);
      inPath = inFile.toPath();

      if (!Files.exists(inPath))
      {
         throw new InvalidSyntaxException(
           getMessage("error.file_not_found", inPath));
      }

      if (!Files.isDirectory(inPath))
      {
         throw new InvalidSyntaxException(
           getMessage("error.file_not_found", inPath));
      }

      if (helpsetDirName == null)
      {
         helpsetDirName = getHelpLib().getHelpsetDirName();
      }

      if (helpsetDirName.endsWith("/"))
      {
         helpsetDirName = helpsetDirName.substring(0, helpsetDirName.length()-1);
      }

      helpsetPath = inPath.resolve(helpsetDirName);

      try
      { 
         baseUri = inPath.toUri();
         helpsetUri = helpsetPath.toUri();
      }
      catch (Exception e)
      {
         throw new InvalidSyntaxException(e.getMessage(), e);
      }

      if (zipFile == null)
      {
         zipFile = new File(inFile, helpsetDirName+".zip");
      }
   }

   protected void run() throws IOException
   {
      helpset = new Helpset(getHelpLib());

      Files.walkFileTree(helpsetPath, new SimpleFileVisitor<Path>()
       {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
             throws IOException
          {
             if (!attrs.isDirectory())
             {
                String type = Files.probeContentType(path);

                if (HelpsetFile.isSupportedType(type))
                {
                   URI uri = path.toUri();

                   URI rUri = baseUri.relativize(uri);

                   Locale locale = null;

                   if (localeNames != null)
                   {
                      for (String tag : localeNames)
                      {
                         String name = (localePrefix == null ? tag : localePrefix+tag);


                         for (int i = 0; i < path.getNameCount()-1; i++)
                         {
                            if (path.getName(i).toString().equals(name))
                            {
                               locale = Locale.forLanguageTag(tag);

                               break;
                            }
                         }
                      }
                   }

                   HelpsetFile hsFile = new HelpsetFile(getHelpLib(),
                     rUri.toString(), type, locale);

                   hsFile.setPath(path);
                   hsFile.setNameFrom(inPath.relativize(path));

                   hsFile.setEncodingFromPath();

                   helpset.add(hsFile);
                }
                else
                {
                   publishMessage(getMessage("message.skipping_unsupported",
                    path, type));
                }
             }

             return FileVisitResult.CONTINUE;
          }
       });

      helpset.writeHelpset(zipFile);
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

   Path inPath = null, helpsetPath;
   URI baseUri, helpsetUri;
   String resourcePathName = null, helpsetDirName=null;
   File zipFile = null;

   Vector<String> localeNames;
   Helpset helpset;

   String localePrefix = null;
}
