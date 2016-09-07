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

package polyglot.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The pretty-printing algorithm is loosely based on the Modula-3
 * pretty-printer, and on notes by Greg Nelson. It was extended to support
 * breaks at multiple levels.
 *
 * OptimalCodeWriter follows the "break from root" rule: if a break is broken,
 * breaks of equal or lower level in all containing blocks must also be
 * broken, and breaks in the same block must also be broken if they are
 * of strictly lower level or if they are of the same level but marked as
 * "unified".
 */
public class OptimalCodeWriter extends CodeWriter {
    /**
     * Create a OptimalCodeWriter object with output stream {@code o}
     * and width {@code width_}.
     * @param o the writer to write to. Must be non-null.
     * @param width_ the formatting width. Must be positive.
     */
    public OptimalCodeWriter(OutputStream o, int width_) {
        this(new PrintWriter(new OutputStreamWriter(o)), width_);
    }

    /**
     * Create a OptimalCodeWriter object.
     * @param o the writer to write to. Must be non-null.
     * @param width_ the formatting width. Must be positive.
     */
    public OptimalCodeWriter(PrintWriter o, int width_) {
        output = o;
        width = width_;
        current = input = new BlockItem(0);
        if (OptimalCodeWriter.showInput) {
            trace("new OptimalCodeWriter: width = " + width);
        }
    }

    /**
     * Create a OptimalCodeWriter object.
     * @param o the writer to write to. Must be non-null.
     * @param width_ the formatting width. Must be positive.
     */
    public OptimalCodeWriter(Writer o, int width_) {
        this(new PrintWriter(o), width_);
    }

    @Override
    public void write(String s) {
        if (s.length() > 0) write(s, s.length());
    }

    @Override
    public void write(String s, int length) {
        if (OptimalCodeWriter.showInput) {
            trace("write '" + s + "' (" + length + ")");
        }
        current.add(new TextItem(s, length));
    }

    protected List<BlockItem> blockStack = new LinkedList<>();

    /**
     * Start a new block with a relative indentation of {@code n}
     * characters.
     * <p>
     * A block is a formatting unit. The formatting algorithm will try to put
     * the whole block in one line unless
     * <ul>
     * <li>there is a level-0 break in the block, or</li>
     * <li>the block cannot fit in one line.</li>
     * </ul>
     * <p>
     * If either of the two conditions is satisfied, the formatting algorithm
     * will break the block into lines by generating newlines for some of the
     * inserted breaks. The first line is printed at the current cursor
     * position {@code pos}, all the following lines are printed at the
     * position {@code pos+n}.
     *
     * @param n
     *            the number of characters increased on indentation (relative
     *            to the current position) for all lines in the block.
     *            Requires: n >= 0.
     */
    @Override
    public void begin(int n) {
        if (OptimalCodeWriter.showInput) {
            trace("begin " + n);
            incIndent();
        }
        BlockItem b = new BlockItem(n);
        current.add(b);
        blockStack.add(0, current);
        current = b;
    }

    /**
     * Terminate the most recent outstanding {@code begin}.
     */
    @Override
    public void end() {
        if (OptimalCodeWriter.showInput) {
            decIndent();
            trace("end");
        }
        if (blockStack.isEmpty())
            throw new InternalCompilerError("Mismatched blocks");
        current = blockStack.remove(0);
    }

    @Override
    public void allowBreak(int n, int level, String alt, int altlen) {
        if (OptimalCodeWriter.showInput) {
            trace("allowBreak " + n + " level=" + level);
        }
        current.add(new AllowBreak(n, level, alt, altlen, false));
    }

    /** @see polyglot.util.CodeWriter#unifiedBreak */
    @Override
    public void unifiedBreak(int n, int level, String alt, int altlen) {
        if (OptimalCodeWriter.showInput) {
            trace("unifiedBreak " + n + " level=" + level);
        }
        current.add(new AllowBreak(n, level, alt, altlen, true));
    }

