/**
 * CodeWriter -- Andrew C. Myers, May 2005
 *   Originally developed for use in Cornell University Computer Science
 *   412/413, April 2001.
 * 
 * The pretty-printing algorithm is loosely based on the Modula-3
 * pretty-printer, and on notes by Greg Nelson. It was extended to support
 * breaks at multiple levels.
 */

package polyglot.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A <code>CodeWriter</code> is a pretty-printing engine. It formats
 * structured text onto an output stream <code>o</code> in the minimum number
 * of lines, while keeping the width of the output within <code>width</code>
 * characters if possible. Newlines occur in the output only at places where a
 * line break is permitted by the use of <code>allowBreak</code> and
 * <code>unifiedBreak</code>.
 * 
 * Line breaks can have different levels, which is useful for implementing
 * things like Scheme's "miser mode".
 */
public class CodeWriter
{
    /**
     * Create a CodeWriter object with output stream <code>o</code>
     * and width <code>width_</code>.
     */
    public CodeWriter(OutputStream o, int width_) {
        output = new PrintWriter(new OutputStreamWriter(o));
        width = width_;
        current = input = new BlockItem(null, 0);
    }

    /**
     * Create a CodeWriter object.
     * @param w the output to write to. Must be non-null.
     * @param width the formatting width. Must be positive.
     */
    public CodeWriter(PrintWriter o, int width_) {
        output = o;
        width = width_;
        current = input = new BlockItem(null, 0);
    }

    /**
     * Create a CodeWriter object.
     * @param w the output to write to. Must be non-null.
     * @param width the formatting width. Must be positive.
     */
    public CodeWriter(Writer o, int width_) {
        output = new PrintWriter(o);
        width = width_;
        current = input = new BlockItem(null, 0);
    }
        
        
    /**
     * Print the string <code>s</code> verbatim on the output stream.
     * @param the string to print.
     */
    public void write(String s) {
       if (s.length() > 0) write(s, s.length());
    }
    /**
     * Print the string <code>s</code> on the output stream. Pretend that it
     * has width <code>length</code> even if it has a different number of
     * characters. This is useful when the string contains escape sequences,
     * HTML character entity references, etc.
     * 
     * @param s
     * @param length
     */
    public void write(String s, int length) {
        current.add(new TextItem(s, length));
    }

    /**
     * Start a new block with a relative indentation of <code>n</code>
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
     * position <code>pos</code>, all the following lines are printed at the
     * position <code>pos+n</code>.
     * 
     * @param n
     *            the number of characters increased on indentation (relative
     *            to the current position) for all lines in the block.
     *            Requires: n >= 0.
     */         
    public void begin(int n) {
        BlockItem b = new BlockItem(current, n);
        current.add(b);
        current = b;
    }
        
    /**
     * Terminate the most recent outstanding <code>begin</code>.
     */
    public void end() {
        current = current.parent;
        //@ assert current != null
        // if (current == null) throw new RuntimeException();
    }

