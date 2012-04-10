package polyglot.ext.jl5.types;

import java.util.*;

import polyglot.ext.jl5.types.inference.InferenceSolver;
import polyglot.ext.jl5.types.inference.InferenceSolver_c;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.jl5.types.inference.LubType_c;
import polyglot.ext.jl5.types.reflect.JL5ClassFileLazyClassInitializer;
import polyglot.ext.param.types.PClass;
import polyglot.ext.param.types.ParamTypeSystem_c;
import polyglot.ext.param.types.Subst;
import polyglot.frontend.Source;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLazyClassInitializer;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.UniqueID;

public class JL5TypeSystem_c extends ParamTypeSystem_c implements JL5TypeSystem {

    protected ClassType ENUM_;

    protected ClassType ANNOTATION_;

    // this is for extended for
    protected ClassType ITERABLE_;

    protected ClassType ITERATOR_;

    public ClassType Enum() {
        if (ENUM_ != null) {
            return ENUM_;
        }
        else {
            return ENUM_ = load("java.lang.Enum");
        }
    }

    public ClassType Iterable() {
        if (ITERABLE_ != null) {
            return ITERABLE_;
        }
        else {
            return ITERABLE_ = load("java.lang.Iterable");
        }
    }

    public ClassType Iterator() {
        if (ITERATOR_ != null) {
            return ITERATOR_;
        }
        else {
            return ITERATOR_ = load("java.util.Iterator");
        }
    }

    public boolean accessibleFromPackage(Flags flags, Package pkg1, Package pkg2) {
        return super.accessibleFromPackage(flags, pkg1, pkg2);
    }
    public ClassType wrapperClassOfPrimitive(PrimitiveType t) {
        try {
            return (ClassType)this.typeForName(t.wrapperTypeString(this));
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Couldn't find primitive wrapper " + t.wrapperTypeString(this), e);
        }

    }

    @Override
    public PrimitiveType primitiveTypeOfWrapper(Type l) {
        try {
            if (l.equals(this.typeForName("java.lang.Boolean"))) return this.Boolean();
            if (l.equals(this.typeForName("java.lang.Character"))) return this.Char();
            if (l.equals(this.typeForName("java.lang.Byte"))) return this.Byte();
            if (l.equals(this.typeForName("java.lang.Short"))) return this.Short();
            if (l.equals(this.typeForName("java.lang.Integer"))) return this.Int();
            if (l.equals(this.typeForName("java.lang.Long"))) return this.Long();
            if (l.equals(this.typeForName("java.lang.Float"))) return this.Float();
            if (l.equals(this.typeForName("java.lang.Double"))) return this.Double();
        } catch (SemanticException e) {
            throw new InternalCompilerError("Couldn't find wrapper class");
        }
        return null;
    }

    @Override
    public boolean isPrimitiveWrapper(Type l) {
        if (primitiveTypeOfWrapper(l) != null) {
            return true;
        } else {
            return false;
        }
    }

    protected final Flags TOP_LEVEL_CLASS_FLAGS = JL5Flags.setEnum(super.TOP_LEVEL_CLASS_FLAGS);

    protected final Flags MEMBER_CLASS_FLAGS = JL5Flags.setEnum(super.MEMBER_CLASS_FLAGS);

    public void checkTopLevelClassFlags(Flags f) throws SemanticException {
        if (!f.clear(TOP_LEVEL_CLASS_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare a top-level class with flag(s) "
                    + f.clear(TOP_LEVEL_CLASS_FLAGS) + ".");
        }

        if (f.isFinal() && f.isInterface()) {
            throw new SemanticException("Cannot declare a final interface.");
        }

        checkAccessFlags(f);
    }

    public void checkMemberClassFlags(Flags f) throws SemanticException {
        if (!f.clear(MEMBER_CLASS_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare a member class with flag(s) "
                    + f.clear(MEMBER_CLASS_FLAGS) + ".");
        }

        if (f.isStrictFP() && f.isInterface()) {
            throw new SemanticException("Cannot declare a strictfp interface.");
        }

        if (f.isFinal() && f.isInterface()) {
            throw new SemanticException("Cannot declare a final interface.");
        }

        checkAccessFlags(f);
    }

    public ConstructorInstance defaultConstructor(Position pos, ClassType container) {
        assert_(container);

        Flags access = Flags.NONE;

        if (container.flags().isPrivate() || JL5Flags.isEnum(container.flags())) {
            access = access.Private();
        }
        if (container.flags().isProtected()) {
            access = access.Protected();
        }
        if (container.flags().isPublic() && !JL5Flags.isEnum(container.flags())) {
            access = access.Public();
        }
        return constructorInstance(pos, container, access, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

    }

    @Override
    public ParsedClassType createClassType(LazyClassInitializer init, Source fromSource) {
        return new JL5ParsedClassType_c(this, init, fromSource);
    }

    @Override
    protected PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
        return new JL5PrimitiveType_c(this, kind);
    }    

    @Override
    protected NullType createNull() {
        return new JL5NullType_c(this);
    }

    public EnumInstance findEnumConstant(ReferenceType container, String name, Context c)
    throws SemanticException {
        ClassType ct = null;
        if (c != null)
            ct = c.currentClass();
        return findEnumConstant(container, name, ct);
    }

    public EnumInstance findEnumConstant(ReferenceType container, String name, ClassType currClass)
    throws SemanticException {
        Collection enumConstants = findEnumConstants(container, name);
        if (enumConstants.size() == 0) {
            throw new NoMemberException(JL5NoMemberException.ENUM_CONSTANT, "Enum Constant: \""
                    + name + "\" not found in type \"" + container + "\".");
        }
        Iterator i = enumConstants.iterator();
        EnumInstance ei = (EnumInstance) i.next();

        if (i.hasNext()) {
            EnumInstance ei2 = (EnumInstance) i.next();

            throw new SemanticException("Enum Constant \"" + name
                    + "\" is ambiguous; it is defined in both " + ei.container() + " and "
                    + ei2.container() + ".");
        }

        if (currClass != null && !isAccessible(ei, currClass)) {
            throw new SemanticException("Cannot access " + ei + ".");
        }

        return ei;
    }

    public EnumInstance findEnumConstant(ReferenceType container, String name)
    throws SemanticException {
        return findEnumConstant(container, name, (ClassType) null);
    }

