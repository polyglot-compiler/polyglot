package polyglot.ext.jl.qq;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.main.Report;
import polyglot.lex.Lexer;

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
        this(ext, Position.COMPILER_GENERATED);
    }

    public QQ(ExtensionInfo ext, Position pos) {
        this.ext = ext;
        this.pos = pos;
    }

    private List list() { return Collections.EMPTY_LIST; }
    private List list(Object o1) { return Collections.singletonList(o1); }

    private List list(Object o1, Object o2) {
        return list(new Object[] { o1, o2 });
    }
 
    private List list(Object o1, Object o2, Object o3) {
        return list(new Object[] { o1, o2, o3 });
    }

    private List list(Object o1, Object o2, Object o3, Object o4) {
        return list(new Object[] { o1, o2, o3, o4 });
    }

    private List list(Object o1, Object o2, Object o3, Object o4, Object o5) {
        return list(new Object[] { o1, o2, o3, o4, o5 });
    }

    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6 });
    }

    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6, o7 });
    }

    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6, o7, o8 });
    }

    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6, o7, o8, o9 });
    }

    private List list(Object[] os) {
        return Arrays.asList(os);
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt) {
        return parseFile(fmt, list());
    }

    public SourceFile parseFile(String fmt, Object o1) {
        return parseFile(fmt, list(o1));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2) {
        return parseFile(fmt, list(o1, o2));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3) {
        return parseFile(fmt, list(o1, o2, o3));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseFile(fmt, list(o1, o2, o3, o4));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseFile(fmt, list(o1, o2, o3, o4, o5));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    public SourceFile parseFile(String fmt, Object[] os) {
        return parseFile(fmt, list(os));
    }

    public SourceFile parseFile(String fmt, List subst) {
        return (SourceFile) parse(fmt, subst, FILE);
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt) {
        return parseDecl(fmt, list());
    }

    public ClassDecl parseDecl(String fmt, Object o1) {
        return parseDecl(fmt, list(o1));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2) {
        return parseDecl(fmt, list(o1, o2));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3) {
        return parseDecl(fmt, list(o1, o2, o3));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseDecl(fmt, list(o1, o2, o3, o4));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseDecl(fmt, list(o1, o2, o3, o4, o5));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    public ClassDecl parseDecl(String fmt, Object[] os) {
        return parseDecl(fmt, list(os));
    }

    public ClassDecl parseDecl(String fmt, List subst) {
        return (ClassDecl) parse(fmt, subst, DECL);
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt) {
        return parseMember(fmt, list());
    }

    public ClassMember parseMember(String fmt, Object o1) {
        return parseMember(fmt, list(o1));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2) {
        return parseMember(fmt, list(o1, o2));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3) {
        return parseMember(fmt, list(o1, o2, o3));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseMember(fmt, list(o1, o2, o3, o4));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseMember(fmt, list(o1, o2, o3, o4, o5));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    public ClassMember parseMember(String fmt, Object[] os) {
        return parseMember(fmt, list(os));
    }

    public ClassMember parseMember(String fmt, List subst) {
        return (ClassMember) parse(fmt, subst, MEMB);
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt) {
        return parseExpr(fmt, list());
    }

    public Expr parseExpr(String fmt, Object o1) {
        return parseExpr(fmt, list(o1));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2) {
        return parseExpr(fmt, list(o1, o2));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3) {
        return parseExpr(fmt, list(o1, o2, o3));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseExpr(fmt, list(o1, o2, o3, o4));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseExpr(fmt, list(o1, o2, o3, o4, o5));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    public Expr parseExpr(String fmt, Object[] os) {
        return parseExpr(fmt, list(os));
    }

    public Expr parseExpr(String fmt, List subst) {
        return (Expr) parse(fmt, subst, EXPR);
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt) {
        return parseStmt(fmt, list());
    }

    public Stmt parseStmt(String fmt, Object o1) {
        return parseStmt(fmt, list(o1));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2) {
        return parseStmt(fmt, list(o1, o2));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3) {
        return parseStmt(fmt, list(o1, o2, o3));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseStmt(fmt, list(o1, o2, o3, o4));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseStmt(fmt, list(o1, o2, o3, o4, o5));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    public Stmt parseStmt(String fmt, Object[] os) {
        return parseStmt(fmt, list(os));
    }

    public Stmt parseStmt(String fmt, List subst) {
        return (Stmt) parse(fmt, subst, STMT);
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt) {
        return parseType(fmt, list());
    }

    public TypeNode parseType(String fmt, Object o1) {
        return parseType(fmt, list(o1));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2) {
        return parseType(fmt, list(o1, o2));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3) {
        return parseType(fmt, list(o1, o2, o3));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseType(fmt, list(o1, o2, o3, o4));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseType(fmt, list(o1, o2, o3, o4, o5));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    public TypeNode parseType(String fmt, Object[] os) {
        return parseType(fmt, list(os));
    }

    public TypeNode parseType(String fmt, List subst) {
        return (TypeNode) parse(fmt, subst, TYPE);
    }

    protected Lexer lexer(String fmt, Position pos, List subst) {
        return new polyglot.ext.jl.qq.Lexer_c(fmt, pos, subst);
    }

    protected QQParser parser(Lexer lexer, TypeSystem ts, NodeFactory nf, ErrorQueue eq) {
        return new polyglot.ext.jl.qq.Grm(lexer, ts, nf, eq);
    }

    /** Parse a string into an AST node of the given type,
     * applying substitutions. */
    protected Node parse(String fmt, List subst, int kind) {
        TypeSystem ts = ext.typeSystem();
        NodeFactory nf = ext.nodeFactory();
        ErrorQueue eq = ext.compiler().errorQueue();

        // Replace Types with TypeNodes
        for (ListIterator i = subst.listIterator(); i.hasNext(); ) {
            Object o = i.next();

            if (o instanceof Type) {
                Type t = (Type) o;
                i.set(nf.CanonicalTypeNode(t.position(), t));
            }
            else if (o instanceof List) {
                List l = (List) o;

                for (ListIterator j = l.listIterator(); j.hasNext(); ) {
                    Object p = j.next();

                    if (p instanceof Type) {
                        Type t = (Type) p;
                        j.set(nf.CanonicalTypeNode(t.position(), t));
                    }
                }
            }
        }

        polyglot.lex.Lexer lexer = lexer(fmt, pos, subst);
        QQParser grm = parser(lexer, ts, nf, eq);

        if (Report.should_report(polyglot.ext.jl.Topics.qq, 1)) {
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
                if (Report.should_report(polyglot.ext.jl.Topics.qq, 1))
		    Report.report(1, "result: " + n);
                return n;
            }

            throw new QQError("Unable to parse: \"" + fmt + "\".", pos);
        }
        catch (IOException e) {
            throw new QQError("Unable to parse: \"" + fmt + "\".", pos);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            // Used by cup to indicate a non-recoverable error.
            throw new QQError("Unable to parse: \"" + fmt + "\"; " +
                              e.getMessage(), pos);
        }
    }
}
