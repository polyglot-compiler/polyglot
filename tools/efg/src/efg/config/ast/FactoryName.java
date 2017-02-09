package efg.config.ast;

import polyglot.util.Position;

/**
 * The basename of a factory method and its delegate.
 */
public class FactoryName extends Node {
    public final Name basename;
    public final Name delegate;

    public FactoryName(Position pos, Name basename) {
        this(pos, basename, null);
    }

    public FactoryName(Position pos, Name basename, Name delegate) {
        super(pos);
        this.basename = basename;
        this.delegate = delegate;
    }
}