    /**
     * Like newline(), but forces a newline with a specified indentation.
     */
    @Override
    public void newline(int n, int level) {
        if (OptimalCodeWriter.showInput) {
            trace("newline " + n);
        }
        current.add(new Newline(n, level));
    }

    /**
     * Send out the current batch of text to be formatted. All outstanding
     * {@code begin}'s are closed and the current indentation level is
     * reset to 0. Returns true if formatting was completely successful (the
     * margins were obeyed).
     */
    @Override
    public boolean flush() throws IOException {
        return flush(true);
    }

    /** Like {@code flush}, but passing {@code format=false}
     * causes output to be generated in the fastest way possible, with
     * all breaks broken.
     * @param format whether to pretty-print the output
     * @return whether formatting was completely successful.
     * @throws IOException
     */
    @Override
    public boolean flush(boolean format) throws IOException {
        if (OptimalCodeWriter.showInput) {
            trace("flush");
        }
        if (!blockStack.isEmpty())
            throw new InternalCompilerError("Mismatched blocks");
        boolean success = true;
        format_calls = 0;

        Map<AllowBreak, Boolean> brkAssignment;
        if (format)
            brkAssignment = OCItem.format(input, width);
        else brkAssignment = Collections.emptyMap();
        input.sendOutput(output, 0, 0, brkAssignment);

        output.flush();
        if (OptimalCodeWriter.debug) {
            System.err.println("Total calls to format = " + format_calls);
            System.err.flush();
        }
        current = input = new BlockItem(0);
        return success;
    }

    @Override
    public void close() throws IOException {
        flush();
        output.close();
    }

    /**
     * Return a readable representation of all the structured input given to
     * the CodeWriter since the last flush.
     */
    @Override
    public String toString() {
        return input.toString();
    }

    protected BlockItem input;
    protected BlockItem current;

    protected static OCItem top;

    protected PrintWriter output;
    protected int width;
    protected static int format_calls = 0;
    public static final boolean debug = false; // show every step
    public static final boolean showInput = false; // show input
    public static final boolean visualize = false; // visualize formatting
    // (requires VT100 terminal)

    public static final boolean precompute = true; // use memoization

    // Debugging methods

    /** Amount to indent during tracing. */
    protected int trace_indent = 0;

    /** Increment tracing indentation. */
    void incIndent() {
        trace_indent++;
    }

    /** Decrement tracing indentation. */
    void decIndent() {
        trace_indent--;
        if (trace_indent < 0) throw new RuntimeException("unmatched end");
    }

    /** Print a debug message. */
    void trace(String s) {
        for (int i = 0; i < trace_indent; i++)
            System.out.print(" ");
        System.out.println(s);
    }

}

class ConsList<T> {
    static <T> ConsList<T> empty() {
        return new ConsList<>();
    }

    static <T> ConsList<T> cons(T elem, ConsList<T> next) {
        return new ConsList<>(elem, next);
    }

    T elem;
    ConsList<T> next;

    private ConsList() {
    }

    private ConsList(T elem, ConsList<T> next) {
        this.elem = elem;
        this.next = next;
    }

    boolean isEmpty() {
        return next == null;
    }

    private int length = -1;

    int length() {
        if (length == -1) length = next == null ? 0 : 1 + next.length();
        return length;
    }

    String toStringAux() {
        if (next == null) return "";
        return elem + ", " + next.toStringAux();
    }

    @Override
    public String toString() {
        return "[" + toStringAux() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConsList<?>) {
            ConsList<?> l = (ConsList<?>) o;
            if (length() != l.length()) return false;
            if (elem == null || l.elem == null) {
                if (elem != l.elem) return false;
            }
            else if (!elem.equals(l.elem)) return false;
            if (next == null || l.next == null) return next == l.next;
            return next.equals(l.next);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        if (next == null) hc = next.hashCode() * 31;
        return hc + elem.hashCode();
    }
}

class SearchState implements Cloneable {

