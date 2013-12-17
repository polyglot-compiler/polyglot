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
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.ConstructorDecl;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.ast.ProcedureDeclOps;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Declaration;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.UnknownType;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5ConstructorDeclExt extends JL5AnnotatedElementExt implements
        ProcedureDeclOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<ParamTypeNode> typeParams;

    public List<ParamTypeNode> typeParams() {
        return this.typeParams;
    }

    public ConstructorDecl typeParams(List<ParamTypeNode> typeParams) {

        ConstructorDecl n = (ConstructorDecl) this.node().copy();
        JL5ConstructorDeclExt ext = (JL5ConstructorDeclExt) JL5Ext.ext(n);
        ext.typeParams = typeParams;
        return n;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        ConstructorDecl cd = (ConstructorDecl) this.node();
        JL5ConstructorInstance ci =
                (JL5ConstructorInstance) cd.constructorInstance();
        ci.setAnnotations(annotations);
    }

    @Override
    protected Declaration declaration() {
        ConstructorDecl cd = (ConstructorDecl) this.node();
        return cd.constructorInstance();
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        JL5ConstructorDeclExt ext =
                (JL5ConstructorDeclExt) JL5Ext.ext(this.node());

        List<ParamTypeNode> paramTypes =
                this.node().visitList(ext.typeParams(), v);

        List<AnnotationElem> annots =
                this.node().visitList(ext.annotationElems(), v);

        Node newN = super.visitChildren(v);
        JL5ConstructorDeclExt newext = (JL5ConstructorDeclExt) JL5Ext.ext(newN);

        if (!CollectionUtil.equals(annots, newext.annotationElems())
                || !CollectionUtil.equals(paramTypes, newext.typeParams())) {
            // the annotations or param types changed! Let's update the node.
            if (newN == this.node()) {
                // we need to create a copy.
                newN = (Node) newN.copy();
                newext = (JL5ConstructorDeclExt) JL5Ext.ext(newN);
            }
            else {
                // the call to super.visitChildren(v) already
                // created a copy of the node (and thus of its extension).
            }
            newext.annotations = annots;
            newext.typeParams = paramTypes;
        }
        return newN;

    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {

        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        ConstructorDecl cd = (ConstructorDecl) this.node();
        JL5ConstructorDeclExt ext = (JL5ConstructorDeclExt) JL5Ext.ext(cd);

        if (ct == null) {
            return cd;
        }

        boolean vararg = false;
        List<UnknownType> formalTypes =
                new ArrayList<UnknownType>(cd.formals().size());
        for (int i = 0; i < cd.formals().size(); i++) {
            formalTypes.add(ts.unknownType(cd.position()));
            Formal f = cd.formals().get(i);
            JL5FormalExt fext = (JL5FormalExt) JL5Ext.ext(f);
            if (fext.isVarArg()) vararg = true;
        }

        List<UnknownType> throwTypes =
                new ArrayList<UnknownType>(cd.throwTypes().size());
        for (int i = 0; i < cd.throwTypes().size(); i++) {
            throwTypes.add(ts.unknownType(cd.position()));
        }

        List<TypeVariable> typeParams =
                new ArrayList<TypeVariable>(ext.typeParams().size());
        for (int i = 0; i < ext.typeParams().size(); i++) {
            typeParams.add(ts.unknownTypeVariable(cd.position()));
        }

        Flags flags = cd.flags();
        if (vararg) {
            flags = JL5Flags.VARARGS.set(cd.flags());
        }
        ConstructorInstance ci =
                ts.constructorInstance(cd.position(),
                                       ct,
                                       flags,
                                       formalTypes,
                                       throwTypes,
                                       typeParams);
        ct.addConstructor(ci);

        return cd.constructorInstance(ci).flags(flags);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        ConstructorDecl n =
                (ConstructorDecl) this.superLang().disambiguate(this.node(), ar);
        JL5ConstructorDeclExt ext = (JL5ConstructorDeclExt) JL5Ext.ext(n);

        List<TypeVariable> typeParams = new LinkedList<TypeVariable>();

        for (TypeNode tn : ext.typeParams()) {
            if (!tn.isDisambiguated()) {

                return n;
            }
            TypeVariable tv = (TypeVariable) tn.type();
            typeParams.add(tv);

        }
        // now type nodes are disambiguated
        JL5ConstructorInstance ci =
                (JL5ConstructorInstance) n.constructorInstance();
        ci.setTypeParams(typeParams);
        return n;
    }

    @Override
    public Context enterScope(Context c) {
        c = this.superLang().enterScope(this.node(), c);
        JL5ConstructorDeclExt ext =
                (JL5ConstructorDeclExt) JL5Ext.ext(this.node());

        for (TypeNode pn : ext.typeParams()) {
            ((JL5Context) c).addTypeVariable((TypeVariable) pn.type());
        }
        return c;
    }

    @Override
    public void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr) {
        ConstructorDecl n = (ConstructorDecl) this.node();
        JL5ConstructorDeclExt ext = (JL5ConstructorDeclExt) JL5Ext.ext(n);

        w.begin(0);

        w.begin(0);
        for (AnnotationElem ae : ext.annotationElems()) {
            tr.lang().prettyPrint(ae, w, tr);
            w.newline();
        }
        w.end();

        w.write(JL5Flags.clearVarArgs(n.flags()).translate());

        // type params
        boolean printTypeVars = true;
        if (tr instanceof JL5Translator) {
            JL5Translator jl5tr = (JL5Translator) tr;
            printTypeVars = !jl5tr.removeJava5isms();
        }
        if (printTypeVars && !ext.typeParams().isEmpty()) {
            w.write("<");
            for (Iterator<ParamTypeNode> iter = ext.typeParams().iterator(); iter.hasNext();) {
                ParamTypeNode ptn = iter.next();
                tr.lang().prettyPrint(ptn, w, tr);
                if (iter.hasNext()) {
                    w.write(",");
                    w.allowBreak(0, " ");
                }
            }
            w.write("> ");
        }

        tr.print(n, n.id(), w);
        w.write("(");

        w.begin(0);

        for (Iterator<Formal> i = n.formals().iterator(); i.hasNext();) {
            Formal f = i.next();
            ((Node_c) n).print(f, w, tr);

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.end();
        w.write(")");

        if (!n.throwTypes().isEmpty()) {
            w.allowBreak(6);
            w.write("throws ");

            for (Iterator<TypeNode> i = n.throwTypes().iterator(); i.hasNext();) {
                TypeNode tn = i.next();
                ((Node_c) n).print(tn, w, tr);

                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(4, " ");
                }
            }
        }

        w.end();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ConstructorDecl cd = (ConstructorDecl) this.node();
        JL5ConstructorDeclExt ext = (JL5ConstructorDeclExt) JL5Ext.ext(cd);

        JL5ConstructorInstance ci =
                (JL5ConstructorInstance) cd.constructorInstance();
        for (ParamTypeNode typeParam : ext.typeParams()) {
            ts.checkCycles(typeParam.type().toReference());
        }
        // mark the formals as being procedure formals (since they are)
        for (Formal f : cd.formals()) {
            JL5LocalInstance li = (JL5LocalInstance) f.localInstance();
            li.setProcedureFormal(true);
        }

        cd = (ConstructorDecl) super.typeCheck(tc);
        ext = (JL5ConstructorDeclExt) JL5Ext.ext(cd);

        // check at most last formal is variable
        for (int i = 0; i < cd.formals().size(); i++) {
            Formal f = cd.formals().get(i);
            JL5FormalExt fext = (JL5FormalExt) JL5Ext.ext(f);
            if (fext.isVarArg()) {
                if (i != cd.formals().size() - 1) {
                    throw new SemanticException("Only last formal can be variable in constructor declaration.",
                                                f.position());
                }
                else {
                    ci.setFlags(JL5Flags.setVarArgs(ci.flags()));
                    cd = cd.flags(JL5Flags.setVarArgs(cd.flags()));
                    ext = (JL5ConstructorDeclExt) JL5Ext.ext(cd);
                }
            }
        }

        // check that the varargs flag is consistent with the type of the last argument.
        if (JL5Flags.isVarArgs(cd.flags()) != JL5Flags.isVarArgs(ci.flags())) {
            throw new InternalCompilerError("VarArgs flag of AST and type disagree");
        }

        if (JL5Flags.isVarArgs(ci.flags())) {
            // check that the last formal type is an array
            if (ci.formalTypes().isEmpty()) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
            Type last = ci.formalTypes().get(ci.formalTypes().size() - 1);
            if (!(last instanceof JL5ArrayType && ((JL5ArrayType) last).isVarArg())) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
        }
        return cd;
    }

}
