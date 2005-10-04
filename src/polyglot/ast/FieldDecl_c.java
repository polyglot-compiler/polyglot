package polyglot.ext.jl.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

/**
 * A <code>FieldDecl</code> is an immutable representation of the declaration
 * of a field of a class.
 */
public class FieldDecl_c extends Term_c implements FieldDecl {
    Flags flags;
    TypeNode type;
    String name;
    Expr init;
    FieldInstance fi;
    InitializerInstance ii;

    public FieldDecl_c(Position pos, Flags flags, TypeNode type,
                       String name, Expr init)
    {
        super(pos);
        this.flags = flags;
        this.type = type;
        this.name = name;
        this.init = init;
    }

    public boolean isDisambiguated() {
        return fi != null && fi.isCanonical() && (init == null || (ii != null && ii.isCanonical())) && super.isDisambiguated();
    }
    
    public MemberInstance memberInstance() {
        return fi;
    }

    public VarInstance varInstance() {
        return fi;
    }

    /** Get the initializer instance of the initializer. */
    public InitializerInstance initializerInstance() {
        return ii;
    }

    /** Set the initializer instance of the initializer. */
    public FieldDecl initializerInstance(InitializerInstance ii) {
        FieldDecl_c n = (FieldDecl_c) copy();
        n.ii = ii;
        return n;
    }

    /** Get the type of the declaration. */
    public Type declType() {
        return type.type();
    }

    /** Get the flags of the declaration. */
    public Flags flags() {
        return flags;
    }

    /** Set the flags of the declaration. */
    public FieldDecl flags(Flags flags) {
        FieldDecl_c n = (FieldDecl_c) copy();
        n.flags = flags;
        return n;
    }

    /** Get the type node of the declaration. */
    public TypeNode type() {
        return type;
    }

    /** Set the type of the declaration. */
    public FieldDecl type(TypeNode type) {
        FieldDecl_c n = (FieldDecl_c) copy();
        n.type = type;
        return n;
    }

    /** Get the name of the declaration. */
    public String name() {
        return name;
    }

    /** Set the name of the declaration. */
    public FieldDecl name(String name) {
        FieldDecl_c n = (FieldDecl_c) copy();
        n.name = name;
        return n;
    }

    /** Get the initializer of the declaration. */
    public Expr init() {
        return init;
    }

    /** Set the initializer of the declaration. */
    public FieldDecl init(Expr init) {
        FieldDecl_c n = (FieldDecl_c) copy();
        n.init = init;
        return n;
    }

    /** Set the field instance of the declaration. */
    public FieldDecl fieldInstance(FieldInstance fi) {
        FieldDecl_c n = (FieldDecl_c) copy();
        n.fi = fi;
        return n;
    }

    /** Get the field instance of the declaration. */
    public FieldInstance fieldInstance() {
        return fi;
    }

    /** Reconstruct the declaration. */
    protected FieldDecl_c reconstruct(TypeNode type, Expr init) {
        if (this.type != type || this.init != init) {
            FieldDecl_c n = (FieldDecl_c) copy();
            n.type = type;
            n.init = init;
            return n;
        }

        return this;
    }

