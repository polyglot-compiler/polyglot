package polyglot.ext.jl.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.main.Report;
import java.util.*;

/**
 * A <code>LocalDecl</code> is an immutable representation of the declaration
 * of a local variable.
 */
public class LocalDecl_c extends Node_c implements LocalDecl {
    Flags flags;
    TypeNode type;
    String name;
    Expr init;
    LocalInstance li;

    public LocalDecl_c(Position pos, Flags flags, TypeNode type,
                       String name, Expr init)
    {
        super(pos);
        this.flags = flags;
        this.type = type;
        this.name = name;
        this.init = init;
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
    public LocalDecl flags(Flags flags) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.flags = flags;
        return n;
    }

    /** Get the type node of the declaration. */
    public TypeNode type() {
        return type;
    }

    /** Set the type of the declaration. */
    public LocalDecl type(TypeNode type) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.type = type;
        return n;
    }

    /** Get the name of the declaration. */
    public String name() {
        return name;
    }

    /** Set the name of the declaration. */
    public LocalDecl name(String name) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.name = name;
        return n;
    }

    /** Get the initializer of the declaration. */
    public Expr init() {
        return init;
    }

    /** Set the initializer of the declaration. */
    public LocalDecl init(Expr init) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.init = init;
        return n;
    }

    /** Set the local instance of the declaration. */
    public LocalDecl localInstance(LocalInstance li) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.li = li;
        return n;
    }

    /** Get the local instance of the declaration. */
    public LocalInstance localInstance() {
        return li;
    }

    /** Reconstruct the declaration. */
    protected LocalDecl_c reconstruct(TypeNode type, Expr init) {
        if (this.type != type || this.init != init) {
            LocalDecl_c n = (LocalDecl_c) copy();
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

    /*
    public Context enterScope(Context c) {
        // Add the local to the _current_ scope.  This ensures li is in scope
        // when evaluating the initializer.
        c.addVariable(li);
        return c;
    }
    */

    public void addDecls(Context c) {
        c.addVariable(li);
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
            ts.checkLocalFlags(flags);
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        // Check if the variable is multiply defined.
        Context c = tc.context();

        try {
            c.findLocal(li.name());
        }
        catch (SemanticException e) {
            // not found, so not multiply-defined
            return this;
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

        return this;
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == init) {
            return type.type();
        }

        return child.type();
    }

    public String toString() {
        return flags.translate() + type + " " + name +
                (init != null ? " = " + init : "") + ";";
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        boolean semi = tr.appendSemicolon(true);

        w.write(flags.translate());
        print(type, w, tr);
        w.write(" ");
        w.write(name);

        if (init != null) {
            w.write(" =");
            w.allowBreak(2, " ");
            print(init, w, tr);
        }

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
    }

    public Term entry() {
        if (init() != null) {
            return init().entry();
        }
        return this;
    }

    public List acceptCFG(CFGBuilder v, List succs) {
        if (init() != null) {
            v.visitCFG(init(), this);
        }

        return succs;
    }
}
