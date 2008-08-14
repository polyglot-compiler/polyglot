/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 */
package polyglot.frontend;

/**
 * Version information for the base compiler.
 */
public class JLVersion extends polyglot.main.Version {
    public String name() { return "jl"; }
    public int major() { return 2; }
    public int minor() { return 4; }
    public int patch_level() { return 0; }
    public String toString() { return "2.4.0 (2008-08-14 11:16:45)"; }
}
