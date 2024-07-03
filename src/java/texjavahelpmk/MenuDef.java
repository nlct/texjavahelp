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
import com.dickimawbooks.texparserlib.latex.*;

import com.dickimawbooks.texparserlib.latex.glossaries.*;
import com.dickimawbooks.texparserlib.latex.nlctdoc.StandaloneDef;
import com.dickimawbooks.texparserlib.latex.nlctdoc.TaggedColourBox;

public class MenuDef extends StandaloneDef
{
   public MenuDef(FrameBoxEnv outerBox, FrameBox rightBox,
     FrameBox noteBox, GlossariesSty sty)
   {
      this("menudef", outerBox, rightBox, noteBox, sty);
   }

   public MenuDef(String name, FrameBoxEnv outerBox, FrameBox rightBox,
     FrameBox noteBox, GlossariesSty sty)
   {
      super(name, outerBox, rightBox, noteBox, sty);
   }

   @Override
   public Object clone()
   {
      return new MenuDef(getName(), outerBox, rightBox, noteBox, getSty());
   }

   @Override
   protected String getDefinitionCsName()
   {     
      return null;
   }

   @Override
   protected void addEntryName(TeXObjectList list, GlsLabel glslabel, TeXParser parser)
   {
      if (parentEntry != null)
      {
         list.add(parser.getListener().getControlSequence("menutrail"));
         list.add(new GlsLabel("glsparentlabel", parentEntry.getLabel(), parentEntry));
         list.add(parser.getListener().getControlSequence("menusep"));
      }

      list.add(parser.getListener().getControlSequence("menufmt"));
      Group grp = parser.getListener().createGroup();
      list.add(grp);

      grp.add(parser.getListener().getControlSequence("glossentryname"));
      grp.add(glslabel);

      TeXObject keystroke = glslabel.getField("keystroke");

      if (keystroke != null)
      {
         list.add(parser.getListener().getSpace());
         list.add(keystroke, true);
      }
   }

   @Override
   protected void preArgHook(TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      popOptArg(parser, stack);
   }

   @Override
   protected void postArgHook(GlsLabel glslabel, TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      note = popOptArg(parser, stack);

      if (outerBox instanceof TaggedColourBox)
      {
         TaggedColourBox taggedBox = (TaggedColourBox)outerBox;

         TeXObjectList title = parser.getListener().createStack();
         title.add(parser.getListener().getControlSequence("icon"));
         title.add(parser.getListener().createGroup("novaluesetting"));
         taggedBox.setTitle(title);
      }

      if (isStar)
      {
         parentEntry = null;
      }
      else
      {
         parentEntry = glslabel.getParent(stack);
      }
   }

   protected TeXObject getNote(GlsLabel glslabel, TeXParser parser)
   {
      return note;
   }

   TeXObject note;
   GlossaryEntry parentEntry;
}
