package polyglot.ast;

import polyglot.types.Package;

/**
 * A <code>PackageNode</code> is the syntactic representation of a 
 * Java package within the abstract syntax tree.
 */
public interface PackageNode extends Node, Prefix, QualifierNode
{
    Package package_();
    PackageNode package_(Package p);
}
