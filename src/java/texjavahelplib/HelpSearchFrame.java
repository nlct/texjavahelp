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

import java.util.TreeSet;
import java.util.Vector;
import java.util.HashMap;
import java.util.Comparator;

import java.text.BreakIterator;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTML;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import javax.swing.text.BadLocationException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Frame for showing search results.
 */
public class HelpSearchFrame extends JFrame
 implements HyperlinkListener
{
   public HelpSearchFrame(final HelpFrame helpFrame)
   {
      super(helpFrame.getHelpLib().getMessage("help.navigation.search.title"));

      this.helpFrame = helpFrame;

      init();
   }

   protected void init()
   {
      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();

      titleTooltipText = helpLib.getMessage("help.navigation.search_link_title");
      paraTooltipText = helpLib.getMessage("help.navigation.search_link_para");

      JComponent searchPanel = Box.createVerticalBox();

      JPanel inputPanel = new JPanel();
      searchPanel.add(inputPanel);

      searchLabel = helpLib.createJLabel("help.navigation.search.keywords");
      inputPanel.add(searchLabel);

      searchBox = new JTextField(20);
      searchLabel.setLabelFor(searchBox);
      inputPanel.add(searchBox);

      TJHAbstractAction findAction = new TJHAbstractAction(helpLib,
        "help.navigation.search", "find")
       {
          @Override
          public void doAction()
          {
             find();
          }
       };

      searchButton = new JButton(findAction);
      inputPanel.add(searchButton);

      getRootPane().setDefaultButton(searchButton);

      JPanel togglePanel = new JPanel();
      searchPanel.add(togglePanel);

      caseBox = helpLib.createJCheckBox("help.navigation.search", "case", false);
      togglePanel.add(caseBox);

      exactBox = helpLib.createJCheckBox("help.navigation.search", "exact", true);
      togglePanel.add(exactBox);

      resultComp = new JEditorPane("text/html", "");
      resultComp.setEditable(false);

      resultComp.addHyperlinkListener(this);
      resultComp.addMouseListener(new MouseAdapter()
       {
          @Override
          public void mouseReleased(MouseEvent evt)
          {
             jumpToContext(evt.getPoint());
          }
       });

      resultComp.addMouseMotionListener(new MouseMotionAdapter()
       {
          @Override
          public void mouseMoved(MouseEvent e)
          {
             updateToolTipText(e.getPoint());
          }
       });

      JComponent bottomPanel = new JPanel(new BorderLayout());

      foundComp = new JPanel(new BorderLayout());
      foundComp.setVisible(false);

      foundLabel = new JLabel();
      foundComp.add(foundLabel, "Center");

      TJHAbstractAction clearAction = new TJHAbstractAction(helpLib,
        "help.navigation.search", "clear_results")
       {
          @Override
          public void doAction()
          {
             clear();
          }
       };

      foundComp.add(new JButton(clearAction), "East");

      bottomPanel.add(foundComp, "North");
      bottomPanel.add(new JScrollPane(resultComp), "Center");

      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        new JScrollPane(searchPanel), bottomPanel);

      getContentPane().add(splitPane, "Center");

      processComp = new JPanel(new BorderLayout());
      getContentPane().add(processComp, "South");

      processComp.add(helpLib.createJLabel("help.navigation.searching"), "West");

      progressBar = new JProgressBar();
      processComp.add(progressBar, "Center");

      TJHAbstractAction stopAction = new TJHAbstractAction(helpLib,
        "help.navigation.search", "stop")
       {
          @Override
          public void doAction()
          {
             stopSearch();
          }
       };
      processComp.add(helpLib.createToolBarButton(stopAction));

      processComp.setVisible(false);

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = getPreferredSize();
      dim.height = (int)tk.getScreenSize().getHeight()/2;
      setSize(dim.width, dim.height);
   }


   public void open()
   {
      if (isVisible())
      {
         toFront();
      }
      else
      {
         setLocationRelativeTo(helpFrame);
         setVisible(true);
      }

      searchBox.requestFocusInWindow();
   }

   public void stopSearch()
   {
      worker.stopSearch();
   }

   public void setFound(String text)
   {
      foundComp.setVisible(true);
      foundLabel.setText(text);
   }

   public void clear()
   {
      searchBox.setText("");
      foundLabel.setText("");
      resultComp.setText("");
      foundComp.setVisible(false);
   }

   public void find()
   {
      String searchList = searchBox.getText().trim();

      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();

      if (searchList.isEmpty())
      {
         helpLib.error(
           helpLib.getMessage("error.missing_search_term"));

         return;
      }

      resultComp.setText("");
      foundLabel.setText("");
      progressBar.setValue(0);

      worker = new SearchWorker(this, searchList);
      worker.addPropertyChangeListener(new PropertyChangeListener()
       {
          @Override
          public void propertyChange(PropertyChangeEvent evt)
          {
             if ("progress".equals(evt.getPropertyName()))
             {
                progressBar.setValue((Integer)evt.getNewValue());
             }
          }
       });

      processComp.setVisible(true);

      worker.execute();
   }

   public boolean isExactOn()
   {
      return exactBox.isSelected();
   }

   public boolean isCaseOn()
   {
      return caseBox.isSelected();
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpFrame.getHelpLib();
   }

   public void setResults(int matches, TreeSet<SearchResult> results)
   {
      worker = null;
      processComp.setVisible(false);

      if (matches == 0)
      {
         setFound(
           getHelpLib().getMessage("help.navigation.search.not_found"));
      }
      else
      {
         setFound(
           getHelpLib().getMessage("help.navigation.search.found", matches));

         StringBuilder builder = new StringBuilder();

         HelpFontSettings fontSettings = helpFrame.getHelpFontSettings();

         builder.append("<html><head><style>.highlight { background: yellow; }");
         fontSettings.appendRules(builder);
         builder.append("</style></head><body>");

         SearchData searchData = getHelpLib().getSearchData();

         for (SearchResult result : results)
         {
            builder.append(result.getHighlightedContext(searchData));

            builder.append("<hr>");
         }

         builder.append("</body></html>");

         resultComp.setText(builder.toString());
         resultComp.setCaretPosition(0);
      }
   }

   public void findFailed(Exception e)
   {
      setFound(e.getMessage());

      worker = null;
      processComp.setVisible(false);

      getHelpLib().error(e);
   }

   @Override
   public void hyperlinkUpdate(HyperlinkEvent evt)
   {
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
         /* 
            href is in the non-standard form node-label#ref so can't
            use evt.getUrl()
          */

         String desc = evt.getDescription();

         int idx = desc.lastIndexOf('#');
         String ref = null;

         if (idx > -1)
         {
            ref = desc.substring(idx+1);
            desc = desc.substring(0, idx);
         }

         try
         {
            helpFrame.setPage(desc, ref);
         }
         catch (IOException e)
         {
            getHelpLib().error(e);
         }
      }
   }

   protected void jumpToContext(Point p)
   {
      HTMLDocument doc = (HTMLDocument)resultComp.getDocument();

      if (doc.getLength() > 0)
      {
         int pos = resultComp.viewToModel(p);

         if (pos > -1)
         {
            Element elem = doc.getParagraphElement(pos);

            if (elem != null)
            {
               AttributeSet attrSet = elem.getAttributes();
               String id = (String)attrSet.getAttribute(HTML.Attribute.ID);

               if (id != null)
               {
                  int idx = id.lastIndexOf('#');

                  if (idx > 0)
                  {
                     String nodeId = id.substring(0, idx);
                     String ref = id.substring(idx+1);

                     try
                     {
                        helpFrame.setPage(nodeId, ref);
                     }
                     catch (IOException e)
                     {
                        getHelpLib().error(e);
                     }
                  }
               }
            }
         }
      }
   }

   protected void updateToolTipText(Point p)
   {
      String text = null;

      HTMLDocument doc = (HTMLDocument)resultComp.getDocument();

      if (doc.getLength() > 0)
      {
         int pos = resultComp.viewToModel(p);

         if (pos > -1)
         {
            Element elem = doc.getParagraphElement(pos);

            if (elem != null)
            {
               AttributeSet attrSet = elem.getAttributes();
               String id = (String)attrSet.getAttribute(HTML.Attribute.ID);

               if (id == null)
               {
                  text = titleTooltipText;
               }
               else
               {
                  int idx = id.lastIndexOf('#');

                  if (idx > 0)
                  {
                     text = paraTooltipText;
                  }
               }
            }
         }
      }

      resultComp.setToolTipText(text);
   }

   public void update()
   {
      HTMLDocument doc = (HTMLDocument)resultComp.getDocument();
      StyleSheet styles = doc.getStyleSheet();

      helpFrame.getHelpFontSettings().addFontRulesToStyleSheet(styles);
   }

   protected HelpFrame helpFrame;
   protected JTextField searchBox;
   protected JLabel searchLabel, foundLabel;
   protected JButton searchButton, stopButton;
   protected JCheckBox exactBox, caseBox;
   protected JComponent processComp, foundComp;
   protected JEditorPane resultComp;
   protected JProgressBar progressBar;
   protected SearchWorker worker;

   protected String titleTooltipText, paraTooltipText;
}

