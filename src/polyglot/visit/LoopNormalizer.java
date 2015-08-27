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
import java.util.Arrays;
import java.util.List;

import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.BooleanLit;
import polyglot.ast.Branch;
import polyglot.ast.Do;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.For;
import polyglot.ast.ForUpdate;
import polyglot.ast.If;
import polyglot.ast.JLang;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Loop;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.While;
import polyglot.frontend.Job;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.UniqueID;

/** 
 * Turns all loops into while(true) loops.
 */
public class LoopNormalizer extends NodeVisitor {

    protected final Job job;
    protected final TypeSystem ts;
    protected final NodeFactory nf;
    protected final boolean dumbDo;

    public LoopNormalizer(Job job, TypeSystem ts, NodeFactory nf, boolean dumbDo) {
        super(nf.lang());
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        this.dumbDo = dumbDo;
    }
    
    public LoopNormalizer(Job job, TypeSystem ts, NodeFactory nf) {
        this(job, ts, nf, true);
    }

    @Override
    public JLang lang() {
        return (JLang) super.lang();
    }

    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (n instanceof While) {
            While s = (While) n;
            return translateWhile(s);
        }

        if (n instanceof Do) {
            Do s = (Do) n;
            return translateDo(s);
        }

        if (n instanceof For) {
            For s = (For) n;
            return translateFor(s);
        }

