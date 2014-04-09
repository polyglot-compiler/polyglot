package polyglot.ext.jl7.types;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ClassType;
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

    protected JL5ClassType mostSpecific() {
        if (inferred != null) return inferred;
        return base;
    }

    @Override
    public JL7TypeSystem typeSystem() {
        return (JL7TypeSystem) super.typeSystem();
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
        return this.typeSystem().NoAnnotations();
    }

    @Override
    public Set<? extends Type> superclasses() {
        return mostSpecific().superclasses();
    }

    @Override
    public boolean inStaticContext() {
        return mostSpecific().inStaticContext();
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
        return mostSpecific().kind();
    }

    @Override
    public ClassType outer() {
        return mostSpecific().outer();
    }

    @Override
    public String name() {
        return mostSpecific().name();
    }

    @Override
    public Package package_() {
        return mostSpecific().package_();
    }

    @Override
    public Flags flags() {
        return mostSpecific().flags();
    }

    @Override
    public List<? extends ConstructorInstance> constructors() {
        return mostSpecific().constructors();
    }

    @Override
    public List<? extends ClassType> memberClasses() {
        return mostSpecific().memberClasses();
    }

    @Override
    public List<? extends MethodInstance> methods() {
        return mostSpecific().methods();
    }

    @Override
    public List<? extends FieldInstance> fields() {
        return this.inferred().fields();
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        return mostSpecific().interfaces();
    }

    @Override
    public Type superType() {
        return mostSpecific().superType();
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
        return typeSystem().isImplicitCastValidChain(inferred, toType);
    }

    @Override
    public String translate(Resolver c) {
        return super.translate(c) + "<>";
    }

    @Override
    public String toString() {
        return super.toString() + "<>";
    }
}
