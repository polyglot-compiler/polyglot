package efg.frontend;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.goals.AbstractGoal;

public class EfgInfoValidationGoal extends AbstractGoal {

    protected EfgInfoValidationGoal(Job job) {
        super(job);
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        return new EfgInfoValidationPass(this);
    }

}
