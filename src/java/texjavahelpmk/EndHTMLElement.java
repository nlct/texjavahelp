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
import com.dickimawbooks.texparserlib.html.EndElement;

/** Ends an HTML element.
 * The optional argument is for LaTeX only.
 * The final argument is the element name.
 */
public class EndHTMLElement extends Command
{
   public EndHTMLElement()
   {
      this("EndHTMLElement");
   }

   public EndHTMLElement(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new EndHTMLElement(getName());
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      popOptArg(parser, stack);

      String elemName = popLabelString(parser, stack);

      TeXObjectList expanded = parser.getListener().createStack();

      expanded.add(new EndElement(elemName));

      return expanded;
   }

   @Override
   public TeXObjectList expandfully(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      return expandonce(parser, stack);
   }

   @Override
   public TeXObjectList expandfully(TeXParser parser)
     throws IOException
   {
      return expandonce(parser, parser);
   }

}
