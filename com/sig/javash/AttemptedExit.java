package com.sig.javash;

/**
 * Thrown when the compiler class tries to exit the VM, instead of
 * actually exiting.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 *
 * @see com.sig.javash.InlineEvaluatorSecurityManager
 * @see com.sig.javash.InlineEvaluator#checkExits
 */
public class AttemptedExit extends SecurityException {
  /**
   * The intended exit status.
   */
  public int status;

  /**
   * Create an exit wrapper.
   *
   * @param status Intended exit status.
   */
  public AttemptedExit(int status) {
    this.status=status;
  }
}
