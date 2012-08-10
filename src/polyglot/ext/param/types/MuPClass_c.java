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

package polyglot.ext.param.types;

import java.util.LinkedList;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * A base implementation for mutable parametric classes.
 */
public class MuPClass_c<Formal extends Param, Actual extends TypeObject>
        extends PClass_c<Formal, Actual> implements MuPClass<Formal, Actual> {
    protected List<Formal> formals;
    protected ClassType clazz;

    protected MuPClass_c() {
    }

    public MuPClass_c(TypeSystem ts) {
        this(ts, null);
    }

    public MuPClass_c(TypeSystem ts, Position pos) {
        super(ts, pos);
        formals = new LinkedList<Formal>();
    }

    /////////////////////////////////////////////////////////////////////////
    // Implement PClass

    @Override
    public List<Formal> formals() {
        return formals;
    }

    @Override
    public ClassType clazz() {
        return clazz;
    }

    /////////////////////////////////////////////////////////////////////////
    // Implement MuPClass

    @Override
    public void formals(List<Formal> formals) {
        this.formals = formals;
    }

    @Override
    public void addFormal(Formal param) {
        formals().add(param);
    }

    @Override
    public void clazz(ClassType clazz) {
        this.clazz = clazz;
    }
}
