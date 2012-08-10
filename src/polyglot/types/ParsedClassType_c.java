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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.frontend.Job;
import polyglot.frontend.Source;
import polyglot.main.Report;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.TypeInputStream;

/**
 * ParsedClassType
 *
 * Overview: 
 * A ParsedClassType represents a information that has been parsed (but not
 * necessarily type checked) from a .java file.
 **/
public class ParsedClassType_c extends ClassType_c implements ParsedClassType {
    protected transient LazyClassInitializer init;
    protected transient Source fromSource;
    protected transient Job job;

    protected Type superType;
    protected List<ReferenceType> interfaces;
    protected List<MethodInstance> methods;
    protected List<FieldInstance> fields;
    protected List<ConstructorInstance> constructors;
    protected Package package_;
    protected Flags flags;
    protected Kind kind;
    protected String name;
    protected ClassType outer;

    protected transient List<ClassType> memberClasses;

    public LazyClassInitializer init() {
        return init;
    }

    public void setInit(LazyClassInitializer init) {
        this.init = init;
    }

    /** Was the class declared in a static context? */
    protected boolean inStaticContext = false;

    /** Wether we need to serialize this class. */
    protected boolean needSerialization = true;

    protected ParsedClassType_c() {
        super();
    }

    public ParsedClassType_c(TypeSystem ts, LazyClassInitializer init,
            Source fromSource) {
        super(ts);
        this.fromSource = fromSource;

        setInitializer(init);

        this.interfaces = new LinkedList<ReferenceType>();
        this.methods = new LinkedList<MethodInstance>();
        this.fields = new LinkedList<FieldInstance>();
        this.constructors = new LinkedList<ConstructorInstance>();
        this.memberClasses = new LinkedList<ClassType>();
    }

    @Override
    public LazyInitializer initializer() {
        return this.init;
    }

    @Override
    public void setInitializer(LazyInitializer init) {
        this.init = (LazyClassInitializer) init;
        ((LazyClassInitializer) init).setClass(this);
    }

    @Override
    public Source fromSource() {
        return fromSource;
    }

    @Override
    public Job job() {
        return job;
    }

    @Override
    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public void inStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    @Override
    public boolean inStaticContext() {
        return inStaticContext;
    }

    @Override
    public ClassType outer() {
        if (isTopLevel()) return null;
        if (outer == null)
            throw new InternalCompilerError("Nested classes must have outer classes.");

        return outer;
    }

    @Override
    public String name() {
//        if (isAnonymous())
//            throw new InternalCompilerError("Anonymous classes cannot have names.");
//
//        if (name == null)
//            throw new InternalCompilerError("Non-anonymous classes must have names.");
        return name;
    }

    /** Get the class's super type. */
    @Override
    public Type superType() {
        init.initSuperclass();
        return this.superType;
    }

    /** Get the class's package. */
    @Override
    public Package package_() {
        return package_;
    }

    /** Get the class's flags. */
    @Override
    public Flags flags() {
        if (isAnonymous()) return Flags.NONE;
        return flags;
    }

    @Override
    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    @Override
    public void flags(Flags flags) {
        this.flags = flags;
    }

    @Override
    public void kind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public void outer(ClassType outer) {
        if (isTopLevel())
            throw new InternalCompilerError("Top-level classes cannot have outer classes.");
        this.outer = outer;
    }

    @Override
    public void setContainer(ReferenceType container) {
        if (container instanceof ClassType && isMember()) {
            outer((ClassType) container);
        }
        else {
            throw new InternalCompilerError("Only member classes can have containers.");
        }
    }

    @Override
    public void name(String name) {
        if (isAnonymous())
            throw new InternalCompilerError("Anonymous classes cannot have names.");
        this.name = name;
    }

    @Override
    public void position(Position pos) {
        this.position = pos;
    }

    @Override
    public void package_(Package p) {
        this.package_ = p;
    }

