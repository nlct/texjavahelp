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

import java.util.HashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.xml.sax.SAXException;


/**
 * Navigation tree.
 */
public class NavigationTree
{
   public NavigationTree(String base, NavigationNode rootNode)
   {
      if (base == null || rootNode == null)
      {
         throw new NullPointerException();
      }

      this.base = base;
      this.rootNode = rootNode;

      createMap();
   }

   protected void createMap()
   {
      idToNodeMap = new HashMap<String,NavigationNode>();
      refToNodeMap = new HashMap<String,NavigationNode>();

      addToMaps(rootNode);
   }

   protected void addToMaps(NavigationNode node)
   {
      idToNodeMap.put(node.getKey(), node);
      refToNodeMap.put(node.getRef(), node);

      if (!node.isLeaf())
      {
         for (NavigationNode childNode: node.getChildren())
         {
            addToMaps(childNode);
         }
      }
   }

   public NavigationNode getNodeById(String id)
   {
      return idToNodeMap.get(id);
   }

   public NavigationNode getNodeByRef(String ref)
   {
      return refToNodeMap.get(ref);
   }

   public NavigationNode getRoot()
   {
      return rootNode;
   }

   public String getBase()
   {
      return base;
   }

   public static NavigationTree load(TeXJavaHelpLib helpLib)
    throws IOException,SAXException
   {
      BufferedReader in = null;

      NavigationNode node = null;

      try
      {
         in = new BufferedReader(new InputStreamReader(
            helpLib.getNavigationXMLInputStream()));
         node = NavigationNode.readTree(helpLib.getMessageSystem(), in);
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      return new NavigationTree(helpLib.getHelpSetResourcePath(), node);
   }

   protected String base;
   protected NavigationNode rootNode;
   protected HashMap<String,NavigationNode> idToNodeMap, refToNodeMap;
}
