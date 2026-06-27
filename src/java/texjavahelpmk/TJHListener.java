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

import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Locale;

import java.util.zip.*;

import java.util.regex.Pattern;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.awt.Dimension;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.primitives.Relax;
import com.dickimawbooks.texparserlib.primitives.Undefined;
import com.dickimawbooks.texparserlib.latex.AtGobble;
import com.dickimawbooks.texparserlib.latex.AtFirstOfOne;
import com.dickimawbooks.texparserlib.latex.AtFirstOfTwo;
import com.dickimawbooks.texparserlib.latex.AtSecondOfTwo;
import com.dickimawbooks.texparserlib.latex.FloatBoxStyle;
import com.dickimawbooks.texparserlib.latex.FrameBox;
import com.dickimawbooks.texparserlib.latex.GobbleOpt;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.LaTeXSty;
import com.dickimawbooks.texparserlib.latex.MissingValue;
import com.dickimawbooks.texparserlib.latex.color.ColorSty;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryEntry;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryGroup;

import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.L2HImage;
import com.dickimawbooks.texparserlib.html.L2HItem;
import com.dickimawbooks.texparserlib.html.L2HList;
import com.dickimawbooks.texparserlib.html.DivisionNode;
import com.dickimawbooks.texparserlib.html.DocumentBlockType;
import com.dickimawbooks.texparserlib.html.FileData;
import com.dickimawbooks.texparserlib.html.HtmlTag;
import com.dickimawbooks.texparserlib.html.HtmlLiteral;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;
import com.dickimawbooks.texparserlib.html.VoidElement;
import com.dickimawbooks.texparserlib.auxfile.DivisionInfo;
import com.dickimawbooks.texparserlib.auxfile.LabelInfo;
import com.dickimawbooks.texparserlib.auxfile.CiteInfo;
import com.dickimawbooks.texparserlib.auxfile.CrossRefInfo;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.NavigationNode;
import com.dickimawbooks.texjavahelplib.IndexItem;
import com.dickimawbooks.texjavahelplib.SearchItem;
import com.dickimawbooks.texjavahelplib.SearchData;
import com.dickimawbooks.texjavahelplib.TJHIconFile;

public class TJHListener extends L2HConverter
{
   public TJHListener(TeXJavaHelpMk app, Charset outCharset)
   {
      this(app, outCharset, DocumentTargetType.HELPSET);
   }

   public TJHListener(TeXJavaHelpMk app, Charset outCharset, DocumentTargetType type)
   {
      super(app, app.isUseMathJaxOn(), app.getOutDirectory(),
        outCharset, app.isParsePackagesOn(), app.getSplitLevel());

      this.generator = "TeXJavaHelpMk";
      this.documentTargetType = type;
      this.isHtml5 = (type != DocumentTargetType.HELPSET);
      this.isXml = (type == DocumentTargetType.EPUB);
      setSeparateCss(true);

      setSplitUseBaseNamePrefix(app.isSplitBaseNamePrefixOn());
      setSuffix(app.getHtmlSuffix());

      // NB this will need to be off for search to work properly
      setUseEntities(app.isUseHtmlEntitiesOn());
      setSupportUnicodeScript(app.isUseUnicodeSuperSubScriptsOn());

      enableLinkBox(false);
      enableToTopLink(false);

      String extraHead = app.getExtraHeadCode();

      if (extraHead != null)
      {
         addToHead(extraHead);
      }

      File outDir = app.getOutDirectory();

      setNavigationFile(new File(outDir, "navigation."+suffix));

      if (documentTargetType == DocumentTargetType.HELPSET)
      {
         setNavigationXmlFile(new File(outDir, "navigation.xml"));
         setIndexXmlFile(new File(outDir, "index.xml"));
         setSearchXmlFile(new File(outDir, "search.xml"));
         setIconXmlFile(new File(outDir, "icons.xml"));

         String omissions = app.getMessageWithFallback("manual.no-search",
           "and the");

         noSearchWords = omissions.trim().split("\\s+");
      }

      if (documentTargetType != DocumentTargetType.HTML)
      {
         indexData = new HashMap<String,IndexItem>();
      }

      // HTMLDocument only has very limited support

      addCssStyle("div.figure { margin-top: 10pt; }");
      addCssStyle(".locationlist { padding-left: 20pt; }");
      addCssStyle(".spacekey { padding-left: 2ex; padding-right: 2ex; }");
      addCssStyle("div.valuesetting { margin-top: 10pt; }");
      addCssStyle(".subfigure { display: inline-block; padding: 5pt; }");
      addCssStyle(".quadleft { padding-left: 1em; }");
      addCssStyle(".topalign { vertical-align: top; }");
      addCssStyle(".numbered .displaylist { padding-left: 0pt; margin-left: 0pt; }");
      addCssStyle(".numbered .displaylist .displaylist { padding-left: 10pt; margin-left: 10pt; }");
      addCssStyle("div.iconstartpar { float: left; padding-right: 1em;  }");

      addCssStyle("img.framed { border: solid 1pt; }");
      addCssStyle("img.smallicon, img.largeicon { border: solid 1pt; }");
      addCssStyle(".scenebreak { padding-top: .5ex; padding-bottom: .5ex; }");

      addCssStyle(TeXJavaHelpLib.KEYSTROKE_CSS);
      addCssStyle(TeXJavaHelpLib.MENU_CSS);
      addCssStyle(TeXJavaHelpLib.ICON_CSS);

      String locPrefString = app.getMessageIfExists("symbol.location_prefix");

      if (locPrefString != null)
      {
         // HTMLDocument has difficulty with nested span elements.

         String tag = (documentTargetType == DocumentTargetType.HELPSET ? "font" : "span");

         locationPrefix = new HtmlLiteral(
           String.format("<%s class=\"locationprefix\">%s</%s>",
             tag, locPrefString, tag));
      }

      // reduce default set of image extensions


      switch (documentTargetType)
      {
         case HELPSET:
            setImageExtensions(".png", ".jpg", ".jpeg", ".pdf", ".tex");
         break;
         case HTML:
            setImageExtensions(".pdf", ".png", ".jpg", ".jpeg", ".tex");
         break;
         case EPUB:

            addCssStyle("nav.toc ol { list-style-type: none; }");

            createMainDiv = false;

            setImageExtensions(".png", ".jpg", ".jpeg");

            epubContents = new Vector<String>();
            epubContents.add("META-INF/container.xml");
            epubContents.add("content.opf");
         break;
      }
   }

