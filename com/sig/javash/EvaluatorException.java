package com.sig.javash;

/**
 * Problem occurring within the evaluator.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public class EvaluatorException extends Exception {
  /**
   * Create a new evaluator exception.
   *
   * @param s The message.
   */
  public EvaluatorException(String s) {super(s);}
}
