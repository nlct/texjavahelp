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

package com.dickimawbooks.texjavahelpdemo;

import java.util.Locale;
import java.util.Properties;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.PrintWriter;

import java.text.MessageFormat;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.FlowLayout;
import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.xml.sax.SAXException;

import com.dickimawbooks.texjavahelplib.*;

public class TeXJavaHelpDemo extends JFrame
  implements TeXJavaHelpLibApp, ActionListener
{
   protected TeXJavaHelpDemo()
   {
      super(APP_NAME);
   }

   protected void parseArgs(String[] args)
   {
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("debug"))
         {
            debugMode = 1;
         }
         else if (args[i].equals("nodebug"))
         {
            debugMode = 0;
         }
      }
   }

   protected void init() throws IOException,SAXException
   {
      loadProperties();

      helpLib = new TeXJavaHelpLib(this,
       getLocaleProperty("messages.locale", Locale.getDefault()),
       getLocaleProperty("helpset.locale", Locale.getDefault()));

      helpLib.initHelpSet();

      initGui();

      HelpFrame helpFrame = helpLib.getHelpFrame();

      ImageIcon ic = helpLib.getHelpIcon("manual", true);

      if (ic != null)
      {
         Image img = ic.getImage();

         setIconImage(img);
         helpFrame.setIconImage(img);
      }

      helpFrame.setLocationRelativeTo(this);

      setVisible(true);
   }

   protected File getPropertyFile()
   {
      return null;
   }

   protected void loadProperties() throws IOException
   {
      File file = getPropertyFile();
      properties = new Properties();

      if (file == null || !file.exists())
      {
         return;
      }

      BufferedReader in = null;

      try
      {
         in = new BufferedReader(new FileReader(file));
         properties.load(in);
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }

   protected void saveProperties() throws IOException
   {
      File file = getPropertyFile();

      if (file != null)
      {
         PrintWriter writer = null;

         try
         {
            writer = new PrintWriter(file);
            properties.store(writer,
             getMessage("properties.comment", getApplicationName()));
         }
         finally
         {
            if (writer != null)
            {
               writer.close();
            }
         }
      }
   }

   public Locale getLocaleProperty(String propName, Locale defaultValue)
   {
      Object value = properties.get(propName);

      if (value == null)
      {
         return defaultValue;
      }

      if (value instanceof Locale)
      {
         return (Locale)value;
      }
      else
      {
         return Locale.forLanguageTag(value.toString());
      }
   }

   public String getStringProperty(String propName, String defaultValue)
   {
      Object value = properties.get(propName);

      if (value == null)
      {
         return defaultValue;
      }

      return value.toString();
   }

   protected void initGui()
   {
      String lookandfeel = getStringProperty("lookandfeel", null);

      if (lookandfeel != null)
      {
         try
         {
            setUI(lookandfeel);
         }
         catch (Exception e)
         {
            error(getMessage("error.lookandfeel_failed", lookandfeel), e);
            properties.remove("lookandfeel");
         }
      }

      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent evt)
         {
            quit();
         }
      });

      ImageIcon ic = getLogoIcon();

      if (ic != null)
      {
         setIconImage(ic.getImage());
      }

      JToolBar toolbar = new JToolBar();
      getContentPane().add(toolbar, "North");

      JMenuBar mBar = new JMenuBar();
      setJMenuBar(mBar);

      JMenu fileM = createJMenu("menu.file");
      mBar.add(fileM);

      fileM.add(createJMenuItem("menu.file", "quit",
        KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)));

      JMenu helpM = createJMenu("menu.help");
      mBar.add(helpM);

      TJHAbstractAction manualAction = helpLib.createHelpAction();

      helpM.add(new JMenuItem(manualAction));
      toolbar.add(manualAction);

      JPanel mainPanel = new JPanel(new FlowLayout());

      getContentPane().add(mainPanel, "Center");

      mainPanel.add(createJLabel("label.demo"));
      mainPanel.add(new JButton(helpLib.createHelpAction("sec:intro", mainPanel)));
      mainPanel.add(new JTextField("sample"));

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width*3/4, dim.height*3/4);

      setLocationRelativeTo(null);
   }

   public void setUI(String lookandfeel)
    throws ClassNotFoundException,
           InstantiationException,
           IllegalAccessException,
           UnsupportedLookAndFeelException
   {
      UIManager.setLookAndFeel(lookandfeel);

      SwingUtilities.updateComponentTreeUI(this);
      SwingUtilities.updateComponentTreeUI(helpLib.getHelpFrame());

      properties.setProperty("lookandfeel", lookandfeel);
   }

   public ImageIcon getLogoIcon()
   {
      return null;
   }

   public void quit()
   {
      if (confirmYesNo("message.confirm_quit")
         != JOptionPane.YES_OPTION)
      {
         return;
      }

      try
      {
         saveProperties();
      }
      catch (Exception e)
      {
         error(getMessage("error.saveprops_failed", getPropertyFile()), e);
      }

      System.exit(0);
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("manual"))
      {
         helpLib.openHelp();
      }
      else if (action.equals("quit"))
      {
         quit();
      }
   }

   public JMenu createJMenu(String tag)
   {
      return helpLib.createJMenu(tag);
   }

   public JMenuItem createJMenuItem(String tag)
   {
      return helpLib.createJMenuItem(tag);
   }

   public JMenuItem createJMenuItem(String parentTag, String action)
   {
      return helpLib.createJMenuItem(parentTag, action, this);
   }

   public JMenuItem createJMenuItem(String parentTag, String action,
     KeyStroke accelerator)
   {
      return helpLib.createJMenuItem(parentTag, action, this, accelerator);
   }

   public JMenuItem createJMenuItem(String parentTag, String action,
     ActionListener actionListener)
   {
      return helpLib.createJMenuItem(parentTag, action, actionListener);
   }

   public JMenuItem createJMenuItem(String parentTag, String action,
     ActionListener actionListener, KeyStroke accelerator)
   {
      return helpLib.createJMenuItem(parentTag, action, actionListener,
       accelerator);
   }

   public JLabel createJLabel(String tag)
   {
      return helpLib.createJLabel(tag);
   }

   public JButton createJButton(String tag)
   {
      return helpLib.createJButton(tag);
   }

   public JButton createJButton(String parentTag, String action)
   {
      return helpLib.createJButton(parentTag, action, this);
   }

   public JButton createJButton(String parentTag, String action,
     ActionListener actionListener)
   {
      return helpLib.createJButton(parentTag, action, actionListener);
   }

   public int confirmYesNo(String tag)
   {
      return JOptionPane.showConfirmDialog(this,
         getMessage(tag),
         getMessage(tag+".title"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
   }

   public int getMnemonic(String label)
   {
      return helpLib.getMnemonic(label);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      return helpLib.getMessageIfExists(label, args);
   }

   public String getMessageWithFallback(String label,
      String fallbackFormat, Object... params)
   {
      if (helpLib == null)
      {
         MessageFormat fmt = new MessageFormat(fallbackFormat);

         return fmt.format(params);
      }

      return helpLib.getMessageWithFallback(label, fallbackFormat, params);
   }

   public String getMessage(String label, Object... params)
   {
      return helpLib.getMessage(label, params);
   }

   @Override
   public void message(String msg)
   {
      System.out.println(msg);
   }

   @Override
   public void warning(String msg)
   {
      JOptionPane.showMessageDialog(this, msg,
        getMessageWithFallback("warning.title", "Warning"),
        JOptionPane.ERROR_MESSAGE);
   }

   @Override
   public void warning(String msg, Throwable e)
   {
      JOptionPane.showMessageDialog(this, msg,
        getMessageWithFallback("warning.title", "Warning"),
        JOptionPane.ERROR_MESSAGE);

      e.printStackTrace();
   }

   @Override
   public void error(String msg)
   {
      JOptionPane.showMessageDialog(this, msg, 
        getMessageWithFallback("error.title", "Error"),
        JOptionPane.ERROR_MESSAGE);
   }

   @Override
   public void error(Throwable e)
   {
      String msg = e.getMessage();

      if (msg == null)
      {
         if (e.getCause() == null)
         {
            msg = e.toString();
         }
         else
         {
            msg = e.getCause().getMessage();

            if (msg == null)
            {
               msg = e.toString()+" caused by " + e.getCause();
            }
         }
      }

      error(msg);
      e.printStackTrace();
   }

   @Override
   public void error(String message, Throwable e)
   {
      error(message);
      e.printStackTrace();
   }

   @Override
   public void debug(String message, Throwable e)
   {
      if (debugMode > 0)
      {
         if (message != null)
         {
            System.err.println(message);
         }

         if (e != null)
         {
            e.printStackTrace();
         }
      }
   }

   @Override
   public void debug(String message)
   {
      debug(message, null);
   }

   @Override
   public void debug(Throwable e)
   {
      debug(e.getMessage(), e);
   }

   @Override
   public String getApplicationName()
   {
      return APP_NAME;
   }

   public static void main(final String[] args)
   {
      try
      {
         SwingUtilities.invokeAndWait(new Runnable()
         { 
            public void run()
            {
               TeXJavaHelpDemo gui = new TeXJavaHelpDemo();
               gui.parseArgs(args);

               try
               {
                  gui.init();
               }
               catch (Exception e)
               {
                  gui.error(e);
                  e.printStackTrace();
                  System.exit(1);
               }
            }
         });
      }
      catch (InterruptedException | java.lang.reflect.InvocationTargetException e)
      {
         String msg = e.getMessage();

         if (msg == null)
         {
            if (e.getCause() == null)
            {
               msg = e.toString();
            }
            else
            {
               msg = e.getCause().getMessage();

               if (msg == null)
               {
                  msg = e.toString()+" caused by "+e.getCause();
               }
            }
         }

         JOptionPane.showMessageDialog(null, msg, "Error",
           JOptionPane.ERROR_MESSAGE);

         System.err.println(e.getMessage());
         e.printStackTrace();
      }
   }

   private TeXJavaHelpLib helpLib;

   private Properties properties;

   private int debugMode = 1;

   public final static String APP_NAME = "TeX Java Help Demo";
}
