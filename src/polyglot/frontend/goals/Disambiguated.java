/*
 * TypeChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.passes.DisambiguatorPass;
import polyglot.frontend.passes.TypeCheckPass;
import polyglot.main.Report;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.*;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;


public class Disambiguated extends SourceFileGoal {
    public Disambiguated(Job job) {
        super(job);
    }

    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new VisitorPass(this, new AmbiguityRemover(job(), ts, nf));
    }

    private static final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend });
}
