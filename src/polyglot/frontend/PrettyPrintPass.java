package polyglot.frontend;

import polyglot.ast.Node;
import polyglot.frontend.goals.Goal;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

/** An output pass generates output code from the processed AST. */
public class PrettyPrintPass extends AbstractPass
{
    protected PrettyPrinter pp;
    protected CodeWriter w;

    /**
     * Create a PrettyPrinter.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public PrettyPrintPass(Goal goal, CodeWriter w, PrettyPrinter pp) {
	super(goal);
        this.pp = pp;
        this.w = w;
    }

    public boolean run() {
        Node ast = goal.job().ast();

        if (ast == null) {
            w.write("<<<< null AST >>>>");
        }
        else {
            pp.printAst(ast, w);
        }

        return true;
    }
}