    /** Visit the children of the declaration. */
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type, v);
        Expr init = (Expr) visitChild(this.init, v);
        return reconstruct(type, init);
    }

    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return tb.pushCode();
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return this;
        }

        Flags f = flags;

        if (ct.flags().isInterface()) {
            f = f.Public().Static().Final();
        }
        
        FieldDecl n;

        if (init != null) {
            Flags iflags = f.isStatic() ? Flags.STATIC : Flags.NONE;
            InitializerInstance ii = ts.initializerInstance(init.position(),
                                                            ct, iflags);
            n = initializerInstance(ii);
        }
        else {
            n = this;
        }

        // XXX: MutableFieldInstance
        FieldInstance fi = ts.fieldInstance(position(), ct, f,
                                            ts.unknownType(position()), name);
        ct.addField(fi);

        return n.flags(f).fieldInstance(fi);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (this.fi.isCanonical()) {
            // Nothing to do.
            return this;
        }

        if (declType().isCanonical()) {
            this.fi.setType(declType());
        }
        
        return this;
    }

    public Context enterScope(Context c) {
        if (ii != null) {
            return c.pushCode(ii);
        }
        return c;
    }
   
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        if (init != null && ! init.constantValueSet()) {
            // HACK to add dependencies for computing the constant value.
            final Scheduler scheduler = cc.typeSystem().extensionInfo().scheduler();
            final Goal myGoal = scheduler.FieldConstantsChecked(this.fi);

            init.visit(new NodeVisitor() {
                public Node leave(Node old, Node n, NodeVisitor v) {
                    if (n instanceof Field) {
                        Field f = (Field) n;
                        if (!f.fieldInstance().orig().constantValueSet()) {
                            Goal newGoal = scheduler.FieldConstantsChecked(f.fieldInstance().orig());
                            for (Iterator i = newGoal.prerequisiteGoals(scheduler).iterator(); i.hasNext();) {
                                Goal g = (Goal) i.next();
                                if (scheduler.prerequisiteDependsOn(g, myGoal)) {
                                    FieldDecl_c.this.fi.setNotConstant();
                                }
                            }
                            throw new MissingDependencyException(newGoal, true);
                        }
                    }
                    return n;
               }
            });
            
            return this;
        }
        
        if (init == null || ! init.isConstant() || ! fi.flags().isFinal()) {
            fi.setNotConstant();
        }
        else {
            fi.setConstantValue(init.constantValue());
        }

        return this;
    }
    
    public boolean constantValueSet() {
        return fi != null && fi.constantValueSet();
    }

    /** Type check the declaration. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        try {
            ts.checkFieldFlags(flags);
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        if (tc.context().currentClass().flags().isInterface()) {
            if (flags.isProtected() || flags.isPrivate()) {
                throw new SemanticException("Interface members must be public.",
                                            position());
            }
        }

        if (init != null) {
            if (init instanceof ArrayInit) {
                ((ArrayInit) init).typeCheckElements(type.type());
            }
            else {
                if (! ts.isImplicitCastValid(init.type(), type.type()) &&
                    ! ts.equals(init.type(), type.type()) &&
                    ! ts.numericConversionValid(type.type(),
                                                init.constantValue())) {

                    throw new SemanticException("The type of the variable " +
                                                "initializer \"" + init.type() +
                                                "\" does not match that of " +
                                                "the declaration \"" +
                                                type.type() + "\".",
                                                init.position());
                }
            }
        }

        // check that inner classes do not declare static fields, unless they
        // are compile-time constants
        if (flags().isStatic() &&
              fieldInstance().container().toClass().isInnerClass()) {
            // it's a static field in an inner class.
            if (!flags().isFinal() || init == null || !init.isConstant()) {
                throw new SemanticException("Inner classes cannot declare " +
                        "static fields, unless they are compile-time " +
                        "constant fields.", this.position());
            }

        }

        return this;
    }

    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        SubtypeSet s = ec.throwsSet();

        for (Iterator i = s.iterator(); i.hasNext(); ) {
            Type t = (Type) i.next();

            if (! t.isUncheckedException()) {
                ec.throwsSet().clear();
                throw new SemanticException(
                    "A field initializer may not throw a "
                    + t + ".", position());
            }
        }

        ec.throwsSet().clear();

        return super.exceptionCheck(ec);
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == init) {
            TypeSystem ts = av.typeSystem();

            // If the RHS is an integral constant, we can relax the expected
            // type to the type of the constant.
            if (ts.numericConversionValid(type.type(), child.constantValue())) {
                return child.type();
            }
            else {
                return type.type();
            }
        }

        return child.type();
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term entry() {
        return type.entry();
    }

    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs) {        
        Term next = init != null ? init.entry() : this;
        v.visitCFG(type, next);
        if (init != null) {
            v.visitCFG(init, this);
        }
        return succs;
    }


    public String toString() {
        return flags.translate() + type + " " + name +
                (init != null ? " = " + init : "");
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        boolean isInterface = fi != null && fi.container() != null &&
                              fi.container().toClass().flags().isInterface();

        Flags f = flags;

        if (isInterface) {
            f = f.clearPublic();
            f = f.clearStatic();
            f = f.clearFinal();
        }

        w.write(f.translate());
        print(type, w, tr);
        w.write(" ");
        w.write(name);

        if (init != null) {
            w.write(" =");
            w.allowBreak(2, " ");
            print(init, w, tr);
        }

        w.write(";");
    }

    public void dump(CodeWriter w) {
        super.dump(w);

        if (fi != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + fi + ")");
            w.end();
        }

	w.allowBreak(4, " ");
	w.begin(0);
	w.write("(name " + name + ")");
	w.end();
    }
}
