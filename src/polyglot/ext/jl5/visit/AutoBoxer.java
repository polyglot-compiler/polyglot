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

import java.util.Collections;

import polyglot.ast.Call;
import polyglot.ast.Cast;
import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
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

            if (((JL5Options) ts.extensionInfo().getOptions()).morePermissiveCasts) {
                // Optional support for widening conversion after unboxing for compatibility
                // with "javac -source 1.5"
                if (ts.isPrimitiveWrapper(fromType)) {
                    return fromWrapToPrim(fromType,
                                          ts.primitiveTypeOfWrapper(fromType),
                                          e);
                }
            }
            return fromWrapToPrim(fromType, toType.toPrimitive(), e);
        }
        else if (!toType.isPrimitive() && fromType.isPrimitive()
                && !fromType.isVoid() && !ts.String().equals(toType)) {
            // going from a primitive value to a wrapper type.
            // translate e to XXX.valueOf(e), where XXX is the java.lang.Integer, java.lang.Double, etc.

            if (((JL5Options) ts.extensionInfo().getOptions()).morePermissiveCasts) {
                // Optional support for allowing a boxing conversion when using a literal
                // in an initializer for compatibility with with "javac -source 1.5"
                if (ts.isPrimitiveWrapper(toType)) {
                    return fromPrimToWrapWithWidening(fromType.toPrimitive(),
                                                      (ReferenceType) toType,
                                                      e);
                }
            }
            return fromPrimToWrap(fromType.toPrimitive(), toType, e);
        }

        return super.ascribe(e, toType);
    }

    private Expr fromPrimToWrapWithWidening(PrimitiveType fromType,
            ReferenceType toType, Expr e) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;

        PrimitiveType toTypePrim = ts.primitiveTypeOfWrapper(toType);

        TypeNode toTypeNode = nf.CanonicalTypeNode(e.position(), toTypePrim);
        Cast cast = nf.Cast(e.position(), toTypeNode, e);

        String methodName = "valueOf";
        TypeNode tn = nf.CanonicalTypeNode(e.position(), toType);
        Id id = nodeFactory().Id(e.position(), methodName);
        Call call = nf.Call(e.position(), tn, id, cast);
        call = (Call) call.type(toType);
        call =
                call.methodInstance(ts.findMethod(toType,
                                                  methodName,
                                                  CollectionUtil.list((Type) toTypePrim),
                                                  this.context().currentClass(),
                                                  true));
        return call;
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
                                                  this.context().currentClass(),
                                                  true));
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
                                                  this.context().currentClass(),
                                                  true));
        return call;
    }

}
