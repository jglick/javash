package com.sig.javash;
import java.io.*;

/**
 * Permits you to create a loaded, resolved, and otherwise ready-to-go
 * <code>Class</code> object directly from Java source code. This
 * class compiles the code using an external system command (e.g. you
 * could use guavac); subclasses may use different (hopefully more
 * efficient) techniques. One evaluator may be reused to make multiple
 * classes.
 *
 * <p>Recognized properties:
 *
 * <p><table border=1>
 *
 * <tr> <th>Name</th> <th>Description</th> <th>Default</th> </tr>
 *
 * <tr> <td><tt>com.sig.javash.BasicEvaluator.tempDir</tt></td>
 * <td>Name of temporary directory to store scratch classes and source
 * files into during evaluation.</td> <td><tt>.javash</tt></td> </tr>
 *
 * <tr>
 * <td><tt>com.sig.javash.BasicEvaluator.tempDirIsRelative</tt></td>
 * <td>Is the temporary directory to be understood as relative to the
 * user's home directory?</td> <td>true</td> </tr>
 *
 * <tr> <td><tt>com.sig.javash.BasicEvaluator.debug</tt></td>
 * <td>Debug operation?</td> <td>false</td> </tr>
 *
 * <tr> <td><tt>com.sig.javash.BasicEvaluator.compiler</tt></td>
 * <td>What compiler pathname should be used?</td>
 * <td><tt>javac</tt></td> </tr>
 *
 * </table>
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 *
 * @see com.sig.javash.InlineEvaluator
 */
public class BasicEvaluator implements Evaluator {
  /**
   * What scratch directory should be used?
   */
  public static final String tempDir=initTempDir();
  // JDK 1.1 javac compiler bug: tricky to definitely assign it.
  private static String initTempDir() {
    String base=
      System.getProperty("com.sig.javash.BasicEvaluator.tempDir", ".javash");
    boolean isRel=
      System.getProperty("com.sig.javash.BasicEvaluator.tempDirIsRelative")==null ?
      true :
      Boolean.getBoolean("com.sig.javash.BasicEvaluator.tempDirIsRelative");
    if (isRel)
      return System.getProperty("user.home") + File.separator + base;
    else
      return base;
  }

  /**
   * Should debugging statements be turned on?
   */
  public static final boolean debug=
    Boolean.getBoolean("com.sig.javash.BasicEvaluator.debug");

  /**
   * What compiler path should be used?
   */
  public static final String compiler=
    System.getProperty("com.sig.javash.BasicEvaluator.compiler", "javac");

  /**
   * Custom classloader capable of loading & resolving generated classes.
   */
  protected BasicEvaluatorLoader loader;

  /**
   * Make a new evaluator.
   */
  public BasicEvaluator() {
    loader=new BasicEvaluatorLoader();
  }

  /**
   * Create a handler for the indicated source code. Subclasses should
   * generally override this to use their own handlers.
   *
   * @param code Java source code for a class.
   * @return The handler.
   * @exception com.sig.javash.EvaluatorException If there
   * is a problem.
   */
  protected BasicEvaluatorHandler getHandler(String code) throws EvaluatorException {
    return new BasicEvaluatorHandler(code, loader);
  }

  /**
   * Compile and load the source code and produce a ready-to-use class object.
   *
   * @param code Java source code for a class or interface.
   * @return The compiled and loaded class object.
   * @exception com.sig.javash.EvaluatorException If there
   * is a problem.
   */
  public Class evaluate(String code) throws EvaluatorException {
    BasicEvaluatorHandler h=getHandler(code);
    h.save();
    h.compile();
    synchronized (loader) {
      loader.setMasterClass(h.getName());
      return h.load();
    }
  }
}
