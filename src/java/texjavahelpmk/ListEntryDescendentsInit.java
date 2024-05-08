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

public class ListEntryDescendentsInit extends ControlSequence
{
   public ListEntryDescendentsInit()
   {
      this("listentrydescendentsinit");
   }

   public ListEntryDescendentsInit(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new ListEntryDescendentsInit(getName());
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      parser.putControlSequence(true, new GenericCommand(true, 
        "glsxtrpostnameapplication", null, new TeXCsRef("postclihook")));
      parser.putControlSequence(true, new GenericCommand(true, 
        "glsxtrpostnameswitch", null, new TeXCsRef("postswitchhook")));
      parser.putControlSequence(true, new GenericCommand(true, 
        "glsxtrpostnameoption", null, new TeXCsRef("postoptionhook")));
      parser.putControlSequence(true, new GenericCommand(true, 
        "glsxtrpostnamemenu", null, new TeXCsRef("postmenuhook")));
   }

   @Override
   public void process(TeXParser parser)
     throws IOException
   {
      process(parser, parser);
   }
}
