package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.frontend.*;
import java.util.*;

/**
 * A <code>ClassDecl</code> is the definition of a class, abstract class,
 * or interface. It may be a public or other top-level class, or an inner
 * named class, or an anonymous class.
 */
public class ClassDecl_c extends Node_c implements ClassDecl
{
    protected Flags flags;
    protected String name;
    protected TypeNode superClass;
    protected List interfaces;
    protected ClassBody body;

    protected ParsedClassType type;

    public ClassDecl_c(Ext ext, Position pos, Flags flags, String name, TypeNode superClass, List interfaces, ClassBody body) {
	    super(ext, pos);
	    this.flags = flags;
	    this.name = name;
	    this.superClass = superClass;
	    this.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
	    this.body = body;
    }

    public ParsedClassType type() {
	    return type;
    }

    public ClassDecl type(ParsedClassType type) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.type = type;
	    return n;
    }

    public Flags flags() {
	    return this.flags;
    }

    public ClassDecl flags(Flags flags) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.flags = flags;
	    return n;
    }

    public String name() {
	    return this.name;
    }

    public ClassDecl name(String name) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.name = name;
	    return n;
    }

    public TypeNode superClass() {
	    return this.superClass;
    }

    public ClassDecl superClass(TypeNode superClass) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.superClass = superClass;
	    return n;
    }

    public List interfaces() {
	    return this.interfaces;
    }

    public ClassDecl interfaces(List interfaces) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
	    return n;
    }

    public ClassBody body() {
	    return this.body;
    }

    public ClassDecl body(ClassBody body) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.body = body;
	    return n;
    }

    protected ClassDecl_c reconstruct(TypeNode superClass, List interfaces, ClassBody body) {
	    if (superClass != this.superClass || ! CollectionUtil.equals(interfaces, this.interfaces) || body != this.body) {
		    ClassDecl_c n = (ClassDecl_c) copy();
		    n.superClass = superClass;
		    n.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
		    n.body = body;
		    return n;
	    }

	    return this;
    }

    public Node visitChildren(NodeVisitor v) {
	    TypeNode superClass = (TypeNode) visitChild(this.superClass, v);
	    List interfaces = visitList(this.interfaces, v);
	    ClassBody body = (ClassBody) visitChild(this.body, v);
	    return reconstruct(superClass, interfaces, body);
    }

    public Node buildTypesEnter_(TypeBuilder tb) throws SemanticException {
	TypeSystem ts = tb.typeSystem();
	tb.pushClass(position(), flags, name);
        return this;
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	ParsedClassType type = tb.currentClass();
	tb.popClass();
        return type(type);
    }

    public void enterScope(Context c) {
	    c.pushClass(type);
    }

    public void leaveScope(Context c) {
	    c.popClass();
    }

    public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
        if (ar.kind() != AmbiguityRemover.SUPER) {
            return this;
        }

        TypeSystem ts = ar.typeSystem();

        Types.report(2, "Cleaning " + type + ".");

        if (this.superClass != null) {
            Type t = this.superClass.type();

            if (! t.isCanonical()) {
                throw new SemanticException("Could not disambiguate super" +
                        " class of " + type + ".", superClass.position());
            }

            if (! t.isClass() || t.toClass().flags().isInterface()) {
                throw new SemanticException("Super class " + t + " of " +
                        type + " is not a class.", superClass.position());
            }

            Types.report(3, "setting super type of " + this.type + " to " + t);

            this.type.superType(t);

            ts.checkCycles(t.toReference());
        }
        else {
            this.type.superType(ts.Object());
        }

        for (Iterator i = this.interfaces.iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            Type t = tn.type();

            if (! t.isCanonical()) {
                throw new SemanticException("Could not disambiguate super" +
                        " class of " + type + ".", tn.position());
            }

            if (! t.isClass() || ! t.toClass().flags().isInterface()) {
                throw new SemanticException("Interface " + t + " of " +
                        type + " is not an interface.", tn.position());
            }

            Types.report(3, "adding interface of " + this.type + " to " + t);

            this.type.addInterface(t);

            ts.checkCycles(t.toReference());
        }

        return this;
    }

    public Node addMembers_(AddMemberVisitor tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();
	NodeFactory nf = tc.nodeFactory();
        return addDefaultConstructorIfNeeded(ts, nf);
    }

    protected Node addDefaultConstructorIfNeeded(TypeSystem ts,
                                                 NodeFactory nf) {
        if (defaultConstructorNeeded()) {
            return addDefaultConstructor(ts, nf);
        }
        return this;
    }

    protected boolean defaultConstructorNeeded() {
        if (flags().isInterface()) {
            return false;
        }
        return type().constructors().isEmpty();
    }

    protected Node addDefaultConstructor(TypeSystem ts, NodeFactory nf) {
        ConstructorInstance ci = ts.defaultConstructor(position(), this.type);
        this.type.addConstructor(ci);
        ConstructorDecl cd = nf.ConstructorDecl(position(), Flags.PUBLIC,
                                                name, Collections.EMPTY_LIST,
                                                Collections.EMPTY_LIST,
                                                nf.Block(position(),
                                                nf.SuperCall(position(),
                                                Collections.EMPTY_LIST)));
        cd = (ConstructorDecl) cd.constructorInstance(ci);
        return body(body.addMember(cd));
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        // Make sure that static members are not declared inside inner classes
        // (recall that, according to the JLS, static member classes are not
        // really inner classes since they may not refer to their outer
        // instance).
        if (this.type.isMember() && this.type.flags().isStatic()) {
            ClassType container = this.type.toMember().outer();

            if (container.isMember() && ! container.flags().isStatic() ||
                container.isLocal() || container.isAnonymous()) {

                throw new SemanticException("Cannot declare static member " +
                                            "class \"" + this.type +
                                            "\" inside inner class \"" +
                                            container + "\".", position());
            }
        }

        return this;
    }

    public String toString() {
	    return flags.clearInterface().translate() +
		       (flags.isInterface() ? "interface " : "class ") + name + " " + body;
    }

    public void translate_(CodeWriter w, Translator tr) {
	    enterScope(tr.context());

	    w.write(flags.clearInterface().translate());

	    if (flags.isInterface()) {
		    w.write("interface ");
	    }
	    else {
		    w.write("class ");
	    }

	    w.write(name);

	    if (superClass() != null) {
		    w.write(" extends ");
		    superClass().ext().translate(w, tr);
	    }

	    if (! interfaces.isEmpty()) {
		    if (flags.isInterface()) {
			    w.write(" extends ");
		    }
		    else {
			    w.write(" implements ");
		    }

		    for (Iterator i = interfaces().iterator(); i.hasNext(); ) {
			    TypeNode tn = (TypeNode) i.next();
			    tn.ext().translate(w, tr);

			    if (i.hasNext()) {
				    w.write (", ");
			    }
		    }
	    }

	    w.write(" ");
	    body().ext().translate(w, tr);

	    leaveScope(tr.context());
    }

	public void dump(CodeWriter w) {
		super.dump(w);

		w.allowBreak(4, " ");
		w.begin(0);
		w.write("(name " + name + ")");
		w.end();

		if (type != null) {
			w.allowBreak(4, " ");
			w.begin(0);
			w.write("(type " + type + ")");
			w.end();
		}
	}
}
