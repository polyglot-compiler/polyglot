package polyglot.ext.jl.ast;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.SchedulerException;
import polyglot.frontend.goals.Goal;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

/**
 * A <code>New</code> is an immutable representation of the use of the
 * <code>new</code> operator to create a new instance of a class.  In
 * addition to the type of the class being created, a <code>New</code> has a
 * list of arguments to be passed to the constructor of the object and an
 * optional <code>ClassBody</code> used to support anonymous classes.
 */
public class New_c extends Expr_c implements New
{
    protected Expr qualifier;
    protected TypeNode tn;
    protected List arguments;
    protected ClassBody body;
    protected ConstructorInstance ci;
    protected ParsedClassType anonType;

    public New_c(Position pos, Expr qualifier, TypeNode tn, List arguments, ClassBody body) {
	super(pos);
        this.qualifier = qualifier;
        this.tn = tn;
	this.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	this.body = body;
    }

    /** Get the qualifier expression of the allocation. */
    public Expr qualifier() {
        return this.qualifier;
    }

    /** Set the qualifier expression of the allocation. */
    public New qualifier(Expr qualifier) {
        New_c n = (New_c) copy();
        n.qualifier = qualifier;
        return n;
    }

    /** Get the type we are instantiating. */
    public TypeNode objectType() {
        return this.tn;
    }

    /** Set the type we are instantiating. */
    public New objectType(TypeNode tn) {
        New_c n = (New_c) copy();
	n.tn = tn;
	return n;
    }

    public ParsedClassType anonType() {
	return this.anonType;
    }

    public New anonType(ParsedClassType anonType) {
	New_c n = (New_c) copy();
	n.anonType = anonType;
	return n;
    }

    public ProcedureInstance procedureInstance() {
	return constructorInstance();
    }

    public ConstructorInstance constructorInstance() {
	return this.ci;
    }

    public New constructorInstance(ConstructorInstance ci) {
	New_c n = (New_c) copy();
	n.ci = ci;
	return n;
    }

    public List arguments() {
	return this.arguments;
    }

    public ProcedureCall arguments(List arguments) {
	New_c n = (New_c) copy();
	n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	return n;
    }

    public ClassBody body() {
	return this.body;
    }

    public New body(ClassBody body) {
	New_c n = (New_c) copy();
	n.body = body;
	return n;
    }