   @Override
   protected void addPredefined()
   {
      super.addPredefined();

      if (documentTargetType == DocumentTargetType.HELPSET)
      {
         putControlSequence(new AtFirstOfTwo("IfHelpSetTF"));
         putControlSequence(new AtFirstOfOne("IfHelpSetT"));
         putControlSequence(new AtGobble("IfHelpSetF"));
      }
      else
      {
         putControlSequence(new AtSecondOfTwo("IfHelpSetTF"));
         putControlSequence(new AtGobble("IfHelpSetT"));
         putControlSequence(new AtFirstOfOne("IfHelpSetF"));
      }

      putControlSequence(new Relax("break"));
      putControlSequence(new Relax("nobreak"));
      putControlSequence(new Relax("clearpage"));
      putControlSequence(new Relax("cleardoublepage"));
      putControlSequence(new GobbleOpt("pagebreak"));
      putControlSequence(new GobbleOpt("nopagebreak"));

      if (documentTargetType == DocumentTargetType.EPUB)
      {
         tocList = new L2HList("tjh@toclist",
           new StartElement("ol", true, true),
           new EndElement("ol", true, true)
         );

         putControlSequence(tocList);

         tocListItem = new L2HItem("tjh@tocitem",
           new StartElement("li", true, true),
           new EndElement("li", true, true),
          false);

         putControlSequence(tocListItem);
      }
   }

   public boolean isWriteOGMarkupOn()
   {
      return documentTargetType == DocumentTargetType.HTML;
   }

   public DocumentTargetType getDocumentTargetType()
   {
      return documentTargetType;
   }

   @Override
   protected void writeMetaData(String title) throws IOException
   {
      // Override any settings in the LaTeX document

      if (keywordsProperty != null)
      {
         setDocumentProperty("Keywords", keywordsProperty);
      }

      if (subjectProperty != null)
      {
         setDocumentProperty("Subject", subjectProperty);
      }

      if (descriptionProperty != null)
      {
         setDocumentProperty("Description", descriptionProperty);
      }

      super.writeMetaData(title);

      File file = currentNode == null ? getRootFile() : currentNode.getFile();

      if (ogUrlPath != null && file != null)
      {
         String url = ogUrlPath + file.getName();

         writeMeta(String.format("property=\"og:url\" content=\"%s\"",
           HtmlTag.encodeAttributeValue(url, true)));

         if (title != null)
         {
            writeMeta(String.format("property=\"og:title\" content=\"%s\"",
              HtmlTag.encodeAttributeValue(title, false)));
         }
      }
   }

   @Override
   protected void writeDocType()
     throws IOException
   {
      setCurrentBlockType(DocumentBlockType.HEAD);

      writeliteralln("<!DOCTYPE html>");
   }

   @Override
   /**   
    * Writes the <code>&lt;html&gt;</code> start tag.
    */   
   protected void writeHtmlStart()
     throws IOException
   {     
      if (documentTargetType == DocumentTargetType.EPUB)
      {  
         writeliteralln(HTML_EPUB_START);
      }     
      else
      {  
         writeliteralln("<html>");
      }     
   }  

   public void setBreadCrumbTrailEnabled(boolean on)
   {
      breadcrumbtrail = on;
   }

   public boolean isBreadCrumbTrailEnabled()
   {
      return breadcrumbtrail;
   }

   public void setMiniTocEnabled(boolean on)
   {
      minitoc = on;
   }

   public boolean isMiniTocEnabled()
   {
      return minitoc;
   }

   public void setMiniTocPreamble(String html)
   {
      minitocPreamble = html;
   }

   public String getMiniTocPreamble()
   {
      return minitocPreamble;
   }

   public void setMiniTocPostamble(String html)
   {
      minitocPostamble = html;
   }

   public String getMiniTocPostamble()
   {
      return minitocPostamble;
   }

   public void setMiniTocDivClass(String cls)
   {
      minitocDivClass = cls;
   }

   public void setMiniTocDivId(String id)
   {
      minitocDivId = id;
   }

   public void setBodyPreamble(String html)
   {
      bodyPreamble = html;
   }

   public void setBodyPostamble(String html)
   {
      bodyPostamble = html;
   }

   public void setOgUrlPath(String url)
   {
      ogUrlPath = url;
   }

   public void setKeywords(String keywords)
   {
      keywordsProperty = keywords;
   }

   public void setSubject(String subject)
   {
      subjectProperty = subject;
   }

   public void setDescription(String description)
   {
      descriptionProperty = description;
   }

   public void setTitle(String title, String titleFileAs)
   {
      titleProperty = title;
      titleFileAsProperty = titleFileAs;
   }

   public void setAuthor(String author, String authorFileAs)
   {
      authorProperty = author;
      authorFileAsProperty = authorFileAs;
   }

   public void setISBN(String isbn)
   {
      setIdentifier("onix:codelist5", "15", isbn);
   }

   public void setIdentifier(String scheme, String type, String id)
   {
      dcIdentifierScheme = scheme;
      dcIdentifierType = type;
      dcIdentifier = id;
   }

   public void setCoverImage(File file)
   {
      coverImage = file;
   }

   @Override
   protected void assignMetaData(TeXObjectList stack)
     throws IOException
   {
      super.assignMetaData(stack);

      if (authorProperty == null || authorProperty.isEmpty())
      {
         ControlSequence cs = getControlSequence("TeXParser@optauthor");

         if (!(cs instanceof Undefined) && !cs.isEmpty())
         {
            parser.startGroup();
            parser.putControlSequence(true, new AtSecondOfTwo("texorpdfstring"));
            authorProperty = stripTags(processToString(cs, stack));
            parser.endGroup();
         }
      }

      if (authorFileAsProperty == null || authorFileAsProperty.isEmpty())
      {
         ControlSequence cs = getControlSequence("TeXParser@optauthor");

         if (!(cs instanceof Undefined) && !cs.isEmpty())
         {
            parser.startGroup();
            parser.putControlSequence(true, new AtSecondOfTwo("texorpdfstring"));
            authorFileAsProperty = stripTags(processToString(cs, stack));
            parser.endGroup();
         }
         else
         {
            authorFileAsProperty = authorProperty;
         }
      }

      if (titleFileAsProperty == null || titleFileAsProperty.isEmpty())
      {
         ControlSequence cs = getControlSequence("TeXParser@opttitle");

         if (!(cs instanceof Undefined) && !cs.isEmpty())
         {
            parser.startGroup();
            parser.putControlSequence(true, new AtSecondOfTwo("texorpdfstring"));
            titleFileAsProperty = stripTags(processToString(cs, stack));
            parser.endGroup();
         }
         else
         {
            titleFileAsProperty = titleProperty;
         }
      }
   }

   public void parseAux(String prefix, File auxFile) throws IOException
   {
      super.parseAux(prefix, auxFile);

      if (getAuxData() == null)
      {
         throw new TeXSyntaxException(getParser(),
           getTeXJavaHelpMk().getMessage("error.no_aux_data"));
      }
   }

