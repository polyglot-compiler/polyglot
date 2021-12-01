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
package polyglot.ext.jl8.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import polyglot.ext.jl7.types.JL7TypeSystem_c;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.NoMemberException;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.UnknownType;

public class JL8TypeSystem_c extends JL7TypeSystem_c implements JL8TypeSystem {
    @Override
    public boolean isImplicitCastValid(Type fromType, Type toType) {
        if (fromType instanceof UnknownType) return true;
        if (fromType instanceof FunctionType && toType instanceof ReferenceType) {
            List<MethodInstance> methods = nonObjectPublicAbstractMethods((ReferenceType) toType);
            if (methods.size() != 1) return false;
            MethodInstance method = methods.get(0);
            FunctionType functionType = (FunctionType) fromType;
            List<? extends Type> methodFormalTypes = method.formalTypes();
            List<? extends Type> functionFormalTypes = functionType.formalTypes();
            if (methodFormalTypes.size() != functionFormalTypes.size()) return false;
            for (int i = 0; i < methodFormalTypes.size(); i++) {
                if (!isImplicitCastValid(functionFormalTypes.get(i), methodFormalTypes.get(i))) {
                    return false;
                }
            }
            Type functionReturnType = functionType.returnType();
            if (functionReturnType == null) functionReturnType = unknownType;
            return isImplicitCastValid(functionReturnType, method.returnType());
        }
        return super.isImplicitCastValid(fromType, toType);
    }

    @Override
    public FunctionType functionType(List<? extends Type> formalTypes, Type returnType) {
        return new FunctionType_c(this, formalTypes, returnType);
    }

    @Override
    public List<MethodInstance> nonObjectPublicAbstractMethods(ReferenceType referenceType) {
        List<MethodInstance> objectPublicMethods = this.objectPublicMethods();
        List<MethodInstance> nonObjectPublicAbstractMethods = new ArrayList<>();
        for (MethodInstance m : referenceType.methods()) {
            Flags flags = m.flags();
            if (flags.isPublic() && flags.isAbstract()) {
                boolean isObjectMethod = false;
                for (MethodInstance objectMethod : objectPublicMethods) {
                    if (m.isSameMethod(objectMethod)) {
                        isObjectMethod = true;
                        break;
                    }
                }
                if (!isObjectMethod) nonObjectPublicAbstractMethods.add(m);
            }
        }
        return nonObjectPublicAbstractMethods;
    }

    private List<MethodInstance> objectPublicMethods() {
        List<MethodInstance> objectMethods = new ArrayList<>();
        for (MethodInstance i : Object().methods()) {
            if (i.flags().isPublic()) {
                objectMethods.add(i);
            }
        }
        return objectMethods;
    }

    @Override
    public MethodInstance findMethodForMethodReference(
            ReferenceType container,
            java.lang.String name,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs,
            ClassType currClass,
            Type expectedReturnType,
            boolean fromClient)
            throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List<MethodInstance> acceptable = new ArrayList<>();
        try {
            acceptable.addAll(
                    findAcceptableMethods(
                            container,
                            name,
                            argTypes,
                            typeArgs,
                            currClass,
                            expectedReturnType,
                            fromClient));
        } catch (NoMemberException e) {
            // No nothing.
        }

        if (argTypes.size() > 0 && equals(container, argTypes.get(0))) {
            List<? extends Type> argTypesWithoutReceiver = argTypes.subList(1, argTypes.size());
            try {
                acceptable.addAll(
                        findAcceptableMethods(
                                container,
                                name,
                                argTypesWithoutReceiver,
                                typeArgs,
                                currClass,
                                expectedReturnType,
                                fromClient));
            } catch (NoMemberException e) {
                // No nothing.
            }
        }

        if (acceptable.size() == 0) {
            throw new NoMemberException(
                    NoMemberException.METHOD,
                    "No valid method call found for "
                            + name
                            + "("
                            + listToString(argTypes)
                            + ")"
                            + " in "
                            + container
                            + ".");
        }

        Collection<? extends MethodInstance> maximal = findMostSpecificProcedures(acceptable);

        if (maximal.size() > 1) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<? extends MethodInstance> i = maximal.iterator(); i.hasNext(); ) {
                MethodInstance ma = i.next();
                sb.append(ma.returnType());
                sb.append(" ");
                sb.append(ma.container());
                sb.append(".");
                sb.append(ma.signature());
                if (i.hasNext()) {
                    if (maximal.size() == 2) {
                        sb.append(" and ");
                    } else {
                        sb.append(", ");
                    }
                }
            }

            throw new SemanticException(
                    "Reference to "
                            + name
                            + " is ambiguous, multiple methods match: "
                            + sb.toString());
        }

        MethodInstance mi = maximal.iterator().next();
        return mi;
    }
}
