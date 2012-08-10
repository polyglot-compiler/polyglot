package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.ConstructorDecl_c;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
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
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5ConstructorDecl_c extends ConstructorDecl_c implements
        JL5ConstructorDecl {

    protected List<ParamTypeNode> typeParams;
    protected List<AnnotationElem> annotations;

    public JL5ConstructorDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        this(pos,
             flags,
             annotations,
             name,
             formals,
             throwTypes,
             body,
             new ArrayList<ParamTypeNode>());
    }

    public JL5ConstructorDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        super(pos, flags, name, formals, throwTypes, body);
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

    public JL5ConstructorDecl annotations(List<AnnotationElem> annotations) {
        JL5ConstructorDecl_c n = (JL5ConstructorDecl_c) copy();
        n.annotations = annotations;
        return n;
    }

    @Override
    public List<ParamTypeNode> typeParams() {
        return this.typeParams;
    }

    @Override
    public JL5ConstructorDecl typeParams(List<ParamTypeNode> typeParams) {
        JL5ConstructorDecl_c n = (JL5ConstructorDecl_c) copy();
        n.typeParams = typeParams;
        return n;
    }

    protected JL5ConstructorDecl_c reconstruct(Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<AnnotationElem> annotations, List<ParamTypeNode> typeParams) {
        if (!CollectionUtil.equals(formals, this.formals) || name != this.name
                || !CollectionUtil.equals(throwTypes, this.throwTypes)
                || body != this.body
                || !CollectionUtil.equals(annotations, this.annotations)
                || !CollectionUtil.equals(typeParams, this.typeParams)) {
            JL5ConstructorDecl_c n = (JL5ConstructorDecl_c) copy();
            n.name = name;
            n.formals = ListUtil.copy(formals, true);
            n.throwTypes = ListUtil.copy(throwTypes, true);
            n.body = body;
            n.annotations = ListUtil.copy(annotations, true);
            n.typeParams = typeParams;
            return n;
        }
        return this;

    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<AnnotationElem> annotations = visitList(this.annotations, v);
        List<ParamTypeNode> typeParams = visitList(this.typeParams, v);
        Id name = (Id) visitChild(this.name, v);
        List<Formal> formals = visitList(this.formals, v);
        List<TypeNode> throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(name,
                           formals,
                           throwTypes,
                           body,
                           annotations,
                           typeParams);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return this;
        }

        boolean vararg = false;
        List<UnknownType> formalTypes =
                new ArrayList<UnknownType>(formals.size());
        for (int i = 0; i < formals.size(); i++) {
            formalTypes.add(ts.unknownType(position()));
            JL5Formal f = (JL5Formal) formals.get(i);
            if (f.isVarArg()) vararg = true;
        }

        List<UnknownType> throwTypes =
                new ArrayList<UnknownType>(throwTypes().size());
        for (int i = 0; i < throwTypes().size(); i++) {
            throwTypes.add(ts.unknownType(position()));
        }

        List<TypeVariable> typeParams =
                new ArrayList<TypeVariable>(typeParams().size());
        for (int i = 0; i < typeParams().size(); i++) {
            typeParams.add(ts.unknownTypeVariable(position()));
        }
        if (vararg) flags = JL5Flags.VARARGS.set(this.flags);
        ConstructorInstance ci =
                ts.constructorInstance(position(),
                                       ct,
                                       flags,
                                       formalTypes,
                                       throwTypes,
                                       typeParams);
        ct.addConstructor(ci);

        return constructorInstance(ci).flags(flags);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        JL5ConstructorDecl_c n = (JL5ConstructorDecl_c) super.disambiguate(ar);
        List<TypeVariable> typeParams = new LinkedList<TypeVariable>();

        for (TypeNode tn : n.typeParams) {
            if (!tn.isDisambiguated()) {

                return n;
            }
            TypeVariable tv = (TypeVariable) tn.type();
            typeParams.add(tv);
            tv.declaringProcedure((JL5ProcedureInstance) this.ci);

        }
        // now type nodes are disambiguated
        JL5ConstructorInstance ci =
                (JL5ConstructorInstance) n.constructorInstance();
        ci.setTypeParams(typeParams);
        return n;
    }

    @Override
    public Context enterScope(Context c) {
        c = super.enterScope(c);
        for (TypeNode pn : typeParams) {
            ((JL5Context) c).addTypeVariable((TypeVariable) pn.type());
        }
        return c;
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) annoCheck.typeSystem();
        for (AnnotationElem element : annotations) {
            ts.checkAnnotationApplicability(element, this.constructorInstance());
        }
        return this;
    }

    @Override
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
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

        tr.print(this, name, w);
        w.write("(");

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
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ConstructorDecl cd = this;
        JL5ConstructorInstance ci =
                (JL5ConstructorInstance) this.constructorInstance();
        JL5TypeSystem ts = (JL5TypeSystem) ci.typeSystem();

        // check at most last formal is variable
        for (int i = 0; i < formals.size(); i++) {
            JL5Formal f = (JL5Formal) formals.get(i);
            if (f.isVarArg()) {
                if (i != formals.size() - 1) {
                    throw new SemanticException("Only last formal can be variable in constructor declaration.",
                                                f.position());
                }
                else {
                    ci.setFlags(JL5Flags.setVarArgs(ci.flags()));
                    cd = cd.flags(JL5Flags.setVarArgs(cd.flags()));
                }
            }
        }

        Flags flags = ci.flags();
        // check that the varargs flag is consistent with the type of the last argument.
        if (JL5Flags.isVarArgs(cd.flags()) != JL5Flags.isVarArgs(flags)) {
            throw new InternalCompilerError("VarArgs flag of AST and type disagree");
        }

        if (JL5Flags.isVarArgs(flags)) {
            // check that the last formal type is an array
            if (ci.formalTypes().isEmpty()) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
            Type last = ci.formalTypes().get(ci.formalTypes().size() - 1);
            if (!(last instanceof JL5ArrayType && ((JL5ArrayType) last).isVarArg())) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
        }

        // set the retained annotations
        ci.setRetainedAnnotations(ts.createRetainedAnnotations(this.annotationElems(),
                                                               this.position()));

        return super.typeCheck(tc);
    }
}
