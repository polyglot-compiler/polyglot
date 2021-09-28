package polyglot.ext.jl8.ast;

import polyglot.ast.ProcedureCall;
import polyglot.ast.ProcedureCallOps;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

public abstract class JL8ProcedureCallExt extends JL8Ext implements ProcedureCallOps {
    @Override
    public ProcedureCall node() {
        return (ProcedureCall) super.node();
    }

    @Override
    public void printArgs(CodeWriter w, PrettyPrinter tr) {
        superLang().printArgs(this.node(), w, tr);
    }
}
