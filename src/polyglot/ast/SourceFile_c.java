package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import jltools.types.Package;
import java.util.*;

/**
 * A <code>SourceFile</code> is an immutable representations of a Java
 * langauge source file.  It consists of a package name, a list of 
 * <code>Import</code>s, and a list of <code>GlobalDeclaration</code>s.
 */
public class SourceFile_c extends Node_c implements SourceFile
{
    protected PackageNode package_;
    protected List imports;
    protected List decls;

    public SourceFile_c(Ext ext, Position pos, PackageNode package_, List imports, List decls) {
	super(ext, pos);
	this.package_ = package_;
	this.imports = TypedList.copyAndCheck(imports, Import.class, true);
	this.decls = TypedList.copyAndCheck(decls, TopLevelDecl.class, true);
    }

    public PackageNode package_() {
	return this.package_;
    }

    public SourceFile package_(PackageNode package_) {
	SourceFile_c n = (SourceFile_c) copy();
	n.package_ = package_;
	return n;
    }

    public List imports() {
	return Collections.unmodifiableList(this.imports);
    }

    public SourceFile imports(List imports) {
	SourceFile_c n = (SourceFile_c) copy();
	n.imports = TypedList.copyAndCheck(imports, Import.class, true);
	return n;
    }

    public List decls() {
	return Collections.unmodifiableList(this.decls);
    }

    public SourceFile decls(List decls) {
	SourceFile_c n = (SourceFile_c) copy();
	n.decls = TypedList.copyAndCheck(decls, TopLevelDecl.class, true);
	return n;
    }

    protected SourceFile_c reconstruct(PackageNode package_, List imports, List decls) {
	if (package_ != this.package_ || ! CollectionUtil.equals(imports, this.imports) || ! CollectionUtil.equals(decls, this.decls)) {
	    SourceFile_c n = (SourceFile_c) copy();
	    n.package_ = package_;
	    n.imports = TypedList.copyAndCheck(imports, Import.class, true);
	    n.decls = TypedList.copyAndCheck(decls, TopLevelDecl.class, true);
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
        PackageNode package_ = null;

	if (this.package_ != null) {
	    package_ = (PackageNode) this.package_.visit(v);
	}

	List imports = new ArrayList(this.imports.size());
	for (Iterator i = this.imports.iterator(); i.hasNext(); ) {
	    Import n = (Import) i.next();
	    n = (Import) n.visit(v);
	    imports.add(n);
	}

	List decls = new ArrayList(this.decls.size());
	for (Iterator i = this.decls.iterator(); i.hasNext(); ) {
	    TopLevelDecl n = (TopLevelDecl) i.next();
	    n = (TopLevelDecl) n.visit(v);
	    decls.add(n);
	}

	return reconstruct(package_, imports, decls);
    }

    // Set the package before we recurse into the declarations.
    public Node buildTypesOverride_(TypeBuilder tb) throws SemanticException {
        if (package_ != null) {
	    tb.setPackage(package_.package_());
	}
	else {
	    tb.setPackage(null);
	}

	return null;
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	Set names = new HashSet();
	boolean hasPublic = false;

	for (Iterator i = decls.iterator(); i.hasNext();) {
	    TopLevelDecl d = (TopLevelDecl) i.next();
	    String s = d.name();

	    if (names.contains(s)) {
		throw new SemanticException("Duplicate declaration: \"" + s + 
		    "\".", d.position());
	    }

	    names.add(s);

	    if (d.flags().isPublic()) {
		if (hasPublic) {
		    throw new SemanticException(
			"The source contains more than one public declaration.",
			d.position());
		}

		hasPublic = true;
	    }
	}
     
	return this;
    }

    public String toString() {
	return "/* source file */";
    }

    public void translate_(CodeWriter w, Translator tr) {
	if (package_ != null) {
	    w.write("package ");
	    package_.ext().translate(w, tr);
	    w.write(";");
	    w.newline(0);
	    w.newline(0);
	}

	for (Iterator i = imports.iterator(); i.hasNext(); ) {
	    Import im = (Import) i.next();
	    im.ext().translate(w, tr);
	}
	 
	if (! imports.isEmpty()) {
	    w.newline(0);
	}

	for (Iterator i = decls.iterator(); i.hasNext(); ) {
	    ClassDecl cd = (ClassDecl) i.next();
	    cd.ext().translate(w, tr);
	    w.newline(0);
	}
    }
}
