/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import coffer.types.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * An AST node for a <code>Key</code>.  The key may be ambiguous. 
 */
public class KeyNode_c extends Node_c implements KeyNode
{
    protected Key key;

    public KeyNode_c(Position pos, Key key) {
        super(pos);
        this.key = key;
    }

    public String name() {
        return key.name();
    }

    public Key key() {
        return key;
    }

    public KeyNode key(Key key) {
        KeyNode_c n = (KeyNode_c) copy();
        n.key = key;
        return n;
    }

    public boolean isDisambiguated() {
        return super.isDisambiguated() && key != null && key.isCanonical();
    }

    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        CofferTypeSystem ts = (CofferTypeSystem) sc.typeSystem();

        Key key = this.key;

        if (! key.isCanonical()) {
            CofferContext c = (CofferContext) sc.context();

            try {
                key = c.findKey(key.name());
            }
            catch (SemanticException e) {
                if (c.inCode()) {
                    key = ts.instKey(key.position(), key.name());
                }
                else {
                    throw e;
                }
            }

            if (! key.isCanonical()) {
                return this;
            }
        }

        return this.key(key);
    }

    public void addDecls(Context c) {
        CofferContext vc = (CofferContext) c;
        if (key.isCanonical()) {
            vc.addKey(key);
        }
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(key.toString());
    }

    public void translate(CodeWriter w, Translator tr) {
	throw new InternalCompilerError(position(),
	    "Cannot translate key \"" + key + "\".");
    }

    public String toString() {
        return key.toString();
    }
}
