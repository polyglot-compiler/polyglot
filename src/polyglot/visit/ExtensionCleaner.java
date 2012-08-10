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

package polyglot.visit;

import polyglot.ast.Call;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.ClassDecl;
import polyglot.ast.ConstructorCall;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Initializer;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.PackageNode;
import polyglot.ast.TypeNode;
import polyglot.frontend.ExtensionInfo;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;

/**
 * This visitor overwrites all extension object refs with null,
 * sets delegate object refs to point back to the node,
 * and strips type information out.
 **/
public class ExtensionCleaner extends NodeVisitor {
    protected NodeFactory nf;
    protected TypeSystem ts;
    protected ExtensionInfo javaExt;

    public ExtensionCleaner(ExtensionInfo javaExt) {
        this.javaExt = javaExt;
        this.nf = javaExt.nodeFactory();
        this.ts = javaExt.typeSystem();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        n = n.ext(null);
        n = n.del(null);
        n = strip(n);
        return n;
    }

    protected Node strip(Node n) {
        // FIXME: should use method dispatch for this.
        if (n instanceof Call) {
            n = ((Call) n).methodInstance(null);
        }

        if (n instanceof ClassDecl) {
            n = ((ClassDecl) n).type(null);
        }

        if (n instanceof ConstructorCall) {
            n = ((ConstructorCall) n).constructorInstance(null);
        }

        if (n instanceof Field) {
            n = ((Field) n).fieldInstance(null);
        }

        if (n instanceof FieldDecl) {
            n = ((FieldDecl) n).fieldInstance(null);
            n = ((FieldDecl) n).initializerInstance(null);
        }

        if (n instanceof Formal) {
            n = ((Formal) n).localInstance(null);
        }

        if (n instanceof Initializer) {
            n = ((Initializer) n).initializerInstance(null);
        }

        if (n instanceof Local) {
            n = ((Local) n).localInstance(null);
        }

        if (n instanceof LocalDecl) {
            n = ((LocalDecl) n).localInstance(null);
        }

        if (n instanceof MethodDecl) {
            n = ((MethodDecl) n).methodInstance(null);
        }

        if (n instanceof New) {
            n = ((New) n).anonType(null);
            n = ((New) n).constructorInstance(null);
        }

        if (n instanceof TypeNode) {
            n = convert((TypeNode) n);
        }

        if (n instanceof PackageNode) {
            n = convert((PackageNode) n);
        }

        if (n instanceof Expr) {
            n = ((Expr) n).type(null);
        }

        return n;
    }

    protected TypeNode convert(TypeNode n) {
        Type t = n.type();

        if (n instanceof CanonicalTypeNode) {
            if (t.typeSystem() == ts) {
                return n;
            }
            else {
                throw new InternalCompilerError("Unexpected Jx type: " + t
                        + " found in rewritten AST.");
            }
        }

        // Must be an AmbTypeNode

        if (t != null && t.isCanonical()) {
            if (t.typeSystem() == ts) {
                return nf.CanonicalTypeNode(n.position(), t);
            }
        }

        n = n.type(null);

        return n;
    }

    protected PackageNode convert(PackageNode n) {
        Package p = n.package_();

        if (p != null && p.isCanonical()) {
            if (p.typeSystem() == ts) {
                return nf.PackageNode(n.position(), p);
            }
        }

        return nf.PackageNode(n.position(), ts.createPackage(n.toString()));
    }
}
