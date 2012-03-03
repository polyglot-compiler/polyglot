package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.*;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.types.*;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5EnumDecl_c extends JL5ClassDecl_c implements JL5EnumDecl {

	public JL5EnumDecl_c(Position pos, Flags flags, Id name,
			TypeNode superClass, List interfaces, ClassBody body) {
		super(pos, flags, name, superClass, interfaces, body);
	}
	
	
	@Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        // figure out if this should be an abstract type.
	    // need to do this before any anonymous subclasses are typechecked.
        for (Iterator it = type().methods().iterator(); it.hasNext();) {
            MethodInstance mi = (MethodInstance) it.next();
            if (!mi.flags().isAbstract())
                continue;
            
            // mi is abstract! First, mark the class as abstract.
            type().setFlags(type().flags().Abstract());
        }
        return super.typeCheckEnter(tc);
    }


    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (flags().isAbstract()) {
            throw new SemanticException("Enum types cannot have abstract modifier", this.position());
        }
        if (flags().isPrivate() && !type().isInnerClass()) {
            throw new SemanticException("Top level enum types cannot have private modifier", this.position());
        }
        if (flags().isFinal()) {
            throw new SemanticException("Enum types cannot have final modifier", this.position());
        }
        
        for (Iterator it = type().constructors().iterator(); it.hasNext();) {
            ConstructorInstance ci = (ConstructorInstance) it.next();
            if (!ci.flags().clear(Flags.PRIVATE).equals(Flags.NONE)) {
                throw new SemanticException("Modifier " + ci.flags().clear(Flags.PRIVATE)
                        + " not allowed here", ci.position());
            }
        }
        
        ClassDecl n = (ClassDecl) super.typeCheck(tc);
        if (n.type().isMember()) {
        	// it's a nested class
        	n = this.flags(this.flags().Static());
        	n.type().flags(n.type().flags().Static());
        }
        
        for (ClassMember m : (List<ClassMember>)this.body().members()) {
            if (m.memberInstance().flags().isAbstract()) {
                n = this.flags(this.flags().Abstract());
                n.type().flags(n.type().flags().Abstract());                
                break;
            }
        }
        
        return n;
	}
	
    @Override
	protected Node addDefaultConstructorIfNeeded(TypeSystem ts, NodeFactory nf) throws SemanticException {
    	JL5EnumDecl_c n = (JL5EnumDecl_c) super.addDefaultConstructorIfNeeded(ts, nf);
    	return n.addEnumMethodsIfNeeded(ts, nf);
    }
    

	@Override
	protected Node addDefaultConstructor(TypeSystem ts, NodeFactory nf) throws SemanticException {
        ConstructorInstance ci = this.defaultCI;
        if (ci == null) {
            throw new InternalCompilerError("addDefaultConstructor called without defaultCI set");
        }

        // insert call to appropriate super constructor
		List args = new ArrayList(2);
		args.add(nf.NullLit(Position.compilerGenerated()));// XXX the right thing to do is change the type of java.lang.Enum instead of adding these dummy params
		args.add(nf.IntLit(Position.compilerGenerated(), IntLit.INT, 0));
        Block block = nf.Block(position().startOf(),
        		((JL5NodeFactory)nf).ConstructorCall(position.startOf(), ConstructorCall.SUPER, null, args, true));
        

        //Default constructor of an enum is private 
        ConstructorDecl cd = nf.ConstructorDecl(body().position().startOf(),
                                                Flags.PRIVATE,
                                                name, Collections.EMPTY_LIST,
                                                Collections.EMPTY_LIST,
                                                block);
        cd = (ConstructorDecl) cd.constructorInstance(ci);
        return body(body.addMember(cd));

	}
    
    private Node addEnumMethodsIfNeeded(TypeSystem ts, NodeFactory nf) {
        if (enumMethodsNeeded()) {
            return addEnumMethods(ts, nf);
        }
        return this;
	}
    
    private boolean enumMethodsNeeded() { 
    	boolean valueOfMethodFound = false;
    	boolean valuesMethodFound = false;
        // We added it to the type, check if it's in the class body.
        for (Iterator i = this.type.members().iterator(); i.hasNext(); ) {
            MemberInstance mi = (MemberInstance) i.next();
            if (mi instanceof MethodInstance) {
            	MethodInstance md = (MethodInstance) mi;
                if (md.name().equals("valueOf")) {
                	valueOfMethodFound = true;
                }
                if (md.name().equals("values")) {
                	valuesMethodFound = true;
                }
            }
        }

        return !(valueOfMethodFound && valuesMethodFound);
    	
    }
    
    protected Node addEnumMethods(TypeSystem ts, NodeFactory nf) {
        ClassBody newBody = body();
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add values method
        JL5MethodInstance valuesMI = (JL5MethodInstance) ts.methodInstance(position(), this.type(), flags, ts.arrayOf(this.type()), "values", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        this.type.addMethod(valuesMI);


        // add valueOf method
        JL5MethodInstance valueOfMI = (JL5MethodInstance)ts.methodInstance(position(), this.type(), flags, this.type(), "valueOf", 
        		Collections.singletonList(ts.String()), 
        		Collections.EMPTY_LIST);
        this.type.addMethod(valueOfMI);

        return this;
    }
    
    @Override
	public void prettyPrintModifiers(CodeWriter w, PrettyPrinter tr) {
    	w.write(flags.clearAbstract().translate());
		// do not write out class
	}        
}
