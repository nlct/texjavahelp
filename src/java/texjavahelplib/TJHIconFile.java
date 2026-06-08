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
package com.dickimawbooks.texjavahelplib;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.charset.Charset;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import com.dickimawbooks.texparserlib.TeXPath;

public class TJHIconFile
{
   public TJHIconFile(TeXPath path, 
     String resourcePath, String name, String suffix, String ext)
   {
      this(path, path.getFile(), resourcePath, name, suffix, ext, 0);
   }

   public TJHIconFile(TeXPath path, File pdfFile,
     String resourcePath, String name, String suffix, String ext)
   {
      this(path, pdfFile, resourcePath, name, suffix, ext, 0);
   }

   public TJHIconFile(TeXPath path, File pdfFile,
     String resourcePath, String name, String suffix, String ext, int pageNum)
   {
      this.texPath = path;
      this.pdfFile = pdfFile;
      this.resourcePath = resourcePath;
      this.name = name;
      this.suffix = suffix;
      this.ext = ext;
      this.pageNum = pageNum;
   }

   public String formatTeXPath()
   {
      return texPath.getTeXPath(false);
   }

   public String formatMap()
   {
      return String.format((Locale)null,
               "\\tjhmapiconimage{%s}{%s}{%s}{%s}{%s}{%d}",
               resourcePath, name, suffix, ext, pdfFile.getName(), pageNum);
   }

   public void setPageNumber(int pageNum)
   {
      this.pageNum = pageNum;
   }

   public int getPageNumber()
   {
      return pageNum;
   }

   public TeXPath getTeXPath()
   {
      return texPath;
   }

   public File getPdfFile()
   {
      return pdfFile;
   }

   public String getResourcePath()
   {
      return resourcePath;
   }

   public String getName()
   {
      return name;
   }

   public String getSuffix()
   {
      return suffix;
   }

   public String getExtension()
   {
      return ext;
   }

   public String toImageFileName()
   {
      return name+"-"+suffix+"."+ext;
   }

   public boolean isReferenced()
   {
      return referenced;
   }

   public void setReferenced(boolean referenced)
   {
      this.referenced = referenced;
   }

   protected void writeXML(PrintWriter out)
   throws IOException
   {
      writeXML(out, null);
   }

   protected void writeXML(PrintWriter out, String size)
   throws IOException
   {
      if (referenced)
      {
         if (size == null)
         {
            out.format("<icon name=\"%s\" path=\"%s\" filename=\"%s\" ext=\"%s\" />%n",
              name, resourcePath, toImageFileName(), ext);
         }
         else
         {
            out.format("<icon name=\"%s\" path=\"%s\" filename=\"%s\" ext=\"%s\" size=\"%s\" />%n",
              name, resourcePath, toImageFileName(), ext, size);
         }
      }
   }

   public static void writeXML(HashMap<String,TJHIconFile> iconMap,
     HashMap<String,TJHIconFile> smallIconMap,
     HashMap<String,TJHIconFile> largeIconMap,
     PrintWriter out, Charset charset)
   throws IOException
   {
      out.print("<?xml version=\"1.0\" encoding=\"");
      out.print(charset.name());
      out.println("\" standalone=\"no\"?>");

      out.println("<icons>");

      if (iconMap != null)
      {
         for (Iterator<String> it = iconMap.keySet().iterator(); it.hasNext(); )
         {
            String key = it.next();
            TJHIconFile icf = iconMap.get(key);

            icf.writeXML(out);
         }
      }

      if (smallIconMap != null)
      {
         for (Iterator<String> it = smallIconMap.keySet().iterator(); it.hasNext(); )
         {
            String key = it.next();
            TJHIconFile icf = smallIconMap.get(key);

            icf.writeXML(out, "small");
         }
      }

      if (largeIconMap != null)
      {
         for (Iterator<String> it = largeIconMap.keySet().iterator(); it.hasNext(); )
         {
            String key = it.next();
            TJHIconFile icf = largeIconMap.get(key);

            icf.writeXML(out, "large");
         }
      }

      out.println("</icons>");
   }

   @Override
   public String toString()
   {
      return String.format("%s[TeX Path=%s, PDF File=%s, resource path=%s, name=%s, suffix=%s, ext=%s, pageNum=%d, referenced=%s]",
        getClass().getSimpleName(),
        texPath, pdfFile, resourcePath, name, suffix, ext, pageNum, referenced
      );
   }

   TeXPath texPath;
   File pdfFile;
   String resourcePath, name, suffix, ext;
   int pageNum;
   boolean referenced = false;
}

