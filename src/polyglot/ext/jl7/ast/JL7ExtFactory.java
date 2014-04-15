package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ext.jl5.ast.JL5ExtFactory;

public interface JL7ExtFactory extends JL5ExtFactory {

    Ext extMultiCatch();

}