    public Set findEnumConstants(ReferenceType container, String name) {
        assert_(container);
        if (container == null) {
            throw new InternalCompilerError("Cannot access enum constant \"" + name
                    + "\" within a null container type.");
        }
        EnumInstance ei = null;

        
        if (container instanceof JL5ClassType) {
            ei = ((JL5ClassType) container).enumConstantNamed(name);
        }

        if (ei != null) {
            return Collections.singleton(ei);
        }

        Set enumConstants = new HashSet();

        return enumConstants;
    }

    public EnumInstance enumInstance(Position pos, ClassType ct, Flags f, String name,
            ParsedClassType anonType, long ordinal) {
        assert_(ct);
        return new EnumInstance_c(this, pos, ct, f, name, anonType, ordinal);
    }

    public Context createContext() {
        return new JL5Context_c(this);
    }

    public FieldInstance findFieldOrEnum(ReferenceType container, String name, ClassType currClass)
    throws SemanticException {

        FieldInstance fi = null;

        try {
            fi = findField(container, name, currClass);
        } catch (NoMemberException e) {
            fi = findEnumConstant(container, name, currClass);
        }

        return fi;
    }

    @Override
    public MethodInstance methodInstance(Position pos, ReferenceType container, Flags flags,
            Type returnType, String name, List argTypes, List excTypes) {
        return methodInstance(pos, container, flags, returnType, name, argTypes, excTypes, Collections.EMPTY_LIST);
    }
    @Override
    public JL5MethodInstance methodInstance(Position pos, ReferenceType container, Flags flags,
            Type returnType, String name, List argTypes, List excTypes, List typeParams) {

        assert_(container);
        assert_(returnType);
        assert_(argTypes);
        assert_(excTypes);
        assert_(typeParams);
        return new JL5MethodInstance_c(this, pos, container, flags, returnType, name, argTypes, excTypes, typeParams);
    }

    @Override
    public ConstructorInstance constructorInstance(Position pos, ClassType container, Flags flags,
            List argTypes, List excTypes) {
        return constructorInstance(pos, container, flags, argTypes, excTypes, Collections.EMPTY_LIST);
    }
    @Override
    public JL5ConstructorInstance constructorInstance(Position pos, ClassType container, Flags flags,
            List argTypes, List excTypes, List typeParams) {
        assert_(container);
        assert_(argTypes);
        assert_(excTypes);
        assert_(typeParams);
        return new JL5ConstructorInstance_c(this, pos, container, flags, argTypes, excTypes, typeParams);
    }

    public TypeVariable typeVariable(Position pos, String name, ReferenceType upperBound) {
//        System.err.println("JL5TS_c typevar created " + name + " " + bounds);
        return new TypeVariable_c(this, pos, name, upperBound);
    }

    public boolean isBaseCastValid(Type fromType, Type toType) {
        if (toType.isArray()) {
            Type base = ((ArrayType) toType).base();
            assert_(base);
            return fromType.isImplicitCastValidImpl(base);
        }
        return false;
    }

    public boolean numericConversionBaseValid(Type t, Object value) {
        if (t.isArray()) {
            return super.numericConversionValid(((ArrayType) t).base(), value);
        }
        return false;
    }

    public Flags flagsForBits(int bits) {
        Flags f = super.flagsForBits(bits);
        if ((bits & JL5Flags.ENUM_MOD) != 0) {
            f = JL5Flags.setEnum(f);
        }
        if ((bits & JL5Flags.VARARGS_MOD) != 0) {
            f = JL5Flags.setVarArgs(f);
        }
        return f;
    }

    @Override
    public ClassFileLazyClassInitializer classFileLazyClassInitializer(
            ClassFile clazz) {
        //return new ClassFileLazyClassInitializer(clazz, this);
        return new JL5ClassFileLazyClassInitializer(clazz, this);
    }

    @Override
    public ImportTable importTable(String sourceName, polyglot.types.Package pkg) {
        assert_(pkg);
        return new JL5ImportTable(this, pkg, sourceName);
    }

    @Override
    public ImportTable importTable(polyglot.types.Package pkg) {
        assert_(pkg);
        return new JL5ImportTable(this, pkg);
    }

    protected ArrayType createArrayType(Position pos, Type type, boolean isVarargs) {
        JL5ArrayType at = new JL5ArrayType_c(this, pos, type, isVarargs);
        return at;
    }

    @Override
    public ArrayType arrayOf(Position position, Type type, boolean isVarargs) {
        return arrayType(position, type, isVarargs);
    }

    /**
     * Factory method for ArrayTypes.
     */
    @Override
    protected ArrayType createArrayType(Position pos, Type type) {
        return new JL5ArrayType_c(this, pos, type, false);
    }

    Map<Type,ArrayType> varargsArrayTypeCache = new HashMap<Type,ArrayType>();
    protected ArrayType arrayType(Position pos, Type type, boolean isVarargs) {
        if (isVarargs) {
            ArrayType t = varargsArrayTypeCache.get(type);
            if (t == null) {
                t = createArrayType(pos, type, isVarargs);
                varargsArrayTypeCache.put(type, t);
            }
            return t;
        } else {
            return super.arrayType(pos, type);
        }
    }

    /*@Override
	protected Collection findMostSpecificProcedures(List acceptable) throws SemanticException {
		throw new InternalCompilerError("Unimplemented");
	}*/


    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.12.2
     */
    @Override
    protected List findAcceptableMethods(ReferenceType container, String name, List argTypes, ClassType currClass) throws SemanticException {
        return findAcceptableMethods(container, name, argTypes, Collections.EMPTY_LIST, currClass);
    }
    //@Override
    protected List findAcceptableMethods(ReferenceType container, String name, List argTypes, List actualTypeArgs, ClassType currClass) throws SemanticException {
        return findAcceptableMethods(container, name, argTypes, actualTypeArgs, currClass, null);
    }
    //@Override
    protected List findAcceptableMethods(ReferenceType container, String name, List argTypes, List actualTypeArgs, ClassType currClass, Type expectedReturnType) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        SemanticException error = null;

        // List of methods accessible from curClass that have valid method
        // calls without boxing/unboxing conversion or variable arity and
        // are not overridden by an unaccessible method
        List<MethodInstance> phase1methods = new ArrayList<MethodInstance>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion
        List<MethodInstance> phase2methods = new ArrayList<MethodInstance>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion and variable arity
        List<MethodInstance> phase3methods = new ArrayList<MethodInstance>();

        // A list of unacceptable methods, where the method call is valid, but
        // the method is not accessible. This list is needed to make sure that
        // the acceptable methods are not overridden by an unacceptable method.
        List<MethodInstance> inaccessible = new ArrayList<MethodInstance>();

