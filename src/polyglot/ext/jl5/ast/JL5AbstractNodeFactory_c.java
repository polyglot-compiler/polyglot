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

import polyglot.ast.ClassBody;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Javadoc;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.NodeFactory_c;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;

/**
 * This is a node factory that creates no nodes.
 */
public abstract class JL5AbstractNodeFactory_c extends NodeFactory_c implements
        JL5NodeFactory {
    public JL5AbstractNodeFactory_c() {
        this(J5Lang_c.instance);
    }

    public JL5AbstractNodeFactory_c(J5Lang lang) {
        super(lang);
    }

    public JL5AbstractNodeFactory_c(J5Lang lang, JL5ExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public final ConstructorCall ConstructorCall(Position pos, Kind kind,
            Expr outer, List<Expr> args, boolean isEnumConstructorCall) {
        return ConstructorCall(pos,
                               kind,
                               null,
                               outer,
                               args,
                               isEnumConstructorCall);
    }

    @Override
    public final ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, List<Expr> args) {
        return ConstructorCall(pos, kind, typeArgs, null, args);
    }

    @Override
    public final ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args) {
        return ConstructorCall(pos, kind, typeArgs, outer, args, false);
    }

    @Override
    public final polyglot.ext.jl5.ast.ElementValueArrayInit ElementValueArrayInit(
            Position pos) {
        return ElementValueArrayInit(pos, Collections.<Term> emptyList());
    }

    @Override
    public final EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, Javadoc javadoc,
            List<Expr> args) {
        return EnumConstantDecl(pos,
                                flags,
                                annotations,
                                name,
                                args,
                                null,
                                javadoc);
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public final EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args) {
        return EnumConstantDecl(pos, flags, annotations, name, args, null);
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public final FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        return FieldDecl(pos, flags, annotations, type, name, null);
    }

    @Override
    public final Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        return Formal(pos, flags, annotations, type, name, false);
    }

    @Override
    public final LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        return LocalDecl(pos, flags, annotations, type, name, null);
    }

    @Override
    public final AnnotationElem MarkerAnnotationElem(Position pos, TypeNode name) {
        return NormalAnnotationElem(pos, name, null);
    }

    @Override
    public final New New(Position pos, List<TypeNode> typeArgs, TypeNode type,
            List<Expr> args, ClassBody body) {
        return New(pos, null, typeArgs, type, args, body);
    }

    @Override
    public final ConstructorCall SuperCall(Position pos,
            List<TypeNode> typeArgs, List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.SUPER, typeArgs, null, args);
    }

    @Override
    public final ConstructorCall SuperCall(Position pos,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args) {
        return ConstructorCall(pos,
                               ConstructorCall.SUPER,
                               typeArgs,
                               outer,
                               args);
    }

    @Override
    public final ConstructorCall ThisCall(Position pos,
            List<TypeNode> typeArgs, List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.THIS, typeArgs, null, args);
    }

    @Override
    public final ConstructorCall ThisCall(Position pos,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.THIS, typeArgs, outer, args);
    }
}
