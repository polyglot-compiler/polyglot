package polyglot.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

/**
 * A <code>TypeNode</code> represents the syntactic representation of a
 * <code>Type</code> within the abstract syntax tree.
 */
public class ArrayTypeNode_c extends TypeNode_c implements ArrayTypeNode
{
    protected TypeNode base;

    public ArrayTypeNode_c(Position pos, TypeNode base) {
	super(pos);
	this.base = base;
    }

    public TypeNode base() {
        return base;
    }

    public ArrayTypeNode base(TypeNode base) {
        ArrayTypeNode_c n = (ArrayTypeNode_c) copy();
	n.base = base;
	return n;
    }

    protected ArrayTypeNode_c reconstruct(TypeNode base) {
        if (base != this.base) {
	    ArrayTypeNode_c n = (ArrayTypeNode_c) copy();
	    n.base = base;
	    return n;
	}

	return this;
    }
    
    public boolean isDisambiguated() {
        return false;
    }
    
    public Node visitChildren(NodeVisitor v) {
        TypeNode base = (TypeNode) visitChild(this.base, v);
	return reconstruct(base);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
	TypeSystem ts = tb.typeSystem();
        return type(ts.arrayOf(position(), base.type()));
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	TypeSystem ts = ar.typeSystem();
	NodeFactory nf = ar.nodeFactory();

        if (! base.isDisambiguated()) {
            return this;
        }

        Type baseType = base.type();

        if (! baseType.isCanonical()) {
            return this;
	}

        return nf.CanonicalTypeNode(position(),
		                    ts.arrayOf(position(), baseType));
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot type check ambiguous node " + this + ".");
    }

    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot exception check ambiguous node " + this + ".");
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        print(base, w, tr);
        w.write("[]");
    }

    public void translate(CodeWriter w, Translator tr) {
    	  if (tr.job().extensionInfo().getOptions().output_ambiguous_nodes) {
    		  super.translate(w, tr);
    		  return;
    	  }
      throw new InternalCompilerError(position(),
                                      "Cannot translate ambiguous node "
                                      + this + ".");
    }

    public String toString() {
        return base.toString() + "[]";
    }
}
