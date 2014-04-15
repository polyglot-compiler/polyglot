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
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5FormalExt;
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
        else if (n instanceof EnumConstantDecl) {
            return rewriteEnumConstantDecl((EnumConstantDecl) n);
        }
        else if (n instanceof ProcedureDecl) {
            return rewriteProcedureDecl((ProcedureDecl) n);
        }
        else {
            return n;
        }
    }

    private Node rewriteProcedureDecl(ProcedureDecl n) {
        List<Formal> formals = new ArrayList<>(n.formals());
        if (formals.size() > 0) {
            int varArgIndex = formals.size() - 1;
            Formal varArgFormal = formals.get(varArgIndex);
            JL5FormalExt varArgFormalExt =
                    (JL5FormalExt) JL5Ext.ext(varArgFormal);

            if (varArgFormalExt.isVarArg()) {
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

    private Node rewriteEnumConstantDecl(EnumConstantDecl n) {
        JL5ProcedureInstance pi =
                (JL5ProcedureInstance) n.constructorInstance();
        return n.args(rewriteProcedureArgs(pi, n.args(), n.position()));

    }

    private Node rewriteCall(ProcedureCall n) {
        JL5ProcedureInstance pi = (JL5ProcedureInstance) n.procedureInstance();
        return n.arguments(rewriteProcedureArgs(pi, n.arguments(), n.position()));
    }

    private List<Expr> rewriteProcedureArgs(JL5ProcedureInstance pi,
            List<Expr> args, Position pos) {
        if (pi.isVariableArity()) {
            int numArgs = args.size();
            int numStandardFormals = pi.formalTypes().size() - 1;
            ArrayType varArgArrayType =
                    (ArrayType) pi.formalTypes().get(numStandardFormals);

            if (numStandardFormals == numArgs - 1) {
                Type lastArgType = args.get(numStandardFormals).type();
                if (lastArgType.isImplicitCastValid(varArgArrayType)) {
                    return args;
                }
            }

            List<Expr> standardArgs =
                    new ArrayList<>(args.subList(0, numStandardFormals));

            ArrayInit initValues =
                    nf.ArrayInit(pos, args.subList(numStandardFormals, numArgs));
            initValues = (ArrayInit) initValues.type(varArgArrayType);
            NewArray varArgArray =
                    nf.NewArray(pos,
                                nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                     varArgArrayType.base()),
                                1,
                                initValues);
            varArgArray = (NewArray) varArgArray.type(varArgArrayType);
            standardArgs.add(varArgArray);
            return standardArgs;
        }
        return args;
    }
}
