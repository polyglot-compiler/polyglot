/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import java.util.LinkedList;
import java.util.List;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayAccessAssign;
import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.ExtFactory;
import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.NodeFactory_c;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import coffer.extension.AssignDel_c;
import coffer.types.Key;
import coffer.types.KeySet;

/** An implementation of the <code>CofferNodeFactory</code> interface. 
 */
public class CofferNodeFactory_c extends NodeFactory_c implements
        CofferNodeFactory {
    public CofferNodeFactory_c() {
        super(new CofferExtFactory_c());
    }

    protected CofferNodeFactory_c(ExtFactory extFact) {
        super(extFact);
    }

    @Override
    public New TrackedNew(Position pos, Expr outer, KeyNode key,
            TypeNode objectType, List<Expr> args, ClassBody body) {
        return New(pos,
                   outer,
                   TrackedTypeNode(key.position(), key, objectType),
                   args,
                   body);

    }

    @Override
    public Free Free(Position pos, Expr expr) {
        Free n = new Free_c(pos, expr);
        n = (Free) n.ext(((CofferExtFactory_c) extFactory()).extFree());
        return n;
    }

    @Override
    public TrackedTypeNode TrackedTypeNode(Position pos, KeyNode key,
            TypeNode base) {
        TrackedTypeNode n = new TrackedTypeNode_c(pos, key, base);
        n =
                (TrackedTypeNode) n.ext(((CofferExtFactory_c) extFactory()).extTrackedTypeNode());
        return n;
    }

    @Override
    public AmbKeySetNode AmbKeySetNode(Position pos, List<KeyNode> keys) {
        AmbKeySetNode n = new AmbKeySetNode_c(pos, keys);
        n =
                (AmbKeySetNode) n.ext(((CofferExtFactory_c) extFactory()).extAmbKeySetNode());
        return n;
    }

    @Override
    public CanonicalKeySetNode CanonicalKeySetNode(Position pos, KeySet keys) {
        CanonicalKeySetNode n = new CanonicalKeySetNode_c(pos, keys);
        n =
                (CanonicalKeySetNode) n.ext(((CofferExtFactory_c) extFactory()).extCanonicalKeySetNode());
        return n;
    }

    @Override
    public KeyNode KeyNode(Position pos, Key key) {
        KeyNode n = new KeyNode_c(pos, key);
        n = (KeyNode) n.ext(((CofferExtFactory_c) extFactory()).extKeyNode());
        return n;
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        return CofferClassDecl(pos,
                               flags,
                               name,
                               null,
                               superClass,
                               interfaces,
                               body);
    }

    @Override
    public CofferClassDecl CofferClassDecl(Position pos, Flags flags, Id name,
            KeyNode key, TypeNode superClass, List<TypeNode> interfaces,
            ClassBody body) {
        CofferClassDecl n =
                new CofferClassDecl_c(pos,
                                      flags,
                                      name,
                                      key,
                                      superClass,
                                      interfaces,
                                      body);
        n = (CofferClassDecl) n.ext(extFactory().extClassDecl());
        return n;
    }

    @Override
    public ThrowConstraintNode ThrowConstraintNode(Position pos, TypeNode tn,
            KeySetNode keys) {
        ThrowConstraintNode n = new ThrowConstraintNode_c(pos, tn, keys);
        n =
                (ThrowConstraintNode) n.ext(((CofferExtFactory_c) extFactory()).extThrowConstraintNode());
        return n;
    }

    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> excTypes, Block body) {
        List<ThrowConstraintNode> l = new LinkedList<ThrowConstraintNode>();

        for (TypeNode tn : excTypes) {
            l.add(ThrowConstraintNode(tn.position(), tn, null));
        }

        return CofferMethodDecl(pos,
                                flags,
                                returnType,
                                name,
                                formals,
                                null,
                                null,
                                l,
                                body);

    }

    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> excTypes, Block body) {
        List<ThrowConstraintNode> l = new LinkedList<ThrowConstraintNode>();

        for (TypeNode tn : excTypes) {
            l.add(ThrowConstraintNode(tn.position(), tn, null));
        }

        return CofferConstructorDecl(pos,
                                     flags,
                                     name,
                                     formals,
                                     null,
                                     null,
                                     l,
                                     body);
    }

    @Override
    public CofferMethodDecl CofferMethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            KeySetNode entryKeys, KeySetNode returnKeys,
            List<ThrowConstraintNode> throwConstraints, Block body) {
        CofferMethodDecl n =
                new CofferMethodDecl_c(pos,
                                       flags,
                                       returnType,
                                       name,
                                       formals,
                                       entryKeys,
                                       returnKeys,
                                       throwConstraints,
                                       body);
        n = (CofferMethodDecl) n.ext(extFactory().extMethodDecl());
        return n;
    }

    @Override
    public CofferConstructorDecl CofferConstructorDecl(Position pos,
            Flags flags, Id name, List<Formal> formals, KeySetNode entryKeys,
            KeySetNode returnKeys, List<ThrowConstraintNode> throwConstraints,
            Block body) {
        CofferConstructorDecl n =
                new CofferConstructorDecl_c(pos,
                                            flags,
                                            name,
                                            formals,
                                            entryKeys,
                                            returnKeys,
                                            throwConstraints,
                                            body);
        n = (CofferConstructorDecl) n.ext(extFactory().extConstructorDecl());
        return n;
    }

    @Override
    public FieldAssign FieldAssign(Position pos, Field left,
            Assign.Operator op, Expr right) {
        return (FieldAssign) super.FieldAssign(pos, left, op, right)
                                  .del(new AssignDel_c());
    }

    @Override
    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess left,
            Assign.Operator op, Expr right) {
        return (ArrayAccessAssign) super.ArrayAccessAssign(pos, left, op, right)
                                        .del(new AssignDel_c());
    }

    @Override
    public LocalAssign LocalAssign(Position pos, Local left,
            Assign.Operator op, Expr right) {
        return (LocalAssign) super.LocalAssign(pos, left, op, right)
                                  .del(new AssignDel_c());
    }

    @Override
    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
        return (Assign) super.Assign(pos, left, op, right)
                             .del(new AssignDel_c());
    }

}