    /** Reconstruct the expression. */
    protected New_c reconstruct(Expr qualifier, TypeNode tn, List arguments, ClassBody body) {
	if (qualifier != this.qualifier || tn != this.tn || ! CollectionUtil.equals(arguments, this.arguments) || body != this.body) {
	    New_c n = (New_c) copy();
	    n.tn = tn;
	    n.qualifier = qualifier;
	    n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	    n.body = body;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression. */
    public Node visitChildren(NodeVisitor v) {
	Expr qualifier = (Expr) visitChild(this.qualifier, v);
	TypeNode tn = (TypeNode) visitChild(this.tn, v);
	List arguments = visitList(this.arguments, v);
	ClassBody body = (ClassBody) visitChild(this.body, v);
	return reconstruct(qualifier, tn, arguments, body);
    }

    public Context enterChildScope(Node child, Context c) {
        if (child == body && anonType != null && body != null) {
            c = c.pushClass(anonType, anonType);
        }
        return super.enterChildScope(child, c);
    }

    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        if (body != null) {
            /*
            // bypass the visiting of the body of the anonymous class. We'll
            // get around to visiting it in the buildTypes method.
            // We do this because we need to visit the body of the anonymous
            // class after we've pushed an anon class onto the type builder, 
            // but we need to check the arguments, and qualifier, etc. outside 
            // of the scope of the anon class.            
            return tb.bypass(body);
            */
            return tb.pushAnonClass(position());
        }
        
        return tb;
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        New_c n = this;
        TypeSystem ts = tb.typeSystem();

        List l = new ArrayList(n.arguments.size());
        for (int i = 0; i < n.arguments.size(); i++) {
            l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci = ts.constructorInstance(position(), ts.Object(),
                                                        Flags.NONE, l,
                                                        Collections.EMPTY_LIST);
        n = (New_c) n.constructorInstance(ci);
        
        if (n.body() != null) {
            /*
            // let's get a type builder that is prepared to visit the
            // body; tb wants to bypass it, due to the buildTypesEnter method.
            TypeBuilder bodyTB = (TypeBuilder)tb.visitChildren();
            
            // push an anonymous class on the stack.
            bodyTB = bodyTB.pushAnonClass(position());

            n = (New_c) n.body((ClassBody)n.body().visit(bodyTB));
            ParsedClassType type = (ParsedClassType) bodyTB.currentClass();
            */
            ParsedClassType type = tb.currentClass();
            n = (New_c) n.anonType(type);
            
            type.setMembersAdded(true);

//            n = n.addTypeBelow(type);
        }
        
        return n.type(ts.unknownType(position()));
    }
    
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar) throws SemanticException {
        // Skip it -- we'll do all the disambiguation during type checking.
        return this;
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        New_c n = this;

        if (! tn.isDisambiguated()) {
            return this;
        }

        if (! tn.type().isClass()) {
            return this;
        }
        
        if (qualifier == null) {
            ClassType ct = tn.type().toClass();

            if (ct.isMember() && ! ct.flags().isStatic()) {
                n = findQualifier(ar, ct);
            }
        }
        
        if (n.qualifier() != null && ! n.qualifier().isDisambiguated()) {
            return this;
        }
        
        if (anonType != null && ! anonType.supertypesResolved()) {
            ClassType ct = tn.type().toClass();
            
            if (! ct.flags().isInterface()) {
                anonType.superType(ct);
            }
            else {
                anonType.superType(ar.typeSystem().Object());
                anonType.addInterface(ct);
            }
            
            anonType.setSupertypesResolved(true);
        }
        
        return n;
    }

    /**
     * @param ar
     * @param ct
     * @throws SemanticException
     */
    private New_c findQualifier(AmbiguityRemover ar, ClassType ct) throws SemanticException {
        // If we're instantiating a non-static member class, add a "this"
        // qualifier.
        NodeFactory nf = ar.nodeFactory();
        TypeSystem ts = ar.typeSystem();
        Context c = ar.context();
        
        // Search for the outer class of the member.  The outer class is
        // not just ct.outer(); it may be a subclass of ct.outer().
        Type outer = null;
        
        String name = ct.name();
        ClassType t = c.currentClass();
        
        // We're in one scope too many.
        if (t == anonType) {
            t = t.outer();
        }
        
        while (t != null) {
            try {
                // HACK: PolyJ outer() doesn't work
                t = ts.staticTarget(t).toClass();
                ClassType mt = ts.findMemberClass(t, name, c.currentClass());
                
                if (ts.equals(mt, ct)) {
                    outer = t;
                    break;
                }
            }
            catch (SemanticException e) {
            }
            
            t = t.outer();
        }
        
        if (outer == null) {
            throw new SemanticException("Could not find non-static member class \"" +
                                        name + "\".", position());
        }
        
        // Create the qualifier.
        Expr q;

        if (outer.equals(c.currentClass())) {
            q = nf.This(position());
        }
        else {
            q = nf.This(position(),
                        nf.CanonicalTypeNode(position(),
                                             outer));
        }
        
        q = q.type(outer);
        return (New_c) qualifier(q);
    }

    public New disambiguateObjectType(TypeChecker tc) throws SemanticException {
        New n = this;
        
        // The type for qualifier should already have been computed.
        if (qualifier != null) {
            if (! qualifier.isDisambiguated()) {
                Scheduler scheduler = tc.job().extensionInfo().scheduler();
                Goal g = scheduler.TypeChecked(tc.job());
                throw new MissingDependencyException(g);
            }

            // Get the qualifier type first.
            Type qt = qualifier.type();

            if (! qt.isClass()) {
                throw new SemanticException("Cannot instantiate non-class " +
                                            qt + ".");
            }
            
            // Disambiguate the type node as a member of the qualifier type.
            TypeNode tn = disambiguateTypeNode(this.tn, tc, qt.toClass());

            n = objectType(tn);
        }

        return n;
    }
    
    protected TypeNode disambiguateTypeNode(TypeNode tn, TypeChecker tc, ClassType outer) throws SemanticException
    {
        // We have to disambiguate the type node as if it were a member of the
        // static type, outer, of the qualifier.  For Java this is simple: type
        // nested type is just a name and we
        // use that name to lookup a member of the outer class.  For some
        // extensions (e.g., PolyJ), the type node may be more complex than
        // just a name.  We'll just punt here and let the extensions handle
        // this complexity.

        if (tn instanceof CanonicalTypeNode) {
            return tn;
        }

        String name = null;

        if (tn instanceof AmbTypeNode && ((AmbTypeNode) tn).qual() == null) {
            name = ((AmbTypeNode) tn).name();
        }
        else {
            throw new SemanticException(
                "Cannot instantiate a member class.",
                tn.position());
        }

        TypeSystem ts = tc.typeSystem();
        NodeFactory nf = tc.nodeFactory();
        Context c = tc.context();

        ClassType ct = ts.findMemberClass(outer, name, c.currentClass());
        return nf.CanonicalTypeNode(tn.position(), ct);
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        
        List argTypes = new ArrayList(arguments.size());
        
        for (Iterator i = this.arguments.iterator(); i.hasNext(); ) {
            Expr e = (Expr) i.next();
            if (! e.isDisambiguated()) {
                return this;
            }
            argTypes.add(e.type());
        }
        
        if (! tn.isDisambiguated() || ! tn.type().isClass()) {
            return this;
        }
        
        typeCheckFlags(tc);
        typeCheckNested(tc);
        
        if (this.body != null) {
            ts.checkClassConformance(anonType);
        }
        
        ClassType ct = tn.type().toClass();
        
        if (! ct.flags().isInterface()) {
            Context c = tc.context();
            if (anonType != null) {
                c = c.pushClass(anonType, anonType);
            }
            ci = ts.findConstructor(ct, argTypes, c.currentClass());
        }
        else {
            ci = ts.defaultConstructor(this.position(), ct);
        }
        
        New n = this.constructorInstance(ci);
        
        if (anonType != null) {
            // The type of the new expression is the anonymous type, not the base type.
            ct = anonType;
        }

        return n.type(ct);
    }

    protected void typeCheckNested(TypeChecker tc) throws SemanticException {
        if (qualifier != null) {
            // We have not disambiguated the type node yet.

            // Get the qualifier type first.
            Type qt = qualifier.type();

            if (! qt.isClass()) {
                throw new SemanticException(
                    "Cannot instantiate member class of a non-class type.",
                    qualifier.position());
            }
            
            // Disambiguate the type node as a member of the qualifier type.
            ClassType ct = tn.type().toClass();

            // According to JLS2 15.9.1, the class type being
            // instantiated must be inner.
	    if (! ct.isInnerClass()) {
                throw new SemanticException(
                    "Cannot provide a containing instance for non-inner class " +
		    ct.fullName() + ".", qualifier.position());
            }
        }
        else {
            ClassType ct = tn.type().toClass();

            if (ct.isMember()) {
                for (ClassType t = ct; t.isMember(); t = t.outer()) {
                    if (! t.flags().isStatic()) {
                        throw new SemanticException(
                            "Cannot allocate non-static member class \"" +
                            t + "\".", position());
                    }
                }
            }
        }
    }

    protected void typeCheckFlags(TypeChecker tc) throws SemanticException {
        ClassType ct = tn.type().toClass();

	if (this.body == null) {
	    if (ct.flags().isInterface()) {
		throw new SemanticException(
		    "Cannot instantiate an interface.", position());
	    }

	    if (ct.flags().isAbstract()) {
		throw new SemanticException(
		    "Cannot instantiate an abstract class.", position());
	    }
	}
	else {
	    if (ct.flags().isFinal()) {
		throw new SemanticException(
		    "Cannot create an anonymous subclass of a final class.",
                    position());
            }

	    if (ct.flags().isInterface() && ! arguments.isEmpty()) {
	        throw new SemanticException(
		    "Cannot pass arguments to an anonymous class that " +
		    "implements an interface.",
		    ((Expr) arguments.get(0)).position());
	    }
	}


        if (! ct.flags().isInterface()) {
            Context c = tc.context();
            if (body != null) {
                c = c.pushClass(anonType, anonType);
            }
        }
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == qualifier) {
            ReferenceType t = ci.container();
                     
            if (t.isClass() && t.toClass().isMember()) {
                t = t.toClass().container();
                return t;
            }

            return child.type();
        }

        Iterator i = this.arguments.iterator();
        Iterator j = ci.formalTypes().iterator();

        while (i.hasNext() && j.hasNext()) {
	    Expr e = (Expr) i.next();
	    Type t = (Type) j.next();

            if (e == child) {
                return t;
            }
        }

        return child.type();
    }

    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	// something didn't work in the type check phase, so just ignore it.
	if (ci == null) {
	    throw new InternalCompilerError(position(),
		"Null constructor instance after type check.");
	}

