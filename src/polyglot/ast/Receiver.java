/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * A <code>Receiver</code> represents any node that can be used as the
 * receiver of a method or of a field access.  Usually, this is just
 * expressions and types.
 */
public interface Receiver extends Prefix, Typed
{
}
