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

/**
  Java language quasiquoter.  This class contains methods for parsing strings
  into ASTs.

  <p>
  To use the class, invoke one of the <code>parseT</code> methods to create a
  <code>Node</code> of type <code>T</code>.

  <p>
  Each <code>parseT</code> method takes a format string as its first argument
  and some additional <code>Object</code> arguments.  Each pattern in the
  format string is matched with its corresponding <code>Object</code>.

  <p>
  The format string may contain the following patterns:
  <ul>
    <li> <code>%s</code> matches <code>String (parsed as an identifier)

    <li> <code>%T</code> matches <code>Type</code> or <code>TypeNode></code>
    <li> <code>%E</code> matches <code>Expr</code>
    <li> <code>%S</code> matches <code>Stmt</code>
    <li> <code>%C</code> matches <code>ClassDecl</code>
    <li> <code>%M</code> matches <code>ClassMember</code>
    <li> <code>%F</code> matches <code>Formal</code>

    <li> <code>%LT</code> matches <code>List&lt;Type&gt;</code> or <code>List&lt;TypeNode&gt;</code>
    <li> <code>%LE</code> matches <code>List&lt;Expr&gt;</code>
    <li> <code>%LS</code> matches <code>List&lt;Stmt&gt;</code>
    <li> <code>%LC</code> matches <code>List&lt;ClassDecl&gt;</code>
    <li> <code>%LM</code> matches <code>List&lt;ClassMember&gt;</code>
    <li> <code>%LF</code> matches <code>List&lt;Formal&gt;</code>
  </ul>
  These patterns are recognized as tokens by the lexer--surrounding the token
  with whitespace or parens may be needed to parse the string.

  <p>
  For example:
  <pre>
      Expr e;
      TypeNode t;
      Stmt s = qq.parseStmt("%T %s = new %T(%E);", t, "tmp", t, e);
  </pre>
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
      language extension. */
    public QQ(ExtensionInfo ext) {
        this(ext, Position.COMPILER_GENERATED);
    }

    /** Create a new quasiquoter to create AST nodes of the given
      language extension, using <code>pos</code> for the position
      of the nodes created. */
    public QQ(ExtensionInfo ext, Position pos) {
        this.ext = ext;
        this.pos = pos;
    }

    /** Create an empty list. */
    private List list() { return Collections.EMPTY_LIST; }

    /** Create a singleton list. */
    private List list(Object o1) { return Collections.singletonList(o1); }

    /** Create a 2-element list. */
    private List list(Object o1, Object o2) {
        return list(new Object[] { o1, o2 });
    }
 
    /** Create a 3-element list. */
    private List list(Object o1, Object o2, Object o3) {
        return list(new Object[] { o1, o2, o3 });
    }

    /** Create a 4-element list. */
    private List list(Object o1, Object o2, Object o3, Object o4) {
        return list(new Object[] { o1, o2, o3, o4 });
    }

    /** Create a 5-element list. */
    private List list(Object o1, Object o2, Object o3, Object o4, Object o5) {
        return list(new Object[] { o1, o2, o3, o4, o5 });
    }

    /** Create a 6-element list. */
    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6 });
    }

    /** Create a 7-element list. */
    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6, o7 });
    }

    /** Create a 8-element list. */
    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6, o7, o8 });
    }

    /** Create a 9-element list. */
    private List list(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9) {
        return list(new Object[] { o1, o2, o3, o4, o5, o6, o7, o8, o9 });
    }

    /** Create a list from an array. */
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

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1) {
        return parseFile(fmt, list(o1));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2) {
        return parseFile(fmt, list(o1, o2));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3) {
        return parseFile(fmt, list(o1, o2, o3));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseFile(fmt, list(o1, o2, o3, o4));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseFile(fmt, list(o1, o2, o3, o4, o5));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
    {
        return parseFile(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, Object[] os) {
        return parseFile(fmt, list(os));
    }

    /**
     * Parse a string into a <code>SourceFile</code> AST node,
     * applying substitutions.
     */
    public SourceFile parseFile(String fmt, List subst) {
        return (SourceFile) parse(fmt, subst, FILE);
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt) {
        return parseDecl(fmt, list());
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1) {
        return parseDecl(fmt, list(o1));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2) {
        return parseDecl(fmt, list(o1, o2));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3) {
        return parseDecl(fmt, list(o1, o2, o3));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseDecl(fmt, list(o1, o2, o3, o4));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseDecl(fmt, list(o1, o2, o3, o4, o5));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseDecl(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, Object[] os) {
        return parseDecl(fmt, list(os));
    }

    /** Parse a string into a <code>ClassDecl</code> AST node,
     * applying substitutions. */
    public ClassDecl parseDecl(String fmt, List subst) {
        return (ClassDecl) parse(fmt, subst, DECL);
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt) {
        return parseMember(fmt, list());
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1) {
        return parseMember(fmt, list(o1));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2) {
        return parseMember(fmt, list(o1, o2));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3) {
        return parseMember(fmt, list(o1, o2, o3));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseMember(fmt, list(o1, o2, o3, o4));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseMember(fmt, list(o1, o2, o3, o4, o5));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseMember(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, Object[] os) {
        return parseMember(fmt, list(os));
    }

    /** Parse a string into a <code>ClassMember</code> AST node,
     * applying substitutions. */
    public ClassMember parseMember(String fmt, List subst) {
        return (ClassMember) parse(fmt, subst, MEMB);
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt) {
        return parseExpr(fmt, list());
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1) {
        return parseExpr(fmt, list(o1));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2) {
        return parseExpr(fmt, list(o1, o2));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3) {
        return parseExpr(fmt, list(o1, o2, o3));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseExpr(fmt, list(o1, o2, o3, o4));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseExpr(fmt, list(o1, o2, o3, o4, o5));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseExpr(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, Object[] os) {
        return parseExpr(fmt, list(os));
    }

    /** Parse a string into a <code>Expr</code> AST node,
     * applying substitutions. */
    public Expr parseExpr(String fmt, List subst) {
        return (Expr) parse(fmt, subst, EXPR);
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt) {
        return parseStmt(fmt, list());
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1) {
        return parseStmt(fmt, list(o1));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2) {
        return parseStmt(fmt, list(o1, o2));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3) {
        return parseStmt(fmt, list(o1, o2, o3));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseStmt(fmt, list(o1, o2, o3, o4));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseStmt(fmt, list(o1, o2, o3, o4, o5));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseStmt(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, Object[] os) {
        return parseStmt(fmt, list(os));
    }

    /** Parse a string into a <code>Stmt</code> AST node,
     * applying substitutions. */
    public Stmt parseStmt(String fmt, List subst) {
        return (Stmt) parse(fmt, subst, STMT);
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt) {
        return parseType(fmt, list());
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1) {
        return parseType(fmt, list(o1));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2) {
        return parseType(fmt, list(o1, o2));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3) {
        return parseType(fmt, list(o1, o2, o3));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4)
    {
        return parseType(fmt, list(o1, o2, o3, o4));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5)
    {
        return parseType(fmt, list(o1, o2, o3, o4, o5));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6)
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7)
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6, o7));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8)
       
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object o1, Object o2, Object o3,
        Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
       
    {
        return parseType(fmt, list(o1, o2 , o3, o4, o5, o6, o7, o8, o9));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, Object[] os) {
        return parseType(fmt, list(os));
    }

    /** Parse a string into a <code>TypeNode</code> AST node,
     * applying substitutions. */
    public TypeNode parseType(String fmt, List subst) {
        return (TypeNode) parse(fmt, subst, TYPE);
    }

    /** Create a lexer that performs the substitutions in <code>subst</code>. */
    protected Lexer lexer(String fmt, Position pos, List subst) {
        return new polyglot.ext.jl.qq.Lexer_c(fmt, pos, subst);
    }

    /** Create a quasiquoting parser. */
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
