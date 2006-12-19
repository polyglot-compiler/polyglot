/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.qq;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * Signals an error in the quasiquoter.
 */
public class QQError extends InternalCompilerError {
    public QQError(String msg, Position pos) {
        super(msg, pos);
    }
}
