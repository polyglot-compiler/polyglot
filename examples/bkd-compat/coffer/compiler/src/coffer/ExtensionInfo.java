/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
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
import coffer.ast.CofferNodeFactory_c;
import coffer.parse.Grm;
import coffer.parse.Lexer_c;
import coffer.types.CofferTypeSystem_c;
import coffer.visit.KeyChecker;

/**
 * Extension information for coffer extension.
 */
public class ExtensionInfo extends polyglot.ext.param.ExtensionInfo {
    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
        return "cof";
    }

    @Override
    public String compilerName() {
        return "cofferc";
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
        return new CofferNodeFactory_c();
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new CofferTypeSystem_c();
    }

    @Override
    public Scheduler createScheduler() {
        return new CofferScheduler(this);
    }

    static class CofferScheduler extends JLScheduler {
        CofferScheduler(ExtensionInfo extInfo) {
            super(extInfo);
        }

        public Goal KeyCheck(final Job job) {
            TypeSystem ts = job.extensionInfo().typeSystem();
            NodeFactory nf = job.extensionInfo().nodeFactory();

            Goal g =
                    internGoal(new VisitorGoal(job, new KeyChecker(job, ts, nf)) {
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
                    l.add(KeyCheck(job));
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
