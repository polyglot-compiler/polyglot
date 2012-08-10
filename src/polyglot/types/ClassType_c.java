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
import java.util.List;

import polyglot.frontend.Job;
import polyglot.main.Options;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * A <code>ClassType</code> represents a class -- either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 */
public abstract class ClassType_c extends ReferenceType_c implements ClassType {
    /** Used for deserializing types. */
    protected ClassType_c() {
    }

    public ClassType_c(TypeSystem ts) {
        this(ts, null);
    }

    public ClassType_c(TypeSystem ts, Position pos) {
        super(ts, pos);
        this.decl = this;
    }

    protected transient Resolver memberCache;

    @Override
    public Resolver resolver() {
        if (memberCache == null) {
            memberCache =
                    new CachingResolver(ts.createClassContextResolver(this));
        }
        return memberCache;
    }

    @Override
    public ClassType_c copy() {
        ClassType_c n = (ClassType_c) super.copy();
        n.memberCache = null;
        return n;
    }

    protected ClassType decl;

    @Override
    public Declaration declaration() {
        return decl;
    }

    @Override
    public void setDeclaration(Declaration decl) {
        this.decl = (ClassType) decl;
    }

    public abstract Job job();

    /** Get the class's kind. */
    @Override
    public abstract Kind kind();

    /** Get the class's outer class, or null if a top-level class. */
    @Override
    public abstract ClassType outer();

    /** Get the short name of the class, if possible. */
    @Override
    public abstract String name();

    /** Get the container class if a member class. */
    @Override
    public ReferenceType container() {
        if (!isMember())
            throw new InternalCompilerError("Non-member class " + this
                    + " cannot have container classes.");
        if (outer() == null)
            throw new InternalCompilerError("Nested class " + this
                    + " must have an outer class.");
        return outer();
    }

    /** Get the full name of the class, if possible. */
    @Override
    public String fullName() {
        if (isAnonymous()) {
            return toString();
        }
        String name = name();
        if (isTopLevel() && package_() != null) {
            return package_().fullName() + "." + name;
        }
        else if (isMember() && container() instanceof Named) {
            return ((Named) container()).fullName() + "." + name;
        }
        else {
            return name;
        }
    }

    @Override
    public boolean isTopLevel() {
        return kind() == TOP_LEVEL;
    }

    @Override
    public boolean isMember() {
        return kind() == MEMBER;
    }

    @Override
    public boolean isLocal() {
        return kind() == LOCAL;
    }

    @Override
    public boolean isAnonymous() {
        return kind() == ANONYMOUS;
    }

    /**
    * @deprecated Was incorrectly defined. Use isNested for nested classes, 
    *          and isInnerClass for inner classes.
    */
    @Deprecated
    @Override
    public final boolean isInner() {
        return isNested();
    }

    @Override
    public boolean isNested() {
        // Implement this way rather than with ! isTopLevel() so that
        // extensions can add more kinds.
        return kind() == MEMBER || kind() == LOCAL || kind() == ANONYMOUS;
    }

