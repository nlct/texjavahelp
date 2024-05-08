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
import com.dickimawbooks.texparserlib.primitives.IfTrue;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.AtGobble;
import com.dickimawbooks.texparserlib.latex.LaTeXSyntaxException;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.PrintUnsrtGlossary;
import com.dickimawbooks.texparserlib.latex.glossaries.Glossary;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryEntry;

public class ListMenuItems extends ListEntryDescendents
{
   public ListMenuItems(GlossariesSty sty)
   {
      this("listmenuitems", sty);
   }

   public ListMenuItems(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new ListMenuItems(getName(), sty);
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      KeyValList options = sty.popOptKeyValList(stack);
      String label = popLabelString(parser, stack);

      GlossaryEntry entry = sty.getEntry(label);

      if (entry == null)
      {
         throw new LaTeXSyntaxException(parser, 
          GlossariesSty.ENTRY_NOT_DEFINED, label);
      }

      if (options == null || options.get("title") == null)
      {
         TeXObjectList title = listener.createStack();
         title.add(listener.getControlSequence("glsfmttext"));
         title.add(listener.createGroup(label));
         title.add(listener.getSpace());
         title.addAll(listener.createString("Summary"));

         if (options == null)
         {
            options = new KeyValList();
         }

         options.put("title", title);
      }

      int level = entry.getLevel();

      if (options == null || options.get("leveloffset") == null)
      {
         if (options == null)
         {
            options = new KeyValList();
         }

         options.put("leveloffset", new UserNumber(- level - 1));
      }

      Glossary minilist;

      if (sty.isGlossaryDefined("minilist"))
      {
         minilist = sty.getGlossary("minilist");
         minilist.clear();
      }
      else
      {
         minilist = sty.createGlossary("minilist", null, true, true);
      }

      TeXObject childlist = entry.get("childlist");

      if (childlist instanceof DataObjectList)
      {
         for (TeXObject obj : (DataObjectList)childlist)
         {
            String childLabel = parser.expandToString(obj, stack);

            minilist.add(childLabel);

            addChildren(minilist, childLabel, parser, stack);
         }
      }
      else
      {
         throw new LaTeXSyntaxException(parser, "error.no_child_list", label);
      }

      parser.startGroup();

      parser.putControlSequence(true, new IfTrue("ifglsnogroupskip"));

      parser.putControlSequence(true, new GenericCommand(true,
          "@@glossaryseclabel", null, TeXParserUtils.createStack(parser,
          new TeXCsRef("label"), 
          parser.getListener().createGroup(label+"-summary"))));

      parser.putControlSequence(true,
        new TextualContentCommand("glsdefaulttype", "minilist"));

      parser.putControlSequence(true, new AtGobble("glossaryentrynumbers"));

      sty.setGlossaryStyle("menuitems", stack);

      parser.putControlSequence(true, new GenericCommand(true,
       "glossarypreamble", null, TeXParserUtils.createStack(listener,
         listener.getControlSequence("glssummaryadd"),
         listener.createGroup(label))));

      doGlossary(options, parser, stack);

      parser.endGroup();
   }

}
