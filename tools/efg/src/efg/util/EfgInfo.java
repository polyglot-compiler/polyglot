package efg.util;

import static efg.util.EfgInfo.ValidationState.FAILED;
import static efg.util.EfgInfo.ValidationState.SUCCEEDED;
import static efg.util.EfgInfo.ValidationState.UNKNOWN;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import efg.ExtensionInfo;
import efg.Options;
import efg.config.ast.Config;
import efg.config.ast.FactoryMapping;
import efg.config.ast.Name;
import efg.config.parse.Grm;
import efg.config.parse.Lexer;
import efg.types.EfgTypeSystem;
import java_cup.runtime.Symbol;
import polyglot.ast.ClassDecl;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;

/**
 * Holds information on how to generate extension factories, based on the
 * configuration file and the source files.
 */
public class EfgInfo {
    protected static enum ValidationState {
        SUCCEEDED, FAILED, UNKNOWN
    }

    protected ValidationState validation;

    protected ExtensionInfo extInfo;
    protected EfgTypeSystem ts;
    protected Config config;

    protected Map<ClassType, EfgClassInfo> factoryMappings;

    public EfgInfo() {
        validation = UNKNOWN;
        factoryMappings = new TreeMap<>(new Comparator<ClassType>() {
            @Override
            public int compare(ClassType o1, ClassType o2) {
                if (o1 == o2) return 0;

                // Null comes last.
                if (o1 == null) return 1;
                if (o2 == null) return -1;

                // Compare according to simple names.
                String sn1 = o1.name();
                String sn2 = o2.name();
                int comp = sn1.compareTo(sn2);
                if (comp != 0) return comp;

                // Compare according to fully qualified names.
                return o1.fullName().compareTo(o2.fullName());
            }
        });
    }

    /**
     * @return
     *     the CamelCase name of the language. (Should be a valid Java
     *     identifier.)
     */
    public String lang() {
        return config.lang.name;
    }

    /**
     * @return the package into which the files should be generated.
     */
    public String packageName() {
        return config.packageName.name;
    }

    /**
     * @return the fully qualified name of the ExtFactory interface to extend.
     */
    public String superInterface() {
        return config.superInterface.name;
    }

    /**
     * @return
     *     the fully qualified name of the AbstractExtFactory class to extend.
     */
    public String superClass() {
        return config.superClass.name;
    }

    public Map<ClassType, EfgClassInfo> factoryMappings() {
        return Collections.unmodifiableMap(factoryMappings);
    }

    /**
     * Ensures that the configuration file has been read and processed.
     */
    public void ensureConfig(ExtensionInfo extInfo) throws SemanticException {
        // Make sure we've read and processed the configuration file.
        if (config == null) {
            this.extInfo = extInfo;
            ts = extInfo.typeSystem();

            config = readConfig(extInfo);

            // Incorporate the configuration file into this object.
            for (FactoryMapping fm : config.factoryMappings) {
                ClassType ct = fm.classType;
                EfgClassInfo efmi = get(ct);
                if (efmi == null) {
                    efmi = new EfgClassInfo(extInfo,
                                            EfgClassInfo.State.CONFIG,
                                            fm.pos,
                                            ct);
                    factoryMappings.put(ct, efmi);
                }

                efmi.setConfig(fm);
            }
        }
    }

    /**
     * Reads the configuration file.
     */
    protected Config readConfig(ExtensionInfo extInfo)
            throws SemanticException {
        Options options = (Options) polyglot.main.Options.global;
        File file = options.confFile();
        ErrorQueue eq = extInfo.compiler().errorQueue();

        try (Reader reader = new FileReader(file)) {
            Lexer lexer = new Lexer(reader, file.getPath(), eq);
            Grm grm = new Grm(lexer, eq);

            Symbol sym;
            try {
                sym = grm.parse();
            }
            catch (IOException e) {
                eq.enqueue(ErrorInfo.IO_ERROR, e.getMessage());
                return Config.dummy();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                eq.enqueue(ErrorInfo.SYNTAX_ERROR, e.getMessage());
                return Config.dummy();
            }

            if (sym.value == null) {
                eq.enqueue(ErrorInfo.SYNTAX_ERROR, "Error parsing " + file);
                return Config.dummy();
            }

            Config result = (Config) sym.value;

            // Ensure the existence of the interface and class being extended.
            return result.validate(extInfo.typeSystem());
        }
        catch (IOException e) {
            throw new InternalCompilerError(e);
        }
    }

    /**
     * Autogenerates a factory mapping skeleton for the given class.
     */
    public void addClass(ExtensionInfo extInfo, ClassDecl cd) {
        ClassType ct = cd.type();
        EfgClassInfo efmi = get(ct);
        if (efmi == null) {
            efmi = new EfgClassInfo(extInfo,
                                    EfgClassInfo.State.AUTO,
                                    cd.position(),
                                    ct);
            factoryMappings.put(ct, efmi);
        }

        efmi.addAuto(cd);
    }

    public EfgClassInfo get(ClassType ct) {
        return factoryMappings.get(ct);
    }

    /**
     * Fills in unspecified values with defaults and ensures that factory method
     * names are not duplicated, and that all delegates exist.
     *
     * @param eq any errors encountered will be added to this error queue.
     */
    public void validate(ErrorQueue eq) {
        if (validation != UNKNOWN) return;
        int errorsBefore = eq.errorCount();
        try {
            // Ensure factory method names are not duplicated. While doing so,
            // collect a set of factory method basenames (each mapped to the
            // name of its corresponding class) that we will be generating.
            Map<String, String> allBasenames = new HashMap<>();
            for (EfgClassInfo classInfo : factoryMappings.values()) {
                for (Name basename : classInfo.basenames.keySet()) {
                    // Check for duplicates in parent extension factory.
                    ClassType dup = ts.hasFactory(config.superInterfaceCT,
                                                  basename.name);
                    if (dup != null) {
                        eq.enqueue(ErrorInfo.SEMANTIC_ERROR,
                                   "Duplicate factory method: already declared in "
                                           + dup,
                                   basename.pos);
                    }
                    // Check for duplicates in extension factory being generated.
                    else if (allBasenames.containsKey(basename.name)) {
                        eq.enqueue(ErrorInfo.SEMANTIC_ERROR,
                                   "Duplicate factory method: already declared "
                                           + "by "
                                           + allBasenames.get(basename.name),
                                   basename.pos);
                    }
                    else {
                        allBasenames.put(basename.name,
                                         classInfo.classType.fullName());
                    }
                }
            }

            // Ensure delegates exist.
            for (EfgClassInfo classInfo : factoryMappings.values()) {
                classInfo.checkDelegates(eq,
                                         config.superInterfaceCT,
                                         allBasenames.keySet());
            }

            // Fill in defaults where needed.
            for (EfgClassInfo classInfo : factoryMappings.values()) {
                classInfo.fillInDefaults(eq,
                                         config.superInterfaceCT,
                                         allBasenames.keySet());
            }
        }
        finally {
            validation = eq.errorCount() == errorsBefore ? SUCCEEDED : FAILED;
        }
    }
}
