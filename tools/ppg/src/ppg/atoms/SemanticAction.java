package ppg.atoms;

import ppg.util.CodeWriter;

public class SemanticAction extends GrammarPart {
    private String action;

    public SemanticAction(String actionCode) {
        action = actionCode;
    }

    @Override
    public Object clone() {
        return new SemanticAction(action.toString());
    }

    @Override
    public void unparse(CodeWriter cw) {
        cw.begin(0);
        cw.write("{:");
        cw.allowBreak(-1);
        cw.write(action);
        cw.allowBreak(0);
        cw.write(":}");
        cw.end();
    }

    @Override
    public String toString() {
        return "{:" + action + ":}\n";
    }
}
