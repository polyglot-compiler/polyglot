/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.*;
import polyglot.visit.*;
import coffer.types.*;
import polyglot.types.*;
import polyglot.util.*;

/**
 * An AST node for an exception throw declaration annotated with a key set.
 */
public class ThrowConstraintNode_c extends Node_c implements ThrowConstraintNode
{
    TypeNode tn;
    KeySetNode keys;
    ThrowConstraint constraint;

    public ThrowConstraintNode_c(Position pos,
                                 TypeNode tn, KeySetNode keys) {
        super(pos);
        this.tn = tn;
        this.keys = keys;
    }

    public boolean isDisambiguated() {
        return super.isDisambiguated() && constraint != null && constraint.isCanonical();
    }
    
    public TypeNode type() { return tn; }
    public KeySetNode keys() { return keys; }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        CofferTypeSystem ts = (CofferTypeSystem) tb.typeSystem();
        ThrowConstraint constraint = ts.throwConstraint(position(),
                                                        tn.type(),
                                                        keys != null ? keys.keys() : null);
        return constraint(constraint);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (constraint.isCanonical()) {
            return this;
        }
        if (! tn.type().isCanonical()) {
            return this;
        }
        if (keys != null && ! keys.keys().isCanonical()) {
            return this;
        }
        
        CofferTypeSystem ts = (CofferTypeSystem) ar.typeSystem();
        
        constraint.setThrowType(tn.type());
        constraint.setKeys(keys != null ? keys.keys() : null);

        return this;
    }

    public ThrowConstraint constraint() {
        return constraint; 
    }

    public ThrowConstraintNode constraint(ThrowConstraint constraint) {
	ThrowConstraintNode_c n = (ThrowConstraintNode_c) copy();
	n.constraint = constraint;
	return n;
    }

    public ThrowConstraintNode keys(KeySetNode keys) {
	ThrowConstraintNode_c n = (ThrowConstraintNode_c) copy();
	n.keys = keys;
	return n;
    }

    public ThrowConstraintNode type(TypeNode tn) {
	ThrowConstraintNode_c n = (ThrowConstraintNode_c) copy();
	n.tn = tn;
	return n;
    }

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

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        print(tn, w, pp);
        if (keys != null)
            print(keys, w, pp);
    }

    public void translate(CodeWriter w, Translator tr) {
        print(tn, w, tr);
    }
}
