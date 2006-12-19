/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * Predicate.java
 */

package polyglot.util;

/**
 * Predicate
 *
 * Overview:
 *     This interface provides a general means for describing predicates
 *     about objects.
 **/
public interface Predicate { 
  public boolean isTrue(Object o);  
}


