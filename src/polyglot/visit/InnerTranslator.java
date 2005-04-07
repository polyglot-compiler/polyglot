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
	
	class ClassInfo {
		ParsedClassType ct;
		Map localNameCount; // Count how many local/anonymous classes with a particular name have appeared.
		List newMemberClasses; // New member class declarations converted from local/anonymous classes. 
		List newConsFormals; // The list of added formals to constructors. 
							 // The first one should be the reference to the outer class instance, 
							 // and the remaining ones are the final locals.
		List innerClassInfo; // List of inner class info. 
		boolean hasOuterField;
		CodeInfo insideCode; // For local/anonymous classes, this is the code where the declaration is.
		
		public ClassInfo(ParsedClassType ct) {
			this.ct = ct;
			localNameCount = new HashMap();
			newMemberClasses = new LinkedList();
			newConsFormals = new LinkedList();
			innerClassInfo = new LinkedList();
			hasOuterField = false;
			insideCode = null;
		}
		
		public String toString() {
			return ct.toString();
		}
		
		// For anonymous classes, name would be "".
		public int addLocalClassName(String name) { 
			if (localNameCount.containsKey(name)) {
				Integer i = (Integer)localNameCount.get(name);
				localNameCount.put(name, new Integer(i.intValue() + 1));
				return i.intValue();
			}
			else {
				localNameCount.put(name, new Integer(1));
				return 0;
			}
		}
		
		// Generate new names for local/anonymous classes.
		public String localClassName(String name, int nameCount) {
			String thisName = ct.fullName();
			return namePrefix() + thisName + "$" + nameCount + name; 
		}
						
		public ParsedClassType classType() {
			return ct;
		}
		
		public void addConsFormal(Formal f) {
			newConsFormals.add(f);
			FieldInstance fi = ts.fieldInstance(Position.compilerGenerated(), 
					  						  ct, 
											  Flags.PROTECTED, 
											  f.type().type(), 
											  newFieldName(f.name()));
			ct.addField(fi);
		}
		
		public List newConsFormals() {
			return newConsFormals;
		}
		
		public List newMemberClasses() {
			return newMemberClasses;
		}
		
		public void addMemberClass(ClassDecl cd) {
			newMemberClasses.add(cd);
			ct.addMemberClass(cd.type());
		}
		
//		public ClassType memberClassNamed(String name) {
//			return cd.type().memberClassNamed(name);
//		}
		
		public void addInnerClassInfo(ClassInfo cinfo) {
			innerClassInfo.add(cinfo);
		}
		
		public ClassInfo findInnerClassInfo(ClassType ct) {
			for (Iterator it = innerClassInfo.iterator(); it.hasNext(); ) {
				ClassInfo cinfo = (ClassInfo)it.next();
				if (cinfo.classType() == ct) {
					return cinfo;
				}
			}
			return null;
		}
		
		public void hasOuterField(boolean b) {
			hasOuterField = b;
		}
		
		public boolean hasOuterField() {
			return hasOuterField;
		}
		
		public CodeInfo insideCode() {
			return insideCode;
		}
		
		public void insideCode(CodeInfo ci) {
			insideCode = ci;
		}
	}
	
	// Information about methods, constructors, and initializers.
	class CodeInfo {
		CodeInstance ci;
		List finalArgs; // the list of final arguments if any. 
		List localClassInfo; // List of local/anonymous class info.
		Stack blockFinals; // stack of lists of final variables defined in a block.
		
		public CodeInfo(CodeInstance ci) {
			this.ci = ci;
			finalArgs = new LinkedList();
			localClassInfo = new LinkedList();
			blockFinals = new Stack();
		}
		
		public String toString() {
			return ci.toString();
		}
		
		public void addFinalArg(LocalInstance li) {
			finalArgs.add(li);
		}
		
		public void pushBlock() {
			blockFinals.push(new LinkedList());
		}
		
		public void popBlock() {
			blockFinals.pop();
		}
		
		public void addFinalLocal(LocalInstance li) {
			List current = (List)blockFinals.peek();
			current.add(li);
		}
		
		public List finalList() {
			List result = new LinkedList();
			result.addAll(finalArgs);
			for (Iterator it = blockFinals.iterator(); it.hasNext(); ) {
				List l = (List)it.next();
				result.addAll(l);
			}
			return result;
		}
		
		public ClassInfo findLocalClassInfo(ClassType ct) {
			for (Iterator it = localClassInfo.iterator(); it.hasNext(); ) {
				ClassInfo cinfo = (ClassInfo)it.next();
				if (cinfo.classType() == ct) {
					return cinfo;
				}
			}
			return null;
		}
		
		/*
		 * Add the name of a local/anonymous class, where anonymous classes have name "".
		 */
		public void addLocalClassInfo(ClassInfo cinfo) {
			localClassInfo.add(cinfo);
		}
		
		/*
		 * Return whether the code is static.
		 * Local/anonymous classes inside static code should not have outer field.
		 */
		public boolean isStatic() {
			return ci.flags().isStatic();
		}
		
		/*
		 * Check whether a final local variable with the specified name exists.
		 */
		public boolean existFinal(String name) {
			for (int i = blockFinals.size() - 1; i >= 0; i--) {
				List l = (List)blockFinals.get(i);
				for (Iterator it = l.iterator(); it.hasNext(); ) {
					LocalInstance li = (LocalInstance)it.next();
					if (li.name().equals(name)) return true;
				}
			}
			for (Iterator it = finalArgs.iterator(); it.hasNext(); ) {
				LocalInstance li = (LocalInstance)it.next();
				if (li.name().equals(name)) return true;
			}
			return false;
		}
	}

	Stack classContext; // The context stack of all the enclosing classes.
					   // It is a stack of ClassInfo.
	Stack codeContext; // The context stack of all the enclosing code.
					  // It is a stack of CodeInfo.
	HashMap innerClassInfoMap; // The map from full names to class infos of inner classes.
	Stack insideCode; // Boolean stack that indicates whether it is inside a piece of code now. 
	Stack staticFieldDecl; // Boolean stack that indicates whether it is inside 
						  // the initialization of a static field.
	
	protected String namePrefix() {
		return "jl$";
	}
	
	/*
	 * Generate the new name for a field that comes from a final local variable.
	 */
	protected String newFieldName(String name) {
		return namePrefix() + name;
	}
	
	public InnerTranslator(TypeSystem ts, NodeFactory nf) {
		this.ts = ts;
		this.nf = nf;
		classContext = new Stack();
		codeContext = new Stack();
		innerClassInfoMap = new HashMap();
		insideCode = new Stack();
		staticFieldDecl = new Stack();
	}
	
	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl)n;
			enterClassDecl(cd);
		}
		else if (n instanceof New) {
			New newExpr = (New)n;
			enterNew(newExpr);
		}
		else if (n instanceof CodeDecl) {
			CodeDecl cd = (CodeDecl)n;
			enterCodeDecl(cd);
		}
		else if (n instanceof Block) {
			CodeInfo cinfo = (CodeInfo)codeContext.peek();
			cinfo.pushBlock();
		}
		else if (n instanceof LocalDecl) {
			LocalDecl ld = (LocalDecl)n;
			enterLocalDecl(ld);
		}
		else if (n instanceof ClassBody) {
			insideCode.push(new Boolean(false));
		}
		else if (n instanceof FieldDecl) {
			if (((FieldDecl)n).flags().isStatic()) {
				staticFieldDecl.push(new Boolean(true));
			}
			else {
				staticFieldDecl.push(new Boolean(false));
			}
		}
		return this;
	}

	protected void enterClassDecl(ClassDecl cd) {
		ParsedClassType ct = cd.type();
		ClassInfo cinfo = new ClassInfo(ct);
		if (ct.isInnerClass() && ct.isMember()) {
			ClassInfo classInfo = (ClassInfo)classContext.peek();
			cinfo.addConsFormal(produceOuterFormal(ct, classInfo.classType()));
			cinfo.hasOuterField(true);
			classInfo.addInnerClassInfo(cinfo);
			innerClassInfoMap.put(ct.fullName(), cinfo);
		}
		
		if (ct.isLocal()) {
			CodeInfo codeInfo = (CodeInfo)codeContext.peek();
			ClassInfo classInfo = (ClassInfo)classContext.peek();
			if (!codeInfo.isStatic()) {
				// If this local/anonymous class is inside a static method, 
				// then it shouldn't have an outer field.
				cinfo.addConsFormal(produceOuterFormal(cd.type(), classInfo.classType()));
				cinfo.hasOuterField(true);
			}
			codeInfo.addLocalClassInfo(cinfo);
			cinfo.insideCode(codeInfo);
			ct.kind(ClassType.MEMBER);
			ct.outer(classInfo.classType());
			ct.needSerialization(false); // local classes don't need serialization.
			String className = classInfo.localClassName(cd.name(), classInfo.addLocalClassName(cd.name()));
			ct.name(className);
		
			for (Iterator it = codeInfo.finalList().iterator(); it.hasNext(); ) {
				LocalInstance li = (LocalInstance)it.next();
				String name = li.name();
				Formal f = nf.Formal(Position.compilerGenerated(), 
									 Flags.NONE, 
									 nf.CanonicalTypeNode(Position.compilerGenerated(), 
														  li.type()), 
									 name);
				f = f.localInstance(ts.localInstance(Position.compilerGenerated(), 
								    f.flags(), 
								    f.type().type(), 
								    f.name()));
				cinfo.addConsFormal(f);
			}
			innerClassInfoMap.put(ct.fullName(), cinfo);
		}
		
		classContext.push(cinfo);
	}
	
	protected void enterNew(New newExpr) {
		if (newExpr.body() != null) {
			// If this is an anonymous class declaration.
			ParsedClassType ct = newExpr.anonType();
			ct.flags(Flags.NONE);
			ClassInfo cinfo = new ClassInfo(ct);
			
			// Check whether the anonymous class is defined outside a code (as the initialization of a field)
			boolean inCode = ((Boolean)insideCode.peek()).booleanValue();
			CodeInfo codeInfo = null;
			if (inCode) {
				codeInfo = (CodeInfo)codeContext.peek();
			}
			ClassInfo classInfo = (ClassInfo)classContext.peek();
			
			if (inCode && !codeInfo.isStatic() 
			|| !inCode && !((Boolean)staticFieldDecl.peek()).booleanValue()) {
				// If this local/anonymous class is inside a static method, 
				// then it shouldn't have an outer field.
				cinfo.addConsFormal(produceOuterFormal(ct, classInfo.classType()));
				cinfo.hasOuterField(true);
			}
			
			if (inCode) {
				codeInfo.addLocalClassInfo(cinfo);
			}
			else {
				classInfo.addInnerClassInfo(cinfo);
			}
			cinfo.insideCode(codeInfo);
			
			ct.kind(ClassType.MEMBER);
			ct.outer(classInfo.classType());
			ct.needSerialization(false); // anonymous classes don't need serialization.
			String className = classInfo.localClassName("", classInfo.addLocalClassName(""));
			ct.name(className);
			
			if (inCode) {
				for (Iterator it = codeInfo.finalList().iterator(); it.hasNext(); ) {
					LocalInstance li = (LocalInstance)it.next();
					String name = li.name();
					Formal f = nf.Formal(Position.compilerGenerated(), 
										 Flags.NONE, 
										 nf.CanonicalTypeNode(Position.compilerGenerated(), 
															  li.type()), 
										 name);
					f = f.localInstance(ts.localInstance(Position.compilerGenerated(), 
									    f.flags(), 
									    f.type().type(), 
									    f.name()));
					cinfo.addConsFormal(f);
				}
			}
			
			innerClassInfoMap.put(ct.fullName(), cinfo);

			classContext.push(cinfo);
		}
	}
	
	protected void enterCodeDecl(CodeDecl cd) {
		CodeInfo cinfo = new CodeInfo(cd.codeInstance());
		
		// If it is a constructor or method, find all the final arguments and add them to the finalVar map. 
		if (cd instanceof ProcedureDecl) {
			ProcedureDecl pd = (ProcedureDecl)cd;
			for (Iterator it = pd.formals().iterator(); it.hasNext(); ) {
				Formal f = (Formal)it.next();
				if (f.flags().isFinal()) {
					cinfo.addFinalArg(f.localInstance());
				}
			}
		}
		
		codeContext.push(cinfo);
		insideCode.push(new Boolean(true));
	}
	
	protected void enterLocalDecl(LocalDecl ld) {
		if (ld.flags().isFinal()) {
			((CodeInfo)codeContext.peek()).addFinalLocal(ld.localInstance());
		}
	}
	
	public Node leave(Node old, Node n, NodeVisitor v) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl)n;
			ParsedClassType ct = cd.type();

			ClassInfo selfInfo = (ClassInfo)classContext.pop();

			// Do nothing if it is already a static class, or it is a toplevel class,  
			// but need to add those classes converted from local/anonymous classes.
			if (ct.flags().isStatic() || ct.isTopLevel()) {
				if (selfInfo.newMemberClasses().size() > 0) {
					cd = addMemberClasses(cd, selfInfo);
				}
				return cd;
			}
			
			// Deal with ordinary inner classes.
			if (selfInfo.insideCode() == null) {
				cd = updateClassDecl(cd, ct, selfInfo);
			}
			else {
				ClassInfo cinfo = (ClassInfo)classContext.peek();
				cd = cd.name(ct.name());
				cd = updateClassDecl(cd, ct, selfInfo);
				cinfo.addMemberClass(cd);
			}
			
			return cd;
		}
		else if (n instanceof New) {
			New newExpr = (New)n;
			if (newExpr.body() != null) {
				// Anonymous class declaration.
				// Need to create a class declaration, and add it to the enclosing class.
				ParsedClassType ct = newExpr.anonType();
				ClassDecl cd = nf.ClassDecl(Position.compilerGenerated(), 
										   ct.flags(), 
										   ct.name(), 
										   nf.CanonicalTypeNode(Position.compilerGenerated(), 
										   					   ct.superType()), 
										   Collections.EMPTY_LIST, 
										   newExpr.body());
				cd = cd.type(ct);
				
				ClassInfo selfInfo = (ClassInfo)classContext.pop();
				ClassInfo cinfo = (ClassInfo)classContext.peek();
				cd = cd.name(ct.name());
				cd = addAnonymousConstructor(cd, ct, selfInfo, newExpr);
				cd = updateClassDecl(cd, ct, selfInfo);
				cinfo.addMemberClass(cd);
			}
			return updateNewExpr(newExpr);
		}
		else if (n instanceof ConstructorCall) {
			ConstructorCall cc = (ConstructorCall)n;
			return updateConstructorCall(cc);
		}
		else if (n instanceof Special) {
			Special s = (Special)n;
			if (s.kind() == Special.THIS && s.qualifier() != null) {
				ClassType tOuter = (ClassType)s.qualifier().type();
				ClassType tThis = ((ClassInfo)classContext.peek()).classType();
				Expr t = s.qualifier(null);
				while (!ts.equals(tOuter, tThis)) {
					t = nf.Field(Position.compilerGenerated(), t, newFieldName(outerThisName(tThis)));
					tThis = (ClassType)tThis.outer();
				}
				return t;
			}
			return s;
		}
		else if (n instanceof Field) {
			// Check whether the field access is a disguised form of "A.this.f".
			Field field = (Field)n;
			if (field.isTargetImplicit()) {
				ClassType tOuter = findField(field.name());
				ClassType tThis = ((ClassInfo)classContext.peek()).classType();
				Expr t = nf.Special(Position.compilerGenerated(), Special.THIS);
				while (!ts.equals(tOuter, tThis)) {
					t = nf.Field(Position.compilerGenerated(), t, newFieldName(outerThisName(tThis)));
					tThis = (ClassType)tThis.outer();
				}
				Field f = nf.Field(Position.compilerGenerated(), t, field.name());
				return f;
			}
			return field;
		}
		else if (n instanceof LocalClassDecl) {
			// Need to remove local class declarations.
			return nf.Empty(Position.compilerGenerated());
		}
		else if (n instanceof CodeDecl) {
			codeContext.pop();
			insideCode.pop();
		}
		else if (n instanceof Block) {
			CodeInfo cinfo = (CodeInfo)codeContext.peek();
			cinfo.popBlock();
		}
		else if (n instanceof Local) {
			Local local = (Local)n;
			if (local.flags().isFinal()) {
				CodeInfo codeInfo = (CodeInfo)codeContext.peek();
				if (!codeInfo.existFinal(local.name())) {
					String newName = newFieldName(local.name());
					ClassType tOuter = findField(newName);
					ClassType tThis = ((ClassInfo)classContext.peek()).classType();
					Expr t = nf.Special(Position.compilerGenerated(), Special.THIS);
					while (!ts.equals(tOuter, tThis)) {
						t = nf.Field(Position.compilerGenerated(), t, newFieldName(outerThisName(tThis)));
						tThis = (ClassType)tThis.outer();
					}
					Field f = nf.Field(Position.compilerGenerated(), t, newName);
					return f;
				}
			}
			return local;
		}
		else if (n instanceof ClassBody) {
			insideCode.pop();
		}
		else if (n instanceof FieldDecl) {
			staticFieldDecl.pop();
		}
		
		return n;
    }
	
	/*
	 * Add the constructor for an anonymous class.
	 */
	protected ClassDecl addAnonymousConstructor(ClassDecl cd, ParsedClassType ct, ClassInfo cinfo, New newExpr) {
		List formals = new ArrayList(newExpr.arguments().size() + 1);
		List args = new ArrayList(newExpr.arguments().size() + 1);
		List ftypes = new ArrayList(newExpr.arguments().size() + 1);
		
		ClassType outerType = ct.outer();

		int i = 0;
		for (Iterator it = newExpr.arguments().iterator(); it.hasNext(); i++) {
			Expr arg = (Expr)it.next();
			Formal f = nf.Formal(Position.compilerGenerated(), 
								Flags.NONE, 
								nf.CanonicalTypeNode(Position.compilerGenerated(), 
													arg.type()), 
								"arg" + i);
			LocalInstance li = ts.localInstance(Position.compilerGenerated(), 
											  Flags.NONE, 
											  arg.type(), 
											  "arg" + i);
			f = f.localInstance(li);
			formals.add(f);
			
			Local l = nf.Local(Position.compilerGenerated(), "arg" + i);
			l = l.localInstance(li);
			args.add(l);
			
			ftypes.add(arg.type());
		}
		ConstructorCall cc = nf.SuperCall(Position.compilerGenerated(), args);
		cc = cc.constructorInstance(newExpr.constructorInstance());
		cc = updateConstructorCall(cc);
		ConstructorDecl cons = nf.ConstructorDecl(Position.compilerGenerated(), 
											    Flags.NONE, 
												ct.name(), 
												formals, 
												Collections.EMPTY_LIST, 
												nf.Block(Position.compilerGenerated(), cc));
		ConstructorInstance consInst = ts.constructorInstance(Position.compilerGenerated(), 
														   ct, 
														   Flags.NONE, 
														   ftypes, 
														   Collections.EMPTY_LIST);
		cons = cons.constructorInstance(consInst);
		List members = cd.body().members();
		List newMembers = new ArrayList(members.size() + 1);
		newMembers.add(cons);
		newMembers.addAll(members);
		cd = cd.body(nf.ClassBody(cd.body().position(), newMembers));
		return cd;
	}
	
	/*
	 * Find the class type inside which a field with specified name is declared.
	 */
	protected ClassType findField(String name) {
		for (int i = classContext.size() - 1; i >= 0; i--) {
			ClassInfo cinfo = (ClassInfo)classContext.get(i);
			ClassType ct = cinfo.classType();
			FieldInstance fi = ct.fieldNamed(name);
			if (fi != null) {
				return ct;
			}
		}
		throw new RuntimeException("Unable to find field " + name + ".");
	}
		
	protected ConstructorCall updateConstructorCall(ConstructorCall cc) {
		ConstructorInstance ci = cc.constructorInstance();
		ClassType ct = (ClassType)ci.container();
		if (cc.kind().equals(ConstructorCall.THIS)) {
			// If calling a constructor of the same class, just need to pass all the new formals. 
			ClassInfo cinfo = (ClassInfo)classContext.peek();
			ci = updateConstructorInst(ct, ci, cinfo);
			List formals = cinfo.newConsFormals();
			List args = new ArrayList(cc.arguments().size() + formals.size());
			for (Iterator it = formals.iterator(); it.hasNext(); ) {
				Formal f = (Formal)it.next();
				args.add(nf.Local(Position.compilerGenerated(), f.name()));
			}
			args.addAll(cc.arguments());
			cc = (ConstructorCall)cc.arguments(args);
			cc = cc.constructorInstance(ci);
		}
		else {
			ClassInfo cinfo = (ClassInfo)innerClassInfoMap.get(ct.fullName());
			if (cinfo != null) {
				// it is an inner class.
				ClassInfo selfInfo = (ClassInfo)classContext.peek();
				if (cinfo.insideCode() == null) {
					// For member inner classes, only need to add one formal that refers to the outer instance.
					ci = updateConstructorInst(ct, ci, cinfo);
					List args = new ArrayList(cc.arguments().size() + 1);
					args.add(nf.Local(Position.compilerGenerated(), outerThisName(ct)));
					args.addAll(cc.arguments());
					cc = (ConstructorCall)cc.arguments(args);
					cc = cc.constructorInstance(ci);
				}
				else if (selfInfo.insideCode() == cinfo.insideCode()) {
					// The super class is a local class, and they are within the same code.
					ci = updateConstructorInst(ct, ci, cinfo);
					List formals = cinfo.newConsFormals();
					List args = new ArrayList(cc.arguments().size() + formals.size());
					for (Iterator it = formals.iterator(); it.hasNext(); ) {
						Formal f = (Formal)it.next();
						args.add(nf.Local(Position.compilerGenerated(), f.name()));
					}
					args.addAll(cc.arguments());
					cc = (ConstructorCall)cc.arguments(args);
					cc = cc.constructorInstance(ci);
				}
				else {
					// The super class is a local class, and they are not within the same code.
					// Need to first find an enclosing class that is inside the same code as the super class,
					// and use its fields as arguments.
					Expr outer = nf.Local(Position.compilerGenerated(), outerThisName(ct));
					ClassType outerCt = selfInfo.classType().outer();
					ClassInfo outerCInfo = (ClassInfo)innerClassInfoMap.get(outerCt.fullName());
					while (outerCInfo.insideCode() != cinfo.insideCode()) {
						outer = nf.Field(Position.compilerGenerated(), outer, newFieldName(outerThisName(ct)));
						outerCt = outerCt.outer();
						outerCInfo = (ClassInfo)innerClassInfoMap.get(outerCt.fullName());
					}
					ci = updateConstructorInst(ct, ci, cinfo);
					List formals = cinfo.newConsFormals();
					List args = new ArrayList(cc.arguments().size() + formals.size());
					for (Iterator it = formals.iterator(); it.hasNext(); ) {
						Formal f = (Formal)it.next();
						args.add(nf.Field(Position.compilerGenerated(), outer, newFieldName(f.name())));
					}
					args.addAll(cc.arguments());
					cc = (ConstructorCall)cc.arguments(args);
					cc = cc.constructorInstance(ci);
				}
			}
		}
		return cc;
	}
	
	/*
	 * Add those member classes converted from local/anonymous classes to the class declaration.
	 */
	protected ClassDecl addMemberClasses(ClassDecl cd, ClassInfo cinfo) {
		List members = new ArrayList(cd.body().members().size() + cinfo.newMemberClasses().size());
		members.addAll(cd.body().members());
		members.addAll(cinfo.newMemberClasses());
		ClassBody b = nf.ClassBody(cd.body().position(), members);
		b = (ClassBody)b.exceptions(cd.body().exceptions());
		cd = cd.body(b);
		ParsedClassType ct = cd.type(); 
		for (Iterator it = cinfo.newMemberClasses().iterator(); it.hasNext(); ) {
			ClassDecl addedCd = (ClassDecl)it.next();
			ct.addMemberClass(addedCd.type());
		}
		cd = cd.type(ct);
		return cd;
	}
	
	protected Expr updateNewExpr(New newExpr) {
		ClassType ct = (ClassType)newExpr.type(); 
		ClassInfo classInfo = (ClassInfo)classContext.peek();
		ClassInfo cinfo = null;
		boolean inCode = ((Boolean)insideCode.peek()).booleanValue();
		if (inCode) {
			CodeInfo codeInfo = (CodeInfo)codeContext.peek();
			cinfo = codeInfo.findLocalClassInfo(ct);
		}
		if (cinfo == null) {
			cinfo = classInfo.findInnerClassInfo(ct);
		}
		if (cinfo != null) {
			ConstructorInstance ci = newExpr.constructorInstance();
			List formals = cinfo.newConsFormals();
			List args = new ArrayList(newExpr.arguments().size() + formals.size());
			List ftypes = new ArrayList(newExpr.arguments().size() + formals.size());
			Iterator it = formals.iterator();
			if (cinfo.hasOuterField()) {
				if (newExpr.qualifier() != null) {
					args.add(newExpr.qualifier());
					ftypes.add(newExpr.qualifier().type());
				}
				else {
					args.add(nf.This(Position.compilerGenerated()));
					ftypes.add(classInfo.classType());
				}
				it.next(); // Only if there is an outer field, the first argument needs to be skipped.
			}
			for (; it.hasNext(); ) {
				Formal f = (Formal)it.next();
				args.add(nf.Local(Position.compilerGenerated(), f.name()));
				ftypes.add(f.type().type());
			}
			args.addAll(newExpr.arguments());
			ftypes.addAll(ci.formalTypes());
			
			New nExpr = (New)newExpr.arguments(args);
			ci.setFormalTypes(ftypes);
			if (newExpr.anonType() != null) {
				ci.setContainer(newExpr.anonType());
				nExpr = nExpr.objectType(nf.CanonicalTypeNode(Position.compilerGenerated(), newExpr.anonType()));
			}

			nExpr = nExpr.qualifier(null);
			nExpr = nExpr.anonType(null);
			nExpr = nExpr.body(null);
			nExpr = nExpr.constructorInstance(ci);
			
			return nExpr;
		}
		else if (ct.isInnerClass()) {
			// Maybe we have encountered the new expression of an inner class before it is translated.
			ConstructorInstance ci = newExpr.constructorInstance();
			List args = new ArrayList(newExpr.arguments().size() + 1);
			List ftypes = new ArrayList(newExpr.arguments().size() + 1);
			if (newExpr.qualifier() != null) {
				args.add(newExpr.qualifier());
				ftypes.add(newExpr.qualifier().type());
			}
			else {
				args.add(nf.This(Position.compilerGenerated()));
				ftypes.add(((ClassInfo)classContext.peek()).classType());
			}
			args.addAll(newExpr.arguments());
			ftypes.addAll(ci.formalTypes());
			ci.setFormalTypes(ftypes);
			New nExpr = (New)newExpr.arguments(args);
			nExpr = nExpr.qualifier(null);
			nExpr = nExpr.constructorInstance(ci);
			return nExpr;
		}
		return newExpr;
	}
	
	protected ClassDecl updateClassDecl(ClassDecl cd, ParsedClassType ct, ClassInfo cinfo) {
		Flags f = ct.flags().Static();
		List members = new LinkedList();
		
		List fields = produceFieldDecls(ct, cinfo);
		members.addAll(fields);
		
//		for (Iterator it = fields.iterator(); it.hasNext(); ) {
//			FieldDecl fd = (FieldDecl)it.next();
//			ct.addField(fd.fieldInstance());
//		}
		
		ct.clearConstructors();
		
		for (Iterator it = cd.body().members().iterator(); it.hasNext(); ) {
			ClassMember m = (ClassMember)it.next();
			if (m instanceof ConstructorDecl) {
				ConstructorDecl cons = (ConstructorDecl)m;
				ConstructorInstance ci = cons.constructorInstance();
				ConstructorDecl newCons = updateConstructor(cd, ct, cons, cinfo);
				ConstructorInstance newCi = updateConstructorInst(ct, ci, cinfo);
				newCons = newCons.constructorInstance(newCi);
				members.add(newCons);
				ct.addConstructor(newCi);
			}
			else {
			    members.add(m);
			}
		}
		
		List newMemClasses = cinfo.newMemberClasses();
		members.addAll(newMemClasses);
//		for (Iterator it = newMemClasses.iterator(); it.hasNext(); ) {
//			ClassDecl memCd = (ClassDecl)it.next();
//			ct.addMemberClass(memCd.type());
//		}
		
		ClassBody cb = cd.body();
		cb = cb.members(members);
		cd = cd.body(cb);
		cd = cd.type(ct);
		cd = cd.flags(cd.flags().Static());
		
		return cd;
	}
		
	protected List produceFieldDecls(ClassType ct, ClassInfo cinfo) {
		List newFormals = cinfo.newConsFormals();
		List fields = new ArrayList(newFormals.size());
		for (Iterator it = newFormals.iterator(); it.hasNext(); ) {
			Formal formal = (Formal)it.next();
			FieldDecl fd = nf.FieldDecl(Position.compilerGenerated(), 
									  Flags.PROTECTED, 
									  formal.type(), 
									  newFieldName(formal.name()));
			FieldInstance fi = ts.fieldInstance(Position.compilerGenerated(), 
											  ct, 
											  Flags.PROTECTED, 
											  formal.type().type(), 
											  newFieldName(formal.name()));
			fd = fd.fieldInstance(fi);
			fields.add(fd);
		}
		return fields;
	}
	
	// Return the name of the "outer this" field and formal in ct. 
	protected String outerThisName(ClassType ct) { 
		return "outer$this";
	}
			
	/*
	 * ct  - ClassType of the inner class that we are dealing with.
	 * oct - ClassType of the outer class.
	 */
	protected Formal produceOuterFormal(ClassType ct, ClassType oct) {
		String fn = outerThisName(ct);
		Formal formal = nf.Formal(Position.compilerGenerated(), 
				                  Flags.NONE, 
								 nf.CanonicalTypeNode(Position.compilerGenerated(), oct), 
								 fn);
		formal = formal.localInstance(ts.localInstance(Position.compilerGenerated(), 
													formal.flags(), 
													formal.type().type(), 
													formal.name()));
		return formal;
	}
	
	protected ConstructorInstance updateConstructorInst(ClassType ct, ConstructorInstance ci, ClassInfo cinfo) {
		List newFormals = cinfo.newConsFormals();
		List ftypes = new ArrayList(ci.formalTypes().size() + newFormals.size());
		for (Iterator it = newFormals.iterator(); it.hasNext(); ) {
			Formal f = (Formal)it.next();
			ftypes.add(f.type().type());
		}
		ftypes.addAll(ci.formalTypes());
		ci.setFormalTypes(ftypes);
		ci.setContainer(ct);
		return ci;
	}
	
	// Add new argument(s) to a constructor
	protected ConstructorDecl updateConstructor(ClassDecl cd, ClassType ct, ConstructorDecl cons, ClassInfo cinfo) {
		List newFormals = cinfo.newConsFormals();
		List formals = new ArrayList(cons.formals().size() + newFormals.size());
		formals.addAll(newFormals);
		formals.addAll(cons.formals());
		
		List oldStmts = cons.body().statements();
		List stmts = new ArrayList(oldStmts.size() + newFormals.size());
		Iterator it = oldStmts.iterator();

		// Check whether the first statement is a constructor call.
		if (it.hasNext()) {
			Stmt s = (Stmt)it.next();
			if (s instanceof ConstructorCall) {
				stmts.add(s);
				// If it calls another constructor in the same class, we don't need to initialize the field.
				if (((ConstructorCall)s).kind() != ConstructorCall.THIS) {
					stmts.addAll(produceFieldInits(cinfo));
				}
			}
			else {
				stmts.addAll(produceFieldInits(cinfo));
				stmts.add(s);
			}
		}
		else {
			stmts.addAll(produceFieldInits(cinfo));
		}
		while (it.hasNext()) {
			stmts.add(it.next());
		}
		
		Block b = nf.Block(Position.compilerGenerated(), stmts);
		return nf.ConstructorDecl(Position.compilerGenerated(), 
								cons.flags(), 
								ct.name(), 
								formals, 
								cons.throwTypes(), 
								b);
	}
	
	// Generate a list that contains all the field assignments for initializing newly added fields.
	protected List produceFieldInits(ClassInfo cinfo) {
		List newFormals = cinfo.newConsFormals();
		List fInits = new ArrayList(newFormals.size());
		for (Iterator it = newFormals.iterator(); it.hasNext(); ) {
			Formal formal = (Formal)it.next();
			Local local = nf.Local(Position.compilerGenerated(), formal.name());
			Field field = nf.Field(Position.compilerGenerated(), 
								  nf.This(Position.compilerGenerated()), 
								  newFieldName(formal.name()));
			FieldAssign fAssign = nf.FieldAssign(Position.compilerGenerated(), 
											   field, 
											   Assign.ASSIGN, 
											   local);
			Stmt stmt = nf.Eval(Position.compilerGenerated(), fAssign);
			fInits.add(stmt);
		}
		return fInits;
	}

}