        Set<Type> visitedTypes = new HashSet<Type>();

        LinkedList<Type> typeQueue = new LinkedList<Type>();
        typeQueue.addLast(container);

//        System.err.println("JL5TS: findAcceptableMethods for " + name + " in " + container);
        while (! typeQueue.isEmpty()) {
            Type type = typeQueue.remove();

//            System.err.println("   looking at type " + type + " " + type.getClass());
            // Make sure each type is considered only once
            if (visitedTypes.contains(type)) continue;
            visitedTypes.add(type);

            if (Report.should_report(Report.types, 2)) {
                Report.report(2, "Searching type " + type + " for method " + name + "(" + listToString(argTypes) + ")");
            }

            if (! type.isReference()) {
                throw new SemanticException("Cannot call method in " + " non-reference type " + type + ".");
            }

            for (JL5MethodInstance mi : (List<JL5MethodInstance>) type.toReference().methods()) {
                if (Report.should_report(Report.types, 3)) Report.report(3, "Trying " + mi);

                // Method name must match
                if (! mi.name().equals(name)) continue;
                JL5MethodInstance substMi = methodCallValid(mi, name, argTypes, actualTypeArgs, expectedReturnType); 
                if (substMi != null) {
                    mi = substMi;
                    if (isAccessible(mi, container, currClass)) {
                        if (Report.should_report(Report.types, 3)) {
                            Report.report(3, "->acceptable: " + mi + " in " + mi.container());
                        }
                        if (varArgsRequired(mi)) phase3methods.add(mi);
                        else if (boxingRequired(mi, argTypes)) phase2methods.add(mi);
                        else phase1methods.add(mi);
                    } else {
                        // method call is valid but the method is unaccessible
                        inaccessible.add(mi);
                        if (error == null) {
                            error = new NoMemberException(NoMemberException.METHOD,
                                    "Method " + mi.signature() + " in " + container + " is inaccessible.");
                        }
                    }
                } else {
                    if (error == null) {
                        error = new NoMemberException(NoMemberException.METHOD,
                                "Method " + mi.signature() +
                                " in " + container +
                                " cannot be called with arguments " +
                                "(" + listToString(argTypes) + ")."); 
                    }
                }
            }

            if (type.toReference().superType() != null) {
                typeQueue.addLast(type.toReference().superType());
            }

            typeQueue.addAll(type.toReference().interfaces());
        }

        if (error == null) {
            error = new NoMemberException(NoMemberException.METHOD,
                    "No valid method call found for " + name +
                    "(" + listToString(argTypes) + ")" +
                    " in " +
                    container + ".");
        }

        // remove any methods that are overridden by an inaccessible method
        for (MethodInstance mi : inaccessible) {
            phase1methods.removeAll(mi.overrides());
            phase2methods.removeAll(mi.overrides());
            phase3methods.removeAll(mi.overrides());
        }

//        System.err.println("JL5ts_c: acceptable methods for " + name + argTypes + " is " + phase1methods);
//        System.err.println("              " + phase2methods);
//        System.err.println("              " + phase3methods);
        if (! phase1methods.isEmpty()) return phase1methods;
        if (! phase2methods.isEmpty()) return phase2methods;
        if (! phase3methods.isEmpty()) return phase3methods;

