package efg.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import efg.ExtensionInfo;
import efg.config.ast.FactoryMapping;
import efg.config.ast.FactoryName;
import efg.config.ast.Name;
import efg.types.EfgTypeSystem;
import polyglot.ast.ClassDecl;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;

/**
 * Information about the ExtFactory methods to generate for a single
 * ClassType.
 */
public class EfgClassInfo {
    public static enum State {
        /**
         * Indicates an {@link EFMethodInfo} that was derived automatically.
         */
        AUTO,

        /**
         * Indicates an {@link EFMethodInfo} that was derived from a
         * configuration file.
         */
        CONFIG
    }

    protected final ExtensionInfo extInfo;
    protected final EfgTypeSystem ts;

    protected EfgClassInfo.State state;

    /**
     * The position associated with {@link classType}.
     */
    protected final Position pos;

    /**
     * The class type to which this object corresponds.
     */
    protected final ClassType classType;

    /**
     * A supertype of {@link classType}. The supertype is assumed to have an
     * extension factory in the Polyglot base language.
     */
    protected ClassType superType;

    /**
     * The base names for the factory methods that will be generated, paired
     * with the basenames of their delegates.
     */
    protected final Map<Name, Name> basenames;

    public EfgClassInfo(ExtensionInfo extInfo, EfgClassInfo.State state,
            Position pos, ClassType classType) {
        this.pos = pos;
        this.extInfo = extInfo;
        ts = extInfo.typeSystem();
        this.classType = classType;
        this.state = state;
        basenames = new TreeMap<>(new Comparator<Name>() {
            @Override
            public int compare(Name o1, Name o2) {
                if (o1 == o2) return 0;
                if (o1 == null) return 1;
                if (o2 == null) return -1;
                return o1.name.compareTo(o2.name);
            }
        });
    }

    /**
     * Overrides any automatically generated factory names with the given
     * factory mapping.
     */
    public void setConfig(FactoryMapping fm) {
        if (state == State.AUTO) {
            basenames.clear();
            state = State.CONFIG;
        }

        superType = fm.superClassType;

        if (fm.factoryNames == null) {
            basenames.put(new Name(fm.className.pos,
                                   extInfo.defaultBasename(classType)),
                          null);
        }
        else {
            for (FactoryName fn : fm.factoryNames) {
                basenames.put(fn.basename, fn.delegate);
            }
        }
    }

    /**
     * Adds automatically generated basenames for the given class.
     */
    public void addAuto(ClassDecl cd) {
        if (state == State.CONFIG) {
            // Do nothing: basenames are not automatically generated for the
            // class to which this object corresponds.
            return;
        }

        basenames.put(new Name(cd.id().position(),
                               extInfo.defaultBasename(cd.type())),
                      null);
    }

    /**
     * @return true if nothing will be generated for {@link classType}.
     */
    public boolean isEmpty() {
        return basenames != null && basenames.isEmpty();
    }

    public ClassType superType() {
        return superType;
    }

    public Map<Name, Name> basenames() {
        return Collections.unmodifiableMap(basenames);
    }

    /**
     * Verifies that all delegates exist.
     *
     * @param eq any errors encountered will be added to this error queue.
     * @param superInterfaceCT
     *         the class type of the extension factory that will be extended.
     * @param allBasenames
     *         the basenames of all factory methods that will be generated.
     */
    public void checkDelegates(ErrorQueue eq, ClassType superInterfaceCT,
            Set<String> allBasenames) {
        for (Name delegate : basenames.values()) {
            if (delegate == null) continue;

            if (!allBasenames.contains(delegate.name)
                    && ts.hasFactory(superInterfaceCT, delegate.name) == null) {
                eq.enqueue(ErrorInfo.SEMANTIC_ERROR,
                           "Extension factory method is undefined",
                           delegate.pos);
            }
        }
    }

