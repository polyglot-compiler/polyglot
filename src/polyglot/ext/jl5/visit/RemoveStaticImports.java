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
package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Call;
import polyglot.ast.Field;
import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceFile;
import polyglot.ext.jl5.ast.JL5Import;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Remove static imports
 */
public class RemoveStaticImports extends ContextVisitor {
    public RemoveStaticImports(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (n instanceof SourceFile) {
            // remove the static imports
            SourceFile sf = (SourceFile) n;
            List<Import> imports = new ArrayList<>(sf.imports());
            boolean changed = false;
            for (Iterator<Import> iter = imports.iterator(); iter.hasNext();) {
                Import imp = iter.next();
                if (imp.kind() == JL5Import.SINGLE_STATIC_MEMBER
                        || imp.kind() == JL5Import.STATIC_ON_DEMAND) {
                    // a static import!
                    iter.remove();
                    changed = true;
                }
            }
            if (changed) {
                n = sf.imports(imports);
            }
        }

        if (n instanceof Field) {
            Field f = (Field) n;
            if (f.flags().isStatic() && f.isTargetImplicit()) {
                // check if we need to make the target explicit
                FieldInstance fi = f.fieldInstance();
                ClassType currClass = this.context().currentClass();
                // just use a reasonable approximation to figure out whether we need an explicit target
                if (currClass == null || !currClass.isSubtype(fi.container())) {
                    n = f.targetImplicit(false);
                }
            }
        }
        if (n instanceof Call) {
            Call c = (Call) n;
            MethodInstance mi = c.methodInstance();
            if (mi.flags().isStatic() && c.isTargetImplicit()) {
                // check if we need to make the target explicit
                ClassType currClass = this.context().currentClass();
                // just use a reasonable approximation to figure out whether we need an explicit target
                if (currClass == null || !currClass.isSubtype(mi.container())) {
                    n = c.targetImplicit(false);
                }
            }
        }
        return n;
    }
}