    int lmargin, rmargin, pos;
    int minbr, minbu, minbo, maxbr, maxbi;
    boolean forward;
    boolean findminovf;
    int minovf;

    Map<AllowBreak, Boolean> brkAssignment;
    ConsList<Boolean> afterBrkAssignment;

    ConsList<BlockItem> blks;
    ConsList<Integer> lmargins, rmargins;
    ConsList<Integer> minbrs, minbus, minbos, maxbrs, maxbis;

    AllowBreak it;
    SearchState prevBreak;

    SearchState(int lmargin, int rmargin, int pos, int minbr, int minbu,
            int minbo, int maxbr, int maxbi) {
        this.lmargin = lmargin;
        this.rmargin = rmargin;
        this.pos = pos;
        this.minbr = minbr;
        this.minbu = minbu;
        this.minbo = minbo;
        this.maxbr = maxbr;
        this.maxbi = maxbi;
        forward = true;
        findminovf = false;
        brkAssignment = new LinkedHashMap<>();
        afterBrkAssignment = ConsList.empty();

        blks = ConsList.empty();
        lmargins = ConsList.empty();
        rmargins = ConsList.empty();
        minbrs = ConsList.empty();
        minbus = ConsList.empty();
        minbos = ConsList.empty();
        maxbrs = ConsList.empty();
        maxbis = ConsList.empty();
    }

    void pushBlock(BlockItem it) {
        blks = ConsList.cons(it, blks);
        lmargins = ConsList.cons(lmargin, lmargins);
        rmargins = ConsList.cons(rmargin, rmargins);
        minbrs = ConsList.cons(minbr, minbrs);
        minbus = ConsList.cons(minbu, minbus);
        minbos = ConsList.cons(minbo, minbos);
        maxbrs = ConsList.cons(maxbr, maxbrs);
        maxbis = ConsList.cons(maxbi, maxbis);
    }

    BlockItem popBlock() {
        // Restore search parameters.
        BlockItem result = blks.elem;
        blks = blks.next;
        lmargin = lmargins.elem;
        lmargins = lmargins.next;
        rmargin = rmargins.elem;
        rmargins = rmargins.next;
        int outerminbr = minbrs.elem;
        minbrs = minbrs.next;
        int outerminbu = minbus.elem;
        minbus = minbus.next;
        // The min break levels are the max required levels so far.
        minbr = minbu = minbo;
        if (minbr < outerminbr) minbr = outerminbr;
        if (minbu < outerminbu) minbu = outerminbu;
        minbo = minbos.elem;
        minbos = minbos.next;
        maxbr = maxbrs.elem;
        maxbrs = maxbrs.next;
        maxbi = maxbis.elem;
        maxbis = maxbis.next;
        return result;
    }

    SearchState copy() {
        try {
            SearchState s = (SearchState) clone();
            return s;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone weirdness", e);
        }
    }
}

/**
 * An {@code OCItem} is a piece of input handed to the formatter. It
 * contains a reference to a possibly empty list of items that follow it.
 */
abstract class OCItem {
    /** next is null if this is the last item in the list. */
    OCItem next;

    protected OCItem() {
        next = null;
    }

