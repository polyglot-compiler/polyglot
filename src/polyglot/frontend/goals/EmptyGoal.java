package polyglot.frontend.goals;

import polyglot.frontend.EmptyPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;

/**
 * An empty goal that runs an empty pass.
 */
public class EmptyGoal extends AbstractGoal {

  public EmptyGoal(Job job) {
    super(job);
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.frontend.goals.AbstractGoal#createPass(polyglot.frontend.ExtensionInfo)
   */
  public Pass createPass(ExtensionInfo extInfo) {
    return new EmptyPass(this);
  }

}
