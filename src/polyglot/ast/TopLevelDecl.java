package jltools.ast;

import jltools.types.Flags;

public interface TopLevelDecl extends Node
{
    Flags flags();
    String name();
}
