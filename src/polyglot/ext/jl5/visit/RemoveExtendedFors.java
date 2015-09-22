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
package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.Cast;
import polyglot.ast.CodeDecl;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Id;
import polyglot.ast.IntLit;
import polyglot.ast.Labeled;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Loop;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.While;
import polyglot.ext.jl5.ast.ExtendedFor;
import polyglot.frontend.Job;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Translate enums to Java 1.4 language features.
 */
public class RemoveExtendedFors extends ContextVisitor {
    public RemoveExtendedFors(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    /** track how many iterator variables we have created in this CodeDecl
     * 
     */
    private LinkedList<Integer> varCount = new LinkedList<>();

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof CodeDecl) {
            varCount.addLast(0);
        }
        return this;
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (isExtendedFor(n) && !(parent instanceof Labeled)) {
            n =
                    translateExtendedFor((ExtendedFor) n,
                                         Collections.<String> emptyList());
        }
        if (n instanceof CodeDecl) {
            varCount.removeLast();
        }
        if (n instanceof Labeled && !(parent instanceof Labeled)) {
            Node s = n;
            List<String> labels = new ArrayList<>();
            while (s instanceof Labeled) {
                Labeled lbled = (Labeled) s;
                labels.add(lbled.label());
                s = lbled.statement();
            }
            if (isExtendedFor(s)) {
                // we have a situation L1, ..., Ln: for (C x : e) { ...}
                n = translateExtendedFor((ExtendedFor) s, labels);
            }
        }
        return n;
    }

    protected boolean isExtendedFor(Node n) {
        return n instanceof Loop && n instanceof ExtendedFor;
    }

    private Node translateExtendedFor(ExtendedFor n, List<String> labels)
            throws SemanticException {
        ExtendedFor ext = n;
        LocalDecl decl = ext.decl();
        Expr expr = ext.expr();

        if (expr.type().isArray()) {
            return translateExtForArray(n, labels);
        }

        Position pos = Position.compilerGenerated();
        Type iterType = ts.typeForName("java.util.Iterator");
        Type iteratedType = decl.type().type();
        // translate "L1,...,Ln: for (C x: e) b" to 
        // "{ Iterator iter = e.iterator(); L1,...,Ln: while (iter.hasNext();)  { C x = (C)iter.next(); b }"

        // Create the iter declaration "Iterator iter = e.iterator()"
        String iterName = freshName("iter");
        LocalDecl iterDecl;
        LocalInstance iterLI =
                ts.localInstance(pos, Flags.NONE, iterType, iterName);
        {
            Id id = nodeFactory().Id(pos, "iterator");
            Call iterator = nodeFactory().Call(pos, expr, id);
            iterator = (Call) iterator.type(iterType);
            iterator =
                    iterator.methodInstance(ts.findMethod(expr.type().toClass(),
                                                          "iterator",
                                                          Collections.<Type> emptyList(),
                                                          this.context()
                                                              .currentClass(),
                                                          true));

            iterDecl =
                    nodeFactory().LocalDecl(pos,
                                            Flags.NONE,
                                            nodeFactory().CanonicalTypeNode(pos,
                                                                            iterType),
                                            nodeFactory().Id(pos, iterName),
                                            iterator);
            iterDecl = iterDecl.localInstance(iterLI);
        }

        // create the loop body
        List<Stmt> loopBody = new ArrayList<>();
        {
            Id id = nodeFactory().Id(pos, "next");
            Call call =
                    nodeFactory().Call(pos,
                                       ((Local) nodeFactory().Local(pos,
                                                                    nodeFactory().Id(pos,
                                                                                     iterName))
                                                             .type(iterType)).localInstance(iterDecl.localInstance()),
                                       id);
            call = (Call) call.type(ts.Object());
            call =
                    call.methodInstance(ts.findMethod(iterType.toClass(),
                                                      "next",
                                                      Collections.<Type> emptyList(),
                                                      this.context()
                                                          .currentClass(),
                                                      true));

            Cast cast =
                    nodeFactory().Cast(pos,
                                       nodeFactory().CanonicalTypeNode(pos,
                                                                       iteratedType),
                                       call);
            cast = (Cast) cast.type(iteratedType);

            loopBody.add(decl.init(cast));
            loopBody.add(n.body());
        }

        // create the while loop
        While loop;
        {
            Id id = nodeFactory().Id(pos, "hasNext");
            Call cond =
                    nodeFactory().Call(pos,
                                       ((Local) nodeFactory().Local(pos,
                                                                    nodeFactory().Id(pos,
                                                                                     iterName))
                                                             .type(iterType)).localInstance(iterDecl.localInstance()),
                                       id);
            cond = (Call) cond.type(ts.Boolean());
            cond =
                    cond.methodInstance(ts.findMethod(iterType.toClass(),
                                                      "hasNext",
                                                      Collections.<Type> emptyList(),
                                                      this.context()
                                                          .currentClass(),
                                                      true));

            loop =
                    nodeFactory().While(pos,
                                        cond,
                                        nodeFactory().Block(pos, loopBody));
        }

        return nodeFactory().Block(pos, iterDecl, labelStmt(loop, labels));
    }

    protected String freshName(String desc) {
        int count = varCount.removeLast();
        varCount.addLast(count + 1);
        if (count == 0) {
            return "extfor$" + desc;
        }
        return "extfor$" + desc + "$" + count;
    }

    protected Node translateExtForArray(ExtendedFor n, List<String> labels)
            throws SemanticException {
        ExtendedFor ext = n;
        LocalDecl decl = ext.decl();
        Expr expr = ext.expr();

        Position pos = Position.compilerGenerated();
        Type iteratedType = decl.type().type();
        // translate "L1,...,Ln: for (C x: e) b" to 
        // "{ C[] arr = e; int iter = 0;  L1,...,Ln: while (iter < arr.length)  { C x = arr[iter]; iter = iter + 1; b; }"
        List<Stmt> stmts = new ArrayList<>();

        // add the declaration of arr: "C[] arr = e"
        String arrID = freshName("arr");
        LocalInstance arrLI =
                ts.localInstance(pos, Flags.NONE, expr.type(), arrID);
        {
            LocalDecl ld =
                    nodeFactory().LocalDecl(pos,
                                            Flags.NONE,
                                            nodeFactory().CanonicalTypeNode(pos,
                                                                            arrLI.type()),
                                            nodeFactory().Id(pos, arrID));
            ld = ld.localInstance(arrLI);
            ld = ld.init(expr);
            stmts.add(ld);
        }

        // add the declaration of iterator: "int iter = 0"
        String iterID = freshName("iter");
        LocalInstance iterLI =
                ts.localInstance(pos, Flags.NONE, ts.Int(), iterID);
        {
            LocalDecl ld =
                    nodeFactory().LocalDecl(pos,
                                            Flags.NONE,
                                            nodeFactory().CanonicalTypeNode(pos,
                                                                            iterLI.type()),
                                            nodeFactory().Id(pos, iterID));
            ld = ld.localInstance(iterLI);
            ld =
                    ld.init(nodeFactory().IntLit(pos, IntLit.INT, 0)
                                         .type(ts.Int()));
            stmts.add(ld);
        }

        // build the conditional "iter < arr.length"
        Expr cond;
        {
            Id id = nodeFactory().Id(pos, "length");
            Field field =
                    (Field) nodeFactory().Field(pos, makeLocal(pos, arrLI), id)
                                         .type(ts.Int());
            field =
                    field.fieldInstance(ts.findField(arrLI.type().toReference(),
                                                     "length",
                                                     context().currentClass(),
                                                     true));

            cond =
                    nodeFactory().Binary(pos,
                                         makeLocal(pos, iterLI),
                                         Binary.LT,
                                         field).type(ts.Boolean());
        }

        // build the initlizer for the local decl: arr[iter]
        Expr init;
        {
            init =
                    nodeFactory().ArrayAccess(pos,
                                              makeLocal(pos, arrLI),
                                              makeLocal(pos, iterLI));
            init = init.type(iteratedType);
        }

        // build the increment for iter (iter = iter + 1;)
        Stmt inc;
        {
            Expr incExpr =
                    nodeFactory().Binary(pos,
                                         makeLocal(pos, iterLI),
                                         Binary.ADD,
                                         nodeFactory().IntLit(pos,
                                                              IntLit.INT,
                                                              1).type(ts.Int()))
                                 .type(ts.Int());
            Assign incStore =
                    (Assign) nodeFactory().Assign(pos,
                                                  makeLocal(pos, iterLI),
                                                  Assign.ASSIGN,
                                                  incExpr).type(ts.Int());
            inc = nodeFactory().Eval(pos, incStore);
        }

        // build the while loop
        {
            // Create a new loop body from the old body followed by the increment
            Block loopBody =
                    nodeFactory().Block(pos, decl.init(init), inc, n.body());
            While loop = nodeFactory().While(pos, cond, loopBody);
            stmts.add(labelStmt(loop, labels));
        }
        return nodeFactory().Block(pos, stmts);
    }

    protected Expr makeLocal(Position pos, LocalInstance li) {
        Local l =
                (Local) nodeFactory().Local(pos,
                                            nodeFactory().Id(pos, li.name()))
                                     .localInstance(li)
                                     .type(li.type());

        return l;
    }

    /**
     * Label stmt s with labels in the list.
     * If the list contains L1, .., Ln, then the stmt returned will be "L1:L2:...Ln: s"
     * @param s
     * @param labels
     * @return
     */
    private Stmt labelStmt(Stmt s, List<String> labels) {
        for (int i = labels.size() - 1; i >= 0; i--) {
            Id id =
                    nodeFactory().Id(Position.compilerGenerated(),
                                     labels.get(i));
            s = nodeFactory().Labeled(Position.compilerGenerated(), id, s);
        }
        return s;
    }

}
