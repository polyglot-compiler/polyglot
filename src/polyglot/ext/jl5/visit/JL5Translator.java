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

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Receiver;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.ArrayType;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.Translator;

public class JL5Translator extends Translator {

    private final boolean translateEnums;

    private final boolean removeJava5isms;

    public JL5Translator(Job job, TypeSystem ts, NodeFactory nf,
            TargetFactory tf) {
        super(job, ts, nf, tf);
        translateEnums =
                ((JL5Options) job.extensionInfo().getOptions()).translateEnums;
        removeJava5isms =
                ((JL5Options) job.extensionInfo().getOptions()).removeJava5isms;
    }

    public boolean removeJava5isms() {
        return this.removeJava5isms;
    }

    public boolean translateEnums() {
        return this.translateEnums;
    }

    public void translateNode(Node n, CodeWriter w) {
        if (n instanceof ClassDecl) {
            if (removeJava5isms && translateEnums) {
                ClassDecl cd = (ClassDecl) n;
                if (cd.superClass() != null
                        && cd.superClass().type().isClass()
                        && cd.superClass()
                             .type()
                             .toClass()
                             .fullName()
                             .equals("java.lang.Enum")) {
                    // The super class is Enum, so this is really an enum declaration.
                    RemoveEnums.prettyPrintClassDeclAsEnum(cd, w, this);
                    return;
                }
            }
        }
        if (n instanceof TypeNode) {
            TypeNode tn = (TypeNode) n;
            if (removeJava5isms) {
                // Print out the erasure type
                Type t = tn.type();
                Type erastype = ((JL5TypeSystem) ts).erasureType(t);
                if (erastype instanceof LubType) {
                    erastype = ((LubType) erastype).calculateLub();
                    erastype = ((JL5TypeSystem) ts).erasureType(erastype);
                }
                w.write(translateType(erastype));
                return;
            }
            else if (!tn.isDisambiguated()) {
                lang().prettyPrint(tn, w, this);
                return;
            }
            else {
                w.write(tn.type().translate(this.context()));
                return;
            }
        }
//        if (n instanceof TypeNode && ((TypeNode)n).type() instanceof TypeVariable) {
//            // Don't print out the type variable, print out its superclass.
//            TypeNode tn = (TypeNode) n;
//            TypeVariable tv = (TypeVariable) tn.type();
//            translateNode(tn.type(tv.erasureType()), w);
//            return;
//        }

        lang().prettyPrint(n, w, this);
    }

    private String translateType(Type t) {
        if (t instanceof JL5SubstClassType) {
            // For C<T1,...,Tn>, just print C.
            JL5SubstClassType jct = (JL5SubstClassType) t;
            return jct.base().translate(this.context);
        }
        else if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            return translateType(at.base()) + "[]";
        }
        else {
            return t.translate(this.context());
        }
    }

    public void printReceiver(Receiver target, CodeWriter w) {
        if (target == null) {
            return;
        }
        if (target instanceof TypeNode) {
            Type t = ((TypeNode) target).type();
            if (t instanceof JL5ClassType) {
                JL5ClassType ct = (JL5ClassType) t;
                w.write(ct.translateAsReceiver(this.context()));
                return;
            }
        }
        this.translateNode(target, w);
    }
}
