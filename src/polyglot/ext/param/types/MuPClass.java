package polyglot.ext.param.types;

import polyglot.types.*;
import polyglot.util.*;

import polyglot.ext.jl.types.*;

import java.util.*;

/*
 * Mutable parametric classes.  This interface is a wrapper around
 * a ClassType that associates formal parameters with the class.
 * formals can be any type object.
 */
public interface MuPClass extends PClass {

    /**
     * Destructively update the formals.
     * @param formals The new formals
     */
    void formals(List formals);

    /**
     * Destructively add a formal to the end of the formals list.
     * @param param The new formal
     */
    void addFormal(Param param);

    /**
     * Destructively update the clazz.
     * @param formals The new clazz
     */
    void clazz(ClassType clazz);
}