    /**
     * Assigns a default value to {@link superType} and fills in default
     * delegates, as needed.
     *
     * @param eq any errors encountered will be added to this error queue.
     * @param superInterfaceCT
     *         the class type of the extension factory that will be extended.
     * @param allBasenames
     *         the basenames of all factory methods that will be generated.
     */
    public void fillInDefaults(ErrorQueue eq, ClassType superInterfaceCT,
            Set<String> allBasenames) {
        // Only need a super type if at least one extension factory method will
        // be generated.
        if (superType == null && !isEmpty()) {
            try {
                superType = findSuperType();
            }
            catch (SemanticException e) {
                eq.enqueue(ErrorInfo.SEMANTIC_ERROR,
                           e.getMessage(),
                           e.position());
            }
        }

        // Fill in default delegates.
        try {
            for (Map.Entry<Name, Name> entry : basenames.entrySet()) {
                if (entry.getValue() != null) continue;

                Name defaultDelegate =
                        new Name(Position.compilerGenerated(),
                                 findDefaultDelegateBasename(entry.getKey(),
                                                             superInterfaceCT,
                                                             allBasenames));

                entry.setValue(defaultDelegate);
            }
        }
        catch (SemanticException e) {
            eq.enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), e.position());
        }
    }

    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>(2);
        map.put("state", state);
        map.put("type", classType);
        map.put("supertype", superType);
        map.put("basenames", basenames);
        return map.toString();
    }

    /**
     * Finds the most specific supertype of {@link classType}, such that the
     * supertype has an extension factory in the Polyglot base language.
     *
     * @throws SemanticException
     *     if no such type could not be found, or if more than one type
     *     qualifies
     */
    protected ClassType findSuperType() throws SemanticException {
        // Find supertypes for which there is an extension factory method in the
        // Polyglot base language. (We prune supertypes of types that we have
        // already found, since we will eventually be taking the most specific
        // one.)
        LinkedList<ClassType> toExamine = new LinkedList<>();
        toExamine.add(classType);

        Set<ClassType> candidates = new HashSet<>();
        while (!toExamine.isEmpty()) {
            ClassType curCT = toExamine.remove();
            if (!curCT.isSubtype(ts.Node())) continue;
            if (candidates.contains(curCT)) continue;

            if (ts.hasBaseFactory(curCT)) {
                candidates.add(curCT);
                continue;
            }

            addSuperTypes(toExamine, curCT);
        }

        Set<ClassType> mostSpecific = mostSpecific(candidates);
        if (mostSpecific.isEmpty()) {
            throw new SemanticException("No base factory found for "
                    + classType.name(), pos);
        }

        if (mostSpecific.size() > 1) {
            throw new SemanticException("Multiple base factories found for "
                    + classType.name()
                    + ". Please specify one in the configuration file.\n"
                    + "Candidate base factories: " + mostSpecific, pos);
        }

        return mostSpecific.iterator().next();
    }

    /**
     * Finds the basename of the default delegate. This is the name of the most
     * specific super type, for which an extension factory method either will be
     * created, or exists in the extension factory's supertype.
     *
     * @param name the basename for which we are finding the default delegate.
     */
    protected String findDefaultDelegateBasename(Name basename,
            ClassType superInterfaceCT, Set<String> allBasenames)
            throws SemanticException {
        // We prune supertypes of types that we have already found, since we
        // will eventually be taking the most specific one.
        LinkedList<ClassType> toExamine = new LinkedList<>();
        toExamine.add(classType);

        Map<ClassType, String> candidates = new HashMap<>();
        while (!toExamine.isEmpty()) {
            ClassType curCT = toExamine.remove();
            String curBasename = extInfo.defaultBasename(curCT);
            if (!curCT.isSubtype(ts.Node())) continue;
            if (candidates.containsKey(curCT)) continue;

            // Avoid delegating to self.
            if (!curBasename.equals(basename.name)) {
                if (ts.hasFactory(superInterfaceCT, curBasename) != null) {
                    candidates.put(curCT, curBasename);
                    continue;
                }
            }

            addSuperTypes(toExamine, curCT);
        }

        Set<ClassType> mostSpecific = mostSpecific(candidates.keySet());
        if (mostSpecific.isEmpty()) {
            throw new SemanticException("No delegate found for "
                    + classType.name(), pos);
        }

        if (mostSpecific.size() > 1) {
            candidates.keySet().retainAll(mostSpecific);
            throw new SemanticException("Multiple possible delegates found for "
                    + classType.name()
                    + ". Please specify one in the configuration file.\n"
                    + "Candidate delegates: "
                    + new HashSet<>(candidates.values()), pos);
        }

        return candidates.get(mostSpecific.iterator().next());
    }

    /**
     * Adds immediate super types of the given {@link ClassType} to the given
     * {@link Collection}.
     */
    protected void addSuperTypes(Collection<ClassType> s, ClassType ct) {
        Type superType = ct.superType();
        if (superType != null) s.add(superType.toClass());

        @SuppressWarnings("unchecked")
        Collection<ClassType> interfaces =
                (Collection<ClassType>) ct.interfaces();
        s.addAll(interfaces);
    }

    protected Set<ClassType> mostSpecific(Collection<ClassType> c) {
        // Find the most specific ones.
        // This could probably be made more efficient, but it will do for now.
        Set<ClassType> mostSpecific = new HashSet<>();
        for (ClassType curCT : c) {
            boolean isMostSpecific = true;
            for (ClassType otherCT : c) {
                if (curCT.equals(otherCT)) continue;
                if (otherCT.isSubtype(curCT)) {
                    isMostSpecific = false;
                    break;
                }
            }

            if (isMostSpecific) mostSpecific.add(curCT);
        }

        return mostSpecific;
    }
}
