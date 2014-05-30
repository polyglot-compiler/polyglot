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
        setDeclaration(base);
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
        return base;
    }

    @Override
    public JL5SubstClassType inferred() {
        return inferred;
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
        return translate(resolver);
    }

    @Override
    public Annotations annotations() {
        return typeSystem().NoAnnotations();
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
        return base.translate(c) + "<>";
    }

    @Override
    public String toString() {
        return base.toString() + "<>";
    }
}
