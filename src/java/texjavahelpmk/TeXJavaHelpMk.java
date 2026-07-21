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
import java.util.Locale;
import java.util.Vector;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.*;

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
import com.dickimawbooks.texjavahelplib.MessageSystem;

public class TeXJavaHelpMk extends CLITeXAppAdapter
{
   @Override
   protected void loadDictionaries(MessageSystem msgSys) throws IOException
   {
      super.loadDictionaries(msgSys);

      msgSys.loadDictionary(
        "/com/dickimawbooks/texjavahelpmk/dictionaries/",
        "texjavahelpmk");
   }

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
      if (arg.equals("--split")
       || arg.equals("--add-css")
       || arg.equals("--add-css-from-file")
       || arg.equals("--head")
       || arg.equals("--head-from-file")
       || arg.equals("--html-ext")
       || arg.equals("--og-url-path")
       || arg.equals("--keywords")
       || arg.equals("--subject")
       || arg.equals("--description")
       || arg.equals("--author")
       || arg.equals("--author-file-as")
       || arg.equals("--title")
       || arg.equals("--title-file-as")
       || arg.equals("--isbn")
       || arg.equals("--identifier")
       || arg.equals("--identifier-type")
       || arg.equals("--identifier-scheme")
       || arg.equals("--root-name")
       || arg.equals("--root-page-preamble")
       || arg.equals("--root-page-preamble-from-file")
       || arg.equals("--body-preamble")
       || arg.equals("--body-preamble-from-file")
       || arg.equals("--body-postamble")
       || arg.equals("--body-postamble-from-file")
       || arg.equals("--minitoc-div-class")
       || arg.equals("--minitoc-div-id")
       || arg.equals("--minitoc-preamble")
       || arg.equals("--minitoc-preamble-from-file")
       || arg.equals("--minitoc-postamble")
       || arg.equals("--minitoc-postamble-from-file")
       || arg.equals("--in") || arg.equals("-i")
       || arg.equals("--output") || arg.equals("-o")
       || arg.equals("--out-charset")
       || arg.equals("--pdf-to-image-converter")
       || arg.equals("--image-dest")
       || arg.equals("--image-preamble")
       || arg.equals("--image-preamble-from-file")
       || arg.equals("--cover-image")
         )
      {
         return 1;
      }

