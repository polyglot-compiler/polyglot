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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.StringUtil;

/**
 * Loads member classes using a TopLevelResolver that can only handle
 * top-level classes.
 */
public class MemberClassResolver implements TopLevelResolver {
    protected TypeSystem ts;
    protected TopLevelResolver inner;
    protected boolean allowRawClasses;
    protected Set<String> nocache;

    protected final static Collection<String> report_topics =
            CollectionUtil.list(Report.types,
                                Report.resolver,
                                Report.loader,
                                "mcr");

    /**
     * Create a member class resolver.
     * @param ts The type system
     * @param inner The resolver for top-level classes
     */
    public MemberClassResolver(TypeSystem ts, TopLevelResolver inner,
            boolean allowRawClasses) {
        this.ts = ts;
        this.inner = inner;
        this.allowRawClasses = allowRawClasses;
        this.nocache = new HashSet<String>();
    }

    @Override
    public boolean packageExists(String name) {
        return inner.packageExists(name);
    }

    /**
     * Find a type by name.
     */
    @Override
    public Named find(String name) throws SemanticException {
        if (Report.should_report(report_topics, 3))
            Report.report(3, "MemberCR.find(" + name + ")");

        if (nocache.contains(name)) {
            throw new NoClassException(name);
        }

        Named n = ts.systemResolver().check(name);

        if (n != null) {
            return n;
        }

        SemanticException error = null;

        // First, just try the long name.
        try {
            if (Report.should_report(report_topics, 2))
                Report.report(2, "MCR: loading " + name + " from " + inner);
            return inner.find(name);
        }
        catch (SemanticException e) {
            if (Report.should_report(report_topics, 2))
                Report.report(2, "MCR: " + e.getMessage());
            if (StringUtil.isNameShort(name)) {
                throw e;
            }
            error = e;
        }

        boolean install = true;

        // Now try the prefix of the name and look for a member class
        // within it named with the suffix.
        String prefix = StringUtil.getPackageComponent(name);
        String suffix = StringUtil.getShortNameComponent(name);

        // Try the full name of the prefix first, then the raw class name,
        // so that encoded type information and source files are preferred
        // to the raw class file.
        try {
            if (Report.should_report(report_topics, 2))
                Report.report(2, "MCR: loading prefix " + prefix);

            n = find(prefix);

            // This may be called during deserialization; n's
            // member classes might not be initialized yet.
            if (n instanceof ParsedTypeObject) {
                return findMember(n, suffix);
            }
        }
        catch (SemanticException e) {
        }

        if (install) {
            nocache.add(name);
        }

        throw error;
    }

    protected Named findMember(Named container, String name)
            throws SemanticException {
        if (container instanceof ClassType) {
            ClassType ct = (ClassType) container;

            if (Report.should_report(report_topics, 2))
                Report.report(2, "MCR: found prefix " + ct);

            // Uncomment if we should search superclasses
            // return ct.resolver().find(name);
            Named n = ct.memberClassNamed(name);

            if (n != null) {
                if (Report.should_report(report_topics, 2))
                    Report.report(2, "MCR: found member of " + ct + ": " + n);
                return n;
            }
        }

        throw new NoClassException(container.fullName() + "." + name);
    }
}