    @Override
    public void superType(Type t) {
        this.superType = t;
    }

    @Override
    public void addInterface(ReferenceType t) {
        interfaces.add(t);
    }

    @Override
    public void addMethod(MethodInstance mi) {
        methods.add(mi);
    }

    @Override
    public void addConstructor(ConstructorInstance ci) {
        constructors.add(ci);
    }

    @Override
    public void addField(FieldInstance fi) {
        fields.add(fi);
    }

    @Override
    public void addMemberClass(ClassType t) {
        memberClasses.add(t);
    }

    @Override
    public void setInterfaces(List<? extends ReferenceType> l) {
        this.interfaces = new ArrayList<ReferenceType>(l);
    }

    @Override
    public void setMethods(List<? extends MethodInstance> l) {
        this.methods = new ArrayList<MethodInstance>(l);
    }

    @Override
    public void setFields(List<? extends FieldInstance> l) {
        this.fields = new ArrayList<FieldInstance>(l);
    }

    @Override
    public void setConstructors(List<? extends ConstructorInstance> l) {
        this.constructors = new ArrayList<ConstructorInstance>(l);
    }

    @Override
    public void setMemberClasses(List<? extends ClassType> l) {
        this.memberClasses = new ArrayList<ClassType>(l);
    }

    @Override
    public boolean defaultConstructorNeeded() {
        init.initConstructors();
        if (flags().isInterface()) {
            return false;
        }
        return this.constructors.isEmpty();
    }

    /** Return an immutable list of constructors */
    @Override
    public List<? extends ConstructorInstance> constructors() {
        init.initConstructors();
        init.canonicalConstructors();
        return Collections.unmodifiableList(constructors);
    }

    /** Return an immutable list of member classes */
    @Override
    public List<? extends ClassType> memberClasses() {
        init.initMemberClasses();
        return Collections.unmodifiableList(memberClasses);
    }

    /** Return an immutable list of methods. */
    @Override
    public List<? extends MethodInstance> methods() {
        init.initMethods();
        init.canonicalMethods();
        return Collections.unmodifiableList(methods);
    }

    /** Return a list of all methods with the given name. */
    @Override
    public List<? extends MethodInstance> methodsNamed(String name) {
        // Override to NOT call methods(). Do not check that all
        // methods are canonical, just that the particular method
        // returned is canonical.
        init.initMethods();

        List<MethodInstance> l = new LinkedList<MethodInstance>();

        for (MethodInstance mi : methods) {
            if (mi.name().equals(name)) {
                if (!mi.isCanonical()) {
                    // Force an exception to get thrown.
                    init.canonicalMethods();
                }
                l.add(mi);
            }
        }

        return l;
    }

    /** Return an immutable list of fields */
    @Override
    public List<? extends FieldInstance> fields() {
        init.initFields();
        init.canonicalFields();
        return Collections.unmodifiableList(fields);
    }

    /** Get a field of the class by name. */
    @Override
    public FieldInstance fieldNamed(String name) {
        // Override to NOT call fields(). Do not check that all
        // fields are canonical, just that the particular field
        // returned is canonical.  This avoids an infinite loop
        // during disambiguation of path-dependent types like
        // in Jx or Jif.
        init.initFields();

        for (FieldInstance fi : fields) {
            if (fi.name().equals(name)) {
                if (!fi.isCanonical()) {
                    // Force an exception to get thrown.
                    init.canonicalFields();
                }
                return fi;
            }
        }

        return null;
    }

    /** Return an immutable list of interfaces */
    @Override
    public List<? extends ReferenceType> interfaces() {
        init.initInterfaces();
        return Collections.unmodifiableList(interfaces);
    }

    protected boolean membersAdded;
    protected boolean supertypesResolved;
    protected boolean signaturesResolved;

    /**
     * @return Returns the membersAdded.
     */
    @Override
    public boolean membersAdded() {
        return membersAdded;
    }

