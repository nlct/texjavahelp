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

import com.dickimawbooks.texparserlib.html.DivisionNode;

public class SearchItem implements Comparable<SearchItem>
{
   public SearchItem(String word, 
     int contextStartIdx, int contextEndIdx,
     String nodeLabel, int contextId)
   {
      if (word == null || nodeLabel == null)
      {
         throw new NullPointerException();
      }

      if (contextEndIdx < contextStartIdx)
      {
         throw new IllegalArgumentException(String.format(
          "context end index %d is less than start index %d",
           contextEndIdx, contextStartIdx));
      }

      this.word = word;
      this.contextStartIdx = contextStartIdx;
      this.contextEndIdx = contextEndIdx;
      this.nodeLabel = nodeLabel;
      this.contextId = contextId;
   }

   public String getWord()
   {
      return word;
   }

   public int getContextStart()
   {
      return contextStartIdx;
   }

   public int getContextEnd()
   {
      return contextEndIdx;
   }

   public String getNodeLabel()
   {
      return nodeLabel;
   }

   public int getContextId()
   {
      return contextId;
   }

   @Override
   public boolean equals(Object other)
   {
      if (other == null || !(other instanceof SearchItem)) return false;

      SearchItem item = (SearchItem)other;

      return contextId == item.contextId
           && contextStartIdx == item.contextStartIdx
           && contextEndIdx == item.contextEndIdx;
   }

   @Override
   public int compareTo(SearchItem other)
   {
      if (contextId < other.contextId)
      {
         return -1;
      }
      else if (contextId > other.contextId)
      {
         return 1;
      }
      else if (contextStartIdx == other.contextStartIdx)
      {
         return 0;
      }
      else if (contextStartIdx < other.contextStartIdx
            || contextEndIdx < other.contextEndIdx)
      {
         return -1;
      }
      else
      {
         return 1;
      }
   }

   @Override
   public String toString()
   {
      return String.format("%s[word=%s,node=%s,contextId=%d,start=%d,end=%d)]",
         getClass().getSimpleName(), word, nodeLabel,
         contextId, contextStartIdx, contextEndIdx);
   }

   public NavigationNode getNode(TeXJavaHelpLib helpLib)
    throws UnknownNodeException
   {
      if (node == null)
      {
         node = helpLib.getNavigationTree().getNodeById(nodeLabel);

         if (node == null)
         {
            throw new UnknownNodeException(helpLib.getMessageWithFallback(
              "error.node_id_not_found", "Node with ID ''{0}'' not found",
              nodeLabel));
         }
      }

      return node;
   }

   protected String word;
   protected int contextStartIdx, contextEndIdx;
   protected int contextId;
   protected String nodeLabel;
   protected NavigationNode node;
}
