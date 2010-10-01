package com.sig.javash;
import java.io.*;
import java.util.*;
/* import java.util.zip.*; */

/**
 * JavaShell main class.
 *
 * <p>JavaShell allows you to interactively evaluate Java expressions
 * and inspect the results, avoiding the hassle of creating endless
 * files named <tt>/tmp/Test99.java</tt>, compiling them, and trying
 * to figure out how to get them to print structured information as a
 * result.
 *
 * <p>JavaShell is entirely text-based and is well suited to running
 * inside Emacs.
 *
 * <p>Run this class like this: <code>java
 * com.sig.javash.Main</code>. There is a little online help
 * accessible by typing <tt>?</tt>.
 *
 * <p>JDK 1.1 is required; 1.2 will be useful for getting full object
 * inspection, including private or protected fields. You may use
 * inner classes in commands and expressions.
 *
 * <p>Recognized parameters:
 *
 * <p><table border=1>
 *
 * <tr> <th>Name</th> <th>Description</th> <th>Default</th> </tr>
 *
 * <tr> <td><tt>com.sig.javash.Main.shell</tt></td> <td>Shell to use
 * for system commands. May have arguments as needed. Actual command
 * string will be passed to this shell as a lone final
 * argument. Normally no environment variables will be set thanks to
 * Java, so you might try e.g. <tt>bash -login -c</tt>, though this
 * will be slower.</td> <td><tt>sh -c</tt></td> </tr>
 *
 * <tr> <td><tt>com.sig.javash.Main.evaluatorClass</tt></td> <td>Name
 * of class implementing Evaluator to use.</td>
 * <td><code>com.sig.javash.JavacEvaluator</code></td> </tr>
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
public class Main {
  /**
   * JavaShell version.
   */
  // $Format: "  public final static String javashVersion=\"$JavashRelease$\";"$
  public final static String javashVersion="0.001";

  /**
   * A command shell to use for system commands.
   */
  public final static String[] shell;
  static {
    String blob=System.getProperty("com.sig.javash.Main.shell", "sh -c");
    StringTokenizer tok=new StringTokenizer(blob, " \t\n\r");
    int count=0;
    while (tok.hasMoreTokens()) {
      tok.nextToken();
      count++;
    }
    shell=new String[count];
    tok=new StringTokenizer(blob, " \t\n\r");
    int index=0;
    while (tok.hasMoreTokens())
      shell[index++]=tok.nextToken();
  }

  /**
   * Classname to use for evaluation. Must implement <code>Evaluator</code>.
   *
   * @see com.sig.javash.Evaluator
   */
  public static final String evaluatorClass=
    System.getProperty("com.sig.javash.Main.evaluatorClass",
           "com.sig.javash.JavacEvaluator");

  /**
   * Run the shell.
   *
   * @param argv The argument list (ignored).
   */
  public static void main(String[] argv) {
    new Main().run();
  }

  /**
   * Object wrappers for primitive Java types.
   */
  protected final static Dictionary wrappers=new Hashtable();
  static {
    wrappers.put("boolean", "Boolean");
    wrappers.put("char", "Character");
    wrappers.put("byte", "Byte");
    wrappers.put("short", "Short");
    wrappers.put("int", "Integer");
    wrappers.put("long", "Long");
    wrappers.put("float", "Float");
    wrappers.put("double", "Double");
  }

  /**
   * JavaShell's input stream from which commands are read.
   */
