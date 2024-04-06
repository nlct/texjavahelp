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

import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

/**
 * Frame for showing history page list.
 */
public class HelpHistoryFrame extends JFrame
{
   public HelpHistoryFrame(final HelpFrame helpFrame)
   {
      super(helpFrame.getHelpLib().getMessage("navigation.history.title"));

      this.helpFrame = helpFrame;

      pointer = helpFrame.getHelpLib().getMessage("navigation.history.pointer");
      header = helpFrame.getHelpLib().getMessage("navigation.history.header.page");

      init();
   }

   private void init()
   {
      tableModel = new AbstractTableModel()
       {
          @Override
          public int getColumnCount()
          {
             return 1;
          }

          @Override
          public int getRowCount()
          {
             return helpFrame.getHistoryCount();
          }

          @Override
          public Object getValueAt(int row, int col)
          {
             int idx = getRowCount() - 1 - row;

             String title = helpFrame.getHistoryItem(idx).toString();

             if (idx == helpFrame.getHistoryIndex())
             {
                return pointer + " " + title;
             }
             else
             {
                return title;
             }
          }
       };

      table = new JTable(tableModel);

      TableColumnModel colModel = table.getColumnModel();
      TableColumn col = colModel.getColumn(0);
      col.setHeaderValue(header);

      getContentPane().add(new JScrollPane(table), "Center");

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width/3, dim.height/2);

   }

   public int getRowCount()
   {
      return table.getRowCount();
   }

   public void revalidateTable()
   {
      table.revalidate();
      table.repaint();
   }

   protected HelpFrame helpFrame;
   protected JTable table;
   protected AbstractTableModel tableModel;
   protected String pointer, header;
}
