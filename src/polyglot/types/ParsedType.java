package jltools.types;

import jltools.frontend.Job;
import jltools.util.Position;

/** A type parsed from a source file. */
public interface ParsedType extends Type
{
    /** The job that created the type. */
    Job job();
}
