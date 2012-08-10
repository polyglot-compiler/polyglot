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

import java.io.IOException;

/**
 * A <code>CodeWriter</code> is a pretty-printing engine. It formats
 * structured text onto an output stream <code>o</code> in the minimum number
 * of lines, while keeping the width of the output within <code>width</code>
 * characters if possible. Newlines occur in the output only at places where a
 * line break is permitted by the use of <code>allowBreak</code> and
 * <code>unifiedBreak</code>.
 * 
 * Line breaks can have different levels, which is useful for implementing
 * things like "miser mode" layout.
 */
public abstract class CodeWriter {
    /**
     * Print the string <code>s</code> verbatim on the output stream.
     * @param s the string to print.
     */
    public abstract void write(String s);

    /**
     * Print the string <code>s</code> on the output stream. Pretend that it
     * has width <code>length</code> even if it has a different number of
     * characters. This is useful when the string contains escape sequences,
     * HTML character entity references, etc.
     * 
     * @param s
     * @param length
     */
    public abstract void write(String s, int length);

    /**
     * Start a new block with a relative indentation of <code>n</code>
     * characters.
     */
    public abstract void begin(int n);

    /**
     * Terminate the most recent outstanding <code>begin</code>.
     */
    public abstract void end();

    /**
     * Insert a break (an optional newline). Indentation will be preserved.
     * Every break has a level. A level 0 break is always broken to form a
     * newline. The codewriter tries to avoid breaking higher-level breaks, and
     * the higher the level, the harder it tries.
     *
     * @param n
     *            indentation relative to the current block if the newline is
     *            inserted. Requires: n >= 0
     * @param level
     *            the level of the break. Requires: level >= 0
     * @param alt
     *            if no newline is inserted, the string <code>alt</code> is
     *            output instead. Requires: alt != null
     * @param altlen
     *            the length of 'alt' in characters
     */
    public abstract void allowBreak(int n, int level, String alt, int altlen);

    /**
     * Insert a unified break. Unified breaks act like the breaks inserted by
     * <code>allowBreak</code>, but unified breaks should also break if any break of
     * the same level in the same block is broken, whereas ordinary breaks do
     * not necessarily break in this case. That is, unified breaks act as if
     * they were slightly lower level than other breaks of the same level
     * (including other unified breaks!).
     * 
     * @param n
     *            the relative indentation
     * @param level
     *            the level of the break
     * @param alt
     *            the alternative text
     * @param altlen
     *            the length in characters that 'alt' will be treated as taking up.
     * @see polyglot.util.CodeWriter#allowBreak
     */
    public abstract void unifiedBreak(int n, int level, String alt, int altlen);

    /**
     * The most common use of "unifiedBreak": level 1, with an alternative of a
     * single space. 
     * 
     * @param n
     *            the indentation relative to the current block.
     */
    public void unifiedBreak(int n) {
        unifiedBreak(n, 1, " ", 1);
    }

    /**
     * The most common use of "allowBreak": level 1, with an alternative of a
     * single space. 
     * 
     * @param n
     *            the indentation relative to the current block.
     */
    public void allowBreak(int n) {
        allowBreak(n, 1, " ", 1);
    }

    public void allowBreak(int n, String alt) {
        allowBreak(n, 1, alt, 1);
    }

    /**
     * Force a newline. Indentation will be preserved. This method
     * should be used sparingly; a call to allowBreak() gives the
     * pretty-printer much more flexibility to do a good job.
     */
    public void newline() {
        newline(0, 1);
    }

    /**
     * Like newline(), but forces a newline with a specified indentation.
     */
    public void newline(int n) {
        newline(n, 1);
    }

    /**
     * newline with a specified indentation and level.
     */
    public abstract void newline(int n, int level);

    /**
     * Flush all formatted text to the underlying writer.
     * Returns true if formatting was completely successful (the
     * margins were obeyed).
     */
    public abstract boolean flush() throws IOException;

    /**
     * Flush all formatted text, reset formatter state, and
     * close the underlying writer.
     */
    public abstract void close() throws IOException;

    /** Like <code>flush</code>, but passing <code>format=false</code>
     * causes output to be generated in the fastest way possible, with
     * all breaks broken.
     * @param format whether to pretty-print the output
     * @return whether formatting was completely successful.
     * @throws IOException
     */
    public abstract boolean flush(boolean format) throws IOException;

    /**
     * Return a readable representation of all the structured input given to
     * the CodeWriter since the last flush.
     */
    @Override
    public abstract String toString();
}
