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

package polyglot.qq;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceFile;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.frontend.ExtensionInfo;
import polyglot.lex.Lexer;
import polyglot.main.Report;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;

/**
 * Java language quasiquoter.  This class contains methods for parsing strings
 * into ASTs.
 *
 * <p>
 * To use the class, invoke one of the {@code parseT} methods to create a
 * {@code Node} of type {@code T}.
 *
 * <p>
 * Each {@code parseT} method takes a format string as its first argument
 * and some additional {@code Object} arguments.  Each pattern in the
 * format string is matched with its corresponding {@code Object}.
 *
 * <p>
 * The format string may contain the following patterns:
 * <ul>
 * <li> {@code %s} matches {@code String} (parsed as an identifier)
 *
 * <li> {@code %T} matches {@code Type} or {@code TypeNode}
 * <li> {@code %E} matches {@code Expr}
 * <li> {@code %S} matches {@code Stmt}
 * <li> {@code %D} matches {@code ClassDecl}
 * <li> {@code %M} matches {@code ClassMember}
 * <li> {@code %F} matches {@code Formal}
 *
 * <li> {@code %LT} matches {@code List<Type>} or {@code List<TypeNode>}
 * <li> {@code %LE} matches {@code List<Expr>}
 * <li> {@code %LS} matches {@code List<Stmt>}
 * <li> {@code %LD} matches {@code List<ClassDecl>}
 * <li> {@code %LM} matches {@code List<ClassMember>}
 * <li> {@code %LF} matches {@code List<Formal>}
 * </ul>
 * These patterns are recognized as tokens by the lexer--surrounding the token
 * with whitespace or parens may be needed to parse the string.
 *
 * <p>
 * For example:
 * <pre>
 * Expr e;
 * TypeNode t;
 * Stmt s = qq.parseStmt("%T %s = new %T(%E);", t, "tmp", t, e);
 * </pre>
 */
public class QQ {
    protected ExtensionInfo ext;
    protected Position pos;

    protected static final int EXPR = 0;
    protected static final int STMT = 1;
    protected static final int TYPE = 2;
    protected static final int MEMB = 3;
    protected static final int DECL = 4;
    protected static final int FILE = 5;

    /** Create a new quasiquoter to create AST nodes of the given
     * language extension. */
    public QQ(ExtensionInfo ext) {
        this(ext, null);
    }

    /** Create a new quasiquoter to create AST nodes of the given
     * language extension, using {@code pos} for the position
     * of the nodes created. */
    public QQ(ExtensionInfo ext, Position pos) {
        this.ext = ext;
        this.pos = pos;
    }

    /**
     * Parse a string into a {@code SourceFile} AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object... os) {
        return (SourceFile) parse(FILE, fmt, os);
    }

    /**
     * Parse a string into a {@code SourceFile} AST node,
     * applying substitutions.
     *
     * @param subst a list containing the values to be substituted
     * @deprecated Use {@link #parseFile(String, Object...)} instead.
     */
    @Deprecated
    public SourceFile parseFile(String fmt, List<?> subst) {
        return (SourceFile) parse(FILE, fmt, subst.toArray());
    }

    /** Parse a string into a {@code ClassDecl} AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object... os) {
        return (ClassDecl) parse(DECL, fmt, os);
    }

    /**
     * Parse a string into a {@code ClassDecl} AST node,
     * applying substitutions.
     *
     * @param subst a list containing the values to be substituted
     * @deprecated Use {@link #parseDecl(String, Object...)} instead.
     */
    @Deprecated
    public ClassDecl parseDecl(String fmt, List<?> subst) {
        return (ClassDecl) parse(DECL, fmt, subst.toArray());
    }

    /** Parse a string into a {@code ClassMember} AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object... os) {
        return (ClassMember) parse(MEMB, fmt, os);
    }

    /**
     * Parse a string into a {@code ClassMember} AST node,
     * applying substitutions.
     *
     * @param subst a list containing the values to be substituted
     * @deprecated Use {@link #parseMember(String, Object...)} instead.
     */
    @Deprecated
    public ClassMember parseMember(String fmt, List<?> subst) {
        return (ClassMember) parse(MEMB, fmt, subst.toArray());
    }

    /** Parse a string into a {@code Expr} AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object... os) {
        return (Expr) parse(EXPR, fmt, os);
    }

    /**
     * Parse a string into a {@code Expr} AST node,
     * applying substitutions.
     *
     * @param subst a list containing the values to be substituted
     * @deprecated Use {@link #parseExpr(String, Object...)} instead.
     */
    @Deprecated
    public Expr parseExpr(String fmt, List<?> subst) {
        return (Expr) parse(EXPR, fmt, subst.toArray());
    }

