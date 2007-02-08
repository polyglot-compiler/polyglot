/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2007 IBM Corporation
 */


package polyglot.util;

import polyglot.util.*;
import polyglot.visit.NodeVisitor;
import polyglot.ast.Node;
import polyglot.types.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Map;
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
	Set cache = new java.util.HashSet();
	w.write("(");
        dumpObject(o, cache);
	w.write(")");
	w.newline(0);
        try {
            w.flush();
        }
        catch (IOException e) {}
    }
  
    protected void dumpObject(Object obj, Set cache) {
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
                if ((field.getModifiers() & modifiersMask) != 0)
                    continue;
		w.write("(");
                w.write(field.getName());
                w.allowBreak(1, " ");
		try {
		    Object o = field.get(obj);
		    dumpObject(o, cache);
		}
                catch (IllegalAccessException exn) {
		    w.write("##["+exn.getMessage()+"]");
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

