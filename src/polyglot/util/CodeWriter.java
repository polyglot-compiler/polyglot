/**
 * CodeWriter -- Andrew C. Myers, April 2001
 *   For use in Cornell University Computer Science 412/413
 */

package polyglot.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Vector;

/** 
 * A <code>CodeWriter</code> is a pretty-printing engine.
 * It formats structured text onto an output stream <code>o</code> in the
 * minimum number of lines, while keeping the width of the output
 * within <code>width</code> characters if possible.
 */
public class CodeWriter
{
    /**
     * Create a CodeWriter object with output stream <code>o</code>
     * and width <code>width_</code>.
     * <esc>requires o != null</esc>
     * <esc>requires width_ > 0</esc>
     */
    public CodeWriter(OutputStream o, int width_) {
        output = new OutputStreamWriter(o);
        width = width_;
        current = input = new BlockItem(null, 0);
    }

    /**
     * Create a CodeWriter object with output <code>w</code> and
     * width <code>width_</code>.
     * <esc>requires w != null</esc>
     * <esc>requires width_ > 0</esc>
     */
    public CodeWriter(Writer w, int width_) {
        output = w;
        width = width_;
        current = input = new BlockItem(null, 0);
    }
        
    /** Print the string <code>s</code> verbatim on the output stream.
     * <esc>requires s != null</esc>
     */
    public void write(String s) {
       if (s.length() > 0)
          current.add(new StringItem(s));
    }

    /** Force a newline with no added indentation. */
    public void newline()
    {
       newline(0);
    }

    /**
     * Start a new block with a relative indentation of <code>n</code>
     * characters.
     * <br>
     * A block is a formatting unit. The formatting algorithm will try
     * to put the whole block in one line unless
     * <ul>
     * <li>there is a <code>newline</code> item in the block.</li>
     * <li>the block cannot fit in one line.</li>
     * </ul>
     * If either of the two conditions is satisfied, the
     * formatting algorithm will break the block into lines: every
     * <code>allowBreak</code> will cause a line change, the first line
     * is printed at the current cursor position <code>pos</code>,
     * all the following lines are printed at the position
     * <code>pos+n</code>.
     * 
     * @param n the number of characters increased on indentation (relative
     * to the current position) for all lines in the block.
     *
     * <esc>requires n >= 0</esc>
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
     * Allow a newline. Indentation will be preserved.
     * If no newline is inserted, a single space character is output instead.
     *
     * @param n the amount of increase in indentation if
     * the newline is inserted.
     *
     * <esc>requires n >= 0</esc>
     */ 
    public void allowBreak(int n) {
        current.add(new AllowBreak(n, " "));
    }

    /**
     * Allow a newline. Indentation will be preserved.
     *
     * @param n the amount of increase in indentation if
     *  the newline is inserted.
     * @param alt if no newline is inserted, the string <code>alt</code> is
     *  output instead.   
     *
     * <esc>requires n >= 0</esc>
     * <esc>requires alt != null</esc>
     */ 
    public void allowBreak(int n, String alt) {
        current.add(new AllowBreak(n, alt));
    }

    /**
     * Force a newline. Indentation will be preserved.  This method
     * should be used sparingly; usually a call to <code>allowBreak</code> is
     * preferable because forcing a newline also causes all breaks
     * in containing blocks to be broken.
     *
     * <esc>requires n >= 0</esc>
     */
    public void newline(int n) {
        current.add(new Newline(n));
    }

    /**
     * Send out the current batch of text to be formatted. All
     * outstanding <code>begin</code>'s are closed and the current
     * indentation level is reset to 0. Returns true if formatting
     * was completely successful (the margins were obeyed).
     */
    public boolean flush() throws IOException {
	boolean success = true;
	try {
	    Item.format(input,0, 0, width, width, true, true);
	} catch (Overrun o) { success = false; }
        input.sendOutput(output, 0, 0);
        output.flush();
        input.free();
        current = input = new BlockItem(null, 0);
	return success;
    }
    /**
     * Return a readable representation of all the structured input
     * given to the CodeWriter since the last flush.
     */
    public String toString() {
	return input.toString();
    }

    BlockItem input;
    BlockItem current;

