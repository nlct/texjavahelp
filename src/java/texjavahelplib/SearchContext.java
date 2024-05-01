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

package com.dickimawbooks.texjavahelplib;

import java.util.Vector;

public class SearchContext
{
   public SearchContext(int contextId, CharSequence context)
   {
      this.contextId = contextId;
      this.context = context;
   }

   public void addItem(SearchItem item)
   {
      if (items == null)
      {
         items = new Vector<SearchItem>();
      }

      items.add(item);
   }

   public CharSequence getText()
   {
      return context;
   }

   public int getId()
   {
      return contextId;
   }

   public SearchResult find(TeXJavaHelpLib helpLib,
     Vector<String> words, boolean caseSensitive, boolean exact)
   throws UnknownNodeException
   {
      if (items == null) return null;

      SearchResult result = null;

      for (SearchItem item : items)
      {
         String word = item.getWord();

         if (!caseSensitive)
         {
            word = word.toLowerCase();
         }

         boolean found = false;

         if (exact)
         {
            found = words.contains(word);
         }
         else
         {
            String nodeLabel = item.getNodeLabel();
            int contextId = item.getContextId();
            int itemStartIdx = item.getContextStart();

            for (String w : words)
            {
               int idx = word.indexOf(w);

               if (idx > -1)
               {
                  int endIdx = idx + w.length();

                  if (idx > 0 || endIdx < word.length())
                  {
                     SearchItem newItem = new SearchItem(w,
                       itemStartIdx+idx, itemStartIdx+endIdx,
                       nodeLabel, contextId);

                     item = newItem;
                  }

                  // break to avoid overlap
                  found = true;
                  break;
               }
            }
         }

         if (found)
         {
            if (result == null)
            {
               result = new SearchResult(item.getNode(helpLib), contextId);
            }

            if (result != null)
            {
               result.addItem(item);
            }
         }
      }

      return result;
   }

   @Override
   public String toString()
   {
      return String.format("%s[id=%d,numItems=%d,context=%s]",
        getClass().getSimpleName(), contextId,
        (items== null ? 0 : items.size()),
        context);
   }

   protected CharSequence context;
   protected int contextId;
   protected Vector<SearchItem> items;
}
