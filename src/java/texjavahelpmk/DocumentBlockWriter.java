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

import com.dickimawbooks.texparserlib.TeXParser;
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
            writeAndClear();
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
               writeAndClear();
            }
         }
      }
   }

   protected void processBuffer() throws IOException
   {
      Matcher m = REDUNDANT_P.matcher(buffer);

      if (m.find())
      {
         buffer.delete(0, m.start(1)-1);
      }

      contextId++;

      m = START_NO_ID.matcher(buffer);

      String idAttr = String.format(" id=\"context%d\"", contextId);

      if (m.find())
      {
         buffer.insert(m.end()-1, idAttr);
      }
      else
      {
         buffer.insert(0, String.format("<a%s></a>", idAttr));
      }

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

            m = IMG.matcher(buffer);

            int nextCp = (i < buffer.length()-1 ? buffer.codePointAt(i+1) : -1);

            if (nextCp == 'i' && m.find(i) && m.start() == i)
            {
               processBuffer(m.start(1), m.end(1), context);

               i = m.end()-1;
               cp = buffer.codePointAt(i);

               inTag = false;
               start = i+1;
            }
         }

         i += Character.charCount(cp);
      }

      if (!inTag)
      {
         processBuffer(start, buffer.length(), context);
      }

      writeAndClear();
   }

   protected void processBuffer(int start, int end, StringBuilder context)
     throws IOException
   {
      String source = listener.getHelpLib().preProcessSearchWordList(
        buffer.substring(start, end));

      int contextOffset = context.length();

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
             listener.getCurrentNode().getId(),
             contextId);

            listener.addSearchItem(item, context);
         }
      }
   }

   @Override
   public void write(int c) throws IOException
   {
      if (writerClosed && c > 0 && !Character.isWhitespace(c))
      {
         throw new IOException(
          String.format("Writer closed. Can't write character 0x%x", c));
      }

      buffer.append((char)c);
   }

   @Override
   public void write(char[] cbuff) throws IOException
   {
      if (writerClosed && cbuff.length > 0)
      {
         throw new IOException("Writer closed. Can't write cbuff "+new String(cbuff));
      }

      buffer.append(cbuff);
   }

   @Override
   public void write(char[] cbuff, int off, int len) throws IOException
   {
      if (writerClosed && len > 0)
      {
         throw new IOException("Writer closed. Can't write cbuff "
           +new String(cbuff, off, len));
      }

      buffer.append(cbuff, off, len);
   }

   @Override
   public void write(String str) throws IOException
   {
      if (writerClosed && !str.isEmpty())
      {
         throw new IOException("Writer closed. Can't write string " +str);
      }

      buffer.append(str);
   }

   @Override
   public void write(String str, int off, int len) throws IOException
   {
      if (writerClosed && len > 0)
      {
         throw new IOException("Writer closed. Can't write substring "
           + str.substring(off, len));
      }

      buffer.append(str, off, len);
   }

   @Override
   public void flush() throws IOException
   {
      writeAndClear();

      listener.getParser().debugMessage(TeXParser.DEBUG_IO,
        "FLUSHING "+toString());

      writer.flush();
   }

   protected void writeAndClear() throws IOException
   {
      listener.getParser().debugMessage(TeXParser.DEBUG_IO,
        "WRITING "+toString());

      index += buffer.length();

      writer.write(buffer.toString());
      buffer.setLength(0);
   }

   @Override
   public void close() throws IOException
   {
      flush();

      listener.getParser().debugMessage(TeXParser.DEBUG_IO,
        "CLOSING "+toString());

      writer.close();

      writerClosed = true;
   }

   public void setWriter(Writer writer)
    throws IOException
   {
      if (this.writer != null)
      {
         flush();
      }

      writerClosed = false;
      this.writer = writer;
      index = 0;
   }

   /**
    * Returns the index of the end of the processed content.
    * NB there may be pending content after this index.
    * Note also that HTMLDocument converts the HTML code parsed into
    * its own structure so the index of the caret position doesn't
    * relate to the corresponding character position in the HTML
    * source code.
    */
   public int getIndex()
   {
      return index;
   }

   @Override
   public String toString()
   {
      return String.format("%s[writer=%s,closed=%s,index=%d,contextId=%d,buffer=%s]",
       getClass().getSimpleName(), writer, writerClosed, index, contextId, buffer);
   }

   protected Writer writer;
   protected int index = 0;
   protected StringBuilder buffer;
   protected TJHListener listener;
   protected boolean writerClosed = false;

   protected int contextId = 0;

   public static final Pattern EMPTY_P
     = Pattern.compile("\\s*<p>\\s*(</p>)?\\s*");

   public static final Pattern REDUNDANT_P
     = Pattern.compile("^\\s*<p>\\s*<(ul|ol|dl|li|dt|dd|pre|table|caption)\\s*");

   public static final Pattern START_NO_ID
     = Pattern.compile("^\\s*<(?:p|div|ul|ol|dl|pre|li|dt|dd|table)(?:\\s+class=\"[^\"]*\")?\\s*>");

   public static final Pattern IMG
     = Pattern.compile("<img\\s+[^>]*alt\\s*=\\s*\"([^\"]+)\"[^>]*/?>");
}
