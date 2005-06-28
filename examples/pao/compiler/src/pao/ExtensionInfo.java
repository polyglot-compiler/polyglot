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

    protected List compileGoalList(Job job) {
        List oldGoals = super.compileGoalList(job);
        ArrayList newGoals = new ArrayList(oldGoals.size() + 1);
        
        for (Iterator i = oldGoals.iterator(); i.hasNext(); ) {
            Goal g = (Goal) i.next();
            if (g instanceof Serialized) {
                newGoals.add(new VisitorGoal(job, new PaoBoxer(job, ts, nf)));
            }
            newGoals.add(g);
        }
        
        return newGoals;
    }

    static {
        // Make sure the class Topics is loaded.
        new Topics(); 
    }
}
