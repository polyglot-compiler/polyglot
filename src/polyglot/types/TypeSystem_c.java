package polyglot.ext.jl.types;

import polyglot.util.*;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.types.reflect.ClassFile;
import polyglot.frontend.Compiler;
import polyglot.frontend.Source;
import polyglot.main.Report;

import java.util.*;

/**
 * TypeSystem_c
 *
 * Overview:
 *    A TypeSystem_c is a universe of types, including all Java types.
 **/
public class TypeSystem_c implements TypeSystem
{
    Resolver systemResolver;
    TableResolver parsedResolver;
    LoadedClassResolver loadedResolver;

    public TypeSystem_c() {}

    /**
     * Initializes the type system and its internal constants (which depend on
     * the resolver).
     */
    public void initialize(LoadedClassResolver loadedResolver)
                           throws SemanticException {

        if (Report.should_report("ts", 1))
	    Report.report(1, "Initializing " + getClass().getName());

        // The parsed class resolver.  This resolver contains classes parsed
        // from source files.
        this.parsedResolver = new TableResolver();


        // The loaded class resolver.  This resolver automatically loads types
        // from class files and from source files not mentioned on the command
        // line.
        this.loadedResolver = loadedResolver;

        CompoundResolver compoundResolver =
            new CompoundResolver(parsedResolver, loadedResolver);

        // The system class resolver.  The class resolver contains a map from
        // class names to ClassTypes.  A Job looks up classes first in its
        // import table and then in the system resolver.  The system resolver
        // first tries to find the class in parsed class resolver.
        this.systemResolver = new CachingResolver(compoundResolver);

        initTypes();
    }

    protected void initTypes() throws SemanticException {
        // FIXME: don't do this when rewriting a type system!

        // Prime the resolver cache so that we don't need to check
        // later if these are loaded.

        // We cache the most commonly used ones in fields.
        OBJECT_ = (ClassType) systemResolver.findType("java.lang.Object");
        CLASS_  = (ClassType) systemResolver.findType("java.lang.Class");
        STRING_ = (ClassType) systemResolver.findType("java.lang.String");
        THROWABLE_ = (ClassType) systemResolver.findType("java.lang.Throwable");

        systemResolver.findType("java.lang.Error");
        systemResolver.findType("java.lang.Exception");
        systemResolver.findType("java.lang.RuntimeException");
        systemResolver.findType("java.lang.Cloneable");
        systemResolver.findType("java.io.Serializable");
        systemResolver.findType("java.lang.NullPointerException");
        systemResolver.findType("java.lang.ClassCastException");
        systemResolver.findType("java.lang.ArrayIndexOutOfBoundsException");
        systemResolver.findType("java.lang.ArrayStoreException");
        systemResolver.findType("java.lang.ArithmeticException");
    }

    public Resolver systemResolver() {
      return systemResolver;
    }

    public TableResolver parsedResolver() {
        return parsedResolver;
    }

    public LoadedClassResolver loadedResolver() {
        return loadedResolver;
    }

    public ImportTable importTable(String sourceName, Package pkg) {
        assert_(pkg);
        return new ImportTable(this, systemResolver, pkg, sourceName);
    }

    public ImportTable importTable(Package pkg) {
        assert_(pkg);
        return new ImportTable(this, systemResolver, pkg);
    }

    protected void assert_(Collection l) {
        for (Iterator i = l.iterator(); i.hasNext(); ) {
            TypeObject o = (TypeObject) i.next();
            assert_(o);
        }
    }

    protected void assert_(TypeObject o) {
        if (o != null && o.typeSystem() != this) {
            throw new InternalCompilerError("we are " + this + " but " + o +
                                            " is from " + o.typeSystem());
        }
    }

    public String wrapperTypeString(PrimitiveType t) {
        assert_(t);

	if (t.kind() == PrimitiveType.BOOLEAN) {
	    return "java.lang.Boolean";
	}
	if (t.kind() == PrimitiveType.CHAR) {
	    return "java.lang.Character";
	}
	if (t.kind() == PrimitiveType.BYTE) {
	    return "java.lang.Byte";
	}
	if (t.kind() == PrimitiveType.SHORT) {
	    return "java.lang.Short";
	}
	if (t.kind() == PrimitiveType.INT) {
	    return "java.lang.Integer";
	}
	if (t.kind() == PrimitiveType.LONG) {
	    return "java.lang.Long";
	}
	if (t.kind() == PrimitiveType.FLOAT) {
	    return "java.lang.Float";
	}
	if (t.kind() == PrimitiveType.DOUBLE) {
	    return "java.lang.Double";
	}
	if (t.kind() == PrimitiveType.VOID) {
	    return "java.lang.Void";
	}
	if (t.kind() == PrimitiveType.CHAR) {
	    return "java.lang.Character";
	}

	throw new InternalCompilerError("Unrecognized primitive type.");
    }

    public Context createContext() {
	return new Context_c(this);
    }

    public Resolver packageContextResolver(Resolver cr, Package p) {
        assert_(p);
	return new PackageContextResolver(this, p, cr);
    }

    public Resolver classContextResolver(ClassType type) {
        assert_(type);
	return new ClassContextResolver(this, type);
    }

