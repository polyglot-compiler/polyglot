package efg.frontend;

import efg.ExtensionInfo;
import polyglot.frontend.AbstractPass;
import polyglot.frontend.goals.Goal;
import polyglot.util.ErrorQueue;

public class EfgInfoValidationPass extends AbstractPass {

    public EfgInfoValidationPass(Goal goal) {
        super(goal);
    }

    @Override
    public boolean run() {
        ErrorQueue eq = goal.job().compiler().errorQueue();
        int nErrorsBefore = eq.errorCount();
        ExtensionInfo.EFG_INFO.validate(eq);
        int nErrorsAfter = eq.errorCount();
        return nErrorsBefore == nErrorsAfter;
    }

}
