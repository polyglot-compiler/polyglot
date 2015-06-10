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
package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassMember;
import polyglot.ast.Documentable;
import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;

/**
 * Declaration of an enum constant.
 */
public interface EnumConstantDecl extends ClassMember, Documentable {
    /** get args */
    List<Expr> args();

    /** set args */
    EnumConstantDecl args(List<Expr> args);

    /** set name */
    EnumConstantDecl name(Id name);

    /** get name */
    Id name();

    /** set body */
    EnumConstantDecl body(ClassBody body);

    /** get body */
    ClassBody body();

    ParsedClassType type();

    EnumConstantDecl type(ParsedClassType pct);

    ConstructorInstance constructorInstance();

    EnumConstantDecl constructorInstance(ConstructorInstance ci);

    EnumInstance enumInstance();

    EnumConstantDecl enumInstance(EnumInstance ei);

    Flags flags();

    long ordinal();

    EnumConstantDecl ordinal(long ordinal);
}
