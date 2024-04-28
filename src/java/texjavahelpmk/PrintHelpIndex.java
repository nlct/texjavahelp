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
import com.dickimawbooks.texparserlib.latex.etoolbox.EtoolboxList;
import com.dickimawbooks.texparserlib.latex.glossaries.*;
import com.dickimawbooks.texparserlib.latex.nlctdoc.PrintIndex;
import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;
import com.dickimawbooks.texparserlib.html.VoidElement;

public class PrintHelpIndex extends PrintIndex
{
   public PrintHelpIndex(GlossariesSty sty)
   {
      this("printindex", "main", "docindex", sty);
   }

   public PrintHelpIndex(String name, String type, String label, GlossariesSty sty)
   {
      super(name, type, label, sty);
   }

   @Override
   public Object clone()
   {
      return new PrintHelpIndex(getName(), glosType, glosLabel, getSty());
   }

   @Override
   protected void addLocationList(GlsLabel glslabel, TeXObjectList content,
     TeXParserListener listener)
   {
      L2HConverter l2h = (L2HConverter)listener;

      TeXObject loc = glslabel.getField("loclist");

      if (loc == null || !(loc instanceof EtoolboxList))
      {
         loc = glslabel.getField("location");

         if (loc != null)
         {
            content.add(listener.getControlSequence("qquad"));
            content.add(loc);
         }
      }
      else if (!loc.isEmpty())
      {
         EtoolboxList list = (EtoolboxList)loc;

         StartElement elem = new StartElement("div");
         elem.putAttribute("class", "locationlist");

         content.add(elem);

         for (int i = 0; i < list.size(); i++)
         {
            if (i > 0)
            {
               content.add(l2h.createVoidElement("br", true));
            }

            content.add(list.get(i));
         }

         content.add(new EndElement("div"));
      }
   }

}
