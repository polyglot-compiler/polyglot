package polyglot.visit;

import polyglot.ast.Call;
import polyglot.ast.Field;
import polyglot.ast.New;
import polyglot.ast.Node;

public class TypeClosure extends NodeVisitor {
    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (n instanceof Call) {
            // just make sure that all classes are loaded appropriately.
            Call c = (Call) n;
            c.target().type().toReference().members();
        }
        if (n instanceof Field) {
            // just make sure that all classes are loaded appropriately.
            Field f = (Field) n;
            f.target().type().toReference().members();
        }
        if (n instanceof New) {
            // just make sure that all classes are loaded appropriately.
            New ne = (New) n;
            ne.type().toReference().members();
        }
        return n;
    }
}
