/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

/**
 * An <code>Importable</code> is a type object that can be imported by another
 * type object.  An <code>Importable</code> is contained in a
 * <code>Package</code>.  
 */
public interface Importable extends Named
{
    Package package_();
}
