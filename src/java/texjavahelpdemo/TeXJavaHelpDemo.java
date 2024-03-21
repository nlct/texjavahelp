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

import java.io.IOException;

import java.text.MessageFormat;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.*;

public class TeXJavaHelpDemo extends JFrame implements TeXJavaHelpLibApp
{
   protected TeXJavaHelpDemo()
   {
      super(APP_NAME);
   }

   protected void init() throws IOException
   {
      helpLib = new TeXJavaHelpLib(this);
      initGui();
      setVisible(true);
   }

   protected void initGui()
   {
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent evt)
         {
            if (confirmYesNo("message.confirm_quit")
               == JOptionPane.YES_OPTION)
            {
               quit();
            }
         }
      });

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width*3/4, dim.height*3/4);

      setLocationRelativeTo(null);
   }

   public void quit()
   {
      System.exit(0);
   }

   public int confirmYesNo(String tag)
   {
      return JOptionPane.showConfirmDialog(this,
         helpLib.getMessage(tag),
         helpLib.getMessage(tag+".title"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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

   public void error(Exception e)
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
   public String getApplicationName()
   {
      return APP_NAME;
   }

   public static void main(String[] args)
   {
      try
      {
         SwingUtilities.invokeAndWait(new Runnable()
         { 
            public void run()
            {
               TeXJavaHelpDemo gui = new TeXJavaHelpDemo();

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

   public final static String APP_NAME = "TeX Java Help Demo";
}
