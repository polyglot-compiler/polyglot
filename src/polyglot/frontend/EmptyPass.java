package polyglot.frontend;

import polyglot.frontend.goals.Goal;

/**
 * An <code>EmptyPass</code> does nothing.
 */
public class EmptyPass extends AbstractPass
{
    public EmptyPass(Goal goal) {
      	super(goal);
    }

    public boolean run() {
        return true;
    }
}
