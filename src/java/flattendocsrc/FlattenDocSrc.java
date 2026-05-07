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

package com.dickimawbooks.flattendocsrc;

import java.util.Vector;

import java.io.*;
import java.net.URL;
         
import java.nio.file.*;
import java.nio.charset.Charset;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex2latex.*;
import com.dickimawbooks.texparserlib.latex.KeyValList;

import com.dickimawbooks.texparserlib.auxfile.AuxParser;
import com.dickimawbooks.texparserlib.auxfile.AuxData;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibAppAdapter;
import com.dickimawbooks.texjavahelplib.CLITeXAppAdapter;
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;

public class FlattenDocSrc extends CLITeXAppAdapter
{
   @Override
   protected void parseNoSwitchCLIArg(String arg)
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
   protected int getCLIArgCount(String arg)
   {
      if (
             arg.equals("--log")
          || arg.equals("--in") ||  arg.equals("-i")
          || arg.equals("--in-charset")
          || arg.equals("--output") || arg.equals("-o")
          || arg.equals("--charset")
          || arg.equals("--out-charset")
          || arg.equals("--image-dest")
          || arg.equals("--debug")
          || arg.equals("--debug-mode")
         )
      {
         return 1;
      }

      return 0;
   }


   @Override
   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = getSyntaxParser();

