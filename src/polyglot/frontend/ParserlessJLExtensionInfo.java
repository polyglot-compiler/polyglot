package polyglot.frontend;

import java.io.Reader;

import polyglot.ast.NodeFactory;
import polyglot.ast.NodeFactory_c;
import polyglot.frontend.goals.Goal;
import polyglot.main.Version;
import polyglot.types.*;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;

/** This class implements most of the <code>ExtensionInfo</code> for the Java language.
 * It does not include a parser, however.  EPL-licensed extensions should extend this class
 * rather than JLExtensionInfo since they should not use the CUP-based grammar.
 * @author nystrom
 *
 */
public abstract class ParserlessJLExtensionInfo extends AbstractExtensionInfo {

    protected void initTypeSystem() {
        try {
            LoadedClassResolver lr;
            lr = new SourceClassResolver(compiler, this, getOptions().constructFullClasspath(),
                                         compiler.loader(), true,
                                         getOptions().compile_command_line_only,
                                         getOptions().ignore_mod_times);

            TopLevelResolver r = lr;

            // Resolver to handle lookups of member classes.
            if (TypeSystem.SERIALIZE_MEMBERS_WITH_CONTAINER) {
                MemberClassResolver mcr = new MemberClassResolver(ts, lr, true);
                r = mcr;
            }

            ts.initialize(r, this);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(
                "Unable to initialize type system: " + e.getMessage());
        }
    }
    
    protected polyglot.frontend.Scheduler createScheduler() {
        return new JLScheduler(this);
    }

    public String defaultFileExtension() {
        return "jl";
    }

    public String compilerName() {
        return "jlc";
    }

    public Version version() {
        return new JLVersion();
    }

    /** Create the type system for this extension. */
    protected TypeSystem createTypeSystem() {
        return new TypeSystem_c();
    }

    /** Create the node factory for this extension. */
    protected NodeFactory createNodeFactory() {
        return new NodeFactory_c();
    }

    public JobExt jobExt() {
      return null;
    }

    /**
     * Return a parser for <code>source</code> using the given
     * <code>reader</code>.
     */
    public abstract Parser parser(Reader reader, FileSource source, ErrorQueue eq);
    
    /**
     * Return the <code>Goal</code> to compile the source file associated with
     * <code>job</code> to completion.
     */
    public Goal getCompileGoal(Job job) {
        return scheduler.CodeGenerated(job);
    }

    static { Topics t = new Topics(); }

}
