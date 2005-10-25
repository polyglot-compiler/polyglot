/*
 * AccessControlledResolver.java
 * 
 * Author: nystrom
 * Creation date: Oct 24, 2005
 */
package polyglot.types;

public interface AccessControlResolver extends Resolver {
    /**
     * Find a type object by name, checking if the object is accessible from the accessor class.
     * A null accessor indicates no access check should be performed.
     */
    public Named find(String name, ClassType accessor) throws SemanticException;
}
