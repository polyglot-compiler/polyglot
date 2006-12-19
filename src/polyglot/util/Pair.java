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
}
