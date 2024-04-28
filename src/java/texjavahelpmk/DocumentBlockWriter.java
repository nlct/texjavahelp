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
import java.io.Writer;

import java.text.BreakIterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.dickimawbooks.texparserlib.TeXParserUtils;
import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.DocumentBlockType;
import com.dickimawbooks.texparserlib.html.DocumentBlockTypeListener;
import com.dickimawbooks.texparserlib.html.DocumentBlockTypeEvent;

import com.dickimawbooks.texjavahelplib.SearchItem;

public class DocumentBlockWriter extends Writer
  implements DocumentBlockTypeListener
{
   public DocumentBlockWriter(Writer writer, TJHListener listener)
   {
      super();

      this.writer = writer;
      this.listener = listener;

      buffer = new StringBuilder();
   }

   @Override
   public void documentBlockUpdate(DocumentBlockTypeEvent evt)
    throws IOException
   {
      if (evt.getWriter() == this)
      {
         DocumentBlockType oldType = evt.getOldType();
         DocumentBlockType newType = evt.getNewType();

         if (TeXParserUtils.isBlank(buffer))
         {
            flush();
         }
         else if (oldType == DocumentBlockType.PARAGRAPH)
         {
            if (EMPTY_P.matcher(buffer).matches())
            {
               // discard
               buffer.setLength(0);
            }
         }

         if (buffer.length() > 0)
         {
            if (oldType == DocumentBlockType.PARAGRAPH
                || oldType == DocumentBlockType.BLOCK)
            {
               processBuffer();
            }
            else
            {
               flush();
            }
         }
      }
   }

   protected void processBuffer() throws IOException
   {
      contextId++;

      int start = 0;
      boolean inTag = false;

      StringBuilder context = new StringBuilder(buffer.length());

      for (int i = 0; i < buffer.length(); )
      {
         int cp = buffer.codePointAt(i);

         if (cp == '>')
         {
            inTag = false;
            start = i+1;
         }
         else if (cp == '<')
         {
            inTag = true;
            processBuffer(start, i, context);
         }

         i += Character.charCount(cp);
      }

      if (!inTag)
      {
         processBuffer(start, buffer.length(), context);
      }

      flush();
   }

   protected void processBuffer(int start, int end, StringBuilder context)
     throws IOException
   {
      String source = listener.getHelpLib().preProcessSearchWordList(
        buffer.substring(start, end));

      int contextOffset = context.length();
      int offset = index + start;

      context.append(source);

      BreakIterator boundary = BreakIterator.getWordInstance();

      boundary.setText(source);

      int idx1 = boundary.first();

      for (int idx2 = boundary.next(); idx2 != BreakIterator.DONE;
           idx1 = idx2, idx2 = boundary.next())
      {
         String word = source.substring(idx1, idx2);

         if (listener.isValidSearchWord(word))
         {
            SearchItem item = new SearchItem(word, 
             contextOffset+idx1, contextOffset+idx2,
             offset+idx1, offset+idx2,
             listener.getCurrentNode().getId(),
             contextId);

            listener.addSearchItem(item, context);
         }
      }
   }

   @Override
   public void write(int c) throws IOException
   {
      buffer.append((char)c);
   }

   @Override
   public void write(char[] cbuff) throws IOException
   {
      buffer.append(cbuff);
   }

   @Override
   public void write(char[] cbuff, int off, int len) throws IOException
   {
      buffer.append(cbuff, off, len);
   }

   @Override
   public void write(String str) throws IOException
   {
      buffer.append(str);
   }

   @Override
   public void write(String str, int off, int len) throws IOException
   {
      buffer.append(str, off, len);
   }

   @Override
   public void flush() throws IOException
   {
      index += buffer.length();

      writer.write(buffer.toString());
      writer.flush();
      buffer.setLength(0);
   }

   @Override
   public void close() throws IOException
   {
      flush();
      writer.close();
   }

   public void setWriter(Writer writer)
   {
      this.writer = writer;
      index = 0;
   }

   protected Writer writer;
   protected int index = 0;
   protected StringBuilder buffer;
   protected TJHListener listener;

   protected int contextId = 0;

   public static final Pattern EMPTY_P = Pattern.compile("\\s*<p>\\s*(</p>)?\\s*");
}
