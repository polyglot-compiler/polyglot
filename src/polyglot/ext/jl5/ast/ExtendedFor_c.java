package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.FloatLit;
import polyglot.ast.IntLit;
import polyglot.ast.Lit;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Loop_c;
import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.Term;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class ExtendedFor_c extends Loop_c implements ExtendedFor {
    /** Loop body */
    protected LocalDecl decl;
    protected Expr expr;
    protected Stmt body;

    public ExtendedFor_c(Position pos, LocalDecl decl, Expr expr, Stmt stmt) {
        super(pos);
        this.decl = decl;
        this.expr = expr;
        this.body = stmt;
    }

    @Override
    public Stmt body() {
        return this.body;
    }

    /** Set the body of the statement. */
    @Override
    public ExtendedFor body(Stmt body) {
        ExtendedFor_c n = (ExtendedFor_c) copy();
        n.body = body;
        return n;
    }

    @Override
    public LocalDecl decl() {
        return this.decl;
    }

    /** Set the body of the statement. */
    @Override
    public ExtendedFor decl(LocalDecl decl) {
        ExtendedFor_c n = (ExtendedFor_c) copy();
        n.decl = decl;
        return n;
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public ExtendedFor expr(Expr expr) {
        ExtendedFor_c n = (ExtendedFor_c) copy();
        n.expr = expr;
        return n;
    }

    /** Reconstruct the statement. */
    protected ExtendedFor_c reconstruct(LocalDecl decl, Expr expr, Stmt body) {
        if (!decl.equals(this.decl) || expr != this.expr || body != this.body) {
            ExtendedFor_c n = (ExtendedFor_c) copy();
            n.decl = decl;
            n.expr = expr;
            n.body = body;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        LocalDecl decl = (LocalDecl) visitChild(this.decl, v);
        Expr expr = (Expr) visitChild(this.expr, v);
        Stmt body = (Stmt) visitChild(this.body, v);
        return reconstruct(decl, expr, body);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    /** Type check the statement. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        NodeFactory nf = tc.nodeFactory();
        // Check that the expr is an array or of type Iterable
        Type t = expr.type();
//        System.err.println(" t is a " + t.getClass());
//        System.err.println("    t is a " + ts.allAncestorsOf((ReferenceType) t));
//        System.err.println("    erasure(t) is " + ts.erasureType(t));
//        System.err.println("    iterable is a " + ts.Iterable().getClass());
        if (!expr.type().isArray()
                && !t.isSubtype(ts.rawClass((JL5ParsedClassType) ts.Iterable()))) {
            throw new SemanticException("Can only iterate over an array or an instance of java.util.Iterable",
                                        expr.position());
        }

        // Check that type is the same as elements in expr
        Type declType = decl().localInstance().type();
        Type elementType;
        if (expr.type().isArray()) {
            elementType = expr.type().toArray().base();
        }
        else {
            JL5SubstClassType iterableType =
                    ts.findGenericSupertype((JL5ParsedClassType) ts.Iterable(),
                                            t.toReference());
            elementType = iterableType.actuals().get(0);
        }
        if (!elementType.isImplicitCastValid(declType)) {
            throw new SemanticException("Incompatible types: required "
                    + declType + " but found " + elementType, this.position());
        }

        if (expr instanceof Local
                && decl.localInstance().equals(((Local) expr).localInstance())) {
            throw new SemanticException("Variable: " + expr
                    + " may not have been initialized", expr.position());
        }
        if (expr instanceof NewArray) {
            if (((NewArray) expr).init() != null) {
                for (Expr next : ((NewArray) expr).init().elements()) {
                    if (next instanceof Local
                            && decl.localInstance()
                                   .equals(((Local) next).localInstance())) {
                        throw new SemanticException("Varaible: "
                                                            + next
                                                            + " may not have been initialized",
                                                    next.position());
                    }
                }
            }
        }
        // Set the initializer so that the InitChecker doesn't get confused.
        Lit lit;
        Type type = decl.declType();
        Position pos = Position.compilerGenerated();
        if (type.isReference()) {
            lit = (Lit) nf.NullLit(pos).type(type.typeSystem().Null());
        }
        else if (type.isBoolean()) {
            lit = (Lit) nf.BooleanLit(pos, false).type(type);
        }
        else if (type.isInt() || type.isShort() || type.isChar()
                || type.isByte()) {
            lit = (Lit) nf.IntLit(pos, IntLit.INT, 0).type(type);
        }
        else if (type.isLong()) {
            lit = (Lit) nf.IntLit(pos, IntLit.LONG, 0).type(type);
        }
        else if (type.isFloat()) {
            lit = (Lit) nf.FloatLit(pos, FloatLit.FLOAT, 0.0).type(type);
        }
        else if (type.isDouble()) {
            lit = (Lit) nf.FloatLit(pos, FloatLit.DOUBLE, 0.0).type(type);
        }
        else throw new InternalCompilerError("Don't know default value for type "
                + type);
        return decl(decl.init(lit));
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr,
                   FlowGraph.EDGE_KEY_TRUE,
                   decl,
                   ENTRY,
                   FlowGraph.EDGE_KEY_FALSE,
                   this,
                   EXIT);
        v.visitCFG(decl, body, ENTRY);
        v.push(this).visitCFG(body, continueTarget(), ENTRY);
        return succs;
    }

    @Override
    public Expr cond() {
        return null;
    }

    @Override
    public Term continueTarget() {
        return body;
    }

    /** Write the statement to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("for (");
        w.begin(0);

        boolean oldSemiColon = tr.appendSemicolon(false);
        // print the decl without an initializer
        printBlock(decl.init(null), w, tr);
        tr.appendSemicolon(oldSemiColon);

        w.allowBreak(1, " ");
        w.write(":");
        w.allowBreak(1, " ");
        print(expr, w, tr);
        w.end();
        w.write(")");

        printSubStmt(body, w, tr);
    }

}
