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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.charset.Charset;
import java.nio.file.Files;

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
import com.dickimawbooks.texparserlib.auxfile.DivisionInfo;
import com.dickimawbooks.texparserlib.auxfile.LabelInfo;
import com.dickimawbooks.texparserlib.auxfile.CiteInfo;
import com.dickimawbooks.texparserlib.auxfile.CrossRefInfo;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.NavigationNode;
import com.dickimawbooks.texjavahelplib.IndexItem;

public class TJHListener extends L2HConverter
{
   public TJHListener(TeXJavaHelpMk app, Charset outCharset)
   {
      super(app, app.isUseMathJaxOn(), app.getOutDirectory(),
        outCharset, app.isParsePackagesOn(), app.getSplitLevel());

      setSeparateCss(true);

      setSplitUseBaseNamePrefix(app.isSplitBaseNamePrefixOn());

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

      indexData = new HashMap<String,IndexItem>();
   }

   public void setNavigationXmlFile(File file)
   {
      navigationXmlFile = file;
   }

   public void setIndexXmlFile(File file)
   {
      indexXmlFile = file;
   }

   @Override
   protected void footerNav() throws IOException
   {
   }

   @Override
   protected void writeNavigationFile(TeXObjectList stack)
     throws IOException
   {
      super.writeNavigationFile(stack);

      DivisionInfo divInfo = divisionData.firstElement();
      DivisionNode divNode = (DivisionNode)divInfo.getSpecial();

      NavigationNode rootNode = NavigationNode.createTree(divNode);

      PrintWriter writer = null;

      try
      {
         writer = new PrintWriter(
           Files.newBufferedWriter(navigationXmlFile.toPath(), getHtmlCharset()));

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

            indexItem.setName(nameStr);
            indexItem.setDescription(descStr);
            indexItem.setShortValue(shortStr);
            indexItem.setLongValue(longStr);
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

   public TeXJavaHelpMk getTeXJavaHelpMk()
   {
      return (TeXJavaHelpMk)getTeXApp();
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

   protected File navigationXmlFile;
   protected File indexXmlFile;
   protected TeXJavaHelpSty tjhSty;
   protected HashMap<String,IndexItem> indexData;
}
