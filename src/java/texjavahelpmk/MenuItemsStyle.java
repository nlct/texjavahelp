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

import java.io.IOException;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.LaTeXGenericCommand;
import com.dickimawbooks.texparserlib.latex.AtGobble;
import com.dickimawbooks.texparserlib.latex.Overwrite;

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;

import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

public class MenuItemsStyle extends ControlSequence
{
   public MenuItemsStyle(GlossariesSty sty)
   {
      this("menuitems", null, sty);
   }

   public MenuItemsStyle(String styleName, String cssClass, GlossariesSty sty)
   {
      this("@glsstyle@"+styleName, styleName, cssClass, sty);
   }

   public MenuItemsStyle(String csname, String styleName, String cssClass,
    GlossariesSty sty)
   {
      super(csname);
      this.styleName = styleName;
      this.cssClass = cssClass;
      this.sty = sty;
   }

   public Object clone()
   {
      return new MenuItemsStyle(getName(), styleName, cssClass, sty);
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      L2HConverter listener = (L2HConverter)parser.getListener();

      TeXObjectList beginCode = listener.createStack();

      StartElement startElem = new StartElement("dl", true);

      if (cssClass != null)
      {
         startElem.putAttribute("class", cssClass+" "+styleName);
      }
      else
      {
         startElem.putAttribute("class", styleName);
      }

      beginCode.add(startElem);

      TeXObjectList endCode = listener.createStack();

      endCode.add(new EndElement("dl", true));

      listener.newenvironment(Overwrite.ALLOW, "renewenvironment",
       "theglossary", 0, null, beginCode, endCode);

      listener.putControlSequence(true,
           new GenericCommand(true, "glossaryheader"));

      listener.putControlSequence(true, new AtGobble("glsgroupheading"));

      listener.putControlSequence(true,
        new GenericCommand(true, "glsgroupskip"));

      listener.putControlSequence(true, new MenuItemGlossEntry(sty));

      TeXObjectList def = listener.createStack();

      def.add(new TeXCsRef("subglossentry"));
      def.add(UserNumber.ZERO);

      def.add(TeXParserUtils.createGroup(listener, listener.getParam(1)));
      def.add(TeXParserUtils.createGroup(listener, listener.getParam(2)));

      listener.putControlSequence(true,
        new LaTeXGenericCommand(true, "glossentry", "mm", def));
   }

   @Override
   public void process(TeXParser parser) throws IOException
   {
      process(parser, parser);
   }

   protected String styleName, cssClass;
   protected GlossariesSty sty;
}

class MenuItemGlossEntry extends AbstractGlsCommand
{
   protected MenuItemGlossEntry(GlossariesSty sty)
   {
      this("subglossentry", sty);
   }

   protected MenuItemGlossEntry(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new MenuItemGlossEntry(getName(), sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      L2HConverter listener = (L2HConverter)parser.getListener();

      // This style has a flat hierarchy but allows styling per level
      int level = TeXParserUtils.popInt(parser, stack);
      GlsLabel glslabel = popEntryLabel(parser, stack);
      popArg(parser, stack);// discard location list

      TeXObjectList expanded = listener.createStack();

      StartElement startElem = new StartElement("dt", true);
      startElem.putAttribute("class", "menulist"+level);

      expanded.add(startElem);

      expanded.add(listener.getControlSequence("glssummaryadd"));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));

      expanded.add(listener.getControlSequence("menutrail"));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));

      TeXObject keystroke = glslabel.getField("keystroke");

      if (keystroke != null)
      {
         expanded.add(listener.getControlSequence("qquad"));
         expanded.add(keystroke, true);
      }

      expanded.add(new EndElement("dt", true));

      TeXObject desc = glslabel.getField("description");

      if (desc == null)
      {
         desc = glslabel.getField("tooltip");
      }

      if (desc != null && !desc.isEmpty())
      {
         expanded.add(new StartElement("dd", true));

         if (parser.isStack(desc) 
              && ((TeXObjectList)desc).firstElement() instanceof Letter)
         {
            TeXObjectList descList = (TeXObjectList)desc;

            CharObject chobj = (CharObject)descList.firstElement();
            int cp = chobj.getCharCode();

            if (Character.isLowerCase(cp))
            {
               expanded.add(listener.getLetter(Character.toTitleCase(cp)));

               for (int i = 1; i < descList.size(); i++)
               {
                  expanded.add(descList.get(i));
               }
            }
            else
            {
               expanded.add(desc, true);
            }
         }
         else
         {
            expanded.add(listener.getControlSequence("makefirstuc"));
            Group grp = listener.createGroup();
            grp.add(desc, true);
            expanded.add(grp);
         }

         expanded.add(listener.getControlSequence("glspostdescription"));

         expanded.add(new EndElement("dd", true));
      }

      return expanded;
   }
}
