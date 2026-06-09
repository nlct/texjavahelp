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
package com.dickimawbooks.tjhcreateiconpdf;

import java.io.*;

import java.nio.file.Files;
import java.nio.charset.Charset;

import java.util.Locale;
import java.util.Vector;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.awt.Dimension;

import com.dickimawbooks.texjavahelplib.*;

import com.dickimawbooks.texparserlib.TeXParser;
import com.dickimawbooks.texparserlib.TeXPath;
import com.dickimawbooks.texparserlib.latex2latex.LaTeX2LaTeX;

/**
 * Creates a PDF with each icon image as a separate page. This
 * is designed to allow texjavahelp.sty to include an application icon in the
 * documentation without having to copy all the icon images into the
 * graphics path.
 */
public class CreateIconPdf extends CLITeXAppAdapter
{
   public CreateIconPdf()
   {
      inFileNames = new Vector<String>();
   }

   @Override
   protected void loadDictionaries(MessageSystem msgSys) throws IOException
   {
      super.loadDictionaries(msgSys);

      msgSys.loadDictionary(
       "/com/dickimawbooks/tjhcreateiconpdf/dictionaries/",
       "tjhcreateiconpdf");
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

   @Override
   public void printCLISyntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("clisyntax.usage",
        getMessage("syntax.options", getCLIApplicationName())));

      System.out.println();

      printSyntaxItem(getMessage("syntax.in", "--in", "-i"));

      printSyntaxItem(getMessage("syntax.size", "--size", "-s"));

      printSyntaxItem(getMessage("syntax.base", "--base", "-b"));

      printSyntaxItem(getMessage("syntax.resource-path", "--resource-path", "-p"));

      printSyntaxItem(getMessage("syntax.keep-doc-src", "--[no]keep-doc-src", "-k"));

      printSyntaxItem(getMessage("syntax.out", "--out-dir", "-o"));

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
      if (arg.equals("--in") || arg.equals("-i")
       || arg.equals("--out-dir") || arg.equals("-o")
       || arg.equals("--size") || arg.equals("-s")
       || arg.equals("--base") || arg.equals("-b")
       || arg.equals("--resource-path") || arg.equals("-p")
         )
      {
         return 1;
      }

