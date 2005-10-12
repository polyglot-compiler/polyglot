/*
 * ParseFile.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.frontend.*;



public class Parsed extends SourceFileGoal {
    public static Goal create(Scheduler scheduler, Job job) {
        return scheduler.internGoal(new Parsed(job));
    }

    protected Parsed(Job job) { super(job); }
    
    public Pass createPass(ExtensionInfo extInfo) {
        return new ParserPass(extInfo.compiler(), this);
    }
}
