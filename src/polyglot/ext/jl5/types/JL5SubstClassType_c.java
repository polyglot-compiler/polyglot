/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ext.jl5.types;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ext.param.types.PClass;
import polyglot.ext.param.types.SubstClassType_c;
import polyglot.main.Options;
import polyglot.types.*;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JL5SubstClassType_c extends SubstClassType_c<TypeVariable, ReferenceType> implements JL5SubstClassType
{
    public JL5SubstClassType_c(JL5TypeSystem ts, Position pos,
                                 JL5ParsedClassType base, JL5Subst subst) {
        super(ts, pos, base, subst);
        this.setDeclaration(base);
    }

    ////////////////////////////////////////////////////////////////
    // Implement methods of JL5SubstType

    @Override
    public PClass instantiatedFrom() {
        return base().pclass();
    }

    @Override
    public List actuals() {
        PClass pc = instantiatedFrom();
        JL5Subst subst = (JL5Subst) this.subst;

        return subst.substTypeList(pc.formals());
    }


    ////////////////////////////////////////////////////////////////
    // Implement methods of JL5ClassType
    @Override
    public EnumInstance enumConstantNamed(String name) {
        for(EnumInstance ei : enumConstants()){
            if (ei.name().equals(name)){
                return ei;
            }
        }
        return null;
    }

    @Override
    public List<EnumInstance> enumConstants() {
        return subst.substFieldList(((JL5ClassType)base).enumConstants());
    }
    
    @Override
    public AnnotationElemInstance annotationElemNamed(String name) {
        for(AnnotationElemInstance ai : annotationElems()){
            if (ai.name().equals(name)){
                return ai;
            }
        }
        return null;
    }

    @Override
    public List<AnnotationElemInstance> annotationElems() {
        return ((JL5ClassType)this.base).annotationElems();
    }


    /** Pretty-print the name of this class to w. */
    @Override
    public void print(CodeWriter w) {
        super.print(w);
        this.printParams(w);
    }

    @Override
    public void printParams(CodeWriter w) {        
        JL5ParsedClassType ct = this.base();
        if (ct.typeVariables().isEmpty()) {
            return;
        }
        w.write("<");
        Iterator<TypeVariable> it = ct.typeVariables().iterator();
        while (it.hasNext()) {
            TypeVariable act = it.next();            
            this.subst().substType(act).print(w);
            if (it.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.write(">");                
    }
    
    @Override
    public String toString() {
        // really want to call ClassType_c.toString here, but we will copy code :(
        StringBuffer sb = new StringBuffer();
        if (isTopLevel()) {
            if (package_() != null) {
                sb.append(package_() + ".");
            }
            sb.append(name());
        }
        else if (isMember()) {
            sb.append(container().toString() + "." + name());
        }
        else if (isLocal()) {
            sb.append(name());
        }
        else if (isAnonymous()) {
            sb.append("<anonymous class>");
        }
        else {
            sb.append("<unknown class>");
        }

        // now append the parameters.
        JL5ParsedClassType ct = this.base();
        if (!ct.typeVariables().isEmpty()) {
            sb.append('<');
            Iterator<TypeVariable> iter = ct.typeVariables().iterator();
            while (iter.hasNext()) {
                TypeVariable act = iter.next();            
                sb.append(this.subst().substType(act));
                if (iter.hasNext()) {
                    sb.append(',');                    
                }
            }
            sb.append('>');
        }
        return sb.toString();
    }

    @Override
    public boolean isRawClass() {
        return false;
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
        LinkedList<Type> chain = null;
        if (ts.isSubtype(this, toType)) {
            chain = new LinkedList<Type>();
            chain.add(this);
            chain.add(toType);
        }
        return chain;
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (super.descendsFromImpl(ancestor)) {
            return true;
        }

        JL5TypeSystem ts = (JL5TypeSystem)this.ts;
        
//        System.err.println("jl5substclasstype: descends from " + this + " <: " + ancestor);
//        System.err.println("    superclass of  " + this + " is " + this.superType());
//        System.err.println("    base class of  " + this + " is " + this.base());
//        System.err.println("       super of " + this.base() + " is " + this.base().superType());
//        System.err.println("    subst  of  " + this + " is " + this.subst());
//        System.err.println("   applying subst " + subst.substType(this.base()) + " super " + ((ReferenceType)subst.substType(this.base())).superType());
        
        // See JLS 3rd ed 4.10.2
        if (hasWildCardArg()) {
            Type captured = ts.applyCaptureConversion(this);
            // Note: we want descendsFrom, not isSubtype, since the direct ancestors of this class
            // are the direct ancestors of captured, but not captured itself.
//            System.err.println("      A");
            if (ts.descendsFrom(captured, ancestor)) {
//                System.err.println("      B");
                return true;
            }
        }
        
        if (ancestor instanceof RawClass) {
            // it's a raw class! Is it our raw class?
            RawClass rc = (RawClass)ancestor;
            if (this.base().equals(rc.base())) {
                // The raw type C is a direct supertype of C<X>
                return true;
            }
        }
        if (ancestor instanceof JL5SubstClassType_c) {
            JL5SubstClassType_c anc = (JL5SubstClassType_c) ancestor;
//            System.err.println("      C");
            if (this.base.equals(anc.base)) {
//                System.err.println("      D");
                // same base. check the params
                JL5ParsedClassType base = (JL5ParsedClassType) this.base;
                // go through each type variable, and check containment
                boolean allContained = true;
                for (TypeVariable tv : base.typeVariables()) {
                    Type ti = this.subst.substType(tv);
                    Type si = anc.subst.substType(tv);
//                    System.err.println("      E " + ti + " contained in "+si+" ? " + ts.isContained(ti, si));
                    if (!ts.isContained(ti, si)) {
                        allContained = false;
                        break;
                    }
                }
                if (allContained) {
                    return true;
                }
                
            }
        }
        
        return false;
    }
    
    private boolean hasWildCardArg() {
        JL5ParsedClassType b = (JL5ParsedClassType) this.base;
        
        for (TypeVariable t : b.typeVariables()) {
            if (subst.substType(t) instanceof WildCardType) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JL5ParsedClassType base() {
        return (JL5ParsedClassType) this.base;
    }
    
    @Override
    public String translate(Resolver c) {
        StringBuffer sb = new StringBuffer(this.translateAsReceiver(c));
        JL5ParsedClassType ct = this.base();
        if (ct.typeVariables().isEmpty()) {
            return sb.toString();
        }
        sb.append('<');
        Iterator<TypeVariable> iter = ct.typeVariables().iterator();
        while (iter.hasNext()) {
            TypeVariable act = iter.next();            
            sb.append(this.subst().substType(act).translate(c));
            if (iter.hasNext()) {
                sb.append(',');                    
            }
        }
        sb.append('>');
        return sb.toString();
    }

    @Override
    public String translateAsReceiver(Resolver c) {
        if (isTopLevel()) {
            if (package_() == null) {
                return name();
            }

            // Use the short name if it is unique.
            if (c != null && !Options.global.fully_qualified_names) {
                try {
                    Named x = c.find(name());

                    if (ts.equals(this, x)) {
                        return name();
                    }
                }
                catch (SemanticException e) {
                }
            }

            return package_().translate(c) + "." + name();
        }
        else if (isMember()) {
            // Use only the short name if the outer class is anonymous.
            if (container().toClass().isAnonymous()) {
                return name();
            }

            // never use the short name for a member class if we need to perform substitution...
            ReferenceType container = this.container();
            if (!this.isInnerClass()) {
                // if we are not an inner class (i.e., we are
                // a static nested class), then make sure that we
                // do not print out the parameters for our container.
                JL5TypeSystem ts = (JL5TypeSystem)this.ts;
                container = (ReferenceType)ts.erasureType(this.container());
            }
            return container.translate(c) + "." + name();
        }
        else if (isLocal()) {
            return name();
        }
        else {
            throw new InternalCompilerError("Cannot translate an anonymous class.");
        }
    }

    @Override
    public ClassType outer() {
        if (this.isMember() && !this.isInnerClass()) {
            if (!(super.outer() instanceof RawClass)) {
                JL5TypeSystem ts = (JL5TypeSystem)this.typeSystem();
                return (ClassType)ts.erasureType(super.outer());
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
            return super.outer().equals(maybe_outer) ||
                    super.outer().isEnclosed(maybe_outer);
        }
        return false;
    }


    
}
