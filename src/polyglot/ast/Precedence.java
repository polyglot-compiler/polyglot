package jltools.ast;

import jltools.util.Enum;

/**
 * Constants defining the precedence of an expression.  Higher
 * values denote higher precedence (i.e., tighter binding).
 */
public class Precedence extends Enum {
    private int value;

    protected Precedence(String name, int value) {
	super("prec_" + name);
	this.value = value;
    }

    public boolean equals(Object o) {
        return o instanceof Precedence && isSame((Precedence) o);
    }

    public boolean isSame(Precedence p) {
	return value == p.value;
    }

    public boolean isTighter(Precedence p) {
	return value < p.value;
    }

    public static final Precedence LITERAL     = new Precedence("literal", 0);
    public static final Precedence UNARY       = new Precedence("unary", 1);
    public static final Precedence CAST        = new Precedence("cast", 1);
    public static final Precedence MUL         = new Precedence("*", 2);
    public static final Precedence STRING_ADD  = new Precedence("+", 3);
    public static final Precedence ADD         = new Precedence("+", 4);
    public static final Precedence SHIFT       = new Precedence("<<", 5);
    public static final Precedence RELATIONAL  = new Precedence("<", 6);
    public static final Precedence INSTANCEOF  = new Precedence("isa", 7);
    public static final Precedence EQUAL       = new Precedence("==", 8);
    public static final Precedence BIT_AND     = new Precedence("&", 9);
    public static final Precedence BIT_XOR     = new Precedence("^", 10);
    public static final Precedence BIT_OR      = new Precedence("|", 11);
    public static final Precedence COND_AND    = new Precedence("&&", 12);
    public static final Precedence COND_OR     = new Precedence("||", 13);
    public static final Precedence CONDITIONAL = new Precedence("?:", 14);
    public static final Precedence ASSIGN      = new Precedence("=", 13);
    public static final Precedence UNKNOWN     = new Precedence("unknown", 999);
}