        // No acceptable accessible methods were found
        throw error;
    }

    @Override
    public boolean methodCallValid(MethodInstance mi,
            String name, List argTypes) {
        return this.methodCallValid((JL5MethodInstance) mi, name, argTypes, null) != null;
    }
    @Override
    public JL5MethodInstance methodCallValid(JL5MethodInstance mi, String name, List<Type> argTypes, List<Type> actualTypeArgs) {
        return methodCallValid(mi, name, argTypes, actualTypeArgs, null);
    }
    @Override
    public JL5MethodInstance methodCallValid(JL5MethodInstance mi, String name, List<Type> argTypes, List<Type> actualTypeArgs, Type expectedReturnType) {
        if (actualTypeArgs == null) {
            actualTypeArgs = Collections.EMPTY_LIST;
        }
        JL5Subst subst = null;
        if (!mi.typeParams().isEmpty() && actualTypeArgs.isEmpty()) {
            // need to perform type inference
            subst = inferTypeArgs(mi, argTypes, expectedReturnType);
        }
        else if (!mi.typeParams().isEmpty() && !actualTypeArgs.isEmpty()) {
            Map<TypeVariable, Type> m = new HashMap<TypeVariable, Type>();
            Iterator<Type> iter = actualTypeArgs.iterator();
            for (TypeVariable tv : mi.typeParams()) {
                m.put(tv, iter.next());
            }
            subst = (JL5Subst) this.subst(m, new HashMap());
        }
        JL5MethodInstance mj = mi;
        if (!mi.typeParams().isEmpty() && subst != null) {
            //mj = (JL5MethodInstance) this.instantiate(mi.position(), mi, actualTypeArgs);
            mj = (JL5MethodInstance) subst.substMethod(mi);
        }
//        System.err.println("JL5TS methocall valid to " + mi + " with argtypes " + argTypes + " and actuals " + actualTypeArgs);
//        System.err.println("  subst is " + subst);
//        System.err.println("  Call to mi " + mi + " after inference is " +  mj);
//        System.err.println("  super.methodCallValid ? " +  super.methodCallValid(mj, name, argTypes));
        if (super.methodCallValid(mj, name, argTypes)) { 
            return mj;
        }
        return null;
    }

    @Override
    public boolean callValid(ProcedureInstance mi, List argTypes) {
        return this.callValid((JL5ProcedureInstance) mi, (List<Type>)argTypes, null) != null;
    }
    @Override
    public JL5ProcedureInstance callValid(JL5ProcedureInstance mi, List<Type> argTypes, List<Type> actualTypeArgs) {
        if (actualTypeArgs == null) {
            actualTypeArgs = Collections.EMPTY_LIST;
        }
        JL5Subst subst = null;
        if (!mi.typeParams().isEmpty() && actualTypeArgs.isEmpty()) {
            // need to perform type inference
            subst = inferTypeArgs(mi, argTypes, null);
        }
        else if (!mi.typeParams().isEmpty() && !actualTypeArgs.isEmpty()) {
            Map<TypeVariable, Type> m = new HashMap<TypeVariable, Type>();
            Iterator<Type> iter = actualTypeArgs.iterator();
            for (TypeVariable tv : mi.typeParams()) {
                m.put(tv, iter.next());
            }
            subst = (JL5Subst) this.subst(m, new HashMap());
        }
        
        JL5ProcedureInstance mj = mi;
        if (!mi.typeParams().isEmpty() && subst != null) {
            // check that the substitution satisfies the bounds

            for (TypeVariable tv : (Set<TypeVariable>) subst.substitutions().keySet()) {
                Type a = (Type) subst.substitutions().get(tv);
                if (!isSubtype(a, tv.upperBound())) {
                    return null;
                }
            }

            mj = (JL5ProcedureInstance) subst.substProcedure(mi);
        }

        if (super.callValid(mj, argTypes)) {
            return mj;
        }

        return null;
    }

    /**
     * Infer type arguments for mi, when it is called with arguments of type argTypes
     * @param mi
     * @param argTypes
     * @return
     */
    private JL5Subst inferTypeArgs(JL5ProcedureInstance mi, List argTypes, Type expectedReturnType) {
        InferenceSolver s = new InferenceSolver_c(mi, argTypes, this);
        Map<TypeVariable, Type> m;
        if (expectedReturnType != null) {
            m = s.solve(expectedReturnType);
        }
        else {
            m = s.solve();            
        }
        if (m == null) return null;
        JL5Subst subst = (JL5Subst) this.subst(m, new HashMap());
        return subst;
        
    }
    

    @Override
    public ClassType instantiate(Position pos, PClass base, List actuals) throws SemanticException {
        JL5ParsedClassType clazz = (JL5ParsedClassType) base.clazz();
        return instantiate(pos, clazz, actuals);
    }
    @Override
    public ClassType instantiate(Position pos, JL5ParsedClassType clazz, List<Type> actuals) throws SemanticException {
        if (clazz.typeVariables().isEmpty() || (actuals == null || actuals.isEmpty())) {
            return clazz;
        }
        boolean allNull = true;
        for (Object o : actuals) {
            if (o != null) {
                allNull = false;
                break;
            }
        }
        if (allNull) {
            return clazz;
        }
        return super.instantiate(pos, clazz.pclass(), actuals);
    }
    public JL5ProcedureInstance instantiate(Position pos, JL5ProcedureInstance mi, List<Type> actuals) {
        Map<TypeVariable, Type> m = new LinkedHashMap<TypeVariable, Type>();
        Iterator<Type> iter = actuals.iterator();
        for (TypeVariable tv : mi.typeParams()) {
            m.put(tv, iter.next());
        }
        JL5Subst subst = (JL5Subst) this.subst(m, new HashMap());
        JL5ProcedureInstance ret = subst.substProcedure(mi);
        ret.setContainer(mi.container());
        return ret;
    }


    private boolean boxingRequired(JL5ProcedureInstance pi, List<Type> paramTypes) {
        int numFormals = pi.formalTypes().size();
        for (int i = 0; i < numFormals -1; i++) {
            Type formal = (Type) pi.formalTypes().get(i);
            Type actual = paramTypes.get(i);
            if (formal.isPrimitive() ^ actual.isPrimitive())
                return true;
        }
        if (pi.isVariableArity()) {
            Type lastParams = ((JL5ArrayType) pi.formalTypes().get(numFormals - 1)).base();
            for (int i = numFormals - 1; i < paramTypes.size() - 1; i++) {
                if (lastParams.isPrimitive() ^ paramTypes.get(i).isPrimitive())
                    return true;
            }
        }
        else if (numFormals > 0) {
            Type formal = (Type) pi.formalTypes().get(numFormals - 1);
            Type actual = paramTypes.get(numFormals - 1);
            if (formal.isPrimitive() ^ actual.isPrimitive())
                return true;
        }
        return false;
    }

    private boolean varArgsRequired(JL5ProcedureInstance pi) {
        return pi.isVariableArity();
    }

    public List<ReferenceType> allAncestorsOf(ReferenceType rt) {
        Set<ReferenceType> ancestors = new HashSet<ReferenceType>();
        ancestors.add(rt);
        ReferenceType superT = (ReferenceType) rt.superType();
        if (superT != null) {
            ancestors.add(superT);
            ancestors.addAll(allAncestorsOf(superT));
        }
        for (Iterator it = rt.interfaces().iterator(); it.hasNext();) {
            ReferenceType inter = (ReferenceType) it.next();
            ancestors.add(inter);
            ancestors.addAll(allAncestorsOf(inter));
        }
        return new ArrayList<ReferenceType>(ancestors);
    }

    public static String listToString(List l) {
        StringBuffer sb = new StringBuffer();

        for (Iterator i = l.iterator(); i.hasNext();) {
            Object o = i.next();
            sb.append(o.toString());

            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public Subst subst(Map substMap, Map cache) {
        return new JL5Subst_c(this, substMap, cache);
    }

    @Override
    public boolean hasSameSignature(JL5MethodInstance mi, JL5MethodInstance mj) {
        return hasSameSignature(mi, mj, false);
    }
    protected boolean hasSameSignature(JL5MethodInstance mi, JL5MethodInstance mj, boolean eraseMj) {
        if (!mi.name().equals(mj.name())) {
            return false;
        }
        if (mi.formalTypes().size() != mj.formalTypes().size()) {
            return false;
        }
        if (mi.typeParams().size() != mj.typeParams().size()) {
            return false;            
        }
        
        // replace the type variables of mj with the type variables of mi
        if (!mi.typeParams().isEmpty()) {
            Map<TypeVariable, Type> substm = new LinkedHashMap();
            for (int i = 0; i < mi.typeParams().size(); i++) {
                substm.put(mj.typeParams().get(i), mi.typeParams().get(i));
            }
            Subst subst = this.subst(substm, new HashMap());
            mj = (JL5MethodInstance)subst.substMethod(mj);
        }
        
        // now check that the types match
        Iterator typesi = mi.formalTypes().iterator();
        Iterator typesj = mj.formalTypes().iterator();
        while (typesi.hasNext()) {
            Type ti = (Type)typesi.next();
            Type tj = (Type)typesj.next();
            if (eraseMj) {
                tj = this.erasureType(tj);
            }
            if (!ti.equals(tj)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean isSubSignature(JL5MethodInstance mi, JL5MethodInstance mj) {
        if (hasSameSignature(mi, mj)) {
            return true;
        }
        // check if the signature of mi is the same as the erasure of mj.
        return hasSameSignature(mi, mj, true);
    }

    @Override
    public boolean areOverrideEquivalent(JL5MethodInstance mi, JL5MethodInstance mj) {
        return isSubSignature(mi, mj) || isSubSignature(mj, mi);
    }
    
    @Override
    public boolean isUncheckedConversion(Type fromType, Type toType) {
        if (fromType instanceof JL5ClassType && toType instanceof JL5ClassType) {
            JL5ClassType from = (JL5ClassType)fromType;
            JL5ClassType to = (JL5ClassType)toType;
            if (from.isRawClass()) {
                if (!to.isRawClass() && to instanceof JL5SubstClassType) {
                    JL5SubstClassType tosct = (JL5SubstClassType)to;
                    return from.equals(tosct.base());
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean areReturnTypeSubstitutable(Type ri, Type rj) {
        if (ri.isPrimitive()) {
            return ri.equals(rj);
        }
        else if (ri.isReference()) {
            return ri.isSubtype(rj) || this.isUncheckedConversion(ri, rj) || ri.typeEquals(this.erasureType(rj));
        }
        else if (ri.isVoid()) {
            return rj.isVoid();
        }
        else {
            throw new InternalCompilerError("Unexpected return type: " + ri);
        }
    }

    @Override
    public MethodInstance findImplementingMethod(ClassType ct, MethodInstance mi) {
        ReferenceType curr = ct;
        while (curr != null) {
            List possible = curr.methodsNamed(mi.name());
            for (Iterator k = possible.iterator(); k.hasNext(); ) {
                MethodInstance mj = (MethodInstance)k.next();
                if (!mj.flags().isAbstract() && 
                    ((isAccessible(mi, ct) && isAccessible(mj, ct)) || 
                            isAccessible(mi, mj.container().toClass()))) {
                    // The method mj may be a suitable implementation of mi.
                    // mj is not abstract, and either mj's container 
                    // can access mi (thus mj can really override mi), or
                    // mi and mj are both accessible from ct (e.g.,
                    // mi is declared in an interface that ct implements,
                    // and mj is defined in a superclass of ct).
                    if (this.areOverrideEquivalent((JL5MethodInstance)mi, (JL5MethodInstance)mj)) {
                        return mj;
                    }
                }
            }
            if (curr == mi.container()) {
                // we've reached the definition of the abstract 
                // method. We don't want to look higher in the 
                // hierarchy; this is not an optimization, but is 
                // required for correctness. 
                break;
            }
            
            curr = curr.superType() ==  null ?
                   null : curr.superType().toReference();
        }
        return null;
    }

    @Override
    public Type erasureType(Type t) {
        if (t.isArray()) {
            ArrayType at = t.toArray();
            return at.base(this.erasureType(at.base()));
        }
        if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t;
            return this.erasureType((Type) tv.upperBound());
        }
        if (t instanceof IntersectionType) {
            IntersectionType it = (IntersectionType) t;
            return this.erasureType((Type) it.bounds().get(0));
        }
        if (t instanceof WildCardType) {
            WildCardType tv = (WildCardType) t;
            if(tv.upperBound() == null) {
                return this.Object();
            }
            return this.erasureType(tv.upperBound());
        }
        if (t instanceof JL5SubstType) {
            JL5SubstType jst = (JL5SubstType)t;            
            return this.erasureType(jst.base());
        }
        if (t instanceof JL5ParsedClassType) {
            return this.toRawType(t);
        }
        return t;
    }

    private JL5ClassType selfInstantiation(JL5ParsedClassType ct) {
        Map m = new HashMap();
        for (TypeVariable tv : ct.typeVariables()) {
            m.put(tv, tv);
        }
        return (JL5ClassType)this.subst(ct, m);
    }


    @Override
    public JL5Subst erasureSubst(List<TypeVariable> typeParams) {
        Map m = new LinkedHashMap();
        Set selfReferences = new LinkedHashSet();
        for (TypeVariable tv : typeParams) {
            m.put(tv, tv.erasureType());
        }
        if (m.isEmpty()) {
            return null;
        }
        JL5Subst ret = (JL5Subst)this.subst(m, new HashMap());
        return ret;
    }


    @Override
    public boolean isContained(Type fromType, Type toType) {
        if (toType instanceof WildCardType) {
            WildCardType wTo = (WildCardType) toType;
            if (fromType instanceof WildCardType) {
                WildCardType wFrom = (WildCardType) fromType;
                // JLS 3rd ed 4.5.1.1
                if (wFrom.isExtendsConstraint() && wTo.isExtendsConstraint()) {
                    if (isSubtype(wFrom.upperBound(), wTo.upperBound())) {
                        return true;
                    }
                }
                if (wFrom.isSuperConstraint() && wTo.isSuperConstraint()) {
                    if (isSubtype(wTo.lowerBound(), wFrom.lowerBound())) {
                        return true;
                    }
                }

            }
            if (wTo.isSuperConstraint()) {
                if (isImplicitCastValid(wTo.lowerBound(), fromType)) {
                    return true;
                }
            }
            else if (wTo.isExtendsConstraint()) {
                if (isImplicitCastValid(fromType, wTo.upperBound())) {
                    return true;
                }
            }
            return false;
        }
        else {
            return this.typeEquals(fromType, toType);
        }

    }

    @Override
    public boolean descendsFrom(Type child, Type ancestor) {
//        System.err.println("jl5TS_C: descends from: " + child + " descended from " + ancestor);
        boolean b = super.descendsFrom(child, ancestor);
        if (b) return true;
//        System.err.println("   : descends from 0");
        if (ancestor instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) ancestor;
            // See JLS 3rd ed 4.10.2: type variable is a direct supertype of its lowerbound.
            if (tv.hasLowerBound()) {
//                System.err.println("   : descends from 1");
                return isSubtype(child, tv.lowerBound());
            }
        }
        if (ancestor instanceof WildCardType) {
            WildCardType w = (WildCardType) ancestor;
            // See JLS 3rd ed 4.10.2: type variable is a direct supertype of its lowerbound.
            if (w.hasLowerBound()) {
//                System.err.println("   : descends from 2");
                return isSubtype(child, w.lowerBound());
            }            
        }
//        System.err.println("   : descends from 3");
        return false;
    }

    @Override
    public boolean isSubtype(Type t1, Type t2) {
        if (super.isSubtype(t1, t2)) {
            return true;
        }
        if (t2 instanceof WildCardType) {
            WildCardType wct = (WildCardType)t2;
            if (wct.hasLowerBound() && isSubtype(t1, wct.lowerBound())) {
                return true;
            }
        }
        if (t2 instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable)t2;
            if (tv.hasLowerBound() && isSubtype(t1, tv.lowerBound())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isImplicitCastValid(Type fromType, Type toType) {
        LinkedList<Type> chain = isImplicitCastValidChain(fromType, toType);
        // Try an unchecked conversion, if toType is a parameterized type.
        if (chain == null && toType instanceof JL5SubstClassType) {
            JL5SubstClassType toSubstCT = (JL5SubstClassType)toType;
            chain = isImplicitCastValidChain(fromType, this.rawClass(toSubstCT.base(), toSubstCT.base().position()));
            if (chain != null) {
                // success!
                chain.addLast(toType);
            }
        }
        
        if (chain == null) {
            return false;
        }
        // check whether "the chain of conversions contains two parameterized types that are not not in the subtype relation."
        // See JLS 3rd ed 5.2 and 5.3.
        for (int i = 0; i < chain.size(); i++) {
            Type t = chain.get(i);
            if (t instanceof JL5SubstClassType) {
                for (int j = i+1; j < chain.size(); j++) {
                    Type u = chain.get(j);
                    if (u instanceof JL5SubstClassType) {
                        if (!t.isSubtype(u)) {
                            return false;
                        }
                    }
                }

            }
        }
        return true;
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChain(Type fromType, Type toType) {
        assert_(fromType);
        assert_(toType);

        LinkedList<Type> chain = null; 
        if (fromType instanceof JL5ClassType) {
            chain = ((JL5ClassType)fromType).isImplicitCastValidChainImpl(toType);
        }
        else if (fromType.isImplicitCastValidImpl(toType)) {
            chain = new LinkedList();
            chain.add(fromType);
            chain.add(toType);
        }

        return chain;
    }

    @Override
    public boolean isCastValid(Type fromType, Type toType) {
        if (super.isCastValid(fromType, toType)) {
            return true;
        }
        // try an unchecked conversion.
        Type raw = this.toRawType(toType);
        if (raw != toType) {
            return isCastValid(fromType, raw);
        }
        return false;
    }
        

    @Override
    protected List abstractSuperInterfaces(ReferenceType rt) {
        List superInterfaces = new LinkedList();
        superInterfaces.add(rt);

        for (Iterator iter = rt.interfaces().iterator(); iter.hasNext(); ) {
            JL5ClassType interf = (JL5ClassType)iter.next();
            if (interf.isRawClass()) {
                // it's a raw class, so use the erased version of it
                interf = (JL5ClassType) this.erasureType(interf);
            }
            superInterfaces.addAll(abstractSuperInterfaces(interf));
        }

        if (rt.superType() != null) {
            JL5ClassType c = (JL5ClassType) rt.superType().toClass();
            if (c.flags().isAbstract()) {
                // the superclass is abstract, so it may contain methods
                // that must be implemented.
                superInterfaces.addAll(abstractSuperInterfaces(c));
            }
            else {
                // the superclass is not abstract, so it must implement
                // all abstract methods of any interfaces it implements, and
                // any superclasses it may have.
            }
        }
        return superInterfaces;
    }

    @Override
    public MethodInstance findMethod(ReferenceType container,
            java.lang.String name, List argTypes, List<Type> typeArgs,
            ClassType currClass) throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List acceptable = findAcceptableMethods(container, name, argTypes, typeArgs, currClass);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.METHOD,
                    "No valid method call found for " + name +
                    "(" + listToString(argTypes) + ")" +
                    " in " +
                    container + ".");
        }

        Collection maximal =
            findMostSpecificProcedures(acceptable);

        if (maximal.size() > 1) {
            StringBuffer sb = new StringBuffer();
            for (Iterator i = maximal.iterator(); i.hasNext();) {
                MethodInstance ma = (MethodInstance) i.next();
                sb.append(ma.returnType());
                sb.append(" ");
                sb.append(ma.container());
                sb.append(".");
                sb.append(ma.signature());
                if (i.hasNext()) {
                    if (maximal.size() == 2) {
                        sb.append(" and ");
                    }
                    else {
                        sb.append(", ");
                    }
                }
            }

            throw new SemanticException("Reference to " + name +
                    " is ambiguous, multiple methods match: "
                    + sb.toString());
        }

        MethodInstance mi = (MethodInstance) maximal.iterator().next();
        return mi;
    }


    @Override
    public ConstructorInstance findConstructor(ClassType container, List argTypes, ClassType currClass)
    throws SemanticException {
        return this.findConstructor(container, argTypes, Collections.EMPTY_LIST, currClass);
    }


    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List argTypes, List<Type> typeArgs, ClassType currClass)
    throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List acceptable = findAcceptableConstructors(container, argTypes, typeArgs, currClass);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.CONSTRUCTOR,
                    "No valid constructor found for " +
                    container + "(" + listToString(argTypes) + ").");
        }

        Collection maximal = findMostSpecificProcedures(acceptable);

        if (maximal.size() > 1) {
            throw new NoMemberException(NoMemberException.CONSTRUCTOR,
                    "Reference to " + container + " is ambiguous, multiple " +
                    "constructors match: " + maximal);
        }

        ConstructorInstance ci = (ConstructorInstance) maximal.iterator().next();
        return ci;
    }
    @Override
    protected List findAcceptableConstructors(ClassType container, List argTypes, ClassType currClass) throws SemanticException {
        return this.findAcceptableConstructors(container, argTypes, Collections.EMPTY_LIST, currClass);
    }

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.12.2
     * @throws SemanticException 
     */
    protected List findAcceptableConstructors(ClassType container, List argTypes,
            List<Type> actualTypeArgs, ClassType currClass) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        SemanticException error = null;

        // List of methods accessible from curClass that have valid method
        // calls without boxing/unboxing conversion or variable arity and
        // are not overridden by an unaccessible method
        List<ConstructorInstance> phase1methods = new ArrayList<ConstructorInstance>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion
        List<ConstructorInstance> phase2methods = new ArrayList<ConstructorInstance>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion and variable arity
        List<ConstructorInstance> phase3methods = new ArrayList<ConstructorInstance>();

        if (Report.should_report(Report.types, 2))
            Report.report(2, "Searching type " + container + " for constructor " + container + "(" + listToString(argTypes) + ")");

        for (JL5ConstructorInstance ci : (List<JL5ConstructorInstance>) container.constructors()) {
            if (Report.should_report(Report.types, 3)) Report.report(3, "Trying " + ci);

            JL5ConstructorInstance substCi = (JL5ConstructorInstance) callValid(ci, argTypes, actualTypeArgs); 
            if (substCi != null) {
                ci = substCi;
                if (isAccessible(ci, currClass)) {
                    if (Report.should_report(Report.types, 3)) Report.report(3, "->acceptable: " + ci);
                    if (varArgsRequired(ci)) phase3methods.add(ci);
                    else if (boxingRequired(ci, argTypes)) phase2methods.add(ci);
                    else phase1methods.add(ci);
                } else {
                    if (error == null) {
                        error = new NoMemberException(NoMemberException.CONSTRUCTOR,
                                "Constructor " + ci.signature() +
                        " is inaccessible."); 
                    }
                }
            } else {
                if (error == null) {
                    error = new NoMemberException(NoMemberException.CONSTRUCTOR,
                            "Constructor " + ci.signature() +
                            " cannot be invoked with arguments " +
                            "(" + listToString(argTypes) + ").");
                }
            }
        }

        if (! phase1methods.isEmpty()) return phase1methods;
        if (! phase2methods.isEmpty()) return phase2methods;
        if (! phase3methods.isEmpty()) return phase3methods;

        if (error == null) {
            error = new NoMemberException(NoMemberException.CONSTRUCTOR,
                    "No valid constructor found for " + container +
                    "(" + listToString(argTypes) + ").");
        }

        throw error;
    }


    @Override
    public boolean isAccessible(MemberInstance mi, ReferenceType container, ClassType contextClass) {
        assert_(mi);

        ReferenceType target;
        // does container inhereit mi?
        if (container.descendsFrom(mi.container()) && mi.flags().isPublic()) {
            target = container;
        }
        else {
            target = mi.container();
        }

        Flags flags = mi.flags();

        if (! target.isClass()) {
            // public members of non-classes are accessible;
            // non-public members of non-classes are inaccessible
            return flags.isPublic();
        }
                
        JL5ClassType targetClass = (JL5ClassType)target.toClass();
        
        if (isAccessible_(flags, targetClass, contextClass)) {
            return true;
        }
        // make sure we strip away any parameters.
        ClassType targetClassDecl = (ClassType)targetClass.declaration();
        if (targetClassDecl != targetClass && isAccessible_(flags, targetClassDecl, contextClass)) {
            return true;
        }
        
        ClassType contextClassDecl = (ClassType)contextClass.declaration();
        if (contextClassDecl != contextClass && isAccessible_(flags, targetClass, contextClassDecl)) {
            return true;
        }
        
        if (targetClassDecl != targetClass && contextClassDecl != contextClass && isAccessible_(flags, targetClassDecl, contextClassDecl)) {
            return true;
        }

        return false;
    }
    
    
    protected boolean isAccessible_(Flags flags, ClassType targetClass, ClassType contextClass) {
        if (!classAccessible(targetClass, contextClass)) {
            return false;
        }

        if (equals(targetClass, contextClass))
            return true;

        // If the current class and the target class are both in the
        // same class body, then protection doesn't matter, i.e.
        // protected and private members may be accessed. Do this by
        // working up through contextClass's containers.
        if (isEnclosed(contextClass, targetClass) || isEnclosed(targetClass, contextClass))
            return true;

        ClassType ct = contextClass;
        while (!ct.isTopLevel()) {
            ct = ct.outer();
            if (isEnclosed(targetClass, ct))
                return true;
        }

        // protected
        if (flags.isProtected()) {
            // If the current class is in a
            // class body that extends/implements the target class, then
            // protected members can be accessed. Do this by
            // working up through contextClass's containers.
            if (descendsFrom(contextClass, targetClass)) {
                return true;
            }

            ct = contextClass;
            while (!ct.isTopLevel()) {
                ct = ct.outer();
                if (descendsFrom(ct, targetClass)) {
                    return true;
                }
            }
        }

        return accessibleFromPackage(flags, targetClass.package_(), contextClass.package_());
    }


    @Override
    public boolean isEnclosed(ClassType inner, ClassType outer) {
        if (inner instanceof JL5ClassType) {
            inner = (ClassType)inner.declaration();
        }
        if (outer instanceof JL5ClassType) {
            outer = (ClassType)outer.declaration();
        }
        return inner.isEnclosedImpl(outer);
    }

    @Override
    public boolean hasEnclosingInstance(ClassType inner, ClassType encl) {
        if (inner instanceof JL5ClassType) {
            inner = (ClassType)inner.declaration();
        }
        if (encl instanceof JL5ClassType) {
            encl = (ClassType)encl.declaration();
        }
        return inner.hasEnclosingInstanceImpl(encl);
    }

    @Override
    public WildCardType wildCardType(Position position) {
        return wildCardType(position, null, null);
    }

    @Override
    public WildCardType wildCardType(Position position, ReferenceType upperBound, ReferenceType lowerBound) {
        if (upperBound == null) {
            upperBound = this.Object();
        }
        return new WildCardType_c(this, position, upperBound, lowerBound);
    }

    @Override
    public Type applyCaptureConversion(Type t) {
        if (!(t instanceof JL5SubstClassType_c)) {
            return t;
        }
        JL5SubstClassType_c ct = (JL5SubstClassType_c) t;
        JL5ParsedClassType g = (JL5ParsedClassType) ct.base();

        List<Type> capturedActuals = new ArrayList(g.typeVariables().size());
        Map<TypeVariable, Type> substmap = new LinkedHashMap();
        // first, set up a subst from the formals to the captured variables.
        for (TypeVariable a : g.typeVariables()) {
            Type ti = ct.subst().substType(a);
            Type si = ti;
            if (ti instanceof WildCardType) {
                si = this.typeVariable(ti.position(), 
                        UniqueID.newID("captureConversionFresh"),
                        null); // we'll replace this unknown type soon.            
            }
            capturedActuals.add(si);
            substmap.put(a, si);
        }
        JL5Subst subst = (JL5Subst) this.subst(substmap, new HashMap());
        
        // now go through and substitute the bounds if needed.
        for (TypeVariable a : g.typeVariables()) {
            Type ti = ct.subst().substType(a);
            Type si = subst.substType(a);
            if (ti instanceof WildCardType) {
                WildCardType wti = (WildCardType) ti;
                TypeVariable vsi = (TypeVariable) si;
                if (wti.isExtendsConstraint()) {
                    List newBounds;
                    vsi.setUpperBound(this.glb(wti.upperBound(), (ReferenceType) subst.substType(a.upperBound())));                        
                }
                else {
                    // wti is a super wildcard.
                    vsi.setUpperBound((ReferenceType)subst.substType(a.upperBound()));
                    vsi.setLowerBound(wti.lowerBound());
                }

            }
        }
        
        return subst.substType(g);
    }

    @Override
    public Flags legalLocalFlags() {
        return JL5Flags.setVarArgs(super.legalLocalFlags());
    }
    
    @Override
    public Flags legalMethodFlags() {
        return JL5Flags.setVarArgs(super.legalMethodFlags());
    }

    
    @Override
    public JL5SubstClassType findGenericSupertype(JL5ParsedClassType base, ReferenceType sub) {
        List<ReferenceType> ancestors = allAncestorsOf(sub);
        for (ReferenceType a : ancestors) {
            if (!(a instanceof JL5SubstClassType)) {
                continue;
            }
            JL5SubstClassType instantiatedType = (JL5SubstClassType) a;
            JL5ParsedClassType instBase = (JL5ParsedClassType) instantiatedType.base();
            
            if (typeEquals(base, instBase)) {
                return instantiatedType;
            }
        }
        return null;        
    }

    @Override
    public ReferenceType intersectionType(Position pos, List<ReferenceType> types) {       
        if (types.size() == 1) {
            return types.get(0);
        }
        if (types.isEmpty()) {
            return Object();
        }

        return new IntersectionType_c(this, pos, types);
    }

    @Override
    public boolean checkIntersectionBounds(List<? extends Type> bounds, boolean quiet)
    throws SemanticException {
        /*        if ((bounds == null) || (bounds.size() == 0)) {
 if (!quiet)
 throw new SemanticException("Intersection type can't be empty");
 return false;
 }*/
        List<Type> concreteBounds = concreteBounds(bounds);
        if (concreteBounds.size() == 0) {
            if (!quiet)
                throw new SemanticException("Invalid bounds in intersection type.");
            else
                return false;
        }
        for (int i = 0; i < concreteBounds.size(); i++)
            for (int j = i + 1; j < concreteBounds.size(); j++) {
                Type t1 = concreteBounds.get(i);
                Type t2 = concreteBounds.get(j);
                // for now, no checks if at least one is an array type
                if (!t1.isClass() || !t2.isClass()) {
                    return true;
                }
                if (!t1.toClass().flags().isInterface() && !t2.toClass().flags().isInterface()) {
                    if ((!isSubtype(t1, t2)) && (!isSubtype(t2, t1))) {
                        if (!quiet)
                            throw new SemanticException("Error in intersection type. Types " + t1
                                    + " and " + t2 + " are not in subtype relation.");
                        else
                            return false;
                    }
                }
                if (t1.toClass().flags().isInterface() && t2.toClass().flags().isInterface()
                        && (t1 instanceof JL5SubstClassType) && (t2 instanceof JL5SubstClassType)) {
                    JL5SubstClassType j5t1 = (JL5SubstClassType) t1;
                    JL5SubstClassType j5t2 = (JL5SubstClassType) t2;
                    if (j5t1.base().equals(j5t2.base()) && !j5t1.equals(j5t2)) {
                        if (!quiet) {
                            throw new SemanticException("Error in intersection type. Interfaces "
                                    + j5t1
                                    + " and "
                                    + j5t2
                                    + "are instantinations of the same generic interface but with different type arguments");
                        }
                        else {
                            return false;
                        }
                    }
                }
            }
        return true;
    }

    //@Override
    public List<Type> concreteBounds(List<? extends Type> bounds) {

        Set<Type> included = new LinkedHashSet<Type>();
        Set<Type> visited = new LinkedHashSet<Type>();
        List<Type> queue = new ArrayList<Type>(bounds);
        while (!queue.isEmpty()) {
            Type t = queue.remove(0);
            if (visited.contains(t))
                continue;
            visited.add(t);
            if (t instanceof TypeVariable) {
                TypeVariable tv = (TypeVariable) t;
                queue.add(tv.upperBound());
            }
            else if (t instanceof IntersectionType) {
                IntersectionType it = (IntersectionType) t;
                queue.addAll((List)it.bounds());
            }
            else {
                included.add(t);
            }
        }
        return new ArrayList<Type>(included);
    }
    
    public ReferenceType glb(ReferenceType t1, ReferenceType t2) {
        List<ReferenceType> l = new ArrayList<ReferenceType>();
        l.add(t1);
        l.add(t2);
        try {
            // XXX also need to check that does not have two classes that are not in a subclass relation?
            if (!this.checkIntersectionBounds(l, true)) {
                return this.Object();
            }
            else {
                return this.intersectionType(Position.compilerGenerated(), l);
            }
        } catch (SemanticException e) {
            return this.Object();
        }
    }

    @Override
    public UnknownType unknownReferenceType(Position position) {
        return unknownReferenceType;
    }
    protected UnknownType unknownReferenceType = new UnknownReferenceType_c(this);


    @Override
    public RawClass rawClass(JL5ParsedClassType base) {
        return this.rawClass(base, base.position());
    }
    @Override
    public RawClass rawClass(JL5ParsedClassType base, Position pos) {
        if (base.typeVariables().isEmpty()) {
            throw new InternalCompilerError("Can only create a raw class with a parameterized class");
        }
        return new RawClass_c(base, pos);        
    }
    
     @Override
    public Type toRawType(Type t) {
        if (!t.isReference()) {
            return t;
        }
        if (t instanceof RawClass) {
            return t;
        }
        if (t instanceof JL5ParsedClassType) {
            JL5ParsedClassType ct = (JL5ParsedClassType)t;
            if (ct.typeVariables().isEmpty()) {
                return t;
            }
            return this.rawClass(ct, ct.position());
        }
        if (t instanceof ArrayType) {
            ArrayType at = t.toArray();
            Type b = this.toRawType(at.base());
            return at.base(b);
        }
        return t;
    }
  
     @Override
     public PrimitiveType promote(Type t1, Type t2) throws SemanticException {
         return super.promote(unboxingConversion(t1), unboxingConversion(t2));
     }

     @Override
     public Type boxingConversion(Type t) {
         if (t.isPrimitive()) {
             return this.wrapperClassOfPrimitive(t.toPrimitive());
         }
         return t;
     }
     @Override
     public Type unboxingConversion(Type t) {
         Type s = primitiveTypeOfWrapper(t);
         if (s != null) {
             return s;
         }
         return t;
     }

    @Override
    public LubType lub(Position pos, List<ReferenceType> us) {
        return new LubType_c(this, pos, us);
    }


}