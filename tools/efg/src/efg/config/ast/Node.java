package efg.config.ast;

import polyglot.util.Position;

public abstract class Node {
    public final Position pos;

    public Node(Position pos) {
        this.pos = pos;
    }
}
