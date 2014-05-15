/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
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
