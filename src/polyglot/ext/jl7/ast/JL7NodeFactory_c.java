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
package polyglot.ext.jl7.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.Expr;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.LocalDecl;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5NodeFactory_c;
import polyglot.types.Flags;
import polyglot.util.Position;

public class JL7NodeFactory_c extends JL5NodeFactory_c implements
        JL7NodeFactory {
    public JL7NodeFactory_c() {
        this(J7Lang_c.instance);
    }

    public JL7NodeFactory_c(J7Lang lang) {
        super(lang);
    }

    public JL7NodeFactory_c(J7Lang lang, JL7ExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public JL7ExtFactory extFactory() {
        return (JL7ExtFactory) super.extFactory();
    }

    @Override
    public J7Lang lang() {
        return (J7Lang) super.lang();
    }

    @Override
    public AmbDiamondTypeNode AmbDiamondTypeNode(Position pos, TypeNode base) {
        AmbDiamondTypeNode n = new AmbDiamondTypeNode(pos, base);
        n = (AmbDiamondTypeNode) n.ext(extFactory().extAmbDiamondTypeNode());
        return n;
    }

    @Override
    public TypeNode AmbUnionType(Position pos, List<TypeNode> alternatives) {
        AmbUnionType n = new AmbUnionType(pos, alternatives);
        n = (AmbUnionType) n.ext(extFactory().extAmbUnionType());
        return n;
    }

    @Override
    public MultiCatch MultiCatch(Position pos, Formal formal,
            List<TypeNode> alternatives, Block body) {
        MultiCatch n = new MultiCatch_c(pos, formal, alternatives, body);
        n = (MultiCatch) n.ext(extFactory().extMultiCatch());
        return n;
    }

    @Override
    public LocalDecl Resource(Position pos, Flags flags,
            List<Term> annotations, TypeNode type, Id name, Expr init) {
        return Resource(pos,
                        flags,
                        annotations,
                        type,
                        name,
                        init,
                        null,
                        extFactory());
    }

    protected final LocalDecl Resource(Position pos, Flags flags,
            List<Term> annotations, TypeNode type, Id name, Expr init, Ext ext,
            ExtFactory extFactory) {
        for (;; extFactory = extFactory.nextExtFactory()) {
            Ext e = JL7AbstractExtFactory_c.extResource(extFactory);
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        ext = composeExts(ext, new JL7ResourceExt());
        return super.LocalDecl(pos,
                               flags,
                               annotations,
                               type,
                               name,
                               init,
                               ext,
                               extFactory.nextExtFactory());
    }

    @Override
    public TryWithResources TryWithResources(Position pos,
            List<LocalDecl> resources, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        return TryWithResources(pos,
                                resources,
                                tryBlock,
                                catchBlocks,
                                finallyBlock,
                                null,
                                extFactory());
    }

    protected final TryWithResources TryWithResources(Position pos,
            List<LocalDecl> resources, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock, Ext ext, ExtFactory extFactory) {
        for (; extFactory != null; extFactory = extFactory.nextExtFactory()) {
            Ext e = JL7AbstractExtFactory_c.extTryWithResources(extFactory);
            if (e == null) break;
            ext = composeExts(ext, e);
        }
        return new TryWithResources_c(pos,
                                      resources,
                                      tryBlock,
                                      catchBlocks,
                                      finallyBlock,
                                      ext);
    }
}
