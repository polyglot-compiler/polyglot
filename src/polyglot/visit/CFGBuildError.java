/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * Exception thrown when the CFG cannot be built.  This should be
 * a SemanticException, but is an error so it doesn't need to be declared
 * in the signature of Node.acceptCFG.
 */
public class CFGBuildError extends InternalCompilerError
{
    public CFGBuildError(String msg) {
        super(msg);
    }

    public CFGBuildError(Position position, String msg) {
        super(position, msg);
    }

    public CFGBuildError(String msg, Position position) {
        super(msg, position);
    }
}
