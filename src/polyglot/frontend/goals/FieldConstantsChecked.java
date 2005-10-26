/*
 * MembersAdded.java
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.CheckFieldConstantsPass;
import polyglot.frontend.passes.ConstantCheckPass;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;
import polyglot.visit.ConstantChecker;
import polyglot.visit.TypeBuilder;

/**
 * Comment for <code>MembersAdded</code>
 *
 * @author nystrom
 */
public class FieldConstantsChecked extends AbstractGoal {
    public static Goal create(Scheduler scheduler, FieldInstance fi) {
        return scheduler.internGoal(new FieldConstantsChecked(fi));
    }

    FieldInstance vi;
    ParsedClassType ct;
    
    protected FieldConstantsChecked(FieldInstance fi) {
        super(null);
        this.vi = fi;
        
        ParsedClassType ct = (ParsedClassType) findContainer();
        if (ct != null) {
            this.job = ct.job();
        }
        this.ct = ct;
        
        if (this.job == null && ! fi.constantValueSet()) {
            throw new InternalCompilerError(this + " is unreachable.");
        }
    }
    
    public ParsedClassType container() {
        return ct;
    }
    
    protected ParsedClassType findContainer() {
        if (vi.orig().container() instanceof ParsedClassType) {
            return (ParsedClassType) vi.orig().container();
        }
        return null;
    }

    public Pass createPass(ExtensionInfo extInfo) {
        return new CheckFieldConstantsPass(extInfo.scheduler(), this);
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        if (ct != null) {
            l.add(scheduler.SignaturesResolved(ct));
        }
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    public Collection corequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        if (ct != null && ct.job() != null) {
            l.add(scheduler.ConstantsChecked(ct.job()));
        }
        l.addAll(super.corequisiteGoals(scheduler));
        return l;
    }
    
    public FieldInstance var() {
        return vi;
    }
    
    public int hashCode() {
        return vi.hashCode() + super.hashCode();
    }
    
    public boolean equals(Object o) {
        return o instanceof FieldConstantsChecked && ((FieldConstantsChecked) o).vi.equals(vi) && super.equals(o);
    }
    
    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName()) + "(" + vi + ")";
    }
}
