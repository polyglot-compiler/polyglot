/*
 * Created on Mar 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;

import java.util.*;

/**
 * @author xinqi
 *
 * This class translates inner classes to static nested classes with a field referring to the enclosing 
 * instance. It will also add "new" methods to the enclosing class corresponding to constructors of inner
 * classes.
 * 
 */
public class InnerTranslator extends NodeVisitor {
	TypeSystem ts;
	NodeFactory nf;
	Stack contextClass;
	
	public InnerTranslator(TypeSystem ts, NodeFactory nf) {
		this.ts = ts;
		this.nf = nf;
		contextClass = new Stack();
	}
	
	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) { 
			contextClass.push(((ClassDecl)n).type());
		}
		return this;
	}

	public Node leave(Node old, Node n, NodeVisitor v) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl)n;
			ParsedClassType ct = cd.type();
						
			// Do nothing if it is already a static class, or it is a toplevel class. 
			if (ct.flags().isStatic() || ct.isTopLevel()) return n;
			
			// Deal with ordinary inner classes.
			if (ct.container() instanceof ClassType) {
				Flags f = ct.flags().Static();
				List members = new LinkedList();
				
				// First add a field to enclosing instance
				FieldDecl fd = nf.FieldDecl(Position.compilerGenerated(), 
						                   Flags.PROTECTED, 
						                   nf.CanonicalTypeNode(Position.compilerGenerated(), ct.container()), 
										  outerThisFieldName(ct));
				FieldInstance fi = ts.fieldInstance(Position.compilerGenerated(), 
						                           ct, 
												  Flags.PROTECTED, 
												  ct.container(), 
												  outerThisFieldName(ct));
				fd = fd.fieldInstance(fi);
				members.add(fd);
				ct.addField(fi);
				
				ct.clearConstructors();
				
				for (Iterator it = cd.body().members().iterator(); it.hasNext(); ) {
					ClassMember m = (ClassMember)it.next();
					if (m instanceof ConstructorDecl) {
						ConstructorDecl cons = (ConstructorDecl)m;
						ConstructorInstance ci = cons.constructorInstance();
						ConstructorDecl newCons = updateConstructor(cd, ct, cons);
						newCons = newCons.constructorInstance(ci);
						members.add(newCons);
						ct.addConstructor(updateConstructorInst(ct, ci));
					}
					else {
					    members.add(m);
					}
				}
				
				ClassBody cb = cd.body();
				cb = cb.members(members);
				cd = cd.body(cb);
				cd = cd.type(ct);
				cd = cd.flags(cd.flags().Static());
			}
			
			contextClass.pop();
			return cd;
		}
		else if (n instanceof ConstructorCall) {
			ConstructorCall cc = (ConstructorCall)n;
			ConstructorInstance ci = cc.constructorInstance();
			ClassType ct = (ClassType)ci.container();
			if (ct.isInnerClass()) {
				ci = updateConstructorInst(ct, ci);
				List args = new ArrayList(cc.arguments().size() + 1);
				args.add(nf.Local(Position.compilerGenerated(), outerArgName(ct)));
				cc = (ConstructorCall)cc.arguments(args);
				cc = cc.constructorInstance(ci);
			}
			return cc;
		}
		else if (n instanceof New) {
			New newExpr = (New)n;
			// FIXME: what if it is an anonymous class?
			if (newExpr.qualifier() != null) {
				List args = new ArrayList(newExpr.arguments().size() + 1);
				args.add(newExpr.qualifier());
				args.addAll(newExpr.arguments());
				New nExpr = (New)newExpr.arguments(args);
				nExpr = nExpr.qualifier(null);
				nExpr = nExpr.constructorInstance(updateConstructorInst((ClassType)newExpr.type(), newExpr.constructorInstance()));
				return nExpr;
			}
			return newExpr;
		}
		else if (n instanceof Special) {
			Special s = (Special)n;
			if (s.kind() == Special.THIS && s.qualifier() != null) {
				ClassType tOuter = (ClassType)s.qualifier().type();
				ClassType tThis = (ClassType)contextClass.peek();
				s = s.qualifier(null);
				Expr t = s;
				while (!ts.equals(tOuter, tThis)) {
					t = nf.Field(Position.compilerGenerated(), t, outerThisFieldName(tThis));
					tThis = (ClassType)tThis.container();
				}
				return t;
			}
			return s;
		}
		
		return n;
    }
	
	// Return the name of the "outer this" field in ct. 
	protected String outerThisFieldName(ClassType ct) { 
		return "jl$outer$this";
	}
	
	protected String outerArgName(ClassType ct) {
		return "jl$outer";
	}
	
	protected ConstructorInstance updateConstructorInst(ClassType ct, ConstructorInstance ci) {
		List ftypes = new ArrayList(ci.formalTypes().size() + 1);
		ftypes.add(ct.container());
		ftypes.addAll(ci.formalTypes());
		ConstructorInstance ci2 = (ConstructorInstance) ci.copy();
		ci2.setFormalTypes(ftypes);
		return ci2;
	}
	
	// Add a new argument to all the constructors
	protected ConstructorDecl updateConstructor(ClassDecl cd, ClassType ct, ConstructorDecl cons) {
		String fn = outerArgName(ct);
		Formal formal = nf.Formal(Position.compilerGenerated(), 
				                  Flags.NONE, 
								  nf.CanonicalTypeNode(Position.compilerGenerated(), ct.container()), 
								  fn);
		formal = formal.localInstance(ts.localInstance(Position.compilerGenerated(), 
													   formal.flags(), 
													   formal.type().type(), 
													   formal.name()));
		List formals = new ArrayList(cons.formals().size() + 1);
		formals.add(formal);
		formals.addAll(cons.formals());
		
		List oldStmts = cons.body().statements();
		List stmts = new ArrayList(oldStmts.size() + 1);
		Iterator it = oldStmts.iterator();
		
		Stmt fAssign = nf.Eval(Position.compilerGenerated(), 
							   nf.FieldAssign(Position.compilerGenerated(), 
							   				  nf.Field(Position.compilerGenerated(), 
							   				  		   nf.This(Position.compilerGenerated()), 
													   outerThisFieldName(ct)), 
													   Assign.ASSIGN, 
													   nf.Local(Position.compilerGenerated(), fn)));
		
		// Check whether the first statement is a constructor call.
		if (it.hasNext()) {
			Stmt s = (Stmt)it.next();
			if (s instanceof ConstructorCall) {
				stmts.add(s);
				// If it calls another constructor in the same class, we don't need to initialize the field.
				if (((ConstructorCall)s).kind() != ConstructorCall.THIS) {
					stmts.add(fAssign);
				}
			}
			else {
				stmts.add(fAssign);
				stmts.add(s);
			}
		}
		while (it.hasNext()) {
			stmts.add(it.next());
		}
		
		Block b = nf.Block(Position.compilerGenerated(), stmts);
		return nf.ConstructorDecl(Position.compilerGenerated(), cons.flags(), cons.name(), formals, cons.throwTypes(), b);
	}

}
