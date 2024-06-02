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
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

public class SeeAlsoRefs extends ControlSequence
{
   public SeeAlsoRefs()
   {
      this("seealsorefs");
   }

   public SeeAlsoRefs(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new SeeAlsoRefs(getName());
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

      String[] labels = popLabelString(parser, stack).trim().split(" *, *");

      TeXObjectList expanded = listener.createStack();

      StartElement elem = new StartElement("div", true);
      elem.putAttribute("class", "seealso");

      expanded.add(elem);

      expanded.add(listener.getControlSequence("MFUsentencecase"));
      expanded.add(listener.getControlSequence("seealsoname"));

      if (labels.length > 1)
      {
         expanded.add(listener.getControlSequence("multiseealsosep"));
         expanded.add(new StartElement("ul"));

         for (String label : labels)
         {
            expanded.add(new StartElement("li"));
            expanded.add(listener.getControlSequence("ref"));
            expanded.add(listener.createGroup(label));
            expanded.add(listener.getSpace());
            expanded.add(listener.getControlSequence("nameref"));
            expanded.add(listener.createGroup(label));
            expanded.add(new EndElement("li"));
         }

         expanded.add(new EndElement("ul"));
      }
      else if (labels.length == 1)
      {
         expanded.add(listener.getSpace());
         expanded.add(listener.getControlSequence("ref"));
         expanded.add(listener.createGroup(labels[0]));
         expanded.add(listener.getSpace());
         expanded.add(listener.getControlSequence("nameref"));
         expanded.add(listener.createGroup(labels[0]));
         expanded.add(listener.getOther('.'));
      }

      expanded.add(new EndElement("div"));

      TeXParserUtils.process(expanded, parser, stack);
   }

}
