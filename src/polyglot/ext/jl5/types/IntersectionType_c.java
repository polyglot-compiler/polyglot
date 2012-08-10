package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ClassType_c;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class IntersectionType_c extends ClassType_c implements IntersectionType {

    protected List<ReferenceType> bounds;

//    protected List<Type> concreteBounds;

    protected TypeVariable boundOf_;

    public IntersectionType_c(TypeSystem ts, Position pos,
            List<ReferenceType> bounds) {
        super(ts, pos);
        this.bounds = bounds;
        checkBounds();
    }

    private void checkBounds() {
        if (this.bounds == null || this.bounds.size() < 2) {
            throw new InternalCompilerError("Intersection type needs at least two elements: "
                    + this.bounds);
        }
    }

    @Override
    public List<ReferenceType> bounds() {
        if (bounds == null || bounds.size() == 0) {
            bounds = new ArrayList<ReferenceType>();
            bounds.add(ts.Object());
        }
        return bounds;
    }

    @Override
    public String translate(Resolver c) {
        StringBuffer sb = new StringBuffer();//("intersection[ ");
        for (Iterator<ReferenceType> iter = bounds.iterator(); iter.hasNext();) {
            Type b = iter.next();
            sb.append(b.translate(c));
            if (iter.hasNext()) sb.append(" & ");
        }
        //sb.append(" ]");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();//("intersection[ ");
        sb.append(" ( ");
        for (Iterator<ReferenceType> iter = bounds.iterator(); iter.hasNext();) {
            Type b = iter.next();
            sb.append(b);
            if (iter.hasNext()) sb.append(" & ");
        }
        //sb.append(" ]");
        sb.append(" ) ");
        return sb.toString();
    }

//    protected List<Type> getConcreteBounds() {
//        if (concreteBounds == null) {
//            concreteBounds = ((JL5TypeSystem) typeSystem()).concreteBounds(this.bounds());
//        }
//        return concreteBounds;
//    }

    @Override
    public Type superType() {
        if (bounds.isEmpty()) {
            return ts.Object();
        }
        Type t = bounds.get(0);
        if (t.isClass() && !t.toClass().flags().isInterface()) {
            return t;
        }
        return ts.Object();

//        return getSyntheticClass().superType();
    }

    @Override
    public List<? extends ConstructorInstance> constructors() {
        return Collections.emptyList();
    }

//    protected ParsedClassType syntheticClass = null;
//
//    protected ClassType getSyntheticClass() {
//        if (syntheticClass == null) {
//            syntheticClass = typeSystem().createClassType();
//            ArrayList<Type> onlyClasses = new ArrayList<Type>();
//            for (ReferenceType t : getConcreteBounds()) {
//                if (t.isClass() && ((ClassType)t).flags().isInterface())
//                    syntheticClass.addInterface(t);
//                else
//                    onlyClasses.add(t);
//            }
//            if (onlyClasses.size() > 0) {
//                Collections.sort(onlyClasses, new Comparator<ReferenceType>() {
//                    public int compare(ReferenceType o1, ReferenceType o2) {
//                        JL5TypeSystem ts = (JL5TypeSystem) typeSystem();
//                        if (ts.equals(o1, o2))
//                            return 0;
//                        if (ts.isSubtype(o1, o2))
//                            return -1;
//                        return 1;
//                    }
//                });
//                syntheticClass.superType(onlyClasses.get(0));
//            }
//            syntheticClass.package_(this.package_());
//        }
//        return syntheticClass;
//    }

    @Override
    public List<? extends FieldInstance> fields() {
        return Collections.emptyList();
//        return getSyntheticClass().fields();
    }

    @Override
    public Flags flags() {
        return Flags.PUBLIC.set(Flags.FINAL);
        //return getSyntheticClass().flags();
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        List<ClassType> interfaces = new ArrayList<ClassType>();
        for (Type t : bounds) {
            if (t.isClass() && t.toClass().flags().isInterface()) {
                interfaces.add((ClassType) t);
            }
        }
        return interfaces;
//        return getSyntheticClass().interfaces();
    }

    @Override
    public Kind kind() {
        return INTERSECTION;
    }

    @Override
    public List<? extends ClassType> memberClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends MethodInstance> methods() {
        return Collections.emptyList();
//        return getSyntheticClass().methods();
    }

    @Override
    public String name() {
        return this.toString();
    }

    @Override
    public ClassType outer() {
        return null;
    }

    @Override
    public Package package_() {
//        if (boundOf() != null) 
//            return boundOf().package_();
        return null;
    }

    @Override
    public boolean inStaticContext() {
        return false;
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {

        for (Type b : bounds()) {
            if (typeSystem().isImplicitCastValid(b, toType)) {
                LinkedList<Type> chain = new LinkedList<Type>();
                chain.add(this);
                chain.add(toType);
                return chain;
            }
        }
        //or just isImplicitCastValid(getSyntaticClass(), toType());
        return null;
    }

    @Override
    public boolean isSubtypeImpl(Type ancestor) {
        for (Type b : bounds()) {
            if (typeSystem().isSubtype(b, ancestor)) return true;
        }
        return false;
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        for (Type b : bounds()) {
            if (typeSystem().isCastValid(b, toType)) return true;
        }
        return false;
    }

    @Override
    public void boundOf(TypeVariable tv) {
        boundOf_ = tv;
    }

    @Override
    public TypeVariable boundOf() {
        return boundOf_;
    }

    @Override
    public boolean equalsImpl(TypeObject other) {
        if (!super.equalsImpl(other)) {
            if (other instanceof IntersectionType) {
                IntersectionType it = (IntersectionType) other;
                if (it.bounds().size() != this.bounds().size()) {
                    return false;
                }
                for (int i = 0; i < this.bounds().size(); i++) {
                    Type ti = this.bounds().get(i);
                    Type tj = it.bounds().get(i);
                    if (!typeSystem().equals(ti, tj)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean typeEqualsImpl(Type other) {
        if (!super.typeEqualsImpl(other)) {
            if (other instanceof IntersectionType) {
                IntersectionType it = (IntersectionType) other;
                if (it.bounds().size() != this.bounds().size()) {
                    return false;
                }
                for (int i = 0; i < this.bounds().size(); i++) {
                    Type ti = this.bounds().get(i);
                    Type tj = it.bounds().get(i);
                    if (!typeSystem().typeEquals(ti, tj)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFlags(Flags flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContainer(ReferenceType container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Job job() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBounds(List<ReferenceType> newBounds) {
        this.bounds = newBounds;
        checkBounds();
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public EnumInstance enumConstantNamed(String name) {
        return null;
    }

    @Override
    public List<EnumInstance> enumConstants() {
        return Collections.emptyList();
    }

    @Override
    public String translateAsReceiver(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationTypeElemInstance annotationElemNamed(String name) {
        return null;
    }

    @Override
    public List<AnnotationTypeElemInstance> annotationElems() {
        return Collections.emptyList();
    }

    @Override
    public RetainedAnnotations retainedAnnotations() {
        return ((JL5TypeSystem) this.typeSystem()).NoRetainedAnnotations();
    }
}