class SearchWorker extends SwingWorker<TreeSet<SearchResult>,Void>
{
   protected SearchWorker(HelpSearchFrame helpSearchFrame, String searchList)
   {
      this.helpSearchFrame = helpSearchFrame;
      this.searchList = searchList;
   }

   @Override
   public TreeSet<SearchResult> doInBackground()
    throws UnknownNodeException
   {
      requestStop = false;
      TeXJavaHelpLib helpLib = helpSearchFrame.getHelpLib();

      matches = 0;

      boolean caseSensitive = helpSearchFrame.isCaseOn();
      boolean exact = helpSearchFrame.isExactOn();

      String wordList = helpLib.preProcessSearchWordList(searchList);

      Vector<String> words = null;
   
      BreakIterator boundary = BreakIterator.getWordInstance();
      boundary.setText(wordList);
            
      int idx1 = boundary.first();

      for (int idx2 = boundary.next(); idx2 != BreakIterator.DONE;
           idx1 = idx2, idx2 = boundary.next())
      {
         String word = wordList.substring(idx1, idx2).trim();

         if (!caseSensitive)
         {
            word = word.toLowerCase();
         }

         if (words == null)
         {
            words = new Vector<String>();
         }

         if (!word.isEmpty() && !words.contains(word))
         {
            words.add(word);
         }
      }

      if (words == null || words.isEmpty())
      {
         return null;
      }

      if (!exact && words.size() > 1)
      {
         words.sort(new Comparator<String>()
          {
             public int compare(String str1, String str2)
             {
                int n1 = str1.length();
                int n2 = str2.length();

                if (n1 > n2)
                {
                   return -1;
                }
                else if (n1 < n2)
                {
                   return 1;
                }
                else
                {
                   return 0;
                }
             }
          });
      }

      TreeSet<SearchResult> results = null;

      SearchData searchData = helpLib.getSearchData();

      HashMap<Integer,SearchContext> contexts;

      HashMap<String,Vector<Integer>> wordToContextMap = searchData.getWordToContextMap(caseSensitive);

      if (exact)
      {
         contexts = new HashMap<Integer,SearchContext>();

         for (String word : words)
         {
            Vector<Integer> list = wordToContextMap.get(word);

            if (list != null)
            {
               for (Integer contextId : list)
               {
                  if (contexts.get(contextId) == null)
                  {
                     contexts.put(contextId,
                        searchData.getContext(contextId.intValue()));
                  }
               }
            }
         }
      }
      else
      {
         contexts = searchData.getContexts();
      }

      int n = contexts.size();
      int i = 0;

      for (Integer key : contexts.keySet())
      {
         if (isCancelled())
         {
            return null;
         }

         if (requestStop)
         {
            break;
         }

         SearchContext searchContext = contexts.get(key);

         SearchResult result = searchContext.find(helpLib,
           words, caseSensitive, exact);

         if (result != null)
         {
            if (results == null)
            {
               results = new TreeSet<SearchResult>();
            }

            results.add(result);

            matches += result.getItemCount();
         }

         setProgress(100 * (++i) / n);
      }

      return results;
   }

   @Override
   protected void done()
   {
      try
      {
         helpSearchFrame.setResults(matches, get());
      }
      catch (Exception e)
      {
         helpSearchFrame.findFailed(e);
      }
   }

   public void stopSearch()
   {
      requestStop = true;
   }

   protected HelpSearchFrame helpSearchFrame;
   protected String searchList;
   protected boolean requestStop = false;
   protected int matches;
}