      return super.getCLIArgCount(arg);
   }

   @Override
   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = getSyntaxParser();

      if (arg.equals("--nomathjax"))
      {
         mathJax = false;
      }
      else if (arg.equals("--mathjax"))
      {
         mathJax = true;
      }
      else if (arg.equals("--html") || arg.equals("--nohelpset"))
      {
         docTargetType = DocumentTargetType.HTML;
         breadcrumbtrail = true;
         minitoc = true;
         mathJax = true;
      }
      else if (arg.equals("--helpset"))
      {
         docTargetType = DocumentTargetType.HELPSET;
         breadcrumbtrail = false;
         minitoc = false;
         mathJax = false;
      }
      else if (arg.equals("--epub"))
      {
         docTargetType = DocumentTargetType.EPUB;
         breadcrumbtrail = false;
         minitoc = false;
         mathJax = false;
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
      else if (arg.equals("--noconvert-images") || arg.equals("--no-convert-images"))
      {
         convertImages = false;
      }
      else if (arg.equals("--convert-images"))
      {
         convertImages = true;
      }
      else if (cliParser.isIntArg(arg, "--split", returnVals))
      {
         splitLevel = returnVals[0].intValue();
      }
      else if (cliParser.isArg(arg, "--head", returnVals))
      {
         extraHead = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--head-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            extraHead = getFileContents(new File(filename));
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else if (cliParser.isArg(arg, "--add-css", returnVals))
      {
         if (extraCss == null)
         {
            extraCss = returnVals[0].toString();
         }
         else
         {
            extraCss = String.format("%s%n%s", extraCss, returnVals[0].toString());
         }
      }
      else if (cliParser.isArg(arg, "--add-css-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            String content = getFileContents(new File(filename));

            if (extraCss == null)
            {
               extraCss = content;
            }
            else
            {
               extraCss = String.format("%s%n%s", extraCss, content);
            }
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else if (cliParser.isArg(arg, "--html-ext", returnVals))
      {
         htmlSuffix = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--og-url-path", returnVals))
      {
         ogUrlPath = returnVals[0].toString();

         if (ogUrlPath.isEmpty())
         {
            ogUrlPath = null;
         }
      }
      else if (cliParser.isArg(arg, "--keywords", returnVals))
      {
         keywords = returnVals[0].toString();

         if (keywords.isEmpty())
         {
            keywords = null;
         }
      }
      else if (cliParser.isArg(arg, "--subject", returnVals))
      {
         subject = returnVals[0].toString();

         if (subject.isEmpty())
         {
            subject = null;
         }
      }
      else if (cliParser.isArg(arg, "--description", returnVals))
      {
         description = returnVals[0].toString();

         if (description.isEmpty())
         {
            description = null;
         }
      }
      else if (cliParser.isArg(arg, "--title", returnVals))
      {
         title = returnVals[0].toString();

         if (title.isEmpty())
         {
            title = null;
         }
      }
      else if (cliParser.isArg(arg, "--title-file-as", returnVals))
      {
         titleFileAs = returnVals[0].toString();

         if (titleFileAs.isEmpty())
         {
            titleFileAs = null;
         }
      }
      else if (cliParser.isArg(arg, "--author", returnVals))
      {
         author = returnVals[0].toString();

         if (author.isEmpty())
         {
            author = null;
         }
      }
      else if (cliParser.isArg(arg, "--author-file-as", returnVals))
      {
         authorFileAs = returnVals[0].toString();

         if (authorFileAs.isEmpty())
         {
            authorFileAs = null;
         }
      }
      else if (cliParser.isArg(arg, "--isbn", returnVals))
      {
         String isbn = returnVals[0].toString();

         if (isbn.isEmpty())
         {
            dcIdentifier = null;
         }
         else
         {
            dcIdentifier = isbn;
         }

         dcIdentifierType = "15";
         dcIdentifierScheme = "onix:codelist5";
      }
      else if (cliParser.isArg(arg, "--identifier-type", returnVals))
      {
         dcIdentifierType = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--identifier-scheme", returnVals))
      {
         dcIdentifierType = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--identifier", returnVals))
      {
         dcIdentifier = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--root-name", returnVals))
      {
         rootName = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--root-page-preamble", returnVals))
      {
         rootPagePreMainContent = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--root-page-preamble-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            rootPagePreMainContent = getFileContents(new File(filename));
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else if (cliParser.isArg(arg, "--body-preamble", returnVals))
      {
         bodyPreamble = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--body-preamble-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            bodyPreamble = getFileContents(new File(filename));
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else if (cliParser.isArg(arg, "--body-postamble", returnVals))
      {
         bodyPostamble = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--body-postamble-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            bodyPostamble = getFileContents(new File(filename));
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else if (cliParser.isArg(arg, "--minitoc-preamble", returnVals))
      {
         minitocPreamble = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--minitoc-preamble-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            minitocPreamble = getFileContents(new File(filename));
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else if (cliParser.isArg(arg, "--minitoc-postamble", returnVals))
      {
         minitocPostamble = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--minitoc-postamble-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            minitocPostamble = getFileContents(new File(filename));
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else if (cliParser.isArg(arg, "--minitoc-div-class", returnVals))
      {
         minitocDivClass = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--minitoc-div-id", returnVals))
      {
         minitocDivId = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "-i", "--in", returnVals))
      {
         if (inFile != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one_input"));
         }

         inFile = new File(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "-o", "--output", returnVals))
      {
         if (outDir != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one", arg));
         }

         outDir = new File(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--out-charset", returnVals))
      {
         outCharset = Charset.forName(returnVals[0].toString());
      }
      else if (cliParser.isArg(arg, "--cover-image", returnVals))
      {
         String filename = returnVals[0].toString();

         if (filename.isEmpty())
         {
            coverImage = null;
         }
         else
         {
            coverImage = new File(filename);

            if (!coverImage.exists())
            {
               throw new InvalidSyntaxException(
                  getMessage("error.file_not_found", coverImage));
            }
         }
      }
      else if (cliParser.isArg(arg, "--pdf-to-image-converter", returnVals))
      {
         imageConverter = returnVals[0].toString();
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
         imagePreamble = returnVals[0].toString();
      }
      else if (cliParser.isArg(arg, "--image-preamble-from-file", returnVals))
      {
         String filename = returnVals[0].toString();

         try
         {
            imagePreamble = getFileContents(new File(filename));
         }
         catch (IOException e)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.io_load", filename, e.getMessage()), e);
         }
      }
      else
      {
         return super.parseCLIArg(arg, returnVals);
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
      printSyntaxItem(getMessage("syntax.out_charset", "--out-charset"));
      printSyntaxItem(getMessage("syntax.convert-images", "--[no]convert-images"));
      printSyntaxItem(getMessage("syntax.image-dest", "--image-dest"));
      printSyntaxItem(getMessage("syntax.image-preamble", "--image-preamble"));
      printSyntaxItem(getMessage("syntax.image-preamble-from-file",
         "--image-preamble-from-file"));
      printSyntaxItem(getMessage("syntax.pdf-to-image-converter",
        "--pdf-to-image-converter", imageConverter));
      System.out.println();
      printSyntaxItem(getMessage("syntax.html.options"));
      System.out.println();
      printSyntaxItem(getMessage("syntax.add-css", "--add-css"));
      printSyntaxItem(getMessage("syntax.add-css-from-file", "--add-css-from-file"));
      printSyntaxItem(getMessage("syntax.body-preamble", "--body-preamble"));
      printSyntaxItem(getMessage("syntax.body-preamble-from-file",
         "--body-preamble-from-file"));
      printSyntaxItem(getMessage("syntax.body-postamble", "--body-postamble"));
      printSyntaxItem(getMessage("syntax.body-postamble-from-file",
         "--body-postamble-from-file"));
      printSyntaxItem(getMessage("syntax.breadcrumbtrail", "--[no]breadcrumbtrail"));
      printSyntaxItem(getMessage("syntax.entities", "--[no]entities"));
      printSyntaxItem(getMessage("syntax.head", "--head"));
      printSyntaxItem(getMessage("syntax.head-from-file", "--head-from-file"));
      printSyntaxItem(getMessage("syntax.helpset", "--[no]helpset"));
      printSyntaxItem(getMessage("syntax.html-ext", "--html-ext"));
      printSyntaxItem(getMessage("syntax.mathjax", "--[no]mathjax"));
      printSyntaxItem(getMessage("syntax.minitoc", "--[no]minitoc"));
      printSyntaxItem(getMessage("syntax.minitoc-div-class", "--minitoc-div-class"));
      printSyntaxItem(getMessage("syntax.minitoc-div-id", "--minitoc-div-id"));
      printSyntaxItem(getMessage("syntax.minitoc-preamble", "--minitoc-preamble"));
      printSyntaxItem(getMessage("syntax.minitoc-postamble", "--minitoc-postamble"));
      printSyntaxItem(getMessage("syntax.root-name", "--root-name"));
      printSyntaxItem(getMessage("syntax.root-page-preamble",
         "--root-page-preamble"));
      printSyntaxItem(getMessage("syntax.root-page-preamble-from-file",
         "--root-page-preamble-from-file"));
      printSyntaxItem(getMessage("syntax.support-unicode-script",
         "--[no]support-unicode-script"));
      System.out.println();

      System.out.println();
      System.out.println(getMessage("clisyntax.other.options"));
      System.out.println();

      printCommonCLISyntax();

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

   public String getHtmlSuffix()
   {
      return htmlSuffix;
   }

   public File getOutDirectory()
   {
      return outDir;
   }

   protected void run() throws IOException
   {
      TJHListener listener = new TJHListener(this, outCharset, docTargetType);

      if (extraCss != null)
      {
         listener.addCssStyle(extraCss);
      }

      listener.setOgUrlPath(ogUrlPath);
      listener.setCoverImage(coverImage);

      listener.setKeywords(keywords);
      listener.setSubject(subject);
      listener.setDescription(description);
      listener.setTitle(title, titleFileAs);
      listener.setAuthor(author, authorFileAs);
      listener.setIdentifier(dcIdentifierScheme, dcIdentifierType, dcIdentifier);

      listener.setRootName(rootName);
      listener.setRootPagePreMain(rootPagePreMainContent);
      listener.setBodyPreamble(bodyPreamble);
      listener.setBodyPostamble(bodyPostamble);

      listener.setBreadCrumbTrailEnabled(breadcrumbtrail);
      listener.setMiniTocEnabled(minitoc);
      listener.setMiniTocPreamble(minitocPreamble);
      listener.setMiniTocPostamble(minitocPostamble);

      if (minitocDivClass != null && !minitocDivClass.isEmpty())
      {
         listener.setMiniTocDivClass(minitocDivClass);
      }

      if (minitocDivId != null && !minitocDivId.isEmpty())
      {
         listener.setMiniTocDivId(minitocDivId);
      }

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
            if (imagePreamble == null)
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
         name = String.format((Locale)null, "img%06d", nameIdx);
      }

      Charset charset = listener.getCharSet();
      L2HImage image = null;
      PrintWriter writer = null;

      try
      {
         File file = createTempFile(name+".tex", true);

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
         File destFile = null;

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

         boolean isPdf = false;

         if (mimetype.equals(L2HConverter.MIME_TYPE_PDF))
         {
            isPdf = true;
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_PNG))
         {
            File pngFile = new File(outPath.toFile(), name+".png");
            pdfToImage(pdfFile, pngFile);

            imageDim = getImageFileDimensions(parser, pngFile, mimetype);
         }
         else if (mimetype.equals(L2HConverter.MIME_TYPE_JPEG))
         {
            File jpegFile = new File(outPath.toFile(), name+".jpeg");
            pdfToImage(pdfFile, jpegFile);

            imageDim = getImageFileDimensions(parser, jpegFile, mimetype);
         }
         else
         {
            warning(parser, getMessage("warning.unsupported.image.type",
             mimetype));

            mimetype=L2HConverter.MIME_TYPE_PDF;
            isPdf = true;
         }

         int width=0;
         int height=0;

         if (imageDim != null)
         {
            width = imageDim.width;
            height = imageDim.height;
         }

         if (isPdf)
         {
            destFile = (new File(outPath.toFile(), name+".pdf"));
            copyFile(pdfFile, destFile);

            File pngFile = new File(outPath.toFile(), name+".png");
            pdfToImage(pdfFile, pngFile);

            imageDim = getImageFileDimensions(parser, pngFile,
               L2HConverter.MIME_TYPE_PNG);

            if (crop && imageDim != null)
            {
               width = imageDim.width;
               height = imageDim.height;
            }

            L2HImage fallback = new L2HImage(pngFile.toPath(), 
              L2HConverter.MIME_TYPE_PNG, 
              imageDim == null ? 0 : imageDim.width,
              imageDim == null ? 0 : imageDim.height,
              name, alt, true);  

            alt = fallback;
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

   protected void pdfToImage(File pdfFile, File destFile)
     throws IOException,InterruptedException
   {
      if (imageConverter.equals("pdftoppm"))
      {
         String name = destFile.getName();

         int idx = name.lastIndexOf(".");

         String ext = "png";

         if (idx > -1)
         {
            name = name.substring(0, idx);
            ext = name.substring(idx+1);
         }

         File tmpFile = pdfToImage(pdfFile, name, ext);

         Files.move(tmpFile.toPath(), destFile.toPath());
      }
      else
      {
         int exitCode = getHelpLib().execCommandAndWaitFor(tmpDir,
          MAX_PROCESS_TIME, 
          imageConverter, 
          pdfFile.getAbsolutePath().toString(), 
          destFile.getAbsolutePath().toString());

         if (exitCode != 0)
         {
            throw new IOException(getMessage("error.app_failed",
              String.format("%s \"%s\" \"%s\"", imageConverter,
                pdfFile.getAbsolutePath(), destFile.getAbsolutePath()), exitCode));
         }
      }
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

   public static void main(String[] args)
   {
      final TeXJavaHelpMk app = new TeXJavaHelpMk();

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

   private boolean useHtmlEntities = false;
   private boolean mathJax = false;
   private boolean useUnicodeSubSupScript = false;

   private File inFile, outDir, imageDir;
   private int splitLevel=8;
   private Charset outCharset;

   private boolean convertImages = true;
   private boolean splitUseBaseNamePrefix = false;
   private DocumentTargetType docTargetType = DocumentTargetType.HELPSET;
   private boolean breadcrumbtrail = false;
   private boolean minitoc = false;
   private String minitocPreamble = null;
   private String minitocPostamble = null;
   private String minitocDivClass = null;
   private String minitocDivId = null;
   private String bodyPreamble = null;
   private String bodyPostamble = null;
   private String ogUrlPath = null;
   private String rootPagePreMainContent = null;
   private String rootName = null;
   private String keywords = null;
   private String description = null;
   private String subject = null;
   private String author = null;
   private String authorFileAs = null;
   private String title = null;
   private String titleFileAs = null;
   private String dcIdentifier = null;
   private String dcIdentifierScheme = null;
   private String dcIdentifierType = null;

   private String outputFormat = "latex";
   private String imageConverter = "magick";

   private int nameIdx=0;
   
   private String extraHead=null;
   private String extraCss=null;
   private String htmlSuffix = "html";

   private String imagePreamble = null;
   private File coverImage = null;

   public static final String NAME = "texjavahelpmk";
}
