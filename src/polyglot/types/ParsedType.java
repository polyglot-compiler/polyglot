package jltools.types;

import jltools.frontend.Job;
import jltools.util.Position;

public interface ParsedType extends Type
{
    boolean isClean();
    void setClean(boolean clean);

    /** The job that created the type. */
    Job job();
}
