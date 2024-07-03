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
import javax.swing.ImageIcon;

/**
 * Interface for main application using Java Help Library.
 */
public interface TeXJavaHelpLibApp
{
   /**
   * Get the application name.
   * @return the application's name
   */
   public String getApplicationName();

   /**
   * Listener method called when a dictionary is loaded. This method may do nothing
   * if the application doesn't require this information.
   * @param url the URL of the dictionary that was just loaded
   */
   public void dictionaryLoaded(URL url);

   /**
    * Get application small icon if not on the default TeXJavaHelpLib search path.
    * May return null if the default search path is sufficient or
    * this method may map base to a different file name.
    * @param base the image base name or property to map
    * @param extensions suggested file extensions to search
    * @return the corresponding ImageIcon or null to fallback on
    * default search path
    */ 
   public ImageIcon getSmallIcon(String base, String... extensions);

   /**
    * Get application large icon if not on the default TeXJavaHelpLib search path.
    * May return null if the default search path is sufficient or
    * this method may map base to a different file name.
    * @param base the image base name or property to map
    * @param extensions suggested file extensions to search
    * @return the corresponding ImageIcon or null to fallback on
    * default search path
    */ 
   public ImageIcon getLargeIcon(String base, String... extensions);

   /**
    * General message, which the application may display to the user
    * or log or ignore.
    * @param msg the message
    */ 
   public void message(String msg);

   /**
    * Warning message, which the application may display to the user
    * or log or ignore.
    * @param warning the message
    */ 
   public void warning(String warning);

   /**
    * Warning message and exception, which the application may display to the user
    * or log or ignore.
    * @param warning the message
    * @param e an exception that has occurred
    */ 
   public void warning(String warning, Throwable e);

   /**
    * Error message, which the application should display to the user
    * or log.
    * @param message the error message
    */ 
   public void error(String message);

   /**
    * Exception, which the application should display to the user
    * or log.
    * @param e an exception that has occurred
    */ 
   public void error(Throwable e);

   /**
    * Error message and exception, which the application should display to the user
    * or log.
    * @param message the error message
    * @param e an exception that has occurred
    */ 
   public void error(String message, Throwable e);

   /**
    * Debugging message, which the application may display to the user
    * or log if debugging mode is on.
    * @param message the message
    */ 
   public void debug(String message);

   /**
    * Debugging exception, which the application may display to the user
    * or log if debugging mode is on. The exception is typically
    * non-critical, such as a parsing error that can be ignored.
    * @param e an exception that has occurred
    */ 
   public void debug(Throwable e);

   /**
    * Debugging message and exception, which the application may display to the user
    * or log if debugging mode is on. The exception is typically
    * non-critical, such as a parsing error that can be ignored.
    * @param message the message
    * @param e an exception that has occurred
    */ 
   public void debug(String message, Throwable e);

   public boolean isDebuggingOn();
}
