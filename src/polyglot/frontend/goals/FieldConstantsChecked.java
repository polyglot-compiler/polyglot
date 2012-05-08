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

import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.CheckFieldConstantsPass;
import polyglot.frontend.passes.ConstantCheckPass;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;
import polyglot.visit.ConstantChecker;
import polyglot.visit.TypeBuilder;

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

		ParsedClassType ct = (ParsedClassType) findContainer();
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

	public Pass createPass(ExtensionInfo extInfo) {
		return new CheckFieldConstantsPass(extInfo.scheduler(), this);
	}

	public Collection prerequisiteGoals(Scheduler scheduler) {
		List l = new ArrayList();
		if (ct != null) {
			l.add(scheduler.SignaturesResolved(ct));
		}
		l.addAll(super.prerequisiteGoals(scheduler));
		return l;
	}

	public Collection corequisiteGoals(Scheduler scheduler) {
		List l = new ArrayList();
		if (ct != null && ct.job() != null) {
			l.add(scheduler.TypeChecked(ct.job()));
		}
		l.addAll(super.corequisiteGoals(scheduler));
		return l;
	}

	public FieldInstance var() {
		return vi;
	}

	public int hashCode() {
		return vi.hashCode() + super.hashCode();
	}

	public boolean equals(Object o) {
		return o instanceof FieldConstantsChecked
				&& ((FieldConstantsChecked) o).vi.equals(vi) && super.equals(o);
	}

	public String toString() {
		return StringUtil.getShortNameComponent(getClass().getName()) + "("
				+ vi + ")";
	}
}