    public FieldInstance fieldInstance(Position pos,
	                               ReferenceType container, Flags flags,
				       Type type, String name) {
        assert_(container);
        assert_(type);
	return new FieldInstance_c(this, pos, container, flags, type, name);
    }

    public LocalInstance localInstance(Position pos,
	                               Flags flags, Type type, String name) {
        assert_(type);
	return new LocalInstance_c(this, pos, flags, type, name);
    }

    public ConstructorInstance defaultConstructor(Position pos,
                                                  ClassType container) {
        assert_(container);
        return constructorInstance(pos, container,
                                   Flags.PUBLIC, Collections.EMPTY_LIST,
                                   Collections.EMPTY_LIST);
    }

    public ConstructorInstance constructorInstance(Position pos,
	                                           ClassType container,
						   Flags flags, List argTypes,
						   List excTypes) {
        assert_(container);
        assert_(argTypes);
        assert_(excTypes);
	return new ConstructorInstance_c(this, pos, container, flags,
	                                 argTypes, excTypes);
    }

    public InitializerInstance initializerInstance(Position pos,
	                                           ClassType container,
						   Flags flags) {
        assert_(container);
	return new InitializerInstance_c(this, pos, container, flags);
    }

    public MethodInstance methodInstance(Position pos,
	                                 ReferenceType container, Flags flags,
					 Type returnType, String name,
					 List argTypes, List excTypes) {

        assert_(container);
        assert_(returnType);
        assert_(argTypes);
        assert_(excTypes);
	return new MethodInstance_c(this, pos, container, flags,
				    returnType, name, argTypes, excTypes);
    }

