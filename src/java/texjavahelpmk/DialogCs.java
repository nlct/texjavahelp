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
import com.dickimawbooks.texparserlib.latex.KeyValList;

public class DialogCs extends Command
{
   public DialogCs()
   {
      this("dialog");
   }

   public DialogCs(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new DialogCs(getName());
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      TeXObjectList expanded = listener.createStack();

      expanded.add(listener.getControlSequence("dgls"));

      KeyValList opt = TeXParserUtils.popOptKeyValList(parser, stack);
      String label = popLabelString(parser, stack);

      if (opt != null)
      {
         expanded.add(listener.getOther('['));
         expanded.add(opt);
         expanded.add(listener.getOther(']'));
      }

      expanded.add(listener.createGroup(label+".title"));

      return expanded;
   }

}
