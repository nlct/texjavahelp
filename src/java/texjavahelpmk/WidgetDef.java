/*
    Copyright (C) 2024-2025 Nicola L.C. Talbot
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

public class WidgetDef extends StandaloneDef
{
   public WidgetDef(FrameBoxEnv outerBox, FrameBox rightBox,
     FrameBox noteBox, GlossariesSty sty)
   {
      this("widgetdef", outerBox, rightBox, noteBox, sty);
   }

   public WidgetDef(String name, FrameBoxEnv outerBox, FrameBox rightBox,
     FrameBox noteBox, GlossariesSty sty)
   {
      super(name, outerBox, rightBox, noteBox, sty);
   }

   @Override
   public Object clone()
   {
      return new WidgetDef(getName(), outerBox, rightBox, noteBox, getSty());
   }

   @Override
   protected void postArgHook(GlsLabel glslabel, TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      note = popOptArg(parser, stack);

      if (outerBox instanceof TaggedColourBox)
      {
         TaggedColourBox taggedBox = (TaggedColourBox)outerBox;

         String iconname = "valuesetting";

         TeXObject initValObj = glslabel.getField("initvalue");

         if (initValObj != null)
         {
            String initVal = initValObj.toString(parser).trim();

            if (initVal.equals("true") || initVal.equals("on"))
            {
               iconname = "toggleonsetting";
            }
            else if (initVal.equals("false") || initVal.equals("off"))
            {
               iconname = "toggleoffsetting";
            }
         }

         TeXObjectList title = parser.getListener().createStack();
         title.add(parser.getListener().getControlSequence("icon"));
         title.add(parser.getListener().createGroup(iconname));
         taggedBox.setTitle(title);
      }
   }

   protected TeXObject getNote(GlsLabel glslabel, TeXParser parser)
   {
      return note;
   }

   @Override
   protected void addPreEntryName(TeXObjectList list, GlsLabel glslabel, TeXParser parser)        
   {  
      TeXObject imgName = glslabel.getField("iconimage");

      if (imgName != null)
      {
         list.add(parser.getListener().getControlSequence("includegraphics"));
         Group grp = parser.getListener().createGroup();
         grp.add(imgName, true);
         list.add(grp);
         list.add(parser.getListener().getControlSequence("menuiconsep"));
      }
   }

   TeXObject note;
}
