/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import polyglot.ast.ArrayInit;
import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Binary.Operator;
import polyglot.ast.Block;
import polyglot.ast.BooleanLit;
import polyglot.ast.Cast;
import polyglot.ast.Conditional;
import polyglot.ast.ConstructorCall;
import polyglot.ast.Empty;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.If;
import polyglot.ast.IntLit;
import polyglot.ast.Labeled;
import polyglot.ast.Lit;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Loop;
import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.ast.Unary;
import polyglot.frontend.Job;
import polyglot.types.ArrayType;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.NullType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.UniqueID;

/**
 * Flattens expressions and removes initializers from local variable
 * declarations. Requires LoopNormalizer to be run first.
 *
 * Flattened statements are all of the following forms:
 * <ul>
 * <li>x = y;
 * <li>x = unop y;
 * <li>x = y binop z;
 * <li>x = y.m(z1, z2, ...);
 * <li>x = y.f;
 * <li>x.f = y;
 * <li>x = y[z];
 * <li>x[y] = z;
 * <li>if (x) { ... }
 * <li>while (x) { ... }
 * <li>switch (x) { ... }
 * <li>throw x;
 * <li>return x;
 * </ul>
 *
 * Notes:
 * <ul>
 * <li>x, y, and z are all local variables (or constants where allowed).
 * <li>binop will not be another assignment.
 * <li>Increment and decrement (++, --) operators, as well as compound
 *     assignments (+= etc.), will be broken into simple assignments (=).
 * </ul>
 * 
 * To summarize, each statement will make at most one write, and contain at
 * most one dereference. The only write operation is the simple assignment (=).
 */
public class ExpressionFlattener extends NodeVisitor {

    protected final Job job;
    protected final TypeSystem ts;
    protected final NodeFactory nf;

    /** Stack of nested blocks we are currently in. */
    protected final Stack<List<Stmt>> blockStack = new Stack<>();

    /** Set of expressions not to flatten. Only applies to the expressions
      * themselves, and not their subexpressions (unless they are also in
      * the set explicitly). */
    protected final Set<Expr> dontFlatten = new HashSet<>();

    /** Used to copy a whole AST subtree. */
    protected final DeepCopy deepCopier;

    /** Whether to move initializers of created localDecls to assignments */
    protected boolean flatten_all_decls;

    public ExpressionFlattener(Job job, TypeSystem ts, NodeFactory nf) {
        this(job, ts, nf, false);
    }

    public ExpressionFlattener(Job job, TypeSystem ts, NodeFactory nf,
            boolean flatten_all_decls) {
        super(nf.lang());
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        this.deepCopier = new DeepCopy(nf.lang());
        this.flatten_all_decls = flatten_all_decls;
    }

    @Override
    public Node override(Node parent, Node n) {
        // insert blocks when needed to allow local decls to be inserted.
        if (n instanceof If) {
            If s = (If) n;
            Stmt s1 = s.consequent();
            Stmt s2 = s.alternative();

            if (!(s1 instanceof Block)) {
                s = s.consequent(createBlock(s1));
            }

            if (s2 != null && !(s2 instanceof Block)) {
                s = s.alternative(createBlock(s2));
            }

            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof Loop) {
            Loop lp = (Loop) n;
            Stmt b = visitEdge(lp, createBlock(lp.body()));
            lp = lp.body(b);
            if (!(parent instanceof Labeled)) {
                // we are not a labeled loop
                // add lp to the stmt list.
                addStmt(lp);
            }
            else {
                // we are a labeled loop. Let the parent
                // (i.e., the Labeled AST node) take
                // care of adding us to stmt list.
            }
            return lp;
        }

        // short circuit boolean && and ||
        if (n instanceof Binary) {
            Binary b = (Binary) n;
            return translateBinary(b);
        }

        // conditional ? :
        if (n instanceof Conditional) {
            Conditional c = (Conditional) n;
            Expr cond = visitEdge(c, c.cond());
            LocalDecl d = createDecl(c.type(), c.position(), null);
            addStmt(d);

            Local l = createLocal(d);
            If s = createCondIf(cond, l, c.consequent(), c.alternative(), c);
            s = visitEdge(c, s);
            addStmt(s);

            return l;
        }

        // nothing we can do about constructor calls
        if (n instanceof ConstructorCall) {
            addStmt((Stmt) n);
            return n;
        }

        // can't flatten initializers in field decls
        if (n instanceof FieldDecl) {
            return n;
        }

        return null;
    }

