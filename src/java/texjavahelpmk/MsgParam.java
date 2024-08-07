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
import com.dickimawbooks.texparserlib.latex.latex3.PropertyCommand;

public class MsgParam extends Command
{
   public MsgParam()
   {
      this("msgparam");
   }

   public MsgParam(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new MsgParam(getName());
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();
      int n = popInt(parser, stack);

      TeXObjectList expanded = null;

      PropertyCommand<Integer> propCs
        = PropertyCommand.getPropertyCommand(
             TeXJavaHelpSty.MSG_PARAM_PROP_NAME, parser, false);

      TeXObject obj = propCs.get(Integer.valueOf(n));

      if (obj == null)
      {
         expanded = listener.createStack();
         expanded.add(listener.getControlSequence("meta"));
         Group grp = listener.createGroup("param-"+n);
         expanded.add(grp);
      }
      else
      {
         obj = (TeXObject)obj.clone();

         if (parser.isStack(obj))
         {
            expanded = (TeXObjectList)obj;
         }
         else
         {
            expanded = listener.createStack();

            expanded.add(obj);
         }
      }

      return expanded;
   }

}
