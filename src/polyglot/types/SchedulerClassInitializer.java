/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;

/**
 * A LazyClassInitializer is responsible for initializing members of a class
 * after it has been created. Members are initialized lazily to correctly handle
 * cyclic dependencies between classes.
 * 
 * SchedulerClassInitializer ensures that scheduler dependencies are enforced
 * when a ParsedClassType member is accessed.
 */
public class SchedulerClassInitializer implements LazyClassInitializer {
    protected TypeSystem ts;
    protected ParsedClassType ct;
    protected Scheduler scheduler;

    protected boolean init;
    protected boolean superclassInitialized;
    protected boolean interfacesInitialized;
    protected boolean memberClassesInitialized;
    protected boolean constructorsInitialized;
    protected boolean methodsInitialized;
    protected boolean fieldsInitialized;
    protected boolean constructorsCanonicalized;
    protected boolean methodsCanonicalized;
    protected boolean fieldsCanonicalized;

    public SchedulerClassInitializer(TypeSystem ts) {
        this.ts = ts;
        this.scheduler = ts.extensionInfo().scheduler();
    }

    @Override
    public void setClass(ParsedClassType ct) {
        this.ct = ct;
    }

    @Override
    public boolean fromClassFile() {
        return false;
    }

    @Override
    public void initTypeObject() {
        this.init = true;
    }

    @Override
    public boolean isTypeObjectInitialized() {
        return this.init;
    }

    @Override
    public void initSuperclass() {
        if (!superclassInitialized) {
            if (ct.supertypesResolved()) {
                this.superclassInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.SupertypesResolved(ct));
            }
        }
    }

    @Override
    public void initInterfaces() {
        if (!interfacesInitialized) {
            if (ct.supertypesResolved()) {
                this.interfacesInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.SupertypesResolved(ct));
            }
        }
    }

    @Override
    public void initMemberClasses() {
        if (!memberClassesInitialized) {
            if (ct.membersAdded()) {
                this.memberClassesInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }

    @Override
    public void canonicalConstructors() {
        if (!constructorsCanonicalized) {
            if (ct.signaturesResolved()) {
                this.constructorsCanonicalized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.SignaturesResolved(ct));
            }
        }
    }

    @Override
    public void canonicalMethods() {
        if (!methodsCanonicalized) {
            if (ct.signaturesResolved()) {
                this.methodsCanonicalized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.SignaturesResolved(ct));
            }
        }
    }

    @Override
    public void canonicalFields() {
        if (!fieldsCanonicalized) {
            if (ct.signaturesResolved()) {
                this.fieldsCanonicalized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.SignaturesResolved(ct));
            }
        }
    }

    @Override
    public void initConstructors() {
        if (!constructorsInitialized) {
            if (ct.membersAdded()) {
                this.constructorsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }

    @Override
    public void initMethods() {
        if (!methodsInitialized) {
            if (ct.membersAdded()) {
                this.methodsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }

    @Override
    public void initFields() {
        if (!fieldsInitialized) {
            if (ct.membersAdded()) {
                this.fieldsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }
}
