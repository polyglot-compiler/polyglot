package polyglot.ast;

import polyglot.types.ImportTable;
import polyglot.frontend.Source;
import java.util.List;

/**
 * A <code>SourceFile</code> is an immutable representations of a Java
 * langauge source file.  It consists of a package name, a list of 
 * <code>Import</code>s, and a list of <code>GlobalDecl</code>s.
 */
public interface SourceFile extends Node
{
    /** Get the source's declared package. */
    PackageNode package_();

    /** Set the source's declared package. */
    SourceFile package_(PackageNode package_);

    /** Get the source's declared imports. */
    List imports();

    /** Set the source's declared imports. */
    SourceFile imports(List imports);

    /** Get the source's top-level declarations. */
    List decls();

    /** Set the source's top-level declarations. */
    SourceFile decls(List decls);

    /** Get the source's import table. */
    ImportTable importTable();

    /** Set the source's import table. */
    SourceFile importTable(ImportTable importTable);
 
    /** Get the source file. */
    Source source();

    /** Set the source file. */
    SourceFile source(Source source);
}
