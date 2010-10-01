package com.sig.javash;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * General-purpose wrapper class to store either reference or
 * primitive types (and keep track of which of those it is
 * doing!). Also includes a structured object inspector.
 *
 * <p>Recognized parameters:
 *
 * <p><table border=1>
 *
 * <tr> <th>Name</th> <th>Description</th> <th>Default</th> </tr>
 *
 * <tr> <td><tt>com.sig.javash.Thing.nestingDepth</tt></td>
 * <td>Default nesting depth for inspection.</td> <td>3</td> </tr>
 *
 * <tr> <td><tt>com.sig.javash.Thing.indent</tt></td> <td>Indentation
 * unit in the inspector.</td> <td>two spaces</td> </tr>
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
public class Thing {
  /**
   * Default nesting depth to inspect to. This is typically the number
   * of nested braces that will be displayed before an elliptical
   * notation is used.
   */
  public static final int nestingDepth=
    Integer.getInteger("com.sig.javash.Thing.nestingDepth", 3).intValue();

  /**
   * Indentation string used to visually indicate nesting depth. Will
   * be repeated zero or more times at the start of every line
   * according to depth.
   */
  public static final String indent=
    System.getProperty("com.sig.javash.Thing.indent", "  ");

  /**
   * Return the prettified name of a class. Prints out arrays nicely.
   *
   * @param c The class.
   * @return The name.
   */
  protected static String prettyClassName(Class c) {
    if (c.isArray())
      return prettyClassName(c.getComponentType()) + "[]";
    else
      return c.getName();
  }

  /**
   * The actual value being stored, either as is (for reference types)
   * or in a wrapper object (for primitive types). May also be null if
   * "void" is being stored.
   */
  private Object thing;

  /**
   * The declared class of what is being stored. This may be a
   * primitive class type. It may also be a superclass of the actual
   * type.
   */
  private Class type;

  /**
   * Store an object, as a reference type.
   *
   * @param o An object.
   */
  public Thing(Object o) {
    thing=o;
    if (o==null)
      type=Void.TYPE;
    else
      type=o.getClass();
  }

  /**
   * Store an object, possibly as a primitive.
   *
   * @param o An object, or a primitive wrapper.
   * @param unwrap True if <code>o</code> is to be unwrapped as a
   * primitive.
   */
  public Thing(Object o, boolean unwrap) {
    if (o==null) {
      thing=null;
      type=Void.TYPE;
    } else {
      thing=o;
      if (!unwrap) {
	type=o.getClass();
      } else {
	if (o instanceof Boolean)
	  type=Boolean.TYPE;
	else if (o instanceof Character)
	  type=Character.TYPE;
	else if (o instanceof Byte)
	  type=Byte.TYPE;
	else if (o instanceof Short)
	  type=Short.TYPE;
	else if (o instanceof Integer)
	  type=Integer.TYPE;
	else if (o instanceof Long)
	  type=Long.TYPE;
	else if (o instanceof Float)
	  type=Float.TYPE;
	else if (o instanceof Double)
	  type=Double.TYPE;
	else throw new IllegalArgumentException
	       (o.getClass().getName() + " is not a wrapper type!");
      }
    }
  }

  /**
   * Store a boolean.
   *
   * @param x The value.
   */
  public Thing(boolean x) {
    thing=new Boolean(x);
    type=Boolean.TYPE;
  }

  /**
   * Store a character.
   *
   * @param x The value.
   */
  public Thing(char x) {
    thing=new Character(x);
    type=Character.TYPE;
  }

  /**
   * Store a byte.
   *
   * @param x The value.
   */
  public Thing(byte x) {
    thing=new Byte(x);
    type=Byte.TYPE;
  }

  /**
   * Store a short integer.
   *
   * @param x The value.
   */
  public Thing(short x) {
    thing=new Short(x);
    type=Short.TYPE;
  }

  /**
   * Store an integer.
   *
   * @param x The value.
   */
  public Thing(int x) {
    thing=new Integer(x);
    type=Integer.TYPE;
  }

