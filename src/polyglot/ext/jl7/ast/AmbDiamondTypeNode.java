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

import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class AmbDiamondTypeNode extends TypeNode_c implements Ambiguous {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode base;

    public AmbDiamondTypeNode(Position pos, TypeNode base) {
        super(pos);
        this.base = base;
    }

    @Override
    public String name() {
        return base.name();
    }

    public TypeNode base() {
        return base;
    }

    protected AmbDiamondTypeNode reconstruct(TypeNode base) {
        if (this.base != base) {
            AmbDiamondTypeNode n = (AmbDiamondTypeNode) this.copy();
            n.base = base;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode base = visitChild(this.base, v);
        return reconstruct(base);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (!base.isDisambiguated()) return this;
        JL7TypeSystem ts = (JL7TypeSystem) ar.typeSystem();
        Type base = this.base.type();
        if (base instanceof RawClass) {
            JL5ParsedClassType ct = ((RawClass) base).base();
            Type t = ts.diamondType(position(), ct);
            return ar.nodeFactory().CanonicalTypeNode(this.position, t);
        }
        throw new SemanticException("The type "
                + base
                + " is not generic; it cannot be parameterized with arguments <>");
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(base);
        sb.append("<>");
        return sb.toString();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        tr.lang().prettyPrint(base, w, tr);
        w.write("<>");
    }
}
