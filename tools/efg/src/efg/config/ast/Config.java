package efg.config.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import efg.types.EfgTypeSystem;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.Pair;
import polyglot.util.Position;

public class Config {
    /**
     * The CamelCase name of the language. (Should be a valid Java identifier.)
     */
    public final Name lang;

    /**
     * The package into which the files should be generated. When adding
     * mappings, any unqualified names are automatically qualified with this
     * package.
     */
    public final Name packageName;

    /**
     * The fully qualified name of the ExtFactory interface to extend.
     */
    public final Name superInterface;

    /**
     * The {@link ClassType} referred to by {@link superInterface}. This is
     * populated during validation.
     */
    public final ClassType superInterfaceCT;

    /**
     * The fully qualified name of the AbstractExtFactory class to extend.
     */
    public final Name superClass;

    /**
     * The {@link ClassType} referred to by {@link superClass}. This is
     * populated during validation.
     */
    public final ClassType superClassCT;

    public final List<FactoryMapping> factoryMappings;

    public Config(Name lang, Pair<Name, Name> extendsNames, Name packageName,
            List<FactoryMapping> factoryMappings) {
        this(lang,
             extendsNames == null ? null : extendsNames.part1(),
             extendsNames == null ? null : extendsNames.part2(),
             packageName,
             factoryMappings);
    }

    public Config(Name lang, Name superInterface, Name superClass,
            Name packageName, List<FactoryMapping> factoryMappings) {
        this(lang,
             superInterface,
             null,
             superClass,
             null,
             packageName,
             factoryMappings);
    }

    private Config(Name lang, Name superInterface, ClassType superInterfaceCT,
            Name superClass, ClassType superClassCT, Name packageName,
            List<FactoryMapping> factoryMappings) {
        this.lang = lang;
        this.superInterface = superInterface;
        this.superInterfaceCT = superInterfaceCT;
        this.superClass = superClass;
        this.superClassCT = superClassCT;
        this.packageName = packageName;
        this.factoryMappings =
                Collections.unmodifiableList(new ArrayList<>(factoryMappings));
    }

    public static Config dummy() {
        return new Config(new Name(Position.compilerGenerated(), ""),
                          null,
                          null,
                          new Name(Position.compilerGenerated(), ""),
                          Collections.<FactoryMapping> emptyList());
    }

    public Config validate(EfgTypeSystem ts) throws SemanticException {
        return validate(new Validator(ts));
    }

    /**
     * Qualifies all unqualified classes and checks that all mentioned classes
     * exist.
     */
    protected Config validate(Validator v) throws SemanticException {
        // Make sure the super interface and super class exist.
        final Name superInterface;
        final ClassType superInterfaceCT;
        if (this.superInterface == null) {
            // Use default.
            superInterfaceCT = v.ts.ExtFactory();
            superInterface = new Name(Position.compilerGenerated(),
                                      superInterfaceCT.fullName());
        }
        else {
            superInterface = this.superInterface;
            superInterfaceCT = v.validateClass(superInterface);
        }

        Name superClass;
        ClassType superClassCT;
        if (this.superClass == null) {
            // Use default.
            superClassCT = v.ts.AbstractExtFactory();
            superClass = new Name(Position.compilerGenerated(),
                                  superClassCT.fullName());
        }
        else {
            superClass = this.superClass;
            superClassCT = v.validateClass(superClass);
        }

        // Construct a map from class names to their corresponding
        // FactoryMappings.
        Map<String, FactoryMapping> factoryMap =
                new HashMap<>(factoryMappings.size());
        for (FactoryMapping fm : factoryMappings) {
            fm = fm.qualifyClass(packageName);

            String className = fm.className.name;
            if (factoryMap.containsKey(className)) {
                throw new SemanticException("Duplicate factory declaration for "
                        + "class " + className, fm.pos);
            }

            factoryMap.put(className, fm.validate(v));
        }

        return new Config(lang,
                          superInterface,
                          superInterfaceCT,
                          superClass,
                          superClassCT,
                          packageName,
                          new ArrayList<>(factoryMap.values()));
    }
}
