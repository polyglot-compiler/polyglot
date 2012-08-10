package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.MethodDecl;
import polyglot.ast.MethodDecl_c;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.UnknownType;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5MethodDecl_c extends MethodDecl_c implements JL5MethodDecl {

    protected boolean compilerGenerated;
    protected List<ParamTypeNode> typeParams;
    protected List<AnnotationElem> annotations;

    public JL5MethodDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        this(pos,
             flags,
             annotations,
             returnType,
             name,
             formals,
             throwTypes,
             body,
             new ArrayList<ParamTypeNode>());
    }

    public JL5MethodDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        super(pos, flags, returnType, name, formals, throwTypes, body);
        this.typeParams = typeParams;
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return this.annotations;
    }

    public JL5MethodDecl annotations(List<AnnotationElem> annotations) {
        JL5MethodDecl_c n = (JL5MethodDecl_c) copy();
        n.annotations = annotations;
        return n;
    }

    public boolean isGeneric() {
        if (!typeParams.isEmpty()) return true;
        return false;
    }

    @Override
    public boolean isCompilerGenerated() {
        return compilerGenerated;
    }

    @Override
    public JL5MethodDecl setCompilerGenerated(boolean val) {
        JL5MethodDecl_c n = (JL5MethodDecl_c) copy();
        n.compilerGenerated = val;
        return n;
    }

    @Override
    public List<ParamTypeNode> typeParams() {
        return this.typeParams;
    }

    @Override
    public JL5MethodDecl typeParams(List<ParamTypeNode> typeParams) {
        JL5MethodDecl_c n = (JL5MethodDecl_c) copy();
        n.typeParams = typeParams;
        return n;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return this;
        }

        boolean isVarArgs = false;
        List<UnknownType> formalTypes =
                new ArrayList<UnknownType>(formals.size());
        for (int i = 0; i < formals.size(); i++) {
            formalTypes.add(ts.unknownType(position()));
            JL5Formal f = (JL5Formal) formals.get(i);
            isVarArgs |= f.isVarArg();
        }

        List<UnknownType> throwTypes =
                new ArrayList<UnknownType>(throwTypes().size());
        for (int i = 0; i < throwTypes().size(); i++) {
            throwTypes.add(ts.unknownType(position()));
        }

        Flags f = this.flags;

        if (ct.flags().isInterface()) {
            f = f.Public().Abstract();
        }

        if (isVarArgs) {
            f = JL5Flags.setVarArgs(f);
        }

        List<TypeVariable> typeParams =
                new ArrayList<TypeVariable>(typeParams().size());
        for (int i = 0; i < typeParams().size(); i++) {
            typeParams.add(ts.unknownTypeVariable(position()));
        }

        MethodInstance mi =
                ts.methodInstance(position(),
                                  ct,
                                  f,
                                  ts.unknownType(position()),
                                  name.id(),
                                  formalTypes,
                                  throwTypes,
                                  typeParams);
        ct.addMethod(mi);
        return methodInstance(mi);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        JL5MethodDecl_c n = (JL5MethodDecl_c) super.disambiguate(ar);
        List<TypeVariable> typeParams = new LinkedList<TypeVariable>();

        for (TypeNode tn : n.typeParams) {
            if (!tn.isDisambiguated()) {

                return n;
            }
            TypeVariable tv = (TypeVariable) tn.type();
            typeParams.add(tv);
            tv.declaringProcedure((JL5ProcedureInstance) mi);
        }
        // now type nodes are disambiguated
        JL5MethodInstance mi = (JL5MethodInstance) n.methodInstance();
        mi.setTypeParams(typeParams);
        return n;
    }

    protected MethodDecl_c reconstruct(TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<AnnotationElem> annotations, List<ParamTypeNode> paramTypes) {
        if (returnType != this.returnType || name != this.name
                || !CollectionUtil.equals(formals, this.formals)
                || !CollectionUtil.equals(throwTypes, this.throwTypes)
                || body != this.body
                || !CollectionUtil.equals(annotations, this.annotations)
                || !CollectionUtil.equals(paramTypes, this.typeParams)) {
            JL5MethodDecl_c n = (JL5MethodDecl_c) copy();
            n.name = name;
            n.returnType = returnType;
            n.formals = ListUtil.copy(formals, true);
            n.throwTypes = ListUtil.copy(throwTypes, true);
            n.body = body;
            n.annotations = ListUtil.copy(annotations, true);
            n.typeParams = paramTypes;
            return n;
        }
        return this;

    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // check no duplicate annotations used
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        MethodDecl md = this;
        JL5MethodInstance mi = (JL5MethodInstance) this.methodInstance();
        ts.checkDuplicateAnnotations(annotations);

        // check at most last formal is variable
        for (int i = 0; i < formals.size(); i++) {
            JL5Formal f = (JL5Formal) formals.get(i);
            if (f.isVarArg()) {
                if (i != formals.size() - 1) {
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
                                            position());
            }
        }

        try {
            ts.checkMethodFlags(flags);
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        if (body == null && !(flags.isAbstract() || flags.isNative())) {
            throw new SemanticException("Missing method body.", position());
        }

        if (body != null && flags.isAbstract()) {
            throw new SemanticException("An abstract method cannot have a body.",
                                        position());
        }

        if (body != null && flags.isNative()) {
            throw new SemanticException("A native method cannot have a body.",
                                        position());
        }

        throwsCheck(tc);

        // check that inner classes do not declare static methods
        // unless class is enum
        if (flags.isStatic()
                && !JL5Flags.isEnum(methodInstance().container()
                                                    .toClass()
                                                    .flags())
                && methodInstance().container().toClass().isInnerClass()) {
            // it's a static method in an inner class.
            throw new SemanticException("Inner classes cannot declare "
                    + "static methods.", this.position());
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

        overrideMethodCheck(tc);

        // set the retained annotations
        mi.setRetainedAnnotations(ts.createRetainedAnnotations(this.annotationElems(),
                                                               this.position()));

        return md;
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) annoCheck.typeSystem();
        for (AnnotationElem element : annotations) {
            ts.checkAnnotationApplicability(element, this.methodInstance());
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {

        List<ParamTypeNode> paramTypes = visitList(this.typeParams, v);
        List<AnnotationElem> annotations = visitList(this.annotations, v);
        Id name = (Id) visitChild(this.name, v);
        List<Formal> formals = visitList(this.formals, v);
        TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
        List<TypeNode> throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);

        return reconstruct(returnType,
                           name,
                           formals,
                           throwTypes,
                           body,
                           annotations,
                           paramTypes);
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        if (isCompilerGenerated()) return;

        for (AnnotationElem element : annotations) {
            print(element, w, tr);
        }

        super.translate(w, tr);
    }

    @Override
    public void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr) {
        w.begin(0);

        w.begin(0);
        for (AnnotationElem ae : annotations) {
            ae.prettyPrint(w, tr);
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
        if (printTypeVars && !this.typeParams().isEmpty()) {
            w.write("<");
            for (Iterator<ParamTypeNode> iter = this.typeParams().iterator(); iter.hasNext();) {
                ParamTypeNode ptn = iter.next();
                ptn.prettyPrint(w, tr);
                if (iter.hasNext()) {
                    w.write(",");
                    w.allowBreak(0, " ");
                }
            }
            w.write("> ");
        }

        print(returnType, w, tr);
        w.write(" " + name + "(");
        w.begin(0);

        for (Iterator<Formal> i = formals.iterator(); i.hasNext();) {
            Formal f = i.next();
            print(f, w, tr);

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }

        w.end();
        w.write(")");

        if (!throwTypes().isEmpty()) {
            w.allowBreak(6);
            w.write("throws ");

            for (Iterator<TypeNode> i = throwTypes().iterator(); i.hasNext();) {
                TypeNode tn = i.next();
                print(tn, w, tr);

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
        c = super.enterScope(c);
        for (ParamTypeNode pn : typeParams) {
            ((JL5Context) c).addTypeVariable((TypeVariable) pn.type());
        }
        return c;
    }

}
