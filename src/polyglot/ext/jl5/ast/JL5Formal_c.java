package polyglot.ext.jl5.ast;

import polyglot.ast.Formal_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.types.ArrayType;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5Formal_c extends Formal_c implements JL5Formal {

    protected boolean isVarArg;

    public JL5Formal_c(Position pos, Flags flags, TypeNode type, Id name){
        this(pos, flags, type, name, false);
    }

    public JL5Formal_c(Position pos, Flags flags, TypeNode type, Id name, boolean variable){
        super(pos, flags, type, name);
        this.isVarArg = variable;
    }

    @Override
    public boolean isVarArg(){
        return isVarArg;
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (isVarArg()){
            ((JL5ArrayType)type().type()).setVarArg();
        }
        JL5Formal_c form = (JL5Formal_c) super.disambiguate(ar);

        return form;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr){
        w.write(JL5Flags.clearVarArgs(flags).translate());
        if (isVarArg()){
            w.write(((ArrayType)type.type()).base().toString());
            //print(type, w, tr);
            w.write(" ...");
        }
        else {
            print(type, w, tr);
        }
        w.write(" ");
        w.write(name.id());

    }
}
