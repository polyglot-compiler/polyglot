package polyglot.ext.jl5.visit;

import polyglot.ast.ArrayAccess;
import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Cast;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.IntLit;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.Unary;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.PrimitiveType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.DeepCopy;
import polyglot.visit.HaltingVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Simplify some expressions for the later analyses. Actually, this is a kitchen-sink
 * clean up pass...
 */
public class SimplifyExpressionsForBoxing extends HaltingVisitor {
    NodeFactory nf;
    TypeSystem ts;

    public SimplifyExpressionsForBoxing(NodeFactory nf, TypeSystem ts) {
        super();
        this.nf = nf;
        this.ts = ts;
    }

    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (n instanceof Assign) {
            return simplifyAssignment((Assign) n);
        }
        if (n instanceof Unary) {
            return simplifyUnary((Unary) n, parent instanceof Eval);
        }

        return super.leave(old, n, v);
    }

    private Node simplifyUnary(Unary u, boolean discardValue) {
        JL5TypeSystem ts = (JL5TypeSystem) u.type().typeSystem();
        Type primType = ts.primitiveTypeOfWrapper(u.expr().type());
        if (primType == null) {
            return u;
        }

        // we have a unary operator where the expression has the type of a primitive wrapper
        /*
         * Change x++ to (x += 1)-1
         * Change x-- to (x -= 1)+1
         * Change ++x to (x += 1)
         * Change --x to (x -= 1)
         */
        if (Unary.PRE_DEC.equals(u.operator())
                || Unary.PRE_INC.equals(u.operator())
                || (discardValue && (Unary.POST_DEC.equals(u.operator()) || Unary.POST_INC.equals(u.operator())))) {
            if (!isTargetPure(u.expr())) {
                throw new InternalCompilerError("Don't support effectful LHS "
                        + u + " " + u.expr().getClass());
            }
            Expr left = u.expr();
            boolean add =
                    Unary.PRE_INC.equals(u.operator())
                            || Unary.POST_INC.equals(u.operator());
            Assign.Operator newOp = add ? Assign.ADD_ASSIGN : Assign.SUB_ASSIGN;
            Assign ass =
                    nf.Assign(u.position(),
                              left,
                              newOp,
                              nf.IntLit(u.position(), IntLit.INT, 1)
                                .type(ts.Int()));
            ass = (Assign) ass.type(left.type());
            return simplifyAssignment(ass);
        }
        else if (Unary.POST_DEC.equals(u.operator())
                || Unary.POST_INC.equals(u.operator())) {
            if (!isTargetPure(u.expr())) {
                throw new InternalCompilerError("Don't support effectful LHS "
                        + u + " " + u.expr().getClass());
            }
            Expr left = u.expr();
            Assign.Operator newOp =
                    Unary.POST_INC.equals(u.operator()) ? Assign.ADD_ASSIGN
                            : Assign.SUB_ASSIGN;
            Assign ass =
                    nf.Assign(u.position(),
                              left,
                              newOp,
                              nf.IntLit(u.position(), IntLit.INT, 1)
                                .type(ts.Int()));
            Expr sa = simplifyAssignment((Assign) ass.type(left.type()));

            Binary b =
                    nf.Binary(u.position(),
                              sa,
                              Unary.POST_INC.equals(u.operator()) ? Binary.SUB
                                      : Binary.ADD,
                              nf.IntLit(u.position(), IntLit.INT, 1)
                                .type(ts.Int()));
            b = (Binary) b.type(primType.isLong() ? ts.Long() : ts.Int());
            return b;

        }
        return u;
    }

    protected Expr simplifyAssignment(Assign ass) {
        JL5TypeSystem ts = (JL5TypeSystem) ass.left().type().typeSystem();
        PrimitiveType leftTypePrim =
                ts.primitiveTypeOfWrapper(ass.left().type());
        if (ass.operator().equals(Assign.ASSIGN) || leftTypePrim == null) {
            return ass;
        }
        Type rightTypePrim = ass.right().type();
        if (!rightTypePrim.isPrimitive()) {
            rightTypePrim = ts.primitiveTypeOfWrapper(ass.right().type());
        }

        // we have a op= b where the type of a is the wrapper for some primitive type
        // translate "a op= b" to "a = (A)(a op b)", where A is the type of a.

        Binary.Operator op = ass.operator().binaryOperator();
        Expr right = ass.right();
        Binary b =
                nf.Binary(ass.position(),
                          (Expr) ass.left().visit(new DeepCopy()),
                          op,
                          right);
        if (leftTypePrim.isNumeric() && rightTypePrim.isNumeric()) {
            try {
                b = (Binary) b.type(ts.promote(leftTypePrim, rightTypePrim));
            }
            catch (SemanticException e) {
                throw new InternalCompilerError(e);
            }
        }
        else {
            b = (Binary) b.type(ass.left().type());
        }

        Expr rightExpr = b;

        ass = ass.operator(Assign.ASSIGN).right(rightExpr);

        return ass;
    }

    private boolean isTargetPure(Receiver target) {
        if (target instanceof Expr) {
            return isTargetPure((Expr) target);
        }
        else {
            return true;
        }
    }

    private boolean isTargetPure(Expr target) {
        if (target instanceof Special) return true;
        if (target instanceof Local) return true;
        if (target instanceof Field)
            return isTargetPure(((Field) target).target());
        if (target instanceof Cast)
            return isTargetPure(((Cast) target).expr());
        if (target instanceof ArrayAccess) {
            ArrayAccess aa = (ArrayAccess) target;
            return isTargetPure(aa.array()) && isTargetPure(aa.index());
        }
        return false;
    }

}
