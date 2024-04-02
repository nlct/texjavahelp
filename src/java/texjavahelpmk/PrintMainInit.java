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
import com.dickimawbooks.texparserlib.latex.LaTeXGenericCommand;

public class PrintMainInit extends ControlSequence
{
   public PrintMainInit()
   {
      this("printmaininit");
   }

   public PrintMainInit(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new PrintMainInit(getName());
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      parser.putControlSequence(true, 
        new LaTeXGenericCommand(true, "printunsrtglossaryentryprocesshook",
         "m",
         TeXParserUtils.createStack(parser,
            new TeXCsRef("filterterms"),
            TeXParserUtils.createGroup(parser, parser.getListener().getParam(1))
        )));
   }

   @Override
   public void process(TeXParser parser)
     throws IOException
   {
      process(parser, parser);
   }

}
