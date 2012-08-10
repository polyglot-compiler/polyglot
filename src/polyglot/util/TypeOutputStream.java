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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import polyglot.main.Report;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;

/** Output stream for writing type objects. */
public class TypeOutputStream extends ObjectOutputStream {
    protected TypeSystem ts;
    protected Set<? extends TypeObject> roots;
    protected Map<IdentityKey, Object> placeHolders;

    public TypeOutputStream(OutputStream out, TypeSystem ts, TypeObject root)
            throws IOException {
        super(out);

        this.ts = ts;
        this.roots = ts.getTypeEncoderRootSet(root);
        this.placeHolders = new HashMap<IdentityKey, Object>();

        if (Report.should_report(Report.serialize, 2)) {
            Report.report(2, "Began TypeOutputStream with roots: " + roots);
        }

        enableReplaceObject(true);
    }

    protected Object placeHolder(TypeObject o, boolean useRoots) {
        IdentityKey k = new IdentityKey(o);
        Object p = placeHolders.get(k);
        if (p == null) {
            p =
                    ts.placeHolder(o,
                                   useRoots ? roots
                                           : Collections.<TypeObject> emptySet());
            placeHolders.put(k, p);
        }
        return p;
    }

    @Override
    protected Object replaceObject(Object o) throws IOException {
        if (o instanceof TypeObject) {
            Object r;

            if (roots.contains(o)) {
                if (Report.should_report(Report.serialize, 2)) {
                    Report.report(2, "+ In roots: " + o + " : " + o.getClass());
                }

                r = o;
            }
            else {
                r = placeHolder((TypeObject) o, true);
            }

            if (Report.should_report(Report.serialize, 2)) {
                if (r != o) {
                    Report.report(2, "+ Replacing: " + o + " : " + o.getClass()
                            + " with " + r);
                }
                else {
                    Report.report(2, "+ " + o + " : " + o.getClass());
                }
            }

            return r;
        }
        else {
            if (Report.should_report(Report.serialize, 2)) {
                Report.report(2, "+ " + o + " : " + o.getClass());
            }
            return o;
        }
    }
}
