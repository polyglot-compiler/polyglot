package efg.config.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.Position;

/**
 * A node class name paired with its factory names.
 */
public class FactoryMapping extends Node {
    public final Name className;
    public final ClassType classType;

    public final Name superClassName;
    public final ClassType superClassType;

    public final List<FactoryName> factoryNames;

    public FactoryMapping(Position pos, Name className) {
        this(pos, className, null, null);
    }

    public FactoryMapping(Position pos, Name className, Name superClassName) {
        this(pos, className, superClassName, null);
    }

    public FactoryMapping(Position pos, Name className,
            List<FactoryName> factoryNames) {
        this(pos, className, null, factoryNames);
    }

    public FactoryMapping(Position pos, Name className, Name superClassName,
            List<FactoryName> factoryNames) {
        this(pos, className, null, superClassName, null, factoryNames);
    }

    private FactoryMapping(Position pos, Name className, ClassType classType,
            Name superClassName, ClassType superClassType,
            List<FactoryName> factoryNames) {
        super(pos);
        this.className = className;
        this.classType = classType;
        this.superClassName = superClassName;
        this.superClassType = superClassType;

        if (factoryNames == null) {
            // Use the simple form of the className by default.
            int dotPos = className.name.lastIndexOf(".");
            Name simpleClassName =
                    new Name(className.pos,
                             className.name.substring(dotPos + 1));
            this.factoryNames =
                    Collections.singletonList(new FactoryName(className.pos,
                                                              simpleClassName));
        }
        else {
            this.factoryNames =
                    Collections.unmodifiableList(new ArrayList<>(factoryNames));
        }
    }

    /**
     * Qualifies the class name and supertype name with the given package name.
     */
    public FactoryMapping qualifyClass(Name packageName) {
        Name className = this.className;
        if (!className.name.contains(".")) {
            className = new Name(className.pos,
                                 packageName.name + "." + className.name);
        }

        Name superClassName = this.superClassName;
        if (superClassName != null && !superClassName.name.contains(".")) {
            superClassName =
                    new Name(superClassName.pos,
                             packageName.name + "." + superClassName.name);
        }

        if (className == this.className
                && superClassName == this.superClassName) {
            return this;
        }

        return new FactoryMapping(pos, className, superClassName, factoryNames);
    }

    /**
     * Checks that all mentioned classes exist.
     */
    public FactoryMapping validate(Validator v) throws SemanticException {
        // Ensure the class and the super class exist.
        ClassType classType = v.validateClass(className);

        Name superClassName = null;
        ClassType superClassType = null;
        if (this.superClassName != null) {
            superClassName = this.superClassName;
            superClassType = v.validateClass(this.superClassName);
        }

        return new FactoryMapping(pos,
                                  className,
                                  classType,
                                  superClassName,
                                  superClassType,
                                  factoryNames);
    }
}
