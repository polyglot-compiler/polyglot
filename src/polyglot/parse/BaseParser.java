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

package polyglot.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import polyglot.ast.AmbExpr;
import polyglot.ast.AmbPrefix;
import polyglot.ast.AmbReceiver;
import polyglot.ast.AmbTypeNode;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Javadoc;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Prefix;
import polyglot.ast.QualifierNode;
import polyglot.ast.Receiver;
import polyglot.ast.TypeNode;
import polyglot.lex.JavadocToken;
import polyglot.lex.Lexer;
import polyglot.lex.Token;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyglot.util.Pair;
import polyglot.util.Position;

public abstract class BaseParser extends java_cup.runtime.lr_parser {
    public final Lexer lexer;
    public final ErrorQueue eq;
    public final TypeSystem ts;
    public final NodeFactory nf;
    protected Position prev_pos;
    protected Position position;

    /**
     * Keeps track of every token seen by the Parser
     */
    protected final List<Token> tokenStream;

    /**
     * Mapping from Pos(Line#, Col#) -> Index into tokenStream
     */
    protected final Map<Pair<Integer, Integer>, Integer> positionToTokenIndexMap;

    @Override
    public final Class<?> getSymbolContainer() {
        return sym.class;
    }

    public BaseParser(Lexer l, TypeSystem t, NodeFactory n, ErrorQueue q) {
        super(new ComplexSymbolFactory());
        lexer = l;
        eq = q;
        ts = t;
        nf = n;
        prev_pos = Position.compilerGenerated();
        position = Position.compilerGenerated();

        tokenStream = new ArrayList<>();
        positionToTokenIndexMap = new HashMap<>();
    }

    /**
     * The standard scanning routine for use in the CUP "scan with"
     * declaration. Should read:
     *   scan with {: return nextSymbol(); :}
     */
    public Symbol nextSymbol() throws java.io.IOException {
        Token t = lexer.nextToken();

        while (t instanceof JavadocToken) {
            updateInternal(t);
            t = lexer.nextToken();
        }

        updateInternal(t);

        return getSymbolFactory().newSymbol(t.toString(), t.symbol(), t);
    }

    private void updateInternal(Token t) {
        tokenStream.add(t);
        positionToTokenIndexMap.put(new Pair<>(t.getPosition().line(),
                                               t.getPosition().column()),
                                    tokenStream.size() - 1);

        // use two positions, since the parser does one token lookahead
        position = prev_pos;
        prev_pos = t.getPosition();
    }

    public Position position() {
        return position;
    }

    /**
     * Returns the current position of the parser, representing the position of a null production.
     */
    public Position emptyTokenPos() {
        return prev_pos.startOf();
    }

    /**
     * Override the default CUP routine.
     */
    @Override
    public void report_fatal_error(String message, Object info)
            throws Exception {
        report_error(message, info);
        die();
    }

    /**
     * Report a fatal error then abort parsing.
     */
    public void die(String msg, Position pos) throws Exception {
        report_fatal_error(msg, pos);
    }

    /**
     * Report a fatal error then abort parsing.
     */
    public void die(Position pos) throws Exception {
        report_fatal_error("Syntax error.", pos);
    }

    /**
     * Report a fatal error then abort parsing.
     */
    public void die() throws Exception {
        done_parsing();
        throw new Exception();
    }

    protected Position posForObject(Object o) {
        if (o instanceof Node) {
            return pos((Node) o);
        }
        else if (o instanceof Token) {
            return pos((Token) o);
        }
        else if (o instanceof Type) {
            return pos((Type) o);
        }
        else if (o instanceof List) {
            return pos((List<?>) o);
        }
        else if (o instanceof VarDeclarator) {
            return pos((VarDeclarator) o);
        }
        else {
            return null;
        }
    }

    public Position pos(Object first, Object last) {
        return pos(first, last, first);
    }

    public Position pos(Object first, Object last, Object noEndDefault) {
        //System.out.println("first: "+first+" class: "+first.getClass()+" last: "+last+" class: "+last.getClass());
        Position fpos = posForObject(first);
        Position epos = posForObject(last);

        if (fpos != null && epos != null) {
            if (epos.endColumn() != Position.END_UNUSED) {
                return new Position(fpos, epos);
            }

            // the end line and column are not being used in this extension.
            // so return the default for that case.
            return posForObject(noEndDefault);
        }
        if (epos == null) {
            return posForObject(noEndDefault);
        }
        return null;

    }

    /**
     * Return the position of the given Flags object
     */
    public Position pos(Flags f) {
        if (f == null || f.flags().isEmpty()) return null;
        return f.position();
    }

    /**
     * Return the position of the Token.
     */
    public Position pos(Token t) {
        if (t == null) return null;
        return t.getPosition();
    }

    /**
     * Return the source position of the Type.
     */
    public Position pos(Type n) {
        if (n == null) return null;
        return n.position();
    }

