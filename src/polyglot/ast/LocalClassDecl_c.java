package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * A <code>ClassDecl</code> is the definition of a class, abstract class,
 * or interface. It may be a public or other top-level class, or an inner
 * named class, or an anonymous class.
 */
public class LocalClassDecl_c extends Stmt_c implements LocalClassDecl
{
    protected ClassDecl decl;

    public LocalClassDecl_c(Ext ext, Position pos, ClassDecl decl) {
	super(ext, pos);
	this.decl = decl;
    }

    public ClassDecl decl() {
	return this.decl;
    }

    public LocalClassDecl decl(ClassDecl decl) {
	LocalClassDecl_c n = (LocalClassDecl_c) copy();
	n.decl = decl;
	return n;
    }

    protected LocalClassDecl_c reconstruct(ClassDecl decl) {
        if (decl != this.decl) {
	    LocalClassDecl_c n = (LocalClassDecl_c) copy();
	    n.decl = decl;
	    return n;
	}

	return this;
    }

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

    public String toString() {
	return decl.toString();
    }

    public void translate_(CodeWriter w, Translator tr) {
        enterScope(tr.context());
        translateBlock(decl, w, tr);
	w.write(";");
        leaveScope(tr.context());
    }
}
