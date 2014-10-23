package parser;

/**
 * A representation of counterexamples for a parser conflict.
 * Each set of counterexamples might be unified, indicated by method unified().
 * @author Chinawat
 *
 */
public class Counterexample {
    protected boolean unified;
    protected Derivation d1, d2;

    public Counterexample(Derivation d1, Derivation d2, boolean unified) {
        this.d1 = d1;
        this.d2 = d2;
        this.unified = unified;
    }

    public boolean unified() {
        return unified;
    }

    public String example1() {
        return d1.toString();
    }

    public String prettyExample1() {
        return d1.prettyPrint();
    }

    public String example2() {
        return d2.toString();
    }

    public String prettyExample2() {
        return d2.prettyPrint();
    }
}