      if (arg.equals("--nolog"))
      {
         logFile = null;
      }
      else if (cliParser.isArg(arg, "--log", returnVals))
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
      else if (cliParser.isArg(arg, "--in", "-i", returnVals))
      {
         if (inFile != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.syntax.only_one_input"));
         }

         inFile = new File(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--output", "-o", returnVals))
      {
         if (outDir != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.syntax.only_one", arg));
         }

         outDir = new File(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--charset", returnVals))
      {
         defaultCharset = Charset.forName(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--out-charset", returnVals))
      {
         outCharset = Charset.forName(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--in-charset", returnVals))
      {
         inCharset = Charset.forName(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--image-dest", returnVals))
      {
         String dir = returnVals[0].toString();

         if (dir.isEmpty())
         {
            imageDir = null;
         }
         else
         {
            imageDir = new File(dir);
         }
      }
      else
      {
         return false;
      }

      return true;
   }

   @Override
   protected void postCLIProcess()
     throws InvalidSyntaxException
   {
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

   @Override
   public void printCLISyntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("syntax.title"));
      System.out.println();
      printSyntaxItem(getMessage("syntax.opt_in", getApplicationName()));
      System.out.println();
      printSyntaxItem(getMessage("syntax.general"));
      printSyntaxItem(getMessage("syntax.in", "--in", "-i", getApplicationName()));
      System.out.println();
      printSyntaxItem(getMessage("syntax.output.options"));
      System.out.println();
      printSyntaxItem(getMessage("syntax.out", "--output", "-o"));
      printSyntaxItem(getMessage("syntax.out.charset", "--out-charset"));
      printSyntaxItem(getMessage("syntax.out.image-dest", "--image-dest"));

      System.out.println();
      getMessage("clisyntax.other.options");
      System.out.println();

      printCommonCLISyntax();

      printSyntaxItem(getMessage("clisyntax.debug-mode", "--debug-mode"));
      System.out.println();

      printSyntaxItem(getMessage("clisyntax.log", "--log"));
      printSyntaxItem(getMessage("clisyntax.nolog", "--nolog"));

      System.out.println();
      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   @Override
   public void libraryVersion()
   {  
      libraryVersion(false);
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

   public File getOutDirectory()
   {
      return outDir;
   }

   protected void copyBib2GlsFile(String name, TeXParser parser)
     throws IOException
   {
      TeXPath texPath = new TeXPath(parser, name, bib2glsFilesUseKpsewhich, "bib");

      File srcFile = texPath.getFile();

      if (srcFile.exists())
      {
         File destFile;

         if (bib2glsFilesRetainRelDir)
         {
            Path relPath = texPath.getRelativePath();
            Path dest = outDir.toPath().resolve(relPath);

            destFile = dest.toFile();
         }
         else
         {
            destFile = new File(outDir, srcFile.getName());
         }

         copyFile(srcFile, destFile);
      }
      else
      {
         warning(parser,
           getMessage("tex.error.file.not.found", srcFile));
      }
   }

   protected void copyBib2GlsFiles(AuxData aux, TeXParser parser)
     throws IOException
   {
      KeyValList options = TeXParserUtils.toKeyValList(aux.getArg(0), parser);

      String glstexBasename = aux.getArg(1).toString(parser);

      TeXObject src = options.getValue("src");

      if (src == null)
      {
         copyBib2GlsFile(glstexBasename, parser);
      }
      else
      {
         String[] srcList = src.toString(parser).trim().split(" *, *");

         for (String name : srcList)
         {
            copyBib2GlsFile(name, parser);
         }
      }
   }

   protected void parseAux() throws IOException
   {
      String name = inFile.getName();

      int idx = name.lastIndexOf(".");

      if (idx > -1)
      {
         name = name.substring(0, idx);
      }

      File auxFile = new File(inFile.getParentFile(), name+".aux");

      if (auxFile.exists())
      {
         AuxParser auxListener = new AuxParser(this, inCharset);

         TeXParser auxTeXParser = new TeXParser(auxListener);

         auxListener.addAuxCommand("glsxtr@resource", 2);

         auxListener.enableSaveDivisions(false);
         auxListener.enableSaveLabels(false);
         auxListener.enableSaveCites(false);

         auxListener.parseAuxFile(auxTeXParser, auxFile, null);

         Vector<AuxData> data = auxListener.getAuxData();

         if (data != null)
         {
            for (AuxData aux : data)
            {
               if (aux.getName().equals("glsxtr@resource"))
               {
                  copyBib2GlsFiles(aux, auxTeXParser);
               }
            }
         }
      }
      else
      {
         warning(null,
           getMessage("tex.error.file.not.found", auxFile));
      }

   }

   protected void run() throws IOException
   {
      FlattenDocSrcListener listener = new FlattenDocSrcListener(
        this, outDir, outCharset, replaceGraphicsPath);

      TeXParser parser = new TeXParser(listener);

      if (imageDir == null)
      {
         listener.setReplaceGraphicsPath(false);
      }
      else
      {
         listener.setImageDestinationPath(imageDir.toPath());
         listener.setReplaceGraphicsPath(true);
      }

      listener.setGraphicsUseKpsewhich(graphicsUseKpsewhich);

      File dir = inFile.getParentFile();

      if (dir == null)
      {
         dir = new File(System.getProperty("user.dir"));
      }

      parser.setBaseDir(dir);

      openLogWriter(parser, outCharset);

      parser.setCatCode(false, '_', TeXParser.TYPE_OTHER);
      parser.setCatCode(false, '^', TeXParser.TYPE_OTHER);
      parser.setCatCode(false, '$', TeXParser.TYPE_OTHER);
      parser.setCatCode(false, '~', TeXParser.TYPE_OTHER);

      try
      {
         parser.parse(inFile);

         parseAux();
      }
      finally
      {
         closeLogWriter();
      }
   }

   public static void main(String[] args)
   {
      final FlattenDocSrc app = new FlattenDocSrc();

      try
      {
         app.initialiseHelpAndParse(args);
         app.run();
      }
      catch (InvalidSyntaxException e)
      {
         app.error(e.getMessage(), null);
      }
      catch (Throwable e)
      {
         app.error(null, e);
      }

      System.exit(app.getExitCode());
   }

   private boolean shownVersion = false;

   private File inFile, outDir, imageDir;
   private Charset inCharset, outCharset;

   private boolean bib2glsFilesRetainRelDir = false;
   private boolean bib2glsFilesUseKpsewhich = false;
   private boolean replaceGraphicsPath = true;
   private boolean graphicsUseKpsewhich = false;

   public static final String NAME = "tjhflattendocsrc";
}

