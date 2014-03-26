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

import polyglot.ext.jl5.JL5Scheduler;
import polyglot.ext.jl5.types.reflect.JL5LazyClassInitializer;
import polyglot.frontend.MissingDependencyException;
import polyglot.types.SchedulerClassInitializer;
import polyglot.types.TypeSystem;

public class JL5SchedulerClassInitializer extends SchedulerClassInitializer
        implements JL5LazyClassInitializer {

    public JL5SchedulerClassInitializer(TypeSystem ts) {
        super(ts);
    }

    protected boolean annotationElemsInitialized;
    protected boolean annotationInitialized;
    protected boolean enumConstantsInitialized;

    @Override
    public void initAnnotationElems() {
        if (!annotationElemsInitialized) {
            if (ct.membersAdded()) {
                this.annotationElemsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }

    @Override
    public void initAnnotations() {
        if (!annotationInitialized) {
            JL5ParsedClassType pct = (JL5ParsedClassType) ct;
            if (pct.annotationsResolved()) {
                this.annotationInitialized = true;
            }
            else {
                JL5Scheduler scheduler = (JL5Scheduler) this.scheduler;
                throw new MissingDependencyException(scheduler.AnnotationsResolved(ct));
            }
        }
    }

    @Override
    public void initEnumConstants() {
        if (!enumConstantsInitialized) {
            if (ct.membersAdded()) {
                this.enumConstantsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }
}
