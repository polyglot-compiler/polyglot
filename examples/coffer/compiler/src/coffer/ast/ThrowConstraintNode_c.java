/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.ast.TypeNode;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import coffer.types.CofferTypeSystem;
import coffer.types.ThrowConstraint;

/**
 * An AST node for an exception throw declaration annotated with a key set.
 */
public class ThrowConstraintNode_c extends Node_c implements
        ThrowConstraintNode {
    TypeNode tn;
    KeySetNode keys;
    ThrowConstraint constraint;

    public ThrowConstraintNode_c(Position pos, TypeNode tn, KeySetNode keys) {
        super(pos);
        this.tn = tn;
        this.keys = keys;
    }

    @Override
    public boolean isDisambiguated() {
        return super.isDisambiguated() && constraint != null
                && constraint.isCanonical();
    }

    @Override
    public TypeNode type() {
        return tn;
    }

    @Override
    public KeySetNode keys() {
        return keys;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        CofferTypeSystem ts = (CofferTypeSystem) tb.typeSystem();
        ThrowConstraint constraint =
                ts.throwConstraint(position(),
                                   tn.type(),
                                   keys != null ? keys.keys() : null);
        return constraint(constraint);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (constraint.isCanonical()) {
            return this;
        }
        if (!tn.type().isCanonical()) {
            return this;
        }
        if (keys != null && !keys.keys().isCanonical()) {
            return this;
        }

        constraint.setThrowType(tn.type());
        constraint.setKeys(keys != null ? keys.keys() : null);

        return this;
    }

    @Override
    public ThrowConstraint constraint() {
        return constraint;
    }

    @Override
    public ThrowConstraintNode constraint(ThrowConstraint constraint) {
        ThrowConstraintNode_c n = (ThrowConstraintNode_c) copy();
        n.constraint = constraint;
        return n;
    }

    @Override
    public ThrowConstraintNode keys(KeySetNode keys) {
        ThrowConstraintNode_c n = (ThrowConstraintNode_c) copy();
        n.keys = keys;
        return n;
    }

    @Override
    public ThrowConstraintNode type(TypeNode tn) {
        ThrowConstraintNode_c n = (ThrowConstraintNode_c) copy();
        n.tn = tn;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode tn = (TypeNode) visitChild(this.tn, v);
        KeySetNode keys = (KeySetNode) visitChild(this.keys, v);
        return reconstruct(tn, keys);
    }

    protected ThrowConstraintNode_c reconstruct(TypeNode tn, KeySetNode keys) {
        if (tn != this.tn || keys != this.keys) {
            ThrowConstraintNode_c n = (ThrowConstraintNode_c) copy();
            n.tn = tn;
            n.keys = keys;
            return n;
        }

        return this;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(tn, w, pp);
        if (keys != null) print(keys, w, pp);
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        print(tn, w, tr);
    }
}
