/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * TypeChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import java.util.*;
import java.util.Iterator;

import polyglot.ast.*;
import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.ConstantCheckPass;
import polyglot.main.Report;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.ConstantChecker;
import polyglot.visit.NodeVisitor;


public class ConstantsCheckedForFile extends SourceFileGoal {
    protected boolean reached;
    
    public ConstantsCheckedForFile(Job job) {
        super(job);
        this.reached = false;
    }

    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new ConstantCheckPass(this, new ConstantChecker(job(), ts, nf));
    }

    private static final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend });
}
