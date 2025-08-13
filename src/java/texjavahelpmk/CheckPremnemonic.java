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

public class CheckPremnemonic extends Command
{
   public CheckPremnemonic()
   {
      this("checkpremnemonic");
   }

   public CheckPremnemonic(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new CheckPremnemonic(getName());
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      ControlSequence glslabel = listener.getControlSequence("glslabel");

      TeXObjectList expanded = listener.createStack();

      expanded.add(listener.getControlSequence("ifcomponent"));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));
      expanded.add(TeXParserUtils.createGroup(listener, 
         listener.getControlSequence("premnemonic")));

      Group grp = listener.createGroup();
      expanded.add(grp);

      grp.add(listener.getControlSequence("glsifcategory"));
      grp.add(TeXParserUtils.createGroup(listener, glslabel));
      grp.add(listener.createGroup("menu"));

      Group subGrp = listener.createGroup();
      grp.add(subGrp);

      subGrp.add(listener.getControlSequence("ifglshasparent"));
      subGrp.add(TeXParserUtils.createGroup(listener, glslabel));
      subGrp.add(listener.createGroup());
      subGrp.add(TeXParserUtils.createGroup(listener, 
        listener.getControlSequence("premnemonic")));

      grp.add(listener.createGroup());

      return expanded;
   }

}