    /**
     * @param membersAdded The membersAdded to set.
     */
    @Override
    public void setMembersAdded(boolean membersAdded) {
        this.membersAdded = membersAdded;
    }

    /**
     * @param signaturesDisambiguated The signaturesDisambiguated to set.
     */
    @Override
    public void setSignaturesResolved(boolean signaturesDisambiguated) {
        this.signaturesResolved = signaturesDisambiguated;
    }

    /**
     * @return Returns the supertypesResolved.
     */
    @Override
    public boolean supertypesResolved() {
        return supertypesResolved;
    }

    /**
     * @param supertypesResolved The supertypesResolved to set.
     */
    @Override
    public void setSupertypesResolved(boolean supertypesResolved) {
        this.supertypesResolved = supertypesResolved;
    }

    @Override
    public int numSignaturesUnresolved() {
        if (signaturesResolved) {
            return 0;
        }

        if (!membersAdded()) {
            return Integer.MAX_VALUE;
        }

        // Create a new list of members.  Don't use members() since
        // it ensures that signatures be resolved and this method
        // is just suppossed to check if they are resolved.
        List<MemberInstance> l = new ArrayList<MemberInstance>();
        l.addAll(methods);
        l.addAll(fields);
        l.addAll(constructors);
        l.addAll(memberClasses);

        int count = 0;

        for (MemberInstance mi : l) {
            if (!mi.isCanonical()) {
                count++;
            }
        }

        if (count == 0) {
            signaturesResolved = true;
        }

        return count;
    }

    @Override
    public boolean signaturesResolved() {
        if (!signaturesResolved) {
            if (!membersAdded()) {
                return false;
            }

            // Create a new list of members.  Don't use members() since
            // it ensures that signatures be resolved and this method
            // is just suppossed to check if they are resolved.
            List<MemberInstance> l = new ArrayList<MemberInstance>();
            l.addAll(methods);
            l.addAll(fields);
            l.addAll(constructors);
            l.addAll(memberClasses);

            int count = 0;

            for (MemberInstance mi : l) {
                if (!mi.isCanonical()) {
                    if (Report.should_report("ambcheck", 2))
                        Report.report(2, mi + " is ambiguous");
                    count++;
                }
            }

            if (count == 0) {
                signaturesResolved = true;
            }
        }

        return signaturesResolved;
    }

    @Override
    public String toString() {
        if (kind() == null) {
            return "<unknown class " + name + ">";
        }
        if (isAnonymous()) {
            if (interfaces != null && !interfaces.isEmpty()) {
                return "<anonymous subtype of " + interfaces.get(0) + ">";
            }
            if (superType != null) {
                return "<anonymous subclass of " + superType + ">";
            }
        }
        return super.toString();
    }

    /**
     * When serializing, write out the place holder as well as the object itself.
     * This should be done in TypeOutputStream, not here, but I couldn't get it working.
     * --Nate
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        Object o = ts.placeHolder(this);
        if (o instanceof PlaceHolder && o != this) {
            out.writeBoolean(true);
            out.writeObject(o);
        }
        else {
            out.writeBoolean(false);
        }
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        if (in instanceof TypeInputStream) {
            TypeInputStream tin = (TypeInputStream) in;

            boolean b = tin.readBoolean();

            if (b) {
                tin.enableReplace(false);
                PlaceHolder p = (PlaceHolder) tin.readObject();
                tin.installInPlaceHolderCache(p, this);
                tin.enableReplace(true);
            }

            fromSource = null;
            job = null;

            init = tin.getTypeSystem().deserializedClassInitializer();
            init.setClass(this);

            membersAdded = true;
            supertypesResolved = true;
            signaturesResolved = true;
            memberClasses = new ArrayList<ClassType>();
        }

        in.defaultReadObject();
    }

    @Override
    public void needSerialization(boolean b) {
        needSerialization = b;
    }

    @Override
    public boolean needSerialization() {
        return needSerialization;
    }
}
