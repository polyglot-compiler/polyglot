package polyglot.ext.jl.ast;

import java.util.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.SupertypesResolved;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.visit.*;

/**
 * A <code>ClassDecl</code> is the definition of a class, abstract class,
 * or interface. It may be a public or other top-level class, or an inner
 * named class, or an anonymous class.
 */
public class ClassDecl_c extends Term_c implements ClassDecl
{
    protected Flags flags;
    protected String name;
    protected TypeNode superClass;
    protected List interfaces;
    protected ClassBody body;

    protected ParsedClassType type;

    public ClassDecl_c(Position pos, Flags flags, String name,
                       TypeNode superClass, List interfaces, ClassBody body) {
	    super(pos);
	    this.flags = flags;
	    this.name = name;
	    this.superClass = superClass;
	    this.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
	    this.body = body;
    }
    
    public boolean isDisambiguated() {
        return super.isDisambiguated() && type != null && type.isCanonical() && type.supertypesResolved() && type.signaturesResolved();
    }
    
    public MemberInstance memberInstance() {
        return type;
    }

    public ParsedClassType type() {
	    return type;
    }

    public ClassDecl type(ParsedClassType type) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.type = type;
	    return n;
    }

    public Flags flags() {
	    return this.flags;
    }

    public ClassDecl flags(Flags flags) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.flags = flags;
	    return n;
    }

    public String name() {
	    return this.name;
    }

    public ClassDecl name(String name) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.name = name;
	    return n;
    }

    public TypeNode superClass() {
	    return this.superClass;
    }

    public ClassDecl superClass(TypeNode superClass) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.superClass = superClass;
	    return n;
    }

    public List interfaces() {
	    return this.interfaces;
    }

    public ClassDecl interfaces(List interfaces) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
	    return n;
    }

    public ClassBody body() {
	    return this.body;
    }

    public ClassDecl body(ClassBody body) {
	    ClassDecl_c n = (ClassDecl_c) copy();
	    n.body = body;
	    return n;
    }

    protected ClassDecl_c reconstruct(TypeNode superClass, List interfaces, ClassBody body) {
	    if (superClass != this.superClass || ! CollectionUtil.equals(interfaces, this.interfaces) || body != this.body) {
		    ClassDecl_c n = (ClassDecl_c) copy();
		    n.superClass = superClass;
		    n.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
		    n.body = body;
		    return n;
	    }

	    return this;
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term entry() {
        return this.body().entry();
    }

    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs) {
        v.visitCFG(this.body(), this);
        return succs;
    }

    public Node visitChildren(NodeVisitor v) {
	    TypeNode superClass = (TypeNode) visitChild(this.superClass, v);
	    List interfaces = visitList(this.interfaces, v);
	    ClassBody body = (ClassBody) visitChild(this.body, v);
	    return reconstruct(superClass, interfaces, body);
    }

    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
	tb = tb.pushClass(position(), flags, name);
        
        ParsedClassType type = tb.currentClass();

        // Member classes of interfaces are implicitly public and static.
        if (type.isMember() && type.outer().flags().isInterface()) {
            type.flags(type.flags().Public().Static());
        }

        // Member interfaces are implicitly static. 
        if (type.isMember() && type.flags().isInterface()) {
            type.flags(type.flags().Static());
        }

        // Interfaces are implicitly abstract. 
        if (type.flags().isInterface()) {
            type.flags(type.flags().Abstract());
        }

        return tb;
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
	ParsedClassType type = tb.currentClass();        

	if (type != null) {
	    type.setMembersAdded(true);
	    return type(type).flags(type.flags());
        }
	else {
	    // throw new InternalCompilerError("Missing type.", position());
            return this;
	}
    }

    public Context enterScope(Node child, Context c) {
        if (child == this.body) {
            TypeSystem ts = c.typeSystem();
            c = c.pushClass(type, ts.staticTarget(type).toClass());
        }
        return super.enterScope(child, c);
    }
    
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        ClassDecl_c n = disambiguateSupertypes(ar);
        ParsedClassType type = n.type();

        // Make sure that the inStaticContext flag of the class is correct.
        Context ctxt = ar.context();
        type.inStaticContext(ctxt.inStaticContext());

        // FIXME: shouldn't reach MembersAdded(type) until here!
        return addDefaultConstructorIfNeeded(ar.typeSystem(), ar.nodeFactory());
    }

    protected ClassDecl_c disambiguateSupertypes(AmbiguityRemover ar) throws SemanticException {
        TypeNode newSuperClass;
        List newInterfaces;
 
        boolean supertypesResolved = true;
        
//        System.out.println("  " + ar + ".disamsuper: " + this);
        
        if (! type.supertypesResolved()) {
            if (superClass != null && ! superClass.type().isCanonical()) {
                supertypesResolved = false;
            }
            
            for (Iterator i = interfaces.iterator(); supertypesResolved && i.hasNext(); ) {
                TypeNode tn = (TypeNode) i.next();
                if (! tn.type().isCanonical()) {
                    supertypesResolved = false;
                }
            }
            
            if (! supertypesResolved) {
                Scheduler scheduler = ar.job().extensionInfo().scheduler();
                scheduler.addConcurrentDependency(scheduler.currentGoal(), scheduler.SupertypesResolved(type));
//                System.out.println("    not resolved");
            }
            else {            
//                System.out.println("    resolved");
                setSuperClass(ar, superClass);
                setInterfaces(ar, interfaces);
                type.setSupertypesResolved(true);
            }
        }
        
        return this;
    }

    protected void setSuperClass(AmbiguityRemover ar, TypeNode superClass) throws SemanticException {
        TypeSystem ts = ar.typeSystem();

        if (superClass != null) {
            Type t = superClass.type();
            if (Report.should_report(Report.types, 3))
                Report.report(3, "setting superclass of " + this.type + " to " + t);
            this.type.superType(t);
            ts.checkCycles(t.toReference());
        }
        else if (this.type.equals(ts.Object()) || this.type.fullName().equals(ts.Object().fullName())) {
            // the type is the same as ts.Object(), so it has no superclass.
            if (Report.should_report(Report.types, 3))
                Report.report(3, "setting superclass of " + this.type + " to " + null);
            this.type.superType(null);
        }
        else {
            // the superclass was not specified, and the type is not the same
            // as ts.Object() (which is typically java.lang.Object)
            // As such, the default superclass is ts.Object().
            if (Report.should_report(Report.types, 3))
                Report.report(3, "setting superclass of " + this.type + " to " + ts.Object());
            this.type.superType(ts.Object());
        }    
    }

    protected void setInterfaces(AmbiguityRemover ar, List newInterfaces) throws SemanticException {
        TypeSystem ts = ar.typeSystem();
     
        for (Iterator i = newInterfaces.iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            Type t = tn.type();
    
            if (Report.should_report(Report.types, 3))
		Report.report(3, "adding interface of " + this.type + " to " + t);

            if (!this.type.interfaces().contains(t)) {
                this.type.addInterface(t);
            }

            ts.checkCycles(t.toReference());
        }
    }

    protected Node addDefaultConstructorIfNeeded(TypeSystem ts,
                                                 NodeFactory nf) {
        if (defaultConstructorNeeded()) {
            return addDefaultConstructor(ts, nf);
        }
        return this;
    }

    protected boolean defaultConstructorNeeded() {
        return type().defaultConstructorNeeded();
    }

    protected Node addDefaultConstructor(TypeSystem ts, NodeFactory nf) {
        ConstructorInstance ci = ts.defaultConstructor(position(), this.type);
        this.type.addConstructor(ci);
        Block block = null;
        if (this.type.superType() instanceof ClassType) {
            ConstructorInstance sci = ts.defaultConstructor(position(),
                                                (ClassType) this.type.superType());
            ConstructorCall cc = nf.SuperCall(position(), 
                                              Collections.EMPTY_LIST);
            cc = cc.constructorInstance(sci);
            block = nf.Block(position(), cc);
        }
        else {
            block = nf.Block(position());
        }
        ConstructorDecl cd = nf.ConstructorDecl(position(), Flags.PUBLIC,
                                                name, Collections.EMPTY_LIST,
                                                Collections.EMPTY_LIST,
                                                block);
        cd = (ConstructorDecl) cd.constructorInstance(ci);
        return body(body.addMember(cd));
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (this.type().isNested() && (this.type() instanceof Named)) {
            // The class cannot have the same simple name as any enclosing class.
            ClassType container = this.type.outer();

            while (container instanceof Named) {
                if (!container.isAnonymous()) {
                    String name = ((Named) container).name();
    
                    if (name.equals(this.name)) {
                        throw new SemanticException("Cannot declare member " +
                                                    "class \"" + this.type +
                                                    "\" inside class with the " +
                                                    "same name.", position());
                    }
                }    
                if (container.isNested()) {
                    container = container.outer();
                }
                else {
                    break;
                }
            }
                        
            if (this.type().isLocal()) {
                // a local class name cannot be redeclared within the same
                // method, constructor or initializer, and within its scope                
                Context ctxt = tc.context();

                if (ctxt.isLocal(this.name)) {
                    // something with the same name was declared locally.
                    // (but not in an enclosing class)                                    
                    Named nm = ctxt.find(this.name);
                    if (nm instanceof Type) {
                        Type another = (Type)nm;
                        if (another.isClass() && another.toClass().isLocal()) {
                            throw new SemanticException("Cannot declare local " +
                                "class \"" + this.type + "\" within the same " +
                                "method, constructor or initializer as another " +
                                "local class of the same name.", position());
                        }
                    }
                }                
            }
        }

        // check that inner classes do not declare member interfaces
        if (type().isMember() && flags().isInterface() &&
              type().outer().isInnerClass()) {
            // it's a member interface in an inner class.
            throw new SemanticException("Inner classes cannot declare " + 
                    "member interfaces.", this.position());             
        }

        // Make sure that static members are not declared inside inner classes
        if (type().isMember() && type().flags().isStatic() 
               && type().outer().isInnerClass()) {
            throw new SemanticException("Inner classes cannot declare static " 
                                 + "member classes.", position());
        }
        
        if (type.superType() != null) {
            if (! type.superType().isClass() || type.superType().toClass().flags().isInterface()) {
                throw new SemanticException("Cannot extend non-class \"" +
                                            type.superType() + "\".",
                                            position());
            }

            if (type.superType().toClass().flags().isFinal()) {
                throw new SemanticException("Cannot extend final class \"" +
                                            type.superType() + "\".",
                                            position());
            }
            
            if (this.type.equals(tc.typeSystem().Object()) || this.type.fullName().equals(tc.typeSystem().Object().fullName())) {
                throw new SemanticException("Class \"" + this.type + "\" cannot have a superclass.",
                                            superClass.position());
            }
        }
        
        for (Iterator i = interfaces.iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            Type t = tn.type();
    
            if (! t.isClass() || ! t.toClass().flags().isInterface()) {
                throw new SemanticException("Superinterface " + t + " of " +
                        type + " is not an interface.", tn.position());
            }
            
            if (this.type.equals(tc.typeSystem().Object()) || this.type.fullName().equals(tc.typeSystem().Object().fullName())) {
                throw new SemanticException("Class " + this.type + " cannot have a superinterface.",
                                            tn.position());
            }
        }

        TypeSystem ts = tc.typeSystem();

        try {
            if (type.isTopLevel()) {
                ts.checkTopLevelClassFlags(type.flags());
            }
            if (type.isMember()) {
                ts.checkMemberClassFlags(type.flags());
            }
            if (type.isLocal()) {
                ts.checkLocalClassFlags(type.flags());
            }
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }
        
        // check the class implements all abstract methods that it needs to.
        ts.checkClassConformance(type);

        return this;
    }

    public String toString() {
	    return flags.clearInterface().translate() +
		       (flags.isInterface() ? "interface " : "class ") + name + " " + body;
    }

    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
        if (flags.isInterface()) {
            w.write(flags.clearInterface().clearAbstract().translate());
        }
        else {
            w.write(flags.translate());
        }

        if (flags.isInterface()) {
            w.write("interface ");
        }
        else {
            w.write("class ");
        }

        w.write(name);

        if (superClass() != null) {
            w.write(" extends ");
            print(superClass(), w, tr);
        }

        if (! interfaces.isEmpty()) {
            if (flags.isInterface()) {
                w.write(" extends ");
            }
            else {
                w.write(" implements ");
            }

            for (Iterator i = interfaces().iterator(); i.hasNext(); ) {
                TypeNode tn = (TypeNode) i.next();
                print(tn, w, tr);

                if (i.hasNext()) {
                    w.write (", ");
                }
            }
        }

        w.write(" {");
    }

    public void prettyPrintFooter(CodeWriter w, PrettyPrinter tr) {
        w.write("}");
        w.newline(0);
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        prettyPrintHeader(w, tr);
        print(body(), w, tr);
        prettyPrintFooter(w, tr);
    }

    public void dump(CodeWriter w) {
            super.dump(w);

            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(name " + name + ")");
            w.end();

            if (type != null) {
                    w.allowBreak(4, " ");
                    w.begin(0);
                    w.write("(type " + type + ")");
                    w.end();
            }
    }

    /**
     * @param parent
     * @param ar
     */
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar) throws SemanticException {
        // Don't do anything special for member classes; the disambiguation passes
        // for the container have already been run and visited this class.
        if (type.isMember()) {
            return null;
        }

        ClassDecl n = this;
        Node old = n;
        
        // Ensure supertypes and signatures are disambiguated for all
        // classes visible from this class's scope.
    
        // Check typesBelow to see if we need to disambiguate supertypes
        // and signatures.
        SupertypeDisambiguator supDisamb = new SupertypeDisambiguator(ar);
        n = (ClassDecl) supDisamb.visitEdgeNoOverride(parent, n);
        if (supDisamb.hasErrors()) throw new SemanticException();
    
        // Hack to force n.disambiguate() to be called and supertypes set.
        // n = (ClassDecl) new SupertypeDisambiguator(tc).leave(parent, old, n, new SupertypeDisambiguator(tc));
        SignatureDisambiguator sigDisamb = new SignatureDisambiguator(ar);
        n = (ClassDecl) sigDisamb.visitEdgeNoOverride(parent, n);
        if (sigDisamb.hasErrors()) throw new SemanticException();
        
        // Call enter and leave to manage the context.
        AmbiguityRemover childVisitor = (AmbiguityRemover) ar.enter(parent, n);
        if (ar.hasErrors()) throw new SemanticException();
    
        n = (ClassDecl) n.visitChildren(childVisitor);
        if (childVisitor.hasErrors()) throw new SemanticException();
    
        return ar.leave(parent, old, n, childVisitor);
    }
}
