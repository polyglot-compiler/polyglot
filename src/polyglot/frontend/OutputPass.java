package polyglot.frontend;

import polyglot.ast.Node;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.SourceFileGoal;
import polyglot.util.InternalCompilerError;
import polyglot.visit.Translator;

/** An output pass generates output code from the processed AST. */
public class OutputPass extends AbstractPass
{
    protected Translator translator;

    /**
     * Create a Translator.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public OutputPass(Goal goal, Translator translator) {
	super(goal);
        this.translator = translator;
    }

    public boolean run() {
        Node ast = goal.job().ast();

        if (ast == null) {
            throw new InternalCompilerError("AST is null");
        }

        if (translator.translate(ast)) {
            if (goal instanceof SourceFileGoal) {
                ((SourceFileGoal) goal).markReached();
            }
            return true;
        }
        
        return false;
    }
}
