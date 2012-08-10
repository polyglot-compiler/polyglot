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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.passes.CheckFieldConstantsPass;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * Comment for <code>MembersAdded</code>
 *
 * @author nystrom
 */
public class FieldConstantsChecked extends AbstractGoal {
    public static Goal create(Scheduler scheduler, FieldInstance fi) {
        return scheduler.internGoal(new FieldConstantsChecked(fi));
    }

    protected FieldInstance vi;
    protected ParsedClassType ct;

    protected FieldConstantsChecked(FieldInstance fi) {
        super(null);
        this.vi = fi.orig();

        ParsedClassType ct = findContainer();
        if (ct != null) {
            this.job = ct.job();
        }
        this.ct = ct;

        if (this.job == null && !fi.constantValueSet()) {
            throw new InternalCompilerError(this + " is unreachable.");
        }
    }

    public ParsedClassType container() {
        return ct;
    }

    protected ParsedClassType findContainer() {
        if (vi.container() instanceof ParsedClassType) {
            return (ParsedClassType) vi.container();
        }
        return null;
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        return new CheckFieldConstantsPass(extInfo.scheduler(), this);
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<Goal>();
        if (ct != null) {
            l.add(scheduler.SignaturesResolved(ct));
        }
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    @Override
    public Collection<Goal> corequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<Goal>();
        if (ct != null && ct.job() != null) {
            l.add(scheduler.TypeChecked(ct.job()));
        }
        l.addAll(super.corequisiteGoals(scheduler));
        return l;
    }

    public FieldInstance var() {
        return vi;
    }

    @Override
    public int hashCode() {
        return vi.hashCode() + super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FieldConstantsChecked
                && ((FieldConstantsChecked) o).vi.equals(vi) && super.equals(o);
    }

    @Override
    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName()) + "("
                + vi + ")";
    }
}
