package polyglot.ast;

import polyglot.visit.*;
import java.util.*;

/**
 * An <code>Computation</code> represents any Java expression or statement--an
 * entity that performs some computation.
 */
public interface Computation extends Node
{
    /**
     * Return the first (sub)computation performed when evaluating this
     * computation.
     */
    public Computation entry();

    /**
     * Visit this node, calling calling v.edge() for each successor in succs,
     * if data flows on that edge.
     */
    public List acceptCFG(CFGBuilder v, List succs);
}
