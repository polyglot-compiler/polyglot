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

    public static final Kind SUPER = new Kind("super");
    public static final Kind SIGNATURES = new Kind("signatures");
    public static final Kind ALL = new Kind("all");

    private Kind kind;

    public AmbiguityRemover(Job job, Kind kind) {
        super(job);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    protected Node overrideCall(Node n) throws SemanticException {
        Types.report(2, "disambiguate " + n);
        return n.ext().disambiguateOverride(this);
    }

    protected Node leaveCall(Node n) throws SemanticException {
        Node m = n.ext().disambiguate(this);
        Types.report(2, "leaving disambiguate " + n);
        return m;
    }
}
