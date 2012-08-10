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
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.VisitorPass;
import polyglot.util.StringUtil;
import polyglot.visit.NodeVisitor;

public class VisitorGoal extends SourceFileGoal {
    protected NodeVisitor v;

    public VisitorGoal(Job job, NodeVisitor v) {
        super(job);
        this.v = v;
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        return new VisitorPass(this, v);
    }

    public NodeVisitor visitor() {
        return v;
    }

    @Override
    public int hashCode() {
        return job().hashCode() + visitor().getClass().hashCode()
                + getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VisitorGoal) {
            VisitorGoal g = (VisitorGoal) o;
            return job().equals(g.job())
                    && visitor().getClass() == g.visitor().getClass()
                    && this.getClass() == o.getClass();
        }
        return false;
    }

    @Override
    public String toString() {
        return job() + ":"
                + StringUtil.getShortNameComponent(getClass().getName()) + ":"
                + visitor() + " (" + stateString() + ")";
    }
}
