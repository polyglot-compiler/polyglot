/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import coffer.types.*;
import coffer.extension.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import java.util.*;

/** An implementation of the <code>CofferMethodDecl</code> interface.
 * <code>ConstructorDecl</code> is extended with pre- and post-conditions.
 */
public class CofferMethodDecl_c extends MethodDecl_c implements CofferMethodDecl
{
    protected KeySetNode entryKeys;
    protected KeySetNode returnKeys;
    protected List throwConstraints;

    public CofferMethodDecl_c(Position pos, Flags flags, TypeNode returnType,
	    Id name, List formals, KeySetNode entryKeys, KeySetNode returnKeys,
	    List throwConstraints, Block body) {
	super(pos, flags, returnType, name, formals, Collections.EMPTY_LIST, body);
	this.entryKeys = entryKeys;
	this.returnKeys = returnKeys;
	this.throwConstraints = TypedList.copyAndCheck(throwConstraints, 
		ThrowConstraintNode.class, true);
    }

    public KeySetNode entryKeys() {
	return this.entryKeys;
    }

    public CofferMethodDecl entryKeys(KeySetNode entryKeys) {
	CofferMethodDecl_c n = (CofferMethodDecl_c) copy();
	n.entryKeys = entryKeys;
	return n;
    }

    public KeySetNode returnKeys() {
	return this.returnKeys;
    }

    public CofferMethodDecl returnKeys(KeySetNode returnKeys) {
	CofferMethodDecl_c n = (CofferMethodDecl_c) copy();
	n.returnKeys = returnKeys;
	return n;
    }

    public List throwTypes() {
        return new CachingTransformingList(throwConstraints, new GetType());
    }

    public class GetType implements Transformation {
        public Object transform(Object o) {
            return ((ThrowConstraintNode) o).type();
        }
    }

    public MethodDecl throwTypes(List l) {
        throw new InternalCompilerError("unimplemented");
    }

    public List throwConstraints() {
	return this.throwConstraints;
    }

    public CofferMethodDecl throwConstraints(List throwConstraints) {
	CofferMethodDecl_c n = (CofferMethodDecl_c) copy();
	n.throwConstraints = TypedList.copyAndCheck(throwConstraints, ThrowConstraintNode.class, true);
	return n;
    }

    /*
    public Context enterScope(Context context) {
        CofferContext c = (CofferContext) super.enterScope(context);
        if (entryKeys != null) {
            c = (CofferContext) c.pushBlock();

            for (Iterator i = entryKeys.keys().iterator(); i.hasNext(); ) {
                Key key = (Key) i.next();
                c.addHeldKey(key);
            }
        }

        return c;
    }
    */

    protected CofferMethodDecl_c reconstruct(TypeNode returnType, Id name, List formals, KeySetNode entryKeys, KeySetNode returnKeys, List throwConstraints, Block body) {
      if (entryKeys != this.entryKeys || returnKeys != this.returnKeys || ! CollectionUtil.equals(throwConstraints, this.throwConstraints)) {
          CofferMethodDecl_c n = (CofferMethodDecl_c) copy();
          n.entryKeys = entryKeys;
          n.returnKeys = returnKeys;
          n.throwConstraints = TypedList.copyAndCheck(throwConstraints, ThrowConstraintNode.class, true);
          return (CofferMethodDecl_c) n.reconstruct(returnType, name, formals, Collections.EMPTY_LIST, body);
      }

      return (CofferMethodDecl_c) super.reconstruct(returnType, name, formals, Collections.EMPTY_LIST, body);
    }

