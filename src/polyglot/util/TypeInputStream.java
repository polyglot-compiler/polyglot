package polyglot.util;

import polyglot.main.Report;
import polyglot.types.*;

import java.util.*;
import java.io.*;

/** Input stream for reading type objects. */
public class TypeInputStream extends ObjectInputStream {
    protected TypeSystem ts;
    protected Map cache;
    protected boolean failed;
    
    public TypeInputStream(InputStream in, TypeSystem ts, Map cache) 
        throws IOException
    {
        super(in);
        enableResolveObject(true);
        this.ts = ts;
        this.cache = cache;
        this.failed = false;
    }
    
    public boolean deserializationFailed() {
        return failed;
    }
    
    public TypeSystem getTypeSystem() {
        return ts;
    }
    
    protected Object resolveObject(Object o) {
        String s = "";
        if (Report.should_report(Report.serialize, 2)) {
            try {
                s = o.toString();
            }
            catch (NullPointerException e) {
                s = "<NullPointerException thrown>";
            }
        }	  
        if (o instanceof PlaceHolder) {
            TypeObject t = (TypeObject) cache.get(o);
            if (t == null) {
                try {
                    t = ((PlaceHolder) o).resolve(ts);
                    cache.put(o, t);
                }
                catch (CannotResolvePlaceHolderException e) {
                    failed = true;              
                }
            }
            if (Report.should_report(Report.serialize, 2)) {
                Report.report(2, "- Resolving " + s + " : " + o.getClass()
                              + " to " + t + " : " + t.getClass());      	
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
