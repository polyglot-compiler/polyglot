package polyglot.ext.jl5.types;

import java.util.*;

import polyglot.ext.param.types.PClass;
import polyglot.frontend.Source;
import polyglot.types.*;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.TypedList;

@SuppressWarnings("serial")
public class JL5ParsedClassType_c extends ParsedClassType_c implements JL5ParsedClassType {
    protected PClass pclass;
    protected List<TypeVariable> typeVars = Collections.EMPTY_LIST;
//	protected boolean wasGeneric;
	protected List<EnumInstance> enumConstants;

    public JL5ParsedClassType_c( TypeSystem ts, LazyClassInitializer init, Source fromSource){
        super(ts, init, fromSource);
    }
    
//    public boolean wasGeneric() {
//    	return wasGeneric;
//    }
    
//    public void setWasGeneric(boolean wg) {
//    	this.wasGeneric = wg;
//    }
    
    public void addEnumConstant(EnumInstance ei){
    	addField(ei);
        enumConstants().add(ei);
    }

    public List<EnumInstance> enumConstants(){
        if (enumConstants == null){
            enumConstants = new TypedList(new LinkedList(), 
            		EnumInstance.class, false);
        }    
        return enumConstants;
    }
   
    public EnumInstance enumConstantNamed(String name){
        for(Iterator it = enumConstants().iterator(); it.hasNext();){
            EnumInstance ei = (EnumInstance)it.next();
            if (ei.name().equals(name)){
                return ei;
            }
        }
        return null;
    }
    
	// find methods with compatible name and formals as the given one
    public List methods(JL5MethodInstance mi) {
        List l = new LinkedList();

        for (Object o : methodsNamed(mi.name())) {
        	ProcedureInstance pi = (ProcedureInstance) o;
            if (pi.hasFormals(mi.formalTypes())) {
                l.add(pi);
            }
        }
	return l;
    }
    
    @Override
    public boolean isCastValidImpl(Type toType){        
        if (super.isCastValidImpl(toType)) {
            return true;
        }
        return (this.isSubtype(toType) || toType.isSubtype(this));
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType){
        throw new InternalCompilerError("Should not be called in JL5");
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        LinkedList<Type> chain = null;
        if (ts.isSubtype(this, toType)) {   
            chain = new LinkedList<Type>();
            chain.add(this);
            chain.add(toType);
        }
        else if (toType.isPrimitive()) {
            // see if unboxing will let us cast to the primitive
            PrimitiveType pt = toType.toPrimitive();
            ClassType wrapperType = ts.wrapperClassOfPrimitive(pt);
            chain = ts.isImplicitCastValidChain(this, wrapperType);
            if (chain != null) {
                chain.addLast(toType);
            }
        }
        return chain;
    }

	// /////////////////////////////////////
	// 
    @Override
    public PClass pclass() {
        return pclass;
    }

    @Override
    public void setPClass(PClass pc) {
        this.pclass = pc;
    }

    @Override
    public void setTypeVariables(List<TypeVariable> typeVars) {
        if (typeVars == null) {
            this.typeVars = Collections.EMPTY_LIST;
        }
        else {
            this.typeVars = new TypedList(new ArrayList(typeVars), TypeVariable.class, true);
        }
    }
    @Override
    public List<TypeVariable> typeVariables() {
        return this.typeVars;
    }
    
    @Override
    public JL5Subst erasureSubst() {
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        return ts.erasureSubst(this.typeVariables());
    }


    /** Pretty-print the name of this class to w. */
    public void print(CodeWriter w) {
        // XXX This code duplicates the logic of toString.
        this.printNoParams(w);
        if (this.typeVars == null || this.typeVars.isEmpty()) {
            return;
        }
        w.write("<");
        Iterator<TypeVariable> it =  this.typeVars.iterator();
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
        Iterator<TypeVariable> it =  this.typeVars.iterator();
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
        return this.name();
    }
    
    @Override
    public String translate(Resolver c) {
        // it is a nested class of a parameterized class, use the full name.
        if (isMember()) {
            ClassType container = container().toClass(); 
            if (container instanceof JL5SubstClassType) {
                container = ((JL5SubstClassType)container).base();
            }
            if (container instanceof JL5ParsedClassType && !((JL5ParsedClassType)container).typeVariables().isEmpty()) {
                return container().translate(c) + "." + name();                
            }
        }
        return super.translate(c);
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {        
        if (super.descendsFromImpl(ancestor)) {
            return true;
        }
        if (!this.typeVariables().isEmpty()) {
            // check for raw class
            JL5TypeSystem ts = (JL5TypeSystem)this.ts;
            Type rawClass = ts.rawClass(this, this.position);
            if (ts.isSubtype(rawClass, ancestor)) {
                return true;
            }
        }
        return false;
    }
}
