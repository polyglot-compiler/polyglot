package polyglot.ast;

import polyglot.types.Flags;

/**
 * A top-level declaration.  This is any declaration that can appear in the
 * outermost scope of a source file.
 */
public interface TopLevelDecl extends Node
{
    Flags flags();
    String name();
}