    /**
     * Insert a break (an optional newline). Indentation will be preserved.
     * Every break has a level. A level 0 break is always broken to form a
     * newline. The codewriter tries to avoid breaking higher-level breaks, and
     * the higher the level, the harder it tries. It follows the "break from
     * root" rule: if a break is broken, breaks of equal or lower level in all
     * containing blocks must also be broken, and breaks of strictly lower
     * level in the same block must also be broken.
     * 
     * @param n
     *            indentation relative to the current block if the newline is
     *            inserted. Requires: n >= 0
     * @param alt
     *            if no newline is inserted, the string <code>alt</code> is
     *            output instead. Requires: alt != null
     * @param level
     *            the level of the break. Requires: level >= 0
     */ 
    public void allowBreak(int n, int level, String alt, int altlen) {
        current.add(new AllowBreak(n, level, alt, altlen, false));
    }
    /**
     * Insert a unified break. Unified breaks act like the breaks inserted by
     * <code>allowBreak</code>, but unified breaks also break if any break of
     * the same level in the same block is broken, whereas ordinary breaks do
     * not necessarily break in this case. That is, unified breaks act as if
     * they were slightly lower level that other breaks of the same level
     * (including other unified breaks!).
     * 
     * @param n
     *            the relative indentation
     * @param level
     *            the level of the break
     * @param alt
     *            the alternative text
     * @see allowBreak
     */
    public void unifiedBreak(int n, int level, String alt, int altlen) {
        current.add(new AllowBreak(n, level, alt, altlen, true));
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
     * Force a newline. Indentation will be preserved. This method should be
     * used sparingly; usually a call to <code>allowBreak</code> is preferable
     * because forcing a newline also causes all breaks in containing blocks to
     * be broken.
     *  
     */
    public void newline() {
        current.add(new AllowBreak(0, 0, "", 0, false));
    }
    /**
     * Like newline(), but forces a newline with a specified indentation.
     */
    public void newline(int n) {
        current.add(new AllowBreak(n, 0, "", 0, false));
    }


    /**
     * Send out the current batch of text to be formatted. All outstanding
     * <code>begin</code>'s are closed and the current indentation level is
     * reset to 0. Returns true if formatting was completely successful (the
     * margins were obeyed).
     */
    public boolean flush() throws IOException {
        return flush(true);
    }
    /** Like <code>flush</code>, but passing <code>format=false</code>
     * causes output to be generated in the fastest way possible, with
     * all breaks broken.
     * @param format whether to pretty-print the output
     * @return whether formatting was completely successful.
     * @throws IOException
     */
    public boolean flush(boolean format) throws IOException {
	boolean success = true;
	format_calls = 0;
	if (format) {
	    try {
	        top = input;
	        Item.format(input, 0, 0, width, width,
                    new MaxLevels(Integer.MAX_VALUE, Integer.MAX_VALUE), 0, 0);
	    } catch (Overrun o) { success = false; }
	} else success = false;
	
        input.sendOutput(output, 0, 0, success, null);
        output.flush();
        if (CodeWriter.debug) {
            System.err.println("Total calls to format = " + format_calls);
            System.err.flush();
        }
        current = input = new BlockItem(null, 0);
	return success;
    }
    /**
     * Return a readable representation of all the structured input given to
     * the CodeWriter since the last flush.
     */
    public String toString() {
	return input.toString();
    }

    BlockItem input;
    BlockItem current;
    
    static Item top;    

    PrintWriter output;
    int width;
    static int format_calls = 0;
    public static final boolean debug = false;
    public static final boolean precompute = true;
}

/**
 * An <code>Overrun</code> represents a formatting that failed because the
 * right margin was exceeded by at least <code>amount</code> chars. If
 * sameLine, the overrun occurred on the first line of the requested
 * formatting; otherwise, it occurred on a subsequent line.
 */
class Overrun extends Exception 
{
    int amount;
    int type;
    final static int POS = 0;
    final static int WIDTH = 1;
    final static int FIN = 2;

    private static final Overrun overrun = new Overrun();

    private Overrun() {}
    static Overrun overrun(int amount, int type) {
	if (CodeWriter.debug) System.err.println("-- Overrun: " + amount);
        overrun.amount = amount;
        overrun.type = type;
        return overrun;
    }
}


/**
 * An <code>Item</code> is a piece of input handed to the formatter. It
 * contains a reference to a possibly empty list of items that follow it.
 */
abstract class Item 
{
    /** next is null if this is the last item in the list. */
    Item next;

    protected Item() { next = null; }

    /**
     * Try to format this and subsequent items.
     * 
     * @return the final cursor position (which may overrun rmargin, fin, or
     *         both), and set any contained breaks accordingly.
     *         </p>
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
     * <code>minLevel</code> is used to communicate the level of breaks broken
     * in nested blocks (and earlier in the current block). Any break of level
     * <= minLevel <em>must</em> be broken. The parameter <code>
     * minLevelUnified</code> is the minimum level at which unified breaks must
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
    abstract FormatResult formatN(int lmargin, int pos, int rmargin, int fin,
            MaxLevels m, int minLevel, int minLevelUnified)
    	throws Overrun;
    /**
     * Send the output associated with this item to <code>o</code>, using the
     * current break settings.
     * 
     * @param success
     */
    abstract int sendOutput(PrintWriter o, int lmargin, int pos, boolean success, Item last)
      throws IOException;
    
