package java_cup;

/* Defines integers that represent the associativity of terminals
 * @version last updated: 7/3/96
 * @author  Frank Flannery
 */

public class assoc {

    /* various associativities, no_prec being the default value */
    public static final int left = 0;
    public static final int right = 1;
    public static final int nonassoc = 2;
    public static final int no_prec = -1;
}
