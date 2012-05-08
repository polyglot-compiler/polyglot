package polyglot.ext.jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public interface JL5ExtFactory extends ExtFactory {

	Ext extEnumDecl();

	Ext extExtendedFor();

	Ext extEnumConstantDecl();

	Ext extEnumConstant();

	Ext extParamTypeNode();

}
