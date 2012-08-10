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

package polyglot.frontend.goals;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.passes.TypeExistsPass;
import polyglot.types.TypeSystem;

/**
 * Comment for <code>TypeExists</code>
 *
 * @author nystrom
 */
public class TypeExists extends AbstractGoal {
    public static Goal create(Scheduler scheduler, String name) {
        return scheduler.internGoal(new TypeExists(name));
    }

    protected String typeName;

    protected TypeExists(String name) {
        super(null);
        this.typeName = name;
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        return new TypeExistsPass(extInfo.scheduler(), ts, this);
    }

    public String typeName() {
        return typeName;
    }

    @Override
    public int hashCode() {
        return typeName.hashCode() + super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TypeExists
                && ((TypeExists) o).typeName.equals(typeName)
                && super.equals(o);
    }

    @Override
    public String toString() {
        return "TypeExists(" + typeName + ")";
    }
}