    /**
     * Returns true iff child and ancestor are distinct
     * reference types, and child descends from ancestor.
     **/
    public boolean descendsFrom(Type child, Type ancestor) {
        assert_(child);
        assert_(ancestor);
        return child.descendsFromImpl(ancestor);
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from fromType to toType is valid; in other
     * words, some non-null members of fromType are also members of toType.
     **/
    public boolean isCastValid(Type fromType, Type toType) {
        assert_(fromType);
        assert_(toType);
        return fromType.isCastValidImpl(toType);
    }

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff an implicit cast from fromType to toType is valid;
     * in other words, every member of fromType is member of toType.
     *
     * Returns true iff child and ancestor are non-primitive
     * types, and a variable of type child may be legally assigned
     * to a variable of type ancestor.
     *
     */
    public boolean isImplicitCastValid(Type fromType, Type toType) {
        assert_(fromType);
        assert_(toType);
        return fromType.isImplicitCastValidImpl(toType);
    }

    /**
     * Returns true iff type1 and type2 are the same type.
     */
    public boolean isSame(Type type1, Type type2) {
        assert_(type1);
        assert_(type2);
	return type1.isSameImpl(type2);
    }

    /**
     * Returns true if <code>value</code> can be implicitly cast to Primitive
     * type <code>t</code>.
     */
    public boolean numericConversionValid(Type t, long value) {
        assert_(t);
        return t.numericConversionValidImpl(value);
    }

    ////
    // Functions for one-type checking and resolution.
    ////

    /**
     * Returns true iff <type> is a canonical (fully qualified) type.
     */
    public boolean isCanonical(Type type) {
        assert_(type);
	return type.isCanonical();
    }

    /**
     * Checks whether a method, field or inner class within "outer" with access
     * flags "flags" can be accessed from Context "context".
     */
    public boolean isAccessible(MemberInstance mi, Context context) {
        assert_(mi);

        ReferenceType target = mi.container();
	Flags flags = mi.flags();

	ClassType ctc = context.currentClass();
	ClassType cts = context.currentClassScope();

        if (flags.isPublic()) return true;

	if (isSame(target, ctc)) return true;

	if (! target.isClass()) return false;

	ClassType ctt = target.toClass();

	// If the current class is enclosed in the target class,
	// protection doesn't matter.
	if (isEnclosed(ctc, ctt)) return true;

	// Similarly, if the target is enclosed in this class.
	if (isEnclosed(ctt, ctc)) return true;

	// Check for package level scope.
	// FIXME: protected too?
	if (ctt.package_() == null && ctc.package_() == null &&
	    flags.isPackage())
	    return true;

	// kliger: this used to only allow access if the context and the
	//   target are in the same package and the flags have package-level
	//   access set.
	// However, JLS2 6.6.1 says that if the protected flag is set, then
	//   if the package is the same for target and context, then access
	//   is allowed, as well.  (in addition to the normal "subclasses get
	//   access" rule for protected members).
	// This is confusing for C++ programmers like me.
	if (ctt.package_() != null && ctt.package_().equals (ctc.package_()) &&
	    (flags.isPackage() || flags.isProtected())) {
	    return true;
	}

	// protected
	if (descendsFrom(ctc, ctt) && flags.isProtected()) return true;

	// else,
	return false;
    }

    public boolean isEnclosed(ClassType inner, ClassType outer) {
        return inner.isEnclosedImpl(outer);
    }

    public void checkCycles(ReferenceType goal) throws SemanticException {
	checkCycles(goal, goal);
    }

    protected void checkCycles(ReferenceType curr, ReferenceType goal)
	throws SemanticException {

        assert_(curr);
        assert_(goal);

	if (curr == null) {
	    return;
	}

	ReferenceType superType = null;

	if (curr.superType() != null) {
	    superType = curr.superType().toReference();
	}

	if (goal == superType) {
	    throw new SemanticException("Type " + goal +
		" is a supertype of itself.");
	}

	checkCycles(superType, goal);

	for (Iterator i = curr.interfaces().iterator(); i.hasNext(); ) {
	    Type si = (Type) i.next();

	    if (si == goal) {
		throw new SemanticException("Type " + goal +
		    " is a supertype of itself.");
	    }

	    checkCycles(si.toReference(), goal);
	}
    }

    ////
    // Various one-type predicates.
    ////

    /**
     * Returns true iff an object of type <type> may be thrown.
     **/
    public boolean isThrowable(Type type) {
        assert_(type);
        return type.isThrowable();
    }

    /**
     * Returns a true iff the type or a supertype is in the list
     * returned by uncheckedExceptions().
     */
    public boolean isUncheckedException(Type type) {
        assert_(type);
        return type.isUncheckedException();
    }

    /**
     * Returns a list of the Throwable types that need not be declared
     * in method and constructor signatures.
     */
    public Collection uncheckedExceptions() {
        List l = new ArrayList(2);
	l.add(Error());
	l.add(RuntimeException());
	return l;
    }

    public boolean isSubtype(Type t1, Type t2) {
        assert_(t1);
        assert_(t2);
        return t1.isSubtypeImpl(t2);
    }

    ////
    // Functions for type membership.
    ////

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns the fieldMatch named 'name' defined on 'type' visible in
     * context.  If no such field may be found, returns a fieldmatch
     * with an error explaining why. name and context may be null, in which case
     * they will not restrict the output.
     **/
    public FieldInstance findField(ReferenceType container, String name,
	                           Context c) throws SemanticException {

        assert_(container);

        FieldInstance fi = findField(container, name);

        if (! isAccessible(fi, c)) {
            throw new SemanticException("Cannot access " + fi + ".");
        }

        return fi;
    }

    public FieldInstance findField(ReferenceType container, String name)
                                   throws SemanticException {
        assert_(container);

        Stack s = new Stack();
        s.push(container);

        while (! s.isEmpty()) {
            Type t = (Type) s.pop();

	    if (! t.isReference()) {
	        throw new SemanticException("Cannot access a field in " +
		    " non-reference type " + t + ".");
	    }

            ReferenceType rt = t.toReference();

            FieldInstance fi = rt.fieldNamed(name);

            if (fi != null) {
                return fi;
            }

            if (rt.isClass()) {
                // Need to check interfaces for static fields.
                ClassType ct = rt.toClass();

                for (Iterator i = ct.interfaces().iterator(); i.hasNext(); ) {
                    Type it = (Type) i.next();
                    s.push(it);
                }
            }

            if (rt.superType() != null) {
                s.push(rt.superType());
            }
	}

	throw new SemanticException("Field \"" + name +
				    "\" not found in type \"" +
                                    container + "\".");
    }

    public MemberClassType findMemberClass(ClassType container, String name,
	                                   Context c) throws SemanticException {

        assert_(container);

        MemberClassType t = findMemberClass(container, name);

        if (! isAccessible(t, c)) {
            throw new SemanticException("Cannot access member \"" + t + "\".");
        }

        return t;
    }

    public MemberClassType findMemberClass(ClassType container, String name)
                                           throws SemanticException {

        assert_(container);

        Stack s = new Stack();
        s.push(container);

        while (! s.isEmpty()) {
            Type t = (Type) s.pop();

	    if (! t.isClass()) {
	        throw new SemanticException("Cannot access a field in " +
		    " non-class type " + t + ".");
	    }

            ClassType ct = t.toClass();

            MemberClassType mt = ct.memberClassNamed(name);

            if (mt != null) {
                return mt;
            }

            for (Iterator i = ct.interfaces().iterator(); i.hasNext(); ) {
                Type it = (Type) i.next();
                s.push(it);
            }

            if (ct.superType() != null) {
                s.push(ct.superType());
            }
	}

	throw new SemanticException("Member class \"" + name +
				    "\" not found in type \"" +
                                    container + "\".");
    }

    protected String listToString(List l) {
	String s = "";

	for (Iterator i = l.iterator(); i.hasNext(); ) {
	    Object o = i.next();
	    s += o.toString();

	    if (i.hasNext()) {
		s += ", ";
	    }
	}

	return s;
    }

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns the MethodInstance named 'name' defined on 'type' visible in
     * context.  If no such field may be found, returns a fieldmatch
     * with an error explaining why.  Access flags are considered.
     **/
    public MethodInstance findMethod(ReferenceType container,
	                             String name, List argTypes, Context c)
	throws SemanticException {

        assert_(container);
        assert_(argTypes);

	List acceptable = findAcceptableMethods(container, name, argTypes, c);

	if (acceptable.size() == 0) {
	    throw new SemanticException(
		"No valid method call found for " + name +
		"(" + listToString(argTypes) + ")" +
		" in " +
		container + ".");
	}

	MethodInstance mi = (MethodInstance)
	    findProcedure(acceptable, container, argTypes, c);

	if (mi == null) {
	    throw new SemanticException(
		"Reference to " + name + " is ambiguous, multiple methods match: "
		+ acceptable);
	}

	return mi;
    }

    public ConstructorInstance findConstructor(ClassType container,
					       List argTypes, Context c)
	throws SemanticException {

        assert_(container);
        assert_(argTypes);

	List acceptable = findAcceptableConstructors(container, argTypes, c);

	if (acceptable.size() == 0) {
	    throw new SemanticException("No valid constructor found for " +
			container + "(" + listToString(argTypes) + ").");
	}

	ConstructorInstance ci = (ConstructorInstance)
	    findProcedure(acceptable, container, argTypes, c);

	if (ci == null) {
	    throw new SemanticException(
		"Reference to " + container + " is ambiguous, multiple " +
		"constructors match: " + acceptable);
	}

	return ci;
    }

    protected ProcedureInstance findProcedure(List acceptable,
	                                      ReferenceType container,
					      List argTypes,
					      Context c)
	throws SemanticException {

        assert_(container);
        assert_(argTypes);

	// now, use JLS 15.11.2.2
	// First sort from most- to least-specific.
	MostSpecificComparator msc = new MostSpecificComparator();
	Collections.sort(acceptable, msc);

	Iterator i = acceptable.iterator();

	ProcedureInstance maximal = (ProcedureInstance) i.next();

	// Now check to make sure that we have a maximal most-specific method.
	while (i.hasNext()) {
	    ProcedureInstance p = (ProcedureInstance) i.next();

	    if (msc.compare(maximal, p) > 0) {
	        return null;
	    }
	}

        return maximal;
    }

    /**
     * Class to handle the comparisons; dispatches to moreSpecific method.
     */
    protected class MostSpecificComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    ProcedureInstance p1 = (ProcedureInstance) o1;
	    ProcedureInstance p2 = (ProcedureInstance) o2;

	    if (moreSpecific(p1, p2)) return -1;
	    if (moreSpecific(p2, p1)) return 1;

	    // otherwise equally maximally specific

	    // JLS2 15.12.2.2 "two or more maximally specific methods"
	    // if both abstract or not abstract, equally applicable
	    // otherwise the non-abstract is more applicable
	    if (p1.flags().isAbstract() == p2.flags().isAbstract()) return 0;
	    if (p1.flags().isAbstract()) return 1;
	    return -1;
	}
    }

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.11.2.1
     */
    protected List findAcceptableMethods(ReferenceType container, String name,
	                                 List argTypes, Context context)
	throws SemanticException {

        assert_(container);
        assert_(argTypes);

	List acceptable = new ArrayList();

	Set visitedTypes = new HashSet();

	LinkedList typeQueue = new LinkedList();
	typeQueue.addLast(container);

	while (! typeQueue.isEmpty()) {
	    Type type = (Type) typeQueue.removeFirst();

	    if (visitedTypes.contains(type)) {
		continue;
	    }

	    visitedTypes.add(type);

	    if (Report.should_report("ts", 2))
		Report.report(2, "Searching type " + type + " for method " +
                              name + " with args " + listToString(argTypes));

	    if (! type.isReference()) {
	        throw new SemanticException("Cannot call method in " +
		    " non-reference type " + type + ".");
	    }

	    for (Iterator i = type.toReference().methods().iterator();
		 i.hasNext(); ) {

		MethodInstance mi = (MethodInstance) i.next();

		if (Report.should_report("ts", 3))
		    Report.report(3, "Trying " + mi);

		if (methodCallValid(mi, name, argTypes) &&
		    isAccessible(mi, context)) {

		    if (Report.should_report("ts", 3))
			Report.report(3, "->acceptable: " + mi);

		    acceptable.add(mi);
		}
	    }

	    if (type.toReference().superType() != null) {
		typeQueue.addLast(type.toReference().superType());
	    }

	    typeQueue.addAll(type.toReference().interfaces());
	}

	return acceptable;
    }

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.11.2.1
     */
    protected List findAcceptableConstructors(ClassType container,
					      List argTypes, Context context) {
        assert_(container);
        assert_(argTypes);

	List acceptable = new ArrayList();

	if (Report.should_report("ts", 2))
	    Report.report(2, "Searching type " + container +
                          " for constructor " + " with args " +
                          listToString(argTypes));

	for (Iterator i = container.constructors().iterator(); i.hasNext(); ) {
	    ConstructorInstance ci = (ConstructorInstance) i.next();

	    if (Report.should_report("ts", 3))
		Report.report(3, "Trying " + ci);

	    if (callValid(ci, argTypes) && isAccessible(ci, context)) {
		if (Report.should_report("ts", 3))
		    Report.report(3, "->acceptable: " + ci);
		acceptable.add(ci);
	    }
	}

	return acceptable;
    }

    /**
     * Returns whether method 1 is <i>more specific</i> than method 2,
     * where <i>more specific</i> is defined as JLS 15.11.2.2
     */
    public boolean moreSpecific(ProcedureInstance p1, ProcedureInstance p2) {
        return p1.moreSpecificImpl(p2);
    }

    /**
     * Returns the supertype of type, or null if type has no supertype.
     **/
    public Type superType(ReferenceType type) {
        assert_(type);
	return type.superType();
    }

    /**
     * Returns an immutable list of all the interface types which type
     * implements.
     **/
    public List interfaces(ReferenceType type) {
        assert_(type);
	return type.interfaces();
    }

    /**
     * Requires: all type arguments are canonical.
     * Returns the least common ancestor of Type1 and Type2
     **/
    public Type leastCommonAncestor(Type type1, Type type2)
        throws SemanticException
    {
        assert_(type1);
        assert_(type2);

	if (isSame(type1, type2)) return type1;

	if (type1.isNumeric() && type2.isNumeric()) {
	    if (isImplicitCastValid(type1, type2)) {
	        return type2;
	    }

	    if (isImplicitCastValid(type2, type1)) {
	        return type1;
	    }

	    if (type1.isChar() && type2.isByte() ||
	    	type1.isByte() && type2.isChar()) {
		return Int();
	    }
	}

	if (type1.isArray() && type2.isArray()) {
	    return arrayOf(leastCommonAncestor(type1.toArray().base(),
					       type2.toArray().base()));
	}

	if (type1.isReference() && type2.isNull()) return type1;
	if (type2.isReference() && type1.isNull()) return type2;

	if (type1.isReference() && type2.isReference()) {
	    // Don't consider interfaces.
	    if (type1.isClass() && type1.toClass().flags().isInterface()) {
	        return Object();
	    }

	    if (type2.isClass() && type2.toClass().flags().isInterface()) {
	        return Object();
	    }

	    // Check against Object to ensure superType() is not null.
	    if (isSame(type1, Object())) return type1;
	    if (isSame(type2, Object())) return type2;

	    if (isSubtype(type1, type2)) return type2;
	    if (isSubtype(type2, type1)) return type1;

	    // Walk up the hierarchy
	    Type t1 = leastCommonAncestor(type1.toReference().superType(),
		                          type2);
	    Type t2 = leastCommonAncestor(type2.toReference().superType(),
					  type1);

	    if (isSame(t1, t2)) return t1;

	    return Object();
	}

	throw new SemanticException(
	   "No least common ancestor found for types \"" + type1 +
	   "\" and \"" + type2 + "\".");
    }

    ////
    // Functions for method testing.
    ////

    /**
     * Returns true iff <p1> throws fewer exceptions than <p2>.
     */
    public boolean throwsSubset(ProcedureInstance p1, ProcedureInstance p2) {
        assert_(p1);
        assert_(p2);
        return p1.throwsSubsetImpl(p2);
    }

    /** Return true if t overrides mi */
    public boolean hasArguments(ProcedureInstance pi, List argumentTypes) {
        assert_(pi);
        assert_(argumentTypes);
        return pi.hasArgumentsImpl(argumentTypes);
    }

    /** Return true if t overrides mi */
    public boolean hasMethod(ReferenceType t, MethodInstance mi) {
        assert_(t);
        assert_(mi);
        return t.hasMethodImpl(mi);
    }

    public List overrides(MethodInstance mi) {
        return mi.overridesImpl();
    }

    public List implemented(MethodInstance mi) {
	return mi.implementedImpl(mi.container());
    }
    
    public boolean canOverride(MethodInstance mi, MethodInstance mj) {
        return mi.canOverrideImpl(mj);
    }

    /**
     * Returns true iff <m1> is the same method as <m2>
     */
    public boolean isSameMethod(MethodInstance m1, MethodInstance m2) {
        assert_(m1);
        assert_(m2);
        return m1.isSameMethodImpl(m2);
    }

    public boolean methodCallValid(MethodInstance prototype,
				   String name, List argTypes) {
        assert_(prototype);
        assert_(argTypes);
	return prototype.methodCallValidImpl(name, argTypes);
    }

    public boolean callValid(ProcedureInstance prototype, List argTypes) {
        assert_(prototype);
        assert_(argTypes);
        return prototype.callValidImpl(argTypes);
    }

    ////
    // Functions which yield particular types.
    ////
    public NullType Null()         { return NULL_; }
    public PrimitiveType Void()    { return VOID_; }
    public PrimitiveType Boolean() { return BOOLEAN_; }
    public PrimitiveType Char()    { return CHAR_; }
    public PrimitiveType Byte()    { return BYTE_; }
    public PrimitiveType Short()   { return SHORT_; }
    public PrimitiveType Int()     { return INT_; }
    public PrimitiveType Long()    { return LONG_; }
    public PrimitiveType Float()   { return FLOAT_; }
    public PrimitiveType Double()  { return DOUBLE_; }

    protected ClassType load(String name) {
      try {
          return typeForName(name);
      }
      catch (SemanticException e) {
          throw new InternalCompilerError("Cannot find class \"" +
                                          name + "\"; " + e.getMessage(),
                                          e.position());
      }
    }

    public ClassType typeForName(String name) throws SemanticException {
      return (ClassType) systemResolver.findType(name);
    }

    protected ClassType OBJECT_;
    protected ClassType CLASS_;
    protected ClassType STRING_;
    protected ClassType THROWABLE_;

    public ClassType Object()  { if (OBJECT_ != null) return OBJECT_;
                                 return load("java.lang.Object"); }
    public ClassType Class()   { if (CLASS_ != null) return CLASS_;
                                 return load("java.lang.Class"); }
    public ClassType String()  { if (STRING_ != null) return STRING_;
                                 return load("java.lang.String"); }
    public ClassType Throwable() { if (THROWABLE_ != null) return THROWABLE_;
                                   return load("java.lang.Throwable"); }
    public ClassType Error() { return load("java.lang.Error"); }
    public ClassType Exception() { return load("java.lang.Exception"); }
    public ClassType RuntimeException() { return load("java.lang.RuntimeException"); }
    public ClassType Cloneable() { return load("java.lang.Cloneable"); }
    public ClassType Serializable() { return load("java.io.Serializable"); }
    public ClassType NullPointerException() { return load("java.lang.NullPointerException"); }
    public ClassType ClassCastException()   { return load("java.lang.ClassCastException"); }
    public ClassType OutOfBoundsException() { return load("java.lang.ArrayIndexOutOfBoundsException"); }
    public ClassType ArrayStoreException()  { return load("java.lang.ArrayStoreException"); }
    public ClassType ArithmeticException()  { return load("java.lang.ArithmeticException"); }

    protected NullType createNull() {
        return new NullType_c(this);
    }

    protected PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
        return new PrimitiveType_c(this, kind);
    }

    protected final NullType NULL_         = createNull();
    protected final PrimitiveType VOID_    = createPrimitive(PrimitiveType.VOID);
    protected final PrimitiveType BOOLEAN_ = createPrimitive(PrimitiveType.BOOLEAN);
    protected final PrimitiveType CHAR_    = createPrimitive(PrimitiveType.CHAR);
    protected final PrimitiveType BYTE_    = createPrimitive(PrimitiveType.BYTE);
    protected final PrimitiveType SHORT_   = createPrimitive(PrimitiveType.SHORT);
    protected final PrimitiveType INT_     = createPrimitive(PrimitiveType.INT);
    protected final PrimitiveType LONG_    = createPrimitive(PrimitiveType.LONG);
    protected final PrimitiveType FLOAT_   = createPrimitive(PrimitiveType.FLOAT);
    protected final PrimitiveType DOUBLE_  = createPrimitive(PrimitiveType.DOUBLE);

    public TypeObject placeHolder(TypeObject o) {
        assert_(o);
    	return placeHolder(o, new HashSet());
    }

    public TypeObject placeHolder(TypeObject o, Set roots) {
        assert_(o);

	// This should never happen: anonymous and local types cannot
	// appear in signatures.

	if (o instanceof AnonClassType || o instanceof LocalClassType) {
	    throw new InternalCompilerError("Cannot serialize " + o + ".");
	}

	// Need place-holders for primitives since Enums don't serialize
	// correctly.  Bother.
	if (o instanceof PrimitiveType) {
	    return new PlaceHolder_c((Type) o);
	}

	if (o instanceof TopLevelClassType || o instanceof MemberClassType) {
	    return new PlaceHolder_c((Type) o);
	}

	return o;
    }

    protected UnknownType unknownType = new UnknownType_c(this);
    protected UnknownQualifier unknownQualifier = new UnknownQualifier_c(this);

    public UnknownType unknownType(Position pos) {
	return unknownType;
    }

    public UnknownQualifier unknownQualifier(Position pos) {
	return unknownQualifier;
    }

    public Package packageForName(Package prefix, String name) {
        assert_(prefix);
	return new Package_c(this, prefix, name);
    }

    public Package packageForName(String name) {
        if (name == null || name.equals("")) {
	    return null;
	}

	String s = StringUtil.getShortNameComponent(name);
	String p = StringUtil.getPackageComponent(name);

	return packageForName(packageForName(p), s);
    }

    /**
     * Returns a type identical to <type>, but with <dims> more array
     * dimensions.
     */
    public ArrayType arrayOf(Type type) {
        assert_(type);
        return arrayOf(type.position(), type);
    }

    public ArrayType arrayOf(Position pos, Type type) {
        assert_(type);
	return arrayType(pos, type);
    }

    /**
     * Factory method for ArrayTypes.
     */
    protected ArrayType arrayType(Position pos, Type type) {
	return new ArrayType_c(this, pos, type);
    }
    
    public ArrayType arrayOf(Type type, int dims) {
        return arrayOf(null, type, dims);
    }

    public ArrayType arrayOf(Position pos, Type type, int dims) {
	if (dims > 1) {
	    return arrayOf(pos, arrayOf(pos, type, dims-1));
	}
	else if (dims == 1) {
	    return arrayOf(pos, type);
	}
	else {
	    throw new InternalCompilerError(
		"Must call arrayOf(type, dims) with dims > 0");
	}
    }

    /**
     * Returns a canonical type corresponding to the Java Class object
     * theClass.  Does not require that <theClass> have a JavaClass
     * registered in this typeSystem.  Does not register the type in
     * this TypeSystem.  For use only by JavaClass implementations.
     **/
    public Type typeForClass(Class clazz) throws SemanticException
    {
	if (clazz == Void.TYPE)      return VOID_;
	if (clazz == Boolean.TYPE)   return BOOLEAN_;
	if (clazz == Byte.TYPE)      return BYTE_;
	if (clazz == Character.TYPE) return CHAR_;
	if (clazz == Short.TYPE)     return SHORT_;
	if (clazz == Integer.TYPE)   return INT_;
	if (clazz == Long.TYPE)      return LONG_;
	if (clazz == Float.TYPE)     return FLOAT_;
	if (clazz == Double.TYPE)    return DOUBLE_;

	if (clazz.isArray()) {
	    return arrayOf(typeForClass(clazz.getComponentType()));
	}

	return systemResolver.findType(clazz.getName());
    }

    public Set getTypeEncoderRootSet(Type t) {
	/* Need to encode the type and all its members at once. */
	Set s = new HashSet();

	if (t.isClass()) {
	    LinkedList stack = new LinkedList();
	    stack.add(t);

	    while (! stack.isEmpty()) {
		ClassType ct = (ClassType) stack.removeLast();
		s.add(ct);
		stack.addAll(ct.memberClasses());
	    }
	}
	else {
	    s.add(t);
	}

	return s;
    }

    public String translatePackage(Resolver c, Package p) {
        return p.translate(c);
    }

    public String translateArray(Resolver c, ArrayType t) {
        return t.translate(c);
    }

    public String translateTopLevelClass(Resolver c, TopLevelClassType t) {
        return t.translate(c);
    }

    public String translateMemberClass(Resolver c, MemberClassType t) {
        return t.translate(c);
    }

    public String translateLocalClass(Resolver c, LocalClassType t) {
        return t.translate(c);
    }

    public String translatePrimitive(Resolver c, PrimitiveType t) {
        return t.translate(c);
    }

    public PrimitiveType primitiveForName(String name)
	throws SemanticException {

	if (name.equals("void")) return Void();
	if (name.equals("boolean")) return Boolean();
	if (name.equals("char")) return Char();
	if (name.equals("byte")) return Byte();
	if (name.equals("short")) return Short();
	if (name.equals("int")) return Int();
	if (name.equals("long")) return Long();
	if (name.equals("float")) return Float();
	if (name.equals("double")) return Double();

	throw new SemanticException("Unrecognized primitive type \"" +
	    name + "\".");
    }

    protected LazyClassInitializer defaultClassInit;

    public LazyClassInitializer defaultClassInitializer() {
        if (defaultClassInit == null) {
            defaultClassInit = new LazyClassInitializer_c(this);
        }

        return defaultClassInit;
    }

    public ParsedTopLevelClassType topLevelClassType() {
	return topLevelClassType(defaultClassInitializer());
    }

    public ParsedMemberClassType memberClassType() {
	return memberClassType(defaultClassInitializer());
    }

    public ParsedLocalClassType localClassType() {
	return localClassType(defaultClassInitializer());
    }

    public ParsedAnonClassType anonClassType() {
	return anonClassType(defaultClassInitializer());
    }

    public ParsedTopLevelClassType topLevelClassType(LazyClassInitializer init) {
	return new ParsedTopLevelClassType_c(this, init);
    }

    public ParsedMemberClassType memberClassType(LazyClassInitializer init) {
	return new ParsedMemberClassType_c(this, init);
    }

    public ParsedLocalClassType localClassType(LazyClassInitializer init) {
	return new ParsedLocalClassType_c(this, init);
    }

    public ParsedAnonClassType anonClassType(LazyClassInitializer init) {
	return new ParsedAnonClassType_c(this, init);
    }

    public List defaultPackageImports() {
	List l = new ArrayList(1);
	l.add("java.lang");
	return l;
    }

    public PrimitiveType promote(Type t1, Type t2) throws SemanticException {
	if (! t1.isNumeric()) {
	    throw new SemanticException(
		"Cannot promote non-numeric type " + t1);
	}

	if (! t2.isNumeric()) {
	    throw new SemanticException(
		"Cannot promote non-numeric type " + t2);
	}

	return promoteNumeric(t1.toPrimitive(), t2.toPrimitive());
    }

    protected PrimitiveType promoteNumeric(PrimitiveType t1, PrimitiveType t2) {
	if (t1.isDouble() || t2.isDouble()) {
	    return Double();
	}

	if (t1.isFloat() || t2.isFloat()) {
	    return Float();
	}

	if (t1.isLong() || t2.isLong()) {
	    return Long();
	}

	return Int();
    }

    public PrimitiveType promote(Type t) throws SemanticException {
	if (! t.isNumeric()) {
	    throw new SemanticException(
		"Cannot promote non-numeric type " + t);
	}

	return promoteNumeric(t.toPrimitive());
    }

    protected PrimitiveType promoteNumeric(PrimitiveType t) {
	if (t.isByte() || t.isShort() || t.isChar()) {
	    return Int();
	}

	return t.toPrimitive();
    }

    /** All possible <i>access</i> flags. */
    protected static final Flags ACCESS_FLAGS = Flags.PUBLIC
					   .set(Flags.PROTECTED)
					   .set(Flags.PRIVATE);

    /** All flags allowed for a local variable. */
    protected static final Flags LOCAL_FLAGS = Flags.FINAL;

    /** All flags allowed for a field. */
    protected static final Flags FIELD_FLAGS = ACCESS_FLAGS
	                                  .set(Flags.STATIC)
					  .set(Flags.FINAL)
					  .set(Flags.TRANSIENT)
					  .set(Flags.VOLATILE);

    /** All flags allowed for a constructor. */
    protected static final Flags CONSTRUCTOR_FLAGS = ACCESS_FLAGS
    						.set(Flags.NATIVE); // FIXME ??

    /** All flags allowed for an initializer block. */
    protected static final Flags INITIALIZER_FLAGS = Flags.STATIC;

    /** All flags allowed for a method. */
    protected static final Flags METHOD_FLAGS = ACCESS_FLAGS
				           .set(Flags.ABSTRACT)
					   .set(Flags.STATIC)
					   .set(Flags.FINAL)
					   .set(Flags.SYNCHRONIZED)
					   .set(Flags.NATIVE)
					   .set(Flags.STRICTFP);

    /** All flags allowed for a top-level class. */
    protected static final Flags TOP_LEVEL_CLASS_FLAGS = ACCESS_FLAGS
						    .set(Flags.ABSTRACT)
						    .set(Flags.FINAL)
						    .set(Flags.STRICTFP)
						    .set(Flags.INTERFACE);

    /** All flags allowed for a member class. */
    protected static final Flags MEMBER_CLASS_FLAGS = TOP_LEVEL_CLASS_FLAGS
						 .set(Flags.STATIC);

    /** All flags allowed for a local class. */
    protected static final Flags LOCAL_CLASS_FLAGS = TOP_LEVEL_CLASS_FLAGS
					      .clear(ACCESS_FLAGS);

    public void checkMethodFlags(Flags f) throws SemanticException {
      	if (! f.clear(METHOD_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare method with flags " +
		f.clear(METHOD_FLAGS) + ".");
	}

        if (f.isAbstract() && f.isStatic()) {
	    throw new SemanticException(
		"Cannot declare method that is both abstract and static.");
        }

        if (f.isAbstract() && f.isFinal()) {
	    throw new SemanticException(
		"Cannot declare method that is both abstract and final.");
        }

        if (f.isAbstract() && f.isNative()) {
	    throw new SemanticException(
		"Cannot declare method that is both abstract and native.");
        }
    }

    public void checkLocalFlags(Flags f) throws SemanticException {
      	if (! f.clear(LOCAL_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare local variable with flags " +
		f.clear(LOCAL_FLAGS) + ".");
	}
    }

    public void checkFieldFlags(Flags f) throws SemanticException {
      	if (! f.clear(FIELD_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare field with flags " +
		f.clear(FIELD_FLAGS) + ".");
	}

	checkAccessFlags(f);
    }

    public void checkConstructorFlags(Flags f) throws SemanticException {
      	if (! f.clear(CONSTRUCTOR_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare constructor with flags " +
		f.clear(CONSTRUCTOR_FLAGS) + ".");
	}

	checkAccessFlags(f);
    }

    public void checkInitializerFlags(Flags f) throws SemanticException {
      	if (! f.clear(INITIALIZER_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare initializer with flags " +
		f.clear(INITIALIZER_FLAGS) + ".");
	}
    }

    public void checkTopLevelClassFlags(Flags f) throws SemanticException {
      	if (! f.clear(TOP_LEVEL_CLASS_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare top-level class with flags " +
		f.clear(TOP_LEVEL_CLASS_FLAGS) + ".");
	}

        if (f.isFinal() && f.isInterface()) {
            throw new SemanticException("Cannot declare a final interface.");
        }

	checkAccessFlags(f);
    }

    public void checkMemberClassFlags(Flags f) throws SemanticException {
      	if (! f.clear(MEMBER_CLASS_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare member class with flags " +
		f.clear(MEMBER_CLASS_FLAGS) + ".");
	}

        if (f.isFinal() && f.isInterface()) {
            throw new SemanticException("Cannot declare a final interface.");
        }

	checkAccessFlags(f);
    }

    public void checkLocalClassFlags(Flags f) throws SemanticException {
      	if (! f.clear(LOCAL_CLASS_FLAGS).equals(Flags.NONE)) {
	    throw new SemanticException(
		"Cannot declare member class with flags " +
		f.clear(LOCAL_CLASS_FLAGS) + ".");
	}

        if (f.isFinal() && f.isInterface()) {
            throw new SemanticException("Cannot declare a final interface.");
        }

	checkAccessFlags(f);
    }

    public void checkAccessFlags(Flags f) throws SemanticException {
        int count = 0;
        if (f.isPublic()) count++;
        if (f.isProtected()) count++;
        if (f.isPrivate()) count++;

	if (count > 1) {
	    throw new SemanticException(
		"Invalid access flags: " + f.retain(ACCESS_FLAGS) + ".");
	}
    }

    /**
     * Returns t, modified as necessary to make it a legal
     * static target.
     */
    public Type staticTarget(Type t) {
        // Nothing needs done in standard Java.
        return t;
    }

    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }
}
