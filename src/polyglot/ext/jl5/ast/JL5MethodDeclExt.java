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

import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.MethodDecl_c;
import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.ast.ProcedureDeclOps;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.Context;
import polyglot.types.Declaration;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
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
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5MethodDeclExt extends JL5AnnotatedElementExt implements
        ProcedureDeclOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected boolean compilerGenerated;
    protected List<ParamTypeNode> typeParams;

    public boolean isGeneric() {
        if (!typeParams.isEmpty()) return true;
        return false;
    }

    public boolean isCompilerGenerated() {
        return compilerGenerated;
    }

    public MethodDecl setCompilerGenerated(boolean val) {
        MethodDecl n = (MethodDecl) copy();
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(n);
        ext.compilerGenerated = val;
        return n;
    }

    public List<ParamTypeNode> typeParams() {
        return this.typeParams;
    }

    public MethodDecl typeParams(List<ParamTypeNode> typeParams) {
        MethodDecl n = (MethodDecl) copy();
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(n);
        ext.typeParams = typeParams;
        return n;
    }

    @Override
    protected Declaration declaration() {
        MethodDecl n = (MethodDecl) this.node();
        return n.methodInstance();

    }

    @Override
    public void setAnnotations(Annotations annotations) {
        MethodDecl n = (MethodDecl) this.node();
        ((JL5MethodInstance) n.methodInstance()).setAnnotations(annotations);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        MethodDecl md = (MethodDecl) this.node();
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(md);

        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return md;
        }

        boolean isVarArgs = false;
        List<UnknownType> formalTypes =
                new ArrayList<UnknownType>(md.formals().size());
        for (int i = 0; i < md.formals().size(); i++) {
            formalTypes.add(ts.unknownType(md.position()));
            Formal f = md.formals().get(i);
            JL5FormalExt fext = (JL5FormalExt) JL5Ext.ext(f);
            isVarArgs |= fext.isVarArg();
        }

        List<UnknownType> throwTypes =
                new ArrayList<UnknownType>(md.throwTypes().size());
        for (int i = 0; i < md.throwTypes().size(); i++) {
            throwTypes.add(ts.unknownType(md.position()));
        }

        Flags f = md.flags();

        if (ct.flags().isInterface()) {
            f = f.Public().Abstract();
        }

        if (isVarArgs) {
            f = JL5Flags.setVarArgs(f);
        }

        List<TypeVariable> typeParams =
                new ArrayList<TypeVariable>(ext.typeParams().size());
        for (int i = 0; i < ext.typeParams().size(); i++) {
            typeParams.add(ts.unknownTypeVariable(md.position()));
        }

        MethodInstance mi =
                ts.methodInstance(md.position(),
                                  ct,
                                  f,
                                  ts.unknownType(md.position()),
                                  md.name(),
                                  formalTypes,
                                  throwTypes,
                                  typeParams);
        ct.addMethod(mi);
        return md.methodInstance(mi);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        MethodDecl n = (MethodDecl) superLang().disambiguate(this.node(), ar);
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(n);
        List<TypeVariable> typeParams = new LinkedList<TypeVariable>();

        for (TypeNode tn : ext.typeParams()) {
            if (!tn.isDisambiguated()) {

                return n;
            }
            TypeVariable tv = (TypeVariable) tn.type();
            typeParams.add(tv);
        }
        // now type nodes are disambiguated
        JL5MethodInstance mi = (JL5MethodInstance) n.methodInstance();
        mi.setTypeParams(typeParams);
        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // check no duplicate annotations used
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        MethodDecl md = (MethodDecl) this.node();
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(md);

        JL5MethodInstance mi = (JL5MethodInstance) md.methodInstance();
        ts.checkDuplicateAnnotations(ext.annotations);
        for (ParamTypeNode typeParam : ext.typeParams)
            ts.checkCycles(typeParam.type().toReference());

        // mark the formals as being procedure formals (since they are)
        for (Formal f : md.formals()) {
            JL5LocalInstance li = (JL5LocalInstance) f.localInstance();
            li.setProcedureFormal(true);
        }

        // check at most last formal is variable
        for (int i = 0; i < md.formals().size(); i++) {
            Formal f = md.formals().get(i);
            JL5FormalExt fext = (JL5FormalExt) JL5Ext.ext(f);
            if (fext.isVarArg()) {
                if (i != md.formals().size() - 1) {
                    throw new SemanticException("Only last formal can be variable in method declaration.",
                                                f.position());
                }
                else {
                    mi.setFlags(JL5Flags.setVarArgs(mi.flags()));
                    md = md.flags(JL5Flags.setVarArgs(md.flags()));
                }
            }
        }
        Flags flags = mi.flags();
        // repeat super class type checking so it can be specialized
        // to handle inner enum classes which indeed do have
        // static methods
        if (tc.context().currentClass().flags().isInterface()) {
            if (flags.isProtected() || flags.isPrivate()) {
                throw new SemanticException("Interface methods must be public.",
                                            md.position());
            }
        }

        try {
            ts.checkMethodFlags(flags);
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), md.position());
        }

        if (md.body() == null && !(flags.isAbstract() || flags.isNative())) {
            throw new SemanticException("Missing method body.", md.position());
        }

        if (md.body() != null && flags.isAbstract()) {
            throw new SemanticException("An abstract method cannot have a body.",
                                        md.position());
        }

        if (md.body() != null && flags.isNative()) {
            throw new SemanticException("A native method cannot have a body.",
                                        md.position());
        }

        ((MethodDecl_c) md).throwsCheck(tc);

        // check that inner classes do not declare static methods
        // unless class is enum
        if (flags.isStatic()
                && !JL5Flags.isEnum(md.methodInstance()
                                      .container()
                                      .toClass()
                                      .flags())
                && md.methodInstance().container().toClass().isInnerClass()) {
            // it's a static method in an inner class.
            throw new SemanticException("Inner classes cannot declare "
                    + "static methods.", md.position());
        }

        // check that the varargs flag is consistent with the type of the last argument.
        if (JL5Flags.isVarArgs(md.flags()) != JL5Flags.isVarArgs(flags)) {
            throw new InternalCompilerError("VarArgs flag of AST and type disagree");
        }

        if (JL5Flags.isVarArgs(flags)) {
            // check that the last formal type is an array
            if (mi.formalTypes().isEmpty()) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
            Type last = mi.formalTypes().get(mi.formalTypes().size() - 1);
            if (!(last instanceof JL5ArrayType && ((JL5ArrayType) last).isVarArg())) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
        }

        ((MethodDecl_c) md).overrideMethodCheck(tc);

        return md;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(this.node());

        List<ParamTypeNode> paramTypes =
                this.node().visitList(ext.typeParams(), v);

        List<AnnotationElem> annots =
                this.node().visitList(ext.annotationElems(), v);

        Node newN = super.visitChildren(v);
        JL5MethodDeclExt newext = (JL5MethodDeclExt) JL5Ext.ext(newN);

        if (!CollectionUtil.equals(annots, newext.annotationElems())
                || !CollectionUtil.equals(paramTypes, newext.typeParams())) {
            // the annotations or param thypes changed! Let's update the node.
            if (newN == this.node()) {
                // we need to create a copy.
                newN = (Node) newN.copy();
                newext = (JL5MethodDeclExt) JL5Ext.ext(newN);
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
    public void translate(CodeWriter w, Translator tr) {
        MethodDecl md = (MethodDecl) this.node();
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(md);

        if (ext.isCompilerGenerated()) return;

        superLang().translate(this.node(), w, tr);
    }

    @Override
    public void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr) {
        MethodDecl n = (MethodDecl) this.node();
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(n);

        w.begin(0);

        w.begin(0);
        for (AnnotationElem ae : ext.annotationElems()) {
            tr.lang().prettyPrint(ae, w, tr);
            w.newline();
        }
        w.end();

        w.write(JL5Flags.clearVarArgs(flags).translate());

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

        ((Node_c) n).print(n.returnType(), w, tr);
        w.write(" " + n.name() + "(");
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
    public Context enterScope(Context c) {
        MethodDecl md = (MethodDecl) this.node();
        JL5MethodDeclExt ext = (JL5MethodDeclExt) JL5Ext.ext(md);
        c = superLang().enterScope(this.node(), c);
        for (ParamTypeNode pn : ext.typeParams()) {
            ((JL5Context) c).addTypeVariable((TypeVariable) pn.type());
        }
        return c;
    }

}
