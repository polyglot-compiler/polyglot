package jltools.frontend;

import jltools.ast.*;
import jltools.util.*;
import java.util.*;

/** The base class for most passes. */
public abstract class AbstractPass implements Pass
{
    Status status;
    List runAfter;
    List repeatAfter;

    public AbstractPass() {
	this.status = NEW;
	this.runAfter = new ArrayList();
	this.repeatAfter = new ArrayList();
    }

    public void runAfter(Pass pass) {
	if (status == NEW || status == ENQUEUED) {
	    Compiler.report(2, "Adding dependency " + pass + " -> " + this);
	    runAfter.add(pass);
	}
	else if (status == RUNNING) {
	    Compiler.report(2, "Adding repeat dependency " + pass + " -> " + this);
	    repeatAfter.add(pass);
	}
	else {
	    throw new InternalCompilerError(
		"Cannot add dependency to pass is state " + status + ".");
	}
    }

    public boolean repeat() {
	return ! repeatAfter.isEmpty();
    }

    public void reinit() {
	repeatAfter.clear();
    }

    public List runAfter() {
	List l = new ArrayList(runAfter.size() + repeatAfter.size());
	l.addAll(runAfter);
	l.addAll(repeatAfter);
	l.remove(this);
	return l;
    }

    public Status status() {
	return status;
    }

    public void status(Status status) {
	this.status = status;
    }

    public abstract boolean run();

    public String toString() {
	return getClass().getName();
    }
}