    /** Parse a string into a {@code Stmt} AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object... os) {
        return (Stmt) parse(STMT, fmt, os);
    }

    /**
     * Parse a string into a {@code Stmt} AST node,
     * applying substitutions.
     *
     * @param subst a list containing the values to be substituted
     * @deprecated Use {@link #parseStmt(String, Object...)} instead.
     */
    @Deprecated
    public Stmt parseStmt(String fmt, List<?> subst) {
        return (Stmt) parse(STMT, fmt, subst.toArray());
    }

    /** Parse a string into a {@code TypeNode} AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object... os) {
        return (TypeNode) parse(TYPE, fmt, os);
    }

    /**
     * Parse a string into a {@code TypeNode} AST node,
     * applying substitutions.
     *
     * @param subst a list containing the values to be substituted
     * @deprecated Use {@link #parseType(String, Object...)} instead.
     */
    @Deprecated
    public TypeNode parseType(String fmt, List<?> subst) {
        return (TypeNode) parse(TYPE, fmt, subst.toArray());
    }

    /** Create a lexer that performs the substitutions in {@code subst}. */
    protected Lexer lexer(String fmt, Position pos, Object... subst) {
        return new polyglot.qq.Lexer_c(fmt, pos, subst);
    }

    /** Create a quasiquoting parser. */
    protected QQParser parser(Lexer lexer, TypeSystem ts, NodeFactory nf, ErrorQueue eq) {
        return new polyglot.qq.Grm(lexer, ts, nf, eq);
    }

    /** Return a position for a type object. Generate one if the type doesn't have one. */
    protected Position type_position(polyglot.types.TypeObject t) {
        Position p = t.position();
        if (p == null) p = Position.compilerGenerated(0);
        return p;
    }

    /** Parse a string into an AST node of the given type,
     * applying substitutions. */
    protected Node parse(int kind, String fmt, Object... subst) {
        TypeSystem ts = ext.typeSystem();
        NodeFactory nf = ext.nodeFactory();
        ErrorQueue eq = ext.compiler().errorQueue();

        // Replace Types with TypeNodes
        for (int i = 0; i < subst.length; i++) {
            Object o = subst[i];

            if (o instanceof Type) {
                Type t = (Type) o;
                subst[i] = nf.CanonicalTypeNode(type_position(t), t);
            } else if (o instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> l = (List<Object>) o;

                for (ListIterator<Object> j = l.listIterator(); j.hasNext(); ) {
                    Object p = j.next();

                    if (p instanceof Type) {
                        Type t = (Type) p;
                        j.set(nf.CanonicalTypeNode(type_position(t), t));
                    }
                }
            }
        }

        Position pos = this.pos;

        if (pos == null) {
            // this method is frame 1
            // parseXXX is frame 2
            // the client of QQ is frame 3
            pos = Position.compilerGenerated(3);
        }

        polyglot.lex.Lexer lexer = lexer(fmt, pos, subst);
        QQParser grm = parser(lexer, ts, nf, eq);

        if (Report.should_report(polyglot.frontend.Topics.qq, 1)) {
            Report.report(1, "qq: " + fmt);
            Report.report(1, "subst: " + subst);
        }

        try {
            java_cup.runtime.Symbol sym;

            switch (kind) {
                case EXPR:
                    sym = grm.qq_expr();
                    break;
                case STMT:
                    sym = grm.qq_stmt();
                    break;
                case TYPE:
                    sym = grm.qq_type();
                    break;
                case MEMB:
                    sym = grm.qq_member();
                    break;
                case DECL:
                    sym = grm.qq_decl();
                    break;
                case FILE:
                    sym = grm.qq_file();
                    break;
                default:
                    throw new QQError("bad quasi-quoting kind: " + kind, pos);
            }

            if (sym != null && sym.value instanceof Node) {
                Node n = (Node) sym.value;
                if (Report.should_report(polyglot.frontend.Topics.qq, 1))
                    Report.report(1, "result: " + n);
                return n;
            }

            throw new QQError("Unable to parse: \"" + fmt + "\".", pos);
        } catch (IOException e) {
            throw new QQError("Unable to parse: \"" + fmt + "\".", pos);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Used by cup to indicate a non-recoverable error.
            throw new QQError("Unable to parse: \"" + fmt + "\"; " + e.getMessage(), pos);
        }
    }
}
