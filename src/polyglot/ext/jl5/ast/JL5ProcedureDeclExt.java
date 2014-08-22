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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.ast.ProcedureDeclOps;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.UnknownType;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public abstract class JL5ProcedureDeclExt extends JL5AnnotatedElementExt
        implements ProcedureDeclOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<ParamTypeNode> typeParams;

    public JL5ProcedureDeclExt(List<ParamTypeNode> typeParams,
            List<AnnotationElem> annotations) {
        super(annotations);
        this.typeParams = ListUtil.copy(typeParams, true);
    }

    public List<ParamTypeNode> typeParams() {
        return typeParams;
    }

    public Node typeParams(List<ParamTypeNode> typeParams) {
        return typeParams(node(), typeParams);
    }

    protected <N extends Node> N typeParams(N n, List<ParamTypeNode> typeParams) {
        JL5ProcedureDeclExt ext = (JL5ProcedureDeclExt) JL5Ext.ext(n);
        if (CollectionUtil.equals(ext.typeParams, typeParams)) return n;
        if (n == node) {
            n = Copy.Util.copy(n);
            ext = (JL5ProcedureDeclExt) JL5Ext.ext(n);
        }
        ext.typeParams = ListUtil.copy(typeParams, true);
        return n;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        ProcedureDecl pd = (ProcedureDecl) node();
        JL5ProcedureInstance pi = (JL5ProcedureInstance) pd.procedureInstance();
        pi.setAnnotations(annotations);
    }

    protected Node reconstruct(Node n, List<ParamTypeNode> typeParams) {
        n = typeParams(n, typeParams);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<ParamTypeNode> typeParams = visitList(this.typeParams, v);
        Node n = super.visitChildren(v);
        return reconstruct(n, typeParams);
    }

    @Override
    public Context enterScope(Context c) {
        ProcedureDecl pd = (ProcedureDecl) node();
        c = superLang().enterScope(pd, c);
        for (TypeNode pn : typeParams) {
            ((JL5Context) c).addTypeVariable((TypeVariable) pn.type());
        }
        return c;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        ProcedureDecl pd = (ProcedureDecl) node();

        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return pd;
        }

        boolean isVarArgs = false;
        List<UnknownType> formalTypes = new ArrayList<>(pd.formals().size());
        for (int i = 0; i < pd.formals().size(); i++) {
            formalTypes.add(ts.unknownType(pd.position()));
            Formal f = pd.formals().get(i);
            JL5FormalExt fext = (JL5FormalExt) JL5Ext.ext(f);
            if (fext.isVarArg()) isVarArgs = true;
        }

        List<UnknownType> throwTypes = new ArrayList<>(pd.throwTypes().size());
        for (int i = 0; i < pd.throwTypes().size(); i++) {
            throwTypes.add(ts.unknownType(pd.position()));
        }

        List<TypeVariable> typeParams = new ArrayList<>(this.typeParams.size());
        for (int i = 0; i < this.typeParams.size(); i++) {
            typeParams.add(ts.unknownTypeVariable(pd.position()));
        }

        Flags flags = pd.flags();
        if (isVarArgs) {
            flags = JL5Flags.setVarArgs(flags);
        }

        return buildTypesFinish(ts,
                                ct,
                                flags,
                                formalTypes,
                                throwTypes,
                                typeParams);
    }

    protected abstract Node buildTypesFinish(JL5TypeSystem ts,
            ParsedClassType ct, Flags flags, List<? extends Type> formalTypes,
            List<? extends Type> throwTypes, List<TypeVariable> typeParams);

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        ProcedureDecl n = (ProcedureDecl) superLang().disambiguate(node(), ar);

        List<TypeVariable> typeParams = new ArrayList<>(this.typeParams.size());

        for (TypeNode tn : this.typeParams) {
            if (!tn.isDisambiguated()) {
                return n;
            }
            TypeVariable tv = (TypeVariable) tn.type();
            typeParams.add(tv);

        }
        // now type nodes are disambiguated
        JL5ProcedureInstance pi = (JL5ProcedureInstance) n.procedureInstance();
        pi.setTypeParams(typeParams);
        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ProcedureDecl pd = (ProcedureDecl) node();
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        JL5ProcedureInstance pi = (JL5ProcedureInstance) pd.procedureInstance();
        // check no duplicate annotations used
        ts.checkDuplicateAnnotations(annotations);
        for (TypeNode typeParam : typeParams)
            ts.checkCycles(typeParam.type().toReference());

        // mark the formals as being procedure formals (since they are)
        for (Formal f : pd.formals()) {
            JL5LocalInstance li = (JL5LocalInstance) f.localInstance();
            li.setProcedureFormal(true);
        }

        // check at most last formal is variable
        for (int i = 0; i < pd.formals().size(); i++) {
            Formal f = pd.formals().get(i);
            JL5FormalExt fext = (JL5FormalExt) JL5Ext.ext(f);
            if (fext.isVarArg()) {
                if (i != pd.formals().size() - 1) {
                    throw new SemanticException("Only last formal can be variable in method declaration.",
                                                f.position());
                }
                else {
                    pi.setFlags(JL5Flags.setVarArgs(pi.flags()));
                    pd = pd.flags(JL5Flags.setVarArgs(pd.flags()));
                }
            }
        }

        Flags flags = pi.flags();
        // check that the varargs flag is consistent with the type of the last argument.
        if (JL5Flags.isVarArgs(pd.flags()) != JL5Flags.isVarArgs(flags)) {
            throw new InternalCompilerError("VarArgs flag of AST and type disagree");
        }

        if (JL5Flags.isVarArgs(flags)) {
            // check that the last formal type is an array
            if (pi.formalTypes().isEmpty()) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
            Type last = pi.formalTypes().get(pi.formalTypes().size() - 1);
            if (!(last instanceof JL5ArrayType && ((JL5ArrayType) last).isVarArg())) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
        }

        // check duplicate type parameter decls
        Set<String> typeParamNames = new HashSet<>();
        for (TypeNode tn : typeParams) {
            String name = tn.name();
            if (typeParamNames.contains(name))
                throw new SemanticException("Duplicate type variable declaration.",
                                            tn.position());
            typeParamNames.add(name);
        }

        return pd;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        superLang().prettyPrint(node(), w, pp);
    }

    @Override
    public void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr) {
        w.begin(0);

        w.begin(0);
        super.prettyPrint(w, tr);
        w.end();

        w.write(JL5Flags.clearVarArgs(flags).translate());

        // type params
        boolean printTypeVars = true;
        if (tr instanceof JL5Translator) {
            JL5Translator jl5tr = (JL5Translator) tr;
            printTypeVars = !jl5tr.removeJava5isms();
        }
        if (printTypeVars && !typeParams.isEmpty()) {
            w.write("<");
            for (Iterator<ParamTypeNode> iter = typeParams.iterator(); iter.hasNext();) {
                TypeNode ptn = iter.next();
                tr.lang().prettyPrint(ptn, w, tr);
                if (iter.hasNext()) {
                    w.write(",");
                    w.allowBreak(0, " ");
                }
            }
            w.write("> ");
        }

        ProcedureDecl n = (ProcedureDecl) node();
        prettyPrintName(w, tr);

        w.write("(");
        w.begin(0);

        for (Iterator<Formal> i = n.formals().iterator(); i.hasNext();) {
            Formal f = i.next();
            tr.print(n, f, w);

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
                tr.print(n, tn, w);

                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(4, " ");
                }
            }
        }

        w.end();
    }

    protected abstract void prettyPrintName(CodeWriter w, PrettyPrinter pp);
}
