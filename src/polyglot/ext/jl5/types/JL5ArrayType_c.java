package polyglot.ext.jl5.types;

import java.util.Collections;

import polyglot.types.ArrayType_c;
import polyglot.types.MethodInstance;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class JL5ArrayType_c extends ArrayType_c implements JL5ArrayType {

    protected boolean isVarArg;

    public JL5ArrayType_c(TypeSystem ts, Position pos, Type base, boolean isVarargs){
        super(ts, pos, base);
        this.isVarArg = isVarargs;
    }
    
    @Override
    protected MethodInstance createCloneMethodInstance() {
        return ts.methodInstance(position(),
                this,
                ts.Public(),
                this, // clone returns this type
                "clone",
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST);
    }

    @Override
    public boolean isVarArg(){
        return this.isVarArg;
    }
    
    @Override
    public void setVarArg() {
    	this.isVarArg = true;
    }

}