    Writer output;
    int width;
    public static final boolean debug = false;
    public static boolean precompute = false;

    //@ invariant current != null
    //@ invariant input != null
    //@ invariant output != null
    //@ invariant width > 0
}

/**
 * An <code>Overrun</code> represents a formatting that failed because the right
 * margin was exceeded by at least <code>amount</code> chars.
 */
class Overrun extends Exception 
{
    int amount;

    private Overrun() { }
    private /*@ non_null */ static Overrun overrun = new Overrun();

    //@ ensures \result != null
    static Overrun overrun(int amount) {
	if (CodeWriter.debug) System.err.println("-- Overrun: " + amount);
        overrun.amount = amount;
        return overrun;
    }
}

/**
 * An <code>Item</code> is a piece of input handed to the formatter. It
 * contains a reference to a possibly empty list of items that follow it.
 */
abstract class Item 
{
    Item next;

    protected Item() { next = null; }

    /** 
     * Try to format this item and subsequent items. The current cursor
     * position is <code>pos</code>, left and right margins are as
     * specified. Returns the final position, which must be <code>&lt;
     * fin</code>. If breaks may be broken, <code>can_break</code> is
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
     * <esc>requires lmargin < rmargin</esc>
     * <xxx>requires pos <= rmargin</xxx>
     * <esc>requires lmargin >= 0</esc>
     */

    abstract int formatN(int lmargin, int pos, int rmargin, int fin,
			     boolean can_break, boolean nofail) throws Overrun;
    /**
     * Send the output associated with this item to <code>o</code>, using the
     * current break settings.
     * <esc>requires o != null</esc>
     * <esc>requires lmargin >= 0</esc>
     * <esc>requires pos >= 0</esc>
     */
    abstract int sendOutput(Writer o, int lmargin, int pos)
      throws IOException;

    /** Free references to any other items. */
    void free() { 
        if( next != null) { 
            next.free(); 
            next = null; 
        }
    }

    /**
     * Try to format a whole sequence of items in the manner of formatN.
     * The initial position may be an overrun (this is the only way
     * that overruns are checked!) <code>it</code> may be also null,
     * signifying an empty list.
     *
     * <esc>requires lmargin < rmargin</esc>
     * <xxx>requires pos <= rmargin</xxx>
     * <esc>requires lmargin >= 0</esc>
     */
    static int format(Item it, int lmargin, int pos, int rmargin, int fin,
	  	          boolean can_break, boolean nofail) throws Overrun {
	if (CodeWriter.debug) {
	    System.err.println("Format: " + it + "\n  lmargin = " +
		lmargin + " pos = " + pos + " fin = " + fin +
		(can_break ? " can break" : " no breaks") +
		(nofail ? " [nofail]" : ""));
	}
	if (!nofail && pos > rmargin) { // overrun
	    if (CodeWriter.precompute) {
		if (CodeWriter.debug) {
		    System.err.println("MinWidth of " + it + " = " +
					getMinWidth(it));
		    System.err.println("MinPosWidth of " + it + " = " +
					getMinPosWidth(it));
		    System.err.println("MinIndent of " + it + " = " +
					getMinIndent(it));
		}
		int amount = pos + getMinPosWidth(it) - rmargin;
		int amount1 = lmargin + getMinWidth(it) - rmargin;
		if (amount1 > amount) amount = amount1;
		int amount2 = lmargin + getMinIndent(it) - fin;
		if (amount2 > amount) amount = amount2;
		throw Overrun.overrun(amount);
	    } else {
		throw Overrun.overrun(pos - rmargin);
	    }
	}
	if (it == null) { // no items to format. Check against final position.
	    if (!nofail && pos > fin) throw Overrun.overrun(pos - fin);
	    return pos;
	}
	return it.formatN(lmargin, pos, rmargin, fin, can_break, nofail);
    }
  
/*
    The following fields keep track of the tightest formatting that is
    possible with an item and its following items, if all breaks are
    broken.  Formatting is measured relative to both "lmargin" and to
    "pos".

   lmargin
   | pos
   | |
   | xxxxx
   xxxxxxxx
   xxxxxx
   <------>
   min_width (at least min_pos_width)
     <--->
     min_pos_width
   <---->
   min_indent (at most min_width)
*/

