/*
    Copyright (C) 2024 Nicola L.C. Talbot
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

package com.dickimawbooks.texjavahelpmk;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.*;
import java.net.URL;

import java.nio.file.*;
import java.nio.charset.Charset;

import java.awt.Dimension;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.LaTeXSty;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.color.ColorSty;
import com.dickimawbooks.texparserlib.html.*;
import com.dickimawbooks.texparserlib.latex2latex.LaTeXPreambleListener;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibAppAdapter;
import com.dickimawbooks.texjavahelplib.CLITeXAppAdapter;
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;

public class TeXJavaHelpMk extends CLITeXAppAdapter
{
   @Override
   protected void parseNoSwitchArg(String arg)
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
   protected int cliArgCount(String arg)
   {
      if (arg.equals("--log")
       || arg.equals("--split")
       || arg.equals("--head")
       || arg.equals("--minitoc-preamble")
       || arg.equals("--minitoc-postamble")
       || arg.equals("--in") || arg.equals("-i")
       || arg.equals("--output") || arg.equals("-o")
       || arg.equals("--out-charset")
       || arg.equals("--image-dest")
       || arg.equals("--image-preamble")
       || arg.equals("--debug")
       || arg.equals("--debug-mode")
         )
      {
         return 1;
      }

      return 0;
   }

   @Override
   protected boolean parseCliArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = getSyntaxParser();

      if (arg.equals("--nolog"))
      {
         logFile = null;
      }
      else if (arg.equals("--nomathjax"))
      {
         mathJax = false;
      }
      else if (arg.equals("--mathjax"))
      {
         mathJax = true;
      }
      else if (arg.equals("--nohelpset"))
      {
         isHelpset = false;
         breadcrumbtrail = true;
         minitoc = true;
      }
      else if (arg.equals("--helpset"))
      {
         isHelpset = true;
         breadcrumbtrail = false;
         minitoc = false;
      }
      else if (arg.equals("--nobreadcrumbtrail"))
      {
         breadcrumbtrail = false;
      }
      else if (arg.equals("--breadcrumbtrail"))
      {
         breadcrumbtrail = true;
      }
      else if (arg.equals("--nominitoc"))
      {
         minitoc = false;
      }
      else if (arg.equals("--minitoc"))
      {
         minitoc = true;
      }
      else if (arg.equals("--nosupport-unicode-script"))
      {
         useUnicodeSubSupScript = false;
      }
      else if (arg.equals("--support-unicode-script"))
      {
         useUnicodeSubSupScript = true;
      }
      else if (arg.equals("--noentities"))
      {
         useHtmlEntities = false;
      }
      else if (arg.equals("--entities"))
      {
         useHtmlEntities = true;
      }
      else if (arg.equals("--prefix-split"))
      {
        splitUseBaseNamePrefix = true;
      }
      else if (arg.equals("--noprefix-split"))
      {
        splitUseBaseNamePrefix = false;
      }
      else if (arg.equals("--no-rm-tmp-dir"))
      {
         deleteTempDirOnExit = false;
      }
      else if (arg.equals("--rm-tmp-dir"))
      {
         deleteTempDirOnExit = true;
      }
      else if (arg.equals("--no-convert-images"))
      {
         convertImages = false;
      }
      else if (arg.equals("--convert-images"))
      {
         convertImages = true;
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
      else if (cliParser.isIntArg(arg, "--split", returnVals))
      {
         splitLevel = returnVals[0].intValue();
      }
      else if (cliParser.isArg(arg, "--head", returnVals))
      {
         extraHead = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--minitoc-preamble", returnVals))
      {
         minitocPreamble = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--minitoc-postamble", returnVals))
      {
         minitocPostamble = returnVals[0].toString();
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
      else if (cliParser.isArg(arg, "--out-charset", returnVals))
      {
         outCharset = Charset.forName(returnVals[0].toString());
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
      else if (cliParser.isArg(arg, "--image-preamble", returnVals))
      {
         imagePreambleFile = new File(returnVals[0].toString());
      }
      else
      {
         return false;
      }

      return true;
   }

   @Override
   protected void postParseArgs()
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

   public boolean isConvertImagesOn()
   {
      return convertImages;
   }

   public int getSplitLevel()
   {
      return splitLevel;
   }

   public boolean isSplitBaseNamePrefixOn()
   {
      return splitUseBaseNamePrefix;
   }

   public boolean isUseHtmlEntitiesOn()
   {
      return useHtmlEntities;
   }

   public boolean isUseMathJaxOn()
   {
      return mathJax;
   }

   public boolean isUseUnicodeSuperSubScriptsOn()
   {
      return useUnicodeSubSupScript;
   }

   public boolean isParsePackagesOn()
   {
      return true;
   }

   public String getExtraHeadCode()
   {
      return extraHead;
   }

   public File getOutDirectory()
   {
      return outDir;
   }

   protected void run() throws IOException
   {
      TJHListener listener = new TJHListener(this, outCharset, isHelpset);

      listener.setBreadCrumbTrailEnabled(breadcrumbtrail);
      listener.setMiniTocEnabled(minitoc);
      listener.setMiniTocPreamble(minitocPreamble);
      listener.setMiniTocPostamble(minitocPostamble);

      if (imageDir != null)
      {
         listener.setImageDest(imageDir.toPath());
      }

      TeXParser parser = new TeXParser(listener);

      openLogWriter(parser, outCharset);

      try
      {
         if (convertImages)
         {
            if (imagePreambleFile != null)
            {
               imagePreamble = getFileContents(imagePreambleFile);
            }
            else
            {
               imagePreamble = parsePreamble(inFile);
            }
         }

         parser.parse(inFile);
      }
      finally
      {
         if (deleteTempDirOnExit)
         {
            deleteTempDir();
         }

         closeLogWriter();
      }
   }

   private void deleteTempDir() throws IOException
   {
      if (tmpDir == null) return;

      File[] files = tmpDir.listFiles();

      for (File f : files)
      {
         f.delete();
      }

      tmpDir.delete();
   }

   public String getImagePreamble()
   {
      return imagePreamble;
   }

   public L2HImage createImage(TeXParser parser, String preamble,
    String content, String mimetype, TeXObject alt, String name,
    boolean crop, Path relPath)
   throws IOException,InterruptedException
   {
      L2HConverter listener = (L2HConverter)parser.getListener();

      if (name == null)
      {
         nameIdx++;
         name = String.format("img%06d", nameIdx);
      }

      Charset charset = listener.getCharSet();
      L2HImage image = null;
      PrintWriter writer = null;

      try
      {
         if (tmpDir == null)
         {
            tmpDir = Files.createTempDirectory(NAME).toFile();
         }

         File file = new File(tmpDir, tmpDir.getName()+name+".tex");

         if (charset == null)
         {
            writer = new PrintWriter(
              createBufferedWriter(file.toPath(), defaultCharset));
         }
         else
         {
            writer = new PrintWriter(
              createBufferedWriter(file.toPath(), charset));
         }

         writer.println("\\batchmode");
         writer.println(preamble);

         String[] grpaths = listener.getGraphicsPaths();

         if (grpaths != null)
         {
            writer.print("\\graphicspath{");

            Path basePath = inFile.getAbsoluteFile().getParentFile().toPath();
            String basePathStr = basePath.toString();

            if (File.separatorChar != '/')
            {
               basePathStr = basePathStr.replaceAll("/", File.separator);
            }

            if (!basePathStr.endsWith("/"))
            {
               basePathStr += "/";
            }

            for (int i = 0; i < grpaths.length; i++)
            {
               writer.format("{%s%s}", basePathStr, grpaths[i]);
            }

            writer.println("}");
         }

         writer.println("\\begin{document}");
         writer.println(content);
         writer.println("\\end{document}");

         writer.close();
         writer = null;

         String invoker;

         if (listener.isStyLoaded("fontspec"))
         {
            invoker = "lualatex";
         }
         else
         {
            invoker = "pdflatex";
         }

         File dir = inFile.getParentFile();

         if (dir == null)
         {
            dir = new File(".");
         }

         if (isDebuggingOn())
         {
            getHelpLib().debug(getMessageWithFallback("message.running",
              "Running {0}",
               String.format("%s -jobname \"%s\" \"%s\"", invoker, name, file.getName())));
         }
         
         ProcessBuilder pb = new ProcessBuilder(invoker, "-jobname", name, file.getName());

         pb.directory(tmpDir);

         Map<String,String> env = pb.environment();
         env.put("TEXINPUTS", String.format("%s%c",
              dir.getAbsolutePath(), File.pathSeparatorChar));

         Process process = pb.start();
         int processExitCode = process.waitFor();

         if (processExitCode != 0)
         {
            setExitCode(TeXJavaHelpLibAppAdapter.EXIT_PROCESS_FAILED);

            throw new IOException(getMessage("error.app_failed",
              String.format("%s -jobname \"%s\" \"%s\"", invoker, name, file.getName()),
              processExitCode));
         }

         if (mimetype == null)
         {
            mimetype = L2HConverter.MIME_TYPE_PNG;
         }

         File pdfFile = new File(tmpDir, name+".pdf");
         File destFile;

         if (crop)
         {
            invoker = "pdfcrop";

            String croppedPdfName = name+"-crop.pdf";

            processExitCode = getHelpLib().execCommandAndWaitFor(tmpDir,
               MAX_PROCESS_TIME,
               invoker, pdfFile.getName(), croppedPdfName);

            if (processExitCode == 0)
            {
               pdfFile = new File(tmpDir, croppedPdfName);
            }
            else
            {
               warning(parser, getMessage("error.app_failed",
                 String.format("%s \"%s\"", invoker,
                    pdfFile.getName(), croppedPdfName),
                 processExitCode));
            }
         }

         Dimension imageDim = null;

         Path outPath = outDir.toPath();

         if (relPath != null)
         {
            outPath = outPath.resolve(relPath);
         }

         if (mimetype.equals(L2HConverter.MIME_TYPE_PDF))
         {
            destFile = (new File(outPath.toFile(), name+".pdf"));
            copyFile(pdfFile, destFile);
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_PNG))
         {
            File pngFile = pdfToImage(pdfFile, name, "png");

            imageDim = getImageFileDimensions(parser, pngFile, mimetype);

            destFile = new File(outPath.toFile(), pngFile.getName());
            copyFile(pngFile, destFile);
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_JPEG))
         {
            File jpegFile = pdfToImage(pdfFile, name, "jpeg");

            imageDim = getImageFileDimensions(parser, jpegFile, mimetype);

            destFile = new File(outPath.toFile(), jpegFile.getName());
            copyFile(jpegFile, destFile);
         }
         else
         {
            warning(parser, getMessage("warning.unsupported.image.type",
             mimetype));

            mimetype=L2HConverter.MIME_TYPE_PDF;

            destFile = new File(outPath.toFile(), name+".pdf");
            copyFile(pdfFile, destFile);
         }

         int width=0;
         int height=0;

         if (imageDim != null)
         {
            width = imageDim.width;
            height = imageDim.height;
         }

         image = new L2HImage(outDir.toPath().relativize(destFile.toPath()),
          mimetype, width, height, name, alt, true);
      }
      finally
      {
         if (writer != null)
         {
            writer.close();
         }
      }

      return image;
   }

   public Dimension getImageFileDimensions(TeXParser parser, File file,
     String type)
     throws IOException,InterruptedException
   {
      String invoker = "file";

      boolean isPdf = L2HConverter.MIME_TYPE_PDF.equals(type);

      if (isPdf)
      {
         invoker = "pdfinfo";
      }

      String line = null;
      StringBuilder result = new StringBuilder();

      int exitCode = getHelpLib().execCommandAndWaitFor(result,
       MAX_PROCESS_TIME,
       invoker, file.getAbsolutePath());

      Pattern pat = null;

      if (L2HConverter.MIME_TYPE_PNG.equals(type))
      {
         pat = PNG_INFO;
      }
      else if (L2HConverter.MIME_TYPE_JPEG.equals(type))
      {
         pat = JPEG_INFO;
      }
      else if (isPdf)
      {
         pat = PDF_INFO;
      }
      else
      {
         return null;
      }

      if (exitCode == 0)
      {
         line = result.toString();

         if (line == null || line.isEmpty())
         {
            return null;
         }

         Matcher m = pat.matcher(line);

         if (m.matches())
         {
            try
            {
               int width, height;

               if (isPdf)
               {
                  width = (int)Math.round(Float.parseFloat(m.group(1)));
                  height = (int)Math.round(Float.parseFloat(m.group(2)));
               }
               else
               {
                  width = Integer.parseInt(m.group(1));
                  height = Integer.parseInt(m.group(2));
               }

               return new Dimension(width, height);
            }
            catch (NumberFormatException e)
            {// shouldn't happen, pattern ensures format correct
               getHelpLib().debug(e);
            }
         }
      }
      else
      {
         warning(parser, getMessage("error.app_failed",
           String.format("%s \"%s\"", invoker, file.getName()),
           exitCode));
      }

      return null;
   }

   protected File pdfToImage(File pdfFile, String basename, String format)
     throws IOException,InterruptedException
   {
      String invoker = "pdftoppm";

      int exitCode = getHelpLib().execCommandAndWaitFor(tmpDir,
          MAX_PROCESS_TIME, 
          invoker, "-singlefile", "-"+format,
          pdfFile.getAbsolutePath().toString(), basename);

      if (exitCode != 0)
      {
         throw new IOException(getMessage("error.app_failed",
           String.format("%s -singlefile -png \"%s\" \"%s\"", invoker,
             pdfFile.getAbsolutePath(), basename), exitCode));
      }

      return new File(tmpDir, basename+"."+format);
   }

   public String getFileContents(File file)
   throws IOException
   {
      BufferedReader in = null;
      StringWriter writer = new StringWriter();

      try
      {
         in = createBufferedReader(file.toPath(), getDefaultCharset());

         int c = -1;

         while ((c = in.read()) != -1)
         {
            writer.write(c);
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      return writer.toString();
   }

   protected String parsePreamble(File file)
   throws IOException
   {
      StringWriter strWriter = new StringWriter();

      LaTeXPreambleListener preambleListener
         = new LaTeXPreambleListener(this, strWriter);

      preambleListener.enableReplaceJobname(true);

      TeXParser p = new TeXParser(preambleListener);

      preambleListener.putControlSequence(new L2LLoadResources());

      p.parse(file);

      TeXPath inPath = new TeXPath(p, file.getAbsoluteFile());

      strWriter.write(String.format("\\texparserimgfile{%s}",
         inPath.getTeXPath(true)));

      return strWriter.toString();
   }

   @Override
   public void cliSyntax()
   {
      syntax();
   }

   @Override
   public void cliVersion()
   {
      System.out.println(getHelpLib().getAboutInfo(false,
        TeXJavaHelpLib.VERSION,
        TeXJavaHelpLib.VERSION_DATE,
        String.format(
         "Copyright (C) %s Nicola L. C. Talbot (%s)",
          TeXJavaHelpLib.VERSION_DATE.substring(0, 4),
          getHelpLib().getInfoUrl(false, "www.dickimaw-books.com")),
         TeXJavaHelpLib.LICENSE_GPL3,
         true, null
      ));
   }

   public void syntax()
   {
      TeXJavaHelpLib helpLib = getHelpLib();

      versionInfo();
      System.out.println();
      System.out.println(getMessage("syntax.title"));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.opt_in", getApplicationName()));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.general"));
      helpLib.printSyntaxItem(getMessage("syntax.in", "--in", "-i", getApplicationName()));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.debug", "--debug"));
      helpLib.printSyntaxItem(getMessage("syntax.nodebug", "--nodebug"));
      helpLib.printSyntaxItem(getMessage("syntax.debug-mode", "--debug-mode"));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.log", "--log"));
      helpLib.printSyntaxItem(getMessage("syntax.nolog", "--nolog"));
      helpLib.printSyntaxItem(getMessage("syntax.rmtmpdir", "--[no-]rm-tmp-dir"));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.version", "--version", "-v"));
      helpLib.printSyntaxItem(getMessage("syntax.help", "--help", "-h"));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.output.options"));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.out", "--output", "-o"));
      helpLib.printSyntaxItem(getMessage("syntax.out.charset", "--out-charset"));
      helpLib.printSyntaxItem(getMessage("syntax.out.image-dest", "--image-dest"));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.html.options"));
      System.out.println();
      helpLib.printSyntaxItem(getMessage("syntax.head", "--head"));
      helpLib.printSyntaxItem(getMessage("syntax.helpset", "--[no]helpset"));
      helpLib.printSyntaxItem(getMessage("syntax.breadcrumbtrail", "--[no]breadcrumbtrail"));
      helpLib.printSyntaxItem(getMessage("syntax.minitoc", "--[no]minitoc"));
      helpLib.printSyntaxItem(getMessage("syntax.minitoc-preamble", "--minitoc-preamble"));
      helpLib.printSyntaxItem(getMessage("syntax.minitoc-postamble", "--minitoc-postamble"));
      helpLib.printSyntaxItem(getMessage("syntax.mathjax", "--[no]mathjax"));
      helpLib.printSyntaxItem(getMessage("syntax.support-unicode-script",
         "--[no]support-unicode-script"));
      helpLib.printSyntaxItem(getMessage("syntax.entities", "--entities"));
      System.out.println();
      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   public void versionInfo()
   {
      if (!shownVersion)
      {
         System.out.println(getMessageWithFallback("about.version",
           "{0} version {1} ({2})", getApplicationName(), 
           TeXJavaHelpLib.VERSION, TeXJavaHelpLib.VERSION_DATE));

         shownVersion = true;
      }
   }

   public void license()
   {
      System.out.println();
      System.out.format("Copyright %s Nicola Talbot%n",
       getCopyrightDate());
      System.out.println(getMessageWithFallback("about.license", "License"));
      System.out.println("https://github.com/nlct/texjavahelp");
   }

   public String getCopyrightStartYear()
   {
      return "2024";
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
   public String getApplicationName()
   {
      return NAME;
   }

   @Override
   public String getApplicationVersion()
   {
      return TeXJavaHelpLib.VERSION;
   }

   public static void main(String[] args)
   {
      final TeXJavaHelpMk app = new TeXJavaHelpMk();

      try
      {
         app.initHelpLibrary();
         app.parseArgs(args);
         app.run();
      }
      catch (InvalidSyntaxException e)
      {
         app.error(e.getMessage(), null);
         app.setExitCode(TeXJavaHelpLibAppAdapter.EXIT_SYNTAX);
      }
      catch (Throwable e)
      {
         app.error(null, e);
      }

      System.exit(app.getExitCode());
   }

   private boolean shownVersion = false;

   private boolean useHtmlEntities = false;
   private boolean mathJax = false;
   private boolean useUnicodeSubSupScript = false;

   private File inFile, outDir, imageDir;
   private int splitLevel=8;
   private Charset outCharset;

   private File tmpDir = null;

   private boolean deleteTempDirOnExit = true;
   private boolean convertImages = true;
   private boolean splitUseBaseNamePrefix = false;
   private boolean isHelpset = true;
   private boolean breadcrumbtrail = false;
   private boolean minitoc = false;
   private String minitocPreamble = null;
   private String minitocPostamble = null;

   private String outputFormat = "latex";

   private int nameIdx=0;
   
   private String extraHead=null;

   private File imagePreambleFile = null;
   private String imagePreamble = null;

   public static final Pattern PNG_INFO =
    Pattern.compile(".*: PNG image data, (\\d+) x (\\d+),.*");
   public static final Pattern JPEG_INFO =
    Pattern.compile(".*: JPEG image data, .*, (\\d+)x(\\d+),.*");
   public static final Pattern PDF_INFO =
    Pattern.compile(".*Page size:\\s+(\\d*\\.?\\d+) x (\\d*\\.?\\d+) pts.*", Pattern.DOTALL);

   public static final String NAME = "texjavahelpmk";
}
