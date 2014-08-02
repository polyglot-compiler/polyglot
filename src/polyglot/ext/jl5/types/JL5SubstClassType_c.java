/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ext.param.types.PClass;
import polyglot.ext.param.types.SubstClassType_c;
import polyglot.main.Options;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Named;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class JL5SubstClassType_c extends
        SubstClassType_c<TypeVariable, ReferenceType> implements
        JL5SubstClassType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5SubstClassType_c(JL5TypeSystem ts, Position pos,
            JL5ParsedClassType base, JL5Subst subst) {
        super(ts, pos, base, subst);
        this.setDeclaration(base);
    }

    ////////////////////////////////////////////////////////////////
    // Implement methods of JL5SubstType

    @Override
    public PClass<TypeVariable, ReferenceType> instantiatedFrom() {
        return base().pclass();
    }

    @Override
    public List<ReferenceType> actuals() {
        PClass<TypeVariable, ReferenceType> pc = instantiatedFrom();
        JL5Subst subst = (JL5Subst) this.subst;

        return subst.substTypeList(pc.formals());
    }

    ////////////////////////////////////////////////////////////////
    // Implement methods of JL5ClassType
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
    public List<EnumInstance> enumConstants() {
        return subst.substFieldList(((JL5ClassType) base).enumConstants());
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
    public List<AnnotationTypeElemInstance> annotationElems() {
        return ((JL5ClassType) this.base).annotationElems();
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
        LinkedList<Type> chain = null;
        if (ts.isSubtype(this, toType)) {
            chain = new LinkedList<>();
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

        JL5TypeSystem ts = (JL5TypeSystem) this.ts;

//        System.err.println("jl5substclasstype: descends from " + this + " <: " + ancestor);
//        System.err.println("    superclass of  " + this + " is " + this.superType());
//        System.err.println("    base class of  " + this + " is " + this.base());
//        System.err.println("       super of " + this.base() + " is " + this.base().superType());
//        System.err.println("    subst  of  " + this + " is " + this.subst());
//        System.err.println("   applying subst " + subst.substType(this.base()) + " super " + ((ReferenceType)subst.substType(this.base())).superType());

        // See JLS 3rd ed 4.10.2
        if (hasWildCardArg()) {
            Type captured;
            try {
                captured = ts.applyCaptureConversion(this, null);
                // Note: we want descendsFrom, not isSubtype, since the direct ancestors of this class
                // are the direct ancestors of captured, but not captured itself.
                if (ts.descendsFrom(captured, ancestor)) {
                    return true;
                }
            }
            catch (SemanticException e) {
                // nope, can't apply capture conversion.
            }
        }

        if (ancestor instanceof RawClass) {
            // it's a raw class! Is it our raw class?
            RawClass rc = (RawClass) ancestor;
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
                // go through each type variable, and check containment
                boolean allContained = true;
                for (TypeVariable tv : ts.classAndEnclosingTypeVariables(base())) {
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
            Type substType = subst.substType(t);
            if (substType instanceof WildCardType
                    && !(substType instanceof CaptureConvertedWildCardType)) {
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
        return translate(this.base(), c);
    }

    private String translate(JL5ParsedClassType ct, Resolver c) {
        StringBuffer sb = new StringBuffer();
        if (ct.isTopLevel()) {
            boolean done = false;
            if (ct.package_() == null) {
                sb.append(ct.name());
                done = true;
            }

            // Use the short name if it is unique.
            else if (c != null && !Options.global.fully_qualified_names) {
                try {
                    Named x = c.find(ct.name());

                    if (ts.equals(ct, x)) {
                        sb.append(ct.name());
                        done = true;
                    }
                }
                catch (SemanticException e) {
                }
            }

            if (!done) sb.append(ct.package_().translate(c) + "." + ct.name());
        }
        else if (ct.isMember()) {
            boolean done = false;
            // Use only the short name if the outer class is anonymous.
            if (ct.container().toClass().isAnonymous()) {
                sb.append(ct.name());
                done = true;
            }

            // Use the short name if it is unique and not an inner class
            // whose containing class does not descend from outer class.
            else if (c != null && !Options.global.fully_qualified_names) {
                boolean toTry = true;
                if (ct.isInnerClass() && c instanceof Context) {
                    JL5TypeSystem ts = (JL5TypeSystem) typeSystem();
                    Context ctx = (Context) c;
                    ClassType currentClass = ctx.currentClass();
                    ClassType outer = subst().substType(ct.outer()).toClass();
                    if (!ts.isSubtype(currentClass, outer)) toTry = false;
                }

                if (toTry) {
                    try {
                        Named x = c.find(ct.name());

                        if (ts.equals(ct, x)) {
                            sb.append(ct.name());
                            done = true;
                        }
                    }
                    catch (SemanticException e) {
                    }
                }
            }

            if (!done) {
                if (ct.isInnerClass()) {
                    // If ct is inner class, need to translate this substitution on the outer class.
                    sb.append(translate((JL5ParsedClassType) ct.outer(), c)
                            + "." + ct.name());
                }
                else sb.append(ct.outer().translate(c) + "." + ct.name());
            }
        }
        else if (isLocal()) {
            sb.append(ct.name());
        }
        else {
            throw new InternalCompilerError("Cannot translate an anonymous class: "
                                                    + ct,
                                            ct.position());
        }

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
                JL5TypeSystem ts = (JL5TypeSystem) this.ts;
                container = (ReferenceType) ts.erasureType(this.container());
            }
            return container.translate(c) + "." + name();
        }
        else if (isLocal()) {
            return name();
        }
        else {
            throw new InternalCompilerError("Cannot translate an anonymous class: "
                                                    + this,
                                            this.position());
        }
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
    public Annotations annotations() {
        return ((JL5TypeSystem) this.typeSystem()).NoAnnotations();
    }

    @Override
    public Set<Type> superclasses() {
        if (this.superType() == null) {
            return Collections.<Type> emptySet();
        }
        return Collections.singleton(this.superType());
    }
}
