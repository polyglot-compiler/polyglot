/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.*;
import coffer.types.*;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for Coffer extension.
 */
public interface CofferNodeFactory extends NodeFactory {
    Free Free(Position pos, Expr expr);
    TrackedTypeNode TrackedTypeNode(Position pos, KeyNode key, TypeNode base);
    AmbKeySetNode AmbKeySetNode(Position pos, List keys);
    CanonicalKeySetNode CanonicalKeySetNode(Position pos, KeySet keys);
    KeyNode KeyNode(Position pos, Key key);

    New TrackedNew(Position pos, Expr outer, KeyNode key, TypeNode objectType, List args, ClassBody body);

    ThrowConstraintNode ThrowConstraintNode(Position pos, TypeNode tn, KeySetNode keys);

    CofferMethodDecl CofferMethodDecl(Position pos, Flags flags, TypeNode
                                    returnType, Id name, List argTypes,
                                    KeySetNode entryKeys, KeySetNode returnKeys,
                                    List throwConstraints, Block body);

    CofferConstructorDecl CofferConstructorDecl(Position pos, Flags flags, Id name, List
                                              argTypes, KeySetNode entryKeys,
                                              KeySetNode returnKeys, List
                                              throwConstraints, Block body);


    CofferClassDecl CofferClassDecl(Position pos, Flags flags, Id name,
                                  KeyNode key, TypeNode superClass, List
                                  interfaces, ClassBody body);
}
