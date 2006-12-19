/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.*;
import java.util.*;

/**
 * An ambiguous key set AST node.  This is essentially a list of possibly
 * ambiguous key nodes.
 */
public interface AmbKeySetNode extends KeySetNode, Ambiguous
{
    public List keyNodes();
    public AmbKeySetNode keyNodes(List keyNodes);
}
