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

package polyglot.ast;

import polyglot.types.Qualifier;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * An <code>AmbQualifierNode</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a type qualifier.
 */
public class AmbQualifierNode_c extends Node_c implements AmbQualifierNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Qualifier qualifier;
    protected QualifierNode qual;
    protected Id name;

    public AmbQualifierNode_c(Position pos, QualifierNode qual, Id name) {
        super(pos);
        assert (name != null); // qual may be null

        this.qual = qual;
        this.name = name;
    }

    @Override
    public Qualifier qualifier() {
        return this.qualifier;
    }

    @Override
    public Id id() {
        return this.name;
    }

    public AmbQualifierNode id(Id name) {
        AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return this.name.id();
    }

    public AmbQualifierNode name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public QualifierNode qual() {
        return this.qual;
    }

    public AmbQualifierNode qual(QualifierNode qual) {
        AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
        n.qual = qual;
        return n;
    }

    public AmbQualifierNode qualifier(Qualifier qualifier) {
        AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
        n.qualifier = qualifier;
        return n;
    }

    protected AmbQualifierNode_c reconstruct(QualifierNode qual, Id name) {
        if (qual != this.qual || name != this.name) {
            AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
            n.qual = qual;
            n.name = name;
            return n;
        }

        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = visitChild(this.name, v);
        QualifierNode qual = visitChild(this.qual, v);
        return reconstruct(qual, name);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return qualifier(tb.typeSystem().unknownQualifier(position()));
    }

    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        if (qual != null && !qual.isDisambiguated()) {
            return this;
        }

        Node n =
                sc.nodeFactory()
                  .disamb()
                  .disambiguate(this, sc, position(), qual, name);

        if (n instanceof QualifierNode) {
            return n;
        }

        throw new SemanticException("Could not find type or package \""
                + (qual == null ? name.toString() : qual.toString() + "."
                        + name.toString()) + "\".", position());
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Didn't finish disambiguation; just return.
        return this;
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot exception check ambiguous node "
                                                + this + ".");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (qual != null) {
            print(qual, w, tr);
            w.write(".");
            w.allowBreak(2, 3, "", 0);
        }

        tr.print(this, name, w);
    }

    @Override
    public String toString() {
        return (qual == null ? name.toString() : qual.toString() + "."
                + name.toString())
                + "{amb}";
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.AmbQualifierNode(this.position, this.qual, this.name);
    }
}
