package polyglot.ext.jl5.parse;

import polyglot.ast.Id;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.parse.Name;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class JL5Name extends Name {

    public JL5NodeFactory nf;
    public JL5TypeSystem ts;
    

    public JL5Name(NodeFactory nf, TypeSystem ts, Position pos, Id name) {
        super(nf, ts, pos, name);
    }
    
    public JL5Name(NodeFactory nf, TypeSystem ts, Position pos, Name prefix, Id name) {
        super(nf, ts, pos, prefix, name);
    }       
}
