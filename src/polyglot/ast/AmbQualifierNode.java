package jltools.ast;

/**
 * An <code>AmbQualifier</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers.  It must resolve to a type qualifier.
 */
public interface AmbQualifierNode extends Ambiguous, QualifierNode
{
    QualifierNode qual();
    String name();
}
