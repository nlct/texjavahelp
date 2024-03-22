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

import java.io.IOException;

import java.net.URL;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Frame for showing pages of the manual.
 */
public class HelpFrame extends JFrame
{
   public HelpFrame(TeXJavaHelpLib helpLib, String title)
     throws IOException
   {
      super(title);

      this.helpLib = helpLib;

      helpPage = new HelpPage(helpLib);

      getContentPane().add(new JScrollPane(helpPage), "Center");

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width/2, dim.height/2);

   }

   public void setPage(URL url) throws IOException 
   {
      helpPage.setPage(url);
   }

   protected TeXJavaHelpLib helpLib;
   protected HelpPage helpPage;
}
