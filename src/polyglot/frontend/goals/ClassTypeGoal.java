/*
 * SourceFileGoal.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.goals;

import polyglot.types.ParsedClassType;
import polyglot.util.StringUtil;

/**
 * Comment for <code>SourceFileGoal</code>
 *
 * @author nystrom
 */
public abstract class ClassTypeGoal extends AbstractGoal {
    protected ParsedClassType ct;

    protected ClassTypeGoal(ParsedClassType ct) {
        super(ct.job());
        this.ct = ct;
    }
    
    protected ClassTypeGoal(ParsedClassType ct, String name) {
        super(ct.job(), name);
        this.ct = ct;
    }
    
    public ParsedClassType type() {
        return ct;
    }
    
    public int hashCode() {
        return ct.hashCode() + super.hashCode();
    }
    
    public boolean equals(Object o) {
        return o instanceof ClassTypeGoal && ((ClassTypeGoal) o).ct == ct && super.equals(o);
    }
    
    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName()) + "(" + ct + ")";
    }
}
