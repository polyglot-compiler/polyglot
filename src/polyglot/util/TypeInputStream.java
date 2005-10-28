package polyglot.util;

import polyglot.main.Report;
import polyglot.types.*;

import java.util.*;
import java.io.*;

/** Input stream for reading type objects. */
public class TypeInputStream extends ObjectInputStream {
    protected TypeSystem ts;
    protected Map cache;
    protected Set unresolved;
    protected boolean failed;
    boolean usedUnresolved;
    boolean enableReplace;
    
    public TypeInputStream(InputStream in, TypeSystem ts, Map cache, Set unresolved) 
        throws IOException
    {
        super(in);

        enableResolveObject(true);

        this.ts = ts;
        this.cache = cache;
        this.unresolved = unresolved;
        this.failed = false;
        this.usedUnresolved = false;
        this.enableReplace = true;
    }
    
    public boolean deserializationFailed() {
        return failed;
    }

    public boolean usedUnresolved() {
        return usedUnresolved;
    }
    
    public TypeSystem getTypeSystem() {
        return ts;
    }
    
    final static Object UNRESOLVED = new Object();
    
    public void installInPlaceHolderCache(PlaceHolder p, TypeObject t) {
        cache.put(p, t);
        
        String s = "";
        if (Report.should_report(Report.serialize, 2)) {
            try {
                s = t.toString();
            }
            catch (NullPointerException e) {
                s = "<NullPointerException thrown>";
            }
        }
        
        if (Report.should_report(Report.serialize, 2)) {
            Report.report(2, "- Installing " + p
                          + " -> " + s + " in place holder cache");         
        }
    }
    
    public void enableReplace(boolean f) {
        this.enableReplace = f;
    }

    protected Object resolveObject(Object o) {
        if (! enableReplace) {
            return o;
        }
        String s = "";
        if (Report.should_report(Report.serialize, 2)) {
            try {
                s = o.toString();
            }
            catch (NullPointerException e) {
                s = "<NullPointerException thrown>";
            }
        }	  

        if (! enableReplace) {
            return o;
        }
        
        if (o instanceof PlaceHolder) {
            Object t = cache.get(o);
            if (t == UNRESOLVED) {
                // A place holder lower in the call stack is trying to resolve
                // this place holder too.  Abort!
                // The calling place holder should set up depedencies to ensure
                // this pass is rerun.
                failed = true;
                return null;
            }
            else if (t == null) {
                try {
                    cache.put(o, UNRESOLVED);
                    t = ((PlaceHolder) o).resolve(ts);
                    if (t == null) {
                        throw new InternalCompilerError("Resolved " + s + " to null.");
                    }
                    cache.put(o, t);
                    if (Report.should_report(Report.serialize, 2)) {
                        Report.report(2, "- Resolving " + s + " : " + o.getClass()
                                      + " to " + t + " : " + t.getClass());      	
                    }
                }
                catch (CannotResolvePlaceHolderException e) {
                    failed = true;              
                    if (Report.should_report(Report.serialize, 2)) {
                        Report.report(2, "- Resolving " + s + " : " + o.getClass()
                                      + " to " + e);      	
                    }
                }
            }
            else {
                if (Report.should_report(Report.serialize, 2)) {
                    Report.report(2, "- Resolving " + s + " : " + o.getClass()
                                  + " to (cached) " + t + " : " + t.getClass());      	
                }

                if (t instanceof Named) {
                    Named n = (Named) t;
                    if (unresolved.contains(n.fullName())) {
                        usedUnresolved = true;
                    }
                }
            }
            return t;
        }
        else if (o instanceof Enum) {
            if (Report.should_report(Report.serialize, 2)) {    
                Report.report(2, "- Interning " + s + " : " + o.getClass());
            }
            return ((Enum) o).intern();
        }
        else {
            if (Report.should_report(Report.serialize, 2)) {    
                Report.report(2, "- " + s + " : " + o.getClass());
            }
            return o;
        }
    }
}
