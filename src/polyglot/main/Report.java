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
   * <code>level</code> should be reported, based on use of the
   * -report command-line switches given by the user.
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

  /**
   * Whether a message on the topic "topic" at level "level" should
   * be reported.
   */
  public static boolean should_report(String topic, int level) {
    if (Options.global.level("verbose") >= level) return true;
    return (topics != null && Options.global.level(topic) >= level);
  }

  /** This is the standard way to report debugging information in the
   *  compiler.  It reports a message of the specified level (which
   *  controls the presentation of the message. To test whether such
   *  message should be reported, use "should_report"
   *
   *  NOTE: This is a change of spec from earlier versions of Report.
   */
  public static void report(int level, String message) {
    for (int j = 1; j < level; j++) System.err.print("  ");
    System.err.println(message);
  }
}
