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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import polyglot.main.Report;
import polyglot.types.Named;
import polyglot.types.NamedPlaceHolder;
import polyglot.types.PlaceHolder;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;

/** Input stream for reading type objects. */
public class TypeInputStream extends ObjectInputStream {
    protected TypeSystem ts;
    protected Map<Object, Object> cache;
    protected boolean failed;
    protected boolean enableReplace;
    protected Set<Object> placeHoldersUsed;

    public TypeInputStream(InputStream in, TypeSystem ts,
            Map<Object, Object> cache) throws IOException {
        super(in);

        enableResolveObject(true);

        this.ts = ts;
        this.cache = cache;
        this.failed = false;
        this.enableReplace = true;
        this.placeHoldersUsed = new HashSet<Object>();
    }

    public Set<Object> placeHoldersUsed() {
        return placeHoldersUsed;
    }

    public boolean deserializationFailed() {
        return failed;
    }

    public TypeSystem getTypeSystem() {
        return ts;
    }

    private final static Object UNRESOLVED = new Object();

    public void installInPlaceHolderCache(PlaceHolder p, TypeObject t) {
        cache.put(p, t);

        if (t instanceof Named && p instanceof NamedPlaceHolder) {
            NamedPlaceHolder pp = (NamedPlaceHolder) p;
            if (Report.should_report(Report.serialize, 2))
                Report.report(2, "Forcing " + pp.name()
                        + " into system resolver");
            ts.systemResolver().install(pp.name(), (Named) t);
        }

        String s = "";
        if (Report.should_report(Report.serialize, 2)) {
            try {
                s = t.toString();
            }
            catch (NullPointerException e) {
                s = "<NullPointerException thrown>";
            }
        }

        if (Report.should_report(Report.serialize, 2)) {
            Report.report(2, "- Installing " + p + " -> " + s
                    + " in place holder cache");
        }
    }

    public void enableReplace(boolean f) {
        this.enableReplace = f;
    }

    @Override
    protected Object resolveObject(Object o) {
        if (!enableReplace) {
            return o;
        }
        String s = "";
        if (Report.should_report(Report.serialize, 2)) {
            try {
                s = o.toString();
            }
            catch (NullPointerException e) {
                s = "<NullPointerException thrown>";
            }
        }

        if (!enableReplace) {
            return o;
        }

        if (o instanceof PlaceHolder) {
            if (failed) {
                return null;
            }

            placeHoldersUsed.add(o);

            Object t = cache.get(o);
            if (t == UNRESOLVED) {
                // A place holder lower in the call stack is trying to resolve
                // this place holder too.  Abort!
                // The calling place holder should set up depedencies to ensure
                // this pass is rerun.
                failed = true;
                return null;
            }
            else if (t == null) {
                try {
                    cache.put(o, UNRESOLVED);
                    t = ((PlaceHolder) o).resolve(ts);
                    if (t == null) {
                        throw new InternalCompilerError("Resolved " + s
                                + " to null.");
                    }
                    cache.put(o, t);
                    if (Report.should_report(Report.serialize, 2)) {
                        Report.report(2,
                                      "- Resolving " + s + " : " + o.getClass()
                                              + " to " + t + " : "
                                              + t.getClass());
                    }
                }
                catch (CannotResolvePlaceHolderException e) {
                    failed = true;
                    if (Report.should_report(Report.serialize, 2)) {
                        Report.report(2,
                                      "- Resolving " + s + " : " + o.getClass()
                                              + " to " + e);
                    }
                }
            }
            else {
                if (Report.should_report(Report.serialize, 2)) {
                    Report.report(2, "- Resolving " + s + " : " + o.getClass()
                            + " to (cached) " + t + " : " + t.getClass());
                }
            }
            return t;
        }
        else if (o instanceof Internable) {
            if (Report.should_report(Report.serialize, 2)) {
                Report.report(2, "- Interning " + s + " : " + o.getClass());
            }
            return ((Internable) o).intern();
        }
        else {
            if (Report.should_report(Report.serialize, 2)) {
                Report.report(2, "- " + s + " : " + o.getClass());
            }

            return o;
        }
    }
}
