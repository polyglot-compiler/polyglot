/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

/**
 * A place holder used to serialize type objects that cannot be serialized.  
 */
public interface NamedPlaceHolder extends PlaceHolder {
    String name();
}
