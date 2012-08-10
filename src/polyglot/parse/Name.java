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

package polyglot.parse;

import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.NodeFactory;
import polyglot.ast.PackageNode;
import polyglot.ast.Prefix;
import polyglot.ast.QualifierNode;
import polyglot.ast.Receiver;
import polyglot.ast.TypeNode;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * Represents an ambiguous, possibly qualified, identifier encountered while parsing.
 */
public class Name {
    public final Name prefix;
    public final Id name;
    public final Position pos;
    public final NodeFactory nf;
    public final TypeSystem ts;

    public Name(NodeFactory nf, TypeSystem ts, Position pos, Id name) {
        this(nf, ts, pos, null, name);
    }

    public Name(NodeFactory nf, TypeSystem ts, Position pos, Name prefix,
            Id name) {
        this.nf = nf;
        this.ts = ts;
        this.pos = pos != null ? pos : Position.compilerGenerated();
        this.prefix = prefix;
        this.name = name;
    }

    // expr
    public Expr toExpr() {
        if (prefix == null) {
            return nf.AmbExpr(pos, name);
        }

        return nf.Field(pos, prefix.toReceiver(), name);
    }

    // expr or type
    public Receiver toReceiver() {
        if (prefix == null) {
            return nf.AmbReceiver(pos, name);
        }

        return nf.AmbReceiver(pos, prefix.toPrefix(), name);
    }

    // expr, type, or package
    public Prefix toPrefix() {
        if (prefix == null) {
            return nf.AmbPrefix(pos, name);
        }

        return nf.AmbPrefix(pos, prefix.toPrefix(), name);
    }

    // type or package
    public QualifierNode toQualifier() {
        if (prefix == null) {
            return nf.AmbQualifierNode(pos, name);
        }

        return nf.AmbQualifierNode(pos, prefix.toQualifier(), name);
    }

    // package
    public PackageNode toPackage() {
        if (prefix == null) {
            return nf.PackageNode(pos, ts.createPackage(null, name.id()));
        }
        else {
            return nf.PackageNode(pos, ts.createPackage(prefix.toPackage()
                                                              .package_(),
                                                        name.id()));
        }
    }

    // type
    public TypeNode toType() {
        if (prefix == null) {
            return nf.AmbTypeNode(pos, name);
        }

        return nf.AmbTypeNode(pos, prefix.toQualifier(), name);
    }

    @Override
    public String toString() {
        if (prefix == null) {
            return name.toString();
        }

        return prefix.toString() + "." + name.toString();
    }
}
