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
//        for (Type b : bounds()) {
//            if (!b.isCanonical()) {
//                return false;
//            }
//        }
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
    //
    //    public Flags flags() {
    //        return flags;
    //    }
    //
    public List constructors() {
        return Collections.emptyList();
    }

    public List memberClasses() {
        return Collections.EMPTY_LIST;
        //return getSyntheticClass().memberClasses();
    }

    public List methods() {
        return Collections.EMPTY_LIST;
//        List l = getSyntheticClass().methods();
////        System.out.println("LIST:"+l);
//        return l;
//        List m = new ArrayList();
//        for (ReferenceType t : bounds()) {
//            m.addAll(t.methods());
//        }
//        return m;
    }

    public List fields() {
        return Collections.EMPTY_LIST;
//        return getSyntheticClass().fields();
//        List m = new ArrayList();
//        for (ReferenceType t : bounds()) {
//            m.addAll(t.fields());
//        }
//        return m;
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
//        List interfaces = new ArrayList();
//        for (ReferenceType t : bounds) {
//            if (t.isClass() && t.toClass().flags().isInterface()) {
//                interfaces.add(t);
//            }
//        }
//        return interfaces;
        //return getSyntheticClass().interfaces();
    }
    //
    //    public boolean inStaticContext() {
    //        return false; // not sure
    //    }
    //
    public ReferenceType erasureType() {
        return (ReferenceType) ((JL5TypeSystem)this.typeSystem()).erasureType(this);
    }
    //    
    //    public String translate(Resolver c) {
    //    	return jl4Type().translate(c);
    //    }
    //
    //    public String toString() {
    //        return name;// +":"+bounds;
    //    }
    //
    //    //Compare type equality using JL4 type
    //    public boolean equalsImpl(TypeObject other) {
    //        if (other instanceof TypeVariable)
    //        	return jl4Type().equalsImpl(((TypeVariable) other).jl4Type());
    //        else
    //        	return jl4Type().equalsImpl(other);
    //    }
    //
    //    public ClassType toClass() {
    //        return this;
    //    }
    //
    //	
    @Override
    public Type superType() {
        return this.upperBound;
//        if (bounds.isEmpty()) {
//            return ts.Object();
//        }
//        Type t = bounds.get(0);
//        if (t.isClass() && !t.toClass().flags().isInterface()) {
//            return t;
//        }
//        return ts.Object();
    }

//    public List<ReferenceType> bounds() {
//        return bounds;
//    }

//    public TypeVariable bounds(List<ReferenceType> newBounds) {
////        System.err.println("BAD TYPEVAR_C");
//        if (this.bounds == newBounds) {
//            return this;
//        }
//        if (this.bounds != null && newBounds != null && this.bounds.size() == newBounds.size()) {
//            boolean allEqual = true;
//            for (int i = 0; i < newBounds.size(); i++) {
//                if (!ts.typeEquals(this.bounds.get(i), newBounds.get(i))) {
//                    allEqual = false;
//                    break;
//                }
//            }
//            if (allEqual) {
//                return this;
//            }
//        }
//        TypeVariable_c tv = (TypeVariable_c) this.copy();
//        tv.bounds = newBounds;
//        return tv;
//    }

//    @Override
//    public void setBounds(List<ReferenceType> newBounds) {
//        this.bounds = newBounds;
////        System.err.println("TypeVariable_c.setBounds on " + this.name + " are now " + bounds);
//    }

/*    protected ClassType getSyntheticClass() {
        if (syntheticClass == null) {
            syntheticClass = typeSystem().createClassType();
            for (ReferenceType t : bounds()) {
                if (t.isClass() && ((ClassType)t).flags().isInterface())
                    syntheticClass.addInterface(t);
                else {
                    if(syntheticClass.supertypesResolved())
                        throw new InternalCompilerError("Cannot have multiple class boundaries!");
                    else {
                        syntheticClass.superType(t);
                        syntheticClass.setSupertypesResolved(true);
                    }
                }
                for(Iterator it = t.methods().iterator(); it.hasNext();) {
                    MethodInstance mi = (MethodInstance) it.next();
                    syntheticClass.addMethod(mi);
                }
                for(Iterator it = t.fields().iterator(); it.hasNext();) {
                    FieldInstance fi = (FieldInstance) it.next();
                    syntheticClass.addField(fi);
                }
                //XXX: do we need to add member classes too?
            }
            syntheticClass.setMembersAdded(true);
            if(!syntheticClass.supertypesResolved()) {
                syntheticClass.superType(ts.Object());
                syntheticClass.setSupertypesResolved(true);
            }

            syntheticClass.package_(this.package_());
            syntheticClass.setSignaturesResolved(true);
//            System.out.println("SYNTH: " + syntheticClass.superType() + " implements " + syntheticClass.interfaces());
        }
        return syntheticClass;
    }
    */
    @Override
    public String translate(Resolver c) {
        return this.name();
    }
    @Override
    public String toString() {
        return this.name();
    }
    @Override
    public boolean typeEqualsImpl(Type t) {
        if (this == t) {
            return true;
        }
        if (t instanceof TypeVariable_c) {
            TypeVariable_c that = (TypeVariable_c) t;
            if (this.name().equals(that.name())
                    && this.declaredIn == that.declaredIn
                    && ((this.declaredIn == TVarDecl.CLASS_TYPE_VARIABLE
                            && this.declaringClass.equals(that.declaringClass))
                            || (this.declaredIn == TVarDecl.PROCEDURE_TYPE_VARIABLE
                                    && this.declaringProcedure.equals(that.declaringProcedure)))
                    && (this.upperBound == that.upperBound || (this.upperBound != null && this.upperBound.equals(that.upperBound)))
                    && (this.lowerBound == that.lowerBound || (this.lowerBound != null && this.lowerBound.equals(that.lowerBound)))
                    ) {
                return true;
            }
            
        }
        return false;
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
