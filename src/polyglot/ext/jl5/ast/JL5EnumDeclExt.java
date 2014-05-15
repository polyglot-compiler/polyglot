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
import java.util.List;

import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Formal;
import polyglot.ast.JLang;
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
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5EnumDeclExt extends JL5ClassDeclExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5EnumDeclExt() {
        this(null);
    }

    public JL5EnumDeclExt(List<AnnotationElem> annotations) {
        super(Collections.<ParamTypeNode> emptyList(), annotations);
    }

    public ClassDecl addValueOfMethodType(TypeSystem ts) {
        ClassDecl n = node();
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add valueOf method
        JL5MethodInstance valueOfMI =
                (JL5MethodInstance) ts.methodInstance(n.position(),
                                                      n.type(),
                                                      flags,
                                                      n.type(),
                                                      "valueOf",
                                                      Collections.singletonList((Type) ts.String()),
                                                      Collections.<Type> emptyList());
        n.type().addMethod(valueOfMI);

        return n;
    }

    public ClassDecl addValuesMethodType(TypeSystem ts) {
        ClassDecl n = node();
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add values method
        JL5MethodInstance valuesMI =
                (JL5MethodInstance) ts.methodInstance(n.position(),
                                                      n.type(),
                                                      flags.set(Flags.NATIVE),
                                                      ts.arrayOf(n.type()),
                                                      "values",
                                                      Collections.<Type> emptyList(),
                                                      Collections.<Type> emptyList());
        n.type().addMethod(valuesMI);

        return n;
    }

    public Node addEnumMethodTypesIfNeeded(TypeSystem ts) {
        ClassDecl n = node();
        JL5EnumDeclExt ext = (JL5EnumDeclExt) JL5Ext.ext(n);

        JL5ParsedClassType ct = (JL5ParsedClassType) n.type();
        if (ct.enumValueOfMethodNeeded()) {
            n = ext.addValueOfMethodType(ts);
        }
        if (ct.enumValuesMethodNeeded()) {
            n = ext.addValuesMethodType(ts);
        }
        return n;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        ClassDecl n = (ClassDecl) super.buildTypes(tb);
        JL5EnumDeclExt ext = (JL5EnumDeclExt) JL5Ext.ext(n);

        if (n.type().isMember()) {
            // it's a nested class
            // JLS 3rd ed. | 8.9: Nested enum types are implicitly static.
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
        ClassDecl n = node();
        // figure out if this should be an abstract type.
        // need to do this before any anonymous subclasses are typechecked.
        for (MethodInstance mi : n.type().methods()) {
            if (!mi.flags().isAbstract()) continue;

            // mi is abstract! First, mark the class as abstract.
            n.type().setFlags(n.type().flags().Abstract());
        }
        return superLang().typeCheckEnter(node(), tc);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ClassDecl n = node();
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

        // set the supertype appropriately
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
        ClassDecl n = node();
        ConstructorInstance ci = defaultCI;
        if (ci == null) {
            throw new InternalCompilerError("addDefaultConstructor called without defaultCI set");
        }

        //Default constructor of an enum is private 
        ConstructorDecl cd =
                nf.ConstructorDecl(n.body().position().startOf(),
                                   Flags.PRIVATE,
                                   n.id(),
                                   Collections.<Formal> emptyList(),
                                   Collections.<TypeNode> emptyList(),
                                   nf.Block(n.position().startOf()));
        cd = cd.constructorInstance(ci);
        return n.body(n.body().addMember(cd));

    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        ClassDecl n = node();
        ((JLang) tr.lang()).prettyPrintHeader(n, w, tr);

        boolean hasEnumConstant = false;
        for (ClassMember m : n.body().members()) {
            if (m instanceof EnumConstantDecl) {
                hasEnumConstant = true;
                break;
            }
        }

        if (!hasEnumConstant) w.write(";");
        print(n.body(), w, tr);
        ((JLang) tr.lang()).prettyPrintFooter(n, w, tr);
    }

}
