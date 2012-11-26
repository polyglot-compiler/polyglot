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
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
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

        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();
        return n.addEnumMethodTypesIfNeeded(ts);
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
            if (!ci.flags().clear(Flags.PRIVATE).equals(Flags.NONE)) {
                throw new SemanticException("Modifier "
                                                    + ci.flags()
                                                        .clear(Flags.PRIVATE)
                                                    + " not allowed here",
                                            ci.position());
            }
        }

        ClassDecl n = (ClassDecl) super.typeCheck(tc);
        if (n.type().isMember()) {
            // it's a nested class
            n = this.flags(this.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        for (ClassMember m : this.body().members()) {
            if (m.memberInstance().flags().isAbstract()) {
                n = this.flags(this.flags().Abstract());
                n.type().flags(n.type().flags().Abstract());
                break;
            }
        }

        return n;
    }

    @Override
    protected Node addDefaultConstructorIfNeeded(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        JL5EnumDecl_c n =
                (JL5EnumDecl_c) super.addDefaultConstructorIfNeeded(ts, nf);
        // Add AST nodes
        return n.addEnumMethodsIfNeeded(ts, nf);
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
        JL5EnumDecl_c n = this;
        if (n.valueOfMethodTypeNeeded()) {
            n = n.addValueOfMethodType(ts);
        }
        if (n.valuesMethodTypeNeeded()) {
            n = n.addValuesMethodType(ts);
        }
        return n;
    }

    private Node addEnumMethodsIfNeeded(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        JL5EnumDecl_c n = this;
        if (n.valueOfMethodNeeded()) {
            n = n.addValueOfMethod(ts, nf);
        }
        if (n.valuesMethodNeeded()) {
            n = n.addValuesMethod(ts, nf);
        }
        return n;
    }

    private boolean valueOfMethodTypeNeeded() {
        for (MemberInstance mi : this.type.members()) {
            if (mi instanceof MethodInstance) {
                MethodInstance md = (MethodInstance) mi;
                if (md.name().equals("valueOf")) {
                    return false;
                }
            }
        }

        return true;

    }

    private boolean valuesMethodTypeNeeded() {
        for (MemberInstance mi : this.type.members()) {
            if (mi instanceof MethodInstance) {
                MethodInstance md = (MethodInstance) mi;
                if (md.name().equals("values")) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean valueOfMethodNeeded() {
        for (ClassMember cm : this.body.members()) {
            if (cm instanceof MethodDecl) {
                MethodDecl md = (MethodDecl) cm;
                if (md.name().equals("valueOf")) {
                    return false;
                }
            }
        }

        return true;

    }

    private boolean valuesMethodNeeded() {
        for (ClassMember cm : this.body.members()) {
            if (cm instanceof MethodDecl) {
                MethodDecl md = (MethodDecl) cm;
                if (md.name().equals("values")) {
                    return false;
                }
            }
        }
        return true;
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

    protected JL5EnumDecl_c addValueOfMethod(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add valueOf method
        Position pos = Position.compilerGenerated();
        JL5MethodInstance valueOfMI =
                (JL5MethodInstance) ts.findMethod(this.type(),
                                                  "valueOf",
                                                  Collections.singletonList((Type) ts.String()),
                                                  this.type());

        Formal formal =
                nf.Formal(pos,
                          Flags.NONE,
                          nf.CanonicalTypeNode(pos, ts.String()),
                          nf.Id(pos, "name"));

        Stmt s =
                nf.Return(pos,
                          nf.Call(pos,
                                  nf.Id(pos, "valueOf"),
                                  nf.ClassLit(pos,
                                              nf.CanonicalTypeNode(pos,
                                                                   this.type)),
                                  nf.Local(pos, nf.Id(pos, "name"))));

        Block methBody = nf.Block(pos, s);

        MethodDecl valueOfMethod =
                nf.MethodDecl(Position.compilerGenerated(),
                              flags,
                              nf.CanonicalTypeNode(position, type),
                              nf.Id(pos, "valueOf"),
                              Collections.singletonList(formal),
                              Collections.<TypeNode> emptyList(),
                              methBody);

        valueOfMethod = valueOfMethod.methodInstance(valueOfMI);
        return (JL5EnumDecl_c) this.body(this.body.addMember(valueOfMethod));
    }

    protected JL5EnumDecl_c addValuesMethodType(TypeSystem ts) {
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add values method
        JL5MethodInstance valuesMI =
                (JL5MethodInstance) ts.methodInstance(position(),
                                                      this.type(),
                                                      flags,
                                                      ts.arrayOf(this.type()),
                                                      "values",
                                                      Collections.<Type> emptyList(),
                                                      Collections.<Type> emptyList());
        this.type.addMethod(valuesMI);

        return this;
    }

    protected JL5EnumDecl_c addValuesMethod(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        Flags flags =
                Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL))
                            .set(Flags.NATIVE);

        // add values method
        Position pos = Position.compilerGenerated();
        JL5MethodInstance valuesMI =
                (JL5MethodInstance) ts.findMethod(this.type(),
                                                  "values",
                                                  Collections.<Type> emptyList(),
                                                  this.type());

        MethodDecl addValuesMethod =
                nf.MethodDecl(Position.compilerGenerated(),
                              flags,
                              nf.CanonicalTypeNode(position, type),
                              nf.Id(pos, "values"),
                              Collections.<Formal> emptyList(),
                              Collections.<TypeNode> emptyList(),
                              null);

        addValuesMethod = addValuesMethod.methodInstance(valuesMI);
        return (JL5EnumDecl_c) this.body(this.body.addMember(addValuesMethod));
    }

}
