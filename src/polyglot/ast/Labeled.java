package polyglot.ast;

/**
 * Am immutable representation of a Java statement with a label.  A labeled
 * statement contains the statement being labelled and a string label.
 */
public interface Labeled extends Stmt 
{
    String label();
    Labeled label(String label);

    Stmt statement();
    Labeled statement(Stmt statement);
}
