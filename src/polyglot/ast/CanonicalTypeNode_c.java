/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 *
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.frontend.ExtensionInfo;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

/**
 * A {@code CanonicalTypeNode} is a type node for a canonical type.
 */
public class CanonicalTypeNode_c extends TypeNode_c
        implements CanonicalTypeNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

//    @Deprecated
    public CanonicalTypeNode_c(Position pos, Type type) {
        this(pos, type, null);
    }

    public CanonicalTypeNode_c(Position pos, Type type, Ext ext) {
        super(pos, ext);
        assert type != null;
        this.type = type;
    }

    /** Type check the type node.  Check accessibility of class types. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        Type type = this.type;
        if (type.isArray()) {
            type = type.toArray().ultimateBase();
            if (type.isClass()) {
                ClassType ct = type.toClass();
                if (ct.isTopLevel() || ct.isMember()) {
                    if (!ts.classAccessible(ct, tc.context())) {
                        throw new SemanticException("Cannot access class \""
                                + ct + "\" from the body of \""
                                + tc.context().currentClass() + "\".",
                                                    position());
                    }
                }
            }
        }

        return this;
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        return rw.typeToJava(type(), position());
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (type == null) {
            w.write("<unknown-type>");
        }
        else {
            type.print(w);
        }
    }

    /**
     * Translate the type.
     * If the "use-fully-qualified-class-names" options is used, then the
     * fully qualified names is written out ({@code java.lang.Object}).
     * Otherwise, the string that originally represented the type in the
     * source file is used.
     */
    @Override
    public void translate(CodeWriter w, Translator tr) {
        w.write(type.translate(tr.context()));
    }

    @Override
    public String toString() {
        if (type == null) return "<unknown-type>";
        return type.toString();
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);
        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(type " + type + ")");
        w.end();
    }

    @Override
    public Node copy(NodeFactory nf) {
        CanonicalTypeNode result = nf.CanonicalTypeNode(position, type);
        return result;
    }

    @Override
    public Node copy(ExtensionInfo extInfo) throws SemanticException {
        TypeNode tn =
                (TypeNode) extInfo.nodeFactory()
                                  .lang()
                                  .copy(this, extInfo.nodeFactory());
        Type t = tn.type();
        if (t != null) {
            // try to copy over type information
            // This should really use a type visitor, if
            // they existed.
            TypeSystem ts = extInfo.typeSystem();
            if (t.isVoid()) {
                t = ts.Void();
            }
            else if (t.isBoolean()) {
                t = ts.Boolean();
            }
            else if (t.isByte()) {
                t = ts.Byte();
            }
            else if (t.isChar()) {
                t = ts.Char();
            }
            else if (t.isDouble()) {
                t = ts.Double();
            }
            else if (t.isFloat()) {
                t = ts.Float();
            }
            else if (t.isInt()) {
                t = ts.Int();
            }
            else if (t.isLong()) {
                t = ts.Long();
            }
            else if (t.isNull()) {
                t = ts.Null();
            }
            else if (t.isShort()) {
                t = ts.Short();
            }
            else {
                // Should do something here.
                // return an amb type node?
            }

            tn = tn.type(t);
        }

        return tn;
    }

}
