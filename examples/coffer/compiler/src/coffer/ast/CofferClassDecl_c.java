/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.param.types.MuPClass;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import coffer.types.CofferClassType;
import coffer.types.CofferContext;
import coffer.types.CofferParsedClassType;
import coffer.types.CofferTypeSystem;
import coffer.types.Key;

/**
 * An implementation of the <code>CofferClassDecl</code> interface.
 * <code>ClassDecl</code> is extended with a possibly-null key name.
 */
public class CofferClassDecl_c extends ClassDecl_c implements CofferClassDecl {
    protected KeyNode key;
    private static final long serialVersionUID = SerialVersionUID.generate();

    public CofferClassDecl_c(Position pos, Flags flags, Id name, KeyNode key,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        super(pos, flags, name, superClass, interfaces, body);
        this.key = key;
    }

    @Override
    public KeyNode key() {
        return this.key;
    }

    @Override
    public CofferClassDecl key(KeyNode key) {
        CofferClassDecl_c n = (CofferClassDecl_c) copy();
        n.key = key;
        return n;
    }

    protected CofferClassDecl_c reconstruct(Id name, KeyNode key,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        CofferClassDecl_c n = this;

        if (this.key != key) {
            n = (CofferClassDecl_c) copy();
            n.key = key;
        }

        return (CofferClassDecl_c) reconstruct(n, name,
                                                 superClass,
                                                 interfaces,
                                                 body);
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = visitChild(this.name, v);
        KeyNode key = visitChild(this.key, v);
        TypeNode superClass = visitChild(this.superClass, v);
        List<TypeNode> interfaces = visitList(this.interfaces, v);
        ClassBody body = visitChild(this.body, v);
        return reconstruct(name, key, superClass, interfaces, body);
    }

    @Override
    public Context enterChildScope(Node child, Context context) {
        CofferContext c = (CofferContext) context;

        CofferParsedClassType ct = (CofferParsedClassType) this.type;
        CofferClassType inst = ct;

        if (child == this.body) {
            c = (CofferContext) c.pushClass(ct, inst);

            if (key != null) c.addKey(key.key());
        }

        return c;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        CofferClassDecl_c n = (CofferClassDecl_c) super.buildTypes(tb);

        CofferTypeSystem ts = (CofferTypeSystem) tb.typeSystem();

        CofferParsedClassType ct = (CofferParsedClassType) n.type;

        MuPClass<Key, Key> pc = ts.mutablePClass(ct.position());
        ct.setInstantiatedFrom(pc);
        pc.clazz(ct);

        if (key != null) {
            ct.setKey(key.key());
        }

        return n;
    }

    @Override
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
        if (flags.isInterface()) {
            w.write(flags.clearInterface().clearAbstract().translate());
        }
        else {
            w.write(flags.translate());
        }

        if (key != null) {
            w.write("tracked(");
            print(key, w, tr);
            w.write(") ");
        }

        if (flags.isInterface()) {
            w.write("interface ");
        }
        else {
            w.write("class ");
        }

        print(name, w, tr);

        if (superClass() != null) {
            w.write(" extends ");
            print(superClass(), w, tr);
        }

        if (!interfaces.isEmpty()) {
            if (flags.isInterface()) {
                w.write(" extends ");
            }
            else {
                w.write(" implements ");
            }

            for (Iterator<TypeNode> i = interfaces().iterator(); i.hasNext();) {
                TypeNode tn = i.next();
                print(tn, w, tr);

                if (i.hasNext()) {
                    w.write(", ");
                }
            }
        }

        w.write(" {");
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        ((CofferClassDecl_c) key(null)).superTranslate(w, tr);
    }

    public void superTranslate(CodeWriter w, Translator tr) {
        super.translate(w, tr);
    }
}
