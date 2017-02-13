package efg.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import efg.ExtensionInfo;
import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.ExtFactory;
import polyglot.ast.Node;
import polyglot.ext.jl7.types.JL7TypeSystem_c;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;

public class EfgTypeSystem_c extends JL7TypeSystem_c implements EfgTypeSystem {
    protected ClassType ABSTRACT_EXT_FACTORY_;
    protected ClassType EXT_FACTORY_;
    protected ClassType NODE_;

    @Override
    public ExtensionInfo extensionInfo() {
        return (ExtensionInfo) super.extensionInfo();
    }

    @Override
    public ClassType AbstractExtFactory() {
        if (ABSTRACT_EXT_FACTORY_ != null) return ABSTRACT_EXT_FACTORY_;
        return ABSTRACT_EXT_FACTORY_ =
                load(AbstractExtFactory_c.class.getName());
    }

    @Override
    public ClassType ExtFactory() {
        if (EXT_FACTORY_ != null) return EXT_FACTORY_;
        return EXT_FACTORY_ = load(ExtFactory.class.getName());
    }

    @Override
    public ClassType Node() {
        if (NODE_ != null) return NODE_;
        return NODE_ = load(Node.class.getName());
    }

    @Override
    public void checkClassConformance(ClassType ct) throws SemanticException {
        // Do nothing. Not interested in class conformance.
    }

    @Override
    public boolean hasBaseFactory(ClassType nodeType) {
        return !ExtFactory().methods(extensionInfo().defaultFactoryBasename(nodeType),
                                     Collections.<Type> emptyList())
                            .isEmpty();
    }

    @Override
    public ClassType hasFactory(ClassType extFactoryCT, String basename) {
        List<ClassType> visited = new ArrayList<>();
        LinkedList<ClassType> toVisit = new LinkedList<>();
        toVisit.add(extFactoryCT);

        while (!toVisit.isEmpty()) {
            ClassType curCT = toVisit.remove();
            if (!curCT.methods(extensionInfo().factoryName(basename),
                               Collections.<Type> emptyList())
                      .isEmpty()) {
                return curCT;
            }

            visited.add(curCT);

            Type superCT = curCT.superType();
            if (superCT != null && !visited.contains(superCT)) {
                toVisit.add(superCT.toClass());
            }

            for (ReferenceType iface : curCT.interfaces()) {
                if (!visited.contains(iface)) {
                    toVisit.add(iface.toClass());
                }
            }
        }

        return null;
    }
}