    /**
     * Try to format a whole sequence of items in the manner of formatN. Unlike
     * for formatN, The initial position may be an overrun (this is the only
     * way that overruns are checked!). The item {@code it} may be also
     * null, signifying an empty list. Requires: lmargin &lt; rmargin, pos &le;
     * rmargin, lmargin &ge; 0.
     *
     * @see formatN
     */
    static Map<AllowBreak, Boolean> format(OCItem it, int rmargin) {
        SearchState s =
                new SearchState(0,
                                rmargin,
                                0,
                                0,
                                0,
                                0,
                                Integer.MAX_VALUE,
                                Integer.MAX_VALUE);
        for (OCItem cur = it; cur != null;) {
            OptimalCodeWriter.format_calls++;
            if (OptimalCodeWriter.debug) {
                if (cur != OptimalCodeWriter.top) {
                    System.err.println("SNAPSHOT:");
                    PrintWriter w =
                            new PrintWriter(new OutputStreamWriter(System.err));
                    cur.sendOutput(w,
                                   0,
                                   0,
                                   Collections.<AllowBreak, Boolean> emptyMap());
                    w.write("<END>\n");
                    w.flush();
                }
                System.err.println("Format: " + cur + "\n  lmargin = "
                        + s.lmargin + " pos = " + s.pos + " max break levels: "
                        + s.maxbr + "/" + s.maxbi + " min break levels: "
                        + s.minbr + "/" + s.minbu);

                System.err.flush();
            }
            cur.selfFormat(s);
            if (s.forward) {
                if (cur instanceof BlockItem) {
                    BlockItem bi = (BlockItem) cur;
                    cur = bi.first;
                }
                else cur = cur.next;
                while (cur == null && !s.blks.isEmpty()) {
                    // Retrieve next item in the outer block.
                    cur = s.popBlock().next;
                }
            }
            else {
                SearchState prev = s.prevBreak;
                cur = prev.it;
                // Restore search parameters.
                s.lmargin = prev.lmargin;
                s.rmargin = prev.rmargin;
                s.pos = prev.pos;
                s.minbr = prev.minbr;
                s.minbu = prev.minbu;
                s.minbo = prev.minbo;
                s.maxbr = prev.maxbr;
                s.maxbi = prev.maxbi;
                s.blks = prev.blks;
                s.lmargins = prev.lmargins;
                s.rmargins = prev.rmargins;
                s.minbrs = prev.minbrs;
                s.minbus = prev.minbus;
                s.minbos = prev.minbos;
                s.maxbrs = prev.maxbrs;
                s.maxbis = prev.maxbis;
            }
        }
        return s.brkAssignment;
    }

    /**
     * Try to format this item.
     *
     * @param lmargin
     *            is the current left margin.
     * @param pos
     *            is the current cursor position.
     * @param rmargin
     *            is the current right margin.
     * @param fin
     *            is a bound on the final cursor position after formatting the
     *            whole item.
     * @param maxLevel
     *            is the maximum level at which breaks may be broken in the
     *            current block.
     * @param maxLevelInner
     *            is the maximum level at which breaks may be broken in nested
     *            blocks.
     * @param minLevel
     *            is the minimum level at which breaks must be broken.
     * @param minLevelUnified
     *            is the minimum level at which unified breaks must be broken.
     *
     * <p>
     * Breaks may be broken up to level maxLevel, which is set whenever a break
     * is not broken. Not breaking an ordinary break means that equal or
     * higher-level breaks in all contained blocks must not be broken either,
     * and breaks of strictly higher level in the same block must not be
     * broken.  Not breaking a unified break means that breaks of the same
     * level in the same block must not also be broken. The parameter
     * maxLevelInner controls the maxLevel in nested blocks; it is equal to
     * either maxLevel or maxLevel-1.
     *
     * <p>
     * <dl>
     * <dt>Example 1:
     * <dd>Suppose we have a current maxLevel of 4, and an ordinary,
     * non-unified break of level 2 is not broken. Then for that block,
     * maxLevel is set to 2 and maxLevelInner is set to 1. This permits further
     * breaks of level 1 or 2 in the same block, but only level-1 breaks in
     * inner blocks.
     *
     * <dt>Example 2:</dt>
     * <dd>Suppose we have a current maxLevel of 4, and a unified break of
     * level 2 is not broken. Then for that block, maxLevel and maxLevelInner
     * are set to 1. This permits no breaks in this block or in any nested
     * blocks.</dd>
     * </dl>
     *
     * <p>
     * When a break is broken in a nested block, it means that all equal or
     * higher-level breaks in containing blocks must be broken. However, these
     * breaks may be encountered after the nested block. The parameter
     * {@code minLevel} is used to communicate the level of breaks broken
     * in nested blocks (and earlier in the current block). Any break of level
     * <= minLevel <em>must</em> be broken. The parameter
     * {@code minLevelUnified} is the minimum level at which unified breaks must
     * be broken.  minLevelUnified is equal to either minLevel or minLevel+1.
     * </p>
     *
     * <dl>
     * <dt>Example 3:
     * <dd>Suppose we have a current maxLevel of 4, and a break of level 2 is
     * broken. Then for its block, minLevel is at least 1, and minLevelUnified
     * is at least 2. For containing blocks, minLevel is at least 2.</dd>
     * </dl>
     *
     * <b>Note: </b> It is important that formatN not necessarily convert
     * overruns in its final position into exceptions. This allows the calling
     * routine to distinguish between 'internal' overruns and ones that it can
     * tack on a conservative estimate of how much formatting the rest of the
     * list will make the overrun go up by. Also, it simplifies the coding of
     * formatN.
     * </p>
     *
     * Requires: rmargin &lt; lmargin, pos &lt;= rmargin, lmargin &lt; rmargin,
     * pos &le; rmargin, lmargin &ge; 0
     */
    abstract void selfFormat(SearchState s);

