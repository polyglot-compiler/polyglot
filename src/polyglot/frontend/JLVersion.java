/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

/**
 * Version information for the base compiler.
 */
public class JLVersion extends polyglot.main.Version {
    public String name()
        { return "jl"; }
    public int major()
	{ return 2; }
    public int minor()
	{ return 0; }
    public int patch_level()
	{ return 0; }
}
