/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.Context;
import polyglot.types.TypeSystem;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class SignatureDisambiguator extends Disambiguator
{
    public SignatureDisambiguator(DisambiguationDriver dd) {
        super(dd);
    }

    public SignatureDisambiguator(Job job, TypeSystem ts, NodeFactory nf, Context c) {
        super(job, ts, nf, c);
    }
    
    public Node override(Node parent, Node n) {
        if (n instanceof Stmt || n instanceof Expr) {
            return n;
        }
        return super.override(parent, n);
    }
}
