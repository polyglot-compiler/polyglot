package polyglot.ext.jl5.types;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ast.Expr;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.frontend.Source;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Declaration;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.ParsedClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public interface JL5TypeSystem extends TypeSystem,
        ParamTypeSystem<TypeVariable, ReferenceType> {
    @Override
    ParsedClassType createClassType(LazyClassInitializer init, Source fromSource);

    @Override
    ParsedClassType createClassType(Source fromSource);

    ClassType Enum();

    ClassType Iterable();

    ClassType Iterator();

    ClassType Annotation();

    ClassType OverrideAnnotation();

    ClassType TargetAnnotation();

    ClassType RetentionAnnotation();

    ClassType AnnotationElementType();

    /** 
     * 
     * Is the annotation element <code>annotation</code> applicable
     * to Declaration decl? For example, if annotation is "@Override" then decl
     * better be a methodInstance that overrides another method. If annotation's
     * type itself has annotations describing which targets are appropriate,
     * then decl must be an appropriate target.
     * 
     * @param annotation
     * @param decl
     * @throws SemanticException
     */
    void checkAnnotationApplicability(AnnotationElem annotation,
            Declaration decl) throws SemanticException;

    boolean accessibleFromPackage(Flags flags, Package pkg1, Package pkg2);

    /**
     * 
     * @return the list of types that makes the implicit cast valid. Returns empty list if isImplicitCastValid(t1, t2) == false.
     *      If the return value is non empty, then t1 is the first element, and t2 is the last.
     */
    LinkedList<Type> isImplicitCastValidChain(Type t1, Type t2);

    /**
     * Do methods mi and mj have the same signature?
     * See JLS 3rd ed. 8.4.2 
     * They have the same signature if they have the same name and argument types. 
     * Two method or constructor declarations M and N have the same argument types if all 
     * of the following conditions hold:
     * 1. They have the same number of formal parameters (possibly zero)
     * 2. They have the same number of type parameters (possibly zero)
     * 3. Let <A1,...,An> be the formal type parameters of M and let <B1,...,Bn> be the 
     *     formal type parameters of N. After renaming each occurrence of a Bi in N's 
     *     type to Ai the bounds of corresponding type variables and the 
     *     argument types of M and N are the same. 
     */
    boolean hasSameSignature(JL5MethodInstance mi, JL5MethodInstance mj);

    /**
     * Is method mi a sub-signature of method mj?  The signature ofmethod mi is a subsignature of the signature of a method mj if either
     * mj hasthe same signature as mi, or
     * the signature of mi is the same as the erasure of the signature of mj.
     * See JLS 3rd ed. 8.4.2 
     */
    boolean isSubSignature(JL5MethodInstance mi, JL5MethodInstance mj);

    /**
     * Are methods mi and mj override equivalent?
     * See JLS 3rd ed. 8.4.2 
     * They are override equivalent if either mi is a subsignature of mj, or mj is a subsignature of mi.  
     */
    boolean areOverrideEquivalent(JL5MethodInstance mi, JL5MethodInstance mj);

    /**
     * Are types ri and rj return type substitutable?
     * See JLS 3rd ed. 8.4.5
     * They are return type substitutable if:
     * - if ri is a primitive type, and rj is identical to ri.
     * - if ri is a reference type, and ri is either a subtype of rj or 
     *      ri can be converted to a subtype of rj by unchecked conversion (5.1.9), or
            ri is the erasure of rj.
     * - if ri is void and rj is void.
     */
    boolean areReturnTypeSubstitutable(Type ri, Type rj);

    /**
     * Is there an unchecked conversion from "from" to "to"?
     * See JLS 3rd ed. 5.1.9
     * 
     * True if: "from" is a generic type G with n formal type parameters,
     * and "to" is any parameterized type of the form G<T1 ... Tn>.
     */
    boolean isUncheckedConversion(Type from, Type to);

    /**
     * Is the type reifiable (i.e., representable at runtime).
     * See JLS 3rd Ed Section 4.7
     */
    boolean isReifiable(Type t);

    /**
     * Instantiate class clazz with actuals.
     */
    ClassType instantiate(Position pos, JL5ParsedClassType clazz,
            List<? extends ReferenceType> actuals) throws SemanticException;

    /**
     * Instantiate class clazz with actuals.
     */
    ClassType instantiate(Position pos, JL5ParsedClassType clazz,
            ReferenceType... actuals) throws SemanticException;

    /**
     * Returns the erased type of t.
     * See JLS 3rd ed. 4.6 
     * 
     */
    Type erasureType(Type t);

    /**
     * Given a JL5ProcedureInstance, produce a JL5Subst that maps the
     * TypeVariables to the erasure of their bounds.
     */
    JL5Subst erasureSubst(JL5ProcedureInstance pi);

    /**
     * Given a , produce a JL5RawSubst that maps the
     * TypeVariables to the erasure of their bounds.
     */
    JL5Subst erasureSubst(JL5ParsedClassType base);

    JL5MethodInstance methodInstance(Position pos, ReferenceType container,
            Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, List<? extends Type> excTypes,
            List<TypeVariable> typeParams);

    JL5ConstructorInstance constructorInstance(Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes, List<TypeVariable> typeParams);

    JL5ProcedureInstance instantiate(Position pos, JL5ProcedureInstance mi,
            List<? extends ReferenceType> actuals);

    /**
     * Check whether <code>mi</code> can be called with name <code>name</code>
     * and arguments of type <code>actualTypes</code>, with type parameters
     * instantiated with actualTypeArgs. If actualTypeArgs is null or empty,
     * then type inference should be performed. (See JLS 3rd ed. 15.12.2.7)
     * Will return null if mi cannot be successfully called. Will return an appropriately 
     * instantiated method instance if the call is valid (i.e., the substitution after type inference). 
     */
    JL5MethodInstance methodCallValid(JL5MethodInstance mi, String name,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs,
            Type expectedReturnType);

    /**
     * Check whether <code>ci</code> can be called with
     * arguments of type <code>actualTypes</code>, with type parameters
     * instantiated with actualTypeArgs. If actualTypeArgs is null or empty,
     * then type inference should be performed. (See JLS 3rd ed. 15.12.2.7)
     * Will return null if ci cannot be successfully called. Will return an appropriately 
     * instantiated instance if the call is valid (i.e., the substitution after type inference). 
     */
    JL5ProcedureInstance callValid(JL5ProcedureInstance mi,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs);

    /**
     * Returns the PrimitiveType corresponding to the wrapper type t. For example primitiveOf([java.lang.Integer]) = [int]
     */
    ClassType wrapperClassOfPrimitive(PrimitiveType t);

    PrimitiveType primitiveTypeOfWrapper(Type l);

    boolean isPrimitiveWrapper(Type l);

    EnumInstance enumInstance(Position pos, ClassType container, Flags f,
            String name, ParsedClassType anonType, long l);

    @Override
    Context createContext();

    EnumInstance findEnumConstant(ReferenceType container, String name,
            ClassType currClass) throws SemanticException;

    EnumInstance findEnumConstant(ReferenceType container, String name,
            Context c) throws SemanticException;

    EnumInstance findEnumConstant(ReferenceType container, String name)
            throws SemanticException;

    FieldInstance findFieldOrEnum(ReferenceType container, String name,
            ClassType currClass) throws SemanticException;

    boolean numericConversionBaseValid(Type t, Object value);

    boolean isBaseCastValid(Type from, Type to);

    /**
     * Is fromType contained in toType? See JLS 3rd ed 4.5.1.1
     */
    boolean isContained(Type fromType, Type toType);

    /**
     * Apply capture conversion to t. See JLS 3rd ed 5.1.10
     */
    Type applyCaptureConversion(Type t);

    @Override
    Flags flagsForBits(int bits);

    TypeVariable typeVariable(Position pos, String name,
            ReferenceType upperBound);

    WildCardType wildCardType(Position position);

    WildCardType wildCardType(Position position, ReferenceType upperBound,
            ReferenceType lowerBound);

    @Override
    boolean equals(TypeObject arg1, TypeObject arg2);

    List<ReferenceType> allAncestorsOf(ReferenceType rt);

    ArrayType arrayOf(Position position, Type base, boolean isVarargs);

    MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass,
            Type expectedReturnType) throws SemanticException;

    ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass)
            throws SemanticException;

    /**
     * Base is a generic supertype (e.g., a class C with uninstantiated parameters).
     * Try to find a supertype of sub that is an instantiation of base. 
     */
    JL5SubstClassType findGenericSupertype(JL5ParsedClassType base,
            ReferenceType sub);

    ReferenceType intersectionType(Position pos, List<ReferenceType> types);

    boolean checkIntersectionBounds(List<? extends Type> bounds, boolean quiet)
            throws SemanticException;

    ReferenceType glb(ReferenceType t1, ReferenceType t2);

    ReferenceType glb(Position pos, List<ReferenceType> bounds);

    UnknownReferenceType unknownReferenceType(Position position);

    /**
     * Create a raw class
     */
    Type toRawType(Type t);

    /**
     * Create a raw class
     */
    RawClass rawClass(JL5ParsedClassType base, Position pos);

    /**
     * Create a raw class
     */
    RawClass rawClass(JL5ParsedClassType base);

    /**
     * Perform boxing conversion on t. If t is a primitive, then
     * the return type will be the ReferenceType appropriate for boxing t 
     * (e.g., java.lang.Integer for int, etc.). If t is not a numeric primitive
     * then the return type is t.
     */
    Type boxingConversion(Type t);

    /**
     * Perform unboxing conversion on t. If t is a wrapper type for a primitive type, then
     * the return type will be the appropriate primitive type 
     * (e.g., int for java.lang.Integer, etc.). If t is not a wrapper type for a  primitive
     * type then the return type is t.
     */
    Type unboxingConversion(Type t);

    /**
     * Compute the least upper bound of a set of types <code>bounds</code>. This is the
     * lub(U1 ... Uk) function, as defined in the JLS 3rd edition, Section 15.12.2.7. 
     */
    LubType lub(Position pos, List<ReferenceType> bounds);

    boolean isValidAnnotationValueType(Type t);

    AnnotationTypeElemInstance annotationElemInstance(Position pos,
            ClassType ct, Flags f, Type type, String name, boolean hasDefault);

    void checkAnnotationValueConstant(Expr value) throws SemanticException;

    AnnotationTypeElemInstance findAnnotation(ReferenceType t, String name,
            ClassType currentClass) throws SemanticException;

    void checkDuplicateAnnotations(List<AnnotationElem> annotations)
            throws SemanticException;

    /**
     * Return the class representing Class<type>
     * @param type
     * @return
     */
    Type Class(Position pos, ReferenceType type);

    /**
     * What are the type variables of class ct or any outer 
     * class for which an instance of ct has an enclosing instance?
     */
    List<TypeVariable> classAndEnclosingTypeVariables(JL5ParsedClassType ct);

    ClassType instantiateInnerClassFromContext(Context c, ClassType ct);

    /**
     * @param position
     * @return
     */
    UnknownTypeVariable unknownTypeVariable(Position position);

    /**
     * Given a list of annotation elements, create a RetainedAnnotations
     * for the annotations that should survive in the binary (i.e., in the
     * type information)
     * @throws SemanticException 
     */
    RetainedAnnotations createRetainedAnnotations(
            List<AnnotationElem> annotationElems, Position pos)
            throws SemanticException;

    /**
     * A special RetainedAnnotations that has no retained annotations.
     */
    RetainedAnnotations NoRetainedAnnotations();

    AnnotationElementValue AnnotationElementValueArray(Position pos,
            List<AnnotationElementValue> vals);

    AnnotationElementValue AnnotationElementValueAnnotation(Position pos,
            Type type,
            Map<String, AnnotationElementValue> annotationElementValues);

    AnnotationElementValue AnnotationElementValueConstant(Position pos,
            Type type, Object constVal);
}
