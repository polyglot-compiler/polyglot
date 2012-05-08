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

package polyglot.ext.param.types;

import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.*;

import java.util.*;

/**
 * A base implementation for parametric classes. This class is a wrapper around
 * a ClassType that associates formal parameters with the class. formals can be
 * any type object.
 */
public abstract class PClass_c extends TypeObject_c implements PClass {
	protected PClass_c() {
	}

	public PClass_c(TypeSystem ts) {
		this(ts, null);
	}

	public PClass_c(TypeSystem ts, Position pos) {
		super(ts, pos);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Implement PClass

	public ClassType instantiate(Position pos, List actuals)
			throws SemanticException {
		ParamTypeSystem pts = (ParamTypeSystem) typeSystem();
		return pts.instantiate(pos, this, actuals);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Implement TypeObject

	public boolean isCanonical() {
		if (!clazz().isCanonical()) {
			return false;
		}

		for (Iterator i = formals().iterator(); i.hasNext();) {
			Param p = (Param) i.next();
			if (!p.isCanonical()) {
				return false;
			}
		}

		return true;
	}

	// ///////////////////////////////////////////////////////////////////////
	// Implement Named

	public String name() {
		return clazz().name();
	}

	public String fullName() {
		return clazz().fullName();
	}

	// ///////////////////////////////////////////////////////////////////////
	// Implement Importable

	public Package package_() {
		return clazz().package_();
	}

}
