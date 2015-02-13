package parser;

/**
 * A representation of counterexamples for a parser conflict.
 * Each set of counterexamples might be unifying, indicated by method unified().
 *
 */
public class Counterexample {
    protected boolean unifying;
    protected boolean timeout;
    protected Derivation d1, d2;

    /**
     * Construct a counterexample representation.
     * @param d1 One possible derivation of the counterexample
     * @param d2 Another possible derivation of the counterexample
     * @param unifying true if the counterexample is unifying, false otherwise
     */
    public Counterexample(Derivation d1, Derivation d2, boolean unifying) {
        this(d1, d2, unifying, false);
    }

    /**
     * Construct a counterexample representation that also encapsulates whether
     * the time limit was exceeded when the counterexample was being searched.
     * @param d1 One possible derivation of the counterexample
     * @param d2 Another possible derivation of the counterexample
     * @param unifying true if the counterexample is unifying, false otherwise
     * @param timeout true if the timeout was reached, resulting in a
     *          nonunifying counterexample, false otherwise
     */
    public Counterexample(Derivation d1, Derivation d2, boolean unifying,
            boolean timeout) {
        this.d1 = d1;
        this.d2 = d2;
        this.unifying = unifying;
        this.timeout = timeout;
    }

    /**
     *
     * @return true if the counterexample is unifying, i.e., the two different
     *          derivations yield an identical sequence of symbols indicating
     *          an ambiguous grammar; false otherwise
     */
    public boolean unifying() {
        return unifying;
    }

    /**
     *
     * @return true if the timeout was reached when the counterexample was
     *          being searched; false otherwise
     */
    public boolean timeout() {
        return timeout;
    }

    /**
     *
     * @return The nonterminal that this counterexample derives from.
     */
    public String cexNonterminal() {
        return d1.sym.name();
    }

    /**
     *
     * @return The string representation of the first derivation tree.
     */
    public String example1() {
        return d1.toString();
    }

    /**
     *
     * @return The sequence of symbols at the leaves of the first derivation
     *          tree.
     */
    public String prettyExample1() {
        return d1.prettyPrint();
    }

    /**
     *
     * @return The string representation of the second derivation tree.
     */
    public String example2() {
        return d2.toString();
    }

    /**
     *
     * @return The sequence of symbols at the leaves of the second derivation
     *          tree.
     */
    public String prettyExample2() {
        return d2.prettyPrint();
    }
}
