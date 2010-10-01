package com.sig.javash;

/**
 * An object which somehow compiles & loads Java source.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public interface Evaluator {
  /**
   * Do the work.
   *
   * @param code Source code for a class or interface.
   * @exception com.sig.javash.EvaluatorException In case of trouble.
   * @return A ready-to-use class object.
   */
  public Class evaluate(String code) throws EvaluatorException;
}
