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
package com.dickimawbooks.tjhflattendocsrc;

import java.io.IOException;
import java.nio.file.Path;

import com.dickimawbooks.texparserlib.*;

public class SetOutputDirAction extends AbstractTeXObject
{
   public SetOutputDirAction(FlattenDocSrcListener listener, Path outPath,
      Path imageDestPath)
   {
      this.listener = listener;
      this.outPath = outPath;
      this.imageDestPath = imageDestPath;
   }

   @Override
   public Object clone()
   {
      return new SetOutputDirAction(listener, outPath, imageDestPath);
   }

   @Override
   public boolean isDataObject()
   {
      return true;
   }

   @Override
   public String format()
   {
      return "";
   }

   @Override
   public String purified()
   {
      return "";
   }

   @Override
   public String toString(TeXParser parser)
   {
      return "";
   }

   @Override
   public String toString()
   {
      return String.format("%s[outPath=%s,imageDestPath=%s]",
        getClass().getSimpleName(),
        outPath, imageDestPath);
   }

   @Override
   public TeXObjectList string(TeXParser parser)
   {
      return parser.getListener().createStack();
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      listener.setOutputPath(outPath);
      listener.setImageDestinationPath(imageDestPath);
   }

   @Override
   public void process(TeXParser parser)
    throws IOException
   {
      process(parser, parser);
   }

   FlattenDocSrcListener listener;
   Path outPath;
   Path imageDestPath;
}
