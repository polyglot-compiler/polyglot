/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * Copyright (c) 2006 IBM Corporation
 * 
 */

package polyglot.parse;

import polyglot.ast.*;
import polyglot.types.TypeSystem;
import polyglot.util.*;

/**
 * Represents an ambiguous, possibly qualified, identifier encountered while parsing.
 */
public class Name {
    public final Name prefix;
    public final String name;
    public final Position pos;
    public final NodeFactory nf;
    public final TypeSystem ts;

    public Name(BaseParser parser, Position pos, String name) {
    	this(parser, pos, null, name);
    }

    public Name(NodeFactory nf, TypeSystem ts, Position pos, String name) {
        this(nf, ts, pos, null, name);
    }

    public Name(BaseParser parser, Position pos, Name prefix, String name) {
    	this(parser.nf, parser.ts, pos, prefix, name);
    }
    
    public Name(NodeFactory nf, TypeSystem ts, Position pos, Name prefix, String name) {
    	this.nf = nf;
        this.ts = ts;
        this.pos = pos;
        
        if (! StringUtil.isNameShort(name)) {
            if (prefix == null) {
                this.prefix = new Name(nf, ts, pos, null, StringUtil.getPackageComponent(name));
                this.name = StringUtil.getShortNameComponent(name);
            }
            else {
                throw new InternalCompilerError("Can only construct a qualified Name with a short name string: " + name + " is not short.");
            }
        }
        else {
            this.prefix = prefix;
            this.name = name;
        }
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
            return nf.PackageNode(pos, ts.createPackage(null, name));
        }
        else {
            return nf.PackageNode(pos, ts.createPackage(prefix.toPackage().package_(), name));
        }
    }

    // type
    public TypeNode toType() {
        if (prefix == null) {
            return nf.AmbTypeNode(pos, name);
        }

        return nf.AmbTypeNode(pos, prefix.toQualifier(), name);
    }

    public String toString() {
        if (prefix == null) {
            return name;
        }

        return prefix.toString() + "." + name;
    }
}
