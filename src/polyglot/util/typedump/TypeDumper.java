/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.util.typedump;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.TypeEncoder;

class TypeDumper {
    static Set<Class<?>> dontExpand;
    static {
        Class<?>[] primitiveLike =
                { Void.class, Boolean.class, Short.class, Integer.class,
                        Long.class, Float.class, Double.class, Class.class,
                        String.class, };
        dontExpand =
                new java.util.HashSet<Class<?>>(java.util.Arrays.asList(primitiveLike));
    }

    TypeObject theType;
    String rawName;
    String compilerVersion;
    Date timestamp;

    TypeDumper(String rawName, TypeObject t, String compilerVersion,
            Long timestamp) {
        theType = t;
        this.rawName = rawName;
        this.compilerVersion = compilerVersion;
        this.timestamp = new Date(timestamp.longValue());
    }

    public static TypeDumper load(String name, TypeSystem ts)
            throws ClassNotFoundException, NoSuchFieldException,
            java.io.IOException, SecurityException {
        Class<?> c = Class.forName(name);
        try {
            Field jlcVersion = c.getDeclaredField("jlc$CompilerVersion");
            Field jlcTimestamp = c.getDeclaredField("jlc$SourceLastModified");
            Field jlcType = c.getDeclaredField("jlc$ClassType");
            String t = (String) jlcType.get(null);
            TypeEncoder te = new TypeEncoder(ts);
            return new TypeDumper(name,
                                  te.decode(t, name),
                                  (String) jlcVersion.get(null),
                                  (Long) jlcTimestamp.get(null));
        }
        catch (IllegalAccessException exn) {
            throw new SecurityException("illegal access: " + exn.getMessage());
        }
    }

    public void dump(CodeWriter w) {
        Map<Object, Object> cache = new java.util.HashMap<Object, Object>();
        cache.put(theType, theType);
        w.write("Type " + rawName + " {");
        w.allowBreak(2);
        w.begin(0);
        w.write("Compiled with polyglot version " + compilerVersion + ".  ");
        w.allowBreak(0);
        w.write("Last modified: " + timestamp.toString() + ".  ");
        w.allowBreak(0);
        w.write(theType.toString());
        w.allowBreak(4);
        w.write("<" + theType.getClass().toString() + ">");
        w.allowBreak(0);
        dumpObject(w, theType, cache);
        w.allowBreak(0);
        w.end();
        w.allowBreak(0);
        w.write("}");
        w.newline(0);
    }

    protected void dumpObject(CodeWriter w, Object obj,
            Map<Object, Object> cache) {
        w.write(" fields {");
        w.allowBreak(2);
        w.begin(0);
        try {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            java.lang.reflect.AccessibleObject.setAccessible(declaredFields,
                                                             true);
            for (int i = 0; i < declaredFields.length; i++) {
                if (Modifier.isStatic(declaredFields[i].getModifiers()))
                    continue;
                w.begin(4);
                w.write(declaredFields[i].getName() + ": ");
                w.allowBreak(0);
                try {
                    Object o = declaredFields[i].get(obj);
                    if (o != null) {
                        Class<?> rtType = o.getClass();
                        w.write("<" + rtType.toString() + ">:");
                        w.allowBreak(0);
                        w.write(o.toString());
                        w.allowBreak(4);
                        if (!Object.class.equals(rtType) && !dontDump(rtType)
                                && !rtType.isArray()
                                && !(cache.containsKey(o) && cache.get(o) == o)) {
                            cache.put(o, o);
                            dumpObject(w, o, cache);
                        }
                    }
                    else {
                        w.write("null");
                    }
                }
                catch (IllegalAccessException exn) {
                    w.write("##[" + exn.getMessage() + "]");
                }
                w.end();
                w.allowBreak(0);
            }
        }
        catch (SecurityException exn) {
        }
        finally {
            w.end();
            w.allowBreak(0);
            w.write("}");
        }
    }

    static boolean dontDump(Class<?> c) {
        return dontExpand.contains(c);
    }

}
