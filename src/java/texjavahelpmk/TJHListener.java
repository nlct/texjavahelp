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

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.NavigationNode;

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
   }

   public void setNavigationXmlFile(File file)
   {
      navigationXmlFile = file;
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

   protected void writeIndexFile(TeXObjectList stack) throws IOException
   {
      File file = new File(((TeXJavaHelpMk)getTeXApp()).getOutDirectory(), "index.xml");

      PrintWriter out = null;

      try
      {
         Charset charset = getHtmlCharset();

         out = new PrintWriter(
           Files.newBufferedWriter(file.toPath(), charset));

         out.print("<?xml version=\"1.0\" encoding=\"");
         out.print(charset.name());
         out.println("\" standalone=\"no\"?>");

         GlossariesSty glossariesSty = tjhSty.getGlossariesSty();

         out.println("<index>");

         for (String label : glossariesSty.entryLabelSet())
         {
            out.format("<entry key=\"%s\"", TeXJavaHelpLib.encodeHTML(label, true));

            Vector<String> targets = glossariesSty.getTargets(label);

            if (targets != null && !targets.isEmpty())
            {
               out.format(" target=\"%s\"",
                 TeXJavaHelpLib.encodeHTML(targets.firstElement(), true));
            }

            out.println(">");

            GlossaryEntry entry = glossariesSty.getEntry(label);

            TeXObject name = entry.get("name");
            String nameStr = "";

            if (name != null)
            {
               nameStr = processToString(name, stack);
            }

            if (!nameStr.isEmpty())
            {
               out.format("<name>%s</name>%n", nameStr);
            }

            TeXObject desc = entry.get("description");
            String descStr = "";

            if (desc != null)
            {
               descStr = processToString(desc, stack);
            }

            if (!descStr.isEmpty())
            {
               out.format("<description>%s</description>%n", descStr);
            }

            TeXObject shortValue = entry.get("short");

            if (shortValue != null)
            {
               String str = processToString(shortValue, stack);

               if (!str.isEmpty() && !str.equals(nameStr))
               {
                  out.format("<short>%s</short>%n", str);
               }
            }

            TeXObject longValue = entry.get("long");

            if (longValue != null)
            {
               String str = processToString(longValue, stack);

               if (!str.isEmpty() && !str.equals(descStr))
               {
                  out.format("<long>%s</long>%n", str);
               }
            }

            out.println("</entry>");
         }

         out.println("</index>");
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

   protected File navigationXmlFile;
   protected TeXJavaHelpSty tjhSty;
}
