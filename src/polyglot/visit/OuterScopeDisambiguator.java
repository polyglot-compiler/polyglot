/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import polyglot.ast.*;
import polyglot.ast.TopLevelDecl;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.Context;
import polyglot.types.TypeSystem;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class OuterScopeDisambiguator extends Disambiguator
{
    public OuterScopeDisambiguator(DisambiguationDriver dd) {
        super(dd);
    }

    public OuterScopeDisambiguator(Job job, TypeSystem ts, NodeFactory nf, Context c) {
        super(job, ts, nf, c);
    }
    
    public Node override(Node parent, Node n) {
        // Only visit imports and package declarations.
        if (n instanceof TopLevelDecl) {
            return n;
        }
        return null;
    }
}
