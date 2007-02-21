/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

/**
 * A <code>InitializerInstance</code> contains the type information for a
 * static or anonymous initializer.
 */
public interface InitializerInstance extends CodeInstance, MemberInstance
{
    /**
     * Set the initializer's flags.
     */
    InitializerInstance flags(Flags flags);

    /**
     * Set the initializer's containing class.
     */
    InitializerInstance container(ClassType container);
}
