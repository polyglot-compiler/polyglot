package jltools.ast;

import java.util.List;

/**
 * A <code>SourceCollection</code> represents a collection of source files.
 */
public interface SourceCollection extends Node
{
    List sources();
    SourceCollection sources(List sources);
}
