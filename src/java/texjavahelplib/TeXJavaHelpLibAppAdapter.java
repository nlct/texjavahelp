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

import java.net.URL;
import java.text.MessageFormat;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * Abstract class adapting TeXJavaHelpLibApp interface.
 */
public abstract class TeXJavaHelpLibAppAdapter implements TeXJavaHelpLibApp
{
   public void setHelpLib(TeXJavaHelpLib helpLib)
   {
      this.helpLib = helpLib;
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   public abstract boolean isGUI();

   public abstract String getApplicationName();

   public abstract boolean isDebuggingOn();

   @Override
   public void dictionaryLoaded(URL url) { }

   @Override
   public ImageIcon getSmallIcon(String base, String... extensions)
   {
      return null;
   }

   @Override
   public IconSet getSmallIconSet(String base, String... extensions)
   {
      ImageIcon ic = getSmallIcon(base, extensions);
      IconSet icSet = null;

      if (ic != null)
      {
         icSet = new IconSet(ic);

         ic = getSmallIcon(base+"_pressed", extensions);

         if (ic != null)
         {
            icSet.setPressedIcon(ic);
         }

         ic = getSmallIcon(base+"_selected", extensions);

         if (ic != null)
         {
            icSet.setSelectedIcon(ic);
         }

         ic = getSmallIcon(base+"_rollover", extensions);

         if (ic != null)
         {
            icSet.setRolloverIcon(ic);
         }

         ic = getSmallIcon(base+"_rollover_selected", extensions);

         if (ic != null)
         {
            icSet.setRolloverSelectedIcon(ic);
         }

         ic = getSmallIcon(base+"_disabled", extensions);

         if (ic != null)
         {
            icSet.setDisabledIcon(ic);
         }

         ic = getSmallIcon(base+"_disabled_selected", extensions);

         if (ic != null)
         {
            icSet.setDisabledSelectedIcon(ic);
         }
      }

      return icSet;
   }

   @Override
   public ImageIcon getLargeIcon(String base, String... extensions)
   {
      return null;
   }

   @Override
   public IconSet getLargeIconSet(String base, String... extensions)
   {
      ImageIcon ic = getLargeIcon(base, extensions);
      IconSet icSet = null;

      if (ic != null)
      {
         icSet = new IconSet(ic);

         ic = getLargeIcon(base+"_pressed", extensions);

         if (ic != null)
         {
            icSet.setPressedIcon(ic);
         }

         ic = getLargeIcon(base+"_selected", extensions);

         if (ic != null)
         {
            icSet.setSelectedIcon(ic);
         }

         ic = getLargeIcon(base+"_rollover", extensions);

         if (ic != null)
         {
            icSet.setRolloverIcon(ic);
         }

         ic = getLargeIcon(base+"_rollover_selected", extensions);

         if (ic != null)
         {
            icSet.setRolloverSelectedIcon(ic);
         }

         ic = getLargeIcon(base+"_disabled", extensions);

         if (ic != null)
         {
            icSet.setDisabledIcon(ic);
         }

         ic = getLargeIcon(base+"_disabled_selected", extensions);

         if (ic != null)
         {
            icSet.setDisabledSelectedIcon(ic);
         }
      }

      return icSet;
   }

   public String getMessageWithFallback(String label, String fallbackFormat,
     Object... params)
   {
      if (helpLib == null)
      {
         if (params.length > 0)
         {
            MessageFormat fmt = new MessageFormat(fallbackFormat);
            return fmt.format(params);
         }
         else
         {
            return fallbackFormat;
         }
      }
      else
      {
         return helpLib.getMessageWithFallback(label, fallbackFormat, params);
      }
   }

   public int getMnemonicWithFallback(String label, int defVal)
   {
      int mnemonic = -1;

      if (helpLib != null)
      {
         mnemonic = helpLib.getMnemonic(label);
      }

      return mnemonic < 0 ? defVal : mnemonic;
   }

   public void stdOutMessage(String msg)
   {
      System.out.println(msg);
   }

   public void stdErrMessage(Throwable e, String msg)
   {
      if (msg != null)
      {
         System.err.println(msg);
      }

      if (e != null)
      {
         e.printStackTrace();
      }
   }

   @Override
   public void message(String msg)
   {
      if (!isGUI())
      {
         stdOutMessage(msg);
      }
   }

   @Override
   public void warning(String msg)
   {
      warning(null, msg);
   }

   @Override
   public void warning(Component owner, String msg)
   {
      warning(owner, msg, null);
   }

   @Override
   public void warning(String msg, Throwable e)
   {
      warning(null, msg, e);
   }

   @Override
   public void warning(Component owner, String msg, Throwable e)
   {
      if (msg == null && e != null)
      {
         msg = e.getMessage();

         if (msg == null)
         {
            if (e.getCause() == null)
            {
               msg = e.toString();
            }
            else
            {
               msg = e.getCause().getMessage();
            }
         }
      }

      if (warningTitle == null)
      {
         warningTitle = getMessageWithFallback("warning.title", "Warning");
      }

      if (isGUI())
      {
         showMessageDialog(owner, msg, warningTitle, JOptionPane.WARNING_MESSAGE);

         if (e != null && isDebuggingOn())
         {
            e.printStackTrace();
         }
      }
      else
      {
         stdErrMessage(e, String.format("%s: %s %s",
           getApplicationName(), warningTitle, msg));
      }
   }

   protected void showMessageDialog(Component owner, String msg,
     String title, int type)
   {
      if (errWarnMessageArea == null)
      {
         initStackTraceMessageArea();
      }

      errWarnMessageArea.setText(msg);

      JOptionPane.showMessageDialog(owner, errWarnMessageAreaSP, title, type);
   }

   @Override
   public void error(String message)
   {
      error(null, message, null);
   }

   @Override
   public void error(Component owner, String msg)
   {
      error(owner, msg, null);
   }

   @Override
   public void error(Throwable e)
   {
      error(null, null, e);
   }

   @Override
   public void error(Component owner, Throwable e)
   {
      error(owner, null, e);
   }

   @Override
   public void error(String message, Throwable e)
   {
      error(null, message, e);
   }

   @Override
   public void error(Component owner, String msg, Throwable e)
   {
      if (msg == null && e != null)
      {
         msg = e.getMessage();

         if (msg == null)
         {
            if (e.getCause() == null)
            {
               msg = e.toString();
            }
            else
            {
               msg = e.getCause().getMessage();
            }
         }
      }

      if (errorTitle == null)
      {
          errorTitle = getMessageWithFallback("error.title", "Error");
      }

      if (msg == null || msg.isEmpty())
      {
         // Not very helpful but better than being silent or
         // printing null!
         msg = errorTitle;
      }

      if (isGUI())
      {
         if (e != null)
         {
            displayStackTrace(owner, errorTitle, msg, e);
         }
         else
         {
            showMessageDialog(owner, msg, errorTitle, JOptionPane.ERROR_MESSAGE);
         }
      }
      else
      {
         stdErrMessage(e, String.format("%s: %s",
           getApplicationName(), msg));
      }
   }

   public void internalError(Component owner, String msg, Throwable e)
   {
      if (msg == null && e != null)
      {
         msg = e.getMessage();

         if (msg == null)
         {
            if (e.getCause() == null)
            {
               msg = e.toString();
            }
            else
            {
               msg = e.getCause().getMessage();
            }
         }
      }

      if (internalErrorTitle == null)
      {
          internalErrorTitle =
             getMessageWithFallback("internal_error.title", "Internal Error");
      }

      if (msg == null || msg.isEmpty())
      {
         // Not very helpful but better than being silent or
         // printing null!
         msg = internalErrorTitle;
      }

      if (isGUI())
      {
         if (e != null)
         {
            displayStackTrace(owner, internalErrorTitle, msg, e);
         }
         else
         {
            if (okayOptionText == null)
            {
               okayOptionText = getMessageWithFallback("action.okay", "Okay");
            }

            if (crashExitOptionText == null)
            {
               crashExitOptionText = getMessageWithFallback(
                  "action.quit_without_saving",
                  "Quit Without Saving");
            }

            int result = JOptionPane.showOptionDialog(owner, msg,
               internalErrorTitle,
               JOptionPane.YES_NO_OPTION,
               JOptionPane.ERROR_MESSAGE, null,
               new String[] {okayOptionText, crashExitOptionText}, okayOptionText);

            if (result == JOptionPane.NO_OPTION)
            {
               System.exit(fatalErrorExitCode);
            }
         }
      }
      else
      {
         stdErrMessage(e, String.format("%s: %s: %s",
           getApplicationName(), internalErrorTitle, msg));
         System.exit(fatalErrorExitCode);
      }
   }


   @Override
   public void debug(String message)
   {
      debug(null, message, null);
   }

   @Override
   public void debug(Component owner, String message)
   {
      debug(owner, message, null);
   }

   @Override
   public void debug(Throwable e)
   {
      debug(null, null, e);
   }

   @Override
   public void debug(Component owner, Throwable e)
   {
      debug(owner, null, e);
   }

   @Override
   public void debug(String message, Throwable e)
   {
      debug(null, message, e);
   }

   @Override
   public void debug(Component owner, String msg, Throwable e)
   {
      if (isDebuggingOn())
      {
         if (e == null)
         {
            stdOutMessage(msg);
         }
         else
         {
            stdErrMessage(e, msg);
         }
      }
   }

   /**
    * Displays stack trace in a dialog box with the option for the
    * user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param frameTitle the title for the dialog box
    * @param e the exception with the required stack trace
    */
   public void displayStackTrace(Component parent,
       String frameTitle, String message, Throwable e)
   {
      if (stackTracePane == null)
      {
         initStackTracePane();
      }

      errWarnMessageArea.setText(message);

      StackTraceElement[] trace = e.getStackTrace();
      StringBuilder stackTrace = new StringBuilder();

      for (int i = 0, n=trace.length; i < n; i++)
      {
         stackTrace.append(String.format("%s%n", trace[i]));
      }

      if (e.getCause() != null)
      {
         appendStackTrace(e, stackTrace);
      }

      stackTraceDetails.setText(stackTrace.toString());

      int result = JOptionPane.showOptionDialog(parent, stackTracePane,
         frameTitle,
         JOptionPane.YES_NO_OPTION,
         JOptionPane.ERROR_MESSAGE, null,
         new String[] {okayOptionText, crashExitOptionText}, okayOptionText);

      if (result == JOptionPane.NO_OPTION)
      {
         System.exit(fatalErrorExitCode);
      }
   }

   protected void appendStackTrace(Throwable e, StringBuilder stackTrace)
   {
      if (e != null)
      {
         StackTraceElement[] trace = e.getStackTrace();

         stackTrace.append(String.format("%n%s%n", e));

         for (int i = 0, n=trace.length; i < n; i++)
         {
            stackTrace.append(String.format("%s%n", trace[i]));
         }

         if (e.getCause() != null)
         {
            appendStackTrace(e.getCause(), stackTrace);
         }
      }
   }

   protected void initStackTraceMessageArea()
   {
      errWarnMessageArea = new JTextArea(20,50);
      errWarnMessageArea.setEditable(false);
      errWarnMessageArea.setLineWrap(true);
      errWarnMessageArea.setWrapStyleWord(true);

      errWarnMessageAreaSP = new JScrollPane(errWarnMessageArea);
   }

   protected void initStackTracePane()
   {  
      stackTracePane = new JTabbedPane();
      String title = getMessageWithFallback("stacktrace.message", "Error Message");

      if (errWarnMessageArea == null)
      {
         initStackTraceMessageArea();
      }
      
      stackTracePane.addTab(title, null, errWarnMessageAreaSP, title);

      JPanel p2 = new JPanel();
      stackTraceDetails = new JTextArea(20,50);
      stackTraceDetails.setEditable(false);

      p2.add(new JScrollPane(stackTraceDetails), "Center");

      JButton copyButton =
         new JButton(getMessageWithFallback("action.copy", "Copy"));
      copyButton.setMnemonic(
        getMnemonicWithFallback("action.copy.mnemonic", (int)'C'));
      copyButton.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent evt)
          {
             stackTraceDetails.selectAll();
             stackTraceDetails.copy();
          }
       });

      p2.add(copyButton,"South");

      title = getMessageWithFallback("stacktrace.details", "Details");
      stackTracePane.addTab(title, null, p2, title);

      if (okayOptionText == null)
      {
         okayOptionText = getMessageWithFallback("action.okay", "Okay");
      }

      if (crashExitOptionText == null)
      {
         crashExitOptionText = getMessageWithFallback("action.quit_without_saving",
            "Quit Without Saving");
      }
   }

   protected TeXJavaHelpLib helpLib;

   private JTabbedPane stackTracePane;
   private JScrollPane errWarnMessageAreaSP;
   private JTextArea errWarnMessageArea, stackTraceDetails;
   private String okayOptionText, crashExitOptionText, errorTitle, internalErrorTitle, warningTitle;
   protected int fatalErrorExitCode = 100;
}