    // XXX
    // the getminwidth etc. code is starting to duplicate the logic of the main
    // formatting code. This suggests they should be merged. format can take
    // two width arguments: one the width left on the current line, and one the
    // width of subsequent lines. Hmmm -- new blocks start relative to current
    // position, so knowing the initial width isn't enough.

    /**
     * Try to format a whole sequence of items in the manner of formatN. Unlike
     * for formatN, The initial position may be an overrun (this is the only
     * way that overruns are checked!). The item <code>it</code> may be also
     * null, signifying an empty list. Requires: lmargin < rmargin, pos <=
     * rmargin, lmargin >= 0.
     * 
     * @see formatN
     */
    static FormatResult format(Item it, int lmargin, int pos, int rmargin, int fin,
            		  MaxLevels m, int minLevel, int minLevelUnified) throws Overrun {
        CodeWriter.format_calls++;
	if (CodeWriter.debug) {
	    if (it != null && it != CodeWriter.top) {
	        System.err.println("SNAPSHOT:");
	        PrintWriter w = new PrintWriter(new OutputStreamWriter(System.err));
	        try {	            	            
	            CodeWriter.top.sendOutput(w, 0, 0, true, it);
	        }
	        catch (IOException e) {  }
	        w.write("<END>\n");
	        w.flush();
	    }
	    System.err.println("Format: " + it + "\n  lmargin = " +
		lmargin + " pos = " + pos + " fin = " + fin +
		" max break levels: " + m +
		" min break levels: " + minLevel + "/" + minLevelUnified);

	    if (CodeWriter.debug) {	    
	        System.err.println("  MinWidth = " + getMinWidth(it, m));
	        System.err.println("  MinPosWidth = " + getMinPosWidth(it, m));
	        System.err.println("  MinIndent = " + getMinIndent(it, m));
	    }
	
	    System.err.flush();
	}
	if (it == null) { // no items to format. Check against final position.
	    if (pos > fin) {
	        if (CodeWriter.debug)
	            System.err.println("Final position overrun: " + (pos-fin));
	        throw Overrun.overrun(pos - fin, Overrun.FIN);
	    }
	    else return new FormatResult(pos, minLevelUnified);
	}
	
	int amount2 = lmargin + getMinWidth(it, m) - rmargin; // lmargin is too far
                                                       // right
	if (amount2 > 0) {
	    if (CodeWriter.debug)
	        System.err.println("Width overrun: " + amount2);
	 
	    throw Overrun.overrun(amount2, Overrun.WIDTH);
	}
	
	int amount = pos + getMinPosWidth(it, m) - rmargin; // overrun on first line
	if (amount > 0) {
	    if (CodeWriter.debug)
	        System.err.println("Position (first line) overrun: " + amount);

	    throw Overrun.overrun(amount, Overrun.POS);
	}
	

	int amount3 = lmargin + getMinIndent(it, m) - fin;   // overrun on last line
	if (amount3 > 0) {
	    if (CodeWriter.debug)
	        System.err.println("Final position (predicted) overrun: " + amount3);

	    throw Overrun.overrun(amount3, Overrun.FIN);
	}	

	return it.formatN(lmargin, pos, rmargin, fin, m, minLevel, minLevelUnified);
    }

/*
 * The following fields keep track of the tightest formatting that is possible
 * with an item and its following items, if all breaks are broken. The purpose
 * is to more aggressively tighten bounds when an overrun occurs. Formatting is
 * measured relative to both "lmargin" and to "pos". T
 * 
 * lmargin | pos | | | xxxxx xxxxxxxx xxxxxx <------> min_width (at least
 * min_pos_width): distance from lmargin to rightmost char <---> min_pos_width:
 * distance from initial pos to end of first line <----> min_indent (at most
 * min_width): distance from lmargin to final position on last line
 */

    //@ invariant min_indent <= min_width
    
    static final int NO_WIDTH = -9999;    

    /** Minimum lmargin-rhs width on second and following lines. 
     * A map from max levels to Integer(width). */

    Map min_widths = new HashMap();
    