    public Node visitChildren(NodeVisitor v) {
        TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
        Id name = (Id) visitChild(this.name, v);
        List formals = visitList(this.formals, v);
	KeySetNode entryKeys = (KeySetNode) visitChild(this.entryKeys, v);
	KeySetNode returnKeys = (KeySetNode) visitChild(this.returnKeys, v);
	List throwConstraints = visitList(this.throwConstraints, v);
	Block body = (Block) visitChild(this.body, v);
	return reconstruct(returnType, name, formals, entryKeys, returnKeys, throwConstraints, body);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        CofferNodeFactory nf = (CofferNodeFactory) tb.nodeFactory();

        CofferMethodDecl n = (CofferMethodDecl) super.buildTypes(tb);

        CofferMethodInstance mi = (CofferMethodInstance) n.methodInstance();

        if (n.entryKeys() == null) {
            n = n.entryKeys(nf.CanonicalKeySetNode(n.position(),
                                                   mi.entryKeys()));
        }

        if (n.returnKeys() == null) {
            n = n.returnKeys(nf.CanonicalKeySetNode(n.position(),
                                                    mi.returnKeys()));
        }

        List l = new LinkedList();
        boolean changed = false;

        for (Iterator i = n.throwConstraints().iterator(); i.hasNext(); ) {
            ThrowConstraintNode cn = (ThrowConstraintNode) i.next();
            if (cn.keys() == null) {
                cn = cn.keys(n.entryKeys());
                changed = true;
            }
            l.add(cn);
        }

        if (changed) {
            n = n.throwConstraints(l);
        }

        CofferTypeSystem vts = (CofferTypeSystem) tb.typeSystem();
        ClassType ct = tb.currentClass();
        
        KeySet entryKeys;
        KeySet returnKeys;
        
        if (n.entryKeys() == null) {
            entryKeys = vts.emptyKeySet(position());
        }
        else {
            entryKeys = n.entryKeys().keys();
        }
        
        if (n.returnKeys() == null) {
            returnKeys = vts.emptyKeySet(position());
        }
        else {
            returnKeys = n.returnKeys().keys();
        }
        
        mi.setEntryKeys(entryKeys);
        mi.setReturnKeys(returnKeys);

        return n;
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        CofferClassType ct = (CofferClassType) tc.context().currentClass();

        CofferMethodInstance mi = (CofferMethodInstance) this.methodInstance();

        if (ct.key() != null) {
            if (! mi.entryKeys().contains(ct.key()) &&
                mi.returnKeys().contains(ct.key())) {
                throw new SemanticException("Method cannot add key \"" +
                                            ct.key() + "\" (associated with " +
                                            "this).", position());
            }
        }

        return super.typeCheck(tc);
    }
    
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (this.mi.isCanonical()) {
            return this;
        }
        
        CofferMethodDecl_c n = (CofferMethodDecl_c) super.disambiguate(ar);
        
        CofferTypeSystem vts = (CofferTypeSystem) ar.typeSystem();
        ClassType ct = ar.context().currentClass();
        
        KeySet entryKeys;
        KeySet returnKeys;
        
        if (n.entryKeys == null) {
            entryKeys = vts.emptyKeySet(position());
        }
        else {
            entryKeys = n.entryKeys.keys();
        }
        
        if (n.returnKeys == null) {
            returnKeys = vts.emptyKeySet(position());
        }
        else {
            returnKeys = n.returnKeys.keys();
        }
        
        CofferMethodInstance mi = (CofferMethodInstance) n.mi;
        mi.setEntryKeys(entryKeys);
        mi.setReturnKeys(returnKeys);
        
        List throwConstraints = new ArrayList(n.throwConstraints.size());
        for (Iterator i = n.throwConstraints.iterator(); i.hasNext(); ) {
            ThrowConstraintNode cn = (ThrowConstraintNode) i.next();

            if (! cn.isDisambiguated()) {
                return this;
            }
            
            if (cn.constraint().keys() != null) {
                throwConstraints.add(cn.constraint());
            }
            else {
                ThrowConstraint c = (ThrowConstraint) cn.constraint().copy();
                c.setKeys(entryKeys);
                throwConstraints.add(c);
            }
        }
        
        mi.setThrowConstraints(throwConstraints);
        
        return n;
    }

    /** Write the method to an output file. */
    public void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr) {
	w.begin(0);
	w.write(flags.translate());
	print(returnType, w, tr);
	w.write(" ");
        print(name, w, tr);
	w.write("(");

	w.begin(0);

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    print(f, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0, " ");
	    }
	}

	w.end();
	w.write(")");
    
	if (! (tr instanceof Translator)) {
	    if (entryKeys != null) {
	        w.allowBreak(6, " ");
	        print(entryKeys, w, tr);
	    }
	    if (returnKeys != null) {
	        w.write(" -> ");
	        print(returnKeys, w, tr);
	    }
	}

	if (! throwConstraints.isEmpty()) {
	    w.allowBreak(6);
	    w.write("throws ");

	    for (Iterator i = throwConstraints.iterator(); i.hasNext(); ) {
	        ThrowConstraintNode cn = (ThrowConstraintNode) i.next();
		print(cn, w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
	}

	w.end();
    }
}