    /**
     * Send the output associated with this item to {@code out}, using the
     * given break settings.
     *
     * @param success
     */
    abstract int sendOutput(PrintWriter out, int lmargin, int pos,
            Map<AllowBreak, Boolean> brkAssignment);

    public String summarize(String s) {
        if (s.length() <= 79) return s;
        return s.substring(0, 76) + "...";
    }

    @Override
    public String toString() {
        if (next == null) return summarize(selfToString());
        return summarize(selfToString() + next.toString());
    }

    abstract String selfToString();

    /**
     * Returns an integer array of length at least 3 such that
     * - [0] is the minimum break level that any break in the containing block
     *   of the containing block of this item must break
     * - [1] is the minimum break level that any break in the containing block
     *   of this item must break
     * - [2] is the minimum break level that any unified break in the containing
     *   block of this item must break
     */
    abstract int[] minBreakLevels();
}

/** A simple string. */
class TextItem extends OCItem {
    String s; //@ invariant s != null
    int length;

    TextItem(String s_, int length_) {
        s = s_;
        length = length_;
    }

    @Override
    void selfFormat(SearchState s) {
        int rpos = s.pos + length;
        if (s.findminovf && rpos > s.rmargin) {
            // If break assignments causing minimal overflow is being sought,
            // and this item overflows, backtrack.
            s.forward = false;
            s.minovf = rpos - s.rmargin;
        }
        else {
            // Otherwise, all preceding break assignments have done their best jobs,
            // so move forward.
            s.pos = rpos;
        }
    }

    @Override
    int sendOutput(PrintWriter o, int lmargin, int pos,
            Map<AllowBreak, Boolean> brkAssignment) {
        o.write(s);
        return pos + length;
    }

    int[] minBreakLevels = { 0, 0, 0 };

    @Override
    int[] minBreakLevels() {
        return minBreakLevels;
    }

    @Override
    String selfToString() {
        java.io.StringWriter sw = new java.io.StringWriter();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ' ')
                sw.write("\\ ");
            else sw.write(c);
        }
        return sw.toString();
    }

    /**
     * @param item
     */
    public void appendTextItem(TextItem item) {
        s += item.s;
        length += item.length;
    }
}

class AllowBreak extends OCItem {
    final int indent;
    final int level;
    final boolean unified;
    final String alt;
    final int altlen;

    //@ invariant indent >= 0
    //@ invariant alt != null

    //@ requires n_ >= 0
    //@ requires alt_ != null
    AllowBreak(int n_, int level_, String alt_, int altlen_, boolean u) {
        indent = n_;
        alt = alt_;
        altlen = altlen_;
        level = level_;
        unified = u;
    }


    int minovf;
    ConsList<Boolean> afterBrkAssignment;

    /* maxbr -> maxbi -> pos -> minovf * afterBrkAssignment */
    Map<Integer, Map<Integer, Map<Integer, Pair<Integer, ConsList<Boolean>>>>> cache =
            new HashMap<>();

