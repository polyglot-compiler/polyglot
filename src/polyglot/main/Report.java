package polyglot.main;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/** Class used for reporting debug messages. */
public class Report {
  /** A collection of string names of topics which can be used with the
      -report command-line switch */
  public static Collection topics = new HashSet();

  /**
   * Return whether a message on <code>topics</code> of obscurity
   * <code>level</code> should be reported, based on the command-line
   * switches given by the user. This method is occasionally useful
   * when the computation of the message to be reported is expensive.
   */
  public static boolean should_report(Collection topics, int level) {
    if (Options.global.level("verbose") >= level) return true;
    if (topics == null) {
	// if (Options.global.level("verbose") >= level) return true;
    } else {
	for (Iterator i = topics.iterator(); i.hasNext();) {
	    String topic = (String) i.next();
	    if (Options.global.level(topic) >= level) return true;
	}
    }
    return false;
  }

  /** This is the standard way to report debugging information in the
   *  compiler.  It conditionally reports a message if it is related to
   *  one of the specified topics. The variable <code>topics</code> is a
   *  collection of strings.  The obscurity of the message is indicated
   *  by <code>level</code>.  The message is reported only if the user
   *  has requested (via the -report command-line option) that messages
   *  of that obscurity be reported for one of the specified topics.
   */
  public static void report(Collection topics, int level, String message) {
    if (should_report(topics, level)) {
	for (int j = 1; j < level; j++) System.err.print("  ");
	System.err.println(message);
    }
  }
}
