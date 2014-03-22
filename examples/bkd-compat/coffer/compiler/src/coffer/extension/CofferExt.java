/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.Ext;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import coffer.types.KeySet;

/** An immutable representation of the Coffer class declaration.
 *  It extends the Java class declaration with the label/principal parameters
 *  and the authority constraint.
 */
public interface CofferExt extends Ext {
    KeySet keyFlow(KeySet held_keys, Type throwType);

    KeySet keyAlias(KeySet stored_keys, Type throwType);

    void checkHeldKeys(KeySet held, KeySet stored) throws SemanticException;
}
