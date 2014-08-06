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
package polyglot.ext.jl5.types;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.ReferenceType_c;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class TypeVariable_c extends ReferenceType_c implements TypeVariable {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String name;

    /*
     * A type variable is either declared as a parameter for a procedure, for a class,
     * is a synthetic type variable (e.g., a closure), or we do not yet know what it is.
     * Field declaredIn depicts what kind of type variable this is. It is null if
     * we do not yet know what kind of variable it is. The declaredIn field may be set 
     * at most once. If the type variable is declared as a parameter for a procedure
     * or a class, then fields declaringClass and declaringProcedure must
     * be set appropriately. For TypeVariables with declaredIn either null or equal to
     * TVarDecl.SYNTHETIC_TYPE_VARIABLE, we use field syntheticUniqueId to distinguish
     * them. For TypeVariables that belong to classes or procedures, syntheticUniqueId
     * will be null.
     */
    protected TVarDecl declaredIn;

    protected ClassType declaringClass;
    protected JL5ProcedureInstance declaringProcedure;

    /**
     * Unique Id for type variables where declaredIn is either null or equal to
     * TVarDecl.SYNTHETIC_TYPE_VARIABLE. Only valid for the duration of a compilation.
     */
    protected transient Long syntheticUniqueId;
    private static long idCount = 1;

    /**
     * The upper bound of this type variable. Should always be non-null. 
     */
    protected ReferenceType upperBound;

    /**
     * It is possible for type variables to have lower bounds. See JLS 3rd ed 4.10.2 and 5.1.10.
     * This field may be null.
     */
    protected ReferenceType lowerBound = null;

    public TypeVariable_c(TypeSystem ts, Position pos, String id,
            ReferenceType upperBound) {
        super(ts, pos);
        this.name = id;
        if (this.name == null) {
            throw new InternalCompilerError("TypeVariables must be given a name.");
        }
        if (upperBound == null) {
            upperBound = ts.Object();
        }
        this.upperBound = upperBound;
        this.syntheticUniqueId = Long.valueOf(idCount++);
    }

    @Override
    public void setDeclaringProcedure(JL5ProcedureInstance pi) {
        if (this.declaredIn == TVarDecl.PROCEDURE_TYPE_VARIABLE
                && this.declaringProcedure == pi) {
            // nothing to do, the fields already match.
            return;
        }
        if (this.declaredIn != null) {
            throw new InternalCompilerError("Can only set declaredIn once: was "
                                                    + this.declaredIn
                                                    + "&"
                                                    + this.declaringProcedure
                                                    + " now wants to be Procedure&"
                                                    + pi + this,
                                            this.position);
        }
        this.declaredIn = TVarDecl.PROCEDURE_TYPE_VARIABLE;
        this.declaringProcedure = pi;
        this.declaringClass = null;
        this.syntheticUniqueId = null;
    }

    @Override
    public void setDeclaringClass(ClassType ct) {
        if (this.declaredIn == TVarDecl.CLASS_TYPE_VARIABLE
                && declaringClass == ct) {
            // nothing to do
            return;
        }
        if (this.declaredIn != null) {
            throw new InternalCompilerError("Can only set declaredIn once",
                                            this.position);
        }
        this.declaredIn = TVarDecl.CLASS_TYPE_VARIABLE;
        this.declaringProcedure = null;
        this.declaringClass = ct;
        this.syntheticUniqueId = null;
    }

    @Override
    public void setSyntheticOrigin() {
        if (declaredIn == TVarDecl.SYNTHETIC_TYPE_VARIABLE) {
            // nothing to do
            return;
        }
        if (declaredIn != null) {
            throw new InternalCompilerError("Can only set declaredIn once",
                                            this.position);
        }
        this.declaredIn = TVarDecl.SYNTHETIC_TYPE_VARIABLE;
        this.declaringProcedure = null;
        this.declaringClass = null;

    }

    @Override
    public TVarDecl declaredIn() {
        return this.declaredIn;
    }

    @Override
    public ClassType declaringClass() {
        if (this.declaredIn == TVarDecl.CLASS_TYPE_VARIABLE)
            return this.declaringClass;
        return null;
    }

    @Override
    public JL5ProcedureInstance declaringProcedure() {
        if (this.declaredIn == TVarDecl.PROCEDURE_TYPE_VARIABLE)
            return this.declaringProcedure;
        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    public List<? extends MethodInstance> methods() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends FieldInstance> fields() {
        return Collections.emptyList();
    }

    @Override
    public FieldInstance fieldNamed(String name) {
        for (FieldInstance fi : fields()) {
            if (fi.name().equals(name)) {
                return fi;
            }
        }
        return null;
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        return Collections.emptyList();
    }

    @Override
    public ReferenceType erasureType() {
        return (ReferenceType) ((JL5TypeSystem) this.typeSystem()).erasureType(this);
    }

    @Override
    public Type superType() {
        return this.upperBound;
    }

    @Override
    public String translate(Resolver c) {
        return this.name();
    }

    @Override
    public String toString() {
        return this.name();
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        if (super.isCastValidImpl(toType)) {
            return true;
        }

        return ts.isCastValid(this.upperBound(), toType);
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (super.descendsFromImpl(ancestor)) {
            return true;
        }
        // See JLS 3rd ed 4.10.2
        return ts.isSubtype(this.upperBound, ancestor);
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        if (super.isImplicitCastValidImpl(toType)) {
            return true;
        }
        
        return ts.isImplicitCastValid(this.upperBound, toType);
    }

    @Override
    public boolean hasLowerBound() {
        return this.lowerBound != null;
    }

    @Override
    public ReferenceType lowerBound() {
        return this.lowerBound;
    }

    @Override
    public void setLowerBound(ReferenceType lowerBound) {
        this.lowerBound = lowerBound;
    }

    @Override
    public ReferenceType upperBound() {
        return upperBound;
    }

    @Override
    public void setUpperBound(ReferenceType upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public TypeVariable upperBound(ReferenceType upperBound) {
        if (this.upperBound.equals(upperBound)) {
            return this;
        }
        TypeVariable tv = (TypeVariable) this.copy();
        tv.setUpperBound(upperBound);
        return tv;
    }

    /*
     * Note that it is difficult to figure out if two type variables are the same. 
     * Object equality does not hold, since we may have two objects that represent the same type variable, that have had
     * substitution applied to their upper bounds. 
     * So we require equality on where the type variable is declared, and depending on where it was declared, on the
     * declaring class or procedure. For synthetic type variables,
     * we require equality on the syntheticUniqueId field.
     * 
     */

    @Override
    public boolean equalsImpl(TypeObject t) {
        if (this == t) return true;
        if (this.name == null) {
            return this == t;
        }
        if (t instanceof TypeVariable_c) {
            TypeVariable_c other = (TypeVariable_c) t;
            return (this.name == other.name || (this.name != null && this.name()
                                                                         .equals(other.name())))
                    && this.declaredIn == other.declaredIn
                    && (this.syntheticUniqueId == other.syntheticUniqueId || (this.syntheticUniqueId != null && this.syntheticUniqueId.equals(other.syntheticUniqueId)))
                    // we don't use .equals on declaringClass and declaringProcedure to avoid infinite loops. 
                    // there may be a better way to do this, but it's hard within the confines
                    // of the equalsImpl/typeEqualsImpl methods.
                    && (this.declaringClass == other.declaringClass)
                    && (this.declaringProcedure == other.declaringProcedure);

        }
        return false;
    }

    @Override
    public boolean typeEqualsImpl(Type t) {
        return equalsImpl(t);
    }

    @Override
    public int hashCode() {
        return (this.name == null ? 0 : this.name.hashCode())
                ^ (this.syntheticUniqueId == null
                        ? 0 : this.syntheticUniqueId.hashCode());
    }

    @SuppressWarnings("unused")
    private static final long writeObjectVersionUID = 2L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (this.declaredIn == null
                || this.declaredIn == TVarDecl.SYNTHETIC_TYPE_VARIABLE) {
            throw new InternalCompilerError("Shouldn't serialize unknown or synthetic type variables",
                                            this.position());
        }
        out.defaultWriteObject();
    }

}
