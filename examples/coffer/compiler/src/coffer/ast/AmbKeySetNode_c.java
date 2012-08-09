/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import coffer.types.CofferTypeSystem;
import coffer.types.KeySet;

/**
 * Implementation of an ambiguous key set AST node.
 */
public class AmbKeySetNode_c extends Node_c implements AmbKeySetNode
{
    protected List<KeyNode> keys;
    protected KeySet keySet;

    public AmbKeySetNode_c(Position pos, List<KeyNode> keys) {
        super(pos);
        this.keys = ListUtil.copy(keys, true);
    }

    @Override
    public KeySet keys() {
        return keySet;
    }

    @Override
    public List<KeyNode> keyNodes() {
        return keys;
    }

    @Override
    public AmbKeySetNode keyNodes(List keys) {
        AmbKeySetNode_c n = (AmbKeySetNode_c) copy();
        n.keys = ListUtil.copy(keys, true);
        return n;
    }

    public AmbKeySetNode_c reconstruct(List keys) {
        if (! CollectionUtil.equals(this.keys, keys)) {
            AmbKeySetNode_c n = (AmbKeySetNode_c) copy();
            n.keys = ListUtil.copy(keys, true);
            return n;
        }

        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<KeyNode> keys = visitList(this.keys, v);
        return reconstruct(keys);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        CofferTypeSystem ts = (CofferTypeSystem) tb.typeSystem();

        KeySet s = ts.emptyKeySet(position());
        
        for (KeyNode i : keys) {
            s = s.add(i.key());
        }

        AmbKeySetNode_c n = (AmbKeySetNode_c) copy();
        n.keys = keys;
        n.keySet = s;
        return n;
    }
        
    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        CofferTypeSystem ts = (CofferTypeSystem) sc.typeSystem();
        CofferNodeFactory nf = (CofferNodeFactory) sc.nodeFactory();

        KeySet s = ts.emptyKeySet(position());

        for (KeyNode key : keys) {

            if (! key.key().isCanonical()) {
                return this;
            }
            s = s.add(key.key());
        }

        return nf.CanonicalKeySetNode(position(), s);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("[");

        w.begin(0);
        
        boolean first = true;
        for (KeyNode key : keys) {
            if (!first) {
                w.write(",");
                w.allowBreak(0, " ");
            }
            first = false;
            print(key, w, tr);
        }

        w.end();
        w.write("]");
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
	throw new InternalCompilerError(position(),
	    "Cannot translate ambiguous key set " + this + ".");
    }

    @Override
    public String toString() {
        String s = "[";

        for (Iterator<KeyNode> i = keys.iterator(); i.hasNext();) {
            KeyNode key = i.next();

            s += key.toString();

            if (i.hasNext()) {
                s += ", ";
            }
        }

        s += "]";
        
        return s;
    }
}
