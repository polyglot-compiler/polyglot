/*
 * AccessControlWrapperResolver.java
 * 
 * Author: nystrom
 * Creation date: Oct 24, 2005
 */
package polyglot.types;

/** A Resolver that wraps an AccessControlResolver. */
public class AccessControlWrapperResolver implements Resolver {
    protected AccessControlResolver inner;
    protected ClassType accessor;
    
    public AccessControlWrapperResolver(AccessControlResolver inner, ClassType accessor) {
        this.inner = inner;
        this.accessor = accessor;
    }
    
    public Named find(String name) throws SemanticException {
        return inner.find(name, accessor);
    }
}
