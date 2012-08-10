/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.util;

/**
 * A <code>StdErrorQueue</code> handles outputing error messages.
 */
public abstract class AbstractErrorQueue implements ErrorQueue {
    protected boolean flushed;
    protected int errorCount;
    protected final int limit;
    protected final String name;

    public AbstractErrorQueue(int limit, String name) {
        this.errorCount = 0;
        this.limit = limit;
        this.name = name;
        this.flushed = true;
    }

    @Override
    public final void enqueue(int type, String message) {
        enqueue(type, message, null);
    }

    @Override
    public final void enqueue(int type, String message, Position position) {
        enqueue(new ErrorInfo(type, message, position));
    }

    @Override
    public final void enqueue(ErrorInfo e) {
        if (e.getErrorKind() != ErrorInfo.WARNING
                && e.getErrorKind() != ErrorInfo.DEBUG) {
            errorCount++;
        }

        flushed = false;

        displayError(e);

        if (errorCount >= limit) {
            tooManyErrors(e);
            flush();
            throw new ErrorLimitError();
        }
    }

    protected abstract void displayError(ErrorInfo error);

    /**
     * This method is called when we have had too many errors. This method
     * give subclasses the opportunity to output appropriate messages, or
     * tidy up.
     * 
     * @param lastError the last error that pushed us over the limit
     */
    protected void tooManyErrors(ErrorInfo lastError) {
    }

    /**
     * This method is called to flush the error queue. Subclasses may want to
     * print summary information in this method.
     */
    @Override
    public void flush() {
        flushed = true;
    }

    @Override
    public final boolean hasErrors() {
        return errorCount > 0;
    }

    @Override
    public final int errorCount() {
        return errorCount;
    }
}
