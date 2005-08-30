package polyglot.ext.pao;

import java.io.Reader;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.ext.pao.ast.PaoNodeFactory_c;
import polyglot.ext.pao.parse.Grm;
import polyglot.ext.pao.parse.Lexer_c;
import polyglot.ext.pao.types.PaoTypeSystem_c;
import polyglot.ext.pao.visit.PaoBoxer;
import polyglot.ext.jl.JLScheduler;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.Serialized;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;

/**
 * Extension information for the PAO extension. This class specifies the
 * appropriate parser, <code>NodeFactory</code> and <code>TypeSystem</code>
 * to use, as well as inserting a new pass: <code>PaoBoxer</code>.
 * 
 * @see polyglot.ext.pao.visit.PaoBoxer
 * @see polyglot.ext.pao.ast.PaoNodeFactory_c 
 * @see polyglot.ext.pao.types.PaoTypeSystem 
 * @see polyglot.ext.pao.types.PaoTypeSystem_c 
 */
public class ExtensionInfo extends polyglot.ext.jl.ExtensionInfo {
    public String defaultFileExtension() {
        return "pao";
    }

    public String compilerName() {
        return "paoc";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source, eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new PaoNodeFactory_c();
    }
    protected TypeSystem createTypeSystem() {
        return new PaoTypeSystem_c();
    }

    public Scheduler createScheduler() {
        return new PAOScheduler(this);
    }

    static class PAOScheduler extends JLScheduler {
        PAOScheduler(ExtensionInfo extInfo) {
            super(extInfo);
        }

        public Goal Rewrite(final Job job) { 
            TypeSystem ts = job.extensionInfo().typeSystem();
            NodeFactory nf = job.extensionInfo().nodeFactory();

            Goal g = internGoal(new VisitorGoal(job, new PaoBoxer(job, ts, nf)) {
                public Collection prerequisiteGoals(Scheduler scheduler) {
                    List l = new ArrayList();
                    l.addAll(super.prerequisiteGoals(scheduler));
                    l.add(scheduler.TypeChecked(job));
                    l.add(scheduler.ConstantsChecked(job));
                    l.add(scheduler.ReachabilityChecked(job));
                    l.add(scheduler.ExceptionsChecked(job));
                    l.add(scheduler.ExitPathsChecked(job));
                    l.add(scheduler.InitializationsChecked(job));
                    l.add(scheduler.ConstructorCallsChecked(job));
                    l.add(scheduler.ForwardReferencesChecked(job));
                    return l;
                }
            });
            return g;
        }

        public Goal Serialized(final Job job) { 
            Goal g = internGoal(new Serialized(job) {
                public Collection prerequisiteGoals(Scheduler scheduler) {
                    List l = new ArrayList();
                    l.addAll(super.prerequisiteGoals(scheduler));
                    l.add(Rewrite(job));
                    return l;
                }
            });
            return g;
        }
    }

    static {
        // Make sure the class Topics is loaded.
        new Topics(); 
    }
}
