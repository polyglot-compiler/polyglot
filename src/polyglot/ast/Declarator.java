package polyglot.ast;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

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
    Type childExpectedType(Expr child, AscriptionVisitor av);

    /** 
     * Write the declarator to an output file. 
     */
    void prettyPrint(CodeWriter w, PrettyPrinter tr, boolean field);
	
}
