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

import java.io.IOException;
import java.io.File;

import java.util.zip.ZipEntry;

/**
 * Exception thrown when encountering a problem reading or writing a
 * zip file.
 */
public class ZipIOException extends IOException
{
   public ZipIOException(TeXJavaHelpLib helpLib, File zipFile, ZipEntry entry,
     String message)
   {
      this(helpLib, zipFile.toString(), entry.getName());
   }

   public ZipIOException(TeXJavaHelpLib helpLib, String zipFileName, String entryName,
     String message)
   {
      super(helpLib.getMessageWithFallback(
            "error.zip_io_file_entry", "{0}!{1}: {2}",
            zipFileName, entryName, message));
   }

   public ZipIOException(TeXJavaHelpLib helpLib, File zipFile, 
     String message)
   {
      this(helpLib, zipFile.toString(), message);
   }

   public ZipIOException(TeXJavaHelpLib helpLib, String zipFileName, 
     String message)
   {
      super(helpLib.getMessageWithFallback(
            "error.zip_io_file", "{0}: {1}", zipFileName, message));
   }

   public ZipIOException(String message)
   {
      super(message);
   }

   public ZipIOException(TeXJavaHelpLib helpLib, File zipFile, ZipEntry entry,
     String message, Throwable cause)
   {
      this(helpLib, zipFile.toString(), entry.getName(), cause);
   }

   public ZipIOException(TeXJavaHelpLib helpLib, String zipFileName, String entryName,
     String message, Throwable cause)
   {
      super(helpLib.getMessageWithFallback(
            "error.zip_io_file_entry", "{0}!{1}: {2}",
            zipFileName, entryName, message), cause);
   }

   public ZipIOException(TeXJavaHelpLib helpLib, File zipFile, 
     String message, Throwable cause)
   {
      this(helpLib, zipFile.toString(), message, cause);
   }

   public ZipIOException(TeXJavaHelpLib helpLib, String zipFileName, 
     String message, Throwable cause)
   {
      super(helpLib.getMessageWithFallback(
            "error.zip_io_file", "{0}: {1}", zipFileName, message), cause);
   }

   public ZipIOException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
