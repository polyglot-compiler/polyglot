package jltools.frontend;

import java.util.*;

/**
 * A <code>CompoundPass</code> runs several passes in list order.
 */
public class CompoundPass implements Pass
{
    List passes;

    public CompoundPass(List passes) {
	this.passes = new ArrayList(passes);
    }

    public boolean run() {
	boolean okay = true;

	for (Iterator i = passes.iterator(); i.hasNext() && okay; ) {
	    Pass p = (Pass) i.next();
	    okay &= p.run();
	}

	return okay;
    }

    public String toString() {
	return "CompoundPass " + passes;
    }
}
