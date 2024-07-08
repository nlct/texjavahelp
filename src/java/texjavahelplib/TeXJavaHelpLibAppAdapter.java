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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Abstract class adapting TeXJavaHelpLibApp interface.
 */
public abstract class TeXJavaHelpLibAppAdapter implements TeXJavaHelpLibApp
{
   public void setHelpLib(TeXJavaHelpLib helpLib)
   {
      this.helpLib = helpLib;
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
   public ImageIcon getLargeIcon(String base, String... extensions)
   {
      return null;
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

      if (isGUI())
      {
         JOptionPane.showMessageDialog(owner, msg,
           getMessageWithFallback("warning.title", "Warning"),
           JOptionPane.WARNING_MESSAGE);
      }
      else
      {
         stdErrMessage(e, String.format("%s: %s",
           getApplicationName(), msg));
      }
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

      if (msg == null || msg.isEmpty())
      {
         // Not very helpful but better than being silent or
         // printing null!
         msg = getMessageWithFallback("error.title", "Error");
      }

      if (isGUI())
      {
         JOptionPane.showMessageDialog(owner, msg,
           getMessageWithFallback("error.title", "Error"),
           JOptionPane.ERROR_MESSAGE);
      }
      else
      {
         stdErrMessage(e, String.format("%s: %s",
           getApplicationName(), msg));
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

   protected TeXJavaHelpLib helpLib;
}
