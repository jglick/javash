package com.sig.javash;
import java.util.*;
import java.io.*;

/**
 * Compile with Javac.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public class JavacEvaluatorHandler extends BasicEvaluatorHandler {
  private static String tempDir=BasicEvaluator.tempDir;

  /**
   * Create a new handler.
   *
   * @param code Source code.
   * @param loader Class loader.
   * @exception com.sig.javash.EvaluatorException The usual.
   */
  public JavacEvaluatorHandler(String code, BasicEvaluatorLoader loader)
    throws EvaluatorException {
    super(code, loader);
  }

  /**
   * Compile. Mostly similar to <code>InlineEvaluatorHandler</code>.
   *
   * @exception com.sig.javash.EvaluatorException The usual.
   *
   * @see com.sig.javash.InlineEvaluatorHandler#compile
   */
  protected void compile() throws EvaluatorException {
    Properties oldp=System.getProperties();
    Properties newp=(Properties)(oldp.clone());
    newp.put("java.class.path",
	     tempDir + File.pathSeparator + newp.get("java.class.path"));
    try {
      System.setProperties(newp);
      String toCompile=tempDir + File.separator + fqn + ".java";
      if (!new sun.tools.javac.Main(System.err, "javac").
	  compile(new String[] {"-deprecation", toCompile}))
	throw new EvaluatorException("Javac compilation failed");
    } finally {
      System.setProperties(oldp);
    }
  }
}
