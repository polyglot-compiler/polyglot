package polyglot.ext.carray.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for carray extension.
 *
 */
public interface CarrayNodeFactory extends NodeFactory {
    public ConstArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base);
}
