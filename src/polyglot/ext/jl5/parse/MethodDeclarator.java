package polyglot.ext.jl5.parse;

import java.util.List;

import polyglot.ast.Id;
import polyglot.util.Position;

/**
 * Encapsulates some of the data in a method declaration.  Used only by the parser.
 */
//CHECK why do we have these in the grammar ?
public class MethodDeclarator {
	public Position pos;
	public Id name;
	public List formals;
    public Integer dims = new Integer(0);    

	public MethodDeclarator(Position pos, Id name, List formals) {
		this.pos = pos;
		this.name = name;
		this.formals = formals;
	}
	
	public MethodDeclarator(Position pos, Id name, List formals, Integer dims) {
        this(pos, name, formals);
        this.dims = dims;
	}
	
	public Position position() {
		return pos;
	}

    public Id name(){
        return name;
    }

    public List formals(){
        return formals;
    }

    public Integer dims(){
        return dims;
    }
}
