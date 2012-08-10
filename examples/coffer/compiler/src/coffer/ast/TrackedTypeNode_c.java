/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import java.util.HashMap;
import java.util.Map;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;
import coffer.types.CofferClassType;
import coffer.types.CofferTypeSystem;
import coffer.types.Key;

/** An implementation of the <code>TrackedTypeNode</code> interface,
 * a type node for a class instantiated with a key.
 */
public class TrackedTypeNode_c extends TypeNode_c implements TrackedTypeNode {
    protected TypeNode base;
    protected KeyNode key;

    public TrackedTypeNode_c(Position pos, KeyNode key, TypeNode base) {
        super(pos);
        this.key = key;
        this.base = base;
    }

    @Override
    public TypeNode base() {
        return this.base;
    }

    @Override
    public TrackedTypeNode base(TypeNode base) {
        TrackedTypeNode_c n = (TrackedTypeNode_c) copy();
        n.base = base;
        return n;
    }

    @Override
    public KeyNode key() {
        return this.key;
    }

    @Override
    public TrackedTypeNode key(KeyNode key) {
        TrackedTypeNode_c n = (TrackedTypeNode_c) copy();
        n.key = key;
        return n;
    }

    protected TrackedTypeNode_c reconstruct(TypeNode base, KeyNode key) {
        if (base != this.base || key != this.key) {
            TrackedTypeNode_c n = (TrackedTypeNode_c) copy();
            n.base = base;
            n.key = key;
            return n;
        }

        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode base = (TypeNode) visitChild(this.base, v);
        KeyNode key = (KeyNode) visitChild(this.key, v);
        return reconstruct(base, key);
    }

    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        CofferTypeSystem ts = (CofferTypeSystem) sc.typeSystem();

        if (!base.isDisambiguated() || !key.isDisambiguated()) {
            return this;
        }

        Type b = base.type();

        if (!(b instanceof CofferClassType)) {
            throw new SemanticException("Cannot instantiate from a non-polymorphic type "
                    + b);
        }

        CofferClassType t = (CofferClassType) b;

        Key key = this.key.key();

        Key formal = t.key();
        Map<Key, Key> subst = new HashMap<Key, Key>();
        subst.put(formal, key);

        return sc.nodeFactory().CanonicalTypeNode(position(),
                                                  ts.subst(t, subst));
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot type check ambiguous node "
                                                + this + ".");
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        throw new InternalCompilerError(position(),
                                        "Cannot exception check ambiguous node "
                                                + this + ".");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("tracked(");
        w.write(key.toString());
        w.write(") ");
        print(base, w, tr);
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        throw new InternalCompilerError(position(),
                                        "Cannot translate ambiguous node "
                                                + this + ".");
    }

    @Override
    public String toString() {
        return "tracked(" + key + ") " + base;
    }
}
