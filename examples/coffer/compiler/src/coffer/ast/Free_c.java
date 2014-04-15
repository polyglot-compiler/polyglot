/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Stmt_c;
import polyglot.ast.Term;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;
import coffer.types.CofferClassType;

/**
 * This statement revokes the key associated with a tracked expression.
 * The expression cannot be evaluated after this statement executes.
 */
public class Free_c extends Stmt_c implements Free {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;

    public Free_c(Position pos, Expr expr) {
        super(pos);
        this.expr = expr;
    }

    @Override
    public Expr expr() {
        return expr;
    }

    @Override
    public Free expr(Expr expr) {
        Free_c n = (Free_c) copy();
        n.expr = expr;
        return n;
    }

    public Free_c reconstruct(Expr expr) {
        if (this.expr != expr) {
            Free_c n = (Free_c) copy();
            n.expr = expr;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        return reconstruct(expr);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Type t = expr.type();

        if (!(t instanceof CofferClassType)) {
            throw new SemanticException("Cannot free expression of non-tracked type \""
                                                + t + "\".",
                                        position());
        }

        return this;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("free ");
        print(expr, w, tr);
        w.write(";");
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        w.write(";");
    }

    @Override
    public String toString() {
        return "free " + expr + ";";
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr, this, EXIT);
        return succs;
    }
}
