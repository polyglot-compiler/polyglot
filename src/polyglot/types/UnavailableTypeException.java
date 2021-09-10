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

package polyglot.types;

import polyglot.frontend.Job;
import polyglot.frontend.SchedulerException;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * An {@code UnavailableTypeException} is an exception thrown when a type
 * object is not in a required state to continue a pass.
 *
 * @author nystrom
 */
public class UnavailableTypeException extends SchedulerException {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Job job;
    protected Position position;

    /**
     * @param job
     * @param fullName
     */
    public UnavailableTypeException(Job job, String fullName) {
        this(job, fullName, null);
    }

    /**
     * @param job
     * @param fullName
     * @param position
     */
    public UnavailableTypeException(Job job, String fullName, Position position) {
        super(fullName);
        this.job = job;
        this.position = position;
    }

    public UnavailableTypeException(ParsedTypeObject ct) {
        this(ct.job(), ct.fullName(), ct.position());
    }

    public Job job() {
        return job;
    }

    public Position position() {
        return position;
    }
}
