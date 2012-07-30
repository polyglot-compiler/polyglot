package polyglot.ext.jl5.types;

import java.util.List;

import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class RawClass_c extends JL5ClassType_c implements RawClass {

    private JL5ParsedClassType base;
    private transient JL5SubstClassType erased;
    
    public RawClass_c(JL5ParsedClassType t, Position pos) {
        super((JL5TypeSystem)t.typeSystem(), pos);
        this.base = t;
        this.setDeclaration(base);
    }

    @Override
    public boolean isRawClass() {
        return true;
    }

    @Override
    public JL5ParsedClassType base() {
        return this.base;
    }

    @Override
    public JL5SubstClassType erased() {
        if (this.erased == null) {            
            JL5TypeSystem ts = (JL5TypeSystem)this.ts;
            JL5Subst es = ts.erasureSubst(this.base);
            this.erased = new JL5SubstClassType_c(ts, base.position(),
                                    base, es);
        }
        return this.erased;
    }

    @Override
    public List<EnumInstance> enumConstants() {
        return this.erased().enumConstants();
    }

    @Override
    public Job job() {
        return null;
    }

    @Override
    public Kind kind() {
        return this.erased().kind();
    }

    @Override
    public ClassType outer() {
        ClassType t = this.erased().outer();
        if (t == null) {
            return t;
        }
        JL5TypeSystem ts = (JL5TypeSystem)this.typeSystem();
        
        return (ClassType)ts.erasureType(this.erased().outer());
    }

    @Override
    public String name() {
        return this.erased().name();
    }

    @Override
    public Package package_() {
        return this.erased().package_();
    }

    @Override
    public Flags flags() {
        return this.erased().flags();
    }

    private transient List<? extends ConstructorInstance> constructors = null;
    @Override
    public List<? extends ConstructorInstance> constructors() {
        if (constructors == null) {
            this.constructors = this.erased().constructors();
        }
        return this.constructors;
    }

    private transient List<? extends ClassType> memberClasses = null;
    @Override
    public List<? extends ClassType> memberClasses() {
        if (memberClasses == null) {
            this.memberClasses = this.erased().memberClasses();
        }
        return this.memberClasses;
    }

    private transient List<? extends MethodInstance> methods = null;
    @Override
    public List<? extends MethodInstance> methods() {
        if (methods == null) {
            this.methods = this.erased().methods();
        }
        return this.methods;
    }

    private transient List<? extends FieldInstance> fields = null;
    @Override
    public List<? extends FieldInstance> fields() {
        if (fields == null) {
            this.fields = this.erased().fields();
            for (FieldInstance fi : this.fields) {
                fi.setContainer(this);
            }
        }
        return this.fields;
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        return this.erased().interfaces();
    }

    @Override
    public Type superType() {
        return this.erased().superType();
    }

    @Override
    public boolean equalsImpl(TypeObject t) {
        if (super.equalsImpl(t)) {
            return true;
        }
        if (t instanceof RawClass) {
            RawClass rt = (RawClass) t;
            return typeSystem().equals(this.base(), rt.base());
        }
        return false;
    }

    @Override
    public boolean typeEqualsImpl(Type t) {
        if (super.typeEqualsImpl(t)) {
            return true;
        }
        if (t instanceof RawClass) {
            RawClass rt = (RawClass) t;
            return typeSystem().typeEquals(this.base(), rt.base());
        }
        return false;
    }

    @Override
    public String translateAsReceiver(Resolver c) {        
        return this.translate(c);
    }
    
    @Override
    public boolean descendsFromImpl(Type ancestor) {
//        System.err.println("   Raw class " + this + " descends from " + ancestor + " ?  interfaces is " + this.interfaces() + "  ::: " + super.descendsFromImpl(ancestor));
//        System.err.println("    base interfaces are "  + this.base.interfaces());
        if (super.descendsFromImpl(ancestor)) {
            return true;
        }
//        Type ra = ((JL5TypeSystem)ts).toRawType(ancestor);
//        if (!ra.equals(ancestor)) {
//            return ts.isSubtype(this, ra);
//        }
        return false;
    }
    
    
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public boolean inStaticContext() {
        return this.erased().inStaticContext();
    }

    @Override
    public void setFlags(Flags flags) {
        throw new InternalCompilerError("Should never be called");
    }

    @Override
    public void setContainer(ReferenceType container) {
        throw new InternalCompilerError("Should never be called");        
    }

    @Override
    public AnnotationTypeElemInstance annotationElemNamed(String name) {
        return this.erased().annotationElemNamed(name);
    }

    @Override
    public List<AnnotationTypeElemInstance> annotationElems() {
        return this.erased().annotationElems();
    }

    @Override
    public RetainedAnnotations retainedAnnotations() {
        return ((JL5TypeSystem) this.typeSystem()).NoRetainedAnnotations();
    }
    
}
