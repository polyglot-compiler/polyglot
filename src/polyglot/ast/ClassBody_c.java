package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;


public class ClassBody_c extends Node_c implements ClassBody
{
    protected List members;

    public ClassBody_c(Ext ext, Position pos, List members) {
	super(ext, pos);
	this.members = TypedList.copyAndCheck(members, ClassMember.class, true);
    }

    public List members() {
	return this.members;
    }

    public ClassBody members(List members) {
	ClassBody_c n = (ClassBody_c) copy();
	n.members = TypedList.copyAndCheck(members, ClassMember.class, true);
	return n;
    }

    public ClassBody addMember(ClassMember member) {
	ClassBody_c n = (ClassBody_c) copy();
	List l = new ArrayList(this.members);
	l.add(member);
	n.members = TypedList.copyAndCheck(l, ClassMember.class, true);
	return n;
    }

    protected ClassBody_c reconstruct(List members) {
	if (! CollectionUtil.equals(members, this.members)) {
	    ClassBody_c n = (ClassBody_c) copy();
	    n.members = TypedList.copyAndCheck(members,
		                               ClassMember.class, true);
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	List members = new ArrayList(this.members.size());
	for (Iterator i = this.members.iterator(); i.hasNext(); ) {
	    ClassMember n = (ClassMember) i.next();
	    n = (ClassMember) n.visit(v);
	    members.add(n);
	}

	return reconstruct(members);
    }

    public void enterScope(Context c) {
        enterScope(c, true);
    }

    public void enterScope(Context c, boolean inherit) {
        c.pushBlock();

	ParsedClassType type = c.currentClass();

	addMembers(c, type, new HashSet(), inherit);
    }

    protected void addMembers(Context c, ReferenceType type, Set visited,
	                      boolean inherit) {
	Types.report(2, "addMembers(" + type + ")");

	if (visited.contains(type)) {
	    return;
	}

	visited.add(type);

	if (inherit) {
	    // Add supertype members first to ensure overrides work correctly.
	    if (type.superType() != null) {
		if (! type.superType().isReference()) {
		    throw new InternalCompilerError(
			"Super class \"" + type.superType() +
			"\" of \"" + type + "\" is ambiguous.  " +
			"An error must have occurred earlier.",
			type.position());
		}

		addMembers(c, type.superType().toReference(), visited, true);
	    }

	    for (Iterator i = type.interfaces().iterator(); i.hasNext(); ) {
		Type t = (Type) i.next();

		if (! t.isReference()) {
		    throw new InternalCompilerError(
			"Interface \"" + t + "\" of \"" + type +
			"\" is ambiguous.  " +
			"An error must have occurred earlier.",
			type.position());
		}

		addMembers(c, t.toReference(), visited, true);
	    }
	}

	for (Iterator i = type.methods().iterator(); i.hasNext(); ) {
	    MethodInstance mi = (MethodInstance) i.next();
	    c.addMethod(mi);
	}

	for (Iterator i = type.fields().iterator(); i.hasNext(); ) {
	    FieldInstance fi = (FieldInstance) i.next();
	    c.addVariable(fi);
	}

	if (type.isClass()) {
	    for (Iterator i = type.toClass().memberClasses().iterator();
		 i.hasNext(); ) {
		MemberClassType mct = (MemberClassType) i.next();
		c.addType(mct);
	    }
	}
    }

    public void leaveScope(Context c) {
        c.popBlock();
    }

    public String toString() {
        return "{ ... }";
    }

    public void translate_(CodeWriter w, Translator tr) {
        enterScope(tr.context());

	if (members.isEmpty()) {
	    w.write("{ }");
	}
	else {
	    w.write("{");
	    w.newline(4);
	    w.begin(0);

	    for (Iterator i = members.iterator(); i.hasNext(); ) {
		ClassMember member = (ClassMember) i.next();

		translateBlock(member, w, tr);

		if (i.hasNext()) {
		    w.newline(0);
		    w.newline(0);
		}
	    }

	    w.end();
	    w.newline(0);
	    w.write("}");
	}

	leaveScope(tr.context());
    }
}
