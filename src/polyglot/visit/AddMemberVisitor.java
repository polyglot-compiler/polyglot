package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;
import jltools.types.Package;

import java.io.IOException;
import java.util.*;

/** Visitor which traverses the AST constructing type objects. */
public class AddMemberVisitor extends SemanticVisitor
{
    public AddMemberVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected Node enterCall(Node n) throws SemanticException {
        return n.addMembersEnter(this);
    }

    protected Node overrideCall(Node n) throws SemanticException {
        return n.addMembersOverride(this);
    }

    protected Node leaveCall(Node n) throws SemanticException {
        return n.addMembers(this);
    }
}
