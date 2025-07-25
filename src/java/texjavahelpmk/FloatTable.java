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
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

/** Analogous to FloatFig but for tables.
 * This is more for consistency as the label is usually placed at
 * the top.
 */
public class FloatTable extends ControlSequence
{
   public FloatTable()
   {
      this("FloatTable");
   }

   public FloatTable(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new FloatTable(getName());
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

      popOptArg(parser, stack);
      String label = popLabelString(parser, stack);
      TeXObject content = popArg(parser, stack);
      TeXObject lot = popOptArg(parser, stack);
      TeXObject caption = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      StartElement elem = new StartElement("div", true, true);

      elem.putAttribute("id", label);

      expanded.add(elem);

      listener.stepcounter("table");

      ControlSequence cs = parser.getControlSequence("fnum@table");

      TeXObject captionPrefix;
      TeXObjectList labelText;

      if (cs == null)
      {
         ControlSequence tabName = parser.getControlSequence("tablecaptionname");

         if (tabName == null)
         {
            tabName = listener.getControlSequence("tablename");
         }

         ControlSequence theTable = listener.getControlSequence("thetable");

         labelText = TeXParserUtils.createStack(listener,
           tabName, listener.getSpace(), theTable);
         captionPrefix =  TeXParserUtils.createStack(listener,
           tabName, listener.getSpace(), theTable);
      }
      else
      {
         labelText = TeXParserUtils.createStack(listener, cs);
         captionPrefix = cs;
      }

      if (lot == null)
      {
         if (!caption.isEmpty())
         {
            labelText.add(listener.getSpace());
            labelText.add((TeXObject)caption.clone(), true);
         }
      }
      else if (!lot.isEmpty())
      {
         labelText.add(listener.getSpace());
         labelText.add(lot, true);
      }

      listener.provideLabel(label, labelText);

      elem = new StartElement("div", true, true);
      elem.putAttribute("class", "caption");

      expanded.add(elem);

      expanded.add(listener.getControlSequence("@makecaption"));

      expanded.add(TeXParserUtils.createGroup(listener, captionPrefix));

      expanded.add(TeXParserUtils.createGroup(listener, caption));

      expanded.add(new EndElement("div", true, true));// caption div

      expanded.add(listener.getControlSequence("posttablecaption"));
      expanded.add(content, true);

      expanded.add(new EndElement("div", true, true));// table div

      TeXParserUtils.process(expanded, parser, stack);
   }

}
