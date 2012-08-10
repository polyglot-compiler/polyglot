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
    private LinkedList<Integer> varCount = new LinkedList<Integer>();

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
        if (n instanceof ExtendedFor && !(parent instanceof Labeled)) {
            n =
                    translateExtendedFor((ExtendedFor) n,
                                         Collections.<String> emptyList());
        }
        if (n instanceof CodeDecl) {
            varCount.removeLast();
        }
        if (n instanceof Labeled && !(parent instanceof Labeled)) {
            Node s = n;
            List<String> labels = new ArrayList<String>();
            while (s instanceof Labeled) {
                Labeled lbled = (Labeled) s;
                labels.add(lbled.label());
                s = lbled.statement();
            }
            if (s instanceof ExtendedFor) {
                // we have a situation L1, ..., Ln: for (C x : e) { ...}
                n = translateExtendedFor((ExtendedFor) s, labels);
            }
        }
        return n;
    }

    private Node translateExtendedFor(ExtendedFor n, List<String> labels)
            throws SemanticException {

        if (n.expr().type().isArray()) {
            return translateExtForArray(n, labels);
        }

        Position pos = Position.compilerGenerated();
        Type iterType = ts.typeForName("java.util.Iterator");
        Type iteratedType = n.decl().type().type();
        // translate "L1,...,Ln: for (C x: e) b" to 
        // "{ Iterator iter = e.iterator(); L1,...,Ln: while (iter.hasNext();)  { C x = (C)iter.next(); b }"

        // Create the iter declaration "Iterator iter = e.iterator()"
        Id iterName = freshName("iter");
        LocalDecl iterDecl;
        LocalInstance iterLI =
                ts.localInstance(pos, Flags.NONE, iterType, iterName.id());
        {
            Id id = nodeFactory().Id(pos, "iterator");
            Call iterator = nodeFactory().Call(pos, n.expr(), id);
            iterator = (Call) iterator.type(iterType);
            iterator =
                    iterator.methodInstance(ts.findMethod(n.expr()
                                                           .type()
                                                           .toClass(),
                                                          "iterator",
                                                          Collections.<Type> emptyList(),
                                                          this.context()
                                                              .currentClass()));

            iterDecl =
                    nodeFactory().LocalDecl(pos,
                                            Flags.NONE,
                                            nodeFactory().CanonicalTypeNode(pos,
                                                                            iterType),
                                            iterName,
                                            iterator);
            iterDecl = iterDecl.localInstance(iterLI);
        }

        // create the loop body
        List<Stmt> loopBody = new ArrayList<Stmt>();
        {
            Id id = nodeFactory().Id(pos, "next");
            Call call =
                    nodeFactory().Call(pos,
                                       ((Local) nodeFactory().Local(pos,
                                                                    iterName)
                                                             .type(iterType)).localInstance(iterDecl.localInstance()),
                                       id);
            call = (Call) call.type(ts.Object());
            call =
                    call.methodInstance(ts.findMethod(iterType.toClass(),
                                                      "next",
                                                      Collections.<Type> emptyList(),
                                                      this.context()
                                                          .currentClass()));

            Cast cast =
                    nodeFactory().Cast(pos,
                                       nodeFactory().CanonicalTypeNode(pos,
                                                                       iteratedType),
                                       call);
            cast = (Cast) cast.type(iteratedType);

            loopBody.add(n.decl().init(cast));
            loopBody.add(n.body());
        }

        // create the while loop
        While loop;
        {
            Id id = nodeFactory().Id(pos, "hasNext");
            Call cond =
                    nodeFactory().Call(pos,
                                       ((Local) nodeFactory().Local(pos,
                                                                    iterName)
                                                             .type(iterType)).localInstance(iterDecl.localInstance()),
                                       id);
            cond = (Call) cond.type(ts.Boolean());
            cond =
                    cond.methodInstance(ts.findMethod(iterType.toClass(),
                                                      "hasNext",
                                                      Collections.<Type> emptyList(),
                                                      this.context()
                                                          .currentClass()));

            loop =
                    nodeFactory().While(pos,
                                        cond,
                                        nodeFactory().Block(pos, loopBody));
        }

        return nodeFactory().Block(pos, iterDecl, labelStmt(loop, labels));
    }

    private Id freshName(String desc) {
        int count = varCount.removeLast();
        varCount.addLast(count + 1);
        if (count == 0) {
            return nodeFactory().Id(Position.compilerGenerated(),
                                    "extfor$" + desc);
        }
        return nodeFactory().Id(Position.compilerGenerated(),
                                "extfor$" + desc + "$" + count);
    }

    private Node translateExtForArray(ExtendedFor n, List<String> labels)
            throws SemanticException {
        Position pos = Position.compilerGenerated();
        Type iteratedType = n.decl().type().type();
        // translate "L1,...,Ln: for (C x: e) b" to 
        // "{ C[] arr = e; int iter = 0;  L1,...,Ln: while (iter < arr.length)  { C x = arr[iter]; b ; iter = iter + 1; }"
        List<Stmt> stmts = new ArrayList<Stmt>();

        // add the declaration of arr: "C[] arr = e"
        Id arrID = freshName("arr");
        LocalInstance arrLI =
                ts.localInstance(pos, Flags.NONE, n.expr().type(), arrID.id());
        {
            LocalDecl ld =
                    nodeFactory().LocalDecl(pos,
                                            Flags.NONE,
                                            nodeFactory().CanonicalTypeNode(pos,
                                                                            arrLI.type()),
                                            arrID);
            ld = ld.localInstance(arrLI);
            ld = ld.init(n.expr());
            stmts.add(ld);
        }

        // add the declaration of iterator: "int iter = 0"
        Id iterID = freshName("iter");
        LocalInstance iterLI =
                ts.localInstance(pos, Flags.NONE, ts.Int(), iterID.id());
        {
            LocalDecl ld =
                    nodeFactory().LocalDecl(pos,
                                            Flags.NONE,
                                            nodeFactory().CanonicalTypeNode(pos,
                                                                            iterLI.type()),
                                            iterID);
            ld = ld.localInstance(iterLI);
            ld =
                    ld.init(nodeFactory().IntLit(pos, IntLit.INT, 0)
                                         .type(ts.Int()));
            stmts.add(ld);
        }

        // build the conditional "iter < arr.length"
        Expr cond;
        {
            Local iterLocal =
                    (Local) nodeFactory().Local(pos, iterID)
                                         .localInstance(iterLI)
                                         .type(ts.Int());
            Local arrLocal =
                    (Local) nodeFactory().Local(pos, arrID)
                                         .localInstance(arrLI)
                                         .type(arrLI.type());
            Id id = nodeFactory().Id(pos, "length");
            Field field =
                    (Field) nodeFactory().Field(pos, arrLocal, id)
                                         .type(ts.Int());
            field =
                    field.fieldInstance(ts.findField(arrLI.type().toReference(),
                                                     "length"));

            cond =
                    nodeFactory().Binary(pos, iterLocal, Binary.LT, field)
                                 .type(ts.Boolean());
        }

        // build the initlizer for the local decl: arr[iter]
        Expr init;
        {
            Local iterLocal =
                    (Local) nodeFactory().Local(pos, iterID)
                                         .localInstance(iterLI)
                                         .type(ts.Int());
            Local arrLocal =
                    (Local) nodeFactory().Local(pos, arrID)
                                         .localInstance(arrLI)
                                         .type(arrLI.type());
            init = nodeFactory().ArrayAccess(pos, arrLocal, iterLocal);
            init = init.type(iteratedType);
        }

        // build the increment for iter (iter = iter + 1;)
        Stmt inc;
        {
            Local iterLocal =
                    (Local) nodeFactory().Local(pos, iterID)
                                         .localInstance(iterLI)
                                         .type(ts.Int());
            Expr incExpr =
                    nodeFactory().Binary(pos,
                                         iterLocal.type(ts.Int()),
                                         Binary.ADD,
                                         nodeFactory().IntLit(pos,
                                                              IntLit.INT,
                                                              1).type(ts.Int()))
                                 .type(ts.Int());
            Assign incStore =
                    (Assign) nodeFactory().Assign(pos,
                                                  iterLocal,
                                                  Assign.ASSIGN,
                                                  incExpr).type(ts.Int());
            inc = nodeFactory().Eval(pos, incStore);
        }

        // build the while loop
        {
            // Create a new loop body from the old body followed by the increment
            Block loopBody =
                    nodeFactory().Block(pos, n.decl().init(init), n.body(), inc);
            While loop = nodeFactory().While(pos, cond, loopBody);
            stmts.add(labelStmt(loop, labels));
        }
        return nodeFactory().Block(pos, stmts);
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
