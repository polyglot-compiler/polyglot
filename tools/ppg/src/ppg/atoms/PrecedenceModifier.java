package ppg.atoms;

import ppg.util.*;

public class PrecedenceModifier extends GrammarPart implements Equatable {
    protected String terminalName;

    public String getTerminalName() {
        return terminalName;
    }

    public PrecedenceModifier(String terminalName) {
        this.terminalName = terminalName;
    }

    @Override
    public Object clone() {
        return new PrecedenceModifier(getTerminalName());
    }

    @Override
    public void unparse(CodeWriter cw) {
        cw.begin(0);
        cw.write("%prec ");
        cw.write(getTerminalName());
        cw.end();
    }

    @Override
    public String toString() {
        return "%prec " + getTerminalName();
    }
}
