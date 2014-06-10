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
    public int minor() { return 6; }

    @Override
    public int patch_level() { return 1; }

    @Override
    public String toString() { return "2.6.1 (2014-06-10 14:10:35)"; }
}
