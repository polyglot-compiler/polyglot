package polyglot.ext.jl7.types;

import java.util.List;
import java.util.Set;

import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ClassType_c;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
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
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class DiamondType_c extends JL5ClassType_c implements DiamondType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected JL5ParsedClassType base;
    protected transient JL5SubstClassType inferred;

    public DiamondType_c(Position pos, JL5ParsedClassType base) {
        super((JL7TypeSystem) base.typeSystem(), pos);
        this.base = base;
        this.setDeclaration(base);
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public JL5ParsedClassType base() {
        return this.base;
    }

    @Override
    public JL5SubstClassType inferred() {
        return this.inferred;
    }

    @Override
    public void inferred(JL5SubstClassType inferred) {
        this.inferred = inferred;
    }

    @Override
    public AnnotationTypeElemInstance annotationElemNamed(String name) {
        return this.inferred().annotationElemNamed(name);
    }

    @Override
    public List<AnnotationTypeElemInstance> annotationElems() {
        return this.inferred().annotationElems();
    }

    @Override
    public String translateAsReceiver(Resolver resolver) {
        return this.translate(resolver);
    }

    @Override
    public Annotations annotations() {
        return ((JL7TypeSystem) this.typeSystem()).NoAnnotations();
    }

    @Override
    public Set<? extends Type> superclasses() {
        return this.inferred().superclasses();
    }

    @Override
    public boolean inStaticContext() {
        return this.inferred().inStaticContext();
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
    public List<EnumInstance> enumConstants() {
        return this.inferred().enumConstants();
    }

    @Override
    public Job job() {
        return null;
    }

    @Override
    public Kind kind() {
        return this.inferred().kind();
    }

    @Override
    public ClassType outer() {
        return this.inferred().outer();
    }

    @Override
    public String name() {
        return this.inferred().name();
    }

    @Override
    public Package package_() {
        return this.inferred().package_();
    }

    @Override
    public Flags flags() {
        return this.inferred().flags();
    }

    private transient List<? extends ConstructorInstance> constructors = null;

    @Override
    public List<? extends ConstructorInstance> constructors() {
        if (constructors == null) {
            this.constructors = this.inferred().constructors();
        }
        return this.constructors;
    }

    private transient List<? extends ClassType> memberClasses = null;

    @Override
    public List<? extends ClassType> memberClasses() {
        if (memberClasses == null) {
            this.memberClasses = this.inferred().memberClasses();
        }
        return this.memberClasses;
    }

    private transient List<? extends MethodInstance> methods = null;

    @Override
    public List<? extends MethodInstance> methods() {
        if (methods == null) {
            this.methods = this.inferred().methods();
        }
        return this.methods;
    }

    private transient List<? extends FieldInstance> fields = null;

    @Override
    public List<? extends FieldInstance> fields() {
        if (fields == null) {
            this.fields = this.inferred().fields();
            for (FieldInstance fi : this.fields) {
                fi.setContainer(this);
            }
        }
        return this.fields;
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        return this.inferred().interfaces();
    }

    @Override
    public Type superType() {
        return this.inferred().superType();
    }
}
