package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Job;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class AmbiguityRemover extends SemanticVisitor
{
    public static class Kind extends Enum {
        private Kind(String name) {
            super(name);
        }
    }

    public static final Kind SUPER = new Kind("disam-super");
    public static final Kind SIGNATURES = new Kind("disam-sigs");
    public static final Kind ALL = new Kind("disam-all");

    private Kind kind;

    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf, Kind kind) {
        super(job, ts, nf);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    protected Node enterCall(Node n) throws SemanticException {
        Types.report(2, ">> " + kind + "::enter " + n);
        Node m = n.disambiguateEnter(this);
        Types.report(2, "<< " + kind + "::enter " + n + " -> " + m);
        return m;
    }

    protected Node overrideCall(Node n) throws SemanticException {
        return n.disambiguateOverride(this);
    }

    protected Node leaveCall(Node n) throws SemanticException {
        Types.report(2, ">> " + kind + "::leave " + n);
        Node m = n.disambiguate(this);
        Types.report(2, "<< " + kind + "::leave " + n + " -> " + m);
        return m;
    }
}
