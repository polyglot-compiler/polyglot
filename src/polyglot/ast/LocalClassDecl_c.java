package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.frontend.*;
import java.util.*;

/**
 * A local class declaration statement.  The node is just a wrapper around
 * a class declaration.
 */
public class LocalClassDecl_c extends Stmt_c implements LocalClassDecl
{
    protected ClassDecl decl;

    public LocalClassDecl_c(Ext ext, Position pos, ClassDecl decl) {
	super(ext, pos);
	this.decl = decl;
    }

    /** Get the class declaration. */
    public ClassDecl decl() {
	return this.decl;
    }

    /** Set the class declaration. */
    public LocalClassDecl decl(ClassDecl decl) {
	LocalClassDecl_c n = (LocalClassDecl_c) copy();
	n.decl = decl;
	return n;
    }

    /** Reconstruct the statement. */
    protected LocalClassDecl_c reconstruct(ClassDecl decl) {
        if (decl != this.decl) {
	    LocalClassDecl_c n = (LocalClassDecl_c) copy();
	    n.decl = decl;
	    return n;
	}

	return this;
    }

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
        ClassDecl decl = (ClassDecl) this.decl.visit(v);
        return reconstruct(decl);
    }

    public void enterScope(Context c) {
    }

    public void leaveScope(Context c) {
        // We should now be back in the scope of the enclosing block.
        // Add the type.
        c.addType(decl.type().toClass().toLocal());
    }

    public Node disambiguateOverride_(AmbiguityRemover ar) throws SemanticException {
        if (ar.kind() == AmbiguityRemover.SUPER) {
            return this;
        }

        if (ar.kind() == AmbiguityRemover.SIGNATURES) {
            return this;
        }

        enterScope(ar.context());

        ClassDecl d = (ClassDecl) ar.job().spawn(ar.context(), decl,
                                                 Pass.CLEAN_SUPER,
                                                 Pass.ADD_MEMBERS_ALL);

        if (d == null) {
            throw new SemanticException(
                "Could not disambiguate local class \"" + decl.name() + "\".",
                position());
        }

        LocalClassDecl n = decl(d);

        n = (LocalClassDecl) n.visitChildren(ar);

        n.leaveScope(ar.context());

        return n;
    }

    public String toString() {
	return decl.toString();
    }

    /** Write the statement to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        enterScope(tr.context());
        translateBlock(decl, w, tr);
	w.write(";");
        leaveScope(tr.context());
    }
}
