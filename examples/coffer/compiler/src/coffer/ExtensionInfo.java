/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer;

import coffer.parse.Lexer_c;
import coffer.parse.Grm;
import coffer.ast.*;
import coffer.types.*;
import coffer.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.Serialized;
import polyglot.main.*;
import polyglot.lex.Lexer;

import java.util.*;
import java.io.*;

/**
 * Extension information for coffer extension.
 */
public class ExtensionInfo extends polyglot.ext.param.ExtensionInfo {
    static {
        // force Topics to load
        Topics t = new Topics();
    }

    public String defaultFileExtension() {
        return "cof";
    }

    public String compilerName() {
        return "cofferc";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source, eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new CofferNodeFactory_c();
    }
    protected TypeSystem createTypeSystem() {
        return new CofferTypeSystem_c();
    }

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

            Goal g = internGoal(new VisitorGoal(job, new KeyChecker(job, ts, nf)) {
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
