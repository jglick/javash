package com.sig.javash;

/**
 * An evaluator implementation that assumes that the compiler is
 * implemented as a Java application, which can thus be run directly
 * from this VM as a regular "Main" method. Traps attempted VM exits
 * and uses the status code to check whether the compile was
 * successful. Designed for the JDK javac, which has this behavior.
 *
 * <p>Recognized properties:
 *
 * <p><table border=1>
 *
 * <tr> <th>Name</th> <th>Description</th> <th>Default</th> </tr>
 *
 * <tr> <td><tt>com.sig.javash.InlineEvaluator.compilerClass</tt></td>
 * <td>What Java class should be used for the compiler?</td>
 * <td><tt>sun.tools.javac.Main</tt></td> </tr>
 *
 * </table>
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public class InlineEvaluator extends BasicEvaluator {
  /**
   * Class of compiler.
   */
  public static final String compilerClass=
    System.getProperty("com.sig.javash.InlineEvaluator.compilerClass",
		       "sun.tools.javac.Main");

  /**
   * Create a new evaluator.
   */
  public InlineEvaluator() {}

  /**
   * A <code>SecurityManager</code> which is installed to trap
   * attempted calls to <code>System.exit(int)</code> by the compiler.
   *
   * @see com.sig.javash.InlineEvaluatorSecurityManager
   * @see com.sig.javash.AttemptedExit
   */
  protected static final SecurityManager checkExits=
    new InlineEvaluatorSecurityManager();

  /**
   * Gets a suitable compilation handler.
   *
   * @param code Source code.
   * @exception com.sig.javash.EvaluatorException The usual.
   */
  protected BasicEvaluatorHandler getHandler(String code) throws EvaluatorException {
    return new InlineEvaluatorHandler(code, loader, checkExits);
  }
}
