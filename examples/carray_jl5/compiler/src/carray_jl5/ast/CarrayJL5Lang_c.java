package carray_jl5.ast;

import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyglot.ext.jl5.ast.J5Lang_c;

public class CarrayJL5Lang_c extends J5Lang_c {
    public static final CarrayJL5Lang_c instance = new CarrayJL5Lang_c();

    protected CarrayJL5Lang_c() {
    }

    protected static CarrayJL5Ext carrayjl5Ext(Node n) {
        return CarrayJL5Ext.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return carrayjl5Ext(n);
    }
}
