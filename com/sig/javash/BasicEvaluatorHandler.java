package com.sig.javash;
import java.util.*;
import java.io.*;

/**
 * The class defining how to compile the source code and deposit the
 * class files into the scratch directory.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public class BasicEvaluatorHandler {
  private static String tempDir=BasicEvaluator.tempDir;
  private static boolean debug=BasicEvaluator.debug;
  private static String compiler=BasicEvaluator.compiler;

  /**
   * Text of the raw source code.
   */
  protected String code;

  /**
   * Classname, including package.
   */
  protected String name;

  /**
   * Classname, including package. Uses filesystem path separator
   * between components.
   */
  protected String fqn;

  /**
   * Classname (without package).
   */
  protected String classname;

  /**
   * Java package of class.
   */
  protected String pkg;

  /**
   * Loader.
   */
  protected BasicEvaluatorLoader loader;

  /**
   * Create the handler and set up some preliminary information from
   * the source code.
   *
   * @param code Complete Java source code for a class or interface.
   * @param loader Loader for the classfiles.
   * @exception com.sig.javash.EvaluatorException If the
   * source code is malformed somehow.
   */
  public BasicEvaluatorHandler(String code, BasicEvaluatorLoader loader)
    throws EvaluatorException {
    this.code=code;
    this.loader=loader;
    // Ghod this sux.  Get class name & package. Assume no weird
    // comments. Could use StreamTokenizer instead?
    StringTokenizer tok=new StringTokenizer(code, " \t\n\r;");
    pkg="";
    while (tok.hasMoreTokens()) {
      String next=tok.nextToken();
      if (next.equals("package")) {
	if (tok.hasMoreTokens())
	  pkg=tok.nextToken() + ".";
	else
	  throw new EvaluatorException("Dangling package statement");
      }
      if (next.equals("class") || next.equals("interface")) {
	if (tok.hasMoreTokens()) {
	  classname=tok.nextToken();
	  break;		// ignore inner classes etc.
	}
	else
	  throw new EvaluatorException("Dangling " + next + " statement");
      }
    }
    if (classname==null) throw new EvaluatorException("No class declaration found");
    name=pkg + classname;
    fqn=pkg.replace('.', File.separatorChar) + classname;
    if (debug) {
      System.err.println("Code:");
      System.err.print(code);
      System.err.println("Package: " + pkg);
      System.err.println("Classname: " + classname);
      System.err.println("FQN: " + fqn);
    }
  }

  /**
   * Get the primary classname.
   *
   * @return The classname.
   */
  protected String getName() {
    return name;
  }

  /**
   * Save the source code into a disk file.
   *
   * @exception com.sig.javash.EvaluatorException If there
   * was a problem writing it out.
   */
  protected void save() throws EvaluatorException {
    if (debug) System.err.println("Saving...");
    try {
      StringBuffer parentName=new StringBuffer(tempDir);
      StringTokenizer tok=new StringTokenizer(pkg, ".");
      while (tok.hasMoreTokens()) {
	parentName.append(File.separator);
	parentName.append(tok.nextToken());
      }
      File parent=new File(parentName.toString());
      if (!parent.exists() && !parent.mkdirs())
	throw new EvaluatorException
	  ("Could not make directory to save source into: " + parentName);
      OutputStreamWriter out=new FileWriter(new File(tempDir, fqn + ".java"));
      out.write(code, 0, code.length());
      out.close();
    } catch (IOException e) {
      throw new EvaluatorException("Could not write out class source: " + e);
    }
    if (debug) System.err.println("...done saving.");
  }

  /**
   * Compile the source code on disk. This implementation runs an
   * external system command for the compiler.
   *
   * @exception com.sig.javash.EvaluatorException If there
   * was a problem compiling it.
   */
  protected void compile() throws EvaluatorException {
    try {
      String toCompile=tempDir + File.separator + fqn + ".java";
      String[] argv={compiler, toCompile};
      String classpath=tempDir + File.pathSeparator +
	System.getProperty("java.class.path");
      String[] envp={"CLASSPATH=" + classpath};
      if (debug) System.err.println("Compiling with `" + envp[0] + " " +
				    compiler + " " + toCompile + "'...");
      Process p=Runtime.getRuntime().exec(argv, envp);
      BufferedReader err=new BufferedReader
	(new InputStreamReader(p.getErrorStream()));
      String line;
      while ((line=err.readLine()) != null)
	System.err.println(line);
      p.waitFor();
      int x=p.exitValue();
      if (x != 0)
	throw new EvaluatorException("Could not compile code (bad status): " + x);
    } catch (InterruptedException e) {
      throw new EvaluatorException("Could not compile code (was interrupted): " + e);
    } catch (IOException e) {
      throw new EvaluatorException("Problem with compiler: " + e);
    }
    if (debug) System.err.println("...done compiling.");
  }

  /**
   * Use the loader object to retrieve the resulting class.
   *
   * @return The class object.
   * @exception com.sig.javash.EvaluatorException If there
   * was a problem loading the class.
   */
  protected Class load() throws EvaluatorException {
    if (debug) System.err.println("Loading " + name + "...");
    try {
      Class res=loader.loadClass(name);
      if (debug) System.err.println("...done loading " + name + ".");
      return res;
    } catch (ClassNotFoundException e) {
      throw new EvaluatorException("Could not load compiled class: " + e);
    }
  }
}
