package polyglot.ext.coffer.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.coffer.types.*;

public interface KeyNode extends Node
{
    public Key key();
    public String name();
    public KeyNode key(Key key);
}
