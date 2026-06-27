/*
    Copyright (C) 2026 Nicola L.C. Talbot
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
import com.dickimawbooks.texparserlib.html.L2HMaketitle;
import com.dickimawbooks.texparserlib.html.HtmlLiteral;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

public class TJHMakeTitle extends L2HMaketitle
{
   public TJHMakeTitle()
   {
      this("maketitle");
   }

   public TJHMakeTitle(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new TJHMakeTitle(getName());
   }

   @Override
   protected TeXObjectList createTitle(TeXParser parser)
    throws IOException
   {
      TeXParserListener listener = parser.getListener();

      TeXObjectList list = super.createTitle(parser);

      StartElement elem;
      ControlSequence cs = listener.getControlSequence("@tjhpublisher");

      if (!cs.isEmpty())
      {
         elem = new StartElement("div");
         elem.putAttribute("class", "publisher");

         list.add(elem);
         list.add(cs);
         list.add(new EndElement("div"));
         list.add(new HtmlLiteral("<!-- end of publisher -->"));
      }

      cs = listener.getControlSequence("@tjheditionblurb");

      if (!cs.isEmpty())
      {
         elem = new StartElement("div");
         elem.putAttribute("class", "edition");

         list.add(elem);
         list.add(cs);
         list.add(new EndElement("div"));
         list.add(new HtmlLiteral("<!-- end of edition -->"));
      }

      cs = listener.getControlSequence("@tjhposttitleblock");

      if (!cs.isEmpty())
      {
         elem = new StartElement("div");
         elem.putAttribute("class", "posttitleblock");

         list.add(elem);
         list.add(cs);
         list.add(new EndElement("div"));
         list.add(new HtmlLiteral("<!-- end of posttitleblock -->"));
      }

      return list;
   }
}