        return n;
    }

    /** Whenever a new node is created, this method is called and should do
      * additional processing of the node as needed. */
    protected <N extends Node> N postCreate(N n) {
        return n;
    }

    protected String newId() {
        return UniqueID.newID("loop");
    }

    protected Block createBlock(List<Stmt> stmts) {
        return postCreate(nf.Block(Position.compilerGenerated(), stmts));
    }

    protected Block createBlock() {
        return postCreate(nf.Block(Position.compilerGenerated()));
    }

    protected While createLoop(Loop source) {
        Position pos = source.position();
        While w = nf.While(pos, createBool(true), createBlock());
        w = postCreate(w);
        return w;
    }

    protected LocalDecl createLoopVar(Loop source, Expr cond) {
        Position pos = source.position();
        LocalInstance li =
                ts.localInstance(pos, Flags.NONE, ts.Boolean(), newId());
        LocalDecl var =
                nf.LocalDecl(pos,
                             Flags.NONE,
                             postCreate(nf.CanonicalTypeNode(pos, ts.Boolean())),
                             postCreate(nf.Id(pos, li.name())),
                             cond);
        var = var.localInstance(li);
        var = postCreate(var);
        return var;
    }

    protected LocalDecl createLoopVar(Loop source) {
        return createLoopVar(source, createBool(false));
    }

    protected If createLoopIf(LocalDecl var, Stmt body) {
        Position pos = var.position();
        Local cond = createLocal(var.localInstance(), pos);
        Branch exit = postCreate(nf.Branch(pos, Branch.BREAK));
        If s = nf.If(pos, cond, body, exit);
        s = postCreate(s);
        return s;
    }

    protected Eval createAssign(LocalDecl var, Expr right) {
        Position pos = var.position();
        Local left = createLocal(var.localInstance(), pos);
        Eval a =
                nf.Eval(pos,
                        postCreate(nf.Assign(pos, left, Assign.ASSIGN, right)));
        a = postCreate(a);
        return a;
    }

    protected Eval createAssign(LocalDecl var) {
        return createAssign(var, createBool(true));
    }

    protected If createInitIf(LocalDecl var, Expr cond) {
        Position pos = var.position();
        Local use = createLocal(var.localInstance(), pos);
        If s = nf.If(pos, use, createAssign(var, cond), createAssign(var));
        s = postCreate(s);
        return s;
    }

    protected If createIterIf(LocalDecl var, List<ForUpdate> iters) {
        Position pos = var.position();
        Local use = createLocal(var.localInstance(), pos);
        List<Stmt> stmts = new ArrayList<>(iters.size());

        for (Stmt s : iters) {
            stmts.add(postCreate(s));
        }

        If s = nf.If(pos, use, createBlock(stmts));
        s = postCreate(s);
        return s;
    }

    protected Local createLocal(LocalInstance li, Position pos) {
        Local l = nf.Local(pos, nf.Id(pos, li.name()));
        l = l.localInstance(li);
        l = (Local) l.type(li.type());
        l = postCreate(l);
        return l;
    }

    protected void addInits(List<Stmt> stmts, For source) {
        for (Stmt s : source.inits()) {
            stmts.add(postCreate(s));
        }
    }

    protected BooleanLit createBool(boolean val) {
        return (BooleanLit) nf.BooleanLit(Position.compilerGenerated(), val)
                              .type(ts.Boolean());
    }

    /* while (e) {...}
     * 
     * becomes
     * 
     * while (true) {
     *   boolean loop = e;
     *   if (loop)
     *     {...}
     *   else
     *     break;
     * }
     */
    protected Stmt translateWhile(While s) {
        Expr cond = s.cond();

        // avoid unnecessary translations
        if (lang().condIsConstantTrue(s, lang())) {
            if (cond instanceof BooleanLit) {
                return s;
            }
            else {
                return s.cond(createBool(true));
            }
        }

        // new loop
        While w = createLoop(s);
        LocalDecl var = createLoopVar(s, cond);
        If branch = createLoopIf(var, s.body());
        List<Stmt> stmts = new ArrayList<>(2);
        stmts.add(var);
        stmts.add(branch);
        w = w.body(((Block) w.body()).statements(stmts));

        return w;
    }

    /* do {...} while (e);
     * 
     * becomes
     * 
     * {...}
     * while (e) {...} // which gets further translated
     * 
     * or
     * 
     * boolean loop = false;
     * while (true) {
     *   if (loop)
     *     loop = e;
     *   else
     *     loop = true;
     *   if (loop)
     *     {...}
     *   else
     *     break;
     * }
     * // java compiler is not smart enough to tell that the block 
     * // is executed at least once, so the loop after translation
     * // may cause compiler to emit "may not have been initialized"
     * // error
     */
    protected Stmt translateDo(Do s) {
        if (dumbDo) {
            While w = nf.While(s.position(), s.cond(), s.body());
            return createBlock(Arrays.<Stmt> asList(s.body(),
                                                    translateWhile(w)));
        } else { 
            Expr cond = s.cond();
    
            // new loop
            While w = createLoop(s);
            LocalDecl var = createLoopVar(s);
            If init = createInitIf(var, cond);
            If branch = createLoopIf(var, s.body());
            List<Stmt> stmts = new ArrayList<>(2);
            stmts.add(init);
            stmts.add(branch);
            w = w.body(((Block) w.body()).statements(stmts));
            stmts = new ArrayList<>(2);
            stmts.add(var);
            stmts.add(w);
    
            return createBlock(stmts);
        }
    }

    /* for (int i = 0; i < 10; i++) {...}
     * 
     * becomes
     * 
     * int i = 0;
     * boolean loop = false;
     * while (true) {
     *   if (loop)
     *     i++;
     *   loop = i < 10;
     *   if (loop)
     *     {...}
     *   else
     *     break;
     * }
     */
    protected Stmt translateFor(For s) {
        Expr cond = s.cond();

        // for loops allow empty conditions, which is the same as true
        if (cond == null) {
            cond = createBool(true);
        }

        // new loop
        While w = createLoop(s);
        LocalDecl var = createLoopVar(s);
        If iter = createIterIf(var, s.iters());
        Eval update = createAssign(var, cond);
        If branch = createLoopIf(var, s.body());
        List<Stmt> stmts = new ArrayList<>(3);
        stmts.add(iter);
        stmts.add(update);
        stmts.add(branch);
        w = w.body(((Block) w.body()).statements(stmts));
        stmts = new ArrayList<>(s.inits().size() + 2);
        addInits(stmts, s);
        stmts.add(var);
        stmts.add(w);

        return createBlock(stmts);
    }
}