    /** Minimum lmargin-final offset */
    Map min_indents = new HashMap();

    /** Minimum pos-rhs width (i.e., min width up to first break) */
    Map min_pos_width = new HashMap();
    
    /** Are there any breaks in this items and following items? */
    boolean contains_brks;
    boolean cb_init = false;

    static int getMinWidth(Item it, MaxLevels m) {
	if (it == null) return NO_WIDTH;
	if (it.min_widths.containsKey(m))
	    return ((Integer)it.min_widths.get(m)).intValue();
	int p1 = it.selfMinWidth(m);	  
	int p2 = it.selfMinIndent(m);	
	int p3 = (p2 != NO_WIDTH) ? getMinPosWidth(it.next, m) + p2 : NO_WIDTH;	
	int p4 = getMinWidth(it.next, m);
	
	if (CodeWriter.debug)
	System.err.println("minwidth: item = " + it + m + ":  p1 = " + p1 + ", p2 = " + p2 + ", p3 = " + p3 + ", p4 = " + p4);
	int result = Math.max(Math.max(p1, p3), p4);
	it.min_widths.put(m, new Integer(result));
	return result;
    }

    static int getMinPosWidth(Item it, MaxLevels m) {
	if (it == null) return 0;
	if (it.min_pos_width.containsKey(m)) {
	    return ((Integer)it.min_pos_width.get(m)).intValue();
	}
	int p1 = it.selfMinPosWidth(m);
	int result;
	if (it.next == null || it.selfContainsBreaks(m)) {
	    result = p1;
	    if (CodeWriter.debug)
	    System.err.println("minpos: item = " + it + m + ":  p1 = " + p1);
	} else {
	    result = p1 + getMinPosWidth(it.next, m);
	    if (CodeWriter.debug)
	    System.err.println("minpos: item = " + it + m + ":  p1 = " + p1 + ", next.minpos = " + getMinPosWidth(it.next, m));
	}
	it.min_pos_width.put(m, new Integer(result));
	return result;
    }

    static int getMinIndent(Item it, MaxLevels m) {
	if (it == null) return NO_WIDTH;
	if (it.min_indents.containsKey(m)) {
	    return ((Integer)it.min_indents.get(m)).intValue();
	}
	int p1 = it.selfMinIndent(m);
	if (it.next == null) return p1;
	int result;
	if (containsBreaks(it.next, m))
	    result = getMinIndent(it.next, m);
	else 
	    result = getMinPosWidth(it.next, m);
	it.min_indents.put(m, new Integer(result));
	return result;
    }

    static boolean containsBreaks(Item it, MaxLevels m) {
	if (it == null) return false;
	if (it.cb_init) return it.contains_brks;
	if (it.selfContainsBreaks(m)) {
	    it.contains_brks = true;
	    it.cb_init = true;
	    return true;
	}
	if (it.next == null) return false;
	it.contains_brks = containsBreaks(it.next, m);
	it.cb_init = true;
	return it.contains_brks;
    }

    public String summarize(String s) {
	if (s.length() <= 79) return s;
	return s.substring(0, 76) + "...";
    }

    public String toString() {
	if (next == null) return summarize(selfToString());
	return summarize(selfToString() + next.toString());
    }

    abstract String selfToString();
    abstract int selfMinIndent(MaxLevels m);
    abstract int selfMinWidth(MaxLevels m);
    abstract int selfMinPosWidth(MaxLevels m);
    abstract boolean selfContainsBreaks(MaxLevels m);
}

/** A simple string. */
class TextItem extends Item {
    String s;		//@ invariant s != null
    int length;    
    
    TextItem(String s_, int length_) { s = s_; length = length_; }
        
