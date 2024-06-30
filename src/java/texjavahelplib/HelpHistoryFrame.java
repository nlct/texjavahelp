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
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.JEditorPane;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;

/**
 * Frame for showing history page list.
 */
public class HelpHistoryFrame extends JFrame
{
   public HelpHistoryFrame(final HelpFrame helpFrame)
   {
      super(helpFrame.getHelpLib().getMessage("help.navigation.history.title"));

      this.helpFrame = helpFrame;

      pointer = helpFrame.getHelpLib().getMessage(
        "symbol.help.navigation.history.pointer");
      header = helpFrame.getHelpLib().getMessage("help.navigation.history.header");

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
          public Class<?> getColumnClass(int columnIndex)
          {
             return HistoryItem.class;
          }

          @Override
          public Object getValueAt(int row, int col)
          {
             int idx = getRowCount() - 1 - row;

             return helpFrame.getHistoryItem(idx);
          }
       };

      table = new JTable(tableModel);

      historyItemRenderer = new HistoryItemRenderer(helpFrame, pointer);

      table.setDefaultRenderer(HistoryItem.class, 
         historyItemRenderer);

      table.addMouseListener(new MouseAdapter()
       {
          @Override
          public void mousePressed(MouseEvent evt)
          {
             if (evt.getClickCount() == 2)
             {
                goToSelectedPage();
             }
          }
       });

      TableColumnModel colModel = table.getColumnModel();
      TableColumn col = colModel.getColumn(0);
      col.setHeaderValue(header);

      getContentPane().add(new JScrollPane(table), "Center");

      JPanel bottomPanel = new JPanel(new BorderLayout());
      getContentPane().add(bottomPanel, "South");

      JPanel btnPanel = new JPanel();
      bottomPanel.add(btnPanel, "East");

      goButton = helpFrame.getHelpLib().createJButton(
        "help.navigation.history", "go", new ActionListener()
        {
           @Override
           public void actionPerformed(ActionEvent evt)
           {
              goToSelectedPage();
           }
        });

      goButton.setEnabled(false);

      btnPanel.add(goButton);

      ListSelectionModel listSelModel = table.getSelectionModel();
      listSelModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      listSelModel.addListSelectionListener(new ListSelectionListener()
       {
          @Override
          public void valueChanged(ListSelectionEvent e)
          {
             goButton.setEnabled(table.getSelectedRow() != -1);
          }
       });

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width/3, dim.height/2);

   }

   protected void goToSelectedPage()
   {
      int idx = table.getSelectedRow();

      if (idx != -1)
      {
         helpFrame.history(table.getRowCount() - 1 - idx);
         helpFrame.setVisible(true);
         helpFrame.toFront();
      }
   }

   public int getRowCount()
   {
      return table.getRowCount();
   }

   public void update()
   {
      tableModel.fireTableDataChanged();
      table.setRowHeight((int)historyItemRenderer.getPreferredSize().getHeight()+2);
   }

   protected HelpFrame helpFrame;
   protected JTable table;
   protected AbstractTableModel tableModel;
   protected String pointer, header;
   protected JButton goButton;
   protected HistoryItemRenderer historyItemRenderer;
}

// adapted from DefaultTableCellRenderer
class HistoryItemRenderer extends JEditorPane
 implements TableCellRenderer
{
   public HistoryItemRenderer(HelpFrame helpFrame, String pointer)
   {
      super();
      this.helpFrame = helpFrame;
      this.pointer = pointer;

      setContentType("text/html");
   }

   public Component getTableCellRendererComponent(JTable table,
     Object value, boolean isSelected, boolean hasFocus, int row, int column)
   {
      if (isSelected)
      {
         super.setBackground(table.getSelectionBackground());
         super.setForeground(table.getSelectionForeground());
      }
      else
      {
         if (background == null)
         {
            super.setBackground(table.getBackground());
         }
         else
         {
            super.setBackground(background);
         }

         if (foreground == null)
         {
            super.setForeground(table.getForeground());
         }
         else
         {
            super.setForeground(foreground);
         }
      }

      Border b = null;

      if (hasFocus)
      {
         if (isSelected)
         {
            b = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
         }

         if (b == null)
         {
            b = UIManager.getBorder("Table.focusCellHighlightBorder");
         }
      }
      else
      {
         b = noFocusBorder;
      }

      setBorder(b);

      Color back = getBackground();
      setOpaque(back != null && back.equals(table.getBackground()));

      HistoryItem item = (HistoryItem)value;
      String title = (item == null ? "" : item.toString());

      int idx = table.getRowCount() - 1 - row;

      HelpFontSettings fontSettings = helpFrame.getHelpLib().getHelpFontSettings();

      StringBuilder builder = new StringBuilder();

      builder.append("<html><style>");
      fontSettings.appendRules(builder);
      builder.append("<style><body>");

      if (idx == helpFrame.getHistoryIndex())
      {
         builder.append("<b><span class=\"icon\">");
         builder.append(pointer);
         builder.append("</span> ");
         builder.append(title);
         builder.append("</b>");
      }
      else
      {
         builder.append(title);
      }

      builder.append("</body></html>");

      setText(builder.toString());

      if (item != null)
      {
         NavigationNode node = item.getNode();

         String tooltip = node.getFileName();

         String ref = item.getReference();

         if (ref != null)
         {
            tooltip += "#"+ref;
         }

         setToolTipText(tooltip);
      }

      return this;
   }

   public void setForeground(Color c)
   {
      super.setForeground(c);
      foreground = c;
   }

   public void setBackground(Color c)
   {
      super.setBackground(c);
      background = c;
   }

   public void updateUI()
   {
      super.updateUI();
      background = null;
      foreground = null;
   }

   protected HelpFrame helpFrame;
   protected String pointer;
   protected Color foreground, background;

   protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
}
