package jltools.frontend;

import java.io.*;
import jltools.ast.*;
import jltools.util.*;

/**
 * A parser interface.  It defines one method, <code>parse()</code>,
 * which returns the root of the AST.
 */
public interface Parser
{
    /** Return the root of the AST */
    Node parse();
}
