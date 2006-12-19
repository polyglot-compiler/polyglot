/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

/** Visitor which performs type checking on the AST. */
public class Disambiguator extends AmbiguityRemover
{
    protected Context beginContext;
 
    public Disambiguator(DisambiguationDriver dd) {
        this(dd.job(), dd.typeSystem(), dd.nodeFactory(), dd.context());
    }
    
    public Disambiguator(Job job, TypeSystem ts, NodeFactory nf, Context c) {
        super(job, ts, nf);
        this.beginContext = c;
        this.context = c;
    }

    public NodeVisitor begin() {
        Disambiguator v = (Disambiguator) super.begin();
        v.context = beginContext;
        return v;
    }
}