    /** Get results from state s into cache */
    Pair<Integer, ConsList<Boolean>> cacheGet(SearchState s) {
        if (cache.containsKey(s.maxbr)) {
            Map<Integer, Map<Integer, Pair<Integer, ConsList<Boolean>>>> brCache =
                    cache.get(s.maxbr);
            if (brCache.containsKey(s.maxbi)) {
                Map<Integer, Pair<Integer, ConsList<Boolean>>> biCache =
                        brCache.get(s.maxbi);
                if (biCache.containsKey(s.pos)) {
                    Pair<Integer, ConsList<Boolean>> result =
                            biCache.get(s.pos);
                    return result;
                }
            }
        }
        return null;
    }

    /** Put results of state s into cache */
    void cachePut(SearchState s) {
        // Memoize overflow results before backtracking.
        Map<Integer, Map<Integer, Pair<Integer, ConsList<Boolean>>>> brCache;
        if (cache.containsKey(s.maxbr))
            brCache = cache.get(s.maxbr);
        else {
            brCache = new HashMap<>();
            cache.put(s.maxbr, brCache);
        }
        Map<Integer, Pair<Integer, ConsList<Boolean>>> biCache;
        if (brCache.containsKey(s.maxbi))
            biCache = brCache.get(s.maxbi);
        else {
            biCache = new HashMap<>();
            brCache.put(s.maxbi, biCache);
        }
        Pair<Integer, ConsList<Boolean>> result =
                new Pair<>(s.minovf, s.afterBrkAssignment);
        biCache.put(s.pos, result);
    }

