package jltools.ext.jl.qq;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.frontend.*;

import jltools.ext.jl.qq.Lexer;
import jltools.ext.jl.qq.Grm;

import java.util.*;
import java.io.*;

public class QQ {
    protected ExtensionInfo ext;
    protected Position pos;

    static Collection topics = new ArrayList();

    static {
        topics.add("qq");
    }

    public static void report(int level, String msg) {
        jltools.main.Report.report(topics, level, msg);
    }

    protected static final int EXPR = 0;
    protected static final int STMT = 1;
    protected static final int TYPE = 2;
    protected static final int MEMB = 3;
    protected static final int DECL = 4;
    protected static final int FILE = 5;

    public QQ(ExtensionInfo ext) {
        this(ext, null);
    }

    public QQ(ExtensionInfo ext, Position pos) {
        this.ext = ext;
        this.pos = pos;
    }

    public SourceFile parseFile(String text) throws SemanticException {
        return parseFile(new HashMap(), text);
    }

    public SourceFile parseFile(Map subst, String text) throws SemanticException {
        return (SourceFile) parse(subst, text, FILE);
    }

    public ClassDecl parseDecl(String text) throws SemanticException {
        return parseDecl(new HashMap(), text);
    }

    public ClassDecl parseDecl(Map subst, String text) throws SemanticException {
        return (ClassDecl) parse(subst, text, DECL);
    }

    public ClassMember parseMember(String text) throws SemanticException {
        return parseMember(new HashMap(), text);
    }

    public ClassMember parseMember(Map subst, String text) throws SemanticException {
        return (ClassMember) parse(subst, text, MEMB);
    }

    public Expr parseExpr(String text) throws SemanticException {
        return parseExpr(new HashMap(), text);
    }

    public Expr parseExpr(Map subst, String text) throws SemanticException {
        return (Expr) parse(subst, text, EXPR);
    }

    public Stmt parseStmt(String text) throws SemanticException {
        return parseStmt(new HashMap(), text);
    }

    public Stmt parseStmt(Map subst, String text) throws SemanticException {
        return (Stmt) parse(subst, text, STMT);
    }

    public TypeNode parseType(String text) throws SemanticException {
        return parseType(new HashMap(), text);
    }

    public TypeNode parseType(Map subst, String text) throws SemanticException {
        return (TypeNode) parse(subst, text, TYPE);
    }

    protected Node parse(Map subst, String text, int kind) throws SemanticException {
        jltools.ext.jl.qq.Lexer lexer;
        jltools.ext.jl.qq.Grm grm;

        TypeSystem ts = ext.typeSystem();
        NodeFactory nf = ext.nodeFactory();

        lexer = new jltools.ext.jl.qq.Lexer(text, pos);
        grm = new jltools.ext.jl.qq.Grm(lexer, ts, nf, subst);

        report(1, "qq: " + text);
        report(1, "subst: " + subst);

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
                    throw new InternalCompilerError("bad kind: " + kind);
            }

            if (sym != null && sym.value instanceof Node) {
                Node n = (Node) sym.value;
                report(1, "result: " + n);
                return n;
            }

            throw new SemanticException("Unable to parse: \"" + text + "\".");
        }
        catch (IOException e) {
            throw new SemanticException("Unable to parse: \"" + text + "\".");
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            // Used by cup to indicate a non-recoverable error.
            throw new SemanticException("Unable to parse: \"" + text + "\"; " +
                                        e.getMessage());
        }
    }
}
