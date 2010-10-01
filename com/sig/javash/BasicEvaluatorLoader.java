package com.sig.javash;
import java.io.*;

/**
 * A classloader specialized for handling scratch classes created on
 * the fly.
 *
 * <!-- $Format: " * <p>$JavashCopyright$"$ -->
 * <p>Copyright (c) 1998 Strategic Interactive Group. All rights reserved. This software may be redistributed under the terms of the GNU General Public License. There is no warranty whatsoever.
 *
 * @author Jesse Glick
 * <!-- $Format: " * @version $JavashRelease$"$ -->
 * @version 0.001
 */
public class BasicEvaluatorLoader extends ClassLoader {
  private static boolean debug=BasicEvaluator.debug;
  private static String tempDir=BasicEvaluator.tempDir;

  /**
   * The name of the main class currently being compiled.
   */
  protected String masterClass=null;

  /**
   * Create a new loader.
   */
  public BasicEvaluatorLoader() {}

  /**
   * Set the primary class name for now.
   *
   * @param c The classname.
   */
  public synchronized void setMasterClass(String c) {
    masterClass=c;
  }

  /**
   * Actually load the class, looking for it in the temporary
   * directory according to its package. Also keeps a cache of
   * already-loaded classes, and will try the system classloader as
   * a backup.
   *
   * @param name The classname.
   * @param resolve Whether it should be resolved.
   * @return The class object.
   * @exception java.lang.ClassNotFoundException If there was a
   * problem loading it.
   */
  protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (debug) System.err.println("Loading " + name);
    // Only load from cache/system if not the master class or an
    // inner class thereof.
    if (masterClass==null || !name.startsWith(masterClass)) {
      if (debug) System.err.println("Trying cache...");
      Class attempt=findLoadedClass(name);
      if (attempt != null) return attempt;
      if (debug) System.err.println("Trying system...");
      try {
	return findSystemClass(name);
      } catch (ClassNotFoundException ignore) {
      }
    }
    if (debug) System.err.println("Loading " + name + " afresh");
    String fqn=name.replace('.', File.separatorChar);
    File f=new File(tempDir, fqn + ".class");
    if (debug) System.err.println("Loading from file " + f);
    InputStream is=null;
    try {
      is=new FileInputStream(f);
    } catch (FileNotFoundException e) {
      throw new ClassNotFoundException("Could not load class file " + f + ": " + e);
    }
    int len=(int)(f.length());
    if (len==0)
      throw new ClassNotFoundException("Class file " + f + " was empty/nonexistent");
    byte buf[]=new byte[len];
    int offset=0;
    int count;
    try {
      while ((count=is.read(buf, offset, len-offset)) > 0)
	offset += count;
    } catch (IOException e) {
      throw new ClassNotFoundException("Could not read class file: " + e);
    }
    if (offset != len)
      throw new ClassNotFoundException("Didn't read whole file " + f);
    if (debug) System.err.println("Defining " + name);
    Class res=defineClass(null, buf, 0, len);
    if (resolve) {
      if (debug) System.err.println("Resolving " + name);
      resolveClass(res);
    }
    return res;
  }
}
