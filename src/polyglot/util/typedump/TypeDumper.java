/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.util.typedump;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Version;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyglot.util.CodeWriter;
import polyglot.util.Position;

public class TypeDumper {
    static Set<Class<?>> dontExpand;
    static {
        Class<?>[] primitiveLike =
                { Void.class, Boolean.class, Short.class, Integer.class,
                        Long.class, Float.class, Double.class, Class.class,
                        String.class, Character.class };
        dontExpand =
                new java.util.HashSet<>(java.util.Arrays.asList(primitiveLike));

        dontExpand.add(Position.class);
        dontExpand.add(Compiler.class);
    }

    TypeObject theType;
    String rawName;
    String compilerVersion;
    Date timestamp;

    public TypeDumper(String rawName, TypeObject t, String compilerVersion,
            Long timestamp) {
        theType = t;
        this.rawName = rawName;
        this.compilerVersion = compilerVersion;
        if (timestamp != null) {
            this.timestamp = new Date(timestamp.longValue());
        }
        else {
            this.timestamp = null;
        }
        initializeType(theType);
    }

    private static void initializeType(TypeObject t) {
        if (t instanceof ClassType) {
            ClassType ct = (ClassType) t;
            ct.methods();
            ct.fields();
            ct.interfaces();
            ct.superType();
            ct.container();
            if (ct instanceof JL5ClassType) {
                JL5ClassType jct = (JL5ClassType) ct;
                jct.annotationElems();
                jct.annotations();
                jct.enumConstants();
            }
        }
    }

    public static TypeDumper load(String name, TypeSystem ts, Version ver)
            throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, SemanticException {
        Class<?> c = Class.forName(name);
        try {
            String suffix = ver.name();
            Field jlcVersion =
                    c.getDeclaredField("jlc$CompilerVersion$" + suffix);
            Field jlcTimestamp =
                    c.getDeclaredField("jlc$SourceLastModified$" + suffix);
            Field jlcType = c.getDeclaredField("jlc$ClassType$" + suffix);
            jlcVersion.setAccessible(true);
            jlcTimestamp.setAccessible(true);
            jlcType.setAccessible(true);
            return new TypeDumper(name,
                                  ts.typeForName(name),
                                  (String) jlcVersion.get(null),
                                  (Long) jlcTimestamp.get(null));
        }
        catch (IllegalAccessException exn) {
            exn.printStackTrace();
            throw new SecurityException("illegal access: " + exn.getMessage());
        }
    }

    public void dump(CodeWriter w) {
        TypeCache cache = new TypeCache();
        cache.put(theType);
        w.write("Type " + rawName + " {");
        w.allowBreak(2);
        w.begin(0);
        w.write("Compiled with polyglot version " + compilerVersion + ".  ");
        w.allowBreak(0);
        if (timestamp != null) {
            w.write("Last modified: " + timestamp.toString() + ".  ");
            w.allowBreak(0);
        }
        w.write(theType.toString());
        w.allowBreak(2);
        w.write("<" + theType.getClass().toString() + ">");
        w.allowBreak(0);
        dumpObjectFields(w, theType, cache);
        w.allowBreak(0);
        w.end();
        w.allowBreak(0);
        w.write("}");
        w.newline(0);
    }

    protected void dumpObject(CodeWriter w, Object o, TypeCache cache,
            Field declaredField) {
        if (o instanceof TypeObject) {
            initializeType((TypeObject) o);
        }
        w.begin(0);
        Class<?> rtType = o.getClass();
        w.write("<" + rtType.toString() + ">:");
        w.allowBreak(0);
        w.write(o.toString());
        if (!Object.class.equals(rtType)
                && !(o instanceof TypeSystem)
                && !(o instanceof ExtensionInfo)
                && !(o instanceof ClassFile)
                && !dontDump(rtType)
                && !(dontDump(rtType.getName(), (declaredField == null
                        ? null : declaredField.getName())))
                && !rtType.isArray()
                && !(cache.containsKey(o) && cache.get(o) == o)) {
            w.allowBreak(2);
            cache.put(o);
            if (o instanceof List || o instanceof Set) {
                @SuppressWarnings("unchecked")
                Collection<Object> list = (Collection<Object>) o;
                for (Object elem : list) {
                    dumpObject(w, elem, cache, null);
                    w.newline();
                }
            }
            else if (o instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> map = (Map<Object, Object>) o;
                for (Object key : map.keySet()) {
                    dumpObject(w, key, cache, null);
                    w.allowBreak(0);
                    w.write(" -> ");
                    w.allowBreak(0);
                    dumpObject(w, map.get(key), cache, null);
                }
            }
            else {
                dumpObjectFields(w, o, cache);
            }
        }
        w.end();
    }

    protected void dumpObjectFields(CodeWriter w, Object obj, TypeCache cache) {
        w.write(" fields {");
        w.newline();
        w.begin(4);
        w.write("    ");
        try {
            List<Field> allFields = new ArrayList<>();
            Class<?> objClass = obj.getClass();
            while (objClass != null) {
                allFields.addAll(Arrays.asList(objClass.getDeclaredFields()));
                java.lang.reflect.AccessibleObject.setAccessible(objClass.getDeclaredFields(),
                                                                 true);
                objClass = objClass.getSuperclass();
            }
            java.lang.reflect.AccessibleObject.setAccessible(allFields.toArray(new AccessibleObject[0]),
                                                             true);
            for (Field declaredField : allFields) {
                if (Modifier.isStatic(declaredField.getModifiers())) continue;
                w.begin(2);
                w.write(declaredField.getName() + ": ");
                try {
                    Object o = declaredField.get(obj);
                    if (o != null) {
                        dumpObject(w, o, cache, declaredField);
                    }
                    else {
                        w.write("null");
                    }
                }
                catch (IllegalAccessException exn) {
                    w.write("##[" + exn.getMessage() + "]");
                }
                w.end();
                w.newline();
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

    private static boolean dontDump(String className, String fieldName) {
        if ("classFileSource".equals(fieldName)) {
            return true;
        }
        return false;
    }

    static boolean dontDump(Class<?> c) {
        return dontExpand.contains(c);
    }

    public static class TypeCache {
        private final Map<TypeSystem, Map<Object, Object>> c = new HashMap<>();

        public void put(Object o) {
            TypeSystem ts = typeSystemFor(o);
            Map<Object, Object> m = c.get(ts);
            if (m == null) {
                m = new HashMap<>();
                c.put(ts, m);
            }
            m.put(o, o);
        }

        private static TypeSystem typeSystemFor(Object o) {
            if (o instanceof TypeObject) {
                TypeObject to = (TypeObject) o;
                return to.typeSystem();
            }
            return null;
        }

        public Object get(Object o) {
            TypeSystem ts = typeSystemFor(o);
            Map<Object, Object> m = c.get(ts);
            if (m != null) {
                return m.get(o);
            }
            return null;
        }

        public boolean containsKey(Object o) {
            TypeSystem ts = typeSystemFor(o);
            Map<Object, Object> m = c.get(ts);
            if (m != null) {
                return m.containsKey(o);
            }
            return false;
        }

    }

}
