package jltools.ast;

/**
 * An <code>AmbTypeNode</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a type.
 */
public interface AmbTypeNode extends TypeNode, Ambiguous
{
    QualifierNode qual();
    AmbTypeNode qual(QualifierNode qual);
    String name();
    AmbTypeNode name(String name);
}
