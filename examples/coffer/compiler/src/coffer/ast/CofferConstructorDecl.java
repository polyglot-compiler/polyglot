/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.*;
import java.util.*;

/** An immutable representation of the Coffer constructor declaration.
 * <code>ConstructorDecl</code> is extended with pre- and post-conditions.
 */
public interface CofferConstructorDecl extends ConstructorDecl {
    KeySetNode entryKeys();
    CofferConstructorDecl entryKeys(KeySetNode entryKeys);
    
    KeySetNode returnKeys();
    CofferConstructorDecl returnKeys(KeySetNode returnKeys);

    List throwConstraints();
    CofferConstructorDecl throwConstraints(List throwConstraints);
}