//  protected BufferedReader in;
  protected InputStream in;

  /**
   * JavaShell's output stream to which the prompt and results are
   * printed.
   */
  protected PrintStream out;

  /**
   * Current Java package in which to evaluate expressions.
   */
  protected String pkg=null;

  /**
   * List of imported Java packages (may include package globs) for
   * use by evaluated expressions.
   */
  protected Vector imports=new Vector();

  /**
   * A table listing the Java types of currently defined scratch variables.
   */
  protected Dictionary varTypes=new Hashtable();

  /**
   * A table indicating which of the scratch variables are of
   * primitive Java type (vs. reference type).
   */
  protected Dictionary varTypesArePrimitive=new Hashtable();

  /**
   * A table of the current values of the scratch variables. These are
   * stored as Thing objects.
   *
   * @see com.sig.javash.Thing
   */
  protected Dictionary varValues=new Hashtable();

  /**
   * The expression evaluator to use.
   *
   * @see evaluatorClass
   */
  protected Evaluator evaluator;
    {
      initEvaluator();
    }
  // Need this crap to work around JDK 1.1 javac bug.
  private void initEvaluator() {
    try {
      evaluator=(Evaluator)(Class.forName(evaluatorClass).newInstance());
    } catch (Exception e) {
      System.err.println("Could not create Evaluator instance of class " +
       evaluatorClass + ": " + e);
      System.exit(1);
    }
  }

  /**
   * A table indicating which classes have already been displayed by
   * the inspector and may subsequently be abbreviated.
   *
   * @see com.sig.javash.Thing#inspect
   */
  protected Dictionary inspectDictionary=new Hashtable();

  /**
   * Create an interpreter with default I/O streams.
   */
  public Main() {
 //   this(new BufferedReader(new InputStreamReader(System.in)),
 //  System.out);
    this(System.in, System.out);
  }

  /**
   * Create a new interpreter.
   *
   * @param in The input stream.
   * @param out The output stream.
   */
  public Main(/* BufferedReader */ InputStream in, PrintStream out) {
    this.in=in;
    this.out=out;
  }

  /**
   * Display a very brief help message.
   */
  protected void help() {
    out.println("For help, type `?'.");
  }

  /**
   * Produce a Java source preamble to prepend to class definitions to
   * be fed to the evaluator. Covers the current package and imports.
   *
   * @return The preamble.
   *
   * @see #pkg
   * @see #imports
   */
  protected String getPreamble() {
    StringBuffer s=new StringBuffer();
    if (pkg != null) {
      s.append("package ");
      s.append(pkg);
      s.append(";\n");
    }
    Enumeration e=imports.elements();
    while (e.hasMoreElements()) {
      s.append("import ");
      s.append((String)(e.nextElement()));
      s.append(";\n");
    }
    return s.toString();
  }

  /**
   * Create a scratch classname for evaluation purposes. <p>Currently
   * this classname encodes the currently running <code>Main</code>
   * object and the time. Effectively you will have a shell history
   * stored in source form in the temporary directory.
   *
   * @return A fresh, valid classname.
   */
  protected String gensym() {
    return "$JavaSh$" +
      // XOR ensures that we will not always get the same address for
      // the initial Main object, as will o.w. happen under 1.1 (tho
      // not 1.2).
      Long.toHexString(hashCode() ^ creationTime) + "_" +
      Long.toHexString(System.currentTimeMillis());
  }
  private long creationTime;
    {
      creationTime=System.currentTimeMillis();
    }

  /**
   * Create Java code to handle scratch variables. Two code segments
   * are returned: a preamble which declares the scratch variables and
   * binds them to values according to the current variables types and
   * values; and a postamble which assigns the final (possibly
   * modified) scratch variable values back into the bindings
   * table. Collectively these provide a bidirectional mapping between
   * the Java local variables present in the resulting scratch class,
   * and the interpreter's table of persistent scratch variables,
   * which are available from one evaluation to the next (for a given
   * interpreter instance).
   *
   * @return An array consisting of the preamble and postamble (in
   * that order).
   *
   * @see #varValues
   * @see #varTypes
   * @see #varTypesArePrimitive
   * @see com.sig.javash.Thing
   */
  protected String[] makeBindings() {
    StringBuffer bindingsIn=new StringBuffer();
    StringBuffer bindingsOut=new StringBuffer();
    Enumeration e=varValues.keys();
    while (e.hasMoreElements()) {
      String var=(String)(e.nextElement());
      String type=(String)(varTypes.get(var));
      boolean isPrim=((Boolean)(varTypesArePrimitive.get(var))).booleanValue();
      bindingsIn.append("    ");
      bindingsIn.append(type);
      bindingsIn.append(" ");
      bindingsIn.append(var);
      bindingsIn.append("=");
      if (isPrim) {
  bindingsIn.append("((java.lang.");
  String wrapper=(String)(wrappers.get(type));
  if (wrapper==null)
    throw new IllegalArgumentException("Primitive type " + type + " has no wrapper");
  bindingsIn.append(wrapper);
  bindingsIn.append(")(((com.sig.javash.Thing)(bindings.get(\"");
  bindingsIn.append(var);
  bindingsIn.append("\"))).getThing())).");
  bindingsIn.append(type);
  bindingsIn.append("Value();\n");
      } else {
  bindingsIn.append("(");
  bindingsIn.append(type);
  bindingsIn.append(")(((com.sig.javash.Thing)(bindings.get(\"");
  bindingsIn.append(var);
  bindingsIn.append("\"))).getThing());\n");
      }
      bindingsOut.append("      bindings.put(\"");
      bindingsOut.append(var);
      bindingsOut.append("\", new com.sig.javash.Thing(");
      bindingsOut.append(var);
      bindingsOut.append("));\n");
    }
    return new String[] {bindingsIn.toString(), bindingsOut.toString()};
  }
  /*
  protected Throwable runWithSeparateClassLoader(final Runnable r) {
    final Throwable[] t=new Throwable[1];
    Thread thr=new Thread() {
      public void run() {
  try {
    r.run();
  } catch (Throwable tt) {
    t[0]=tt;
  }
      }
    };
    final Vector v=new Vector();
    StringTokenizer tok=classPath();
    while (tok.hasMoreTokens()) {
      String path=tok.nextToken();
      File f=new File(path);
      try {
  ZipFile zf=new ZipFile(f);
  v.addElement(zf);
      } catch (IOException e) {
  if (f.isDirectory()) v.addElement(f);
      }
    }
    final int vs=v.size();
    thr.setContextClassLoader(new ClassLoader() {
      Dictionary cache=new Hashtable();
      public byte[] loadClassData(String name) throws ClassNotFoundException {
  cname=name.replace('.', File.separatorChar) + ".class";
  for (int i=0; i < vs; i++) {
    Obj o=v.elementAt(i);
    InputStream is=null;
    if (o instanceof ZipFile) {
      ZipFile zf=(ZipFile)o;
      ZipEntry ze=zf.getEntry(cname);
      if (ze != null) {
        try {
    is=zf.getInputStream(ze);
        } catch (IOException e) {
        }
      }
    } else {
      File f=(File)o;
      try {
        is=new FileInputStream(f);
      } catch (FileNotFoundException e) {
      }
    }
    if (is != null) {
      ByteArrayOutputStream baos=new ByteArrayOutputStream(8192);
      byte[] buf=new byte[8192];
      while (true) {
        int read=is.read(buf);
        if (read == -1) break;
        baos.write(buf, 0, read);
      }
      return baos.toByteArray();
    }
  }
  throw new ClassNotFoundException("class " + name + " not found in " + System.getProperty("java.class.path"));
      }
      protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
  Class c=(Class)cache.get(name);
  if (c==null) {
    byte[] data=loadClassData(name);
    c=defineClass(data, 0, data.length);
    cache.put(name, c);
  }
  if (resolve) resolveClass(c);
  return c;
      }
    });
    thr.run();
    try {
      thr.join();
    } catch (InterruptedException e) {
      return e;
    }
    return t[0];
  }
  */
  /**
   * Command to declare a new scratch variable. <strong>Note</strong>
   * that it is not currently supported to assign an initial value at
   * this time; instead, execute a statement separately to set an
   * initial value. The value will be the normal default for that
   * type, i.e. zero or null.
   *
   * @param args Command arguments. Syntax: <tt>: int $i</tt> or <tt>:
   * int $i, String[] $j</tt>.
   * @exception java.lang.Exception In case of a problem.
   *
   * @see #varTypes
   * @see #varTypesArePrimitive
   * @see #runListVariables
   * @see #runKillVariable
   */
  public void runDeclare(String args) throws Exception {
    StringTokenizer tok=new StringTokenizer(args, " ,");
    while (tok.hasMoreTokens()) {
      String type=tok.nextToken();
      String var=tok.nextToken();
      if (var.length() < 2 || var.charAt(0) != '$')
  throw new Exception("javash declared variables must begin with `$'");
      varTypes.put(var, type);
      Class c=Void.TYPE;
      if (type.equals("boolean")) c=Boolean.TYPE;
      else if (type.equals("char")) c=Character.TYPE;
      else if (type.equals("byte")) c=Byte.TYPE;
      else if (type.equals("short")) c=Short.TYPE;
      else if (type.equals("int")) c=Integer.TYPE;
      else if (type.equals("long")) c=Long.TYPE;
      else if (type.equals("float")) c=Float.TYPE;
      else if (type.equals("double")) c=Double.TYPE;
      else if (type.equals("void"))
  throw new Exception("javash variables cannot be void");
      else try {
  c=Class.forName(type);
      } catch (ClassNotFoundException e) {
      } catch (IllegalArgumentException e) {
      }
      varValues.put(var, new Thing(c, null));
      varTypesArePrimitive.put(var,
             new Boolean(c.isPrimitive() && c != Void.TYPE));
    }
  }

  /**
   * Command to execute one or more Java statements, but not return
   * any value.
   *
   * @param args Command. Syntax: <tt>! $i=5</tt> or <tt>! $i=5;
   * doSomething()</tt>.
   * @exception java.lang.Throwable Executed code may throw anything.
   *
   * @see #runInspectJava
   * @see #runInspectJavaFully
   */
  public void runExecuteJava(String args) throws Throwable {
    String[] bindings=makeBindings();
    String code=getPreamble() +
      "public class " + gensym() + " implements com.sig.javash.Executer {\n" +
      "  public void run(java.util.Dictionary bindings) throws java.lang.Throwable {\n" +
      bindings[0] +
      "    try {\n" +
      "      " + args + ";\n" +
      "    } finally {\n" +
      bindings[1] +
      "    }\n" +
      "  }\n" +
      "}\n";
    ((Executer)evaluator.evaluate(code).newInstance()).run(varValues);
  }

  /**
   * Command to run a system (shell) command. Currently interactive
   * commands expecting input will not work. Shell constructs are
   * permitted, however. <strong>Note</strong> that this is only
   * likely to work on Unix-like systems; a Win32 variant would
   * probably not be difficult.
   *
   * @param args Text of shell command. Syntax: <tt>$ ls -l</tt> or
   * <tt>$ ls -l | head</tt>.
   * @exception java.lang.Exception In case of a problem.
   *
   * @see #shell
   */
  public void runExecuteSystem(String args) throws Exception {
    String[] argv=new String[shell.length + 1];
    for (int i=0; i < shell.length; i++)
      argv[i]=shell[i];
    argv[shell.length]=args;
    Process p=Runtime.getRuntime().exec(argv, new String[] {"JAVASH=yes"});
    class Spewer extends Thread {
      private BufferedReader whence;
      public Spewer(InputStream is) {
  whence=new BufferedReader(new InputStreamReader(is));
      }
      public void run() {
  String spewline;
  try {
    while ((spewline=whence.readLine()) != null)
      out.println(spewline);
  } catch (IOException e) {
    out.println("Error reading subprocess output: " + e);
  }
      }
    }
    Spewer a=new Spewer(p.getInputStream()); // actually stdout despite name
    Spewer b=new Spewer(p.getErrorStream());
    a.start();
    b.start();
    // Something here fails to terminate on Solaris & Linux, tho Irix
    // is fine! (After a SIGPIPE due to e.g. `find / | head'.)
    a.join();
    b.join();
    p.waitFor();
  }

  /**
   * Command to evaluate a Java expression and print the simple string
   * representation of the result.
   *
   * @param args Java expression. Syntax: <tt>=
   * 1+Integer.parseInt("2")</tt>.
   * @exception java.lang.Throwable Evaluated code may throw anything.
   *
   * @see #runExecuteJava
   * @see #runInspectJavaFully
   * @see java.lang.Object#toString
   */
  public void runInspectJava(String args) throws Throwable {
    String[] bindings=makeBindings();
    /* final */ String code=getPreamble() +
      "public class " + gensym() + " implements com.sig.javash.Querier {\n" +
      "  public com.sig.javash.Thing run(java.util.Dictionary bindings) throws java.lang.Throwable {\n" +
      bindings[0] +
      "    try {\n" +
      "      return new com.sig.javash.Thing(" + args + ");\n" +
      "    } finally {\n" +
      bindings[1] +
      "    }\n" +
      "  }\n" +
      "}\n";
/*    final PrintStream out_=out;
    final Evaluator evaluator_=evaluator;
    final Dictionary varValues_=varValues;
    final Throwable[] t=new Throwable[2];
    t[0]=runWithSeparateClassLoader(new Runnable() {
      public void run() {
      try { */
    out.println(((Querier)evaluator.evaluate(code).newInstance()).run(varValues));
/*  } catch (Throwable tt) {
    t[1]=tt;
  }
      }
    });
    if (t[1] != null) throw t[1];
    if (t[0] != null) throw t[0]; */
  }

  /**
   * Command to evaluate a Java expression and print a verbose
   * structure dump of the result, i.e. a noninteractive object
   * inspection.
   *
   * @param args Java expression. Syntax: <tt>@
   * System.getProperties()</tt>.
   * @exception java.lang.Throwable Evaluated code may throw anything.
   *
   * @see com.sig.javash.Thing#inspect
   * @see #runExecuteJava
   * @see #runInspectJava
   */
  public void runInspectJavaFully(String args) throws Throwable {
    String[] bindings=makeBindings();
    String code=getPreamble() +
      "public class " + gensym() + " implements com.sig.javash.Querier {\n" +
      "  public com.sig.javash.Thing run(java.util.Dictionary bindings) throws java.lang.Throwable {\n" +
      bindings[0] +
      "    try {\n" +
      "      return new com.sig.javash.Thing(" + args + ");\n" +
      "    } finally {\n" +
      bindings[1] +
      "    }\n" +
      "  }\n" +
      "}\n";
    ((Querier)evaluator.evaluate(code).newInstance())
      .run(varValues)
      .inspect(out, inspectDictionary);
  }

  /**
   * View a serialization file.
   * May only contain serialized objects, not primitives.
   * @param args file name
   * @exception java.lang.Throwable Sundry reasons.
   */
  public void runViewSer(String args) throws Throwable {
    ObjectInputStream ois=new ObjectInputStream(new FileInputStream(args.trim()));
    while (true) {
      try {
  new Thing(ois.readObject()).inspect(out, inspectDictionary);
      } catch (EOFException e) {
  break;
      }
    }
  }

  /**
   * Command to add a package or package glob to the imports list.
   *
   * @param args Package name or glob. Syntax: <tt>&gt;
   * java.io.Reader</tt> or <tt>&gt; java.io.*</tt>.
   * @exception java.lang.Exception Unlikely.
   *
   * @see #imports
   */
  public void runImport(String args) throws Exception {
    if (imports.contains(args))
      out.println(args + " already imported");
    else
      imports.addElement(args);
  }

  /**
   * Command to remove a package or package glob from the imports
   * list. Note that this is not very smart and will only remove
   * literally what you originally added, not mess around with the
   * meaning of package globs.
   *
   * @param args Package name or glob. Syntax: <tt>&lt;
   * java.io.Reader</tt> or <tt>&lt; java.io.*</tt>.
   * @exception java.lang.Exception Unlikely.
   *
   * @see #imports
   */
  public void runUnImport(String args) throws Exception {
    if (!imports.removeElement(args))
      out.println(args + " was not imported");
  }
  /*
  // in 1.2, System.setProperty() works
  private static void setClassPath(String cp) {
    Properties p=(Properties)System.getProperties().clone();
    p.put("java.class.path", cp);
    System.setProperties(p);
  }
  */
  /*
   * Command to add an entry to the class path (at the front).
   *
   * @param args path entry
   * @exception java.lang.Exception Unlikely.
   *
  public void runAddClassPath(String args) throws Exception {
    args=args.trim();
    if (classPathHas(args))
      out.println(args + " was already in the class path");
    else
      setClassPath(args + System.getProperty("path.separator") + System.getProperty("java.class.path"));
  }

  /*
   * Command to remove an entry from the class path (all matches).
   *
   * @param args path entry
   * @exception java.lang.Exception Unlikely.
   *
  public void runRemoveClassPath(String args) throws Exception {
    args=args.trim();
    if (classPathHas(args)) {
      StringTokenizer tok=classPath();
      StringBuffer buf=new StringBuffer();
      boolean first=true;
      while (tok.hasMoreTokens()) {
  String path=tok.nextToken();
  if (path.equals(args))
    continue;
  if (!first) buf.append(System.getProperty("path.separator"));
  first=false;
  buf.append(path);
      }
      setClassPath(buf.toString());
    } else {
      out.println(args + " was not in the class path");
    }
  }
  */
  /**
   * Command to define a Java class (in the current package) which
   * will then be accessible by name to further commands (and loaded
   * on demand).
   *
   * @param args Complete class (or interface) declaration. Syntax:
   * <tt>- public class Foo extends Whatever {methods ...}</tt>.
   * @exception java.lang.Exception Usually a compiler error.
   *
   * @see #pkg
   */
  public void runDefineJava(String args) throws Exception {
    evaluator.evaluate(getPreamble() + args + "\n");
  }

  /**
   * Command to set the current Java package (or set it to the default
   * global package if none is given).
   *
   * @param args New package, if any. Syntax: <tt>/ com.foocorp</tt>
   * or <tt>/</tt>.
   * @exception java.lang.Exception To preempt a number of bad ideas,
   * you may not set the package to be within one of the standard Java
   * or Sun dominions.
   *
   * @see #pkg
   */
  public void runSetPackage(String args) throws Exception {
    if (args.startsWith("java") || args.startsWith("sun"))
      throw new Exception("Not a chance!");
    pkg=args;
  }

  private static StringTokenizer classPath() {
    return new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
  }
