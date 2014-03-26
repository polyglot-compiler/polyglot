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
/**
 * CodeWriter -- Andrew C. Myers, April 2001
 * For use in Cornell University Computer Science CS 412/413
 */

package ppg.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import polyglot.util.SerialVersionUID;

/** 
 * A {@code CodeWriter} is a pretty-printing engine.
 * It formats structured text onto an output stream {@code o} in the
 * minimum number of lines, while keeping the width of the output
 * within {@code width} characters if possible.
 */
public class CodeWriter {
    /**
     * Create a CodeWriter object with output stream {@code o}
     * and width {@code width_}.
     */

    public CodeWriter(OutputStream o, int width_) {
        output = new OutputStreamWriter(o);
        width = width_;
        current = input = new Block(null, 0);
    }

    /**
     * Create a CodeWriter object with output {@code w} and
     * width {@code width_}.
     */
    public CodeWriter(Writer w, int width_) {
        output = w;
        width = width_;
        current = input = new Block(null, 0);
    }

    /** Print the string {@code s} verbatim on the output stream. */
    public void write(String s) {
        if (s.length() > 0) current.add(new StringItem(s));
    }

    /** Force a newline with no added indentation. */
    public void newline() {
        newline(0);
    }

    /**
     * Start a new block with a relative indentation of {@code n}
     * characters.
     * <br>
     * A block is a formatting unit. The formatting algorithm will try
     * to put the whole block in one line unless
     * <ul>
     * <li>there is a {@code newline} item in the block.</li>
     * <li>the block cannot fit in one line.</li>
     * </ul>
     * If either of the two conditions is satisfied, the
     * formatting algorithm will break the block into lines: every
     * {@code allowBreak} will cause a line change, the first line
     * is printed at the current cursor position {@code pos},
     * all the following lines are printed at the position
     * {@code pos+n}.
     * 
     * @param n the number of characters increased on indentation (relative
     * to the current position) for all lines in the block.
     */
    public void begin(int n) {
        Block b = new Block(current, n);
        current.add(b);
        current = b;
    }

    /** 
     * Terminate the most recent outstanding {@code begin}. 
     */
    public void end() {
        current = current.parent;
        if (current == null) throw new RuntimeException();
    }

    /**
     * Allow a newline. Indentation will be preserved.
     * If no newline is inserted, a single space character is output instead.
     *
     * @param n the amount of increase in indentation if
     * the newline is inserted.
     */
    public void allowBreak(int n) {
        current.add(new AllowBreak(n, " "));
    }

    /**
     * Allow a newline. Indentation will be preserved.
     *
     * @param n the amount of increase in indentation if
     *  the newline is inserted.
     * @param alt if no newline is inserted, the string {@code alt} is
     *  output instead.   
     */
    public void allowBreak(int n, String alt) {
        current.add(new AllowBreak(n, alt));
    }

    /**
     * Force a newline. Indentation will be preserved.  This method
     * should be used sparingly; usually a call to {@code allowBreak} is
     * preferable because forcing a newline also causes all breaks
     * in containing blocks to be broken.
     *
     * @param n the amount of increase in indentation after the newline.
     */
    public void newline(int n) {
        current.add(new Newline(n));
    }

    /**
     * Send out the current batch of text to be formatted. All
     * outstanding {@code begin}'s are closed and the current
     * indentation level is reset to 0. Returns true if formatting
     * was completely successful (the margins were obeyed).
     */
    public boolean flush() throws IOException {
        boolean success = true;
        try {
            Item.format(input, 0, 0, width, width, true, true);
        }
        catch (Overrun o) {
            success = false;
        }
        input.sendOutput(output, 0, 0);
        output.flush();
        input.free();
        current = input = new Block(null, 0);
        return success;
    }

    /**
     * Return a readable representation of all the structured input
     * given to the CodeWriter since the last flush.
     */

    Block input;
    Block current;

    Writer output;
    int width;
}

/**
 * An {@code Overrun} represents a formatting that failed because the right
 * margin was exceeded by at least {@code amount} chars.
 */
class Overrun extends Exception {
    private static final long serialVersionUID = SerialVersionUID.generate();
    int amount;

    Overrun(int amount_) {
        amount = amount_;
    }
}

/**
 * An {@code Item} is a piece of input handed to the formatter. It
 * contains a reference to a possibly empty list of items that follow it.
 */
abstract class Item {
    Item next;

    protected Item() {
        next = null;
    }

    /** 
     * Try to format this item and subsequent items. The current cursor
     * position is {@code pos}, left and right margins are as
     * specified. Returns the final position, which must be {@code < fin}.
     * If breaks may be broken, {@code can_break} is
     * set. Return the new cursor position (which may overrun rmargin,
     * fin, or both, and set any contained breaks accordingly.  (It is
     * important that formatN not necessarily convert overruns in its
     * final position into exceptions. This allows the calling routine
     * to distinguish between 'internal' overruns and ones that it can
     * tack on a conservative estimate of how much formatting the rest
     * of the list will make the overrun go up by.  Also, it simplifies
     * the coding of formatN.)
     *
     * Requires: rmargin &lt; lmargin, pos &lt;= rmargin.
     */

    abstract int formatN(int lmargin, int pos, int rmargin, int fin,
            boolean can_break, boolean nofail) throws Overrun;

