/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * DisambiguationPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.VisitorPass;
import polyglot.frontend.goals.*;
import polyglot.visit.ConstantChecker;
import polyglot.visit.TypeChecker;

/**
 * Comment for <code>DisambiguationPass</code>
 *
 * @author nystrom
 */
public class ConstantCheckPass extends VisitorPass {
    public ConstantCheckPass(ConstantsCheckedForFile goal, ConstantChecker v) {
        this((Goal) goal, v);
    }
    public ConstantCheckPass(FieldConstantsChecked goal, ConstantChecker v) {
        this((Goal) goal, v);
    }
    protected ConstantCheckPass(Goal goal, ConstantChecker v) {
        super(goal, v);
    }
}
