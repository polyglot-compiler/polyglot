package polyglot.ast;

import polyglot.util.Enum;

/**
 * Constants defining the precedence of an expression.  Higher
 * values denote higher precedence (i.e., tighter binding).
 */
public class Precedence extends Enum {
    private int value;

    public Precedence(String name, int value) {
	super("prec_" + name);
	this.value = value;
    }

    /** Returns true if this and p have the same precedence. */
    public boolean equals(Object o) {
        return o instanceof Precedence && equals((Precedence) o);
    }

    /** Returns true if this and p have the same precedence. */
    public boolean equals(Precedence p) {
	return value == p.value;
    }

    /** Returns true if this binds tighter than p. */
    public boolean isTighter(Precedence p) {
	return value < p.value;
    }

    /** The precedence of a literal */
    public static final Precedence LITERAL     = new Precedence("literal", 0);
    /** The precedence of a unary expression. */
    public static final Precedence UNARY       = new Precedence("unary", 1);
    /** The precedence of a cast expression. */
    public static final Precedence CAST        = new Precedence("cast", 1);
    /** The precedence of a <code>*</code>, <code>/</code>, or <code>%</code> expression. */
    public static final Precedence MUL         = new Precedence("*", 2);
    /** The precedence of a <code>+</code> when applied to Strings.  This is of higher precedence than <code>+</code> applied to numbers. */
    public static final Precedence STRING_ADD  = new Precedence("string+", 3);
    /** The precedence of a <code>+</code> when applied to numbers, and the precedence of <code>-</code>. */
    public static final Precedence ADD         = new Precedence("+", 4);
    /** The precedence of the shift expressions <code>&lt;&lt;</code>, <code>&gt;&gt;</code>, and <code>&gt;&gt;&gt;</code>. */
    public static final Precedence SHIFT       = new Precedence("<<", 5);
    /** The precedence of the relational expressions <code>&lt;</code>, <code>&gt;</code>, <code>&lt;=</code>, and <code>&gt;=</code>. */
    public static final Precedence RELATIONAL  = new Precedence("<", 6);
    /** The precedence of <code>instanceof</code> expressions. */
    public static final Precedence INSTANCEOF  = new Precedence("isa", 7);
    /** The precedence of equality operators.  That is, precedence of <code>==</code> and <code>!=</code> expressions. */
    public static final Precedence EQUAL       = new Precedence("==", 8);
    /** The precedence of bitwise AND (<code>&amp;<code>) expressions. */
    public static final Precedence BIT_AND     = new Precedence("&", 9);
    /** The precedence of bitwise XOR (<code>^<code>) expressions. */
    public static final Precedence BIT_XOR     = new Precedence("^", 10);
    /** The precedence of bitwise OR (<code>|<code>) expressions. */
    public static final Precedence BIT_OR      = new Precedence("|", 11);
    /** The precedence of conditional AND (<code>&&<code>) expressions. */
    public static final Precedence COND_AND    = new Precedence("&&", 12);
    /** The precedence of conditional OR (<code>||<code>) expressions. */
    public static final Precedence COND_OR     = new Precedence("||", 13);
    /** The precedence of ternary conditional expressions. */
    public static final Precedence CONDITIONAL = new Precedence("?:", 14);
    /** The precedence of assignment expressions. */
    public static final Precedence ASSIGN      = new Precedence("=", 13);
    /** The precedence of all other expressions. This has the lowest precedence to ensure the expression is parenthesized on output. */
    public static final Precedence UNKNOWN     = new Precedence("unknown", 999);
}
