package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import jltools.types.Package;

public class PackageNode_c extends Node_c implements PackageNode
{
    protected Package package_;

    public PackageNode_c(Ext ext, Position pos, Package package_) {
	super(ext, pos);
	this.package_ = package_;
    }

    public Qualifier qualifier() {
        return this.package_;
    }

    public Package package_() {
	return this.package_;
    }

    public PackageNode package_(Package package_) {
	PackageNode_c n = (PackageNode_c) copy();
	n.package_ = package_;
	return n;
    }

    public void translate_(CodeWriter w, Translator tr) {
        w.write(tr.typeSystem().translatePackage(tr.context(), package_));
    }

    public String toString() {
        return package_.toString();
    }
}
