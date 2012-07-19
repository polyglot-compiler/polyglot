package polyglot.ext.jl5.types;

import polyglot.types.FieldInstance_c;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class EnumInstance_c extends FieldInstance_c implements EnumInstance {

    protected ParsedClassType anonType;
    final long ordinal;
    
    public EnumInstance_c(TypeSystem ts, Position pos, ReferenceType container, Flags f, String name, ParsedClassType anonType, long ordinal){
        super(ts, pos, container, f.set(JL5Flags.STATIC).set(JL5Flags.PUBLIC).set(JL5Flags.FINAL), container, name);
        if(anonType == null)
        	this.type = container;
        else
        	this.type = anonType;
        this.ordinal = ordinal;
    }
    
    @Override
    public ParsedClassType type(){
        return (ParsedClassType) type;
    }

	@Override
	public long ordinal() {
		return ordinal;
	}
}

