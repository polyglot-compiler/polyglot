package jltools.util.jlgen.test.multi;

public class ParenExpr
{
	Expr expr;
	
	public ParenExpr (Expr e) {
		expr = e;
	}
	
	public String toString() {
		return "( " + expr.toString() + " )";
	}

}
