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

public class TeXJavaHelpLoadResources extends Command
{
   public TeXJavaHelpLoadResources()
   {
      this("TeXJavaHelpLoadResources");
   }

   public TeXJavaHelpLoadResources(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new TeXJavaHelpLoadResources(getName());
   }

   @Override
   public boolean canExpand()
   {
      return true;
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      // arguments can be ignored

      TeXObject optArg = popOptArg(parser, stack);
      TeXObject bibListArg = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      expanded.add(listener.getControlSequence("GlsXtrLoadResources"));
      expanded.add(listener.getControlSequence("GlsXtrLoadResources"));

      return expanded;
   }

}
