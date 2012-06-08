package polyglot.ext.jl5.ast;

import polyglot.ast.*;
import polyglot.ext.jl5.types.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

public class JL5Disamb_c extends Disamb_c {
        
    @Override
	protected Node disambiguateTypeNodePrefix(TypeNode tn) throws SemanticException {
        Type t = tn.type();
        //System.out.println("JL5Disamb_c.dis tnp " + this);
    
        if (t.isReference() && exprOK()){
            try {
                FieldInstance fi = ((JL5TypeSystem)ts).findFieldOrEnum(t.toReference(), name.id(), c.currentClass());
                return nf.Field(pos, tn, name).fieldInstance(fi);
            }
            catch(NoMemberException e){
                if (e.getKind() != NoMemberException.FIELD && e.getKind() != JL5NoMemberException.ENUM_CONSTANT){
                    throw e;
                }
            }
        }

        if (t.isClass() && typeOK()){
            Resolver tc = ts.classContextResolver(t.toClass());            
            Named n = tc.find(name.id());
            if (n instanceof Type){
                // we found a type that was named appropriately. Access it 
                // through t in order to ensure that substitution is 
                // applied correctly.
                Type type = t.toClass().memberClassNamed(name.id());
                return nf.CanonicalTypeNode(pos, type);
            }
        }
        return null;
    }

    @Override
	protected Node disambiguateExprPrefix(Expr e) throws SemanticException {
        //System.out.println("JL5Disamb_c.dis ep " + this);
        if (exprOK()){
            return nf.Field(pos, e, name);        
        }
        return null;
    }


    @Override
	protected Node disambiguateVarInstance(VarInstance vi) throws SemanticException {
        //System.out.println("JL5Disamb_c.dis vi " + this);
        if (vi instanceof FieldInstance) {
            FieldInstance fi = (FieldInstance) vi;
            Receiver r = makeMissingFieldTarget(fi);
            return nf.Field(pos, r, name).fieldInstance(fi).targetImplicit(true);
        } else if (vi instanceof LocalInstance) {
            LocalInstance li = (LocalInstance) vi;
            return nf.Local(pos, name).localInstance(li);
        }
        return null;
    }


    @Override
	protected Node disambiguateNoPrefix() throws SemanticException {
        // First try local variables and fields
        if (exprOK()){
            VarInstance vi = c.findVariableSilent(name.id());
            Node n = disambiguateVarInstance(vi);
            if (n != null) return n;
        }
        
        // no variable found. try types.
        if (typeOK()) {
            try {
                Named n = c.find(name.id());
                if (n instanceof Type) {
                    Type type = (Type) n;
                    if (! type.isCanonical()) {
                        throw new InternalCompilerError("Found an ambiguous type in the context: " + type, pos);
                    }
                    return nf.CanonicalTypeNode(pos, type);
                }
            } catch (NoClassException e) {
                if (!name.id().equals(e.getClassName())) {
                    // hmm, something else must have gone wrong
                    // rethrow the exception
                    throw e;
                }

                // couldn't find a type named name. 
                // It must be a package--ignore the exception.
            }
        }
        
        // try type variables.
        TypeVariable res = ((JL5Context)c).findTypeVariableInThisScope(name.id());
        if (res != null){
                //System.out.println("JL5Disamb: TypeVariable " +name.id() + " has type " + res + " " + res.getClass());
            return nf.CanonicalTypeNode(pos, res);
        }

        // Must be a package then...
        if (packageOK()) {
            return nf.PackageNode(pos, ts.packageForName(name.id()));
        }
        return null;
    }
}
