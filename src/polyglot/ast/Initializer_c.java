package polyglot.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SubtypeSet;
import polyglot.visit.*;

/**
 * An <code>Initializer</code> is an immutable representation of an
 * initializer block in a Java class (which appears outside of any
 * method).  Such a block is executed before the code for any of the
 * constructors.  Such a block can optionally be static, in which case
 * it is executed when the class is loaded.
 */
public class Initializer_c extends Term_c implements Initializer
{
    protected Flags flags;
    protected Block body;
    protected InitializerInstance ii;

    public Initializer_c(Position pos, Flags flags, Block body) {
	super(pos);
	this.flags = flags;
	this.body = body;
    }
    
    public boolean isDisambiguated() {
        return ii != null && ii.isCanonical() && super.isDisambiguated();
    }

    public MemberInstance memberInstance() {
        return ii;
    }

    /** Get the flags of the initializer. */
    public Flags flags() {
	return this.flags;
    }

    /** Set the flags of the initializer. */
    public Initializer flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
	Initializer_c n = (Initializer_c) copy();
	n.flags = flags;
	return n;
    }

    /** Get the initializer instance of the initializer. */
    public InitializerInstance initializerInstance() {
        return ii;
    }

    public CodeInstance codeInstance() {
	return initializerInstance();
    }

    /** Set the initializer instance of the initializer. */
    public Initializer initializerInstance(InitializerInstance ii) {
        if (ii == this.ii) return this;
	Initializer_c n = (Initializer_c) copy();
	n.ii = ii;
	return n;
    }

    /** Get the body of the initializer. */
    public Block body() {
	return this.body;
    }

    /** Set the body of the initializer. */
    public CodeDecl body(Block body) {
	Initializer_c n = (Initializer_c) copy();
	n.body = body;
	return n;
    }

    /** Reconstruct the initializer. */
    protected Initializer_c reconstruct(Block body) {
	if (body != this.body) {
	    Initializer_c n = (Initializer_c) copy();
	    n.body = body;
	    return n;
	}

	return this;
    }

    /** Visit the children of the initializer. */
    public Node visitChildren(NodeVisitor v) {
	Block body = (Block) visitChild(this.body, v);
	return reconstruct(body);
    }

    public Context enterScope(Context c) {
	return c.pushCode(ii);
    }

    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return tb.pushCode();
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term entry() {
        return this.body().entry();
    }

    public List acceptCFG(CFGBuilder v, List succs) {
        v.visitCFG(this.body(), this);
        return succs;
    }

    /** Build type objects for the method. */
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();
        ClassType ct = tb.currentClass();
        InitializerInstance ii = ts.initializerInstance(position(), ct, flags);
        return initializerInstance(ii);
    }

    /** Type check the initializer. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	try {
	    ts.checkInitializerFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

        // check that inner classes do not declare static initializers
        if (flags().isStatic() &&
              initializerInstance().container().toClass().isInnerClass()) {
            // it's a static initializer in an inner class.
            throw new SemanticException("Inner classes cannot declare " + 
                    "static initializers.", this.position());             
        }

	return this;
    }

    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec) throws SemanticException {
        if (initializerInstance().flags().isStatic()) {
            return ec.push(new ExceptionChecker.CodeTypeReporter("static initializer block"));
        }
        
        if (!initializerInstance().container().toClass().isAnonymous()) {
            ec = ec.push(new ExceptionChecker.CodeTypeReporter("instance initializer block"));

            // An instance initializer of a named class may not throw
            // a checked exception unless that exception or one of its 
            // superclasses is explicitly declared in the throws clause
            // of each contructor or its class, and the class has at least
            // one explicitly declared constructor.
            SubtypeSet allowed = null;
            Type throwable = ec.typeSystem().Throwable();
            ClassType container = initializerInstance().container().toClass();
            for (Iterator iter = container.constructors().iterator(); iter.hasNext(); ) {
                ConstructorInstance ci = (ConstructorInstance)iter.next();
                if (allowed == null) {
                    allowed = new SubtypeSet(throwable);
                    allowed.addAll(ci.throwTypes());
                }
                else {
                    // intersect allowed with ci.throwTypes()
                    SubtypeSet other = new SubtypeSet(throwable);
                    other.addAll(ci.throwTypes());
                    SubtypeSet inter = new SubtypeSet(throwable);
                    for (Iterator i = allowed.iterator(); i.hasNext(); ) {
                        Type t = (Type)i.next();
                        if (other.contains(t)) {
                            // t or a supertype is thrown by other.
                            inter.add(t);
                        }
                    }
                    for (Iterator i = other.iterator(); i.hasNext(); ) {
                        Type t = (Type)i.next();
                        if (allowed.contains(t)) {
                            // t or a supertype is thrown by the allowed.
                            inter.add(t);
                        }
                    }
                    allowed = inter;
                }
            }
            // allowed is now an intersection of the throw types of all
            // constructors
            
            ec = ec.push(allowed);
            
            
            return ec;
        }

        return ec.push();
    }


    /** Write the initializer to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.begin(0);
	w.write(flags.translate());
	print(body, w, tr);
	w.end();
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (ii != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + ii + ")");
	    w.end();
	}
    }

    public String toString() {
	return flags.translate() + "{ ... }";
    }
}
