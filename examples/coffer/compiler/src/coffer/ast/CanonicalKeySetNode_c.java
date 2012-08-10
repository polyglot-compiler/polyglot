/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import coffer.types.KeySet;

/**
 * Implementation of a canonical key set AST node.  This is just an AST node
 * veneer around a <code>KeySet</code> type object.
 */
public class CanonicalKeySetNode_c extends Node_c implements
        CanonicalKeySetNode {
    protected KeySet keys;

    public CanonicalKeySetNode_c(Position pos, KeySet keys) {
        super(pos);
        this.keys = keys;
    }

    @Override
    public KeySet keys() {
        return keys;
    }

    @Override
    public CanonicalKeySetNode keys(KeySet keys) {
        CanonicalKeySetNode_c n = (CanonicalKeySetNode_c) copy();
        n.keys = keys;
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(keys.toString());
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        throw new InternalCompilerError(position(), "Cannot translate " + this
                + ".");
    }

    @Override
    public String toString() {
        return keys.toString();
    }
}
