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

import java.io.PrintWriter;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Stack;

/**
 * SimpleCodeWriter is a simple, fast, bulletproof implementation of the
 * CodeWriter interface. However, it does not try very hard to use vertical
 * space optimally or to stay within the horizontal margins. If aesthetically
 * appealing output is desired, use OptimalCodeWriter.
 */
public class SimpleCodeWriter extends CodeWriter {
    protected PrintWriter output;
    protected int width;
    protected int rmargin;
    protected int lmargin;
    protected boolean breakAll;
    protected Stack<State> lmargins;
    protected int pos;

    public SimpleCodeWriter(OutputStream o, int width_) {
        this(new PrintWriter(new OutputStreamWriter(o)), width_);
    }

    protected class State {
        public int lmargin;
        public boolean breakAll;

        State(int m, boolean b) {
            lmargin = m;
            breakAll = b;
        }
    }

    public SimpleCodeWriter(PrintWriter o, int width_) {
        output = o;
        width = width_;
        rmargin = width;
        adjustRmargin();
        breakAll = false;
        pos = 0;
        lmargins = new Stack<State>();
    }

    public SimpleCodeWriter(Writer o, int width_) {
        this(new PrintWriter(o), width_);
    }

    private void adjustRmargin() {
        rmargin -= 8;
        if (rmargin < width / 2) rmargin = width / 2;
    }

    @Override
    public void write(String s) {
        if (s == null)
            write("null", 4);
        else if (s.length() > 0) write(s, s.length());
    }

    @Override
    public void write(String s, int length) {
        output.print(s);
        pos += length;
    }

    @Override
    public void begin(int n) {
        lmargins.push(new State(lmargin, breakAll));
        lmargin = pos + n;
    }

    @Override
    public void end() {
        State s = lmargins.pop();
        lmargin = s.lmargin;
        breakAll = s.breakAll;
    }

    @Override
    public void allowBreak(int n, int level, String alt, int altlen) {
        if (pos > width) adjustRmargin();
        if (breakAll || pos > rmargin) {
            newline(n, 1);
            breakAll = true;
        }
        else {
            output.print(alt);
            pos += altlen;
        }
    }

    @Override
    public void unifiedBreak(int n, int level, String alt, int altlen) {
        allowBreak(n, level, alt, altlen);
    }

    private void spaces(int n) {
        for (int i = 0; i < n; i++) {
            output.print(' ');
        }
    }

    @Override
    public void newline() {
        if (pos != lmargin) {
            output.println();
            pos = lmargin;
            spaces(lmargin);
        }
    }

    @Override
    public void newline(int n, int level) {
        newline();
        spaces(n);
        pos += n;
    }

    @Override
    public boolean flush() throws IOException {
        output.flush();
        pos = 0;
        return true;
    }

    @Override
    public boolean flush(boolean format) throws IOException {
        return flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        output.close();
    }

    /**
     * toString is not really supported by this implementation.
     */
    @Override
    public String toString() {
        return "<SimpleCodeWriter>";
    }
}
