//
// Simple CodeWriter -- Andrew C. Myers, March 1999
//                      mod. by N. Sastry Sept 1999
//   For use in Cornell University Computer Science 412/413
//

package jltools.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Vector;

public class CodeWriter
{
   public static final int INDENT = 3;
   
    // A code-writer formats text onto an
    // output stream "o" while keeping the width of the output
    // within "width" characters if possible
    public CodeWriter(OutputStream o, int width_) {
        output = new OutputStreamWriter(o);
        width = width_;
        current = input = new Block(null, 0);
    }

    public CodeWriter(Writer w, int width_) {
        output = w;
        width = width_;
        current = input = new Block(null, 0);
    }
        
    public void write(String s) {
        // Print the string "s" on the output stream
        if(s != null)
          current.add(new StringItem(s));
    }
   
    public void beginBlock()
    {
       newline(INDENT);
       begin(0);
    }
   
    public void endBlock()
    {
       newline(-INDENT);
       end();
    }
    public void newline()
    {
       newline(0);
    }

    public void begin(int n) {
        // Start a new block with indentation increased
        // by "n" characters
        Block b = new Block(current, n);
        current.add(b);
        current = b;
    }
    public void end() {
        // Terminate the most recent outstanding "begin"
        current = current.parent;
        if (current == null) throw new RuntimeException();
    }
    public void allowBreak(int n) {
        // Allow a newline. Indentation will be preserved.
        current.add(new AllowBreak(n));
    }
    public void newline(int n) {
        // Force a newline. Indentation will be preserved.
        current.add(new Newline(n));
         
    }
    public void flush() throws IOException {
        // Send out the current batch of text
        // to be formatted, closing all
        // outstanding "begin"'s and resetting
        // the indentation level to 0.
        try {
            input.format(0, 0, width, true);
        } catch (Overrun e) {
        }
        input.sendOutput(output, 0, 0);
        output.flush();
        current = input = new Block(null, 0);
    }
    
    Block input;
    Block current;

    Writer output;
    int width;
}

class Overrun extends Exception {
    // An overrun represents a formatting that failed because the right
    // margin was exceeded by at least "amount" chars.
    int amount;
    Overrun(int amount_) { amount = amount_; }  
}

abstract class Item {
    Item() { next = null; }
    int format(int lmargin, int pos, int rmargin, boolean canBreak)
        throws Overrun {
        // try to format a whole sequence of items in the manner of format1
        if (pos > rmargin) throw new Overrun(pos - rmargin);
        pos = format1(lmargin, pos, rmargin, canBreak);
        if (next == null) return pos;
        else return next.format(lmargin, pos, rmargin, canBreak);
    }
    abstract int format1(int lmargin, int pos, int rmargin, boolean canBreak)
        throws Overrun;
        // Try to format this item with a current cursor position of
        // "pos", left and right margins as specified. Returns the
        // final position. If breaks
        // may be broken, "canBreak" is set. Return the new cursor
        // position and set any contained breaks appropriately if formatting
        // was successful. Requires rmargin > lmargin, pos <= rmargin.
    abstract int sendOutput(Writer o, int lmargin, int pos)
      throws IOException;
        // Send the output associated with this item to "o", using the
        // current break settings.
    Item next;
}

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
    void add(Item it) {
        if (first == null) first = it; else last.next = it;
        last = it;
    }
    int format1(int lmargin, int pos, int rmargin, boolean canBreak)
        throws Overrun
    {
        if (first == null) return pos;
        try {
            return first.format(pos + indent, pos, rmargin, false);
        } catch (Overrun overrun) {
            if (!canBreak) throw overrun;
            return first.format(pos + indent, pos, rmargin, true);
        }
    }
    int sendOutput(Writer o, int lmargin, int pos)
            throws IOException {
        Item it = first;
        lmargin = pos+indent;
        while (it != null) {
            pos = it.sendOutput(o, lmargin, pos);
            it = it.next;
        }
        return pos;
    }
}

class StringItem extends Item {
    String s;
    StringItem(String s_) { s = s_; }
    int format1(int lmargin, int pos, int rmargin, boolean canBreak) 
        throws Overrun {
        pos += s.length();
        if (pos > rmargin) throw new Overrun(rmargin - pos);
        return pos;
    }
    int sendOutput(Writer o, int lm, int pos) throws IOException {
        o.write(s);
        return pos + s.length();
    }
}

class AllowBreak extends Item {
    int indent;
    boolean broken = true;
    AllowBreak(int n_) { indent = n_; }
    int format1(int lmargin, int pos, int rmargin, boolean canBreak) 
        throws Overrun {
        if (canBreak) {
            broken = true;
            return lmargin + indent;
        } else {
            broken = false;
            return pos;
        }
    }
    int sendOutput(Writer o, int lmargin, int pos)
        throws IOException {
        if (broken) {
            o.write("\r\n");
            for (int i = 0; i < lmargin + indent; i++) o.write(" ");
            return lmargin + indent;
        } else {
            return pos;
        }
    }
}

class Newline extends AllowBreak {
    Newline(int n_) { super(n_); }
    int format1(int lmargin, int pos, int rmargin, boolean canBreak, boolean forceBreak) 
        throws Overrun {
        if (!canBreak) throw new Overrun(1);
        broken = true;
        return lmargin + indent;
    }
    int sendOutput(Writer o, int lmargin, int pos)
            throws IOException {
        o.write("\r\n");
        for (int i = 0; i < lmargin + indent; i++) o.write(" ");
        return lmargin + indent;
    }
}