    @Override
    void selfFormat(SearchState s) {
        boolean backtrack = false;
        boolean findminovf = false;
        boolean assignment;
        if (s.forward) {
            if (s.findminovf) {
                // First, check the cache if we have tried the given
                // search parameters.  If so, just return the memoized
                // result and backtrack.
                Pair<Integer, ConsList<Boolean>> result = cacheGet(s);
                if (result != null) {
                    s.forward = false;
                    s.minovf = result.part1();
                    s.afterBrkAssignment = result.part2();
                    return;
                }
            }
            int rpos = s.pos + altlen;
            // First, check if there is already an assignment for us.
            if (s.afterBrkAssignment.length() > 0) {
                // Just take the specified assignment.
                assignment = s.afterBrkAssignment.elem;
                s.afterBrkAssignment = s.afterBrkAssignment.next;
            }
            else if (canLeaveUnbroken(s.minbr, s.minbu) && rpos <= s.rmargin) {
                // This break can be left unbroken without causing immediate overflow.
                assignment = false;
                findminovf = true;
            }
            else if (canBreak(s.maxbr)) {
                // This break must be broken.
                // If not breaking causes immediate overflow, it is better to
                // break now and possibly overflow later.
                assignment = true;
                findminovf = s.findminovf;
            }
            else if (canLeaveUnbroken(s.minbr, s.minbu)) {
                // Overflow always happens, and we could not break.
                assignment = false;
                if (s.findminovf) {
                    // If an earlier break is finding minimal overflow, punt to that break.
                    backtrack = true;
                    // Since we could not break, the amount of minimal overflow is by not breaking.
                    s.minovf = rpos - s.rmargin;
                }
            }
            else throw new InternalCompilerError("Could not either break or not break."
                    + this);
        }
        else {
            // Later item failed to stay within width limit
            if (!s.brkAssignment.get(this) && canBreak(s.maxbr)) {
                // We tried not breaking and did not work.
                // Save later assignments that cause the overflow when not breaking.
                minovf = s.minovf;
                afterBrkAssignment = s.afterBrkAssignment;
                s.afterBrkAssignment = ConsList.empty();
                // Now we try breaking.
                assignment = true;
            }
            else {
                // We tried all options, and overflow always happens.
                if (afterBrkAssignment != null) {
                    // We tried both breaking and not breaking.
                    if (afterBrkAssignment.length() > s.afterBrkAssignment.length()
                            || afterBrkAssignment.length() == s.afterBrkAssignment.length()
                                    && minovf <= s.minovf) {
                        // Not breaking causes overflow later.
                        //  or
                        // Overflow at the same location,
                        // but breaking does not cause less overflow.
                        assignment = false;
                        // Restore saved assignments.
                        s.minovf = minovf;
                        s.afterBrkAssignment = afterBrkAssignment;
                    }
                    else assignment = true;
                    minovf = Integer.MAX_VALUE;
                    afterBrkAssignment = null;
                }
                else {
                    // We did not save assignments.
                    // If we can break, then we could not break, so we must break.
                    // Otherwise, we must not break.
                    assignment = canBreak(s.maxbr);
                }
                SearchState prev = s.prevBreak;
                s.prevBreak = prev.prevBreak;
                if (prev.findminovf) {
                    // If an earlier break is finding minimal overflow, punt to that break.
                    backtrack = true;
                }
                else {
                    // All earlier breaks have tried their best job,
                    // so we continue on with our best break assignment.
                    s.findminovf = false;
                }
            }
        }
        if (backtrack) {
            s.forward = false;
            // Prepare best assignment causing minimal overflow for earlier break.
            s.brkAssignment.remove(this);
            s.afterBrkAssignment =
                    ConsList.cons(assignment, s.afterBrkAssignment);

            // Memoize overflow results before backtracking.
            cachePut(s);
        }
        else {
            s.forward = true;
            if (findminovf) {
                // Set backtracking point to this break.
                s.it = this;
                // Save provided information.
                s.prevBreak = s.copy();
                s.findminovf = true;
                s.minovf = 0;
            }
            s.brkAssignment.put(this, assignment);
            if (assignment) {
                // Break is broken.
                s.pos = s.lmargin + indent;
                // Since we are breaking, all breaks of lower levels must also be broken.
                if (s.minbr < level) s.minbr = level - 1;
                // If this is a unified break, all unified breaks of our level must also be broken.
                if (unified && s.minbu < level) s.minbu = level;
                // The min break level of outer block must be at least this level.
                if (s.minbo < level) s.minbo = level;
            }
            else {
                // Break is not broken.
                s.pos += altlen;
                // Since we are not breaking, the max break level must be adjusted.
                if (s.maxbr >= level) {
                    if (unified) {
                        // If this is a unified break, the max break level must be less than our level.
                        s.maxbr = level - 1;
                    }
                    else {
                        // Otherwise, the max break level must be at most our level.
                        s.maxbr = level;
                    }
                }
                // The max break level of inner block must be less than our level.
                if (s.maxbi >= level) s.maxbi = level - 1;
            }
        }
    }

    @Override
    int sendOutput(PrintWriter o, int lmargin, int pos,
            Map<AllowBreak, Boolean> brkAssignment) {
        if (brkAssignment.containsKey(this) && !brkAssignment.get(this)) {
            // Do not break.
            o.print(alt);
            return pos + altlen;
        }
        else {
            // Break.
            o.println();
            for (int i = 0; i < lmargin + indent; i++)
                o.print(" ");
            return lmargin + indent;
        }
    }

    boolean canBreak(int maxb) {
        return level <= maxb;
    }

    boolean canLeaveUnbroken(int minLevel, int minLevelUnified) {
        return level > minLevelUnified || !unified && level > minLevel;
    }

    int[] minBreakLevels = { 0, 0, 0 };

    @Override
    int[] minBreakLevels() {
        return minBreakLevels;
    }

    @Override
    String selfToString() {
        String result = unified ? "@<" : "<";
        result += level + ">";
        if (indent > 0) result += "^" + indent;
        return result + " ";
    }
}

/**
 * A Newline is simply an {@code AllowBreak} that must be broken.
 */
class Newline extends AllowBreak {
    Newline(int n, int level) {
        super(n, level, "\n", 0, true);
    }