    /**
     * Return the source position of the first element in the list to the
     * last element in the list.
     */
    public Position pos(List<?> l) {
        if (l == null || l.isEmpty()) {
            return null;
        }

        return pos(l.get(0), l.get(l.size() - 1));
    }

    /**
     * Return the source position of the declaration.
     */
    public Position pos(VarDeclarator n) {
        if (n == null) return null;
        return n.pos;
    }

    /**
     * Return the source position of the Node.
     */
    public Position pos(Node n) {
        if (n == null) {
            return null;
        }
        return n.position();
    }

    /**
     * Return a TypeNode representing a {@code dims}-dimensional
     * array of {@code n}.
     */
    public TypeNode array(TypeNode n, int dims) throws Exception {
        if (dims > 0) {
            if (n instanceof CanonicalTypeNode) {
                Type t = ((CanonicalTypeNode) n).type();
                return nf.CanonicalTypeNode(pos(n), ts.arrayOf(t, dims));
            }
            return nf.ArrayTypeNode(pos(n), array(n, dims - 1));
        }
        else {
            return n;
        }
    }

    /**
     * Helper for exprToType.
     */
    protected QualifierNode prefixToQualifier(Prefix p) throws Exception {
        if (p instanceof TypeNode) {
            return typeToQualifier((TypeNode) p);
        }

        if (p instanceof Expr) {
            return exprToQualifier((Expr) p);
        }

        if (p instanceof AmbReceiver) {
            AmbReceiver a = (AmbReceiver) p;

            if (a.prefix() != null) {
                return nf.AmbQualifierNode(pos(p),
                                           prefixToQualifier(a.prefix()),
                                           nf.Id(pos(p), a.name()));
            }
            else {
                return nf.AmbQualifierNode(pos(p), nf.Id(pos(p), a.name()));
            }
        }

        if (p instanceof AmbPrefix) {
            AmbPrefix a = (AmbPrefix) p;

            if (a.prefix() != null) {
                return nf.AmbQualifierNode(pos(p),
                                           prefixToQualifier(a.prefix()),
                                           nf.Id(pos(p), a.name()));
            }
            else {
                return nf.AmbQualifierNode(pos(p), nf.Id(pos(p), a.name()));
            }
        }

        die(pos(p));
        return null;
    }

    /**
     * Helper for exprToType.
     */
    protected QualifierNode typeToQualifier(TypeNode t) throws Exception {
        if (t instanceof AmbTypeNode) {
            AmbTypeNode a = (AmbTypeNode) t;

            if (a.qualifier() != null) {
                return nf.AmbQualifierNode(pos(t),
                                           a.qual(),
                                           nf.Id(pos(t), a.name()));
            }
            else {
                return nf.AmbQualifierNode(pos(t), nf.Id(pos(t), a.name()));
            }
        }

        die(pos(t));
        return null;
    }

    /**
     * Helper for exprToType.
     */
    protected QualifierNode exprToQualifier(Expr e) throws Exception {
        if (e instanceof AmbExpr) {
            AmbExpr a = (AmbExpr) e;
            return nf.AmbQualifierNode(pos(e), nf.Id(pos(e), a.name()));
        }

        if (e instanceof Field) {
            Field f = (Field) e;
            Receiver r = f.target();
            return nf.AmbQualifierNode(pos(e),
                                       prefixToQualifier(r),
                                       nf.Id(pos(e), f.name()));
        }

        die(pos(e));
        return null;
    }

    /**
     * Convert {@code e} into a type, yielding a {@code TypeNode}.
     * This is used by the cast_expression production.
     */
    public TypeNode exprToType(Expr e) throws Exception {
        if (e instanceof AmbExpr) {
            AmbExpr a = (AmbExpr) e;
            return nf.AmbTypeNode(pos(e), nf.Id(pos(e), a.name()));
        }

        if (e instanceof Field) {
            Field f = (Field) e;
            Receiver r = f.target();
            return nf.AmbTypeNode(pos(e),
                                  prefixToQualifier(r),
                                  nf.Id(pos(e), f.name()));
        }

        die(pos(e));
        return null;
    }

    /**
     * Returns the Javadoc associated with the node at the given Position.
     */
    public Javadoc javadoc(Position pos) {
        if (pos == null) return null;

        Integer index =
                positionToTokenIndexMap.get(new Pair<>(pos.line(), pos.column()));

        if (index != null && index > 0 && index < tokenStream.size()) {
            Token token = tokenStream.get(index - 1);
            if (token instanceof JavadocToken)
                return nf.Javadoc(token.getPosition(),
                                  ((JavadocToken) token).getText());
        }

        return null;
    }

    /**
     *  Returns the Javadoc associated with the node at the first
     *  non-null Position among the pair <pos1, pos2> in that order.
     */
    public Javadoc javadoc(Position pos1, Position pos2) {
        Position pos = pos1 != null ? pos1 : pos2;
        return javadoc(pos);
    }
}
