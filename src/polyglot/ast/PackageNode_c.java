/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.frontend.ExtensionInfo;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.Package;
import polyglot.types.Qualifier;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;

/**
 * A {@code PackageNode} is the syntactic representation of a 
 * Java package within the abstract syntax tree.
 */
public class PackageNode_c extends Node_c implements PackageNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Package package_;

//    @Deprecated
    public PackageNode_c(Position pos, Package package_) {
        this(pos, package_, null);
    }

    public PackageNode_c(Position pos, Package package_, Ext ext) {
        super(pos, ext);
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
        return package_();
    }

    @Override
    public Package package_() {
        return this.package_;
    }

    @Override
    public PackageNode package_(Package package_) {
        return package_(this, package_);
    }

    protected <N extends PackageNode_c> N package_(N n, Package package_) {
        if (n.package_ == package_) return n;
        n = copyIfNeeded(n);
        n.package_ = package_;
        return n;
    }

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
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        PackageNode_c n = (PackageNode_c) super.extRewrite(rw);
        Package p = package_();
        p = rw.to_ts().packageForName(p.fullName());
        n = package_(n, p);
        return n;
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
        PackageNode_c pn =
                (PackageNode_c) extInfo.nodeFactory()
                                       .lang()
                                       .copy(this, extInfo.nodeFactory());
        if (pn.package_() != null) {
            pn =
                    package_(pn,
                             extInfo.typeSystem().packageForName(pn.package_()
                                                                   .fullName()));
        }
        return pn;
    }

}