   // HTMLDocument doesn't support float so add spacing
   @Override
   public void startFrameBox(FrameBox fbox)
    throws IOException
   {
      super.startFrameBox(fbox);

      if (!isHtml5())
      {
         FloatBoxStyle fs = fbox.getFloatStyle();

         if (fs == FloatBoxStyle.RIGHT)
         {
            writeliteral("<span class=\"qquad\"> &nbsp; &nbsp; </span>");
         }
      }
   }

   @Override
   public void endFrameBox(FrameBox fbox)
    throws IOException
   {
      if (!isHtml5())
      {
         FloatBoxStyle fs = fbox.getFloatStyle();

         if (fs == FloatBoxStyle.LEFT)
         {
            writeliteral("<span class=\"qquad\"> &nbsp; &nbsp; </span>");
         }
      }

      super.endFrameBox(fbox);
   }

   public void setNavigationXmlFile(File file)
   {
      navigationXmlFile = file;
   }

   public void setIndexXmlFile(File file)
   {
      indexXmlFile = file;
   }

   public void setSearchXmlFile(File file)
   {
      searchXmlFile = file;
   }

   public void setIconXmlFile(File file)
   {
      iconXmlFile = file;
   }

   @Override
   protected String getNonHtml5AccSuppTag(String tag)
   {
      return tag.equals(AccSupp.TAG_IMG) || tag.equals(AccSupp.TAG_DFN)
         ? tag : (isXml ? "span" : "font");
   }

   @Override
   protected void footerNav() throws IOException
   {
      if (documentTargetType == DocumentTargetType.HTML)
      {
         super.footerNav();
      }
   }

   public boolean inNavigation()
   {
      return inNavigation;
   }

   public void setRootPagePreMain(String html)
   {
      rootPagePreMainContent = html;
   }

   @Override
   protected void rootPagePreMain(TeXObjectList stack) throws IOException
   {
      inNavigation = true;

      if (rootPagePreMainContent != null)
      {
         writeliteralln(rootPagePreMainContent);
      }

      if (minitoc)
      {
         doMiniToc(stack);
      }

      inNavigation = false;
   }

