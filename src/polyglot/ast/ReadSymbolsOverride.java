package jltools.ast;

import jltools.types.*;
import jltools.visit.*;

public interface ReadSymbolsOverride
{
    public Node readSymbols(Node n, SymbolReader sr) throws SemanticException;
}