  /**
   * Store a long integer.
   *
   * @param x The value.
   */
  public Thing(long x) {
    thing=new Long(x);
    type=Long.TYPE;
  }

  /**
   * Store a single-precision float.
   *
   * @param x The value.
   */
  public Thing(float x) {
    thing=new Float(x);
    type=Float.TYPE;
  }

  /**
   * Store a double-precision float.
   *
   * @param x The value.
   */
  public Thing(double x) {
    thing=new Double(x);
    type=Double.TYPE;
  }

  /**
   * Store a null (void type).
   */
  public Thing() {
    thing=null;
    type=Void.TYPE;
  }

  /**
   * Store a default value of the indicated type.
   *
   * @param c The type (reference of primitive).
   * @param ignore Unused; merely present to ensure that the
   * <code>Thing(Object)</code> constructor is not accidentally
   * called.
   */
  public Thing(Class c, Object ignore) {
    type=c;
    if (c.isPrimitive()) {
      if (c==Boolean.TYPE)
	thing=new Boolean(false);
      else if (c==Character.TYPE)
	thing=new Character('\0');
      else if (c==Byte.TYPE)
	thing=new Byte((byte)0);
      else if (c==Short.TYPE)
	thing=new Short((short)0);
      else if (c==Integer.TYPE)
	thing=new Integer(0);
      else if (c==Long.TYPE)
	thing=new Long(0);
      else if (c==Float.TYPE)
	thing=new Float(0.0);
      else if (c==Double.TYPE)
	thing=new Double(0.0);
      else if (c==Void.TYPE)
	thing=null;
      else
	throw new IllegalArgumentException("What the hell is " + c + "?");
    } else
      thing=null;
  }

  /**
   * Get the stored object (as is, or the wrapper).
   *
   * @return The object or wrapper.
   */
  public Object getThing() {
    return thing;
  }

  /**
   * Get the declared object type.
   *
   * @return The type.
   */
  public Class getType() {
    return type;
  }

  /**
   * Is the declared type a primitive type?
   *
   * @return Whether it is primitive.
   */
  public boolean isPrimitive() {
    return type.isPrimitive();
  }

  /**
   * Give a readable string representation, including the declared
   * type.
   *
   * @return The representation.
   */
  public String toString() {
    if (type==Void.TYPE && thing==null)
      return "null";
    else
      return prettyClassName(type) + "=" + thing;
  }

  /**
   * Inspect this object to the standard output stream.
   */
  public void inspect() {
    inspect(System.out);
  }

  /**
   * Inspect this object under the assumption that none of the classes
   * encountered have been "seen" before.
   *
   * @param out The output stream to use.
   */
  public void inspect(PrintStream out) {
    inspect(out, new Hashtable());
  }

  /**
   * Inspect this object to the default nesting depth.
   *
   * @param out The output stream to use.
   * @param seen Hash from classes to Booleans indicating whether that
   * class's static data has already been displayed.
   */
  public void inspect(PrintStream out, Dictionary seen) {
    inspect(out, seen, nestingDepth);
  }

  /**
   * Inspect this object with no indentation.
   *
   * @param out The output stream to use.
   * @param seen Hash from classes to Booleans indicating whether that
   * class's static data has already been displayed.
   * @param depth The nesting depth to limit to.
   */
  public void inspect(PrintStream out, Dictionary seen, int depth) {
    inspect(out, seen, depth, "");
  }

  /**
   * Whether or not a warning about inability to make protected fields
   * accessible has already been displayed.
   */
  static protected boolean mfaAlreadyWarned=false;
  /**
   * Attempt to make a field accessible for inspection, though it may
   * be private or otherwise protected. This only works on JDK 1.2.
   *
   * @param f field to make accessible
   * @return <code>true</code> if the field was made accessible; <code>false</code> if it could not be (probably because you are running JDK 1.1)
   */
  static protected boolean makeFieldAccessible(Field f) {
    try {
      Field.class.getMethod("setAccessible", new Class[] {Boolean.TYPE}).invoke(f, new Object[] {Boolean.TRUE});
      return true;
    } catch (Throwable t) {
      if (!mfaAlreadyWarned)
	System.err.println("com.sig.javash.Thing warning: cannot inspect private fields: " + t);
      mfaAlreadyWarned=true;
      return false;
    }
  }