    /**
     * Send the output associated with this item to {@code o}, using the
     * current break settings.
     */
    abstract int sendOutput(Writer o, int lmargin, int pos) throws IOException;

    /** Make the garbage collector's job easy: free references to any
        other items. */
    void free() {
        if (next != null) {
            next.free();
            next = null;
        }
    }

    /**
     * Try to format a whole sequence of items in the manner of formatN.
     * The initial position may be an overrun (this is the only way
     * that overruns are checked!) {@code it} may be also null,
     * signifying an empty list.
     */
    static int format(Item it, int lmargin, int pos, int rmargin, int fin,
            boolean can_break, boolean nofail) throws Overrun {
        if (!nofail && pos > rmargin) { // overrun
            throw new Overrun(pos - rmargin);
        }
        if (it == null) { // no items to format. Check against final position.
            if (!nofail && pos > fin) throw new Overrun(pos - fin);
            return pos;
        }
        return it.formatN(lmargin, pos, rmargin, fin, can_break, nofail);
    }
}

/**
 * A Block is a formatting unit containing a list of other items
 * to be formatted.
 */
class Block extends Item {
    Block parent;
    Item first;
    Item last;
    int indent;

    Block(Block parent_, int indent_) {
        parent = parent_;
        first = last = null;
        indent = indent_;
    }

    /**
     * Add a new item to the end of the block. Successive
     * StringItems are concatenated together to limit recursion
     * depth when formatting.
     */
    void add(Item it) {
        if (first == null) {
            first = it;
        }
        else {
            if (it instanceof StringItem && last instanceof StringItem) {
                StringItem lasts = (StringItem) last;
                lasts.appendString(((StringItem) it).s);
                return;
            }
            else {
                last.next = it;
            }
        }
        last = it;
    }

    @Override
    int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
            boolean nofail) throws Overrun {
        // "this_fin" is a final-position bound for the formatting of
        // the contained list, cranked in from the right margin
        // when subsequent items overrun.
        int this_fin = rmargin;
        // Keep track of whether to send "nofail" down to contained
        // list. Don't do this unless forced to.
        boolean this_nofail = false;
        // Keep track of whether to send "can_break" down to contained
        // list. Don't do this unless forced to.
        boolean this_break = false;
        while (true) {
            int next_pos;
            try {
                next_pos =
                        format(first,
                               pos + indent,
                               pos,
                               rmargin,
                               this_fin,
                               this_break,
                               this_nofail && this_break);
            }
            catch (Overrun o) {
                if (!can_break) throw o;
                if (!this_break) {
                    this_break = true;
                    continue;
                }
                if (nofail) {
                    this_nofail = true;
                    continue;
                }
                throw o;
            }
            try {
                return format(next,
                              lmargin,
                              next_pos,
                              rmargin,
                              fin,
                              can_break,
                              nofail);
            }
            catch (Overrun o) {
                if (!can_break) throw o; // no way to fix it
                if (next instanceof AllowBreak) throw o; // not our fault
                this_break = true;
                if (next_pos > this_fin) next_pos = this_fin;
                this_fin = next_pos - o.amount;
            }
        }
    }

    @Override
    int sendOutput(Writer o, int lmargin, int pos) throws IOException {
        Item it = first;
        lmargin = pos + indent;
        while (it != null) {
            pos = it.sendOutput(o, lmargin, pos);
            it = it.next;
        }
        return pos;
    }

    @Override
    void free() {
        super.free();

        parent = null;
        if (first != null) {
            first.free();
        }
        last = null;
    }
}

class StringItem extends Item {
    String s;

    StringItem(String s_) {
        s = s_;
    }

    @Override
    int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
            boolean nofail) throws Overrun {
        return format(next,
                      lmargin,
                      pos + s.length(),
                      rmargin,
                      fin,
                      can_break,
                      nofail);
    }

    @Override
    int sendOutput(Writer o, int lm, int pos) throws IOException {
        o.write(s);
        return pos + s.length();
    }

    void appendString(String s) {
        this.s = this.s + s;
    }
}

class AllowBreak extends Item {
    int indent;
    boolean broken = true;
    String alt;

    AllowBreak(int n_, String alt_) {
        indent = n_;
        alt = alt_;
    }

    @Override
    int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
            boolean nofail) throws Overrun {
        if (can_break) {
            pos = lmargin + indent;
            broken = true;
        }
        else {
            pos += alt.length();
            broken = false;
        }
        return format(next, lmargin, pos, rmargin, fin, can_break, nofail);
    }

    @Override
    int sendOutput(Writer o, int lmargin, int pos) throws IOException {
        if (broken) {
            o.write("\r\n");
            for (int i = 0; i < lmargin + indent; i++)
                o.write(" ");
            return lmargin + indent;
        }
        else {
            o.write(alt);
            return pos + alt.length();
        }
    }
}

class Newline extends AllowBreak {
    Newline(int n_) {
        super(n_, "");
    }

    @Override
    int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
            boolean nofail) throws Overrun {
        broken = true;
        if (!can_break) throw new Overrun(1);
        return format(next,
                      lmargin,
                      lmargin + indent,
                      rmargin,
                      fin,
                      can_break,
                      nofail);
    }

    @Override
    int sendOutput(Writer o, int lmargin, int pos) throws IOException {
        o.write("\r\n");
        for (int i = 0; i < lmargin + indent; i++)
            o.write(" ");
        return lmargin + indent;
    }
}
