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
package polyglot.ext.jl7.types.inference;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.inference.InferenceSolver_c;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;

public class JL7InferenceSolver_c extends InferenceSolver_c {

    public JL7InferenceSolver_c(JL5ProcedureInstance pi,
            List<? extends Type> actuals, JL5TypeSystem ts) {
        super(pi, actuals, ts);
    }

    @Override
    protected List<TypeVariable> typeVariablesToSolve(JL5ProcedureInstance pi) {
        if (pi instanceof JL5ConstructorInstance) {
            JL5ConstructorInstance ci = (JL5ConstructorInstance) pi;
            List<TypeVariable> result = new LinkedList<>();
            result.addAll(pi.typeParams());
            ClassType ct = ci.container().toClass();
            while (ct != null) {
                if (ct instanceof JL5ParsedClassType) {
                    JL5ParsedClassType pct = (JL5ParsedClassType) ct;
                    result.addAll(pct.typeVariables());
                }
                ct = ct.outer();
            }
            return result;
        }
        return super.typeVariablesToSolve(pi);
    }

    @Override
    protected Type returnType(JL5ProcedureInstance pi) {
        if (pi instanceof ConstructorInstance) {
            ConstructorInstance ci = (ConstructorInstance) pi;
            ReferenceType container = ci.container();
            if (container.isClass()) {
                // Transform C<Fi> (JL5ParsedClassType) to
                // C<Fi> (JL5SubstClassType) by applying substitution to itself.
                // See JLS SE 7 | 15.9.3:
                // The return type of mj is θj applied to C<F1,...,Fp>.
                JL5ParsedClassType ct =
                        (JL5ParsedClassType) container.toClass();
                JL7TypeSystem ts = (JL7TypeSystem) ci.typeSystem();
                Map<TypeVariable, ReferenceType> substm = new LinkedHashMap<>();
                for (TypeVariable tv : ct.typeVariables())
                    substm.put(tv, tv);
                return ts.subst(container, substm);
            }
            return container;
        }
        return super.returnType(pi);
    }
}
