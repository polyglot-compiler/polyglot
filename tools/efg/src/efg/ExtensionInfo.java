package efg;

import java.io.Reader;
import java.util.Set;

import efg.ast.EfgExtFactory_c;
import efg.ast.EfgLang_c;
import efg.ast.EfgNodeFactory;
import efg.ast.EfgNodeFactory_c;
import efg.frontend.EfgScheduler;
import efg.parse.Grm;
import efg.parse.Lexer_c;
import efg.translate.JL7OutputExtensionInfo;
import efg.types.EfgTypeSystem;
import efg.types.EfgTypeSystem_c;
import efg.util.EfgInfo;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;
import polyglot.ext.jl7.JL7ExtensionInfo;
import polyglot.ext.jl7.ast.JL7ExtFactory_c;
import polyglot.frontend.CupParser;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.Scheduler;
import polyglot.frontend.Source;
import polyglot.frontend.goals.Goal;
import polyglot.lex.Lexer;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;

/**
 * Extension information for Efg extension.
 */
public class ExtensionInfo extends JL7ExtensionInfo {
    // XXX Ugly global. How can this be done differently?
    /**
     * Associates class types with information on how to generate factory
     * methods for that type.
     */
    public static final EfgInfo EFG_INFO = new EfgInfo();

    public static final String EXT_FACTORY_BASENAME = "ExtFactory";
    public static final String ABSTRACT_EXT_FACTORY_BASENAME =
            "AbstractExtFactory_c";

    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
        return "java";
    }

    @Override
    public String compilerName() {
        return "efg";
    }

    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source, eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    @Override
    public Set<String> keywords() {
        return new Lexer_c(null).keywords();
    }

    @Override
    protected Options createOptions() {
        return new Options(this);
    }

    @Override
    protected NodeFactory createNodeFactory() {
        return new EfgNodeFactory_c(EfgLang_c.INSTANCE,
                                    new EfgExtFactory_c(new JL7ExtFactory_c(new JL5ExtFactory_c())));
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new EfgTypeSystem_c();
    }

    @Override
    public EfgNodeFactory nodeFactory() {
        return (EfgNodeFactory) super.nodeFactory();
    }

    @Override
    public EfgTypeSystem typeSystem() {
        return (EfgTypeSystem) super.typeSystem();
    }

    @Override
    public Scheduler createScheduler() {
        return new EfgScheduler(this);
    }

    @Override
    public Goal getCompileGoal(Job job) {
        return ((EfgScheduler) scheduler).EfgInfoValidated(job);
    }

    @Override
    public JL7ExtensionInfo outputExtensionInfo() {
        if (outputExtensionInfo == null) {
            outputExtensionInfo = new JL7OutputExtensionInfo(this);
        }

        return (JL7ExtensionInfo) outputExtensionInfo;
    }

    /**
     * @return the default basename for an extension factory method for the
     *         given node class.
     */
    public String defaultFactoryBasename(ClassType nodeType) {
        return factoryName(defaultBasename(nodeType));
    }

    /**
     * @return the default basename for the extension factory method for the
     *         given node class. (e.g., for Node_c, the default extension
     *         factory method is extNode(), having a basename of "Node")
     */
    public String defaultBasename(ClassType nodeType) {
        // Compute the method's basename by chopping off any "_c" that might
        // occur at the end of the class's simple name.
        String basename = nodeType.name();
        if (basename.endsWith("_c")) {
            basename = basename.substring(0, basename.length() - 2);
        }

        return basename;
    }

    /**
     * @return the name of the extension factory method for the given basename.
     */
    public String factoryName(String basename) {
        return "ext" + capitalizeFirst(basename);
    }

    /**
     * @return the given string with the first letter capitalized.
     */
    public final String capitalizeFirst(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
