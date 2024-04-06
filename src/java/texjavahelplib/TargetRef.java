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

/**
 * An object representing in a node. 
 */
public class TargetRef
{
   public TargetRef(IndexItem indexItem, String ref, NavigationNode node)
   {
      this.indexItem = indexItem;
      this.ref = ref;
      this.node = node;
   }

   public String getRef()
   {
      return ref;
   }

   public NavigationNode getNode()
   {
      return node;
   }

   public IndexItem getIndexItem()
   {
      return indexItem;
   }

   @Override
   public String toString()
   {
      return String.format("%s[ref=%s,index=%s,node=%s]",
        getClass().getSimpleName(), ref, indexItem, node);
   }

   protected String ref;
   protected NavigationNode node;
   protected IndexItem indexItem;
}
