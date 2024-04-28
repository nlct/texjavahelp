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

import java.util.TreeSet;

public class SearchResult implements Comparable<SearchResult>
{
   public SearchResult(NavigationNode node, int contextId)
   {
      this.node = node;
      this.contextId = contextId;
   }

   public void addItem(SearchItem item)
   {
      if (items == null)
      {
         items = new TreeSet<SearchItem>();
      }

      items.add(item);
   }

   public int getItemCount()
   {
      return items == null ? 0 : items.size();
   }

   @Override
   public int compareTo(SearchResult other)
   {
      if (contextId == other.contextId)
      {
         if (getItemCount() == other.getItemCount())
         {
            return 0;
         }
         else if (getItemCount() < other.getItemCount())
         {
            return 1;
         }
         else
         {
            return -1;
         }
      }
      else if (contextId < other.contextId)
      {
         return -1;
      }
      else
      {
         return 1;
      }
   }

   @Override
   public boolean equals(Object other)
   {
      if (other == null || !(other instanceof SearchResult))
      {
         return false;
      }

      SearchResult result = (SearchResult)other;

      if (contextId == result.contextId)
      {
         if (items == null && result.items == null)
         {
            return true;
         }
         else if (items != null && result.items != null)
         {
            return items.equals(result.items);
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }
   }

   protected int contextId;
   protected NavigationNode node;
   protected TreeSet<SearchItem> items;
}
