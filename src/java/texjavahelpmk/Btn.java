/*
    Copyright (C) 2025 Nicola L.C. Talbot
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

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.Dgls;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;

public class Btn extends Dgls
{
   public Btn(GlossariesSty sty)
   {
      this("btn", sty);
   }

   public Btn(String name, GlossariesSty sty)
   {
      super(name, CaseChange.NO_CHANGE, sty);
   }

   @Override
   public Object clone()
   {
      return new Btn(getName(), sty);
   }


   @Override
   protected void preGlsHook(GlsLabel glslabel,
     TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      TeXObject imgName = glslabel.getField("iconimage");

      if (imgName != null)
      {
         TeXParserListener listener = parser.getListener();

         TeXObjectList list = listener.createStack();

         list.add(listener.getControlSequence("includegraphics"));
         list.add(listener.getOther('['));
         list.add(listener.createString("scale"));
         list.add(listener.getOther('='));
         list.add(new TeXFloatingPoint(0.5));
         list.add(listener.getOther(']'));
         Group grp = listener.createGroup();
         grp.add(imgName, true);
         list.add(grp);
         list.add(listener.getControlSequence("btniconsep"));

         TeXParserUtils.process(list, parser, stack);
      }
   }

}
