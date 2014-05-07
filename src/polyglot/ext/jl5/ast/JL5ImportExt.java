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
package polyglot.ext.jl5.ast;

import static polyglot.ext.jl5.ast.JL5Import.SINGLE_STATIC_MEMBER;
import static polyglot.ext.jl5.ast.JL5Import.STATIC_ON_DEMAND;

import java.util.List;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5ImportTable;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.util.StringUtil;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5ImportExt extends JL5Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Import im = (Import) node();
        JL5ImportTable it = (JL5ImportTable) tb.importTable();

        if (im.kind() == JL5Import.SINGLE_STATIC_MEMBER) {
            it.addSingleStaticImport(im.name(), im.position());
        }
        else if (im.kind() == JL5Import.STATIC_ON_DEMAND) {
            it.addStaticOnDemandImport(im.name(), im.position());
        }
        return superLang().buildTypes(im, tb);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Import n = (Import) this.node();
        // check class is exists and is accessible
        if (n.kind() == JL5Import.SINGLE_STATIC_MEMBER) {
            Type nt =
                    (Type) tc.typeSystem()
                             .forName(StringUtil.getPackageComponent(n.name()));
            String id = StringUtil.getShortNameComponent(n.name());
            if (!isIdStaticMember(nt.toClass(),
                                  id,
                                  (JL5TypeSystem) tc.typeSystem(),
                                  tc.context().package_())) {
                throw new SemanticException("Cannot find static member " + id
                        + " in class: " + nt, n.position());
            }
            return n;
        }
        else {
            return superLang().typeCheck(this.node(), tc);
        }
    }

    private static boolean isIdStaticMember(ClassType t, String id,
            JL5TypeSystem ts, Package package_) {
        if (!ts.classAccessibleFromPackage(t.toClass(), package_)) {
            return false;
        }

        try {
            FieldInstance fi = ts.findField(t, id, t.toClass(), true);
            if (fi != null
                    && fi.flags().isStatic()
                    && ts.accessibleFromPackage(fi.flags(),
                                                t.package_(),
                                                package_)) {
                return true;
            }
        }
        catch (SemanticException e) {
        }

        if (ts.hasMethodNamed(t, id)) {
            List<? extends MethodInstance> meths = t.methodsNamed(id);
            boolean anyAccessible = false;
            for (MethodInstance mi : meths) {
                if (mi.flags().isStatic()
                        && ts.accessibleFromPackage(mi.flags(),
                                                    t.package_(),
                                                    package_)) {
                    anyAccessible = true;
                    break;
                }
            }
            return anyAccessible;
        }

        try {
            ClassType ct = ts.findMemberClass(t, id);
            if (ct != null
                    && ct.flags().isStatic()
                    && ts.accessibleFromPackage(ct.flags(),
                                                t.package_(),
                                                package_)) {
                return true;
            }
        }
        catch (SemanticException e) {
        }

        return false;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        Import n = (Import) this.node();
        if (n.kind() == SINGLE_STATIC_MEMBER || n.kind() == STATIC_ON_DEMAND) {
            w.write("import static ");
            w.write(n.name());

            if (n.kind() == STATIC_ON_DEMAND) {
                w.write(".*");
            }

            w.write(";");
            w.newline(0);
        }
        else superLang().prettyPrint(this.node(), w, tr);

    }
}
