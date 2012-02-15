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
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JL5SubstClassType_c extends SubstClassType_c implements JL5SubstClassType
{
    public JL5SubstClassType_c(JL5TypeSystem ts, Position pos,
                                 JL5ParsedClassType base, JL5Subst subst) {
        super(ts, pos, base, subst);
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
        for(Iterator it = enumConstants().iterator(); it.hasNext();){
            EnumInstance ei = (EnumInstance)it.next();
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
    public String toString() {
        JL5ParsedClassType ct = this.base();
        if (ct.typeVariables().isEmpty()) {
            return ct.name();
        }
        StringBuffer sb = new StringBuffer(ct.name());
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
        return sb.toString();
    }

    @Override
    public boolean isRawClass() {
        return false;
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
        if (ts.isSubtype(this.base(), ancestor)) {
            return true;
        }
        
//        System.err.println("      F");
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
        JL5ParsedClassType ct = this.base();
        if (ct.typeVariables().isEmpty()) {
            return ct.name();
        }
        StringBuffer sb = new StringBuffer(ct.name());
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
        JL5ParsedClassType ct = this.base();
        return ct.name();
    }

}
