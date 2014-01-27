package carray.ast;

import polyglot.ast.JLang_c;
import polyglot.ast.Node;
import polyglot.ast.NodeOps;

public class CarrayLang_c extends JLang_c {
    public static final CarrayLang_c instance = new CarrayLang_c();

    protected CarrayLang_c() {
    }

    protected static CarrayExt carrayExt(Node n) {
        return CarrayExt.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return carrayExt(n);
    }
}