      return 0;
   }

   @Override
   protected void parseNoSwitchCLIArg(String arg) throws InvalidSyntaxException
   {
      inFileNames.add(arg);
   }

   @Override
   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = getSyntaxParser();

      if (arg.equals("--keep-doc-src") || arg.equals("-k"))
      {
         keepDocSrc = true;
      }
      else if (arg.equals("--nokeep-doc-src"))
      {
         keepDocSrc = false;
      }
      else if (cliParser.isArg(arg, "--in", "-i", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         inFileNames.add(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--base", "-b", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         basename = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--resource-path", "-p", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         resourcePath = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--out-dir", "-o", returnVals))
      {
         if (outDir != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.syntax.only_one", arg));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         outDir = new File(returnVals[0].toString());

         if (!outDir.isDirectory())
         {
            throw new InvalidSyntaxException(
               getMessage("error.syntax.not_directory", outDir));
         }
      }
      else if (cliParser.isArg(arg, "--size", "-s", returnVals))
      {
         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing.value", arg));
         }

         sizeVal = returnVals[0].toString();

         int idx = sizeVal.indexOf("x");

         try
         {
            if (idx < 0)
            {
               width = Integer.parseInt(sizeVal);
               height = width;
            }
            else
            {
               width = Integer.parseInt(sizeVal.substring(0, idx));
               height = Integer.parseInt(sizeVal.substring(idx+1));
            }
         }
         catch (NumberFormatException e)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.invalid.size_value", arg, sizeVal), e);
         }

         if (width <= 0)
         {
            throw new InvalidSyntaxException(
              getMessage("error.error.syntax.positive_value_required", arg, width));
         }

         if (height <= 0)
         {
            throw new InvalidSyntaxException(
              getMessage("error.error.syntax.positive_value_required", arg, height));
         }
      }
      else
      {
         return super.parseCLIArg(arg, returnVals);
      }

      return true;
   }

   @Override
   protected void postCLIProcess() throws InvalidSyntaxException
   {
      if (inFileNames.isEmpty())
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_in"));
      }
      
      if (outDir == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_out"));
      }

      if (basename == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_option", "--base"));
      }

      if (sizeVal == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_option", "--size"));
      }

      filterPattern = Pattern.compile("(.*)-("+sizeVal+")\\.(png|jpe?g)");

      pdfFile = new File(outDir, basename + "-" +sizeVal + ".pdf");
      mapFile = new File(outDir, basename + "-" +sizeVal + ".def");
   }

   protected void addImageFile(File file)
   throws IOException,InterruptedException
   {
      String name = "";
      String suffix = "";
      String ext = "";

      if (!file.isDirectory())
      {
         Matcher m = filterPattern.matcher(file.getName());

         if (m.matches())
         {
            name = m.group(1);
            suffix = m.group(2);
            ext = m.group(3);
         }
         else
         {
            return;
         }
      }

      if (!file.exists())
      {
         throw new FileNotFoundException(
           getMessageWithFallback(
             "error.image_not_found",
             "Image ''{0}'' not found", file));
      }
      else if (file.isDirectory())
      {
         File[] list = file.listFiles();

         for (File f : list)
         {
            addImageFile(f);
         }
      }
      else
      {
         imageFiles.add(new TJHIconFile(new TeXPath(parser, file.getAbsoluteFile()),
           pdfFile, resourcePath, name, suffix, ext));
      }
   }

   protected void run()
      throws IOException,InterruptedException
   { 
      LaTeX2LaTeX listener = new LaTeX2LaTeX(this, mapFile.getParentFile());

      parser = new TeXParser(listener);

      openLogWriter(parser, defaultCharset);

      PrintWriter writer = null;

      try
      {
         imageFiles = new Vector<TJHIconFile>();

         for (String filename : inFileNames)
         {
            addImageFile(new File(filename));
         }

         if (imageFiles.isEmpty())
         {
            throw new FileNotFoundException(getMessage("error.no_image_files_matching",
             filterPattern.pattern()));
         }

         File file = createTempFile(".tex", true);

         String name = file.getName();

         name = name.substring(0, name.length()-4);

         writer = new PrintWriter(
              createBufferedWriter(file.toPath(), defaultCharset));

         writer.println("\\batchmode");

         writer.println("\\documentclass{article}");
         writer.println("\\usepackage{graphicx}");
         writer.println("\\usepackage{geometry}");
         writer.format((Locale)null,
          "\\geometry{paperwidth=%dbp,paperheight=%dbp,", width, height);

         writer.println("noheadfoot,nomarginpar,margin=0pt}");
         writer.println("\\pagestyle{empty}");
         writer.println("\\setlength\\parindent{0pt}");
         writer.format("\\newcommand\\imagebox[1]{\\parbox[c][%dbp][c]{%dbp}{\\hfil #1\\hfill}}%n",
          height, width);
         writer.println("\\begin{document}%");

         for (int i = 0; i < imageFiles.size(); i++)
         {
            TJHIconFile icf = imageFiles.get(i);
            icf.setPageNumber(i+1);

            if (i > 0)
            {
               writer.println("\\newpage");
            }

            writer.print("\\imagebox{\\includegraphics{");
            writer.print(icf.formatTeXPath());
            writer.println("}}");
         }

         writer.println("\\end{document}");

         writer.close();
         writer = null;

         String invoker = "pdflatex";

         if (isDebuggingOn())
         {
            getHelpLib().debug(getMessageWithFallback("message.running",
              "Running {0}",
               String.format("%s \"%s\"", invoker, file.getName())));
         }

         ProcessBuilder pb = new ProcessBuilder(invoker, file.getName());

         pb.directory(tmpDir);

         Process process = pb.start();
         int processExitCode = process.waitFor();

         if (processExitCode != 0)
         {
            setExitCode(TeXJavaHelpLibAppAdapter.EXIT_PROCESS_FAILED);

            throw new IOException(getMessage("error.app_failed",
              String.format("%s \"%s\"", invoker, file.getName()),
              processExitCode));
         }

         copyFile(new File(tmpDir, name+".pdf"), pdfFile);

         if (keepDocSrc)
         {
            copyFile(file, new File(outDir, basename + "-"+sizeVal+".tex"));
         }

         writer = new PrintWriter(
              createBufferedWriter(mapFile.toPath(), defaultCharset));

         for (int i = 0; i < imageFiles.size(); i++)
         {
            TJHIconFile icf = imageFiles.get(i);

            writer.println(icf.formatMap());
         }

         writer.close();
         writer = null;
      }
      finally
      {
         if (writer != null)
         {
            writer.close();
         }

         if (deleteTempDirOnExit)
         {
            deleteTempDir();
         }
      
         closeLogWriter();
      }
   }

   public static void main(String[] args)
   {
      final CreateIconPdf app = new CreateIconPdf();

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

   protected Pattern filterPattern;
   protected int width=0, height=0;
   protected String sizeVal;
   protected String basename;
   protected String resourcePath="icons/";
   protected File outDir;
   protected File pdfFile;
   protected File mapFile;
   protected boolean keepDocSrc = false;
   protected Vector<String> inFileNames;
   protected Vector<TJHIconFile> imageFiles;
   protected TeXParser parser;

   public static final String NAME = "tjhcreateiconpdf";
}