    //@ invariant min_pos_width <= min_width
    //@ invariant min_indent <= min_width

    /** Minimum lmargin-rhs width. */
    int min_width = -1;
    
    /** Minimum lmargin-final offset */
    int min_indent = -1;

    /** Minimum pos-rhs width (i.e., min width up to first break) */
    int min_pos_width = -1;

    /** Are there any breaks in this items and following items? */
    boolean contains_brks;
    boolean cb_init = false;

    static int max(int i, int j) {
	if (i > j) return i;
	return j;
    }

    static int getMinWidth(Item it) {
	if (it == null) return 0;
	if (it.min_width >= 0) return it.min_width;
	int p1 = it.selfMinWidth();
	int p2 = it.selfMinIndent();
	int p3 = getMinPosWidth(it.next) + p2;
	int p4 = getMinWidth(it.next);
	return (it.min_width = max(max(p1, p3), p4));
    }

    static int getMinPosWidth(Item it) {
	if (it == null) return 0;
	if (it.min_pos_width >= 0) return it.min_pos_width;
	int p1 = it.selfMinPosWidth();
	if (it.next == null ||
	    it.selfContainsBreaks()) return p1;
	return p1 + getMinPosWidth(it.next);
    }

    static int getMinIndent(Item it) {
	if (it == null) return 0;
	if (it.min_indent >= 0) return it.min_indent;
	int p1 = it.selfMinIndent();
	if (it.next == null) return p1;
	if (containsBreaks(it.next))
	    return getMinIndent(it.next);
	return p1 + getMinPosWidth(it.next);
    }

    static boolean containsBreaks(Item it) {
	if (it == null) return false;
	if (it.cb_init) return it.contains_brks;
	if (it.selfContainsBreaks()) {
	    it.contains_brks = true;
	    it.cb_init = true;
	    return true;
	}
	if (it.next == null) return false;
	it.contains_brks = containsBreaks(it.next);
	it.cb_init = true;
	return it.contains_brks;
    }

    public String summarize(String s) {
	if (s.length() <= 60) return s;
	return s.substring(0, 57) + "...";
    }

    public String toString() {
	if (next == null) return summarize(selfToString());
	return summarize(selfToString() + next.toString());
    }

    abstract String selfToString();
    abstract int selfMinIndent();
    abstract int selfMinWidth();
    abstract int selfMinPosWidth();
    abstract boolean selfContainsBreaks();
}

/**
 * A BlockItem is a formatting unit containing a list of other items
 * to be formatted.
 */
class BlockItem extends Item {
    BlockItem parent;
    Item first;
    Item last;
    int indent;

    //@ invariant indent >= 0
        
    BlockItem(BlockItem parent_, int indent_) {
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
	} else {
	    if (it instanceof StringItem && last instanceof StringItem) {
		StringItem lasts = (StringItem)last;
		lasts.appendString(((StringItem)it).s);
		return;
	    } else {
		last.next = it;
	    }
	}
        last = it;
    }

