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

import polyglot.frontend.Job;
import polyglot.util.Position;

/**
 * Comment for <code>SourceLoadException</code>
 *
 * @author nystrom
 */
public class SourceLoadException extends NoClassException {
    protected Job job;

    /**
     * @param className
     */
    public SourceLoadException(Job job, String className) {
        super(className);
        this.job = job;
    }

    /**
     * @param className
     * @param scope
     */
    public SourceLoadException(Job job, String className, Named scope) {
        super(className, scope);
        this.job = job;
    }

    /**
     * @param className
     * @param position
     */
    public SourceLoadException(Job job, String className, Position position) {
        super(className, position);
        this.job = job;
    }

    public Job job() {
        return job;
    }
}
