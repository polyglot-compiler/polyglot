package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * A local variable declaration statement: a type, a name and an optional
 * initializer.
 */
public class LocalDecl_c extends Stmt_c implements LocalDecl
{
    Declarator decl;
    LocalInstance li;

    public LocalDecl_c(Del ext, Position pos, Flags flags, TypeNode type, String name, Expr init) {
        super(ext, pos);
	this.decl = new Declarator_c(flags, type, name, init);
    }

    /** Get the type of the declaration. */
    public Type declType() {
        return decl.declType();
    }

    /** Get the flags of the declaration. */
    public Flags flags() {
        return decl.flags();
    }

    /** Set the flags of the declaration. */
    public LocalDecl flags(Flags flags) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.flags(flags);
	return n;
    }

    /** Get the type node of the declaration. */
    public TypeNode type() {
        return decl.type();
    }

    /** Set the type node of the declaration. */
    public LocalDecl type(TypeNode type) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.type(type);
	return n;
    }

    /** Get the name of the declaration. */
    public String name() {
        return decl.name();
    }

    /** Set the name of the declaration. */
    public LocalDecl name(String name) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.name(name);
	return n;
    }

    /** Get the initializer of the declaration. */
    public Expr init() {
        return decl.init();
    }

    /** Set the initializer of the declaration. */
    public LocalDecl init(Expr init) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.init(init);
	return n;
    }

    /** Get the local instance of the declaration. */
    public LocalInstance localInstance() {
        return li;
    }

    /** Set the local instance of the declaration. */
    public LocalDecl localInstance(LocalInstance li) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.li = li;
        return n;
    }

    /**
     * Get the declarator.
     */
    protected Declarator decl() {
        return decl;
    }

    /**
     * Set the declarator.
     */
    protected LocalDecl decl(Declarator decl) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.decl = decl;
        return n;
    }

    /** Reconstruct the declaration. */
    protected LocalDecl_c reconstruct(TypeNode type, Expr init) {
        if (type() != type || init() != init) {
	    LocalDecl_c n = (LocalDecl_c) copy();
	    n.decl = (Declarator_c) decl.copy();
	    n.decl = n.decl.type(type);
	    n.decl = n.decl.init(init);
	    return n;
	}

	return this;
    }

    /** Visit the children of the declaration. */
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(type(), v);
	Expr init = (Expr) visitChild(init(), v);
	return reconstruct(type, init);
    }

    public Context enterScope(Context c) {
        c.addVariable(li);
        return c;
    }

    public Context updateScope(Context c) {
        c.addVariable(li);
        return c;
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        LocalDecl_c n = (LocalDecl_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        LocalInstance li = ts.localInstance(position(), Flags.NONE,
                                            ts.unknownType(position()), name());
        return n.localInstance(li);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	TypeSystem ts = ar.typeSystem();

	LocalInstance li = ts.localInstance(position(),
	    				    flags(), declType(), name());

        if (li.flags().isFinal() && init() != null && init().isConstant()) {
            Object value = init().constantValue();
            li = (LocalInstance) li.constantValue(value);
        }

	return localInstance(li);
    }

    /** Type check the declaration. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	try {
	    ts.checkLocalFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	decl.typeCheck(tc);

        // Check if the variable is multiply defined.
        // FIXME: This won't find variables defined in the same scope.
        // LocalDecl should be a "let".
        Context c = tc.context();

	try {
	    c.pop().findLocal(li.name());
	}
	catch (SemanticException e) {
            // not found, so not multiply-defined
            return this;
	}

        throw new SemanticException(
            "Local variable " + li + " multiply-defined in " +
            c.currentCode() + ".");
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        return decl.childExpectedType(child, av);
    }

    public String toString() {
	return decl.toString();
    }

    /** Write the declaration to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        boolean semi = tr.appendSemicolon(true);

	decl.prettyPrint(w, tr, false);

	if (semi) {
	    w.write(";");
	}

	tr.appendSemicolon(semi);
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (li != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + li + ")");
	    w.end();
	}

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(decl " + decl + ")");
        w.end();
    }
}
