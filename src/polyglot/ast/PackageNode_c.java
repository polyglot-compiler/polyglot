package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import jltools.types.Package;

/**
 * A <code>PackageNode</code> is the syntactic representation of a 
 * Java package within the abstract syntax tree.
 */
public class PackageNode_c extends Node_c implements PackageNode
{
    protected Package package_;

    public PackageNode_c(Del ext, Position pos, Package package_) {
	super(ext, pos);
	this.package_ = package_;
    }

    /** Get the package as a qualifier. */
    public Qualifier qualifier() {
        return this.package_;
    }

    /** Get the package. */
    public Package package_() {
	return this.package_;
    }

    /** Set the package. */
    public PackageNode package_(Package package_) {
	PackageNode_c n = (PackageNode_c) copy();
	n.package_ = package_;
	return n;
    }

    /** Write the package name to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (package_ == null) w.write("<unknown-package>");
        else w.write(package_.toString());
    }

    public void translate(CodeWriter w, Translator tr) {
        w.write(tr.typeSystem().translatePackage(tr.context(), package_));
    }

    public String toString() {
        return package_.toString();
    }
}
