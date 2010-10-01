package com.sig.javash;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * Compile code with an in-VM compiler.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public class InlineEvaluatorHandler extends BasicEvaluatorHandler {
  private static String tempDir=BasicEvaluator.tempDir;
  private static String compilerClass=InlineEvaluator.compilerClass;

  /**
   * Create a new handler. Only the compilation technique differs.
   *
   * @param code Source code.
   * @param loader Class loader.
   * @param cx A new security manager.
   * @exception com.sig.javash.EvaluatorException The usual.
   */
  public InlineEvaluatorHandler(String code, BasicEvaluatorLoader loader,
				SecurityManager cx) throws EvaluatorException {
    super(code, loader);
    SecurityManager mgr=System.getSecurityManager();
    if (mgr==null)
      System.setSecurityManager(cx);
    else if (mgr != cx)
      throw new EvaluatorException("already have security manager installed: " + mgr);
  }

  /**
   * Compile using an in-VM static method call, rather than a system
   * command.
   *
   * @exception com.sig.javash.EvaluatorException The usual.
   */
  protected void compile() throws EvaluatorException {
    Properties oldp=System.getProperties();
    Properties newp=(Properties)(oldp.clone());
    // Ugly!
    newp.put("java.class.path",
	     tempDir + File.pathSeparator + newp.get("java.class.path"));
    try {
      System.setProperties(newp);
      String toCompile=tempDir + File.separator + fqn + ".java";
      Class c=Class.forName(compilerClass);
      c.getMethod("main", new Class[] {String[].class})
	.invoke(null, new Object[] {new String[] {toCompile}});
    } catch (ClassNotFoundException e) {
      throw new EvaluatorException("compiler class not found: " + e);
    } catch (NoSuchMethodException e) {
      throw new EvaluatorException("compiler class has no main method: " + e);
    } catch (IllegalAccessException e) {
      throw new EvaluatorException("cannot run compiler's main method:" + e);
    } catch (InvocationTargetException e) {
      Throwable ee=e.getTargetException();
      if (ee instanceof AttemptedExit) {
	int exit=((AttemptedExit)ee).status;
	if (exit != 0)
	  throw new EvaluatorException
	    ("compiler exiting with non-zero status: " + exit);
      } else
	throw new EvaluatorException("compiler raised an exception: " + ee);
    } finally {
      System.setProperties(oldp);
    }
  }
}
