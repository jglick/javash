package com.sig.javash;
import java.util.*;

/**
 * Interface to be adhered to by scratch class which will return
 * some result.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public interface Querier {
  /**
   * Do its thing and return a result.
   *
   * @param bindings Current bindings for scratch variables.
   * @return Some result.
   * @exception java.lang.Throwable Arbitrary throw.
   *
   * @see com.sig.javash.Main#varValues
   * @see com.sig.javash.Thing
   */
  public Thing run(Dictionary bindings) throws Throwable;
}
