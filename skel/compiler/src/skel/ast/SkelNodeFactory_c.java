package skelpkg.ast;

import polyglot.ast.*;
import polyglot.util.*;

import java.util.*;

/**
 * NodeFactory for skel extension.
 */
public class SkelNodeFactory_c extends NodeFactory_c implements SkelNodeFactory {
    public SkelNodeFactory_c(SkelLang lang, SkelExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public SkelExtFactory extFactory() {
        return (SkelExtFactory) super.extFactory();
    }

    // TODO:  Implement factory methods for new AST nodes.
    // TODO:  Override factory methods for overridden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.
}