   protected void writeCoverPage(File file)
    throws IOException
   {
      PrintWriter out = null;

      try
      {
         Charset charset = getHtmlCharset();

         out = new PrintWriter(
           getTeXApp().createBufferedWriter(file.toPath(), charset));

         out.println("<!DOCTYPE html>");
         out.println(HTML_EPUB_START);
         out.println("<head>");
         out.format("<meta charset=\"%s\"/>%n", charset.name());
         out.format("<meta name=\"generator\" content=\"%s\"/>%n",
           getTeXApp().getApplicationName());

         out.print("<title>");
         out.print(coverImageTitle);
         out.println("</title>");

         out.println("<style type=\"text/css\" title=\"override_css\">");
         out.println("@page {padding: 0pt; margin:0pt}");
         out.println("body { text-align: center; padding:0pt; margin: 0pt; }");
         out.println("div { margin: 0pt; padding: 0pt; }");
         out.println("</style>");

         out.println("</head>");
         out.println("<body>");

         out.format("<img src=\"%s\" alt=\"%s\" style=\"height: 100%%\" />%n",
           HtmlTag.encodeAttributeValue(coverImage.getName(), true),
           HtmlTag.encodeAttributeValue(coverImageTitle, false)
         );

         out.println("</body>");
         out.println("</html>");
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   @Override
   protected void startBody(TeXObjectList stack) throws IOException
   {
      super.startBody(stack);

      if (coverImage != null)
      {
         File imageFile = new File(getOutputDir(), coverImage.getName());

         try
         {
            coverHtmlFile = new File(getOutputDir(), coverPageId+"."+getSuffix());

            writeCoverPage(coverHtmlFile);

            getTeXApp().copyFile(coverImage, imageFile);

            addToManifest(new FileData(imageFile, coverImageId, 
              Files.probeContentType(imageFile.toPath())));

            addToManifest(new FileData(coverHtmlFile, coverPageId, 
              MIME_TYPE_XHTML));
         }
         catch (Exception e)
         {
            getTeXApp().error(e);
            coverImage = null;
            coverHtmlFile = null;
         }
      }

      inNavigation = true;

      if (bodyPreamble != null)
      {
         writeliteral(bodyPreamble);
      }

      if (breadcrumbtrail)
      {
         TeXObjectList trail = createBreadcrumbTrail();

         if (trail != null)
         {
            TeXParserUtils.process(trail, parser, stack);
         }
      }

      if (minitoc)
      {
         doMiniToc(stack);
      }

      inNavigation = false;
   }

   protected void doMiniToc(TeXObjectList stack) throws IOException
   {
      TeXObjectList minitoc = createMiniToc();

      if (minitoc != null)
      {
         if (minitocDivClass != null || minitocDivId != null)
         {
            StartElement startElem = new StartElement("div");

            if (minitocDivClass != null)
            {
               startElem.putAttribute("class", minitocDivClass);
            }

            if (minitocDivId != null)
            {
               startElem.putAttribute("id", minitocDivId);
            }

            TeXParserUtils.process(startElem, parser, stack);
         }

         if (minitocPreamble != null)
         {
            writeliteral(minitocPreamble);
         }

         TeXObjectList nav = createDivNav(true);

         if (nav != null)
         {
            TeXParserUtils.process(nav, parser, stack);
         }

         TeXParserUtils.process(minitoc, parser, stack);

         if (minitocPostamble != null)
         {
            writeliteral(minitocPostamble);
         }

         if (minitocDivClass != null || minitocDivId != null)
         {
            writeliteral("</div>");
         }
      }
   }

   @Override
   protected void endBody(TeXObjectList stack) throws IOException
   {
      super.endBody(stack);

      if (bodyPostamble != null)
      {
         writeliteral(bodyPostamble);
      }
   }  

   @Override
   protected void writeNavigationFile(TeXObjectList stack)
    throws IOException
   {
      if (documentTargetType == DocumentTargetType.HELPSET
      ||
          (
            documentTargetType == DocumentTargetType.HTML
             && !minitoc 
           )
         )
      {
         super.writeNavigationFile(stack);
      }
   }

   protected void writeNavigationXmlFile()
     throws IOException
   {
      if (divisionData == null)
      {
         throw new TeXSyntaxException(getParser(), "error.no_division_data");
      }

      DivisionInfo divInfo = divisionData.firstElement();
      DivisionNode divNode = (DivisionNode)divInfo.getSpecial();

      NavigationNode rootNode = NavigationNode.createTree(divNode);

      PrintWriter out = null;

      try
      {
         if (documentTargetType == DocumentTargetType.EPUB)
         {
            File ncx = new File(getOutputDir(), "toc.ncx");

            addToManifest(new FileData(ncx, "ncx", MIME_TYPE_NCX));

            out = new PrintWriter(
              getTeXApp().createBufferedWriter(ncx.toPath(), StandardCharsets.UTF_8));

            writeTocNcx(rootNode, out);
         }
         else if (navigationXmlFile != null)
         {
            out = newNavWriter(navigationXmlFile.toPath());

            addToManifest(new FileData(navigationXmlFile, "nav", MIME_TYPE_XML));

            rootNode.saveTree(out, getHtmlCharset());
         }
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   protected void writeTocNcx(NavigationNode rootNode, PrintWriter out) throws IOException
   {
      Locale locale = getMainLanguage();

      out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      out.format("<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\" xml:lang=\"%s\">%n",
         locale.toLanguageTag());

      out.println("  <head>");

      if (dcIdentifier != null)
      {
         out.format("  <meta content=\"%s\" name=\"dtb:uid\" />%n",
            dcIdentifier);
      }

      out.format((Locale)null,
       "<meta name=\"dtb:depth\" content=\"%d\"/>%n", tocDepth);

      out.println("<meta name=\"dtb:totalPageCount\" content=\"0\" />");
      out.println("<meta name=\"dtb:maxPageNumber\" content=\"0\" />");

      out.println("  </head>");

      String title = titleProperty;

      if (title == null)
      {
         title = getDocumentProperty("Title");
      }

      if (title != null)
      {
         out.println("  <docTitle>");
         out.format("    <text>%s</text>%n", title);
         out.println("  </docTitle>");
      }

      out.println("  <navMap>");

      NavigationNode node = rootNode;
      int order = 1;

      String prevFileName = null;

      if (coverHtmlFile != null)
      {
         out.format((Locale)null,
           "   <navPoint id=\"%s\" playOrder=\"%d\">%n", coverPageId, order);

         out.println("     <navLabel>");

         out.print("      <text>");
         out.print(coverImageTitle);
         out.println("</text>");

         out.println("     </navLabel>");

         out.format("     <content src=\"%s\" />%n", coverHtmlFile.getName());

         out.println("   </navPoint>");

         order++;

         prevFileName = coverHtmlFile.getName();
      }

      while ((node = node.getNextNode()) != null)
      {
         String fileName = node.getFileName();

         if (prevFileName != null && prevFileName.equals(fileName))
         {
            // don't include in play order
            // may be a section that wasn't split
         }
         else
         {
            String id = processAnchorName(node.getKey());

            out.format((Locale)null,
              "   <navPoint id=\"%s\" playOrder=\"%d\">%n", id, order);

            out.println("     <navLabel>");

            out.print("      <text>");
            out.print(node.getTitle());
            out.println("</text>");

            out.println("     </navLabel>");

            out.format("     <content src=\"%s\" />%n", node.getRef());

            out.println("   </navPoint>");

            order++;
         }

         prevFileName = fileName;
      }

      out.println("  </navMap>");

      out.println("</ncx>");
   }

   protected void updateGlossaryEntryIndexItems(String label, TeXObjectList stack)
    throws IOException
   {
      GlossariesSty glossariesSty = tjhSty.getGlossariesSty();

      GlossaryEntry entry = glossariesSty.getEntry(label);

      TeXObject name = entry.get("name");
      String nameStr = "";

      if (name != null)
      {
         nameStr = processToString(name, stack);
      }

      TeXObject desc = entry.get("description");
      String descStr = "";

      if (desc != null)
      {
         descStr = processToString(desc, stack);
      }

      TeXObject shortValue = entry.get("short");
      String shortStr = null;

      if (shortValue != null)
      {
         shortStr = processToString(shortValue, stack);

         if (shortStr.isEmpty() || shortStr.equals(nameStr))
         {
            shortStr = null;
         }
      }

      TeXObject longValue = entry.get("long");
      String longStr = null;

      if (longValue != null)
      {
         longStr = processToString(longValue, stack);

         if (longStr.isEmpty() || longStr.equals(descStr))
         {
            longStr = null;
         }
      }

      if (indexData != null)
      {
         Vector<String> targets = glossariesSty.getTargets(label);

         if (targets != null && !targets.isEmpty())
         {
            for (String target : targets)
            {
               IndexItem indexItem = indexData.get(target);

               if (indexItem != null)
               {
                  indexItem.setName(nameStr);
                  indexItem.setDescription(descStr);
                  indexItem.setShortValue(shortStr);
                  indexItem.setLongValue(longStr);
               }
            }
         }
         else
         {
            String target = parser.expandToString(getControlSequence("glslinkprefix"), stack)
                + label;

            IndexItem indexItem = indexData.get(target);

            if (indexItem == null)
            {
               indexItem = createIndexItem(target, target, null);

               indexData.put(target, indexItem);
            }

            indexItem.setName(nameStr);
            indexItem.setDescription(descStr);
            indexItem.setShortValue(shortStr);
            indexItem.setLongValue(longStr);
         }
      }
   }

   @Override
   protected void createDivisionTree(TeXObjectList stack)
      throws IOException
   {
      if (divisionData == null || divisionData.isEmpty()) return;

      super.createDivisionTree(stack);

      GlossariesSty glossariesSty = tjhSty.getGlossariesSty();

      Set<String> keySet = null;

      if (documentTargetType != DocumentTargetType.EPUB)
      {
         keySet = glossariesSty.getRefLabelGroupsKeySet();
      }

      if (keySet != null)
      {
         int index = divisionData.size();

         for (Iterator<String> it = keySet.iterator(); it.hasNext(); )
         {
            String refLabel = it.next();

            Vector<GlossaryGroup> list = glossariesSty.getGroupsForRefLabel(refLabel);

            if (list != null)
            {
               DivisionNode parentNode = getDivisionNode(refLabel);

               if (parentNode != null && parentNode.getChildCount() == 0)
               {
                  for (GlossaryGroup grp : list)
                  {
                     if (grp.getLevel() == 0 && grp.hasGroupTitle())
                     {
                        String grpLabel = grp.getGroupLabel();
                        File file = parentNode.getFile();
                        String anchor = "#"+HtmlTag.getUriFragment(grpLabel);

                        TeXObjectList location = createString(anchor);

                        DivisionInfo data = new DivisionInfo("glossarygroup",
                         null, grp.getGroupTitle(), grpLabel, location);

                        DivisionNode childNode = new DivisionNode(index, data, parentNode);

                        childNode.setTitle(parser.expandToString(
                          (TeXObject)grp.getGroupTitle().clone(), stack));

                        if (parentNode.getRef().startsWith("#"))
                        {
                           childNode.setRef(anchor);
                        }
                        else
                        {
                           childNode.setRef(parentNode.getRef() + anchor);
                        }

                        index++;
                     }
                  }
               }
            }
         }
      }
   }

   protected void writeIconFile(TeXObjectList stack) throws IOException
   {
      if (iconXmlFile == null) return;

      PrintWriter out = null;

      try
      {
         Charset charset = getHtmlCharset();

         out = new PrintWriter(
           getTeXApp().createBufferedWriter(iconXmlFile.toPath(), charset));

         TJHIconFile.writeXML(iconMap, smallIconMap, largeIconMap, out, charset);
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   protected void writeIndexFile(TeXObjectList stack) throws IOException
   {
      if (indexXmlFile == null || indexData == null) return;

      PrintWriter out = null;

      try
      {
         Charset charset = getHtmlCharset();

         out = new PrintWriter(
           getTeXApp().createBufferedWriter(indexXmlFile.toPath(), charset));

         addToManifest(new FileData(indexXmlFile, MIME_TYPE_XML));

         IndexItem.saveIndex(indexData, out, charset, getTeXJavaHelpMk().getOutDirectory());
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   public void addIndexGroupMap(String glosLabel, String grpLabel, String title)
   {
      if (indexData == null) return;

      IndexItem item = createIndexItem(
        String.format("%s.%08d", glosLabel, ++indexGroupIdx),
        grpLabel, glosLabel+"."+suffix);

      item.setName(title);

      indexData.put(item.getKey(), item);
   }

   @Override
   public TeXObject createAnchor(String anchorName, TeXObject text)
    throws IOException
   {
      TeXObject obj = super.createAnchor(anchorName, text);

      if (indexData != null)
      {
         IndexItem item = indexData.get(anchorName);

         if (currentNode == null)
         {
            if (isSaveDivisionsEnabled())
            {
               getParser().warningMessage("error.no_current_node");
            }
            else if (!noDivisionWarningShown)
            {
               getParser().warningMessage("error.no_current_node_use_split", "--split");
               noDivisionWarningShown = true;
            }
         }
         else
         {
            if (item == null)
            {
               item = createIndexItem(anchorName, anchorName, currentNode.getFile().getName());

               indexData.put(anchorName, item);
            }
            else
            {
               if (item.getTarget() == null)
               {
                  item.setTarget(anchorName);
               }

               item.setFileName(currentNode.getFile().getName());
            }
         }

         if (!text.isEmpty())
         {
            item.setName(processToPlainString((TeXObject)text.clone(), null));
         }
      }

      return obj;
   }

   @Override
   protected void createLinkHook(String anchorName, TeXObject text, String ref)
    throws IOException
   {
      if (indexData == null) return;

      IndexItem item = indexData.get(anchorName);

      String filename = null;

      int idx = ref.indexOf('#');

      if (idx > 0)
      {
         filename = ref.substring(0, idx);
      }

      if (item == null)
      {
         item = createIndexItem(anchorName, anchorName, filename);

         indexData.put(anchorName, item);
      }
      else
      {
         if (item.getFileName() == null && filename != null)
         {
            item.setFileName(filename);
         }

         if (item.getName() == null)
         {
            item.setName(processToPlainString(((TeXObject)text.clone()), null));
         }
      }
   }

   protected String processToPlainString(TeXObject obj, TeXObjectList stack)
   {
      try
      {
         String result = processToString(obj, stack);

         result = result.replaceAll("<[^>]+>", "");

         return result;
      }
      catch (IOException e)
      {
         return "";
      }
   }

   @Override
   public void endDocument(TeXObjectList stack)
   throws IOException
   {
      for (String label : glossariesSty.entryLabelSet())
      {
         updateGlossaryEntryIndexItems(label, stack);
      }

      writeIndexFile(stack);

      writeIconFile(stack);

      super.endDocument(stack);
   }

   @Override
   protected void endDocumentHook() throws IOException
   {
      writeNavigationXmlFile();
      writeSearchFile();

      if (documentTargetType == DocumentTargetType.EPUB)
      {
         writeContentOPF();

         createEpubZip();
      }
   }

   @Override
   public String getTocLabel()
   {
      if (tocLabel == null)
      {
         ControlSequence cs = getParser().getControlSequence("@tjhtoclabel");

         if (cs != null)
         {
            try
            {
               tocLabel = getParser().expandToString(cs, null);
            }
            catch (IOException e)
            {
               getTeXApp().error(e);
            }
         }

         if (tocLabel == null)
         {
            tocLabel = "toc";
         }
      }

      return tocLabel;
   }

   @Override
   public L2HList createTocList()
   {
      if (tocList == null)
      {
         return super.createTocList();
      }
      else
      {
         return tocList;
      }
   }

   @Override
   public L2HItem createTocItem()
   {
      if (tocListItem == null)
      {
         return super.createTocItem();
      }
      else
      {
         return tocListItem;
      }
   }

   @Override
   public StartElement createTocStartElement()
   {
      StartElement elem = super.createTocStartElement();

      if (documentTargetType != DocumentTargetType.HELPSET)
      {
         elem.putAttribute("epub:type", "toc");
      }

      return elem;
   }

   protected void writeContentOPF() throws IOException
   {
      File dir = getOutputDir();

      File opf = new File(dir, "content.opf");

      PrintWriter out = null;

      try
      {
         out = new PrintWriter(
           getTeXApp().createBufferedWriter(opf.toPath(), StandardCharsets.UTF_8));

         out.println("<?xml version='1.0' encoding='utf-8'?>");
         out.println("<package xmlns=\"http://www.idpf.org/2007/opf\"");
         out.println("   unique-identifier=\"pub-id\" version=\"3.0\">");
         out.println("<metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
         out.println("           xmlns:opf=\"http://www.idpf.org/2007/opf\"");
         out.println("           xmlns:dcterms=\"http://purl.org/dc/terms/\"");
         out.println("           xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");

         String title = titleProperty;

         if (title == null)
         {
            title = getDocumentProperty("Title");
         }

         if (title != null)
         {
            out.format("  <dc:title id=\"title\">%s</dc:title>%n", title);

            String titleFileAs = titleFileAsProperty;

            if (titleFileAs == null || titleFileAs.isEmpty())
            {
               titleFileAs = title;
            }

            if (!titleFileAs.equals(title))
            {
               out.format("  <meta refines=\"#title\" property=\"file-as\">%s</meta>%n",
                  titleFileAs);
            }

         }

         String author = authorProperty;

         if (author == null)
         {
            author = getDocumentProperty("author");
         }

         String authorFileAs = authorFileAsProperty;

         if (authorFileAs == null || authorFileAs.isEmpty())
         {
            authorFileAs = author;
         }

         if (author != null)
         {
            out.format("  <dc:creator id=\"author\">%s</dc:creator>%n", author);

            if (!authorFileAs.equals(author))
            {
               out.format("  <meta refines=\"#author\" property=\"file-as\">%s</meta>%n",
                  authorFileAs);
            }

            out.println("  <meta refines=\"#author\" property=\"role\" scheme=\"marc:relators\">aut</meta>");
         }

         Locale locale = getMainLanguage();

         out.format("  <dc:language>%s</dc:language>%n", locale.toLanguageTag());

         if (dcIdentifier != null)
         {
            out.format("  <dc:identifier id=\"pub-id\" >%s</dc:identifier>%n",
               dcIdentifier);

            if (dcIdentifierScheme != null && dcIdentifierType != null)
            {
               out.format(
                 " <meta refines=\"#pub-id\" property=\"identifier-type\" scheme=\"%s\">%s",
                 dcIdentifierScheme, dcIdentifierType
               );
            }

            out.println("</meta>");
         }

         String desc = descriptionProperty;

         if (desc == null)
         {
            desc = getDocumentProperty("Description");
         }

         if (desc != null)
         {
            out.print("  <dc:description>");
            out.print(desc);
            out.println("</dc:description>");
         }

         String subject = subjectProperty;

         if (subject == null)
         {
            subject = getDocumentProperty("Subject");
         }

         if (subject != null)
         {
            out.print("  <dc:subject>");
            out.print(subject);
            out.println("</dc:subject>");
         }

         OffsetDateTime oft = OffsetDateTime.now(ZoneOffset.UTC);
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

         out.format("<meta property=\"dcterms:modified\">%s</meta>%n",
            oft.format(dtf)
          );

         out.println("</metadata>");

         out.println("<manifest>");

         Vector<FileData> manifest = getManifest();

         Vector<String> ncx = new Vector<String>();

         if (coverHtmlFile != null)
         {
            ncx.add(coverPageId);
         }

         Path outPath = getOutputPath();

         for (FileData fileData : manifest)
         {
            File file = fileData.getFile();
            Path path = file.toPath();

            if (outPath != null)
            {
               path = outPath.relativize(path);
            }

            if (addToEpubContents(path))
            {
               String id = processAnchorName(fileData.getId());

               out.format("   <item href=\"%s\" id=\"%s\" media-type=\"%s\" ",
                 path, id, fileData.getMimeType());

               if (id.equals(getTocLabel()))
               {
                  out.print("properties=\"nav\" ");
               }

               out.println("/>");

               if (fileData.getNode() != null)
               {
                  ncx.add(id);
               }
            }
         }

         out.println("</manifest>");

         out.println("<spine toc=\"ncx\">");

         for (String id : ncx)
         {
            out.format("   <itemref idref=\"%s\"/>%n", id);
         }

         out.println("</spine>");
         out.println("</package>");

         out.close();
         out = null;

         dir = new File(dir, "META-INF");

         Files.createDirectories(dir.toPath());

         out = new PrintWriter(
           getTeXApp().createBufferedWriter((new File(dir, "container.xml")).toPath(),
             StandardCharsets.UTF_8));

         out.println("<?xml version=\"1.0\"?>");
         out.println("<container version=\"1.0\"");
         out.println("  xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">");
         out.println("   <rootfiles>");
         out.format("      <rootfile full-path=\"%s\"%n", opf.getName());
         out.println("               media-type=\"application/oebps-package+xml\" />");
         out.println("   </rootfiles>");
         out.println("</container>");
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   protected boolean addToEpubContents(Path path)
   {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < path.getNameCount(); i++)
      {
         if (i > 0)
         {
            builder.append('/');
         }

         builder.append(path.getName(i));
      }

      String name = builder.toString();

      if (!epubContents.contains(name))
      {
         epubContents.add(name);
         return true;
      }

      return false;
   }

   protected void createEpubZip() throws IOException
   {
      Path outPath = getOutputPath();

      FileOutputStream fout = null;
      ZipOutputStream zipOut = null;
      FileInputStream fin = null;

      try
      {
         String base = getRootName();

         if (base == null)
         {
            base = baseName;
         }

         fout = new FileOutputStream(
           new File(outPath.toFile(), base+".epub"));

         zipOut = new ZipOutputStream(fout);

         ZipEntry zipEntry = new ZipEntry("mimetype");
         zipEntry.setMethod(ZipEntry.STORED);

         byte[] byteArray = MIME_TYPE_EPUB.getBytes();

         CRC32 crc = new CRC32();
         crc.update(byteArray);
         zipEntry.setCrc(crc.getValue());
         zipEntry.setSize(byteArray.length);
         zipEntry.setCompressedSize(byteArray.length);
         zipOut.putNextEntry(zipEntry);

         zipOut.write(byteArray, 0, byteArray.length);

         for (String name : epubContents)
         {
            File file = new File(name);

            Path path = outPath.resolve(file.toPath());

            fin = new FileInputStream(path.toFile());

            zipEntry = new ZipEntry(name);
            zipOut.putNextEntry(zipEntry);

            byteArray = new byte[1024];
            int length;

            while ((length = fin.read(byteArray)) >= 0)
            {
               zipOut.write(byteArray, 0, length);
            }

            fin.close();
            fin = null;
         }
      }
      finally
      {
         if (fin != null)
         {
            fin.close();
         }

         if (zipOut != null)
         {
            zipOut.close();
         }

         if (fout != null)
         {
            fout.close();
         }
      }
   }

   @Override
   public String processAnchorName(String anchorName)
   {
      if (isXml())
      {
         return anchorName == null ? anchorName : anchorName.replaceAll("[@:+]", "_");
      }
      else
      {
         return anchorName;
      }
   }

   @Override
   protected LaTeXSty getLaTeXSty(KeyValList options, String styName,
      boolean loadParentOptions, TeXObjectList stack)
   throws IOException
   {
      if (styName.equals("texjavahelp"))
      {
         if (colorSty == null)
         {
            colorSty = new ColorSty(options, styName, this, loadParentOptions);
         } 

         tjhSty = new TeXJavaHelpSty(options, this, loadParentOptions, colorSty);

         return tjhSty;
      }
      else
      {
         return super.getLaTeXSty(options, styName, loadParentOptions, stack);
      }
   }

   @Override
   public L2HImage toImage(String preamble,
    String content, String mimeType, TeXObject alt, String name,
    boolean crop, Path relPath)
   throws IOException
   {
      if (getTeXJavaHelpMk().isConvertImagesOn())
      {
         try
         {
            return getTeXJavaHelpMk().createImage(getParser(), preamble, 
              content, mimeType, alt, name, crop, relPath);
         }
         catch (InterruptedException e)
         {
            throw new TeXSyntaxException(e, getParser(),
              getTeXJavaHelpMk().getMessage("error.interrupted"));
         }
      }
      else
      {
         return null;
      }
   }

   @Override
   public Dimension getImageSize(File file, String mimetype)
   {
      try
      {
         return getTeXJavaHelpMk().getImageFileDimensions(getParser(), file, mimetype);
      }
      catch (IOException | InterruptedException e)
      {
         return null;
      }
   }

   @Override
   protected String getImageTag(String mimeType)
   {
      return "img";
   }

   @Override
   public String getImagePreamble() throws IOException
   {
      String preamble = getTeXJavaHelpMk().getImagePreamble();

      if (preamble == null || preamble.isEmpty())
      {
         preamble = super.getImagePreamble();
      }

      return preamble;
   }

   @Override
   protected L2HImage createImage(Path imagePath, String filename,
     StringBuilder optionsBuilder,
     String type, double scale, int zoom,
     TeXObject alt, String cssClass, String cssStyle)
    throws IOException
   {
      L2HImage image = null;

      Path relPath = null;
      Path parent = imagePath.getParent();

      if (parent != null && !imagePath.isAbsolute())
      {
         relPath = parent;
      }

      if (MIME_TYPE_PDF.equals(type))
      {
         File pdfFile = imagePath.toFile();
         String name = pdfFile.getName();

         int idx = name.lastIndexOf('.');

         if (idx > 0)
         {
            name = name.substring(0, idx);
         }

         Path outPath = getTeXJavaHelpMk().getOutDirectory().toPath();

         if (relPath != null)
         {
            outPath = outPath.resolve(relPath);
         }
         
         File pngFile = new File(outPath.toFile(), name+".png");

         try
         {
            getTeXJavaHelpMk().pdfToImage(pdfFile, pngFile);
         }
         catch (InterruptedException e)
         {
            throw new IOException(
              getTeXJavaHelpMk().getMessage("error.interrupted"),
              e);
         }

         Dimension imageDim = getImageSize(pngFile, MIME_TYPE_PNG);

         int width=0;
         int height=0;

         if (imageDim != null)
         {
            width = imageDim.width;
            height = imageDim.height;
         }

         image = new L2HImage(outPath.relativize(pngFile.toPath()),
             MIME_TYPE_PNG, width, height, null, alt, true);

         if (documentTargetType == DocumentTargetType.HTML)
         {
            try
            {
               File destFile = new File(outPath.toFile(), pdfFile.getName());

               getTeXApp().copyFile(pdfFile, destFile);
               alt = image;

               image = new L2HImage(outPath.relativize(destFile.toPath()),
                 MIME_TYPE_PDF, width, height, null, alt, false);
            }
            catch (InterruptedException e)
            {
               throw new IOException(
                 getTeXJavaHelpMk().getMessage("error.interrupted"),
                 e);
            }
         }
      }
      else if (MIME_TYPE_TEX.equals(type))
      {
         StringBuilder content = new StringBuilder("\\includeimg");

         if (optionsBuilder.length() > 0)
         {
            content.append('[');
            content.append(optionsBuilder);
            content.append(']');
         }

         content.append('{');
         content.append(filename);
         content.append('}');

         if (getParser().isDebugMode(TeXParser.DEBUG_IO))
         {
            getParser().logMessage("Creating image "+content.toString());
         }

         int idx = filename.lastIndexOf('.');

         String name = filename;

         if (idx > 0)
         {
            name = name.substring(0, idx);
         }

         idx = name.lastIndexOf('/');

         if (idx > -1)
         {
            name = name.substring(idx+1);
         }

         image = toImage(getImagePreamble(),
          content.toString(), MIME_TYPE_PNG, alt, name, true, relPath);
      }
      else
      {
         image = super.createImage(imagePath, filename, optionsBuilder,
          type, scale, zoom, alt, cssClass, cssStyle);
      }

      return image;
   }

   public void addImageIconMap(String resourcePath, String name, String suffix,  
     String ext, String pdf, int page)
   throws IOException
   {
      if (iconMap == null)
      {
         iconMap = new HashMap<String,TJHIconFile>();
      }

      TeXPath texPath = new TeXPath(getParser(), pdf, "pdf");
      File pdfFile = texPath.getFile();

      TJHIconFile iconFile = new TJHIconFile(texPath, pdfFile,
        resourcePath, name, suffix, ext, page);

      suffix = "-"+suffix;

      iconMap.put(name+suffix, iconFile);
      iconMap.put(iconFile.toImageFileName(), new TJHIconFile(iconFile));

      if (suffix.equals(getHelpLib().getSmallIconSuffix()))
      {
         if (smallIconMap == null)
         {
            smallIconMap = new HashMap<String,TJHIconFile>();
         }

         smallIconMap.put(name, new TJHIconFile(iconFile));
      }
      else if (suffix.equals(getHelpLib().getLargeIconSuffix()))
      {
         if (largeIconMap == null)
         {
            largeIconMap = new HashMap<String,TJHIconFile>();
         }

         largeIconMap.put(name, new TJHIconFile(iconFile));
      }
   }

   public TJHIconFile getSmallIcon(KeyValList options, TeXObjectList stack,
      String filename, TeXObjectList substack)
   throws IOException
   {
      return getIcon(options, stack, smallIconMap, filename, "small", substack);
   }

   public TJHIconFile getLargeIcon(KeyValList options, TeXObjectList stack,
     String filename, TeXObjectList substack)
   throws IOException
   {
      return getIcon(options, stack, largeIconMap, filename, "large", substack);
   }

   public TJHIconFile getIcon(KeyValList options, TeXObjectList stack,
    String filename, TeXObjectList substack)
   throws IOException
   {
      TJHIconFile icf = getLargeIcon(options, stack, filename, substack);

      if (icf == null)
      {
         icf = getIcon(options, stack, iconMap, filename, null, substack);
      }

      return icf;
   }

   protected TJHIconFile getIcon(KeyValList options, TeXObjectList stack,
     HashMap<String,TJHIconFile> map, String filename, String size,
     TeXObjectList substack)
   throws IOException
   {
      TJHIconFile iconFile = null;

      if (map == null) return iconFile;

      iconFile = map.get(filename);

      if (iconFile == null) return iconFile;

      String iconClass;

      if ("large".equals(size))
      {
         iconClass = "largeresourceicon";
      }
      else if ("small".equals(size))
      {
         iconClass = "smallresourceicon";
      }
      else
      {
         iconClass = "resourceicon";
      }

      TeXObject alt = null;
      String cssClass = null;
      String cssStyle = null;

      String border = null;

      boolean hasOtherOptions = false;

      if (options != null)
      {
         for (Iterator<String> it = options.getOrderedKeyIterator();
          it.hasNext();)
         {
            String key = it.next();
            TeXObject value = options.getValue(key);

            if (key.equals("alt") || key.equals("actualtext"))
            {
               alt = value;
            }
            else if (key.equals("artifact"))
            {
               if (value instanceof MissingValue || value.isEmpty()
                   || value.toString(parser).trim().equals("true"))
               {
                  alt = createStack();
               }
            }
            else if (key.equals("class"))
            {
               cssClass = HtmlTag.encodeAttributeValue(
                  processToString((TeXObject)value.clone(), stack), false);
            }
            else if (key.equals("style"))
            {
               cssStyle = HtmlTag.encodeAttributeValue(
                  processToString((TeXObject)value.clone(), stack), false);
            }
            else if (key.equals("border"))
            {
               border = HtmlTag.encodeAttributeValue(
                  processToString((TeXObject)value.clone(), stack), false);
            }
            else
            {
               hasOtherOptions = true;
            }
         }
      }

      if (!hasOtherOptions)
      {
         VoidElement imgElem = createVoidElement("img");

         imgElem.putAttribute("src",
             iconFile.getResourcePath()+HtmlTag.encodeAttributeValue(filename, true,
             isUseEntitiesEnabled()));

         if (alt != null)
         {
            String altVal = processToString(alt, stack);
            altVal = HtmlTag.encodeAttributeValue(altVal, false,
                  isUseEntitiesEnabled());

            imgElem.putAttribute("alt", altVal);
         }

         if (border != null)
         {
            imgElem.putAttribute("border", border);
         }

         if (cssStyle != null)
         {
            imgElem.putAttribute("style", cssStyle);
         }

         if (cssClass == null)
         {
            imgElem.putAttribute("class", iconClass);
         }
         else
         {
            imgElem.putAttribute("class", iconClass+" "+cssClass);
         }

         iconFile.setReferenced(true);

         substack.add(imgElem);
      }

      return iconFile;
   }

   @Override
   public void includegraphics(TeXObjectList stack,
       KeyValList options, String filename)
    throws IOException
   {
      TeXPath texPath = null;

      TeXObjectList substack = createStack();
      TJHIconFile iconFile = getIcon(options, stack, filename, substack);

      if (iconFile != null)
      {
         if (!substack.isEmpty())
         {
            TeXParserUtils.process(substack, getParser(), stack);

            return;
         }

         texPath = iconFile.getTeXPath();

         if (texPath == null)
         {
            texPath = new TeXPath(getParser(), iconFile.getPdfFile());
         }

         if (options == null)
         {
            options = new KeyValList();
         }

         options.put("page", new UserNumber(iconFile.getPageNumber()));
      }

      if (texPath == null)
      {
         texPath = getImagePath(filename);
      }

      includegraphics(stack, options, texPath, filename);
   }

   public TeXJavaHelpMk getTeXJavaHelpMk()
   {
      return (TeXJavaHelpMk)getTeXApp();
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return getTeXJavaHelpMk().getHelpLib();
   }

   protected IndexItem createIndexItem(String key)
   {
      return createIndexItem(key, null, null);
   }

   protected IndexItem createIndexItem(String key, String target, String filename)
   {
      return new IndexItem(getTeXJavaHelpMk().getHelpLib().getMessageSystem(),
        key, target, filename);
   }

   @Override
   protected Writer newHtmlWriter(Path path)
   throws IOException
   {
      Writer out = super.newHtmlWriter(path);

      if (documentBlockWriter == null)
      {
         documentBlockWriter = new DocumentBlockWriter(out, this);
         addDocumentBlockTypeListener(documentBlockWriter);
      }
      else
      {
         documentBlockWriter.setWriter(out);
      }

      return documentBlockWriter;
   }

   public boolean isSearchEnabled()
   {
      return searchXmlFile != null;
   }

   public boolean isValidSearchWord(String word)
   {
      if (word.length() < MIN_SEARCH_LENGTH || TeXParserUtils.isBlank(word)
          || PATTERN_NO_SEARCH.matcher(word).matches() || noSearchWords == null)
      {
         return false;
      }

      String lc = word.toLowerCase();

      for (String w : noSearchWords)
      {
         if (w.toLowerCase().equals(lc))
         {
            return false;
         }
      }

      return true;
   }

   public void addSearchItem(SearchItem item, StringBuilder context)
   {
      if (searchXmlFile == null) return;

      if (searchData == null)
      {
         searchData = new SearchData();
      }

      searchData.add(item, context);
   }

   protected void writeSearchFile() throws IOException
   {
      if (searchXmlFile != null && searchData != null)
      {
         searchData.write(searchXmlFile.toPath(), getHtmlCharset());

         addToManifest(new FileData(searchXmlFile, MIME_TYPE_XML));
      }
   }

   public TeXObject getLocationPrefix()
   {
      return locationPrefix;
   }

   public void writeScenebreak(TeXObjectList stack, boolean heading) throws IOException
   {
      writeScenebreak(null, stack, heading);
   }

   public void writeScenebreak(TeXObject ornament, TeXObjectList stack, boolean heading)
    throws IOException
   {
      setCurrentBlockType(DocumentBlockType.BLOCK);

      writeliteralln("<div class=\"scenebreak\">");

      if (ornament != null)
      {
         TeXParserUtils.process(ornament, getParser(), stack);
      }

      writeliteralln("</div>");

      if (heading)
      {
         setAfterHeading(true);
      }
   }

   protected File navigationXmlFile;
   protected File indexXmlFile;
   protected File searchXmlFile;
   protected File iconXmlFile;
   protected TeXJavaHelpSty tjhSty;
   protected HashMap<String,IndexItem> indexData;
   protected String[] noSearchWords;

   protected int indexGroupIdx; 

   protected TeXObject locationPrefix;

   protected SearchData searchData;

   protected DocumentBlockWriter documentBlockWriter;
   protected DocumentTargetType documentTargetType = DocumentTargetType.HELPSET;
   protected boolean breadcrumbtrail=false, minitoc=false;
   protected String minitocPreamble = null, minitocPostamble = null,
      minitocDivClass=null, minitocDivId=null;
   protected String bodyPreamble = null, bodyPostamble = null;
   protected String ogUrlPath=null;
   protected String keywordsProperty=null;
   protected String descriptionProperty=null;
   protected String subjectProperty=null;
   protected String titleProperty=null;
   protected String titleFileAsProperty=null;
   protected String authorProperty=null;
   protected String authorFileAsProperty=null;
   protected String dcIdentifier=null;
   protected String dcIdentifierType=null;
   protected String dcIdentifierScheme=null;
   protected File coverImage = null;
   protected File coverHtmlFile = null;
   protected String coverImageId = "cover";
   protected String coverPageId = "coverpage";
   protected String coverImageTitle = "Cover";
   protected String tocLabel;
   protected int tocDepth = 1;// TODO
   protected String rootPagePreMainContent = null;
   protected boolean inNavigation = false;
   protected boolean noDivisionWarningShown=false;

   protected L2HList tocList;
   protected L2HItem tocListItem;

   protected Vector<String> epubContents;

   protected HashMap<String,TJHIconFile> iconMap, smallIconMap, largeIconMap;

   public static final int MIN_SEARCH_LENGTH = 3;

   public static final Pattern PATTERN_NO_SEARCH
     = Pattern.compile("x[0-9a-fA-F]{2,4}|[\\d\\.\\-]+");

   public static final String MIME_TYPE_EPUB = "application/epub+zip";
   public static final String MIME_TYPE_NCX = "application/x-dtbncx+xml";

   public static final String HTML_EPUB_START = 
      "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">";
}