    @Override
    void selfFormat(SearchState s) {
        if (!canBreak(s.maxbr))
            throw new InternalCompilerError("Newline cannot be broken.");
        // Break is broken.
        s.pos = s.lmargin + indent;
        // Since we are breaking, all breaks of lower levels must also be broken.
        if (s.minbr < level) s.minbr = level - 1;
        // If this is a unified break, all unified breaks of our level must also be broken.
        if (unified && s.minbu < level) s.minbu = level;
        // The min break level of outer block must be at least this level.
        if (s.minbo < level) s.minbo = level;
    }

    @Override
    boolean canLeaveUnbroken(int minLevel, int minLevelUnified) {
        return false;
    }

    @Override
    String selfToString() {
        if (indent == 0)
            return "\\n";
        else return "\\n[" + indent + "]";
    }

    int[] minBreakLevels = { level, level > 0 ? level - 1 : 0, level };

    @Override
    int[] minBreakLevels() {
        return minBreakLevels;
    }
}

/**
 * A BlockItem is a formatting unit containing a list of other items to be
 * formatted.
 */
class BlockItem extends OCItem {
    OCItem first;
    OCItem last;
    int indent; //@ invariant indent >= 0

    BlockItem(int indent_) {
        first = last = null;
        indent = indent_;
    }

    /**
     * Add a new item to the end of the block. Successive StringItems are
     * concatenated together to limit recursion depth when formatting.
     */
    void add(OCItem it) {
        if (first == null) {
            first = it;
        }
        else {
            if (it instanceof TextItem && last instanceof TextItem) {
                TextItem lasts = (TextItem) last;
                lasts.appendTextItem((TextItem) it);
                return;
            }
            else {
                last.next = it;
            }
        }
        last = it;
    }

    @Override
    void selfFormat(SearchState s) {
        // We are going into a block.
        // Save parameters for the outer block.
        s.pushBlock(this);
        // The new indentation is relative to the current position.
        s.lmargin = s.pos + indent;
        // The min break levels reset.
        int[] minBreakLevels = minBreakLevels();
        s.minbr = minBreakLevels[3];
        s.minbu = minBreakLevels[4];
        s.minbo = minBreakLevels[1];
        // The max break level is now maxbi.
        s.maxbr = s.maxbi;
    }

    @Override
    int sendOutput(PrintWriter o, int lmargin, int pos,
            Map<AllowBreak, Boolean> brkAssignment) {
        lmargin = pos + indent;
        for (OCItem it = first; it != null; it = it.next)
            pos = it.sendOutput(o, lmargin, pos, brkAssignment);
        return pos;
    }

    int[] minBreakLevels = null;

    /**
     * Returns an integer array of length 5 such that
     * - [0] is the minimum break level that any break in the containing block
     *   of the containing block of this item must break
     * - [1] is the minimum break level that any break in the containing block
     *   of this item must break
     * - [2] is the minimum break level that any unified break in the containing
     *   block of this item must break
     * - [3] is the minimum break level that any break in this block must break
     * - [4] is the minimum break level that any unified break in this block
     *   must break
     */
    @Override
    int[] minBreakLevels() {
        if (minBreakLevels == null) {
            minBreakLevels = new int[] { 0, 0, 0, 0, 0 };
            for (OCItem it = first; it != null; it = it.next) {
                int[] mbls = it.minBreakLevels();
                if (minBreakLevels[2] < mbls[0]) minBreakLevels[2] = mbls[0];
                if (minBreakLevels[3] < mbls[1]) minBreakLevels[3] = mbls[1];
                if (minBreakLevels[4] < mbls[2]) minBreakLevels[4] = mbls[2];
            }
            minBreakLevels[0] = minBreakLevels[1] = minBreakLevels[2];
        }
        return minBreakLevels;
    }

    @Override
    String selfToString() {
        if (indent == 0)
            return "[" + first + "]";
        else return "[" + indent + first + "]";
    }
}
