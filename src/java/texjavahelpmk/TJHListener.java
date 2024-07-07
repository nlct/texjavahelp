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

import java.util.regex.Pattern;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import java.awt.Dimension;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.LaTeXSty;
import com.dickimawbooks.texparserlib.latex.color.ColorSty;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryEntry;

import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.L2HImage;
import com.dickimawbooks.texparserlib.html.DivisionNode;
import com.dickimawbooks.texparserlib.html.HtmlTag;
import com.dickimawbooks.texparserlib.auxfile.DivisionInfo;
import com.dickimawbooks.texparserlib.auxfile.LabelInfo;
import com.dickimawbooks.texparserlib.auxfile.CiteInfo;
import com.dickimawbooks.texparserlib.auxfile.CrossRefInfo;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.NavigationNode;
import com.dickimawbooks.texjavahelplib.IndexItem;
import com.dickimawbooks.texjavahelplib.SearchItem;
import com.dickimawbooks.texjavahelplib.SearchData;

public class TJHListener extends L2HConverter
{
   public TJHListener(TeXJavaHelpMk app, Charset outCharset)
   {
      this(app, outCharset, false);
   }

   public TJHListener(TeXJavaHelpMk app, Charset outCharset, boolean isHtml5)
   {
      super(app, app.isUseMathJaxOn(), app.getOutDirectory(),
        outCharset, app.isParsePackagesOn(), app.getSplitLevel());

      this.isHtml5 = isHtml5;
      setSeparateCss(true);

      setSplitUseBaseNamePrefix(app.isSplitBaseNamePrefixOn());

      // NB this will need to be off for search to work properly
      setUseEntities(app.isUseHtmlEntitiesOn());

      enableLinkBox(false);
      enableToTopLink(false);

      String extraHead = app.getExtraHeadCode();

      if (extraHead != null)
      {
         addToHead(extraHead);
      }

      File outDir = app.getOutDirectory();

      setNavigationFile(new File(outDir, "navigation."+suffix));
      setNavigationXmlFile(new File(outDir, "navigation.xml"));
      setIndexXmlFile(new File(outDir, "index.xml"));
      setSearchXmlFile(new File(outDir, "search.xml"));

      indexData = new HashMap<String,IndexItem>();

      String omissions = app.getMessageWithFallback("manual.no-search",
        "and the");

      noSearchWords = omissions.trim().split("\\s+");

      // HTMLDocument only has very limited support

      addCssStyle("div.figure { margin-top: 10pt; }");
      addCssStyle(".locationlist { padding-left: 20pt; }");
      addCssStyle(".spacekey { padding-left: 2ex; padding-right: 2ex; }");
      addCssStyle("div.valuesetting { margin-top: 10pt; }");
      addCssStyle(".subfigure { display: inline-block; padding: 5pt; }");

      addCssStyle(TeXJavaHelpLib.KEYSTROKE_CSS);
      addCssStyle(TeXJavaHelpLib.MENU_CSS);
      addCssStyle(TeXJavaHelpLib.ICON_CSS);

      String locPrefString = app.getMessageIfExists("symbol.location_prefix");

      if (locPrefString != null)
      {
         // HTMLDocument has difficulty with nested span elements.

         String tag = isHtml5 ? "span" : "font";

         locationPrefix = new HtmlTag(
           String.format("<%s class=\"locationprefix\">%s</%s>",
             tag, locPrefString, tag));
      }

      setImageExtensions("png", "PNG", "jpg", "JPG", "jpeg", "JPEG",
        "gif", "GIF", "pdf", "PDF", "tex");
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

   @Override
   public boolean isHtml5()
   {
      return isHtml5;
   }

   @Override
   protected String getNonHtml5AccSuppTag(String tag)
   {
      return tag.equals(AccSupp.TAG_IMG) ? tag : "font";
   }

   @Override
   protected void footerNav() throws IOException
   {
   }

   protected void writeNavigationXmlFile()
     throws IOException
   {
      DivisionInfo divInfo = divisionData.firstElement();
      DivisionNode divNode = (DivisionNode)divInfo.getSpecial();

      NavigationNode rootNode = NavigationNode.createTree(divNode);

      PrintWriter writer = null;

      try
      {
         writer = newNavWriter(navigationXmlFile.toPath());

         rootNode.saveTree(writer, getHtmlCharset());
      }
      finally
      {
         if (writer != null)
         {
            writer.close();
         }
      }
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

   protected void writeIndexFile(TeXObjectList stack) throws IOException
   {
      for (String label : glossariesSty.entryLabelSet())
      {
         updateGlossaryEntryIndexItems(label, stack);
      }

      PrintWriter out = null;

      try
      {
         Charset charset = getHtmlCharset();

         out = new PrintWriter(
           Files.newBufferedWriter(indexXmlFile.toPath(), charset));

         IndexItem.saveIndex(indexData, out, charset);
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

      IndexItem item = indexData.get(anchorName);

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

      if (!text.isEmpty())
      {
         item.setName(processToPlainString((TeXObject)text.clone(), null));
      }

      return obj;
   }

   @Override
   protected void createLinkHook(String anchorName, TeXObject text, String ref)
    throws IOException
   {
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
      writeIndexFile(stack);

      super.endDocument(stack);
   }

   @Override
   protected void endDocumentHook() throws IOException
   {
      writeNavigationXmlFile();
      writeSearchFile();
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
    boolean crop)
   throws IOException
   {
      if (getTeXJavaHelpMk().isConvertImagesOn())
      {
         try
         {
            return getTeXJavaHelpMk().createImage(getParser(), preamble, 
              content, mimeType, alt, name, crop);
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

      if (MIME_TYPE_PDF.equals(type))
      {
         File pdfFile = imagePath.toFile();
         String name = pdfFile.getName();

         int idx = name.lastIndexOf('.');

         if (idx > 0)
         {
            name = name.substring(0, idx);
         }

         File pngFile;

         try
         {
            pngFile = getTeXJavaHelpMk().pdfToImage(pdfFile, name, "png");
         }
         catch (InterruptedException e)
         {
            throw new IOException(
              getTeXJavaHelpMk().getMessage("error.interrupted"),
              e);
         }

         Path pngPath = pngFile.toPath();
         
         Dimension imageDim = getImageSize(pngFile, MIME_TYPE_PNG);

         File outDir = getTeXJavaHelpMk().getOutDirectory();
            
         Path destPath = (new File(outDir, name)).toPath();
         
         Files.copy(pngPath, destPath);

         int width=0;
         int height=0;

         if (imageDim != null)
         {
            width = imageDim.width;
            height = imageDim.height;
         }

         image = new L2HImage(outDir.toPath().relativize(destPath),
          MIME_TYPE_PNG, width, height, name, alt);
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
          content.toString(), MIME_TYPE_PNG, alt, name, true);
      }
      else
      {
         image = super.createImage(imagePath, filename, optionsBuilder,
          type, scale, zoom, alt, cssClass, cssStyle);
      }

      return image;
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

   public boolean isValidSearchWord(String word)
   {
      if (word.length() < MIN_SEARCH_LENGTH || TeXParserUtils.isBlank(word)
          || PATTERN_NO_SEARCH.matcher(word).matches())
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
      if (searchData == null)
      {
         searchData = new SearchData();
      }

      searchData.add(item, context);
   }

   protected void writeSearchFile() throws IOException
   {
      if (searchData != null)
      {
         searchData.write(searchXmlFile.toPath(), getHtmlCharset());
      }
   }

   public TeXObject getLocationPrefix()
   {
      return locationPrefix;
   }

   protected File navigationXmlFile;
   protected File indexXmlFile;
   protected File searchXmlFile;
   protected TeXJavaHelpSty tjhSty;
   protected HashMap<String,IndexItem> indexData;
   protected String[] noSearchWords;

   protected int indexGroupIdx; 

   protected TeXObject locationPrefix;

   protected SearchData searchData;

   protected DocumentBlockWriter documentBlockWriter;
   protected boolean isHtml5=false;

   public static final int MIN_SEARCH_LENGTH = 3;

   public static final Pattern PATTERN_NO_SEARCH
     = Pattern.compile("x[0-9a-fA-F]{2,4}|[\\d\\.\\-]+");
}
