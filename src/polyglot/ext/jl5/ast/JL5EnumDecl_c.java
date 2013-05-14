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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.IntLit;
import polyglot.ast.Lit;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5EnumDecl_c extends JL5ClassDecl_c implements JL5EnumDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5EnumDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body) {
        super(pos, flags, annotations, name, superClass, interfaces, body);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5EnumDecl_c n = (JL5EnumDecl_c) super.buildTypes(tb);

        if (n.type().isMember()) {
            // it's a nested class
            n = (JL5EnumDecl_c) n.flags(n.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        try {
            JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();
            return n.addEnumMethodTypesIfNeeded(ts);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        // figure out if this should be an abstract type.
        // need to do this before any anonymous subclasses are typechecked.
        for (MethodInstance mi : type().methods()) {
            if (!mi.flags().isAbstract()) continue;

            // mi is abstract! First, mark the class as abstract.
            type().setFlags(type().flags().Abstract());
        }
        return super.typeCheckEnter(tc);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (flags().isAbstract()) {
            throw new SemanticException("Enum types cannot have abstract modifier",
                                        this.position());
        }
        if (flags().isPrivate() && !type().isNested()) {
            throw new SemanticException("Top level enum types cannot have private modifier",
                                        this.position());
        }
        if (flags().isFinal()) {
            throw new SemanticException("Enum types cannot have final modifier",
                                        this.position());
        }

        for (ConstructorInstance ci : type().constructors()) {
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
        if (ts.rawClass((JL5ParsedClassType) ts.Enum())
              .equals(this.type().superType())) {
            // the super class is currently a raw Enum.
            // instantiate Enum to on this type.
            this.type()
                .superType(ts.instantiate(this.position(),
                                          (JL5ParsedClassType) ts.Enum(),
                                          Collections.singletonList(this.type())));
        }

        ClassDecl n = (ClassDecl) super.typeCheck(tc);
        if (n.type().isMember()) {
            // it's a nested class
            n = this.flags(this.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        for (ClassMember m : this.body().members()) {
            if (m.memberInstance().flags().isAbstract()
                    && m instanceof MethodDecl) {
                n.type().flags(n.type().flags().Abstract());
                break;
            }
        }

        return n;
    }

    @Override
    protected Node addDefaultConstructor(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        ConstructorInstance ci = this.defaultCI;
        if (ci == null) {
            throw new InternalCompilerError("addDefaultConstructor called without defaultCI set");
        }

        // insert call to appropriate super constructor
        List<Lit> args = new ArrayList<Lit>(2);
        args.add(nf.NullLit(Position.compilerGenerated()));// XXX the right thing to do is change the type of java.lang.Enum instead of adding these dummy params
        args.add(nf.IntLit(Position.compilerGenerated(), IntLit.INT, 0));
        Block block =
                nf.Block(position().startOf(),
                         ((JL5NodeFactory) nf).ConstructorCall(position.startOf(),
                                                               ConstructorCall.SUPER,
                                                               null,
                                                               args,
                                                               true));

        //Default constructor of an enum is private 
        ConstructorDecl cd =
                nf.ConstructorDecl(body().position().startOf(),
                                   Flags.PRIVATE,
                                   name,
                                   Collections.<Formal> emptyList(),
                                   Collections.<TypeNode> emptyList(),
                                   block);
        cd = cd.constructorInstance(ci);
        return body(body.addMember(cd));

    }

    private Node addEnumMethodTypesIfNeeded(TypeSystem ts) {
        JL5ParsedClassType ct = (JL5ParsedClassType) this.type();
        JL5EnumDecl_c n = this;
        if (ct.enumValueOfMethodNeeded()) {
            n = n.addValueOfMethodType(ts);
        }
        if (ct.enumValuesMethodNeeded()) {
            n = n.addValuesMethodType(ts);
        }
        return n;
    }

    protected JL5EnumDecl_c addValueOfMethodType(TypeSystem ts) {
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add valueOf method
        JL5MethodInstance valueOfMI =
                (JL5MethodInstance) ts.methodInstance(position(),
                                                      this.type(),
                                                      flags,
                                                      this.type(),
                                                      "valueOf",
                                                      Collections.singletonList((Type) ts.String()),
                                                      Collections.<Type> emptyList());
        this.type.addMethod(valueOfMI);

        return this;
    }

    protected JL5EnumDecl_c addValuesMethodType(TypeSystem ts) {
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add values method
        JL5MethodInstance valuesMI =
                (JL5MethodInstance) ts.methodInstance(position(),
                                                      this.type(),
                                                      flags.set(Flags.NATIVE),
                                                      ts.arrayOf(this.type()),
                                                      "values",
                                                      Collections.<Type> emptyList(),
                                                      Collections.<Type> emptyList());
        this.type.addMethod(valuesMI);

        return this;
    }

}
