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
 implements HyperlinkListener,HelpFontChangeListener
{
   public HelpSearchFrame(final HelpFrame helpFrame)
   {
      super(helpFrame.getHelpLib().getMessage("help_page_search.title"));

      this.helpFrame = helpFrame;

      init();

      helpFrame.getHelpLib().addHelpFontChangeListener(this);
   }

   protected void init()
   {
      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();

      titleTooltipText = helpLib.getMessage("message.help_page_search_link_title");
      paraTooltipText = helpLib.getMessage("message.help_page_search_link_para");

      JMenuBar menuBar = new JMenuBar();
      setJMenuBar(menuBar);

      JMenu searchM = helpLib.createJMenu("menu.help_page_search.search_menu");
      menuBar.add(searchM);

      JComponent searchPanel = Box.createVerticalBox();

      JPanel inputPanel = new JPanel();
      searchPanel.add(inputPanel);

      searchLabel = helpLib.createJLabel("help_page_search.keywords");
      inputPanel.add(searchLabel);

      searchBox = new JTextField(20);
      searchLabel.setLabelFor(searchBox);
      searchBox.setToolTipText(searchLabel.getToolTipText());
      inputPanel.add(searchBox);

      searchAction = new TJHAbstractAction(helpLib,
        "menu.help_page_search.search_menu", "search")
       {
           @Override
           public void doAction()
           {
              find();
           }
       };

      JButton searchButton = helpLib.createToolBarButton(searchAction, true);
      searchM.add(searchAction);

      inputPanel.add(searchButton);

      getRootPane().setDefaultButton(searchButton);

      JPanel togglePanel = new JPanel();
      searchPanel.add(togglePanel);

      caseBox = helpLib.createJCheckBox("help_page_search", "case", false);
      togglePanel.add(caseBox);

      exactBox = helpLib.createJCheckBox("help_page_search", "exact", true);
      togglePanel.add(exactBox);

      resultComp = new TJHEditorPane();

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

      JComponent foundButtonsComp = new JPanel();
      foundComp.add(foundButtonsComp, "East");

      previousAction = new TJHAbstractAction(helpLib,
        "menu.help_page_search.search_menu", "previous")
       {
          @Override
          public void doAction()
          {
             previousResult();
          }
       };

      foundButtonsComp.add(helpLib.createToolBarButton(previousAction, true));
      searchM.add(previousAction);
      previousAction.setEnabled(false);

      nextAction = new TJHAbstractAction(helpLib,
        "menu.help_page_search.search_menu", "next")
       {
          @Override
          public void doAction()
          {
             nextResult();
          }
       };

      foundButtonsComp.add(helpLib.createToolBarButton(nextAction, true));
      searchM.add(nextAction);
      nextAction.setEnabled(false);

      searchM.addSeparator();
      foundButtonsComp.add(Box.createHorizontalStrut(10));

      clearAction = new TJHAbstractAction(helpLib,
        "menu.help_page_search.search_menu", "reset")
       {
          @Override
          public void doAction()
          {
             clear();
          }
       };

      foundButtonsComp.add(helpLib.createToolBarButton(clearAction, true));
      searchM.add(clearAction);
      clearAction.setEnabled(false);

      bottomPanel.add(foundComp, "North");
      bottomPanel.add(new JScrollPane(resultComp), "Center");

      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        new JScrollPane(searchPanel), bottomPanel);

      getContentPane().add(splitPane, "Center");

      processComp = new JPanel(new BorderLayout());
      getContentPane().add(processComp, "South");

      processComp.add(helpLib.createJLabel(
        "message.help_page_search.searching"), "West");

      progressBar = new JProgressBar();
      processComp.add(progressBar, "Center");

      stopAction = new TJHAbstractAction(helpLib,
        "menu.help_page_search.search_menu", "stop")
       {
          @Override
          public void doAction()
          {
             stopSearch();
          }
       };
      processComp.add(helpLib.createToolBarButton(stopAction), "East");
      searchM.add(stopAction);
      stopAction.setEnabled(false);

      processComp.setVisible(false);

      searchM.add(new TJHAbstractAction(helpLib,
        "button", "close")
       {
          @Override
          public void doAction()
          {
             setVisible(false);
          }
       });

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
      clearAction.setEnabled(true);
      nextAction.setEnabled(true);
      previousAction.setEnabled(true);
      foundComp.setVisible(true);
      foundLabel.setText(text);
   }

   public void clear()
   {
      currentResult = null;
      searchBox.setText("");
      foundLabel.setText("");
      resultComp.setText("");
      foundComp.setVisible(false);
      clearAction.setEnabled(false);
      nextAction.setEnabled(false);
      previousAction.setEnabled(false);
      searchBox.requestFocusInWindow();
   }

   protected void nextResult()
   {
      if (results != null && !results.isEmpty())
      {
         if (currentResult == null)
         {
            currentResult = results.first();
         }
         else
         {
            currentResult = results.higher(currentResult);

            if (currentResult == null)
            {
               currentResult = results.first();
            }
         }

         resultComp.scrollToReference(""+currentResult.getContextId());
      }
   }

   protected void previousResult()
   {
      if (results != null && !results.isEmpty())
      {
         if (currentResult == null)
         {
            currentResult = results.last();
         }
         else
         {
            currentResult = results.lower(currentResult);

            if (currentResult == null)
            {
               currentResult = results.last();
            }
         }

         resultComp.scrollToReference(""+currentResult.getContextId());
      }
   }

   protected void find()
   {
      String searchList = searchBox.getText().trim();

      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();

      if (searchList.isEmpty())
      {
         helpLib.getApplication().error(this,
           helpLib.getMessage("error.missing_search_term"));

         searchBox.requestFocusInWindow();

         if (searchBox.getToolTipText() != null)
         {
            KeyEvent ke = new KeyEvent(searchBox, KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(), InputEvent.CTRL_MASK,
                    KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED);
            searchBox.dispatchEvent(ke);
         }

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

      stopAction.setEnabled(true);
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
      stopAction.setEnabled(false);
      currentResult = null;
      this.results = results;

      if (matches == 0)
      {
         setFound(
           getHelpLib().getMessage("message.help_page_search.not_found"));
      }
      else
      {
         setFound(
           getHelpLib().getMessage("message.help_page_search.found", matches));

         StringBuilder builder = new StringBuilder();

         HelpFontSettings fontSettings = getHelpLib().getHelpFontSettings();

         builder.append("<html><head><style>.highlight { background: yellow; }");
         fontSettings.appendRules(builder);
         builder.append("</style></head><body>");

         SearchData searchData = getHelpLib().getSearchData();

         currentResult = results.first();

         for (SearchResult result : results)
         {
            result.getHighlightedContext(builder, searchData);

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
      results = null;
      processComp.setVisible(false);
      stopAction.setEnabled(false);

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
            helpFrame.requestHelpPageFocus();
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

   @Override
   public void fontChanged(HelpFontChangeEvent evt)
   {
      HelpFontSettings fontSettings = evt.getSettings();

      HTMLDocument doc = (HTMLDocument)resultComp.getDocument();
      StyleSheet styles = doc.getStyleSheet();

      fontSettings.addFontRulesToStyleSheet(styles, evt.getModifiers());
   }

   protected HelpFrame helpFrame;
   protected JTextField searchBox;
   protected JLabel searchLabel, foundLabel;

   TJHAbstractAction searchAction, clearAction, stopAction,
    nextAction, previousAction;

   protected JCheckBox exactBox, caseBox;
   protected JComponent processComp, foundComp;
   protected TJHEditorPane resultComp;
   protected JProgressBar progressBar;
   protected SearchWorker worker;

   protected TreeSet<SearchResult> results;
   protected SearchResult currentResult;

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
