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
package polyglot.ext.jl5.ast;

import java.util.LinkedList;

import polyglot.ast.Disamb_c;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.PackageNode;
import polyglot.ast.Receiver;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5NoMemberException;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.Named;
import polyglot.types.NoClassException;
import polyglot.types.NoMemberException;
import polyglot.types.Qualifier;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.VarInstance;
import polyglot.util.InternalCompilerError;

public class JL5Disamb_c extends Disamb_c {
    @Override
    protected Node disambiguatePackagePrefix(PackageNode pn)
            throws SemanticException {
        Resolver pc = ts.packageContextResolver(pn.package_());

        Named n;

        try {
            n = pc.find(name.id());
        }
        catch (SemanticException e) {
            return null;
        }

        Qualifier q = null;

        if (n instanceof Qualifier) {
            q = (Qualifier) n;
        }
        else {
            return null;
        }

        if (q.isPackage() && packageOK()) {
            return nf.PackageNode(pos, q.toPackage());
        }
        else if (q.isType() && typeOK()) {
            return nf.CanonicalTypeNode(pos, makeRawIfNeeded(q.toType()));
        }

        return null;
    }

    @Override
    protected Node disambiguateTypeNodePrefix(TypeNode tn)
            throws SemanticException {
        Type t = tn.type();
        //System.out.println("JL5Disamb_c.dis tnp " + this);

        if (t.isReference() && exprOK()) {
            try {
                FieldInstance fi =
                        ((JL5TypeSystem) ts).findFieldOrEnum(t.toReference(),
                                                             name.id(),
                                                             c.currentClass());
                return nf.Field(pos, tn, name).fieldInstance(fi);
            }
            catch (NoMemberException e) {
                if (e.getKind() != NoMemberException.FIELD
                        && e.getKind() != JL5NoMemberException.ENUM_CONSTANT) {
                    throw e;
                }
            }
        }

        if (t.isClass() && typeOK()) {
            Resolver tc = ts.classContextResolver(t.toClass());
            Named n = tc.find(name.id());
            if (n instanceof Type) {
                // we found a type that was named appropriately. Access it 
                // through t in order to ensure that substitution is 
                // applied correctly.
                LinkedList<ClassType> typeQueue = new LinkedList<>();
                typeQueue.addLast(t.toClass());
                ClassType type = null;
                while (!typeQueue.isEmpty()) {
                    ClassType container = typeQueue.removeFirst();
                    type = container.toClass().memberClassNamed(name.id());
                    if (type != null) {
                        break;
                    }
                    // haven't found it yet...
                    if (container.superType() != null
                            && container.superType().isClass()) {
                        typeQueue.addLast(container.superType().toClass());
                    }
                    for (ReferenceType inter : container.interfaces()) {
                        if (inter.isClass()) {
                            typeQueue.addLast(inter.toClass());
                        }
                    }
                }
                if (type == null) {
                    throw new InternalCompilerError("Expected to find member class "
                            + name);
                }
                if (type.isInnerClass()) {
                    // First, see if the inner class's container has substitutions
                    if (type.outer() instanceof JL5SubstClassType) {
                        JL5SubstClassType sct =
                                (JL5SubstClassType) type.outer();
                        type = (ClassType) sct.subst().substType(type);
                    }
                }

                return nf.CanonicalTypeNode(pos, makeRawIfNeeded(type));
            }
        }
        return null;
    }

    @Override
    protected Node disambiguateExprPrefix(Expr e) throws SemanticException {
        //System.out.println("JL5Disamb_c.dis ep " + this);
        if (exprOK()) {
            return nf.Field(pos, e, name);
        }
        return null;
    }

    @Override
    protected Node disambiguateVarInstance(VarInstance vi)
            throws SemanticException {
        //System.out.println("JL5Disamb_c.dis vi " + this);
        if (vi instanceof FieldInstance) {
            FieldInstance fi = (FieldInstance) vi;
            Receiver r = makeMissingFieldTarget(fi);
            return nf.Field(pos, r, name)
                     .fieldInstance(fi)
                     .targetImplicit(true);
        }
        else if (vi instanceof LocalInstance) {
            LocalInstance li = (LocalInstance) vi;
            return nf.Local(pos, name).localInstance(li);
        }
        return null;
    }

    @Override
    protected Node disambiguateNoPrefix() throws SemanticException {
        // First try local variables and fields
        if (exprOK()) {
            VarInstance vi = c.findVariableSilent(name.id());
            Node n = disambiguateVarInstance(vi);
            if (n != null) return n;
        }

        // no variable found. try types.
        if (typeOK()) {
            try {
                Named n = c.find(name.id());
                if (n instanceof Type) {
                    Type type = (Type) n;
                    if (!type.isCanonical()) {
                        throw new InternalCompilerError("Found an ambiguous type in the context: "
                                                                + type,
                                                        pos);
                    }
                    if (type.isClass() && type.toClass().isInnerClass()) {
                        type =
                                ((JL5TypeSystem) ts).instantiateInnerClassFromContext(c,
                                                                                      type.toClass());
                    }

                    return nf.CanonicalTypeNode(pos, makeRawIfNeeded(type));
                }
            }
            catch (NoClassException e) {
                if (!name.id().equals(e.getClassName())) {
                    // hmm, something else must have gone wrong
                    // rethrow the exception
                    throw e;
                }

                // couldn't find a type named name. 
                // It must be a package--ignore the exception.
            }
        }

        // try type variables.
        TypeVariable res =
                ((JL5Context) c).findTypeVariableInThisScope(name.id());
        if (res != null) {
            //System.out.println("JL5Disamb: TypeVariable " +name.id() + " has type " + res + " " + res.getClass());
            return nf.CanonicalTypeNode(pos, res);
        }

        // Must be a package then...
        if (packageOK()) {
            return nf.PackageNode(pos, ts.packageForName(name.id()));
        }
        return null;
    }

    /**
     * This method takes a type, and, if that type 
     * is a class with type parameters, then
     * make it a raw class. Also, if the type is a 
     * class with no type variables, but is an inner
     * class of a raw class, then the class should 
     * be a raw class (i.e., the erasure of the type).
     */
    protected Type makeRawIfNeeded(Type type) {
        if (type.isClass()) {
            JL5TypeSystem ts = (JL5TypeSystem) type.typeSystem();
            if (type instanceof JL5ParsedClassType
                    && !((JL5ParsedClassType) type).typeVariables().isEmpty()) {
                // needs to be a raw type
                return ts.rawClass((JL5ParsedClassType) type, pos);
            }
            ClassType ct = type.toClass();
            if (ct.isInnerClass()) {
                ClassType t = ct;
                ClassType outer = ct.outer();
                while (t.isInnerClass() && outer != null) {
                    if (outer instanceof RawClass) {
                        // an inner class of a raw class should be a raw class.
                        return ts.erasureType(ct);
                    }
                    t = outer;
                    outer = outer.outer();
                }
            }
        }
        return type;
    }
}
