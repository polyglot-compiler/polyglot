/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.ClassBody;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.New;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import coffer.types.Key;
import coffer.types.KeySet;

/**
 * NodeFactory for Coffer extension.
 */
public interface CofferNodeFactory extends NodeFactory {
    Free Free(Position pos, Expr expr);

    TrackedTypeNode TrackedTypeNode(Position pos, KeyNode key, TypeNode base);

    AmbKeySetNode AmbKeySetNode(Position pos, List<KeyNode> keys);

    CanonicalKeySetNode CanonicalKeySetNode(Position pos, KeySet keys);

    KeyNode KeyNode(Position pos, Key key);

    New TrackedNew(Position pos, Expr outer, KeyNode key, TypeNode objectType,
            List<Expr> args, ClassBody body);

    ThrowConstraintNode ThrowConstraintNode(Position pos, TypeNode tn,
            KeySetNode keys);

    CofferMethodDecl CofferMethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            KeySetNode entryKeys, KeySetNode returnKeys,
            List<ThrowConstraintNode> throwConstraints, Block body);

    CofferConstructorDecl CofferConstructorDecl(Position pos, Flags flags,
            Id name, List<Formal> formals, KeySetNode entryKeys,
            KeySetNode returnKeys, List<ThrowConstraintNode> throwConstraints,
            Block body);

    CofferClassDecl CofferClassDecl(Position pos, Flags flags, Id name,
            KeyNode key, TypeNode superClass, List<TypeNode> interfaces,
            ClassBody body);
}
