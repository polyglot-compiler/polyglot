package polyglot.types;

import polyglot.util.*;
import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.main.Report;
import java.util.*;

/**
 * A <code>ClassContextResolver</code> looks up type names qualified with a class name.
 * For example, if the class is "A.B", the class context will return the class
 * for member class "A.B.C" (if it exists) when asked for "C".
 */
public class ClassContextResolver extends AbstractAccessControlResolver {
    protected ClassType type;
    
    /**
     * Construct a resolver.
     * @param ts The type system.
     * @param type The type in whose context we search for member types.
     */
    public ClassContextResolver(TypeSystem ts, ClassType type) {
        super(ts);
        this.type = type;
    }
    
    public String toString() {
        return "(class-context " + type + ")";
    }
    
    /**
     * Find a type object in the context of the class.
     * @param name The name to search for.
     */
    public Named find(String name, ClassType accessor) throws SemanticException {
        if (Report.should_report(TOPICS, 2))
	    Report.report(2, "Looking for " + name + " in " + this);

        if (! StringUtil.isNameShort(name)) {
            throw new InternalCompilerError(
                "Cannot lookup qualified name " + name);
        }

        // Check if the name is for a member class.
        ClassType mt = null;

        Named m;

        String fullName = type.fullName() + "." + name;
        String rawName = ts.getTransformedClassName(type) + "$" + name;

        // First check the system resolver.
        m = ts.systemResolver().check(fullName);

        // Try the raw class file name.
        if (m == null) {
            m = ts.systemResolver().check(rawName);
        }

        // Check if the member was explicitly declared.
        if (m == null) {
            m = type.memberClassNamed(name);
        }

        // Go to disk, but only if there is no job for the type.
        // If there is a job, all members should be in the resolver
        // already.
        boolean useLoadedResolver = true;

        if (type instanceof ParsedTypeObject) {
            ParsedTypeObject pto = (ParsedTypeObject) type;
            if (pto.job() != null) {
                useLoadedResolver = false;
            }
        }

        if (m == null && useLoadedResolver) {
            try {
                m = ts.systemResolver().find(rawName);
            }
            catch (SemanticException e) {
                // Not found; will fall through to error handling code
            }
        }

        if (m instanceof ClassType) {
            mt = (ClassType) m;

            if (! mt.isMember()) {
                throw new SemanticException("Class " + mt +
                                            " is not a member class, " +
                                            " but was found in " + type + ".");
            }
            
            if (mt.outer() != type) {
                throw new SemanticException("Class " + mt +
                                            " is not a member class, " +
                                            " of " + type + ".");
            }
            
            if (! canAccess(mt, accessor)) {
                throw new SemanticException("Cannot access member type \"" + mt + "\".");
            }

            return mt;
        }
        
        // Collect all members of the super types.
        // Use a Set to eliminate duplicates.
        Set acceptable = new HashSet();
        
        if (type.superType() != null) {
            Type sup = type.superType();
            if (sup instanceof ClassType) {
                Resolver r = ts.classContextResolver((ClassType) sup, accessor);
                try {
                    Named n = r.find(name);
                    acceptable.add(n);
                }
                catch (SemanticException e) {
                }
            }
        }
        
        for (Iterator i = type.interfaces().iterator(); i.hasNext(); ) {
            Type sup = (Type) i.next();
            if (sup instanceof ClassType) {
                Resolver r = ts.classContextResolver((ClassType) sup, accessor);
                try {
                    Named n = r.find(name);
                    acceptable.add(n);
                }
                catch (SemanticException e) {
                }
            }
        }
        
        if (acceptable.size() == 0) {
            throw new NoClassException(name, type);
        }
        else if (acceptable.size() > 1) {
            Set containers = new HashSet(acceptable.size());
            for (Iterator i = acceptable.iterator(); i.hasNext(); ) {
                Named n = (Named) i.next();
                if (n instanceof MemberInstance) {
                    MemberInstance mi = (MemberInstance) n;
                    containers.add(mi.container());
                }
            }
            
            if (containers.size() == 2) {
                Iterator i = containers.iterator();
                Type t1 = (Type) i.next();
                Type t2 = (Type) i.next();
                throw new SemanticException("Member \"" + name +
                                            "\" of " + type + " is ambiguous; it is defined in both " +
                                            t1 + " and " + t2 + ".");
            }
            else {
                throw new SemanticException("Member \"" + name +
                                            "\" of " + type + " is ambiguous; it is defined in " +
                                            containers + ".");
            }
        }
        
        Named t = (Named) acceptable.iterator().next();
        
        if (Report.should_report(TOPICS, 2))
            Report.report(2, "Found member class " + t);
        
        return t;
    }

    protected boolean canAccess(Named n, ClassType accessor) {
        if (n instanceof MemberInstance) {
            return accessor == null || ts.isAccessible((MemberInstance) n, accessor);
        }
        return true;
    }
    
    /**
     * The class in whose context we look.
     */
    public ClassType classType() {
	return type;
    }

    private static final Collection TOPICS = 
            CollectionUtil.list(Report.types, Report.resolver);

}