/*  private static boolean classPathHas(String entry) {
    StringTokenizer tok=classPath();
    while (tok.hasMoreTokens())
      if (tok.nextToken().equals(entry))
  return true;
    return false;
    } */

  /**
   * Command to view the current package and import list, as well as
   * display a synopsis of available commands.
   *
   * @param args Ignored. Syntax: <tt>?</tt>.
   * @exception java.lang.Exception Not likely.
   */
  public void runSettings(String args) throws Exception {
    out.println("Current package: " + ((pkg==null) ? "(default)" : pkg));
    out.println("Current imports:");
    Enumeration e=imports.elements();
    while (e.hasMoreElements())
      out.println("\t" + e.nextElement());
    out.println("Current class path:");
    StringTokenizer tok=classPath();
    while (tok.hasMoreTokens())
      out.println("\t" + tok.nextToken());
    out.print(
        "Usage:\n" +
        "`:' (declare variables)          : String[] $p, int $i\n" +
        "`~' (kill variable)              ~ $x\n" +
        "`*' (list variables)             *\n" +
        "`!' (exec Java)                  ! $i+=1; $p[2]=\"hello\"\n" +
        "`=' (inspect briefly)            = $i * 3\n" +
        "`@' (inspect fully)              @ System.getProperties()\n" +
        "`-' (define)                     - public class Foo {}\n" +
        "`/' (set package)                / test\n" +
        "`>' (import)                     > java.util.*\n" +
        "`<' (unimport)                   < java.io.PrintReader\n" +
/*        "`}' (add to class path)          } /usr/local/classes\n" +
        "`{' (remove from class path)     { /usr/local/classes.zip\n" + */
        "`&' (view serial file)           & /usr/local/lib/MyBean.ser\n" +
        "`$' (exec system)                $ ls -la\n" +
        "`?' (show this help & settings)  ?\n" +
        "`#' (comment)                    # hello kitty\n" +
        "`.' (quit, also EOF)             .\n"
        );
  }

  /**
   * Command to list the currently defined scratch variables, along
   * with their declared types and current values (simple form).
   *
   * @param args Ignored. Syntax: <tt>*</tt>.
   * @exception java.lang.Exception Not likely.
   *
   * @see #runDeclare
   * @see #runKillVariable
   */
  public void runListVariables(String args) throws Exception {
    // should show individual var too, or maybe list...
    Enumeration e2=varValues.keys();
    while (e2.hasMoreElements()) {
      String var=(String)(e2.nextElement());
      String type=(String)(varTypes.get(var));
      Thing val=(Thing)(varValues.get(var));
      out.println(type + " " + var + "=" + val.getThing());
    }
  }

  /**
   * Command to undefine a scratch variable.
   *
   * @param args Scratch variable name. Syntax: <tt>~ $i</tt>.
   * @exception java.lang.Exception Not likely.
   *
   * @see #runDeclare
   * @see #runListVariables
   */
  public void runKillVariable(String args) throws Exception {
    if (varValues.get(args) != null) {
      varValues.remove(args);
      varTypes.remove(args);
      varTypesArePrimitive.remove(args);
    } else {
      out.println("Variable " + args + " did not exist anyway.");
    }
  }

  /**
   * Main interpreter loop. Reads in commands which are identified by
   * the first character, and the appropriate method is called for the
   * command. Also, <tt># blah blah</tt> leaves a comment in the
   * interpreter for your convenience; <tt>.</tt> or just sending EOF
   * (<tt>^D</tt>) exits; anything unrecognized displays a brief help
   * message. Any errors thrown are just displayed.
   */
  public void run() {
    out.println("Java Shell version " + javashVersion);
    help();
    while (true) {
      try {
  out.print("javash> ");
//  String line=in.readLine();
//  if (line==null) {
//    out.println("Exiting on EOF.");
//    return;
//  }
  StringBuffer buf=new StringBuffer();
  while (true) {
    int c=in.read();
    if (c=='\n' || c=='\r')
      break;
    else
      buf.append((char)c);
  }
  String line=buf.toString();
  if (line.length()==0)
    continue;
  char cmd='x';
  String args=null;
  try {
    cmd=line.charAt(0);
    if (line.charAt(1) != ' ') cmd='x';
    args=line.substring(2);
  } catch (StringIndexOutOfBoundsException e) {}
  switch (cmd) {
  case ':':
    runDeclare(args);
    break;
  case '!':
    runExecuteJava(args);
    break;
  case '$':
    runExecuteSystem(args);
    break;
  case '=':
    runInspectJava(args);
    break;
  case '@':
    runInspectJavaFully(args);
    break;
  case '>':
    runImport(args);
    break;
  case '<':
    runUnImport(args);
    break;
/*  case '}':
    runAddClassPath(args);
    break;
  case '{':
    runRemoveClassPath(args);
    break; */
  case '&':
    runViewSer(args);
    break;
  case '-':
    runDefineJava(args);
    break;
  case '/':
    runSetPackage(args);
    break;
  case '?':
    runSettings(args);
    break;
  case '*':
    runListVariables(args);
    break;
  case '~':
    runKillVariable(args);
    break;
  case '#':
    // comment
    break;
  case '.':
    // exit
    out.println("Goodbye.");
    return;
  default:
    help();
    break;
  }
      } catch (Throwable t) {
  t.printStackTrace(out);
      }
    }
  }
}
