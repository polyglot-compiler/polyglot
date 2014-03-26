package polyglot.ast;

import java.util.List;

import polyglot.types.SemanticException;
import polyglot.visit.ExceptionChecker;

/**
 * This interface allows extensions both to override and reuse functionality in Try_c.
 *
 */
public interface TryOps {

    /**
     * Construct an ExceptionChecker that is suitable for checking the try block of 
     * a try-catch-finally AST node. 
     * @param ec The exception checker immediately prior to the try block.
     * @return
     */
    ExceptionChecker constructTryBlockExceptionChecker(ExceptionChecker ec);

    /**
     * Perform exception checking of the try block of a try-catch-finally
     * AST node, using the supplied exception checker.
     * @param ec
     * @return
     * @throws SemanticException
     */
    Block exceptionCheckTryBlock(ExceptionChecker ec) throws SemanticException;

    /**
     * Perform exception checking of the catch blocks of a try-catch-finally
     * AST node, using the supplied exception checker.
     * 
     * @param ec
     * @return
     * @throws SemanticException
     */
    List<Catch> exceptionCheckCatchBlocks(ExceptionChecker ec)
            throws SemanticException;

    /**
     * Perform exception checking of the finally block of a try-catch-finally
     * AST node (if there is one), using the supplied exception checker.
     * 
     * @param ec
     * @return
     * @throws SemanticException
     */
    Block exceptionCheckFinallyBlock(ExceptionChecker ec)
            throws SemanticException;

}