  /**
   * Inspect this object. The details are too complex to adequately
   * describe.
   *
   * @param out The output stream to use.
   * @param seen Hash from classes to Booleans indicating whether that
   * class's static data has already been displayed.
   * @param depth The nesting depth to limit to.
   * @param prefix The base indentation prefix to use.
   */
  public void inspect(PrintStream out, Dictionary seen, int depth, String prefix) {
    if (depth==0) {
      out.println(prefix + this);
    } else {
      if (type.isPrimitive()) {
	if (type==Void.TYPE)
	  out.println(prefix + "null");
	else
	  out.println(prefix + "(" + prettyClassName(type) + ") " + thing);
      } else if (type==String.class) {
	if (thing==null) {
	  out.println(prefix + "(String) null");
	} else {
	  out.print(prefix + "\"");
	  String s=(String)thing;
	  for (int i=0; i < s.length(); i++) {
	    char c=s.charAt(i);
	    switch (c) {
	    case '"':
	      out.print("\\\"");
	      break;
	    case '\n':
	      out.print("\\n");
	      break;
	    case '\r':
	      out.print("\\r");
	      break;
	    case '\t':
	      out.print("\\t");
	      break;
	    case '\f':
	      out.print("\\f");
	      break;
	    default:
	      if (Character.isISOControl(c)) {
		out.print("\\u");
		int ch=Character.getNumericValue(c);
		if (ch < 0) throw new IllegalArgumentException
			      ("Unicode char `" + c + "' (code " + ch + ") is weird");
		String hex=Integer.toHexString(ch);
		char[] pad=new char[4-hex.length()];
		for (int j=0; j < pad.length; j++)
		  pad[j]='0';
		out.print(pad + hex);
	      } else {
		out.print(c);
	      }
	    }
	  }
	  out.println("\"");
	}
      } else if (type.isArray()) {
	if (thing==null) {
	  out.println(prefix + "(" + prettyClassName(type) + ") null");
	} else {
	  int len=Array.getLength(thing);
	  out.println(prefix + prettyClassName(type.getComponentType())
		      + "[" + len + "] {");
	  String subprefix=prefix + indent;
	  for (int i=0; i < len; i++) {
	    out.println(subprefix + "// [" + i + "]");
	    new Thing(Array.get(thing, i), type.getComponentType().isPrimitive())
	      .inspect(out, seen, depth-1, subprefix);
	  }
	  out.println(prefix + "}");
	}
      } else {
	String subprefix=prefix + indent;
	String subsubprefix=subprefix+indent;
	Class type=this.type;
	Object thing=this.thing;
	boolean inspectingClass=false;
	if (thing != null && thing instanceof Class) {
	  type=(Class)thing;
	  thing=null;
	  inspectingClass=true;
	}
	for (Class c=type; c != null; c=c.getSuperclass()) {
	  Boolean haveSeen=(Boolean)(seen.get(c));
	  seen.put(c, Boolean.TRUE);
	  boolean fresh=(inspectingClass && c==type) ? true :
	    haveSeen==null ? true :
	    !haveSeen.booleanValue();
	  String extendsNote=(c==type ? "" : "extends ");
	  if (fresh) {
	    out.print(prefix + extendsNote +
		      Modifier.toString(c.getModifiers()) +
		      " class " + prettyClassName(c));
	    Class[] xfaces=c.getInterfaces();
	    if (xfaces.length > 0) {
	      out.print(" implements ");
	      for (int i=0; i < xfaces.length; i++) {
		if (i > 0) out.print(", ");
		out.print(prettyClassName(xfaces[i]));
	      }
	    }
	    out.println(" {");
	  } else {
	    out.println(prefix + extendsNote + "class " + prettyClassName(c) + " {");
	  }
	  if (thing==null && !inspectingClass)
	    out.println(subprefix + "// object is null");
	  Field[] fields;
	  try {
	    fields=c.getDeclaredFields();
	  } catch (SecurityException e) {
	    fields=new Field[0];
	    out.println(subprefix + "// fields not accessible: " + e);
	  }
	  for (int i=0; i < fields.length; i++) {
	    Field f=fields[i];
	    boolean isStatic=Modifier.isStatic(f.getModifiers());
	    if ((fresh || !isStatic) && (isStatic || thing != null)) {
	      // Try to print the value. If we find it, great. If not,
	      // but this is the first time through, show that the
	      // field is inaccessible; skip it thereafter. We use the
	      // Java 1.2 trick if at all possible.
	      Object value=null;
	      boolean found=false;
	      try {
		value=f.get(thing);
		found=true;
	      } catch (IllegalAccessException e) {
		try {
		  makeFieldAccessible(f);
		  value=f.get(thing);
		  found=true;
		} catch (SecurityException e3) {
		} catch (IllegalAccessException e4) {
		}
	      }
	      if (found || fresh) {
		out.print(subprefix + Modifier.toString(f.getModifiers()) + " " +
			  prettyClassName(f.getType()) + " " + f.getName());
		if (found) {
		  out.println(" = {");
		  new Thing(value, f.getType().isPrimitive())
		    .inspect(out, seen, depth-1, subsubprefix);
		  out.print(subprefix + "}");
		}
		out.println(";");
	      }
	    }
	  }
	  if (fresh) {
	    Constructor[] cons=c.getDeclaredConstructors();
	    for (int i=0; i < cons.length; i++) {
	      Constructor con=cons[i];
	      StringBuffer paramList=new StringBuffer("");
	      Class[] params=con.getParameterTypes();
	      for (int k=0; k < params.length; k++) {
		if (k > 0) paramList.append(", ");
		paramList.append(prettyClassName(params[k]));
	      }
	      StringBuffer throwsList=new StringBuffer("");
	      Class[] throws_=con.getExceptionTypes();
	      if (throws_.length > 0)
		throwsList.append(" throws ");
	      for (int j=0; j < throws_.length; j++) {
		if (j > 0) throwsList.append(", ");
		throwsList.append(prettyClassName(throws_[j]));
	      }
	      out.println(subprefix + Modifier.toString(con.getModifiers()) + " " +
			  con.getName() + "(" + paramList + ")" + throwsList + ";");
	    }
	    // Dept. of Redundancy & Repetition Dept.
	    Method[] methods=c.getDeclaredMethods();
	    for (int j=0; j < methods.length; j++) {
	      Method m=methods[j];
	      StringBuffer paramList=new StringBuffer("");
	      Class[] params=m.getParameterTypes();
	      for (int k=0; k < params.length; k++) {
		if (k > 0) paramList.append(", ");
		paramList.append(prettyClassName(params[k]));
	      }
	      StringBuffer throwsList=new StringBuffer("");
	      Class[] throws_=m.getExceptionTypes();
	      if (throws_.length > 0)
		throwsList.append(" throws ");
	      for (int l=0; l < throws_.length; l++) {
		if (l > 0) throwsList.append(", ");
		throwsList.append(prettyClassName(throws_[l]));
	      }
	      out.println(subprefix + Modifier.toString(m.getModifiers()) + " " +
			  prettyClassName(m.getReturnType()) + " " + m.getName() +
			  "(" + paramList + ")" + throwsList + ";");
	    }
	  }
	  out.println(prefix + "}");
	}
	if (!inspectingClass)
	  out.println(prefix + "// prints as \"" + thing + "\"");
      }
    }
  }
}
