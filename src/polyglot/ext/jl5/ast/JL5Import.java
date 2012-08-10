package polyglot.ext.jl5.ast;

import polyglot.ast.Import;

public interface JL5Import extends Import {

    public static final Kind SINGLE_STATIC_MEMBER =
            new Kind("single-static-member");
    public static final Kind STATIC_ON_DEMAND = new Kind("static-on-demand");

}
