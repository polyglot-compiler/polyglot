package jltools.frontend;

import jltools.util.Enum;
import java.util.*;

/** A <code>Pass</code> represents a compiler pass. */
public interface Pass
{
    public static class Status extends Enum {
	Status(String name) { super(name); }
    }

    public static final Status NEW      = new Status("new");
    public static final Status ENQUEUED = new Status("enqueued");
    public static final Status RUNNING  = new Status("running");
    public static final Status FAILED   = new Status("failed");
    public static final Status DONE     = new Status("done");

    public Status status();
    public void status(Status status);

    public boolean repeat();
    public void reinit();

    public List runAfter();
    public void runAfter(Pass pass);

    public boolean run();
}
