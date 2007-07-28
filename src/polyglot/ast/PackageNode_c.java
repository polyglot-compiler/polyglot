/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.ast.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import polyglot.types.Package;

/**
 * A <code>PackageNode</code> is the syntactic representation of a 
 * Java package within the abstract syntax tree.
 */
public class PackageNode_c extends Node_c implements PackageNode
{
    protected Package package_;

    public PackageNode_c(Position pos, Package package_) {
	super(pos);
	assert(package_ != null);
	this.package_ = package_;
    }
    
    public boolean isDisambiguated() {
        return package_ != null && package_.isCanonical() && super.isDisambiguated();
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
        if (package_ == null) {
            w.write("<unknown-package>");
        }
        else {
	    package_.print(w);
        }
    }
    
    public void translate(CodeWriter w, Translator tr) {
        w.write(package_.translate(tr.context()));
    }

    public String toString() {
        return package_.toString();
    }
    
    public Node copy(NodeFactory nf) {
        return nf.PackageNode(this.position, this.package_);
    }
    public Node copy(ExtensionInfo extInfo) throws SemanticException {
        PackageNode pn = (PackageNode)this.del().copy(extInfo.nodeFactory());
        if (pn.package_() != null) {
            pn = pn.package_(extInfo.typeSystem().packageForName(pn.package_().fullName()));
        }
        return pn;
    }


}
