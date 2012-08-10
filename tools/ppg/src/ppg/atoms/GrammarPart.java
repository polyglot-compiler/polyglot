package ppg.atoms;

import ppg.parse.*;

public abstract class GrammarPart implements Unparse {
    @Override
    public abstract Object clone();
}
