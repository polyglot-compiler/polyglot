/*
 * MembersAdded.java
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.CheckFieldConstantsPass;
import polyglot.frontend.passes.ConstantCheckPass;
import polyglot.types.*;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
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
    FieldInstance vi;
    ParsedClassType ct;
    
    public FieldConstantsChecked(FieldInstance vi) {
        super(null);
        this.vi = vi;
        
        ParsedClassType ct = (ParsedClassType) findContainer();
        this.job = ct.job();
        this.ct = ct;
        
        if (job == null && ! vi.constantValueSet()) {
            throw new InternalCompilerError(this + " is unreachable.");
        }
    }
    
    public ParsedClassType container() {
        return ct;
    }
    
    protected ParsedClassType findContainer() {
        if (vi.container() instanceof ParsedClassType) {
            return (ParsedClassType) vi.container();
        }
        return null;
    }

    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
            TypeSystem ts = extInfo.typeSystem();
            NodeFactory nf = extInfo.nodeFactory();
            return new ConstantCheckPass(this, new ConstantChecker(job(), ts, nf));
        }
        return new CheckFieldConstantsPass(extInfo.scheduler(), this);
    }
    
    public int distanceFromGoal() {
        return vi.constantValueSet() ? 0 : 1;
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
