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
    protected Stack lmargins;
    protected int pos;

    public SimpleCodeWriter(OutputStream o, int width_) {
        this(new PrintWriter(new OutputStreamWriter(o)), width_);
    }

    protected class State {
	public int lmargin;
	public boolean breakAll;

	State(int m, boolean b) { lmargin = m; b = breakAll; }
    }

    public SimpleCodeWriter(PrintWriter o, int width_) {
        output = o;
        width = width_;
	rmargin = width;
	adjustRmargin();
	breakAll = false;
	pos = 0;
	lmargins = new Stack();
    }

    public SimpleCodeWriter(Writer o, int width_) {
        this(new PrintWriter(o), width_);
    }
    
    private void adjustRmargin() {
	rmargin -= 8;
	if (rmargin < width/2) rmargin = width/2;
    }
        
    public void write(String s) {
       if (s == null)
	    write("null", 4);
       else if (s.length() > 0)
	    write(s, s.length());
    }

    public void write(String s, int length) {
	output.print(s);
	pos += length;
    }

    public void begin(int n) {
	lmargins.push(new State(lmargin, breakAll));
	lmargin = pos + n;
    }
        
    public void end() {
	State s = (State)lmargins.pop();
	lmargin = s.lmargin;
	breakAll = s.breakAll;
    }

    public void allowBreak(int n, int level, String alt, int altlen) {
	if (pos > width) adjustRmargin();
	if (breakAll || pos > rmargin) {
	    newline(n, 1);
	    breakAll = true;
	} else {
	    output.print(alt);
	    pos += altlen;
	}
    }
    public void unifiedBreak(int n, int level, String alt, int altlen) {
	allowBreak(n, level, alt, altlen);
    }

    private void spaces(int n) {
	for (int i = 0; i < n; i++) {
	    output.print(' ');
	}
    }
    public void newline() {
	if (pos != lmargin) {
	    output.println();
	    pos = lmargin;
	    spaces(lmargin);
	}
    }
    public void newline(int n, int level) {
	newline();
	spaces(n);
	pos += n;
    }

    public boolean flush() throws IOException {
	output.flush();
	pos = 0;
	return true;
    }

    public boolean flush(boolean format) throws IOException {
	return flush();
    }

    public void close() throws IOException {
	flush();
	output.close();
    }

    /**
     * toString is not really supported by this implementation.
     */
    public String toString() {
	return "<SimpleCodeWriter>";
    }
}

