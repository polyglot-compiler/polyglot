package polyglot.util.ppg.parse;

import polyglot.util.ppg.util.*;

public interface Unparse { 
    /**
     * Write a human-readable representation of the parse tree
     */
    public void unparse(CodeWriter cw); 
} 

