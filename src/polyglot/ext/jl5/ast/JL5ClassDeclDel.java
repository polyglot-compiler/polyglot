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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDeclOps;
import polyglot.ast.JL;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Node_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.ext.param.types.MuPClass;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5ClassDeclDel extends JL5AnnotatedElementDel implements
        ClassDeclOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        ClassDecl n = (ClassDecl) super.buildTypes(tb);
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);

        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();
        JL5ParsedClassType ct = (JL5ParsedClassType) n.type();

        MuPClass<TypeVariable, ReferenceType> pc =
                ts.mutablePClass(ct.position());
        ct.setPClass(pc);
        pc.clazz(ct);

        if (ext.paramTypes() != null && !ext.paramTypes().isEmpty()) {
            List<TypeVariable> typeVars =
                    new ArrayList<TypeVariable>(ext.paramTypes().size());
            for (ParamTypeNode ptn : ext.paramTypes()) {
                TypeVariable tv = (TypeVariable) ptn.type();
                typeVars.add(tv);
                tv.setDeclaringClass(ct);
            }
            ct.setTypeVariables(typeVars);
            pc.formals(new ArrayList<TypeVariable>(typeVars));
        }

        return n;

    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(this.node());

        List<AnnotationElem> annots =
                this.node().visitList(ext.annotationElems(), v);

        List<ParamTypeNode> paramTypes =
                this.node().visitList(ext.paramTypes(), v);

        Node newN = super.visitChildren(v);
        JL5ClassDeclExt newext = (JL5ClassDeclExt) JL5Ext.ext(newN);

        if (!CollectionUtil.equals(annots, newext.annotationElems())
                || !CollectionUtil.equals(paramTypes, newext.paramTypes())) {
            // the annotations or param thypes changed! Let's update the node.
            if (newN == this.node()) {
                // we need to create a copy.
                newN = (Node) newN.copy();
                newext = (JL5ClassDeclExt) JL5Ext.ext(newN);
            }
            else {
                // the call to super.visitChildren(v) already
                // created a copy of the node (and thus of its extension).
            }
            newext.annotations = annots;
            newext.paramTypes = paramTypes;
        }
        return newN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.ast.NodeOps#enterScope(polyglot.types.Context)
     */
    @Override
    public Context enterChildScope(Node child, Context c) {
        ClassDecl n = (ClassDecl) this.node();
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);

        if (child == n.body()) {
            TypeSystem ts = c.typeSystem();
            c = c.pushClass(n.type(), ts.staticTarget(n.type()).toClass());
        }
        else {
            // Add this class to the context, but don't push a class scope.
            // This allows us to detect loops in the inheritance
            // hierarchy, but avoids an infinite loop.
            c = ((JL5Context) c).pushExtendsClause(n.type());
            c.addNamed(n.type());
        }
        for (ParamTypeNode tn : ext.paramTypes()) {
            ((JL5Context) c).addTypeVariable((TypeVariable) tn.type());
        }
        return child.del().enterScope(c);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ClassDecl n = (ClassDecl) this.node();
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);

        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (n.type().superType() != null
                && JL5Flags.isEnum(n.type().superType().toClass().flags())) {
            throw new SemanticException("Cannot extend enum type", n.position());
        }

        if (ts.equals(ts.Object(), n.type()) && !ext.paramTypes.isEmpty()) {
            throw new SemanticException("Type: " + n.type()
                    + " cannot declare type variables.", n.position());
        }

        if (JL5Flags.isAnnotation(n.flags()) && n.flags().isPrivate()) {
            throw new SemanticException("Annotation types cannot have explicit private modifier",
                                        n.position());
        }

        ts.checkDuplicateAnnotations(ext.annotations);

        // check not extending java.lang.Throwable (or any of its subclasses)
        // with a generic class
        if (n.type().superType() != null
                && ts.isSubtype(n.type().superType(), ts.Throwable())
                && !ext.paramTypes.isEmpty()) {
            // JLS 3rd ed. 8.1.2
            throw new SemanticException("Cannot subclass java.lang.Throwable or any of its subtypes with a generic class",
                                        n.superClass().position());
        }

        // check duplicate type variable decls
        for (int i = 0; i < ext.paramTypes.size(); i++) {
            TypeNode ti = ext.paramTypes.get(i);
            for (int j = i + 1; j < ext.paramTypes.size(); j++) {
                TypeNode tj = ext.paramTypes.get(j);
                if (ti.name().equals(tj.name())) {
                    throw new SemanticException("Duplicate type variable declaration.",
                                                tj.position());
                }
            }
        }

        for (ParamTypeNode paramType : ext.paramTypes)
            ts.checkCycles(paramType.type().toReference());

        return super.typeCheck(tc);
    }

    public void prettyPrintModifiers(CodeWriter w, PrettyPrinter tr) {
        ClassDecl n = (ClassDecl) this.node();
        Flags f = n.flags();
        if (f.isInterface()) {
            f = f.clearInterface().clearAbstract();
        }
        if (JL5Flags.isEnum(f)) {
            f = JL5Flags.clearEnum(f).clearStatic().clearAbstract();
        }
        if (JL5Flags.isAnnotation(f)) {
            f = JL5Flags.clearAnnotation(f);
        }
        w.write(f.translate());

        if (n.flags().isInterface()) {
            if (JL5Flags.isAnnotation(n.flags())) {
                w.write("@interface ");
            }
            else {
                w.write("interface ");
            }
        }
        else if (JL5Flags.isEnum(n.flags())) {
            w.write("enum ");
        }
        else {
            w.write("class ");
        }
    }

    public void prettyPrintName(CodeWriter w, PrettyPrinter tr) {
        ClassDecl n = (ClassDecl) this.node();
        w.write(n.id().id());
    }

    public void prettyPrintHeaderRest(CodeWriter w, PrettyPrinter tr) {
        ClassDecl n = (ClassDecl) this.node();
        if (n.superClass() != null
                && ((!JL5Flags.isEnum(n.flags()) && !JL5Flags.isAnnotation(n.flags())))) {
            w.write(" extends ");
            ((Node_c) n).print(n.superClass(), w, tr);
        }

        if (!n.interfaces().isEmpty() && !JL5Flags.isAnnotation(n.flags())) {
            if (n.flags().isInterface()) {
                w.write(" extends ");
            }
            else {
                w.write(" implements ");
            }

            for (Iterator<TypeNode> i = n.interfaces().iterator(); i.hasNext();) {
                TypeNode tn = i.next();
                ((Node_c) n).print(tn, w, tr);

                if (i.hasNext()) {
                    w.write(", ");
                }
            }
        }

        w.write(" {");
    }

    @Override
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
        ClassDecl n = (ClassDecl) this.node();
        JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);

        w.begin(0);
        for (AnnotationElem ae : ext.annotationElems()) {
            ae.del().prettyPrint(w, tr);
            w.newline();
        }
        w.end();

        prettyPrintModifiers(w, tr);
        prettyPrintName(w, tr);
        // print type variables
        boolean printTypeVars = true;
        if (tr instanceof JL5Translator) {
            JL5Translator jl5tr = (JL5Translator) tr;
            printTypeVars = !jl5tr.removeJava5isms();
        }
        if (printTypeVars && !ext.paramTypes().isEmpty()) {
            w.write("<");
            for (Iterator<ParamTypeNode> iter = ext.paramTypes.iterator(); iter.hasNext();) {
                ParamTypeNode ptn = iter.next();
                ptn.del().prettyPrint(w, tr);
                if (iter.hasNext()) {
                    w.write(", ");
                }
            }
            w.write(">");
        }
        prettyPrintHeaderRest(w, tr);

    }

    @Override
    public void prettyPrintFooter(CodeWriter w, PrettyPrinter tr) {
        JL jl = this.jl();
        if (jl instanceof ClassDeclOps) {
            ((ClassDeclOps) jl).prettyPrintFooter(w, tr);
        }
        else {
            throw new InternalCompilerError("jl() should imple,ent ClassDeclOps");
        }
    }

    @Override
    public Node addDefaultConstructor(TypeSystem ts, NodeFactory nf,
            ConstructorInstance defaultConstructorInstance)
            throws SemanticException {
        JL jl = this.jl();
        if (jl instanceof ClassDeclOps) {
            return ((ClassDeclOps) jl).addDefaultConstructor(ts,
                                                             nf,
                                                             defaultConstructorInstance);
        }
        else {
            throw new InternalCompilerError("jl() should imple,ent ClassDeclOps");
        }
    }

}