    @Override
    public NodeVisitor enter(Node n) {
        // push a new statement list for each block
        if (n instanceof Block) {
            pushBlock();
        }

        // don't flatten the top level expression in an eval
        if (n instanceof Eval) {
            Eval s = (Eval) n;
            addDontFlatten(s.expr());
        }

        // don't flatten top level lhs
        if (n instanceof Assign) {
            Assign a = (Assign) n;
            addDontFlatten(a.left());

            // if lhs is a local, no need to flatten top level rhs, unless it's
            // another assign
            if (a.left() instanceof Local && !isAssign(a.right())) {
                addDontFlatten(a.right());
            }
        }

        // decl with init is like local assign
        if (n instanceof LocalDecl) {
            LocalDecl d = (LocalDecl) n;
            Expr e = d.init();

            if (e != null && !isAssign(e)) {
                addDontFlatten(e);
            }
        }

        // don't flatten array initializers
        if (n instanceof ArrayInit) {
            addDontFlatten((ArrayInit) n);
        }

        // don't flatten target of ++ and --
        if (n instanceof Unary) {
            Unary u = (Unary) n;

            if (isAssign(u)) {
                addDontFlatten(u.expr());
            }
        }

        // don't flatten constant expressions
        if (n instanceof Expr) {
            Expr e = (Expr) n;
            if (lang().isConstant(e, lang())) {
                addDontFlatten(e);
            }
        }

        return this;
    }

    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        // replace blocks with list of replacement statements
        if (n instanceof Block) {
            Block b = (Block) n;
            n = b.statements(popBlock());
        }

        // if a top level expression ended up as a flattened local, remove it;
        // otherwise we have the illegal statement "local_var;"
        if (n instanceof Eval) {
            Eval ev = (Eval) n;
            if (ev.expr() instanceof Local) {
                n = createEmpty();
            }
        }

        // remove initializer from decl
        if (n instanceof LocalDecl) {
            n = translateLocalDecl((LocalDecl) n);
        }

        // add statement to parent block
        if (n instanceof Stmt && parent instanceof Block) {
            addStmt((Stmt) n);
            return n;
        }

        // not in any block, so nothing to do
        if (!inBlock()) {
            return n;
        }

        if (n instanceof Expr) {
            Expr e = (Expr) n;
            Expr o = (Expr) old;
            return flattenExpr(e, o);
        }

