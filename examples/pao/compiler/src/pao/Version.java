/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao;

/**
 * Version information for PAO extension
 */
public class Version extends polyglot.main.Version {
    @Override
    public String name() {
        return "pao";
    }

    @Override
    public int major() {
        return 0;
    }

    @Override
    public int minor() {
        return 1;
    }

    @Override
    public int patch_level() {
        return 0;
    }
}