    FormatResult formatN(int lmargin, int pos, int rmargin, int fin,
            MaxLevels m, int minLevel, int minLevelUnified)
      throws Overrun {
        return format(next, lmargin, pos + length, rmargin, fin,
		      m, minLevel, minLevelUnified);
        // all overruns passed through
    }
    int sendOutput(PrintWriter o, int lm, int pos, boolean success, Item last) throws IOException {
        o.write(s);
        return pos + length;
    }
    boolean selfContainsBreaks(MaxLevels m) { return false; }
    int selfMinIndent(MaxLevels m) { return NO_WIDTH; }
    int selfMinWidth(MaxLevels m) { return NO_WIDTH; } // length only counts on s lines
    int selfMinPosWidth(MaxLevels m) { return length; }
    String selfToString() {
	java.io.StringWriter sw = new java.io.StringWriter();
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (c == ' ') sw.write("\\ ");
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

class AllowBreak extends Item {
    final int indent;
    final int level;
    final boolean unified;
    final String alt;
    final int altlen;
    boolean broken = false;
        
    //@ invariant indent >= 0
    //@ invariant alt != null

    //@ requires n_ >= 0
    //@ requires alt_ != null
    AllowBreak(int n_, int level_, String alt_, int altlen_, boolean u) {
        indent = n_; alt = alt_; altlen = altlen_;
        level = level_; unified = u; }
        
    FormatResult formatN(int lmargin, int pos, int rmargin, int fin,
            MaxLevels m, int minLevel, int minLevelUnified) throws Overrun {
        if (canLeaveUnbroken(minLevel, minLevelUnified)) {
            // first, we can try not breaking it
            try {
                if (CodeWriter.debug)
                    System.err.println("trying not breaking it.");
                broken = false;
                return format(next, lmargin, pos + altlen, rmargin, fin,
                    new MaxLevels(
                            Math.min(unified ? level - 1 : level, m.maxLevel),
                            Math.min(level - 1, m.maxLevelInner)),
                    minLevel, minLevelUnified);

            }
            //  |yyy^xxxx
            //  |xxxxx
            //  |xxx
            // pos overrun: might help by breaking
            // width overrun: might help by breaking (e.g., if breaking permits nested breaks)
            // fin overrun: similar
            catch (Overrun o) {
                if (CodeWriter.debug) {
                    System.err.println("not breaking caused overrun of " + o.amount);
                }
                if (level > m.maxLevel) {
                    if (CodeWriter.debug) {
                        System.err.println("not breaking failed, but can't break either.");
                    }
                    throw o; // can't break it
                }
            }
        }
        if (canBreak(m)) { // now, we can try breaking it
            if (CodeWriter.debug)
                System.err.println("trying breaking " + this);
            broken = true;
            try {
                return format(next, lmargin, lmargin + indent, rmargin, fin, m,
                        Math.max(level-1, minLevel),
                        Math.max(level, minLevelUnified));
            }
            //  |yyy^
            //  |  xxxx
            //  |  xxxxxx
            //  |  xxx
            // pos overrun: becomes a width overrun?
            // width overrun: remains
            // fin overrun: remains? becomes a width overrun?
            catch (Overrun o) {
                o.type = Overrun.WIDTH; throw o;
            }
        }
        throw new IllegalArgumentException("could not either break or not break");
    }
        
    int sendOutput(PrintWriter o, int lmargin, int pos, boolean success, Item last)
        throws IOException {
        if (broken || !success) {
            o.println();
            for (int i = 0; i < lmargin + indent; i++) o.print(" ");
            //o.write("(" + (lmargin+indent) + ")");
            return lmargin + indent;
        } else {
	    o.print(alt);
            return pos + altlen;
        }
    }
    boolean canBreak(MaxLevels m) {
        return level <= m.maxLevel;
    }
    boolean canLeaveUnbroken(int minLevel, int minLevelUnified) {
        return (level > minLevelUnified || !unified && level > minLevel);
    }
    int selfMinIndent(MaxLevels m) {
        if (canBreak(m)) return indent;
        else return NO_WIDTH;
    }
    int selfMinPosWidth(MaxLevels m) {
        if (canBreak(m)) return 0;
        else return altlen;
    }
    int selfMinWidth(MaxLevels m) {
        if (canBreak(m)) return indent;
        else return NO_WIDTH;
    }
    boolean selfContainsBreaks(MaxLevels m) {
        return canBreak(m);
    }

    String selfToString() {
	if (indent == 0) return " ";
	else return "^" + indent; }
}

/**
 * A BlockItem is a formatting unit containing a list of other items to be
 * formatted.
 */
class BlockItem extends Item {
    BlockItem parent;
    Item first;
    Item last;
    int indent;	    //@ invariant indent >= 0
        
    BlockItem(BlockItem parent_, int indent_) {
        parent = parent_;
        first = last = null;
        indent = indent_;
    }

    /**
     * Add a new item to the end of the block. Successive StringItems are
     * concatenated together to limit recursion depth when formatting.
     */
    void add(Item it) {
        if (first == null) {
	    first = it;
	} else {
	    if (it instanceof TextItem && last instanceof TextItem) {
		TextItem lasts = (TextItem)last;
		lasts.appendTextItem(((TextItem)it));
		return;
	    } else {
		last.next = it;
	    }
	}
        last = it;
    }

    FormatResult formatN(int lmargin, int pos, int rmargin, int fin,
            MaxLevels m, int minLevel, int minLevelUnified) throws Overrun {
        int childfin = fin;
        if (childfin + getMinPosWidth(next, m) > rmargin) {
            childfin = rmargin - getMinPosWidth(next, m);
        }
        while (true) {
            FormatResult fr = format(first, pos + indent, pos, rmargin, childfin,
                    new MaxLevels(m.maxLevelInner, m.maxLevelInner), 0, 0);
            int minLevel2 = Math.max(minLevel, fr.minLevel);
            int minLevelU2 = Math.max(minLevelUnified, fr.minLevel);
            try {
                return format(next, lmargin, fr.pos, rmargin, fin,
                        m, minLevel2, minLevelU2);
            } catch (Overrun o) {
                if (o.type == Overrun.WIDTH) {
                    o.type = Overrun.FIN;
                    // Idea: doesn't matter where next item started XXX really?
                    throw o;
                }
                childfin -= o.amount;
            }          
        }
    }

    int sendOutput(PrintWriter o, int lmargin, int pos, boolean success, Item last) throws IOException {
        Item it = first;
        lmargin = pos + indent;
        while (it != null) {
            pos = it.sendOutput(o, lmargin, pos, success, last);
            if (last != null && it == last) {
                throw new IOException();
            }
            it = it.next;
        }
        return pos;
    }

    int selfMinWidth(MaxLevels m) {
        return getMinWidth(first,
                new MaxLevels(m.maxLevelInner, m.maxLevelInner));
    }
    int selfMinPosWidth(MaxLevels m) {
        return getMinPosWidth(first,
                new MaxLevels(m.maxLevelInner, m.maxLevelInner));
    }
    int selfMinIndent(MaxLevels m) {
        return getMinIndent(first,
                new MaxLevels(m.maxLevelInner, m.maxLevelInner));
    }
    
    /**
     * Map from maxlevels to either null or non-null, the latter if it can
     * contain breaks at those maxlevels.
     */
    Map containsBreaks = new HashMap();
    
    boolean selfContainsBreaks(MaxLevels m) {
	if (containsBreaks.containsKey(m)) {
	    return (containsBreaks.get(m) != null);
	}
	boolean result = containsBreaks(first, new MaxLevels(m.maxLevelInner, m.maxLevelInner));	
	containsBreaks.put(m, result ? m : null);
	return result;
    }
    String selfToString() {
	if (indent == 0) return "[" + first + "]";
	else return "[" + indent + first + "]";
    }
}
class FormatResult {
    int pos;
    int minLevel;
    FormatResult(int pos_, int minLevel_) {
        pos = pos_;
        minLevel = minLevel_;
    }
}

class MaxLevels {
    int maxLevel;
    int maxLevelInner;
    MaxLevels(int ml, int mli) {
        maxLevel = ml;
        maxLevelInner = mli;
    }
    public int hashCode() {
        return maxLevel * 17 + maxLevelInner;
    }
    public boolean equals(Object o) {
        if (o instanceof MaxLevels) {
            MaxLevels m2 = (MaxLevels)o;
            return (maxLevel == m2.maxLevel && maxLevelInner == m2.maxLevelInner);
        } else
            return false;
    }
    public String toString() {
        return "[" + maxLevel + "/" + maxLevelInner + "]";
    }
}
