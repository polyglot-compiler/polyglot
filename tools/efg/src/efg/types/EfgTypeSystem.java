package efg.types;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.ExtFactory;
import polyglot.ast.Node;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ClassType;

public interface EfgTypeSystem extends JL7TypeSystem {
    /**
     * @return a {@link ClassType} object representing {@link Node}.
     */
    ClassType Node();

    /**
     * @return a {@link ClassType} object representing {@link ExtFactory}.
     */
    ClassType ExtFactory();

    /**
     * @return a {@link ClassType} object representing
     *         {@link AbstractExtFactory_c}.
     */
    ClassType AbstractExtFactory();

    /**
     * Determines whether there is a factory method for the given node type in
     * the base language's extension factory.
     */
    boolean hasBaseFactory(ClassType nodeType);

    /**
     * Determines whether the given {@link ExtFactory} (or a super type) has a
     * factory method with the given basename. If so, the {@link ClassType}
     * containing the factory method is returned; otherwise, {@code null} is
     * returned.
     */
    ClassType hasFactory(ClassType extFactoryCT, String basename);
}
