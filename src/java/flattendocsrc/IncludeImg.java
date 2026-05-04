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

package com.dickimawbooks.flattendocsrc;

import java.io.IOException;

import com.dickimawbooks.texparserlib.*;

import com.dickimawbooks.texparserlib.latex.KeyValList;

import com.dickimawbooks.texparserlib.latex2latex.L2LControlSequence;

public class IncludeImg extends L2LControlSequence
{
   public IncludeImg()
   {
      this("includeimg");
   }

   public IncludeImg(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new IncludeImg(getName());
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
      throws IOException
   {
      FlattenDocSrcListener listener = (FlattenDocSrcListener)parser.getListener();

      TeXObjectList substack = parser.getListener().createStack();

      TeXObject obj = stack.peekStack(
         TeXObjectList.POP_RETAIN_IGNOREABLES);

      while (obj instanceof Ignoreable || obj instanceof WhiteSpace)
      {
         obj = stack.pop();

         substack.add(obj);

         obj = stack.peekStack(
            TeXObjectList.POP_RETAIN_IGNOREABLES);
      }

      TeXObject options = TeXParserUtils.popOptArg(
        parser, stack, '[', ']');

      if (options != null)
      {
         substack.add(listener.getOther('['));
         substack.add(options);
         substack.add(listener.getOther(']'));
      }

      obj = stack.peekStack(
         TeXObjectList.POP_RETAIN_IGNOREABLES);

      while (obj instanceof Ignoreable || obj instanceof WhiteSpace)
      {
         obj = stack.pop();

         substack.add(obj);

         obj = stack.peekStack(
            TeXObjectList.POP_RETAIN_IGNOREABLES);
      }

      String imgName = popLabelString(parser, stack);

      listener.includeimg(getName(), substack, imgName);
   }

   @Override
   public void process(TeXParser parser)
      throws IOException
   {
      process(parser, parser);
   }
}
