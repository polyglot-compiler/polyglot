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

package polyglot.main;

/** This class encapsulates the version of the compiler. */
public abstract class Version {
    /** 
     * The name of the language.  Files produced by different languages
     * are not compatible.
     */
    public abstract String name();

    /** 
     * Marks major changes in the output format of the files produced by the
     * compiler. Files produced be different major versions are considered
     * incompatible and will not be used as source of class information.
     */
    public abstract int major();

    /** 
     * Indicates a change in the compiler that does not affect the output
     * format.  Source files will be prefered over class files build by
     * compilers with different minor versions, but if no source file is
     * available, then the class file will be used.
     */
    public abstract int minor();

    /**
     * Denote minor changes and bugfixes to the compiler. Class files compiled
     * with versions of the compiler that only differ in patchlevel (from the
     * current instantiation) will always be preferred over source files
     * (unless the source files have newer modification dates).
     */
    public abstract int patch_level();

    @Override
    public String toString() {
        return "" + major() + "." + minor() + "." + patch_level();
    }
}
