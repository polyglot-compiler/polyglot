package polyglot.util;

import java.io.*;
import java.util.StringTokenizer;

/**
 * A <code>StdErrorQueue</code> handles outputing error messages.
 */
public class StdErrorQueue implements ErrorQueue
{
    private PrintStream err;

    private int errorCount;
    private boolean flushed;
    private int limit;
    private String name;
    
    public StdErrorQueue(PrintStream err, int limit, String name) {
	this.err = err;
	this.errorCount = 0;
	this.flushed = true;
	this.limit = limit;
	this.name = name;
    }

    public void enqueue(int type, String message) {
	enqueue(type, message, null);
    }

    public void enqueue(int type, String message, Position position) {
	enqueue(new ErrorInfo(type, message, position));
    }

    public void enqueue(ErrorInfo e) {
	if (e.getErrorKind() != ErrorInfo.WARNING) {
	    errorCount++;
	}

	flushed = false;

	String message = e.getErrorKind() != ErrorInfo.WARNING
		       ? e.getMessage()
		       : e.getErrorString() + " -- " + e.getMessage();

	Position position = e.getPosition();

	String prefix = position != null
	  		? position.nameAndLineString()
			: name;

        /*

        // This doesn't work: it breaks the error message into one word
        // per line.

        CodeWriter w = new CodeWriter(err, 78);

        w.begin(0);
        w.write(prefix + ":");
        w.write(" ");

	StringTokenizer st = new StringTokenizer(message, " ");

	while (st.hasMoreTokens()) {
	    String s = st.nextToken();
            w.write(s);
            if (st.hasMoreTokens()) {
                w.allowBreak(0, " ");
            }
        }

        w.end();
        w.newline(0);

        try {
            w.flush();
        }
        catch (IOException ex) {
        }
        */

	// I (Nate) tried without success to get CodeWriter to do this.
	// It would be nice if we could specify where breaks are allowed 
	// when generating the error.  We don't want to break Jif labels,
	// for instance.

	int width = 0;
	err.print(prefix + ":");
	width += prefix.length() + 1;

	int lmargin = 4;
	int rmargin = 78;

	StringTokenizer st = new StringTokenizer(message, " ");

	while (st.hasMoreTokens()) {
	    String s = st.nextToken();
	    
	    if (s.charAt(0)=='\n') {
		lmargin = 0; 
		width = 0;
	    }
	    if (width + s.length() + 1 > rmargin) {
		err.println();
		for (int i = 0; i < lmargin; i++) err.print(" ");
		width = lmargin;
	    }
	    else {
		err.print(" ");
		width++;
	    }

	    err.print(s);

	    width += s.length();
	}

	err.println();

	if (position != null) {
	    showError(position);
	}

	if (errorCount >= limit) {
	    prefix = position != null ? (position.file() + ": ") : "";
	    err.println(prefix + "Too many errors.  Aborting compilation.");
	    flush();
	    throw new ErrorLimitError();
	}
    }

    protected Reader reader(Position pos) throws IOException {
      if (pos.file() != null && pos.line() != Position.UNKNOWN) {
	  return new FileReader(pos.file());
      }
      return null;
    }

    private void showError(Position pos) {
      try {
        Reader r = reader(pos);

        if (r == null) {
          return;
        }

        LineNumberReader reader = new LineNumberReader(r);

        String s = null;

        for (int i = 0; i < pos.line(); i++) {
          s = reader.readLine();
        }

        reader.close();

        if (s != null) {
          err.println(s);

          if (pos.column() != Position.UNKNOWN) {
            for (int i = 0; i < pos.column(); i++) {
              if (s.charAt(i) == '\t') {
                err.print("\t");
              }
              else {
                err.print(" ");
              }
            }

            err.println("^");
          }

          err.println();
        }
      }
      catch (IOException e) {
      }
    }
    
    public void flush() {
	if (! flushed) {
            if (errorCount > 0) {
                err.println(errorCount + " error" +
                            (errorCount > 1 ? "s." : "."));
            }
	    flushed = true;
	}
    }

    public boolean hasErrors() {
      return errorCount > 0;
    }

    public int errorCount() {
        return errorCount;
    }
}
