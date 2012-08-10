/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.util.Enum;

/**
 * Constants defining the precedence of an expression.  Lower
 * values denote higher precedence (i.e., tighter binding).
 */
public class Precedence extends Enum {
    private int value;

    public Precedence(String name, int value) {
        super("prec_" + name);
        assert (value >= 0);
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    /** Returns true if this and p have the same precedence. */
    @Override
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
    public static final Precedence LITERAL = new Precedence("literal", 0);
    /** The precedence of a unary expression. */
    public static final Precedence UNARY = new Precedence("unary", 10);
    /** The precedence of a cast expression. */
    public static final Precedence CAST = new Precedence("cast", 10);
    /** The precedence of a <code>*</code>, <code>/</code>, or <code>%</code> expression. */
    public static final Precedence MUL = new Precedence("*", 20);
    /** The precedence of a <code>+</code> when applied to Strings.  This is of higher precedence than <code>+</code> applied to numbers. */
    public static final Precedence STRING_ADD = new Precedence("string+", 30);
    /** The precedence of a <code>+</code> when applied to numbers, and the precedence of <code>-</code>. */
    public static final Precedence ADD = new Precedence("+", 40);
    /** The precedence of the shift expressions <code>&lt;&lt;</code>, <code>&gt;&gt;</code>, and <code>&gt;&gt;&gt;</code>. */
    public static final Precedence SHIFT = new Precedence("<<", 50);
    /** The precedence of the relational expressions <code>&lt;</code>, <code>&gt;</code>, <code>&lt;=</code>, and <code>&gt;=</code>. */
    public static final Precedence RELATIONAL = new Precedence("<", 60);
    /** The precedence of <code>instanceof</code> expressions. */
    public static final Precedence INSTANCEOF = new Precedence("isa", 70);
    /** The precedence of equality operators.  That is, precedence of <code>==</code> and <code>!=</code> expressions. */
    public static final Precedence EQUAL = new Precedence("==", 80);
    /** The precedence of bitwise AND (<code>&amp;<code>) expressions. */
    public static final Precedence BIT_AND = new Precedence("&", 90);
    /** The precedence of bitwise XOR (<code>^<code>) expressions. */
    public static final Precedence BIT_XOR = new Precedence("^", 100);
    /** The precedence of bitwise OR (<code>|<code>) expressions. */
    public static final Precedence BIT_OR = new Precedence("|", 110);
    /** The precedence of conditional AND (<code>&&<code>) expressions. */
    public static final Precedence COND_AND = new Precedence("&&", 120);
    /** The precedence of conditional OR (<code>||<code>) expressions. */
    public static final Precedence COND_OR = new Precedence("||", 130);
    /** The precedence of ternary conditional expressions. */
    public static final Precedence CONDITIONAL = new Precedence("?:", 140);
    /** The precedence of assignment expressions. */
    public static final Precedence ASSIGN = new Precedence("=", 150);
    /** The precedence of all other expressions. This has the lowest precedence to ensure the expression is parenthesized on output. */
    public static final Precedence UNKNOWN = new Precedence("unknown", 999);
}
