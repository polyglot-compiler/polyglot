package jltools.frontend;

import java.io.IOException;

/**
 * A <code>BarrierPass</code> is a special pass that ensures that
 * all jobs complete a goal pass before any job continues.
 */
public class BarrierPass implements Pass {
    jltools.frontend.Compiler compiler;
    PassID goal;

    public BarrierPass(jltools.frontend.Compiler compiler, PassID goal) {
	this.compiler = compiler;
	this.goal = goal;
    }

    public boolean run() {
	try {
	    return compiler.finish(goal);
	}
	catch (IOException e) {
	    return false;
	}
    }
}
