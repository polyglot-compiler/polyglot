/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import polyglot.ast.Node;
import polyglot.util.*;
import java.util.*;

/**
 * A PruningVisitor is used to prune the traversal of the AST at a
 * particular node.  Returning a PruningVisitor from the
 * NodeVisitor.enter method ensures no children will be visited.
 */
public class PruningVisitor extends NodeVisitor
{
    public Node override(Node parent, Node n) {
        return n;
    }
}
