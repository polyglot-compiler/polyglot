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

package polyglot.frontend;

import java.util.Date;

/** A <code>Source</code> represents a source file. */
public class Source
{
    protected String name;
    protected String path;
    protected Date lastModified;
    
    /**
     * Indicates if this source was explicitly specified by the user,
     * or if it a source that has been drawn in to the compilation process
     * due to a dependency.
     */
    protected boolean userSpecified;

    protected Source(String name) {
        this(name, null, null, false);
    }

    protected Source(String name, boolean userSpecified) {
        this(name, null, null, userSpecified);
    }

    public Source(String name, String path, Date lastModified) {
        this(name, path, lastModified, false);
    }
    
    public Source(String name, String path, Date lastModified, boolean userSpecified) {
	this.name = name;
        this.path = path;
	this.lastModified = lastModified;
        this.userSpecified = userSpecified;   
    }

    public boolean equals(Object o) {
	if (o instanceof Source) {
	    Source s = (Source) o;
	    return name.equals(s.name) && 
                 (path == s.path || (path != null && path.equals(s.path)));
	}

	return false;
    }

    public int hashCode() {
	return (path==null?0:path.hashCode()) ^ name.hashCode();
    }

    /** The name of the source file. */
    public String name() {
	return name;
    }

    /** The path of the source file. */
    public String path() {
	return path;
    }

    /** Return the date the source file was last modified. */
    public Date lastModified() {
	return lastModified;
    }

    public String toString() {
	return path;
    }
    
    public void setUserSpecified(boolean userSpecified) {
        this.userSpecified = userSpecified;
    }
    
    public boolean userSpecified() {
        return userSpecified;
    }
}
