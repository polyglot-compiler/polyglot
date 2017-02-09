package efg.config.ast;

import efg.types.EfgTypeSystem;
import polyglot.ast.ExtFactory;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;

public class Validator {
    protected final EfgTypeSystem ts;

    /**
     * Represents {@link ExtFactory}.
     */
    protected final ClassType baseExtFactoryCT;

    public Validator(EfgTypeSystem ts) {
        this.ts = ts;

        try {
            baseExtFactoryCT =
                    (ClassType) ts.typeForName(ExtFactory.class.getName());
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    public ClassType validateClass(Name name) throws SemanticException {
        try {
            return ts.typeForName(name.name).toClass();
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), name.pos);
        }
    }
}
