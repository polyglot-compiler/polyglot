package jltools.ast;

import java.util.List;

/**
 * A <code>SourceFile</code> is an immutable representations of a Java
 * langauge source file.  It consists of a package name, a list of 
 * <code>Import</code>s, and a list of <code>GlobalDecl</code>s.
 */
public interface SourceFile extends Node
{
    PackageNode package_();
    SourceFile package_(PackageNode package_);

    List imports();
    SourceFile imports(List imports);

    List decls();
    SourceFile decls(List decls);
}
