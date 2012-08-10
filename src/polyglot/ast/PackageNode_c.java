/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.Package;
import polyglot.types.Qualifier;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;

/**
 * A <code>PackageNode</code> is the syntactic representation of a 
 * Java package within the abstract syntax tree.
 */
public class PackageNode_c extends Node_c implements PackageNode {
    protected Package package_;

    public PackageNode_c(Position pos, Package package_) {
        super(pos);
        assert (package_ != null);
        this.package_ = package_;
    }

    @Override
    public boolean isDisambiguated() {
        return package_ != null && package_.isCanonical()
                && super.isDisambiguated();
    }

    /** Get the package as a qualifier. */
    @Override
    public Qualifier qualifier() {
        return this.package_;
    }

    /** Get the package. */
    @Override
    public Package package_() {
        return this.package_;
    }

    /** Set the package. */
    @Override
    public PackageNode package_(Package package_) {
        PackageNode_c n = (PackageNode_c) copy();
        n.package_ = package_;
        return n;
    }

    /** Write the package name to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (package_ == null) {
            w.write("<unknown-package>");
        }
        else {
            package_.print(w);
        }
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        w.write(package_.translate(tr.context()));
    }

    @Override
    public String toString() {
        return package_.toString();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.PackageNode(this.position, this.package_);
    }

    @Override
    public Node copy(ExtensionInfo extInfo) throws SemanticException {
        PackageNode pn = (PackageNode) this.del().copy(extInfo.nodeFactory());
        if (pn.package_() != null) {
            pn =
                    pn.package_(extInfo.typeSystem()
                                       .packageForName(pn.package_().fullName()));
        }
        return pn;
    }

}
