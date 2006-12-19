/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ext.param;

import polyglot.frontend.JLExtensionInfo;

/**
 * Param is an abstract extension implementing functionality for
 * parameterized types.
 */
public abstract class ExtensionInfo extends JLExtensionInfo {
    static {
        // force Topics to load
        Topics t = new Topics();
    }
}
