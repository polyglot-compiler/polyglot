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
