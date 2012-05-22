package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.types.Package;
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

    public JL5SubstClassType erased() {
        if (this.erased == null) {            
            JL5TypeSystem ts = (JL5TypeSystem)this.ts;
            JL5Subst es = ts.erasureSubst(this.base);
            this.erased = new JL5SubstClassType_c((JL5TypeSystem) ts, base.position(),
                                    (JL5ParsedClassType) base, es);
        }
        return this.erased;
    }

    @Override
    public List enumConstants() {
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
        return this.erased().outer();
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

    private transient List<ConstructorInstance> constructors = null;
    @Override
    public List constructors() {
        if (constructors == null) {
            this.constructors = this.erased().constructors();
        }
        return this.constructors;
    }

    private transient List<ClassType> memberClasses = null;
    @Override
    public List memberClasses() {
        if (memberClasses == null) {
            this.memberClasses = this.erased().memberClasses();
        }
        return this.memberClasses;
    }

    private transient List<MethodInstance> methods = null;
    @Override
    public List methods() {
        if (methods == null) {
            this.methods = this.erased().methods();
        }
        return this.methods;
    }

    private transient List<FieldInstance> fields = null;
    @Override
    public List fields() {
        if (fields == null) {
            this.fields = this.erased().fields();
            for (FieldInstance fi : this.fields) {
                fi.setContainer(this);
            }
        }
        return this.fields;
    }

    @Override
    public List interfaces() {
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
        return this.base.translate(c);
    }
    
    @Override
    public String translate(Resolver c) {
        return this.base.translate(c);
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
    public AnnotationElemInstance annotationElemNamed(String name) {
        return this.erased().annotationElemNamed(name);
    }

    @Override
    public List<AnnotationElemInstance> annotationElems() {
        return this.erased().annotationElems();
    }
    
}
