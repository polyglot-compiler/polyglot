package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.ReferenceType_c;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class TypeVariable_c extends ReferenceType_c implements TypeVariable {

    private static int count = 0;

    /**
     * The unique identifier uniquely identifies a type variable within this invocation of the compiler.
     * Object equality does not hold, since we may have two objects that represent the same type variable, that have had
     * substitution applied to their upper bounds. 
     */
    public final transient int uniqueIdentifier = count++;
    protected String name;

    protected Flags flags;

    protected TVarDecl declaredIn;

    protected ClassType declaringClass;
    protected JL5ProcedureInstance declaringProcedure;

//    private List<ReferenceType> bounds;
    //private ParsedClassType syntheticClass; // SC: should probably combine with bounds, and have intersection classes...

    /**
     * The upper bound of this type variable. Should always be non-null. 
     */
    private ReferenceType upperBound;

    /**
     * It is possible for type variables to have lower bounds. See JLS 3rd ed 4.10.2 and 5.1.10 
     */
    private ReferenceType lowerBound = null;

    public TypeVariable_c(TypeSystem ts, Position pos, String id,
            ReferenceType upperBound) {
        super(ts, pos);
        this.name = id;
        if (upperBound == null) {
            upperBound = ts.Object();
        }
        this.upperBound = upperBound;
    }

    //    
    //    public Job job() {
    //        throw new InternalCompilerError("No job for a type variable");
    //    }
    //    
    @Override
    public void declaringProcedure(JL5ProcedureInstance pi) {
        declaredIn = TVarDecl.PROCEDURE_TYPE_VARIABLE;
        declaringProcedure = pi;
        declaringClass = null;
    }

    @Override
    public void declaringClass(ClassType ct) {
        declaredIn = TVarDecl.CLASS_TYPE_VARIABLE;
        declaringProcedure = null;
        declaringClass = ct;
    }

    @Override
    public TVarDecl declaredIn() {
        if (declaredIn == null) {
            declaredIn = TVarDecl.SYNTHETIC_TYPE_VARIABLE;
        }
        return declaredIn;
    }

    @Override
    public ClassType declaringClass() {
        if (declaredIn.equals(TVarDecl.CLASS_TYPE_VARIABLE))
            return declaringClass;
        return null;
    }

    @Override
    public JL5ProcedureInstance declaringProcedure() {
        if (declaredIn.equals(TVarDecl.PROCEDURE_TYPE_VARIABLE))
            return declaringProcedure;
        return null;
    }

    //
    //    public ClassType outer() {
    //        return null;
    //    }
    //
    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    public polyglot.types.Package package_() {
        if (TVarDecl.CLASS_TYPE_VARIABLE.equals(declaredIn)) {
            return declaringClass().package_();
        }
        if (TVarDecl.PROCEDURE_TYPE_VARIABLE.equals(declaredIn)) {
            return declaringProcedure().container().toClass().package_();
        }
        return null;
    }

    public List<? extends ConstructorInstance> constructors() {
        return Collections.emptyList();
    }

    public List<? extends ClassType> memberClasses() {
        return Collections.emptyList();
        //return getSyntheticClass().memberClasses();
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
    public int uniqueIdentifier() {
        return uniqueIdentifier;
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

    @Override
    public boolean equalsImpl(TypeObject t) {
        if (t instanceof TypeVariable_c) {
            TypeVariable_c other = (TypeVariable_c) t;
            return this.uniqueIdentifier == other.uniqueIdentifier;
        }
        return false;
    }

    @Override
    public boolean typeEqualsImpl(Type t) {
        if (t instanceof TypeVariable_c) {
            TypeVariable_c other = (TypeVariable_c) t;
            return this.uniqueIdentifier == other.uniqueIdentifier;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.uniqueIdentifier;
    }

}
