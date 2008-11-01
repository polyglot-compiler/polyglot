/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.util;

/** A two-element tuple.
 */
public class Pair
{
    protected Object part1;
    protected Object part2;
    
    public Pair(Object p1, Object p2) {
	this.part1 = p1;
	this.part2 = p2;
    }
    
    public Object part1() {
	return part1;
    }
    
    public Object part2() {
	return part2;
    }
    
    public String toString() {
    	return "(" + part1 + ", " + part2 + ")";
    }
    
    public boolean equals(Object o) {
    	if (o instanceof Pair) {
    		Pair p = (Pair)o;
    		return (part1 == null ? p.part1 == null : part1.equals(p.part1)) 
    		    && (part2 == null ? p.part2 == null : part2.equals(p.part2));
    	}
    	return false;
    }
    
    public int hashCode() {
    	return (part1 != null ? part1.hashCode() : 0) ^ (part2 != null ? part2.hashCode() : 0);
    }
}
