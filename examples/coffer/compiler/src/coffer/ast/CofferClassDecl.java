package polyglot.ext.coffer.ast;

import polyglot.ast.*;
import java.util.*;

/** An immutable representation of the Coffer class declaration.
 *  It extends the Java class declaration with the label/principal parameters
 *  and the authority constraint.
 */
public interface CofferClassDecl extends ClassDecl {
    KeyNode key();
    CofferClassDecl key(KeyNode key);
}
