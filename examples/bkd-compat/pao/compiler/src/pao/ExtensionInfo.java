/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import pao.ast.PaoNodeFactory_c;
import pao.parse.Grm;
import pao.parse.Lexer_c;
import pao.types.PaoTypeSystem_c;
import pao.visit.PaoBoxer;
import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.JLScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.Scheduler;
import polyglot.frontend.Source;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.Serialized;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;

/**
 * Extension information for the PAO extension. This class specifies the
 * appropriate parser, <code>NodeFactory</code> and <code>TypeSystem</code>
 * to use, as well as inserting a new pass: <code>PaoBoxer</code>.
 * 
 * @see pao.visit.PaoBoxer
 * @see pao.ast.PaoNodeFactory_c 
 * @see pao.types.PaoTypeSystem 
 * @see pao.types.PaoTypeSystem_c 
 */
public class ExtensionInfo extends JLExtensionInfo {
    @Override
    public String defaultFileExtension() {
        return "pao";
    }

    @Override
    public String compilerName() {
        return "paoc";
    }

    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source, eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    @Override
    public Set<String> keywords() {
	return new Lexer_c((Reader) null).keywords();
    }

    @Override
    protected NodeFactory createNodeFactory() {
        return new PaoNodeFactory_c();
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new PaoTypeSystem_c();
    }

    @Override
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

            Goal g =
                    internGoal(new VisitorGoal(job, new PaoBoxer(job, ts, nf)) {
                        @Override
                        public Collection<Goal> prerequisiteGoals(
                                Scheduler scheduler) {
                            List<Goal> l = new ArrayList<Goal>();
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

        @Override
        public Goal Serialized(final Job job) {
            Goal g = internGoal(new Serialized(job) {
                @Override
                public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
                    List<Goal> l = new ArrayList<Goal>();
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
