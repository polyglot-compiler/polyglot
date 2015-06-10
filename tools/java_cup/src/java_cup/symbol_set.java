package java_cup;

import java.util.HashMap;
import java.util.Iterator;

/** This class represents a set of symbols and provides a series of
 *  set operations to manipulate them.
 *
 * @see     java_cup.symbol
 * @version last updated: 11/25/95
 * @author  Scott Hudson
 */
public class symbol_set implements Iterable<symbol> {

    /*-----------------------------------------------------------*/
    /*--- Constructor(s) ----------------------------------------*/
    /*-----------------------------------------------------------*/

    /** Constructor for an empty set. */
    public symbol_set() {
    }

    /** Constructor for cloning from another set.
     * @param other the set we are cloning from.
     */
    @SuppressWarnings("unchecked")
    public symbol_set(symbol_set other) throws internal_error {
        not_null(other);
        _all = (HashMap<String, symbol>) other._all.clone();
    }

    /*-----------------------------------------------------------*/
    /*--- (Access to) Instance Variables ------------------------*/
    /*-----------------------------------------------------------*/

    /** A hash table to hold the set. Symbols are keyed using their name string.
     */
    protected HashMap<String, symbol> _all = new HashMap<>(11);

    /** Access to all elements of the set. */
    @Override
    public Iterator<symbol> iterator() {
        return _all.values().iterator();
    }

    /** size of the set */
    public int size() {
        return _all.size();
    }

    /*-----------------------------------------------------------*/
    /*--- (Access to) Instance Variables ------------------------*/
    /*-----------------------------------------------------------*/

    /** Helper function to test for a null object and throw an exception
     *  if one is found.
     * @param obj the object we are testing.
     */
    protected void not_null(Object obj) throws internal_error {
        if (obj == null)
            throw new internal_error("Null object used in set operation");
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Determine if the set contains a particular symbol.
     * @param sym the symbol we are looking for.
     */
    public boolean contains(symbol sym) {
        return _all.containsKey(sym.name());
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Determine if this set is an (improper) subset of another.
     * @param other the set we are testing against.
     */
    public boolean is_subset_of(symbol_set other) throws internal_error {
        not_null(other);

        /* walk down our set and make sure every element is in the other */
        for (symbol s : this)
            if (!other.contains(s)) return false;

        /* they were all there */
        return true;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Determine if this set is an (improper) superset of another.
     * @param other the set we are are testing against.
     */
    public boolean is_superset_of(symbol_set other) throws internal_error {
        not_null(other);
        return other.is_subset_of(this);
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Add a single symbol to the set.
     * @param sym the symbol we are adding.
     * @return true if this changes the set.
     */
    public boolean add(symbol sym) throws internal_error {
        Object previous;

        not_null(sym);

        /* put the object in */
        previous = _all.put(sym.name(), sym);

        /* if we had a previous, this is no change */
        return previous == null;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Remove a single symbol if it is in the set.
     * @param sym the symbol we are removing.
     */
    public void remove(symbol sym) throws internal_error {
        not_null(sym);
        _all.remove(sym.name());
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Add (union) in a complete set.
     * @param other the set we are adding in.
     * @return true if this changes the set.
     */
    public boolean add(symbol_set other) throws internal_error {
        boolean result = false;

        not_null(other);

        /* walk down the other set and do the adds individually */
        for (symbol s : other)
            result = add(s) || result;

        return result;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Remove (set subtract) a complete set.
     * @param other the set we are removing.
     */
    public void remove(symbol_set other) throws internal_error {
        not_null(other);

        /* walk down the other set and do the removes individually */
        for (symbol s : other)
            remove(s);
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Equality comparison. */
    public boolean equals(symbol_set other) {
        if (other == null || other.size() != size()) return false;

        /* once we know they are the same size, then improper subset does test */
        try {
            return is_subset_of(other);
        }
        catch (internal_error e) {
            /* can't throw the error (because super class doesn't), so we crash */
            e.crash();
            return false;
        }
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Generic equality comparison. */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof symbol_set))
            return false;
        else return equals((symbol_set) other);
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Compute a hash code. */
    @Override
    public int hashCode() {
        int result = 0;

        for (symbol s : this)
            result ^= s.hashCode();

        return result;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** Convert to a string. */
    @Override
    public String toString() {
        String result;
        boolean comma_flag;

        result = "{";
        comma_flag = false;
        for (symbol s : this) {
            if (comma_flag)
                result += ", ";
            else comma_flag = true;

            result += s.name();
        }
        result += "}";

        return result;
    }

    /*-----------------------------------------------------------*/

}
