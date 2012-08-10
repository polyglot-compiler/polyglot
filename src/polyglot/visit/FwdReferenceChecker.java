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

package polyglot.visit;

import java.util.HashSet;
import java.util.Set;

import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.FieldDecl;
import polyglot.ast.Initializer;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.FieldInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;

/** Visitor which ensures that field intializers and initializers do not
 * make illegal forward references to fields.
 *  This is an implementation of the rules of the Java Language Spec, 2nd
 * Edition, Section 8.3.2.3 
 */
public class FwdReferenceChecker extends ContextVisitor {
    public FwdReferenceChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    private boolean inInitialization = false;
    private boolean inStaticInit = false;
    private Field fieldAssignLHS = null;
    private Set<FieldInstance> declaredFields = new HashSet<FieldInstance>();

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof FieldDecl) {
            FieldDecl fd = (FieldDecl) n;
            declaredFields.add(fd.fieldInstance());

            FwdReferenceChecker frc = (FwdReferenceChecker) this.copy();
            frc.inInitialization = true;
            frc.inStaticInit = fd.flags().isStatic();
            return frc;
        }
        else if (n instanceof Initializer) {
            FwdReferenceChecker frc = (FwdReferenceChecker) this.copy();
            frc.inInitialization = true;
            frc.inStaticInit = ((Initializer) n).flags().isStatic();
            return frc;
        }
        else if (n instanceof FieldAssign) {
            FwdReferenceChecker frc = (FwdReferenceChecker) this.copy();
            frc.fieldAssignLHS = (Field) ((FieldAssign) n).left();
            return frc;
        }
        else if (n instanceof Field) {
            if (fieldAssignLHS == n) {
                // the field is on  the left hand side of an assignment.
                // we can ignore it
                fieldAssignLHS = null;
            }
            else if (inInitialization) {
                // we need to check if this is an illegal fwd reference.
                Field f = (Field) n;

                // an illegal fwd reference if a usage of an instance 
                // (resp. static) field occurs in an instance (resp. static)
                // initialization, and the innermost enclosing class or 
                // interface of the usage is the same as the container of
                // the field, and we have not yet seen the field declaration.
                //
                // In addition, if a field is not accessed as a simple name, 
                // then all is ok

                if (inStaticInit == f.fieldInstance().flags().isStatic()
                        && context().currentClass().equals(f.fieldInstance()
                                                            .container())
                        && !declaredFields.contains(f.fieldInstance().orig())
                        && f.isTargetImplicit()) {
                    throw new SemanticException("Illegal forward reference",
                                                f.position());
                }
            }
        }
        return this;
    }
}
