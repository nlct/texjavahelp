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
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

/** 
 * Icons are the start of a paragraph.
 */
public class IconStartPar extends ControlSequence
{
   public IconStartPar()
   {
      this("iconstartpar", "iconstartpar");
   }

   public IconStartPar(String name, String cssClass)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new IconStartPar(getName(), cssClass);
   }

   @Override
   public void process(TeXParser parser)
     throws IOException
   {
      process(parser, parser);
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TJHListener listener = (TJHListener)parser.getListener();

      KeyValList options = TeXParserUtils.popOptKeyValList(parser, stack);
      String[] fileList = popLabelString(parser, stack).split(" *, *");

      boolean addAlt = true;

      if (options == null)
      {
         options = new KeyValList();
      }
      else if (options.get("alt") != null)
      {
         addAlt = false;
      }

      ControlSequence imgCs = listener.getControlSequence("includegraphics");
      ControlSequence iconNameCs = parser.getControlSequence("alticonname");
      String alttag = "";

      if (iconNameCs != null)
      {
         alttag = " " + parser.expandToString(iconNameCs, stack);
      }

      TeXObjectList expanded = listener.createStack();

      StartElement elem = new StartElement("div", true, true);
      elem.putAttribute("class", cssClass);

      expanded.add(elem);

      ControlSequence sep = listener.getControlSequence("iconstartparsep");

      for (int i = 0; i < fileList.length; i++)
      {
         if (i > 0)
         {
            expanded.add(sep);
         }

         if (addAlt)
         {
            options.put("alt", 
             listener.createString(String.format("[%s%s]", fileList[i], alttag)));
         }

         expanded.add(imgCs);
         expanded.add(listener.getOther('['));
         expanded.add(options);
         expanded.add(listener.getOther(']'));
         expanded.add(listener.createGroup(fileList[i]));
      }

      expanded.add(new EndElement("div", true, true));

      TeXParserUtils.process(expanded, parser, stack);
   }

   String cssClass;
}
