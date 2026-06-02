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

import java.util.Iterator;
import java.util.Vector;

import java.io.*;
import java.net.URL;
         
import java.nio.file.*;
import java.nio.charset.Charset;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex2latex.*;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.graphics.GraphicsPath;

import com.dickimawbooks.texparserlib.auxfile.AuxData;
import com.dickimawbooks.texparserlib.auxfile.AuxCommand;

public class FlattenDocSrcListener extends LaTeX2LaTeX
{
   public FlattenDocSrcListener(FlattenDocSrc app, File outDir, Charset outCharset,
     boolean replaceGraphicsPath)
     throws IOException
   {
      super(app, outDir, outCharset, replaceGraphicsPath);

      setImageExtensions(".pdf", ".png", ".jpg", ".jpeg");
   }

   @Override
   protected void addPredefined()
   {
      super.addPredefined();

      putControlSequence(new GraphicsPath());

      putControlSequence(new IncludeImg());
      putControlSequence(new IncludeImg("includegraphics"));
      putControlSequence(new IncludeImg("pgfimage"));
   }

   public void includeimg(String csname, TeXObject preNameContent, String imgName)
      throws IOException
   {
      String[] grpaths = getGraphicsPaths();

      Path imagePath = null;
      TeXPath path = null;

      try
      {
         if (imgName.contains("."))
         {
            path = new TeXPath(parser, imgName, null, graphicsUseKpsewhich);

            if (imageDestPath == null)
            {
               imagePath = copyImageFile(grpaths, path);
            }
            else
            {
               imagePath =
                  copyImageFile(grpaths, path,
                    imageDestPath.resolve(path.getLeaf()));
            }
         }
         else
         {
            if (csname.equals("includeimg"))
            {
                path = new TeXPath(parser, imgName, "tex",
                    graphicsUseKpsewhich);

                if (imageDestPath == null)
                {
                   imagePath = copyImageFile(grpaths, path);
                }
                else
                {
                   imagePath =
                      copyImageFile(grpaths, path,
                        imageDestPath.resolve(path.getLeaf()));
                }
            }

            for (int i = 0; i < imageExtensions.length && imagePath == null; i++)
            {
                String ext = imageExtensions[i].substring(1);

                path = new TeXPath(parser, imgName, ext, graphicsUseKpsewhich);

                if (imageDestPath == null)
                {
                   imagePath = copyImageFile(grpaths, path);
                }
                else
                {
                   imagePath =
                      copyImageFile(grpaths, path,
                        imageDestPath.resolve(path.getLeaf()));
                }
            }
         }

      }
      catch (InterruptedException e)
      {
         getParser().error(e);
      }

      if (imagePath != null)
      {
         if (isReplaceGraphicsPathEnabled() && imageDestPath == null)
         {
            StringBuilder builder = new StringBuilder();

            Iterator<Path> it = imagePath.iterator();

            while (it.hasNext())
            {
               if (builder.length() > 0)
               {
                  builder.append('/');
               }

               String elemName = it.next().toString();

               if (!it.hasNext() && elemName.endsWith(".tex"))
               {
                  elemName = elemName.substring(0, elemName.length()-4);

                  addTeXImage(path);
               }

               builder.append(elemName);
            }

            imgName = builder.toString();
         }
         else if (path != null && path.getLeaf().toString().endsWith(".tex"))
         {
            addTeXImage(path);
         }
      }

      writeCodePoint(parser.getEscChar());
      write(csname);

      if (preNameContent != null)
      {
         write(preNameContent.toString(parser));
      }

      writeCodePoint(parser.getBgChar());

      write(imgName);

      writeCodePoint(parser.getEgChar());
   }

   public FlattenDocSrc getFlattenDocSrc()
   {
      return (FlattenDocSrc)getTeXApp();
   }

   public void setGraphicsUseKpsewhich(boolean graphicsUseKpsewhich)
   {
      this.graphicsUseKpsewhich = graphicsUseKpsewhich;
   }

   public void addTeXImage(TeXPath imagePath)
   {
      if (imagePath == null)
      {
         throw new NullPointerException();
      }

      if (texImageFiles == null)
      {
         texImageFiles = new Vector<TeXPath>();
      }

      if (!texImageFiles.contains(imagePath))
      {
         texImageFiles.add(imagePath);
      }
   }

   @Override
   public void endDocument(TeXObjectList stack)
     throws IOException
   {

      if (texImageFiles != null)
      {
         parsingImageFile = true;

         for (int i = 0; i < texImageFiles.size(); i++)
         {
            input(texImageFiles.get(i), stack);
         }

         parsingImageFile = false;
      }

      super.endDocument(stack);
   }

   @Override
   public void writeCodePoint(int charCode) throws IOException
   {
      if (!parsingImageFile)
      {
         super.writeCodePoint(charCode);
      }
   }

   @Override
   public void write(char c) throws IOException
   {
      if (!parsingImageFile)
      {
         super.write(c);
      }
   }

   @Override
   public void write(String string) throws IOException
   {
      if (!parsingImageFile)
      {
         super.write(string);
      }
   }

   @Override
   public void writeln(String string) throws IOException
   {
      if (!parsingImageFile)
      {
         super.writeln(string);
      }
   }

   @Override
   public void writeln(char c) throws IOException
   {
      if (!parsingImageFile)
      {
         super.writeln(c);
      }
   }

   @Override
   public void writeln() throws IOException
   {
      if (!parsingImageFile)
      {
         super.writeln();
      }
   }

   boolean graphicsUseKpsewhich = false;

   Vector<TeXPath> texImageFiles;
   boolean parsingImageFile = false;
}

