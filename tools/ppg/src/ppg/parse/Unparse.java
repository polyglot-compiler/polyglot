package jltools.util.jlgen.parse;

import jltools.util.jlgen.util.*;

public interface Unparse { 
    public void unparse(CodeWriter cw)throws ParserError; 
        // Write a human-readable representation of the node to the given 
        // CodeWriter cw. 
} 

