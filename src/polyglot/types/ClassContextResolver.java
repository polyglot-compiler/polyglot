/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * A {@code ClassContextResolver} looks up type names qualified with a class name.
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
    public Named find(String name, ClassType accessor)
            throws SemanticException {
        if (Report.should_report(TOPICS, 2))
            Report.report(2, "Looking for " + name + " in " + this);

        if (!StringUtil.isNameShort(name)) {
            throw new InternalCompilerError("Cannot lookup qualified name "
                    + name);
        }

        LinkedList<ClassType> typeQueue = new LinkedList<>();
        typeQueue.addLast(type);
        Set<MemberInstance> acceptable = new HashSet<>();
        SemanticException error = null;
        while (!typeQueue.isEmpty()) {
            ClassType type = typeQueue.removeFirst();
            // Check if the member was explicitly declared.
            Named m = type.memberClassNamed(name);

            // The desired type could be a member class of a deserialized type.
            // In that case, type will be lazily populated with member classes.
            // So, we need to use the system resolver to populate type as
            // necessary.
            String fullName = type.fullName() + "." + name;
            String rawName = ts.getTransformedClassName(type) + "$" + name;

            // Check the system resolver.
            if (m == null) m = ts.systemResolver().check(fullName);

            // Try the raw class file name.
            if (m == null) m = ts.systemResolver().check(rawName);

            if (m == null) {
                // Go to disk, but only if there is no job for the type.
                // If there is a job, all members should be in the resolver
                // already.
                boolean useLoadedResolver = true;

                if (type instanceof ParsedTypeObject) {
                    ParsedTypeObject pto = (ParsedTypeObject) type;
                    if (pto.job() != null) useLoadedResolver = false;
                }

                if (useLoadedResolver) {
                    try {
                        m = ts.systemResolver().find(rawName);
                    }
                    catch (SemanticException e) {
                        // Not found; will fall through to error handling code
                    }
                }
            }

            if (m instanceof MemberInstance) {
                MemberInstance mi = (MemberInstance) m;
                if (!ts.isMember(mi, this.type)) {
                    if (error == null)
                        error = new SemanticException("Member class " + m
                                + " is not visible in class " + this.type);
                }
                else if (!canAccess(mi, accessor)) {
                    if (error == null)
                        error = new SemanticException("Cannot access member type \""
                                + m + "\" from class " + accessor + ".");
                }
                else acceptable.add(mi);
                continue;
            }

            if (type.superType() != null) {
                Type sup = type.superType();
                if (sup instanceof ClassType) {
                    ClassType ct = (ClassType) sup;
                    typeQueue.addLast(ct);
                }
            }

            for (Type sup : type.interfaces()) {
                if (sup instanceof ClassType) {
                    ClassType ct = (ClassType) sup;
                    typeQueue.addLast(ct);
                }
            }
        }

        if (acceptable.size() == 0) {
            throw error == null ? new NoClassException(name, type) : error;
        }
        else if (acceptable.size() > 1) {
            Set<ReferenceType> containers = new HashSet<>(acceptable.size());
            for (MemberInstance mi : acceptable) {
                containers.add(mi.container());
            }

            if (containers.size() == 2) {
                Iterator<ReferenceType> i = containers.iterator();
                Type t1 = i.next();
                Type t2 = i.next();
                throw new SemanticException("Member \"" + name + "\" of " + type
                        + " is ambiguous; it is defined in both " + t1 + " and "
                        + t2 + ".");
            }
            else {
                throw new SemanticException("Member \"" + name + "\" of " + type
                        + " is ambiguous; it is defined in " + containers
                        + ".");
            }
        }
        MemberInstance mi = acceptable.iterator().next();

        if (Report.should_report(TOPICS, 2))
            Report.report(2, "Found member class " + mi);

        return (Named) mi;
    }

    protected boolean canAccess(MemberInstance n, ClassType accessor) {
        return accessor == null || ts.isAccessible(n, type, accessor);
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
