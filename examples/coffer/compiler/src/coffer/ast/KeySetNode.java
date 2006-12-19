/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import coffer.types.*;

/**
 * An AST node for a <code>KeySet</code>.  The key set may be ambiguous. 
 */
public interface KeySetNode extends Node
{
    KeySet keys();
}
