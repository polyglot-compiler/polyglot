package polyglot.ext.param.types;

import polyglot.types.*;
import polyglot.util.*;

import polyglot.ext.jl.types.*;

import java.util.*;

public interface MuPClass extends PClass {

    /**
     * Destructively update the formals.
     * @param formals The new formals
     * @return <code>this</code>
     */
    MuPClass formals(List formals);

    /**
     * Destructively add a formal to the end of the formals list.
     * @param param The new formal
     * @return <code>this</code>
     */
    MuPClass addFormal(Param param);

    /**
     * Destructively update the clazz.
     * @param formals The new clazz
     * @return <code>this</code>
     */
    MuPClass clazz(ClassType clazz);
}
