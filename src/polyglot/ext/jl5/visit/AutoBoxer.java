package polyglot.ext.jl5.visit;

import java.util.Collections;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.visit.AscriptionVisitor;

/**
 * Translate implicit boxing and unboxing to explicit code.
 */
public class AutoBoxer extends AscriptionVisitor {

    public AutoBoxer(Job job, JL5TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        Type fromType = e.type();
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (toType.isPrimitive() && !toType.isVoid() && !fromType.isPrimitive()
                && !fromType.isSubtype(ts.toRawType(ts.Enum()))) {
            // going from a wrapper object to a primitive type
            // translate e to e.XXXvalue() where XXX is int, long, double, etc.
            return fromWrapToPrim(fromType, toType.toPrimitive(), e);
        }
        else if (!toType.isPrimitive() && fromType.isPrimitive()
                && !fromType.isVoid() && !ts.String().equals(toType)) {
            // going from a primitive value to a wrapper type.
            // translate e to XXX.valueOf(e), where XXX is the java.lang.Integer, java.lang.Double, etc.
            return fromPrimToWrap(fromType.toPrimitive(), toType, e);

        }

        return super.ascribe(e, toType);
    }

    private Expr fromPrimToWrap(PrimitiveType fromType, Type toType, Expr e)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        String methodName = "valueOf";
        ClassType wrapperType = ts.wrapperClassOfPrimitive(fromType);
        TypeNode tn = nf.CanonicalTypeNode(e.position(), wrapperType);
        Id id = nodeFactory().Id(e.position(), methodName);
        Call call = nf.Call(e.position(), tn, id, e);
        call = (Call) call.type(wrapperType);
        call =
                call.methodInstance(ts.findMethod(wrapperType,
                                                  methodName,
                                                  CollectionUtil.list((Type) fromType),
                                                  this.context().currentClass()));
        return call;
    }

    private Expr fromWrapToPrim(Type fromType, PrimitiveType toType, Expr e)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        ClassType wrapperType;
        if (ts.primitiveTypeOfWrapper(fromType) != null) {
            wrapperType = fromType.toClass();
        }
        else {
            wrapperType = ts.wrapperClassOfPrimitive(toType.toPrimitive());
        }

        String methodName = toType.toPrimitive().name() + "Value";
        Id id = nodeFactory().Id(e.position(), methodName);
        Call call = nf.Call(e.position(), e, id);
        call = (Call) call.type(toType);
        call =
                call.methodInstance(ts.findMethod(wrapperType,
                                                  methodName,
                                                  Collections.<Type> emptyList(),
                                                  this.context().currentClass()));
        return call;
    }

}
