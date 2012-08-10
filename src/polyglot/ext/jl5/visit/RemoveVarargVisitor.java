package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ArrayInit;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureCall;
import polyglot.ast.ProcedureDecl;
import polyglot.ext.jl5.ast.JL5Formal;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.frontend.Job;
import polyglot.types.ArrayType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.ErrorHandlingVisitor;

public class RemoveVarargVisitor extends ErrorHandlingVisitor {

    public RemoveVarargVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node n) throws SemanticException {
        if (n instanceof ProcedureCall) {
            return rewriteCall((ProcedureCall) n);
        }
        else if (n instanceof ProcedureDecl) {
            return rewriteProcedureDecl((ProcedureDecl) n);
        }
        else {
            return n;
        }
    }

    private Node rewriteProcedureDecl(ProcedureDecl n) {
        List<Formal> formals = new ArrayList<Formal>(n.formals());
        if (formals.size() > 0) {
            int varArgIndex = formals.size() - 1;
            JL5Formal varArgFormal = (JL5Formal) formals.get(varArgIndex);
            if (varArgFormal.isVarArg()) {
                Formal newFormal =
                        nf.Formal(varArgFormal.position(),
                                  varArgFormal.flags(),
                                  varArgFormal.type(),
                                  varArgFormal.id());
                newFormal =
                        newFormal.type(varArgFormal.type())
                                 .localInstance(varArgFormal.localInstance());
                formals.remove(varArgIndex);
                formals.add(newFormal);
                if (n instanceof MethodDecl) {
                    return ((MethodDecl) n).formals(formals);
                }
                else if (n instanceof ConstructorDecl) {
                    return ((ConstructorDecl) n).formals(formals);
                }
                else {
                    throw new InternalCompilerError("Unexepected ProcedureDecl "
                            + n + " of type " + n.getClass());
                }
            }
        }
        return n;
    }

    private Node rewriteCall(ProcedureCall n) {
        JL5ProcedureInstance pi = (JL5ProcedureInstance) n.procedureInstance();
        if (pi.isVariableArity()) {
            int numArgs = n.arguments().size();
            int numStandardFormals =
                    n.procedureInstance().formalTypes().size() - 1;
            ArrayType varArgArrayType =
                    (ArrayType) n.procedureInstance()
                                 .formalTypes()
                                 .get(numStandardFormals);

            if (numStandardFormals == numArgs - 1) {
                Type lastArgType = n.arguments().get(numStandardFormals).type();
                if (lastArgType.isImplicitCastValid(varArgArrayType)) {
                    return n;
                }
            }

            List<Expr> standardArgs =
                    new ArrayList<Expr>(n.arguments()
                                         .subList(0, numStandardFormals));

            ArrayInit initValues =
                    nf.ArrayInit(Position.compilerGenerated(),
                                 n.arguments().subList(numStandardFormals,
                                                       numArgs));
            initValues = (ArrayInit) initValues.type(varArgArrayType);
            NewArray varArgArray =
                    nf.NewArray(Position.compilerGenerated(),
                                nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                     varArgArrayType.base()),
                                1,
                                initValues);
            varArgArray = (NewArray) varArgArray.type(varArgArrayType);
            standardArgs.add(varArgArray);
            n = n.arguments(standardArgs);
            return n;
        }
        else {
            return n;
        }
    }
}
