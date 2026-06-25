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
package com.dickimawbooks.tjhziphelpset;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import java.net.*;

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.*;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;
import com.dickimawbooks.texjavahelplib.AbstractCLI;
import com.dickimawbooks.texjavahelplib.CLIArgValue;
import com.dickimawbooks.texjavahelplib.HelpsetFile;
import com.dickimawbooks.texjavahelplib.Helpset;
import com.dickimawbooks.texjavahelplib.HelpSetLocale;
import com.dickimawbooks.texjavahelplib.MessageSystem;

public class ZipHelpset extends AbstractCLI
{
   public ZipHelpset()
   {
   }

   @Override
   protected void loadDictionaries(MessageSystem msgSys) throws IOException
   {
      msgSys.loadDictionary(
         "/com/dickimawbooks/texparserlib/dictionaries/", "texjavaparserlib");

      msgSys.loadDictionary(
         "/com/dickimawbooks/tjhziphelpset/dictionaries/", "tjhziphelpset");
   }

   @Override
   public void printCLIAbout()
   {
      System.out.println(getHelpLib().getAboutInfo(false,
        TeXJavaHelpLib.VERSION,
        TeXJavaHelpLib.VERSION_DATE,
        String.format(
         "Copyright (C) %s Nicola L. C. Talbot (%s)",
          getCopyrightDate(),
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

      printSyntaxItem(getMessage("syntax.in", "--in-dir", "-i"));

      printSyntaxItem(getMessage("syntax.helpset", "--helpset",
         getHelpLib().getHelpsetDirName()));

      printSyntaxItem(getMessage("syntax.out", "--output", "-o"));

      printSyntaxItem(getMessage("syntax.locales", "--locales", "-l"));

      printSyntaxItem(getMessage("syntax.locale-prefix", "--locale-prefix", "-p"));

      printSyntaxItem(getMessage("syntax.license-file", "--license-file", "-L"));

      System.out.println();
      System.out.println(getMessage("clisyntax.other.options"));
      System.out.println();

      printCommonCLISyntax();

      System.out.println();

      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   @Override
   protected int getCLIArgCount(String arg)
   {
      if (arg.equals("--in-dir") || arg.equals("-i")
       || arg.equals("--output") || arg.equals("-o")
       || arg.equals("--helpset")
       || arg.equals("--locales") || arg.equals("-l")
       || arg.equals("--locale-prefix") || arg.equals("-p")
       )
      {
         return 1;
      }

      if (arg.equals("--license-file") || arg.equals("-L"))
      {
         return 2;
      }

      return 0;
   }

   @Override
   protected int getCLIMaxArgParams()
   {     
      return 2;
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
      if (isArg(arg, "--in-dir", "-i", returnVals))
      {
         if (resourcePathName != null)
         {
            throw new InvalidSyntaxException(
                  getMessage("error.syntax.only_one_input"));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         resourcePathName = returnVals[0].toString();
      }
      else if (isArg(arg, "--helpset", returnVals))
      {
         helpsetDirName = returnVals[0].toString();
      }
      else if (isListArg(arg, "--locales", "-l", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
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
               getMessage("error.clisyntax.missing_value", arg));
         }

         localePrefix = returnVals[0].toString();
      }
      else if (isArg(arg, "--license-file", "-L", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.syntax.missing_file_after", arg));
         }

         if (returnVals[1] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.syntax.missing_locale_after", arg, returnVals[0]));
         }

         File file = new File(returnVals[0].toString());
         String localeTag = returnVals[1].toString();

         if (!file.exists())
         {
            throw new InvalidSyntaxException(
               getMessage("error.file_not_found", file));
         }

         String ref;

         try
         {
            ref = file.toURI().toString();
         }
         catch (SecurityException e)
         {
            throw new InvalidSyntaxException(
               getMessage("message.no.read", file), e);
         }

         int idx = ref.lastIndexOf('/');

         if (idx > -1)
         {
            ref = ref.substring(idx+1);
         }

         Locale locale = null;

         if (!localeTag.isEmpty())
         {
            Locale.Builder builder = new Locale.Builder();

            try
            {
               builder.setLanguageTag(localeTag);
            }
            catch (IllformedLocaleException e)
            {
               throw new InvalidSyntaxException(
                  getMessage("error.illformed_locale", localeTag), e);
            }
         }

         if (licenseFiles == null)
         {
            licenseFiles = new Vector<HelpsetFile>();
         }

         HelpSetLocale hsl = null;

         if (localeTag != null)
         {
            hsl = new HelpSetLocale(localeTag, locale);
         }

         HelpsetFile hsf = new HelpsetFile(getHelpLib(), ref, HelpsetFile.TYPE_HTML, 
            hsl, true);

         hsf.setPath(file.toPath());
         hsf.setName(file.getName());

         try
         {
            hsf.setEncodingFromPath();
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.unable_to_parse_encoding", file, e.getMessage()), e);
         }

         licenseFiles.add(hsf);
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
               getMessage("error.clisyntax.missing_value", arg));
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
           getMessage("error.file_not_dir", inPath));
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

      if (!Files.exists(helpsetPath))
      {
         throw new InvalidSyntaxException(
           getMessage("error.helpset_dir_not_found", helpsetPath, "--helpset"));
      }

      if (!Files.isDirectory(helpsetPath))
      {
         throw new InvalidSyntaxException(
           getMessage("error.file_not_dir", helpsetPath));
      }

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
         zipFile = new File(inFile, helpsetDirName+"."+Helpset.ZIP_HELPSET_EXT);
      }
   }

   protected void run() throws IOException
   {
      helpset = new Helpset(getHelpLib());

      if (licenseFiles != null)
      {
         for (HelpsetFile hsf : licenseFiles)
         {
            helpset.addLicense(hsf);
         }
      }

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

                   HelpSetLocale hsl = null;

                   if (localeNames != null)
                   {
                      for (String tag : localeNames)
                      {
                         String name = (localePrefix == null ? tag : localePrefix+tag);


                         for (int i = 0; i < path.getNameCount()-1; i++)
                         {
                            if (path.getName(i).toString().equals(name))
                            {
                               hsl = new HelpSetLocale(tag);

                               break;
                            }
                         }
                      }
                   }

                   HelpsetFile hsFile = new HelpsetFile(getHelpLib(),
                     rUri.toString(), type, hsl);

                   hsFile.setPath(path);
                   hsFile.setNameFrom(inPath.relativize(path));

                   hsFile.setEncodingFromPath();

                   helpset.add(hsFile);
                }
                else
                {
                   warning(getMessage("message.skipping_unsupported",
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

   Vector<HelpsetFile> licenseFiles;
}
