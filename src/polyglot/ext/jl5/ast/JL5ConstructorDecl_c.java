package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.*;
import polyglot.ext.jl5.types.*;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

public class JL5ConstructorDecl_c extends ConstructorDecl_c implements JL5ConstructorDecl {

    protected List<TypeNode> typeParams;
    protected List<AnnotationElem> annotations;

    public JL5ConstructorDecl_c(Position pos, Flags flags, List<AnnotationElem> annotations, Id name, List formals, List throwTypes, Block body) {
        this(pos, flags, annotations, name, formals, throwTypes, body, new ArrayList<ParamTypeNode>());
    }

    public JL5ConstructorDecl_c(Position pos, Flags flags, List<AnnotationElem> annotations, Id name, List formals, List throwTypes, Block body, List typeParams){
        super(pos, flags, name, formals, throwTypes, body);
        this.typeParams = typeParams;
        this.annotations = annotations;
    }

    @Override
    public List typeParams(){
        return this.typeParams;
    }

    @Override
    public JL5ConstructorDecl typeParams(List<TypeNode> typeParams){
        JL5ConstructorDecl_c n = (JL5ConstructorDecl_c) copy();
        n.typeParams = typeParams;
        return n;
    }

    protected JL5ConstructorDecl_c reconstruct(Id name, List formals, List throwTypes, Block body, List typeParams){
        if (!CollectionUtil.equals(formals, this.formals)
        		|| name != this.name
                || !CollectionUtil.equals(throwTypes, this.throwTypes)
                || body != this.body
                || !CollectionUtil.equals(typeParams, this.typeParams)) {
            JL5ConstructorDecl_c n = (JL5ConstructorDecl_c) copy();
    	    n.name = name;
            n.formals = TypedList.copyAndCheck(formals, Formal.class, true);
            n.throwTypes = TypedList.copyAndCheck(throwTypes, TypeNode.class, true);
            n.body = body;
            n.typeParams = typeParams;
            return n;
        }
        return this;

    }

    @Override
    public Node visitChildren(NodeVisitor v){
        List typeParams = visitList(this.typeParams, v);
        Id name = (Id) visitChild(this.name, v);
        List formals = visitList(this.formals, v);
        List throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(name, formals, throwTypes, body, typeParams);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return this;
        }

        List formalTypes = new ArrayList(formals.size());
        for (int i = 0; i < formals.size(); i++) {
            formalTypes.add(ts.unknownType(position()));
        }

        List throwTypes = new ArrayList(throwTypes().size());
        for (int i = 0; i < throwTypes().size(); i++) {
            throwTypes.add(ts.unknownType(position()));
        }

        List typeParams = new ArrayList(typeParams().size());
        for (int i = 0; i < typeParams().size(); i++) {
            typeParams.add(ts.unknownType(position()));
        }

        ConstructorInstance ci = ts.constructorInstance(position(), ct,
                                                        flags, formalTypes, throwTypes, typeParams);
        ct.addConstructor(ci);

        return constructorInstance(ci);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        JL5ConstructorDecl_c n = (JL5ConstructorDecl_c)super.disambiguate(ar);
        List<TypeVariable> typeParams = new LinkedList();

        for (TypeNode tn : n.typeParams) {
            if (!tn.isDisambiguated()) {

                return n;
            }
            TypeVariable tv = (TypeVariable)tn.type();
            typeParams.add(tv);
            tv.declaringProcedure((JL5ProcedureInstance) this.ci);

        }
        // now type nodes are disambiguated
        JL5ConstructorInstance ci = (JL5ConstructorInstance)n.constructorInstance();
        ci.setTypeParams(typeParams);
        return n;
    }

    @Override
    public Context enterScope(Context c) {
        c = super.enterScope(c);
        for (TypeNode pn : typeParams) {
            ((JL5Context)c).addTypeVariable((TypeVariable)pn.type());
        }
        return c;
    }

    @Override
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write(JL5Flags.clearVarArgs(flags).translate());

        // type params
        boolean printTypeVars = true;
        if (tr instanceof JL5Translator) {
            JL5Translator jl5tr = (JL5Translator)tr;
            printTypeVars = !jl5tr.removeJava5isms();
        }
        if (printTypeVars && !this.typeParams().isEmpty()) {
            w.write("<");
            for (Iterator<ParamTypeNode> iter = this.typeParams().iterator(); iter.hasNext(); ) {
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

        for (Iterator i = formals.iterator(); i.hasNext(); ) {
            Formal f = (Formal) i.next();
            print(f, w, tr);

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.end();
        w.write(")");

        if (! throwTypes().isEmpty()) {
            w.allowBreak(6);
            w.write("throws ");

            for (Iterator i = throwTypes().iterator(); i.hasNext(); ) {
                TypeNode tn = (TypeNode) i.next();
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
        // check throws clauses are not parameterized
        for (Iterator it = throwTypes.iterator(); it.hasNext(); ){
            TypeNode tn = (TypeNode)it.next();
            Type next = tn.type();
        }
        
        ConstructorDecl cd = this;

        // check at most last formal is variable
        for (int i = 0; i < formals.size(); i++){
            JL5Formal f = (JL5Formal)formals.get(i);
            if (f.isVarArg()){
                if (i != formals.size()-1) {
                    throw new SemanticException("Only last formal can be variable in constructor declaration.", f.position());
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
            Type last = (Type) ci.formalTypes().get(ci.formalTypes().size()-1);
            if (!(last instanceof JL5ArrayType && ((JL5ArrayType)last).isVarArg())) {
                throw new InternalCompilerError("Inconsistent var args flag with procedure type");
            }
        }
        return super.typeCheck(tc);
    }    
}