	for (Iterator i = ci.throwTypes().iterator(); i.hasNext(); ) {
	    Type t = (Type) i.next();
	    ec.throwsException(t, position());
	}

	return super.exceptionCheck(ec);
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    public String toString() {
	return (qualifier != null ? (qualifier.toString() + ".") : "") +
            "new " + tn + "(...)" + (body != null ? " " + body : "");
    }

    protected void printQualifier(CodeWriter w, PrettyPrinter tr) {
        if (qualifier != null) {
            print(qualifier, w, tr);
            w.write(".");
        }
    }

    protected void printArgs(CodeWriter w, PrettyPrinter tr) {
	w.write("(");
	w.begin(0);

	for (Iterator i = arguments.iterator(); i.hasNext();) {
	    Expr e = (Expr) i.next();

	    print(e, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0);
	    }
	}

	w.end();
	w.write(")");
    }

    protected void printBody(CodeWriter w, PrettyPrinter tr) {
	if (body != null) {
	    w.write(" {");
	    print(body, w, tr);
            w.write("}");
	}
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printQualifier(w, tr);
	w.write("new ");
        print(tn, w, tr);
        printArgs(w, tr);
        printBody(w, tr);
    }

    public void translate(CodeWriter w, Translator tr) {
        printQualifier(w, tr);

	w.write("new ");

        if (qualifier != null) {
            ClassType ct = tn.type().toClass();

            if (! ct.isMember()) {
                throw new InternalCompilerError("Cannot qualify a non-member " +
                                                "class.", position());
            }

            tr.setOuterClass(ct.outer());
            print(tn, w, tr);
            tr.setOuterClass(null);
        }
        else {
            print(tn, w, tr);
        }

        printArgs(w, tr);
        printBody(w, tr);
    }

    public Term entry() {
        if (qualifier != null) return qualifier.entry();
        return tn.entry();
    }

    public List acceptCFG(CFGBuilder v, List succs) {
        Term afterArgs = this;
        if (body() != null) {
            afterArgs = body();
        }

        if (qualifier != null) {
            v.visitCFG(qualifier, tn.entry());
        }
        v.visitCFG(tn, listEntry(arguments, afterArgs));

        v.visitCFGList(arguments, afterArgs);

        if (body() != null) {
            v.visitCFG(body(), this);
        }

        return succs;
    }

    public List throwTypes(TypeSystem ts) {
      List l = new LinkedList();
      l.addAll(ci.throwTypes());
      l.addAll(ts.uncheckedExceptions());
      return l;
    }

    /**
     * @param parent
     * @param tc
     */
    public Node typeCheckOverride(Node parent, TypeChecker tc) throws SemanticException {
        New nn = this;
        New old = nn;
        
        BodyDisambiguator bd = new BodyDisambiguator(tc);
        BodyDisambiguator childbd = (BodyDisambiguator) bd.enter(parent, this);

        TypeChecker childtc = (TypeChecker) tc.enter(parent, this);
        
        // Override to ensure that the qualifier type is known before
        // the TypeNode is disambiguated.
        nn = nn.qualifier((Expr) nn.visitChild(nn.qualifier(), childbd));
        if (childbd.hasErrors()) throw new SemanticException();
        
        nn = nn.qualifier((Expr) nn.visitChild(nn.qualifier(), childtc));
        if (childtc.hasErrors()) throw new SemanticException();
        
        // Hack to ensure nn.disambiguate is invoked to set the qualifier.
        nn = (New_c) nn.del().disambiguate(bd);
        if (bd.hasErrors()) throw new SemanticException();
        
        // Now disambiguate nn.objectType().
        if (nn.qualifier() != null) {
            nn = ((New_c) nn).disambiguateObjectType(childtc);
            if (childtc.hasErrors()) throw new SemanticException();
        }
        else {
            nn = nn.objectType((TypeNode) nn.visitChild(nn.objectType(), childbd));
            if (childbd.hasErrors()) throw new SemanticException();
            
            nn = nn.objectType((TypeNode) nn.visitChild(nn.objectType(), childtc));
            if (childtc.hasErrors()) throw new SemanticException();
        }

        // Hack to ensure nn.disambiguate is invoked to set the supertypes.
        nn = (New_c) nn.del().disambiguate(bd);
        if (bd.hasErrors()) throw new SemanticException();

        nn = (New) nn.arguments(nn.visitList(nn.arguments(), childbd));
        if (childbd.hasErrors()) throw new SemanticException();
        
        nn = (New) nn.arguments(nn.visitList(nn.arguments(), childtc));
        if (childtc.hasErrors()) throw new SemanticException();
        
        // Now visit the body.
        if (nn.body() != null) {
            SupertypeDisambiguator supDisamb = new SupertypeDisambiguator(childtc);
            nn = nn.body((ClassBody) nn.visitChild(nn.body(), supDisamb));
            if (supDisamb.hasErrors()) throw new SemanticException();
            
            SignatureDisambiguator sigDisamb = new SignatureDisambiguator(childtc);
            nn = nn.body((ClassBody) nn.visitChild(nn.body(), sigDisamb));
            if (sigDisamb.hasErrors()) throw new SemanticException();
        }
    
        // Now visit the body.
        nn = nn.body((ClassBody) nn.visitChild(nn.body(), childbd));
        if (childbd.hasErrors()) throw new SemanticException();
        
        nn = nn.body((ClassBody) nn.visitChild(nn.body(), childtc));
        if (childtc.hasErrors()) throw new SemanticException();
    
        // Hack to ensure nn.disambiguate is invoked.  What isn't a hack here?
        nn = (New_c) bd.leave(parent, old, nn, childbd);
        if (bd.hasErrors()) throw new SemanticException();
    
        nn = (New_c) tc.leave(parent, old, nn, childtc);
        
        return nn;
    }
}
