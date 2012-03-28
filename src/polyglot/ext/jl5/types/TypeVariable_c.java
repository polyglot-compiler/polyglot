package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.types.*;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class TypeVariable_c extends ReferenceType_c implements TypeVariable {

    private static int count = 0;
    public final int uniqueIdentifier = count++;
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
        
    public TypeVariable_c(TypeSystem ts, Position pos, String id, ReferenceType upperBound) {
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
    public void declaringProcedure(JL5ProcedureInstance pi) {
        declaredIn = TVarDecl.PROCEDURE_TYPE_VARIABLE;
        declaringProcedure = pi;
        declaringClass = null;
    }

    public void declaringClass(ClassType ct) {
        declaredIn = TVarDecl.CLASS_TYPE_VARIABLE;
        declaringProcedure = null;
        declaringClass = ct;
    }

    public TVarDecl declaredIn() {
        if (declaredIn == null) {
            declaredIn = TVarDecl.SYNTHETIC_TYPE_VARIABLE;
        }
        return declaredIn;
    }


    public ClassType declaringClass() {
        if (declaredIn.equals(TVarDecl.CLASS_TYPE_VARIABLE)) return declaringClass;
        return null;
    }

    public JL5ProcedureInstance declaringProcedure() {
        if (declaredIn.equals(TVarDecl.PROCEDURE_TYPE_VARIABLE)) return declaringProcedure;
        return null;
    }
    //
    //    public ClassType outer() {
    //        return null;
    //    }
    //
    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

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
    public List constructors() {
        return Collections.emptyList();
    }

    public List memberClasses() {
        return Collections.EMPTY_LIST;
        //return getSyntheticClass().memberClasses();
    }

    public List methods() {
        return Collections.EMPTY_LIST;
    }

    public List fields() {
        return Collections.EMPTY_LIST;
    }
    public FieldInstance fieldNamed(String name) {
        for (Iterator i = fields().iterator(); i.hasNext(); ) {
            FieldInstance fi = (FieldInstance) i.next();
            if (fi.name().equals(name)) {
                return fi;
            }
        }
        return null;
    }

    public List interfaces() {
        return Collections.EMPTY_LIST;
    }
    public ReferenceType erasureType() {
        return (ReferenceType) ((JL5TypeSystem)this.typeSystem()).erasureType(this);
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
    
}
