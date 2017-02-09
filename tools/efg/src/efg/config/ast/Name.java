package efg.config.ast;

import polyglot.lex.Identifier;
import polyglot.util.Position;

/**
 * Can represent a fully qualified or unqualified name.
 */
public class Name extends Node {
    public final String name;

    public Name(Position pos, String name) {
        super(pos);
        this.name = name;
    }

    public Name(Identifier id) {
        this(id.getPosition(), id.getIdentifier());
    }

    @Override
    public String toString() {
        return name;
    }
}
