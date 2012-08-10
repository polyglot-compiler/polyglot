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

package polyglot.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ObjectDumper {
    CodeWriter w;
    int modifiersMask;

    public ObjectDumper(CodeWriter w) {
        this(w, Modifier.TRANSIENT | Modifier.STATIC);
    }

    public ObjectDumper(CodeWriter w, int modifiersMask) {
        this.w = w;
        this.modifiersMask = modifiersMask;
    }

    public void dump(Object o) {
        Set<Object> cache = new java.util.HashSet<Object>();
        w.write("(");
        dumpObject(o, cache);
        w.write(")");
        w.newline(0);
        try {
            w.flush();
        }
        catch (IOException e) {
        }
    }

    protected void dumpObject(Object obj, Set<Object> cache) {
        if (obj == null) {
            w.write("null");
            return;
        }

        w.write(StringUtil.getShortNameComponent(obj.getClass().getName()));

//        w.allowBreak(0, " ");
//        w.write(obj.toString());

        if (cache.contains(obj)) {
            return;
        }
        cache.add(obj);

        w.allowBreak(1, " ");
        w.begin(0);

        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            java.lang.reflect.AccessibleObject.setAccessible(fields, true);
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if ((field.getModifiers() & modifiersMask) != 0) continue;
                w.write("(");
                w.write(field.getName());
                w.allowBreak(1, " ");
                try {
                    Object o = field.get(obj);
                    dumpObject(o, cache);
                }
                catch (IllegalAccessException exn) {
                    w.write("##[" + exn.getMessage() + "]");
                }
                w.write(")");
                w.newline(0);
            }
        }
        catch (SecurityException exn) {
        }

        w.end();
    }
}
