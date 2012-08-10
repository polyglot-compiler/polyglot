/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.util.CodeWriter;
import polyglot.util.Position;

/**
 * An <code>ArrayType</code> represents an array of base java types.
 */
public class ArrayType_c extends ReferenceType_c implements ArrayType {
    protected Type base;
    protected List<FieldInstance> fields;
    protected List<MethodInstance> methods;
    protected List<ClassType> interfaces;

    /** Used for deserializing types. */
    protected ArrayType_c() {
    }

    public ArrayType_c(TypeSystem ts, Position pos, Type base) {
        super(ts, pos);
        this.base = base;

        methods = null;
        fields = null;
        interfaces = null;
    }

    protected void init() {
        if (methods == null) {
            methods = new ArrayList<MethodInstance>(1);

            // Add method public Object clone()
            methods.add(createCloneMethodInstance());
        }

        if (fields == null) {
            fields = new ArrayList<FieldInstance>(1);

            // Add field public final int length
            fields.add(createLengthFieldInstance());
        }

        if (interfaces == null) {
            interfaces = new ArrayList<ClassType>(2);
            interfaces.add(ts.Cloneable());
            interfaces.add(ts.Serializable());
        }
    }

    protected FieldInstance createLengthFieldInstance() {
        FieldInstance fi =
                ts.fieldInstance(position(),
                                 this,
                                 ts.Public().Final(),
                                 ts.Int(),
                                 "length");
        fi.setNotConstant();
        return fi;
    }

    protected MethodInstance createCloneMethodInstance() {
        return ts.methodInstance(position(),
                                 this,
                                 ts.Public(),
                                 ts.Object(),
                                 "clone",
                                 Collections.<Type> emptyList(),
                                 Collections.<Type> emptyList());
    }

    /** Get the base type of the array. */
    @Override
    public Type base() {
        return base;
    }

    /** Set the base type of the array. */
    @Override
    public ArrayType base(Type base) {
        if (base == this.base) return this;
        ArrayType_c n = (ArrayType_c) copy();
        n.base = base;
        return n;
    }

    /** Get the ulitimate base type of the array. */
    @Override
    public Type ultimateBase() {
        if (base().isArray()) {
            return base().toArray().ultimateBase();
        }

        return base();
    }

    @Override
    public int dims() {
        return 1 + (base().isArray() ? base().toArray().dims() : 0);
    }

    @Override
    public String toString() {
        return base().toString() + "[]";
    }

    @Override
    public void print(CodeWriter w) {
        base().print(w);
        w.write("[]");
    }

    /** Translate the type. */
    @Override
    public String translate(Resolver c) {
        return base().translate(c) + "[]";
    }

    /** Returns true iff the type is canonical. */
    @Override
    public boolean isCanonical() {
        return base().isCanonical();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public ArrayType toArray() {
        return this;
    }

    /** Get the methods implemented by the array type. */
    @Override
    public List<? extends MethodInstance> methods() {
        init();
        return Collections.unmodifiableList(methods);
    }

    /** Get the fields of the array type. */
    @Override
    public List<? extends FieldInstance> fields() {
        init();
        return Collections.unmodifiableList(fields);
    }

    /** Get the clone() method. */
    @Override
    public MethodInstance cloneMethod() {
        return methods().get(0);
    }

    /** Get a field of the type by name. */
    @Override
    public FieldInstance fieldNamed(String name) {
        FieldInstance fi = lengthField();
        return name.equals(fi.name()) ? fi : null;
    }

    /** Get the length field. */
    @Override
    public FieldInstance lengthField() {
        return fields().get(0);
    }

    /** Get the super type of the array type. */
    @Override
    public Type superType() {
        return ts.Object();
    }

    /** Get the interfaces implemented by the array type. */
    @Override
    public List<? extends ReferenceType> interfaces() {
        init();
        return Collections.unmodifiableList(interfaces);
    }

    @Override
    public int hashCode() {
        return base().hashCode() << 1;
    }

    @Override
    public boolean equalsImpl(TypeObject t) {
        if (t instanceof ArrayType) {
            ArrayType a = (ArrayType) t;
            return ts.equals(base(), a.base());
        }
        return false;
    }

    @Override
    public boolean typeEqualsImpl(Type t) {
        if (t instanceof ArrayType) {
            ArrayType a = (ArrayType) t;
            return ts.typeEquals(base(), a.base());
        }
        return false;
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        if (toType.isArray()) {
            if (base().isPrimitive() || toType.toArray().base().isPrimitive()) {
                return ts.typeEquals(base(), toType.toArray().base());
            }
            else {
                return ts.isImplicitCastValid(base(), toType.toArray().base());
            }
        }

        // toType is not an array, but this is.  Check if the array
        // is a subtype of the toType.  This happens when toType
        // is java.lang.Object.
        return ts.isSubtype(this, toType);
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from this to toType is valid; in other
     * words, some non-null members of this are also members of toType.
     **/
    @Override
    public boolean isCastValidImpl(Type toType) {
        if (!toType.isReference()) return false;

        if (toType.isArray()) {
            Type fromBase = base();
            Type toBase = toType.toArray().base();

            if (fromBase.isPrimitive()) return ts.typeEquals(toBase, fromBase);
            if (toBase.isPrimitive()) return false;

            if (fromBase.isNull()) return false;
            if (toBase.isNull()) return false;

            // Both are reference types.
            return ts.isCastValid(fromBase, toBase);
        }

        // Ancestor is not an array, but child is.  Check if the array
        // is a subtype of the ancestor.  This happens when ancestor
        // is java.lang.Object.
        return ts.isSubtype(this, toType);
    }
}
