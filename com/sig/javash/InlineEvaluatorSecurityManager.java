package com.sig.javash;
import java.io.*;
import java.net.*;

/**
 * A security manager capable of trapping calls to
 * <code>System.exit(int)</code>. All other traps do nothing.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 *
 * @see com.sig.javash.InlineEvaluator#checkExits
 */
class InlineEvaluatorSecurityManager extends SecurityManager {
  /**
   * Just throw an <code>AttemptedExit</code>.
   *
   * @param status Intended exit status.
   * @exception com.sig.javash.AttemptedExit The notification.
   */
  public void checkExit(int status) {
    throw new AttemptedExit(status);
  }

  public void checkCreateClassLoader() {}
  public void checkAccess(Thread g) {}
  public void checkAccess(ThreadGroup g) {}
  public void checkExec(String cmd) {}
  public void checkLink(String lib) {}
  public void checkRead(FileDescriptor fd) {}
  public void checkRead(String file) {}
  public void checkRead(String file, Object context) {}
  public void checkWrite(FileDescriptor fd) {}
  public void checkWrite(String file) {}
  public void checkDelete(String file) {}
  public void checkConnect(String host, int port) {}
  public void checkConnect(String host, int port, Object context) {}
  public void checkListen(int port) {}
  public void checkAccept(String host, int port) {}
  public void checkMulticast(InetAddress maddr) {}
  public void checkMulticast(InetAddress maddr, byte ttl) {}
  public void checkPropertiesAccess() {}
  public void checkPropertyAccess(String key) {}
  public void checkPropertyAccess(String key, String def) {}
  public boolean checkTopLevelWindow(Object window) {return true;}
  public void checkPrintJobAccess() {}
  public void checkSystemClipboardAccess() {}
  public void checkAwtEventQueueAccess() {}
  public void checkPackageAccess(String pkg) {}
  public void checkPackageDefinition(String pkg) {}
  public void checkSetFactory() {}
  public void checkMemberAccess(Class clazz, int which) {}
  public void checkSecurityAccess(String provider) {}
}
