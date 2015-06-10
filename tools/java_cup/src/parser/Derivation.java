package parser;

import java.util.List;

import java_cup.symbol;

/**
 * A derivation consists of
 * - a symbol
 * - a list of derivations that derives the symbol
 * In other words, a derivation is a tree, where the symbol is the value at the
 * current node, and the list of derivations forms the node's children.
 */
class Derivation {
    /**
     * A special derivation indicating the dot within the parser state.
     */
    public static final Derivation dot = new Derivation(new symbol("•") {
        @Override
        public boolean is_non_term() {
            throw new UnsupportedOperationException();
        }
    });

    protected final symbol sym;
    protected final List<Derivation> deriv;

    /**
     * Construct a leaf node of a derivation tree.
     * @param sym The symbol to appeat as a leaf
     */
    protected Derivation(symbol sym) {
        this(sym, null);
    }

    /**
     * Construct an internal derivation node.
     * @param sym The symbol at the root of this derivation subtree
     * @param deriv The derivation of the given symbol
     */
    protected Derivation(symbol sym, List<Derivation> deriv) {
        this.sym = sym;
        this.deriv = deriv;
    }

    /**
     *
     * @return The number of nodes within this derivation tree.
     */
    protected int size() {
        int size = 1;
        if (deriv != null) {
            for (Derivation d : deriv)
                size += d.size();
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(sym.name());
        if (deriv != null) {
            sb.append(" ::= [");
            boolean tail = false;
            for (Derivation d : deriv) {
                if (tail)
                    sb.append(" ");
                else tail = true;
                sb.append(d);
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     *
     * @return The sequence of symbols at the leaves of this derivation tree.
     */
    public String prettyPrint() {
        if (deriv == null) return sym.name();

        StringBuffer sb = new StringBuffer();
        for (Derivation d : deriv) {
            String pp = d.prettyPrint();
            if (pp.length() != 0) {
                if (sb.length() != 0) sb.append(" ");
                sb.append(pp);
            }
        }
        return sb.toString();
    }

    protected boolean equals(Derivation d) {
        return sym == d.sym;
    }

    @Override
    public int hashCode() {
        return sym.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Derivation) return equals((Derivation) o);
        return false;
    }
}
