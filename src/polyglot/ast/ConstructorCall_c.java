package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.util.*;
import polyglot.types.*;
import polyglot.visit.*;
import java.util.*;

/**
 * A <code>ConstructorCall_c</code> represents a direct call to a constructor.
 * For instance, <code>super(...)</code> or <code>this(...)</code>.
 */
public class ConstructorCall_c extends Stmt_c implements ConstructorCall
{
    protected Kind kind;
    protected Expr qualifier;
    protected List arguments;
    protected ConstructorInstance ci;

    public ConstructorCall_c(Position pos, Kind kind, Expr qualifier, List arguments) {
	super(pos);
	this.kind = kind;
	this.qualifier = qualifier;
	this.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
    }

    /** Get the qualifier of the constructor call. */
    public Expr qualifier() {
	return this.qualifier;
    }

    /** Set the qualifier of the constructor call. */
    public ConstructorCall qualifier(Expr qualifier) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.qualifier = qualifier;
	return n;
    }

    /** Get the kind of the constructor call. */
    public Kind kind() {
	return this.kind;
    }

    /** Set the kind of the constructor call. */
    public ConstructorCall kind(Kind kind) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.kind = kind;
	return n;
    }

    /** Get the actual arguments of the constructor call. */
    public List arguments() {
	return Collections.unmodifiableList(this.arguments);
    }

    /** Set the actual arguments of the constructor call. */
    public ConstructorCall arguments(List arguments) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	return n;
    }

    public ProcedureInstance procedureInstance() {
	return constructorInstance();
    }

    /** Get the constructor we are calling. */
    public ConstructorInstance constructorInstance() {
        return ci;
    }

    /** Set the constructor we are calling. */
    public ConstructorCall constructorInstance(ConstructorInstance ci) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.ci = ci;
	return n;
    }

    /** Reconstruct the constructor call. */
    protected ConstructorCall_c reconstruct(Expr qualifier, List arguments) {
	if (qualifier != this.qualifier || ! CollectionUtil.equals(arguments, this.arguments)) {
	    ConstructorCall_c n = (ConstructorCall_c) copy();
	    n.qualifier = qualifier;
	    n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	    return n;
	}

	return this;
    }

    /** Visit the children of the call. */
    public Node visitChildren(NodeVisitor v) {
	Expr qualifier = (Expr) visitChild(this.qualifier, v);
	List arguments = visitList(this.arguments, v);
	return reconstruct(qualifier, arguments);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();

        // Remove super() calls for java.lang.Object.
        if (kind == SUPER && tb.currentClass() == ts.Object()) {
            return tb.nodeFactory().Empty(position());
        }

        ConstructorCall_c n = (ConstructorCall_c) super.buildTypes(tb);

        List l = new ArrayList(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
          l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci = ts.constructorInstance(position(), ts.Object(),
                                                        Flags.NONE, l,
                                                        Collections.EMPTY_LIST);
        return n.constructorInstance(ci);
    }

    /** Type check the call. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();
	Context c = tc.context();

	ClassType ct = c.currentClass();

        // The qualifier specifies the enclosing instance of this inner class.
        // The type of the qualifier must be the outer class of this
        // inner class or one of its super types.
        //
        // Example:
        //
        // class Outer {
        //     class Inner { }
        // }
        //
        // class ChildOfInner extends Outer.Inner {
        //     ChildOfInner() { (new Outer()).super(); }
        // }
        if (qualifier != null) {
            if (kind != SUPER) {
                throw new SemanticException("Can only qualify a \"super\"" +
                                            "constructor invocation.",
                                            position());
            }

            Type qt = qualifier.type();

            if (! qt.isClass()) {
                throw new SemanticException("The type of a constructor " +
                                            "invocation qualifier must be a " +
                                            "member class.", position());
            }

            ClassType qct = qt.toClass();

            boolean found = false;

            // Check if ct or a supertype of ct is an member of qct.
            for (Type t = ct; t != null; t = t.toReference().superType()) {
                try {
                    if (t.isClass() && t.toClass().isMember()) {
                        Type s = ts.findMemberClass(qct, t.toClass().name());

                        if (s == t) {
                            found = true;
                            break;
                        }
                    }
                }
                catch (SemanticException e) {
                }
            }

            if (! found) {
                throw new SemanticException("Class \"" + ct +
                                            "\" is not a member of \"" + qt +
                                            "\".", position());
            }
        }

	if (kind == SUPER) {
	    if (! ct.superType().isClass()) {
	        throw new SemanticException("Super type of " + ct +
		    " is not a class.", position());
	    }

	    ct = ct.superType().toClass();
	}

	List argTypes = new LinkedList();

	for (Iterator iter = this.arguments.iterator(); iter.hasNext();) {
	    Expr e = (Expr) iter.next();
	    argTypes.add(e.type());
	}

	ConstructorInstance ci = ts.findConstructor(ct, argTypes, c);

	return constructorInstance(ci);
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == qualifier) {
            // FIXME: Can be more specific
            return ts.Object();
        }

        Iterator i = this.arguments.iterator();
        Iterator j = ci.formalTypes().iterator();

        while (i.hasNext() && j.hasNext()) {
	    Expr e = (Expr) i.next();
	    Type t = (Type) j.next();

            if (e == child) {
                return t;
            }
        }

        return child.type();
    }

    public String toString() {
	return (qualifier != null ? qualifier + "." : "") + kind + "(...)";
    }

    /** Write the call to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (qualifier != null) {
	    print(qualifier, w, tr);
	    w.write(".");
	} 

	w.write(kind + "(");

	w.begin(0);

	for (Iterator i = arguments.iterator(); i.hasNext(); ) {
	    Expr e = (Expr) i.next();
	    print(e, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0);
	    }
	}

	w.end();

	w.write(");");
    }

    public Term entry() {
        if (qualifier != null) {
            return qualifier.entry();
        }
        return listEntry(arguments, this);
    }

    public List acceptCFG(CFGBuilder v, List succs) {
        if (qualifier != null) {
            v.visitCFG(qualifier, listEntry(arguments, this));
        }

        v.visitCFGList(arguments, this);

        return succs;
    }

    public List throwTypes(TypeSystem ts) {
        List l = new LinkedList();
        l.addAll(ci.throwTypes());
        l.addAll(ts.uncheckedExceptions());
        return l;
    }
}
