/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.List;

import polyglot.types.MemberInstance;
import polyglot.types.ProcedureInstance;

/** Coffer procedure instance. A wrapper of all the type information 
 *  related to a procedure. 
 */
public interface CofferProcedureInstance extends ProcedureInstance,
        MemberInstance {
    KeySet entryKeys();

    KeySet returnKeys();

    List<ThrowConstraint> throwConstraints();

    void setEntryKeys(KeySet entryKeys);

    void setReturnKeys(KeySet returnKeys);

    void setThrowConstraints(List<ThrowConstraint> throwConstraints);
}
