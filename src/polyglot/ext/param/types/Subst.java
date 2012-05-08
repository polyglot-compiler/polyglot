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
import java.io.*;

/**
 * Utility class that performs substitutions on type objects.
 */
public interface Subst extends Serializable {
	/**
	 * Entries of the underlying substitution map.
	 * 
	 * @return An <code>Iterator</code> of <code>Map.Entry</code>.
	 */
	public Iterator entries();

	/** Type system */
	public ParamTypeSystem typeSystem();

	/** Get the map of formals to actuals. */
	public Map substitutions();

	/** Perform substitutions on a type. */
	public Type substType(Type t);

	/** Perform substitutions on a PClass. */
	public PClass substPClass(PClass pc);

	/** Perform substititions on a field. */
	public FieldInstance substField(FieldInstance fi);

	/** Perform substititions on a method. */
	public MethodInstance substMethod(MethodInstance mi);

	/** Perform substititions on a constructor. */
	public ConstructorInstance substConstructor(ConstructorInstance ci);

	/** Perform substitutions on a list of types. */
	public List substTypeList(List list);

	/** Perform substitutions on a list of methods. */
	public List substMethodList(List list);

	/** Perform substitutions on a list of constructors. */
	public List substConstructorList(List list);

	/** Perform substitutions on a list of fields. */
	public List substFieldList(List list);
}
