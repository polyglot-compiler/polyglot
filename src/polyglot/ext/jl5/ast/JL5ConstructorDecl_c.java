package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.*;
import polyglot.ext.jl5.types.*;
import polyglot.types.*;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5ConstructorDecl_c extends ConstructorDecl_c implements JL5ConstructorDecl {

    protected List<TypeNode> typeParams;
    
    public JL5ConstructorDecl_c(Position pos, Flags flags, Id name, List formals, List throwTypes, Block body) {
    	this(pos, flags, name, formals, throwTypes, body, new ArrayList<ParamTypeNode>());
    }

    public JL5ConstructorDecl_c(Position pos, Flags flags, Id name, List formals, List throwTypes, Block body, List typeParams){
        super(pos, flags, name, formals, throwTypes, body);
        this.typeParams = typeParams;
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

    protected JL5ConstructorDecl_c reconstruct(List formals, List throwTypes, Block body, List typeParams){
		if (!CollectionUtil.equals(formals, this.formals)
				|| !CollectionUtil.equals(throwTypes, this.throwTypes)
				|| body != this.body
				|| !CollectionUtil.equals(typeParams, this.typeParams)) {
            JL5ConstructorDecl_c n = (JL5ConstructorDecl_c) copy();
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
        List formals = visitList(this.formals, v);
        List throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(formals, throwTypes, body, typeParams);
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
            c = ((JL5Context)c).addTypeVariable((TypeVariable)pn.type());
        }
        return c;
    }
       
    @Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
        // check at most last formal is variable
        for (int i = 0; i < formals.size(); i++){
            JL5Formal f = (JL5Formal)formals.get(i);
            if (i != formals.size()-1 && f.isVarArg()){
                throw new SemanticException("Only last formal can be variable in constructor declaration.", f.position());
            }
        }
        Flags flags = ci.flags();
        // check that the varargs flag is consistent with the type of the last argument.
        if (JL5Flags.isVarArgs(this.flags()) != JL5Flags.isVarArgs(flags)) {
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
