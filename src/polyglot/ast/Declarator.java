package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

/**
 * Encapsulation of the details of a declaration of a 
 * variable (field or local).
 */
public interface Declarator extends Copy {
    
    /** 
     * Get the flags of the declarator. 
     */
    Flags flags();
	
	/**
	 * Set the flags of the declarator.
	 */
    Declarator flags(Flags flags);
	
    /** 
     * Get the type of the declarator. 
     */
    Type declType();

    /**
     * Get the type node of the declarator.
	 */
    TypeNode type();

    /** 
     * Set the type node of the declarator. 
     */
    Declarator type(TypeNode type);

    /** 
     * Get the name of the declarator. 
     */
    String name();

    /** 
     * Set the name of the declarator. 
     */
    Declarator name(String name);

    /** 
     * Get the initializer of the declarator. 
     */
    Expr init();

    /** 
     * Set the initializer of the declarator. 
     */
    Declarator init(Expr init);
	
    /** 
      * Type check the declarator. 
      */
    void typeCheck(TypeChecker tc) throws SemanticException;
	
    /** 
      * Type check a child of the declarator. 
      */
    Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
          throws SemanticException;

    /** 
      * Write the declarator to an output file. 
      */
    void translate(CodeWriter w, Translator tr, boolean field);
	
}
