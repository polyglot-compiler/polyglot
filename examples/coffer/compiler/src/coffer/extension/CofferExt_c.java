/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import java.util.Iterator;

import polyglot.ast.Expr;
import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import coffer.ast.CofferLang;
import coffer.types.CofferClassType;
import coffer.types.Key;
import coffer.types.KeySet;

public class CofferExt_c extends Ext_c implements CofferExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static CofferExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof CofferExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No Coffer extension object for node "
                                                    + n
                                                    + " ("
                                                    + n.getClass()
                                                    + ")",
                                            n.position());
        }
        return (CofferExt) e;
    }

    @Override
    public final CofferLang lang() {
        return CofferLang.instance;
    }

    public String KeysToString(KeySet set) {
        return "K" + eysToString(set);
    }

    public String keysToString(KeySet set) {
        return "k" + eysToString(set);
    }

    private String eysToString(KeySet set) {
        if (set.size() == 1) {
            Key k = set.iterator().next();
            return "ey \"" + k + "\"";
        }
        else {
            String s = "eys [";
            for (Iterator<Key> i = set.iterator(); i.hasNext();) {
                s += "\"" + i.next() + "\"";
                if (i.hasNext()) s += ", ";
            }
            s += "]";
            return s;
        }
    }

    @Override
    public KeySet keyFlow(KeySet held_keys, Type throwType) {
        return held_keys;
    }

    @Override
    public KeySet keyAlias(KeySet stored_keys, Type throwType) {
        return stored_keys;
    }

    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        if (node() instanceof Expr) {
            Expr e = (Expr) node();

            if (e.type() instanceof CofferClassType) {
                Key key = ((CofferClassType) e.type()).key();

                if (key != null) {
                    if (!held.contains(key)) {
                        throw new SemanticException("Can evaluate expression of type \""
                                                            + e.type()
                                                            + "\" only if key \""
                                                            + key
                                                            + "\" is held.",
                                                    e.position());
                    }
                    if (stored.contains(key)) {
                        throw new SemanticException("Can evaluate expression of type \""
                                                            + e.type()
                                                            + "\" only if key \""
                                                            + key
                                                            + "\" is not held in a variable.",
                                                    e.position());
                    }
                }
            }
        }
    }
}