int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
    		boolean nofail) throws Overrun {
	if (CodeWriter.debug)
            System.err.println("BlockItem format " + this + "\n  lmargin = " +
                lmargin + " pos = " + pos + " fin = " + fin +
		(can_break ? " can break" : " no breaks") +
		(nofail ? " [nofail]" : ""));
	    // "this_fin" is a final-position bound for the formatting of
	    // the contained list, cranked in from the right margin
	    // when subsequent items overrun.
	int this_fin = rmargin - getMinPosWidth(next);
	    // Keep track of whether to send "nofail" down to contained
	    // list. Don't do this unless forced to.
	boolean this_nofail = false;
	    // Keep track of whether to send "can_break" down to contained
	    // list. Don't do this unless forced to.
	boolean this_break = false;
	while (true) {
	    int next_pos;
	    try {
	      next_pos = format(first, pos + indent, pos, rmargin,
				  this_fin, this_break, this_nofail && this_break);
	    } catch (Overrun o) {
		if (!can_break) throw o;
		if (!this_break) { this_break = true; continue; }
		if (this_nofail) throw new Error("Failed with this_nofail");
		if (nofail) { this_nofail = true; continue; }
		throw o;
	    }
	    try {
		return format(next, lmargin, next_pos, rmargin, fin,
			      can_break, nofail);
	    } catch (Overrun o) {
		if (!can_break) throw o; // no way to fix it
		if (next instanceof AllowBreak) throw o; // not our fault
		this_break = true;
		if (nofail) throw new Error("Failed with nofail");
		if (next_pos > this_fin) next_pos = this_fin;
		this_fin = next_pos - o.amount;
		if (CodeWriter.debug)
		    System.err.println("  Trying block again with fin = " +
				       this_fin);
	    }
	}
    }

    int sendOutput(Writer o, int lmargin, int pos) throws IOException {
        Item it = first;
        lmargin = pos+indent;
        while (it != null) {
            pos = it.sendOutput(o, lmargin, pos);
            it = it.next;
        }
        return pos;
    }

    void free() {
        super.free();
  
        parent = null;
        if( first != null) {
            first.free();
        }
        last = null;
    }

    int selfMinWidth() {
	return getMinWidth(first);
    }

    int selfMinPosWidth() {
	return getMinPosWidth(first);
    }

    int selfMinIndent() {
	return getMinIndent(first);
    }

    boolean self_contains_brks;
    boolean self_contains_brks_init = false;

    boolean selfContainsBreaks() {
	if (self_contains_brks_init) {
	    return self_contains_brks;
	} else {
	    return (self_contains_brks = containsBreaks(first));
	}
    }
    String selfToString() {
	if (indent == 0) return "[" + first + "]";
	else return "[" + indent + first + "]";
    }
}

class StringItem extends Item {
    String s;
    //@ invariant s != null

    //@ requires s_ != null
    StringItem(String s_) { s = s_; }
        
    int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
		boolean nofail) throws Overrun {
	return format(next, lmargin, pos + s.length(), rmargin, fin,
		      can_break, nofail);
    }
    int sendOutput(Writer o, int lm, int pos) throws IOException {
        o.write(s);
        return pos + s.length();
    }
    void appendString(String s) { this.s = this.s + s; }
    boolean selfContainsBreaks() { return false; }
    int selfMinIndent() { return s.length(); }
    int selfMinWidth() { return s.length(); }
    int selfMinPosWidth() { return s.length(); }
    String selfToString() {
	java.io.StringWriter sw = new java.io.StringWriter();
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (c == ' ') sw.write("\\ ");
	    else sw.write(c);
	}
	return sw.toString();
    }
}

class AllowBreak extends Item 
{
    int indent;
    boolean broken = true;
    String alt;
        
    //@ invariant indent >= 0
    //@ invariant alt != null

    //@ requires n_ >= 0
    //@ requires alt_ != null
    AllowBreak(int n_, String alt_) { indent = n_; alt = alt_; }
        
    int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
    		boolean nofail) throws Overrun {
        if (can_break) { pos = lmargin + indent; broken = true; }
        else { pos += alt.length(); broken = false; }
	return format(next, lmargin, pos, rmargin, fin, can_break, nofail);
    }
        
    int sendOutput(Writer o, int lmargin, int pos)
        throws IOException {
        if (broken) {
            o.write("\r\n");
            for (int i = 0; i < lmargin + indent; i++) o.write(" ");
            return lmargin + indent;
        } else {
	    o.write(alt);
            return pos + alt.length();
        }
    }
    int selfMinIndent() { return indent; }
    int selfMinPosWidth() { return 0; }
    int selfMinWidth() { return indent; }
    boolean selfContainsBreaks() { return true; }

    String selfToString() {
	if (indent == 0) return " ";
	else return "^" + indent; }
}

class Newline extends AllowBreak 
{
    //@ requires n_ >= 0
    Newline(int n_) { super(n_, ""); }
        
    int formatN(int lmargin, int pos, int rmargin, int fin, boolean can_break,
		boolean nofail) throws Overrun {
        broken = true;
	if (!can_break) throw Overrun.overrun(1);
	return format(next, lmargin, lmargin + indent, rmargin, fin,
			can_break, nofail);
    }
        
    int sendOutput(Writer o, int lmargin, int pos)
            throws IOException {
        o.write("\r\n");
        for (int i = 0; i < lmargin + indent; i++) o.write(" ");
        return lmargin + indent;
    }
}
