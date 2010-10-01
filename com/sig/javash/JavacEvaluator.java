package com.sig.javash;

/**
 * An evaluator which uses the JDK Javac compiler in particular. The
 * advantage over InlineEvaluator lies in the fact that nothing
 * special need be done to the security manager.
 *
 * <p>On the other hand, this class may stop working if the
 * implementation of Javac is changed, since this is relying on
 * undocumented <code>sun.*</code> classes, though not heavily.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public class JavacEvaluator extends BasicEvaluator {
  /**
   * Create a new evaluator.
   */
  public JavacEvaluator() {}

  /**
   * Get a handler.
   *
   * @param code Source code.
   * @exception com.sig.javash.EvaluatorException The usual.
   */
  protected BasicEvaluatorHandler getHandler(String code) throws EvaluatorException {
    return new JavacEvaluatorHandler(code, loader);
  }
}
