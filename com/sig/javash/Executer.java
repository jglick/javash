package com.sig.javash;
import java.util.*;

/**
 * Interface to be adhered to by scratch classes which will execute
 * some Java code but not return any result.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public interface Executer {
  /**
   * Do its thing.
   *
   * @param bindings Current bindings for scratch variables.
   * @exception java.lang.Throwable Arbitrary throw.
   *
   * @see com.sig.javash.Main#varValues
   */
  public void run(Dictionary bindings) throws Throwable;
}
