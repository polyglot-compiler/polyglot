package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.types.Package;

import java.io.IOException;
import java.util.*;

/** Visitor which traverses the AST constructing type objects. */
public class AddMemberVisitor extends SemanticVisitor
{
    public AddMemberVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
        return n.del().addMembersEnter(this);
    }

    protected Node overrideCall(Node n) throws SemanticException {
        return n.del().addMembersOverride(this);
    }

    protected Node leaveCall(Node n) throws SemanticException {
        return n.del().addMembers(this);
    }
}
