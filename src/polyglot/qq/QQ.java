package polyglot.ext.jl.qq;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.main.Report;

import polyglot.ext.jl.qq.Lexer_c;
import polyglot.ext.jl.qq.Grm;

import java.util.*;
import java.io.*;

/** JL quasiquoter.  Contains methods for parsing strings into ASTs. */
public class QQ {
    protected ExtensionInfo ext;
    protected Position pos;

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

    /** Parse a string into a <code>SourceFile</code> AST node. */
    public SourceFile parseFile(String text) throws SemanticException {
        return parseFile(new HashMap(), text);
    }

    /** Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions. */
    public SourceFile parseFile(Map subst, String text) throws SemanticException {
        return (SourceFile) parse(subst, text, FILE);
    }

    /** Parse a string into a <code>ClassDecl</code> AST node. */
    public ClassDecl parseDecl(String text) throws SemanticException {
        return parseDecl(new HashMap(), text);
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(Map subst, String text) throws SemanticException {
        return (ClassDecl) parse(subst, text, DECL);
    }

    /** Parse a string into a <code>ClassMember</code> AST node. */
    public ClassMember parseMember(String text) throws SemanticException {
        return parseMember(new HashMap(), text);
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(Map subst, String text) throws SemanticException {
        return (ClassMember) parse(subst, text, MEMB);
    }

    /** Parse a string into a <code>Expr</code> AST node. */
    public Expr parseExpr(String text) throws SemanticException {
        return parseExpr(new HashMap(), text);
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(Map subst, String text) throws SemanticException {
        return (Expr) parse(subst, text, EXPR);
    }

    /** Parse a string into a <code>Stmt</code> AST node. */
    public Stmt parseStmt(String text) throws SemanticException {
        return parseStmt(new HashMap(), text);
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(Map subst, String text) throws SemanticException {
        return (Stmt) parse(subst, text, STMT);
    }

    /** Parse a string into a <code>TypeNode</code> AST node. */
    public TypeNode parseType(String text) throws SemanticException {
        return parseType(new HashMap(), text);
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(Map subst, String text) throws SemanticException {
        return (TypeNode) parse(subst, text, TYPE);
    }

    /** Parse a string into an AST node of the given type,
     * applying substitutions. */
    protected Node parse(Map subst, String text, int kind) throws SemanticException {

        TypeSystem ts = ext.typeSystem();
        NodeFactory nf = ext.nodeFactory();

        polyglot.lex.Lexer lexer =
	    new polyglot.ext.jl.qq.Lexer_c(text, pos);
        polyglot.ext.jl.qq.Grm grm =
	    new polyglot.ext.jl.qq.Grm(lexer, ts, nf, subst);

        if (Report.should_report(polyglot.ext.jl.Topics.qq, 1)) {
	    Report.report(1, "qq: " + text);
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
                    throw new InternalCompilerError("bad kind: " + kind);
            }

            if (sym != null && sym.value instanceof Node) {
                Node n = (Node) sym.value;
                if (Report.should_report(polyglot.ext.jl.Topics.qq, 1))
		    Report.report(1, "result: " + n);
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