    @Override
    public boolean isInnerClass() {
        // it's an inner class if it is not an interface, it is a nested
        // class, and it is not explicitly or implicitly static. 
        return !flags().isInterface() && isNested() && !flags().isStatic()
                && !inStaticContext();
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public ClassType toClass() {
        return this;
    }

    /** Get the class's package. */
    @Override
    public abstract Package package_();

    /** Get the class's flags. */
    @Override
    public abstract Flags flags();

    /** Get the class's constructors. */
    @Override
    public abstract List<? extends ConstructorInstance> constructors();

    /** Get the class's member classes. */
    @Override
    public abstract List<? extends ClassType> memberClasses();

    /** Get the class's methods. */
    @Override
    public abstract List<? extends MethodInstance> methods();

    /** Get the class's fields. */
    @Override
    public abstract List<? extends FieldInstance> fields();

    /** Get the class's interfaces. */
    @Override
    public abstract List<? extends ReferenceType> interfaces();

    /** Get the class's super type. */
    @Override
    public abstract Type superType();

    /** Get a list of all the class's MemberInstances. */
    @Override
    public List<? extends MemberInstance> members() {
        List<MemberInstance> l = new ArrayList<MemberInstance>();
        l.addAll(methods());
        l.addAll(fields());
        l.addAll(constructors());
        l.addAll(memberClasses());
        return l;
    }

    /** Get a field of the class by name. */
    @Override
    public FieldInstance fieldNamed(String name) {
        for (FieldInstance fi : fields()) {
            if (fi.name().equals(name)) {
                return fi;
            }
        }

        return null;
    }

    /** Get a member class of the class by name. */
    @Override
    public ClassType memberClassNamed(String name) {
        for (ClassType t : memberClasses()) {
            if (t.name().equals(name)) {
                return t;
            }
        }

        return null;
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (!ancestor.isCanonical()) {
            return false;
        }

        if (ancestor.isNull()) {
            return false;
        }

        if (ts.typeEquals(this, ancestor)) {
            return false;
        }

        if (!ancestor.isReference()) {
            return false;
        }

        if (ts.typeEquals(ancestor, ts.Object())) {
            return true;
        }

        // Check subtype relation for classes.
        if (!flags().isInterface()) {
            if (ts.typeEquals(this, ts.Object())) {
                return false;
            }

            if (superType() == null) {
                return false;
            }

            if (ts.isSubtype(superType(), ancestor)) {
                return true;
            }
        }

        // Next check interfaces.
        for (Type parentType : interfaces()) {
            if (ts.isSubtype(parentType, ancestor)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isThrowable() {
        return ts.isSubtype(this, ts.Throwable());
    }

    @Override
    public boolean isUncheckedException() {
        if (isThrowable()) {
            for (Type t : ts.uncheckedExceptions()) {
                if (ts.isSubtype(this, t)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        if (!toType.isClass()) return false;
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
        if (!toType.isCanonical()) return false;
        if (!toType.isReference()) return false;

        if (toType.isArray()) {
            // From type is not an array, but to type is.  Check if the array
            // is a subtype of the from type.  This happens when from type
            // is java.lang.Object.
            return ts.isSubtype(toType, this);
        }

        // Both types should be classes now.
        if (!toType.isClass()) return false;

        // From and to are neither primitive nor an array. They are distinct.
        boolean fromInterface = flags().isInterface();
        boolean toInterface = toType.toClass().flags().isInterface();
        boolean fromFinal = flags().isFinal();
        boolean toFinal = toType.toClass().flags().isFinal();

        // This is taken from Section 5.5 of the JLS.
        if (!fromInterface) {
            // From is not an interface.
            if (!toInterface) {
                // Nether from nor to is an interface.
                return ts.isSubtype(this, toType) || ts.isSubtype(toType, this);
            }

            if (fromFinal) {
                // From is a final class, and to is an interface
                return ts.isSubtype(this, toType);
            }

            // From is a non-final class, and to is an interface.
            return true;
        }
        else {
            // From is an interface
            if (!toInterface && !toFinal) {
                // To is a non-final class.
                return true;
            }

            if (toFinal) {
                // To is a final class.
                return ts.isSubtype(toType, this);
            }

            // To and From are both interfaces.
            return true;
        }
    }

    @Override
    public final boolean isEnclosed(ClassType maybe_outer) {
        return ts.isEnclosed(this, maybe_outer);
    }

    @Override
    public final boolean hasEnclosingInstance(ClassType encl) {
        return ts.hasEnclosingInstance(this, encl);
    }

    @Override
    public String translate(Resolver c) {
        if (isTopLevel()) {
            if (package_() == null) {
                return name();
            }

            // Use the short name if it is unique.
            if (c != null && !Options.global.fully_qualified_names) {
                try {
                    Named x = c.find(name());

                    if (ts.equals(this, x)) {
                        return name();
                    }
                }
                catch (SemanticException e) {
                }
            }

            return package_().translate(c) + "." + name();
        }
        else if (isMember()) {
            // Use only the short name if the outer class is anonymous.
            if (container().toClass().isAnonymous()) {
                return name();
            }

            // Use the short name if it is unique.
            if (c != null && !Options.global.fully_qualified_names) {
                try {
                    Named x = c.find(name());

                    if (ts.equals(this, x)) {
                        return name();
                    }
                }
                catch (SemanticException e) {
                }
            }

            return container().translate(c) + "." + name();
        }
        else if (isLocal()) {
            return name();
        }
        else {
            throw new InternalCompilerError("Cannot translate an anonymous class.");
        }
    }

    @Override
    public String toString() {
        if (isTopLevel()) {
            if (package_() != null) {
                return package_() + "." + name();
            }
            return name();
        }
        else if (isMember()) {
            return container().toString() + "." + name();
        }
        else if (isLocal()) {
            return name();
        }
        else if (isAnonymous()) {
            return "<anonymous class>";
        }
        else {
            return "<unknown class>";
        }
    }

    /** Pretty-print the name of this class to w. */
    @Override
    public void print(CodeWriter w) {
        // XXX This code duplicates the logic of toString.
        if (isTopLevel()) {
            if (package_() != null) {
                package_().print(w);
                w.write(".");
                w.allowBreak(2, 3, "", 0);
            }
            w.write(name());
        }
        else if (isMember()) {
            container().print(w);
            w.write(".");
            w.allowBreak(2, 3, "", 0);
            w.write(name());
        }
        else if (isLocal()) {
            w.write(name());
        }
        else if (isAnonymous()) {
            w.write("<anonymous class>");
        }
        else {
            w.write("<unknown class>");
        }
    }

    @Override
    public boolean isEnclosedImpl(ClassType maybe_outer) {
        if (isTopLevel())
            return false;
        else if (outer() != null)
            return outer().equals(maybe_outer)
                    || outer().isEnclosed(maybe_outer);
        else throw new InternalCompilerError("Non top-level classes "
                + "must have outer classes.");
    }

    /** 
     * Return true if an object of the class has
     * an enclosing instance of <code>encl</code>. 
     */
    @Override
    public boolean hasEnclosingInstanceImpl(ClassType encl) {
        if (this.equals(encl)) {
            // object o is the zeroth lexically enclosing instance of itself. 
            return true;
        }

        if (!isInnerClass() || inStaticContext()) {
            // this class is not an inner class, or was declared in a static
            // context; it cannot have an enclosing
            // instance of anything. 
            return false;
        }

        // see if the immediately lexically enclosing class has an 
        // appropriate enclosing instance
        return this.outer().hasEnclosingInstance(encl);
    }
}
