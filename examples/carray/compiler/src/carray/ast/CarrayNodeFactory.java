package jltools.ext.carray.ast;

import jltools.ast.*;
import jltools.ext.jl.ast.*;
import jltools.types.Flags;
import jltools.types.Package;
import jltools.types.Type;
import jltools.types.Qualifier;
import jltools.util.*;
import java.util.*;

/**
 * NodeFactory for carray extension.
 *
 */
public interface CarrayNodeFactory extends NodeFactory {
    public ConstArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base);
}
