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
package ppg.parse;

import polyglot.util.SerialVersionUID;
import ppg.lex.Token;

/** 
 * This error is thrown when the parser has an internal error -- the user should not see these
 * in the ideal case -- ex: we have a null somewhere.
 * If there is a problem with the source, either a syntaxError or SemanticError will be thrown, 
 * depending on nature of the error
 */
public class ParserError extends Exception {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * This contains the errorMessage for that caused the exception 
     */
    protected String errorMessage;

    /**
     * @param message The massage that contains a description of the error	 
     */
    public ParserError(String message) {
        errorMessage = message;
    }

    /**
     * @param file The file where the error came from.
     * @param msg The message that contains a description of the error.
     * @param tok Token from which to get the line number and the text
         *            of the error token.
     */
    public ParserError(String file, String msg, Token tok) {
        //errorMessage = file+ ":" +tok.lineNumber() + ": at " +tok.tokenText+ " :" +msg;	
    }

    /**
     * In rare cases when no error message is know return a generic message	 
     */
    public ParserError() {
        this("There is a parse error in your code...");
    }

    /**
     * @return String that is the message of the error  
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
