package polyglot.ext.param.types;

import polyglot.types.*;
import java.util.Iterator;

/**
 * A type on that substitutions have been applied.
 */
public interface SubstType extends Type
{
    Type base();
    Subst subst();
    Iterator entries();
}
