/*
 * Declaration.java
 * 
 * Author: nystrom
 * Creation date: Sep 20, 2005
 */
package polyglot.types;

/**
 * A Declaration is a type object that has declarations and uses. Some instances
 * may be uses of the declaration; these have references to the original
 * declaration. For example, extensions may perform substitutions on the
 * original declaration to produce the type object for a use of the declaration.
 * To make it easy to create distinct uses by copying the original declaration
 * object, copy() will preserve the pointer to the original declaration; it
 * won't update it to point to the copy. A Declaration used as a declaration has
 * a reference to itself.
 */
public interface Declaration extends TypeObject {
    /** Get the original declaration. */
    Declaration declaration();
    
    /** Set the original declaration. */
    void setDeclaration(Declaration decl);
}
