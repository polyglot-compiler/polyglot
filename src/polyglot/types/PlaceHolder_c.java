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

import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.SchedulerException;
import polyglot.frontend.goals.Goal;
import polyglot.util.CannotResolvePlaceHolderException;
import polyglot.util.SerialVersionUID;

/**
 * A place holder type when serializing the Polylgot type information.
 * When serializing the type information for some class {@code C},
 * Placeholders are used to prevent serializing the class type information
 * for classes that {@code C} depends on.
 */
public class PlaceHolder_c implements NamedPlaceHolder {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * The name of the place holder.
     */
    protected String name;

    /** Used for deserializing types. */
    protected PlaceHolder_c() {}

    /** Creates a place holder type for the type. */
    public PlaceHolder_c(Named t) {
        this(t.fullName());
    }

    public PlaceHolder_c(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof PlaceHolder_c && name.equals(((PlaceHolder_c) o).name));
    }

    @Override
    public TypeObject resolve(TypeSystem ts) throws CannotResolvePlaceHolderException {
        return resolveUnsafe(ts);
    }

    public TypeObject resolveUnsafe(TypeSystem ts) throws CannotResolvePlaceHolderException {
        Scheduler scheduler = ts.extensionInfo().scheduler();
        Goal g = scheduler.TypeExists(name);

        try {
            return ts.systemResolver().find(name);
        } catch (MissingDependencyException e) {
            // The type is in a source file that hasn't been parsed yet.
            g = e.goal();
            scheduler.currentGoal().setUnreachableThisRun();
            scheduler.addDependencyAndEnqueue(scheduler.currentGoal(), g, false);
            throw new CannotResolvePlaceHolderException(e);
        } catch (SchedulerException | SemanticException e) {
            // Some other scheduler error occurred, or the type could not be found.
            scheduler.currentGoal().setUnreachableThisRun();
            scheduler.addDependencyAndEnqueue(scheduler.currentGoal(), g, false);
            throw new CannotResolvePlaceHolderException(e);
        }
    }

    /** A potentially safer alternative implementation of resolve. */
    public TypeObject resolveSafe(TypeSystem ts) throws CannotResolvePlaceHolderException {
        Named n = ts.systemResolver().check(name);

        if (n != null) {
            return n;
        }

        // The class has not been loaded yet.  Set up a dependency
        // to load the class (coreq, in case this pass is the one to load it).
        Scheduler scheduler = ts.extensionInfo().scheduler();
        scheduler.currentGoal().setUnreachableThisRun();
        scheduler.addDependencyAndEnqueue(
                scheduler.currentGoal(), scheduler.TypeExists(name), false);

        throw new CannotResolvePlaceHolderException("Could not resolve " + name);
    }

    @Override
    public String toString() {
        return "PlaceHolder(" + name + ")";
    }
}
