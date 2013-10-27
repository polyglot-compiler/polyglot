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

import java.util.Collections;

import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDecl_c;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Node_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5EnumDeclDel extends JL5ClassDeclDel {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        ClassDecl n = (ClassDecl) super.buildTypes(tb);
        JL5EnumDeclExt ext = (JL5EnumDeclExt) JL5Ext.ext(n);

        if (n.type().isMember()) {
            // it's a nested class
            n = n.flags(n.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        try {
            JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();
            return ext.addEnumMethodTypesIfNeeded(ts);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        ClassDecl n = (ClassDecl) this.node();
        // figure out if this should be an abstract type.
        // need to do this before any anonymous subclasses are typechecked.
        for (MethodInstance mi : n.type().methods()) {
            if (!mi.flags().isAbstract()) continue;

            // mi is abstract! First, mark the class as abstract.
            n.type().setFlags(n.type().flags().Abstract());
        }
        return super.typeCheckEnter(tc);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ClassDecl n = (ClassDecl) this.node();
        if (n.flags().isAbstract()) {
            throw new SemanticException("Enum types cannot have abstract modifier",
                                        n.position());
        }
        if (n.flags().isPrivate() && !n.type().isNested()) {
            throw new SemanticException("Top level enum types cannot have private modifier",
                                        n.position());
        }
        if (n.flags().isFinal()) {
            throw new SemanticException("Enum types cannot have final modifier",
                                        n.position());
        }

        for (ConstructorInstance ci : n.type().constructors()) {
            if (!JL5Flags.clearVarArgs(ci.flags().clear(Flags.PRIVATE))
                         .equals(Flags.NONE)) {
                throw new SemanticException("Modifier "
                                                    + ci.flags()
                                                        .clear(Flags.PRIVATE)
                                                    + " not allowed here",
                                            ci.position());
            }
        }

        // set the supertype appropraitely
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        if (ts.rawClass((JL5ParsedClassType) ts.Enum()).equals(n.type()
                                                                .superType())) {
            // the super class is currently a raw Enum.
            // instantiate Enum to on this type.
            n.type()
             .superType(ts.instantiate(n.position(),
                                       (JL5ParsedClassType) ts.Enum(),
                                       Collections.singletonList(n.type())));
        }

        n = (ClassDecl) super.typeCheck(tc);
        if (n.type().isMember()) {
            // it's a nested class
            n = n.flags(n.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        for (ClassMember m : n.body().members()) {
            if (m.memberInstance().flags().isAbstract()
                    && m instanceof MethodDecl) {
                n.type().flags(n.type().flags().Abstract());
                break;
            }
        }

        return n;
    }

    @Override
    public Node addDefaultConstructor(TypeSystem ts, NodeFactory nf,
            ConstructorInstance defaultCI) throws SemanticException {
        ClassDecl n = (ClassDecl) this.node();
        ConstructorInstance ci = defaultCI;
        if (ci == null) {
            throw new InternalCompilerError("addDefaultConstructor called without defaultCI set");
        }

        //Default constructor of an enum is private 
        ConstructorDecl cd =
                nf.ConstructorDecl(n.body().position().startOf(),
                                   Flags.PRIVATE,
                                   n.name(),
                                   Collections.<Formal> emptyList(),
                                   Collections.<TypeNode> emptyList(),
                                   nf.Block(n.position().startOf()));
        cd = cd.constructorInstance(ci);
        return n.body(n.body().addMember(cd));

    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        ClassDecl n = (ClassDecl) this.node();
        prettyPrintHeader(w, tr);

        boolean hasEnumConstant = false;
        for (ClassMember m : n.body().members()) {
            if (m instanceof EnumConstantDecl) {
                hasEnumConstant = true;
                break;
            }
        }

        if (!hasEnumConstant) w.write(";");
        ((Node_c) n).print(n.body(), w, tr);
        ((ClassDecl_c) n).prettyPrintFooter(w, tr);
    }

}
