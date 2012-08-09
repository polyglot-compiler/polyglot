/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 */
package polyglot.frontend;

/**
 * Version information for the base compiler.
 */
public class JLVersion extends polyglot.main.Version {
    @Override
    public String name() { return "jl"; }

    @Override
    public int major() { return 2; }

    @Override
    public int minor() { return 5; }

    @Override
    public int patch_level() { return 0; }

    @Override
    public String toString() { return "2.5.0 (2012-08-08 10:39:17)"; }
}
