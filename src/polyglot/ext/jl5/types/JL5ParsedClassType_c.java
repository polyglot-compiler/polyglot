package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ext.jl5.types.reflect.JL5LazyClassInitializer;
import polyglot.ext.param.types.PClass;
import polyglot.frontend.Source;
import polyglot.types.ClassType;
import polyglot.types.LazyClassInitializer;
import polyglot.types.ParsedClassType_c;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;

@SuppressWarnings("serial")
public class JL5ParsedClassType_c extends ParsedClassType_c implements
        JL5ParsedClassType {
    protected PClass<TypeVariable, ReferenceType> pclass;
    protected List<TypeVariable> typeVars = Collections.emptyList();
    protected List<EnumInstance> enumConstants;
    protected List<AnnotationTypeElemInstance> annotationElems;
    protected RetainedAnnotations retainedAnnotations;

    public JL5ParsedClassType_c(TypeSystem ts, LazyClassInitializer init,
            Source fromSource) {
        super(ts, init, fromSource);
        annotationElems = new LinkedList<AnnotationTypeElemInstance>();
    }

    @Override
    public void addEnumConstant(EnumInstance ei) {
        addField(ei);
        enumConstants().add(ei);
    }

    @Override
    public List<EnumInstance> enumConstants() {
        if (enumConstants == null) {
            enumConstants = new LinkedList<EnumInstance>();
        }
        return enumConstants;
    }

    @Override
    public EnumInstance enumConstantNamed(String name) {
        for (EnumInstance ei : enumConstants()) {
            if (ei.name().equals(name)) {
                return ei;
            }
        }
        return null;
    }

    @Override
    public AnnotationTypeElemInstance annotationElemNamed(String name) {
        for (AnnotationTypeElemInstance ai : annotationElems()) {
            if (ai.name().equals(name)) {
                return ai;
            }
        }
        return null;
    }

    @Override
    public void addAnnotationElem(AnnotationTypeElemInstance ai) {
        addMethod(ai);
        annotationElems.add(ai);
    }

    @Override
    public List<AnnotationTypeElemInstance> annotationElems() {

        ((JL5LazyClassInitializer) init).initAnnotationElems();
        return Collections.unmodifiableList(annotationElems);
    }

    // find methods with compatible name and formals as the given one
    @Override
    public List<? extends JL5MethodInstance> methods(JL5MethodInstance mi) {
        List<JL5MethodInstance> l = new LinkedList<JL5MethodInstance>();

        for (JL5MethodInstance pi : methodsNamed(mi.name())) {
            if (pi.hasFormals(mi.formalTypes())) {
                l.add(pi);
            }
        }
        return l;
    }

    @Override
    public List<JL5MethodInstance> methodsNamed(String name) {
        @SuppressWarnings("unchecked")
        List<JL5MethodInstance> result =
                (List<JL5MethodInstance>) super.methodsNamed(name);
        return result;
    }

    @Override
    public ClassType outer() {
        if (this.isMember() && !this.isInnerClass()) {
            if (!(super.outer() instanceof RawClass)) {
                JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
                return (ClassType) ts.erasureType(super.outer());
            }
        }
        return super.outer();
    }

    @Override
    public boolean isEnclosedImpl(ClassType maybe_outer) {
        if (super.isEnclosedImpl(maybe_outer)) {
            return true;
        }
        // try it with the stripped out outer...
        if (outer() != null && super.outer() != this.outer()) {
            return super.outer().equals(maybe_outer)
                    || super.outer().isEnclosed(maybe_outer);
        }
        return false;
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        if (super.isCastValidImpl(toType)) {
            return true;
        }
        return (this.isSubtype(toType) || toType.isSubtype(this));
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        throw new InternalCompilerError("Should not be called in JL5");
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        LinkedList<Type> chain = null;
        if (ts.isSubtype(this, toType)) {
            chain = new LinkedList<Type>();
            chain.add(this);
            chain.add(toType);
        }
        else if (toType.isPrimitive()) {
            // see if unboxing will let us cast to the primitive
            if (ts.primitiveTypeOfWrapper(this) != null) {
                chain =
                        ts.isImplicitCastValidChain(ts.primitiveTypeOfWrapper(this),
                                                    toType);
                if (chain != null) {
                    chain.addFirst(this);
                }
            }
        }
        return chain;
    }

    // /////////////////////////////////////
    //
    @Override
    public PClass<TypeVariable, ReferenceType> pclass() {
        return pclass;
    }

    @Override
    public void setPClass(PClass<TypeVariable, ReferenceType> pc) {
        this.pclass = pc;
    }

    @Override
    public void setTypeVariables(List<TypeVariable> typeVars) {
        if (typeVars == null) {
            this.typeVars = Collections.emptyList();
        }
        else {
            this.typeVars = ListUtil.copy(typeVars, true);
        }
    }

    @Override
    public List<TypeVariable> typeVariables() {
        return this.typeVars;
    }

    @Override
    public JL5Subst erasureSubst() {
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        return ts.erasureSubst(this);
    }

    /** Pretty-print the name of this class to w. */
    @Override
    public void print(CodeWriter w) {
        // XXX This code duplicates the logic of toString.
        this.printNoParams(w);
        if (this.typeVars == null || this.typeVars.isEmpty()) {
            return;
        }
        w.write("<");
        Iterator<TypeVariable> it = this.typeVars.iterator();
        while (it.hasNext()) {
            TypeVariable act = it.next();
            w.write(act.name());
            if (it.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.write(">");
    }

    @Override
    public void printNoParams(CodeWriter w) {
        super.print(w);
    }

    @Override
    public String toStringNoParams() {
        return super.toString();
    }

    @Override
    public String toString() {
        if (this.typeVars == null || this.typeVars.isEmpty()) {
            return super.toString();
        }
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append('<');
        Iterator<TypeVariable> it = this.typeVars.iterator();
        while (it.hasNext()) {
            TypeVariable act = it.next();
            sb.append(act);
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public String translateAsReceiver(Resolver c) {
        return super.translate(c);
    }

    @Override
    public String translate(Resolver c) {
        StringBuffer sb = new StringBuffer(super.translate(c));
        if (this.typeVariables().isEmpty()) {
            return sb.toString();
        }
        sb.append('<');
        Iterator<TypeVariable> iter = typeVariables().iterator();
        while (iter.hasNext()) {
            TypeVariable act = iter.next();
            sb.append(act.translate(c));
            if (iter.hasNext()) {
                sb.append(',');
            }
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (super.descendsFromImpl(ancestor)) {
            return true;
        }
        if (!this.typeVariables().isEmpty()) {
            // check for raw class
            JL5TypeSystem ts = (JL5TypeSystem) this.ts;
            Type rawClass = ts.rawClass(this, this.position);
            if (ts.isSubtype(rawClass, ancestor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RetainedAnnotations retainedAnnotations() {
        return this.retainedAnnotations;
    }

    @Override
    public void setRetainedAnnotations(RetainedAnnotations retainedAnnotations) {
        this.retainedAnnotations = retainedAnnotations;
    }

}
