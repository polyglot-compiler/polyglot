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
import java.util.Iterator;
import java.util.Set;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * A <code>ClassContextResolver</code> looks up type names qualified with a class name.
 * For example, if the class is "A.B", the class context will return the class
 * for member class "A.B.C" (if it exists) when asked for "C".
 */
public class ClassContextResolver extends AbstractAccessControlResolver {
    protected ClassType type;

    /**
     * Construct a resolver.
     * @param ts The type system.
     * @param type The type in whose context we search for member types.
     */
    public ClassContextResolver(TypeSystem ts, ClassType type) {
        super(ts);
        this.type = type;
    }

    @Override
    public String toString() {
        return "(class-context " + type + ")";
    }

    /**
     * Find a type object in the context of the class.
     * @param name The name to search for.
     */
    @Override
    public Named find(String name, ClassType accessor) throws SemanticException {
        if (Report.should_report(TOPICS, 2))
            Report.report(2, "Looking for " + name + " in " + this);

        if (!StringUtil.isNameShort(name)) {
            throw new InternalCompilerError("Cannot lookup qualified name "
                    + name);
        }

        // Check if the name is for a member class.
        ClassType mt = null;

        Named m;

        String fullName = type.fullName() + "." + name;
        String rawName = ts.getTransformedClassName(type) + "$" + name;

        // First check the system resolver.
        m = ts.systemResolver().check(fullName);

        // Try the raw class file name.
        if (m == null) {
            m = ts.systemResolver().check(rawName);
        }

        // Check if the member was explicitly declared.
        if (m == null) {
            m = type.memberClassNamed(name);
        }

        // Go to disk, but only if there is no job for the type.
        // If there is a job, all members should be in the resolver
        // already.
        boolean useLoadedResolver = true;

        if (type instanceof ParsedTypeObject) {
            ParsedTypeObject pto = (ParsedTypeObject) type;
            if (pto.job() != null) {
                useLoadedResolver = false;
            }
        }

        if (m == null && useLoadedResolver) {
            try {
                m = ts.systemResolver().find(rawName);
            }
            catch (SemanticException e) {
                // Not found; will fall through to error handling code
            }
        }

        if (m instanceof ClassType) {
            mt = (ClassType) m;

            if (!mt.isMember()) {
                throw new SemanticException("Class " + mt
                        + " is not a member class, " + " but was found in "
                        + type + ".");
            }

            if (mt.outer().declaration() != type.declaration()) {
                throw new SemanticException("Class " + mt
                        + " is not a member class " + " of " + type + ".");
            }

            if (!canAccess(mt, accessor)) {
                throw new SemanticException("Cannot access member type \"" + mt
                        + "\".");
            }

            return mt;
        }

        // Collect all members of the super types.
        // Use a Set to eliminate duplicates.
        Set<Named> acceptable = new HashSet<Named>();

        if (type.superType() != null) {
            Type sup = type.superType();
            if (sup instanceof ClassType) {
                Resolver r = ts.classContextResolver((ClassType) sup, accessor);
                try {
                    Named n = r.find(name);
                    acceptable.add(n);
                }
                catch (SemanticException e) {
                }
            }
        }

        for (Type sup : type.interfaces()) {
            if (sup instanceof ClassType) {
                Resolver r = ts.classContextResolver((ClassType) sup, accessor);
                try {
                    Named n = r.find(name);
                    acceptable.add(n);
                }
                catch (SemanticException e) {
                }
            }
        }

        if (acceptable.size() == 0) {
            throw new NoClassException(name, type);
        }
        else if (acceptable.size() > 1) {
            Set<ReferenceType> containers =
                    new HashSet<ReferenceType>(acceptable.size());
            for (Named n : acceptable) {
                if (n instanceof MemberInstance) {
                    MemberInstance mi = (MemberInstance) n;
                    containers.add(mi.container());
                }
            }

            if (containers.size() == 2) {
                Iterator<ReferenceType> i = containers.iterator();
                Type t1 = i.next();
                Type t2 = i.next();
                throw new SemanticException("Member \"" + name + "\" of "
                        + type + " is ambiguous; it is defined in both " + t1
                        + " and " + t2 + ".");
            }
            else {
                throw new SemanticException("Member \"" + name + "\" of "
                        + type + " is ambiguous; it is defined in "
                        + containers + ".");
            }
        }

        Named t = acceptable.iterator().next();

        if (Report.should_report(TOPICS, 2))
            Report.report(2, "Found member class " + t);

        return t;
    }

    protected boolean canAccess(Named n, ClassType accessor) {
        if (n instanceof MemberInstance) {
            return accessor == null
                    || ts.isAccessible((MemberInstance) n, accessor);
        }
        return true;
    }

    /**
     * The class in whose context we look.
     */
    public ClassType classType() {
        return type;
    }

    private static final Collection<String> TOPICS =
            CollectionUtil.list(Report.types, Report.resolver);

}
