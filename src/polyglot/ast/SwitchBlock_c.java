package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>SwitchBlock</code> is a list of statements within a switch.
 */
public class SwitchBlock_c extends AbstractBlock_c implements SwitchBlock
{
    public SwitchBlock_c(Position pos, List statements) {
	super(pos, statements);
    }
}
