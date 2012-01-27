package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ConstructorCall_c;
import polyglot.ast.Expr;
import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;

public class JL5ConstructorCall_c extends ConstructorCall_c {

	public JL5ConstructorCall_c(Position pos, Kind kind, Expr qualifier,
			List arguments) {
		super(pos, kind, qualifier, arguments);
	}

	@Override
	public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
		ClassType ct = ar.context().currentClass();
		if (ct != null && JL5Flags.isEnum(ct.flags())) {
			if (this.arguments().isEmpty()) {
				// this is an enum decl, so we need to replace a call to the default
				// constructor with a call to java.lang.Enum.Enum(String, int)
				List args = new ArrayList(2);// XXX the right thing to do is change the type of java.lang.Enum instead of adding these dummy params
				args.add(ar.nodeFactory().NullLit(Position.COMPILER_GENERATED));
				args.add(ar.nodeFactory().IntLit(Position.COMPILER_GENERATED, IntLit.INT, 0));
				JL5ConstructorCall_c n = (JL5ConstructorCall_c) this.arguments(args);
				return n.disambiguate(ar);				
			}
		}
		return super.disambiguate(ar);
	}
	
	
}