        return n;
    }

    public Expr flattenExpr(Expr n, Expr old) {
        boolean flatten = !dontFlatten(old);

        Type t = declType(n.type());

        // special handling of ++ and -- (postfix versions are a pain)
        if (n instanceof Unary && isAssign(n)) {
            Unary u = (Unary) n;
            Block inc = createBlock(createIncDec(u));
            Local l = null;
            Eval a = null;

            if (flatten) {
                Expr e = u.expr();
                LocalDecl d = createDecl(t, e.position(), null);
                addStmt(d);
                l = createLocal(d);
                a = createAssign(l, e);
            }

            if (u.operator().isPrefix()) {
                inc = visitEdge(n, inc);
                addStmt(inc);

                if (flatten) {
                    addStmt(a);
                }
            }
            else {
                if (flatten) {
                    addStmt(a);
                }

                inc = visitEdge(n, inc);
                addStmt(inc);
            }

            if (flatten) {
                return l;
            }
            else {
                return nf.Local(n.position().startOf(),
                                nf.Id(n.position().startOf(), "dummy"));
            }
        }

        // break up compound assignments (+= etc)
        if (n instanceof Assign && !isSimpleAssign(n)) {
            Assign a = (Assign) n;
            a = createSimpleAssign(a);

            if (!flatten) {
                addDontFlatten(a);
            }

            return visitEdge(n, a);
        }

        // flatten all other expressions
        if (flatten) {
            Expr e = n;
            Expr val = e;

            // if e is an assign, new value comes from lhs
            if (e instanceof Assign) {
                Assign a = (Assign) e;
                val = (Expr) deepCopy(a.left());
                Eval s = createEval(a);
                addStmt(s);
            }

            // create a local temp for the expression
            if (!flatten_all_decls) {
                return createDeclWithInit(t, e.position(), val);
            }
            else {
                LocalDecl d = createDecl(t, val.position(), null);
                addStmt(d);
                Local l = createLocal(d);
                addStmt(createAssign(l, val));
                return createLocal(d);
            }
        }

        return n;
    }

    protected Type declType(Type t) {
        return t;
    }

    protected Expr translateBinary(Binary b) {
        if (neverFlatten(b)) {
            return b;
        }
        return translateShortCircuitBinary(b);
    }

    protected Expr translateShortCircuitBinary(Binary b) {
        if (b.operator() == Binary.COND_AND) {
            Expr left = visitEdge(b, b.left());

            if (left instanceof BooleanLit) {
                BooleanLit lit = (BooleanLit) left;

                if (!lit.value()) {
                    return lit; // constant false
                }
                else {
                    return visitEdge(b, b.right()); // only rhs matters
                }
            }
            else {
                LocalDecl d = createDecl(ts.Boolean(), b.position(), null);
                addStmt(d);

                Local r = createLocal(d);
                If s = createAndIf(left, r, b.right(), b);
                s = visitEdge(b, s);
                addStmt(s);

                return r;
            }
        }
        else if (b.operator() == Binary.COND_OR) {
            Expr left = visitEdge(b, b.left());

            if (left instanceof BooleanLit) {
                BooleanLit lit = (BooleanLit) left;

                if (lit.value()) {
                    return lit; // constant true
                }
                else {
                    return visitEdge(b, b.right()); // only rhs matters
                }
            }
            else {
                LocalDecl d = createDecl(ts.Boolean(), b.position(), null);
                addStmt(d);

                Local r = createLocal(d);
                If s = createOrIf(left, r, b.right(), b);
                s = visitEdge(b, s);
                addStmt(s);

                return r;
            }
        }
        return null;
    }

    protected Stmt translateLocalDecl(LocalDecl d) {
        Stmt n = d;
        Expr e = d.init();

        if (e != null) {
            if (d.flags().isFinal()) {
                d = d.init(null);
            }
            else {
                d = d.init(defaultValue(d.declType(), d.position()));
            }
            addStmt(d);
            // create new array expression 
            // if initializer is an arrayinit
            if (e instanceof ArrayInit) {
                if (e.type() instanceof ArrayType) {
                    ArrayType at = (ArrayType) e.type();
                    e =
                            createNewArray((ArrayInit) e,
                                           at.ultimateBase(),
                                           at.dims());
                }
                else if (e.type() instanceof NullType) {
                    ArrayType at = (ArrayType) d.type().type();
                    e =
                            createNewArray((ArrayInit) e,
                                           at.ultimateBase(),
                                           at.dims());
                }
                else {
                    throw new InternalCompilerError("Unexpected type for array init: "
                            + e.type());
                }
            }
            Local l = createLocal(d);
            n = createAssign(l, e);
        }
        else {
            if (!d.flags().isFinal())
                n = d.init(defaultValue(d.declType(), d.position()));
        }
        return n;
    }

    /**
     * Create a NewArray expression from an ArrayInit. Raw ArrayInits are only
     * allowed in declarations.
     */
    private Expr createNewArray(ArrayInit e, Type t, int dims) {
        Position pos = e.position();
        TypeNode base = nf.CanonicalTypeNode(pos, t);
        NewArray na = nf.NewArray(pos, base, dims, e);
        return na.type(ts.arrayOf(t));
    }

    /**
     * Create a local declaration that can take a value of the
     * type t, and initialize it to the expression val.
     * Return the new local that was declared.
     * @throws SemanticException 
     */
    protected Local createDeclWithInit(Type t, Position pos, Expr val) {
        LocalDecl d = createDecl(t, pos, val);
        addStmt(d);
        // return the local temp instead of the complex expression
        Local l = createLocal(d);
        return l;
    }

    /** Returns true if e should never be flattened or if e has been added to
      * the don't flatten list. Note that is then removed from the don't flatten
      * list. */
    protected boolean dontFlatten(Expr e) {
        boolean ret = dontFlatten.contains(e);
        dontFlatten.remove(e);
        return ret || neverFlatten(e);
    }

    /** Add e to the list of expressions not to flatten. Note that this only
      * applies to e itself, and not to any of its subexpressions. */
    protected void addDontFlatten(Expr e) {
        dontFlatten.add(e);
    }

    /** Returns true if the expression e is of a type that should never be
      * flattened. */
    protected boolean neverFlatten(Expr e) {
        return e instanceof Lit || e instanceof Special || e instanceof Local;
    }

    /** Pushes a new (nested) block onto the stack. */
    protected void pushBlock() {
        blockStack.push(new ArrayList<Stmt>());
    }

    /** Pops a block off the stack. */
    protected List<Stmt> popBlock() {
        return blockStack.pop();
    }

    /** Checks to see if we are in a block. */
    protected boolean inBlock() {
        return !blockStack.empty();
    }

    /** Adds a statement to the current block. */
    protected void addStmt(Stmt s) {
        blockStack.peek().add(s);
    }

    /** Whenever a new node is created, this method is called and should do
      * additional processing of the node as needed. */
    protected <N extends Node> N postCreate(N n) {
        return n;
    }

    protected String newId() {
        return UniqueID.newID("flat");
    }

    /**
     * Returns true for assignments, and pre and post increments
     * and decrements.
     */
    protected boolean isAssign(Expr e) {
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            return u.operator() == Unary.POST_INC
                    || u.operator() == Unary.POST_DEC
                    || u.operator() == Unary.PRE_INC
                    || u.operator() == Unary.PRE_DEC;
        }

        return e instanceof Assign;
    }

    protected boolean isSimpleAssign(Expr e) {
        if (e instanceof Assign) {
            Assign a = (Assign) e;
            return a.operator() == Assign.ASSIGN;
        }
        else {
            return false;
        }
    }

    protected Node deepCopy(Node n) {
        return n.visit(deepCopier);
    }

    protected Block createBlock(Stmt s) {
        if (s instanceof Block) {
            return (Block) s;
        }

        Block b = nf.Block(s.position(), s);
        b = postCreate(b);
        return b;
    }

    protected Empty createEmpty() {
        Empty s = nf.Empty(Position.compilerGenerated());
        s = postCreate(s);
        return s;
    }

    protected Eval createEval(Expr e) {
        Eval s = nf.Eval(e.position(), e);
        s = postCreate(s);
        return s;
    }

    private Expr defaultValue(Type t, Position pos) {
        // XXX is this factored out somewhere in the code base?
        if (t.isPrimitive()) {
            if (t.isBoolean())
                return nf.BooleanLit(pos, false).type(ts.Boolean());
            if (t.isNumeric()) return nf.IntLit(pos, IntLit.INT, 0).type(t);
            throw new InternalCompilerError("Unexpected primitive type: " + t);
        }
        else {
            return nf.NullLit(pos).type(ts.Null());
        }
    }

    /**
     * Create a declaration for a local variable with the type t at
     * position pos, with initializing expression init. 
     * Return the LocalDecl produced. Do NOT add the local decl
     * to the statements
     */
    protected LocalDecl createDecl(Type t, Position pos, Expr init) {
        String name = newId();
        LocalInstance li = ts.localInstance(pos, Flags.NONE, t, name);
        if (init == null) {
            init = defaultValue(t, pos);
        }
        LocalDecl d =
                nf.LocalDecl(pos,
                             Flags.NONE,
                             postCreate(nf.CanonicalTypeNode(pos, t)),
                             postCreate(nf.Id(pos, name)),
                             init);
        d = d.localInstance(li);
        d = postCreate(d);
        return d;
    }

    /**
     * Create a use of the Local that is declared in the LocalDecl d
     */
    protected Local createLocal(LocalDecl d) {
        Position pos = d.position();
        LocalInstance li = d.localInstance();
        Local l =
                (Local) nf.Local(pos, postCreate(nf.Id(pos, d.name())))
                          .type(typeOf(li));
        l = l.localInstance(li);
        l = postCreate(l);
        return l;
    }

    /**
     * Create an assignment from r to l, i.e., "l = r;"
     * @param l
     * @param r
     * @return
     */
    protected Eval createAssign(Expr l, Expr r) {
        Position pos = l.position();
        l = (Expr) deepCopy(l);
        Eval a =
                nf.Eval(pos,
                        postCreate(nf.Assign(pos, l, Assign.ASSIGN, r)).type(typeOf(l)));
        a = postCreate(a);
        return a;
    }

    /**
     * Convert an assignment "l op= r" to "l = (L)(l op r)" where L is the type of l
     */
    protected Assign createSimpleAssign(Assign a) {
        Position pos = a.position();
        Operator op = a.operator().binaryOperator();

        Binary b =
                postCreate(nf.Binary(pos,
                                     (Expr) deepCopy(a.left()),
                                     op,
                                     a.right()));
        if (typeOf(a.left()).isNumeric() && typeOf(a.right()).isNumeric()) {
            try {
                b =
                        (Binary) b.type(ts.promote(typeOf(a.left()),
                                                   typeOf(a.right())));
            }
            catch (SemanticException e) {
                throw new InternalCompilerError(e);
            }
        }
        else {
            b = (Binary) b.type(a.left().type());
        }
        TypeNode tn = postCreate(nf.CanonicalTypeNode(pos, a.left().type()));
        Cast c = postCreate(nf.Cast(pos, tn, b));
        c = (Cast) c.type(a.left().type());

        a = (Assign) nf.Assign(pos, a.left(), Assign.ASSIGN, c).type(typeOf(a));
        a = postCreate(a);
        return a;
    }

    /**
     * Convert an increment or decrement to an assignment,
     * e.g. "i++" to "i = i + 1"
     * @throws SemanticException 
     */
    protected Eval createIncDec(Unary u) {
        Position pos = u.position();
        Operator op;

        if (u.operator() == Unary.PRE_INC || u.operator() == Unary.POST_INC) {
            op = Binary.ADD;
        }
        else {
            op = Binary.SUB;
        }

        Expr e = u.expr();
        Expr e2 = (Expr) deepCopy(e);
        //XXX: ugh.  Special case for char. javac allows char++ but not char + 1;
        if (e.type().isChar())
            e2 =
                    nf.Cast(pos, nf.CanonicalTypeNode(pos, ts.Int()), e2)
                      .type(ts.Int());

        Expr plus;
        try {
            plus =
                    nf.Binary(pos, e2, op, createInt(1))
                      .type(ts.promote(typeOf(e2)));
        }
        catch (SemanticException se) {
            throw new InternalCompilerError(se);
        }
        plus =
                nf.Cast(pos, nf.CanonicalTypeNode(pos, e.type()), plus)
                  .type(e.type());

        Eval a = createAssign(e, postCreate(plus).type(typeOf(e)));
        return a;
    }

    /**
     * Create an if statement that assigns to l the value
     * of "cond && e", evaluating e only if "cond" is true.
     * i.e., "if (cond) l = e; else l = false;"
     */
    protected If createAndIf(Expr cond, Local l, Expr e, Binary original) {
        Position pos = l.position();
        cond = (Expr) deepCopy(cond);
        If s =
                nf.If(pos,
                      cond,
                      createAssign(l, e),
                      createAssign(l, createBool(false)));
        s = postCreate(s);
        return s;
    }

    /**
     * Create an if statement that assigns to l the value
     * of "cond || e", evaluating e only if "cond" is false.
     * i.e., "if (cond) l = true; else l = e;"
     */
    protected If createOrIf(Expr cond, Local l, Expr e, Binary original) {
        Position pos = l.position();
        cond = (Expr) deepCopy(cond);
        If s =
                nf.If(pos,
                      cond,
                      createAssign(l, createBool(true)),
                      createAssign(l, e));
        s = postCreate(s);
        return s;
    }

    /**
     * Create an if statement that assigns to l the value
     * of "cond ? e1 : e2"
     * i.e., "if (cond) l = e1; else l = e2"
     */
    protected If createCondIf(Expr cond, Local l, Expr e1, Expr e2,
            Conditional original) {
        Position pos = l.position();
        If s = nf.If(pos, cond, createAssign(l, e1), createAssign(l, e2));
        s = postCreate(s);
        return s;
    }

    /**
     * Create a boolean literal
     */
    protected BooleanLit createBool(boolean val) {
        return (BooleanLit) postCreate(nf.BooleanLit(Position.compilerGenerated(),
                                                     val)).type(ts.Boolean());
    }

    /**
     * Create an int literal
     */
    protected IntLit createInt(int val) {
        return (IntLit) postCreate(nf.IntLit(Position.compilerGenerated(),
                                             IntLit.INT,
                                             val)).type(ts.Int());
    }

    protected Type typeOf(Expr e) {
        return e.type();
    }

    protected Type typeOf(LocalInstance li) {
        return li.type();
    }
}
