package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>SwitchBlock</code> is a list of statements within a switch.
 */
public class SwitchBlock_c extends AbstractBlock_c implements SwitchBlock
{
    public SwitchBlock_c(Del ext, Position pos, List statements) {
	super(ext, pos, statements);
    }
}
