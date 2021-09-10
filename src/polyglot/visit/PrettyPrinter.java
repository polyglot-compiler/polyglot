/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.io.IOException;

import polyglot.ast.JLangToJLDel;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;

/**
 * A PrettyPrinter generates output code from the processed AST.
 * Output is sent to a code writer passed into the printAst method.
 *
 * To use:
 *     new PrettyPrinter().printAst(node, new CodeWriter(out));
 */
public class PrettyPrinter {
    private final Lang lang;
    protected boolean appendSemicolon = true;
    protected boolean printType = true;

    @Deprecated
    public PrettyPrinter() {
        this(JLangToJLDel.instance);
    }

    public PrettyPrinter(Lang lang) {
        this.lang = lang;
    }

    public Lang lang() {
        return this.lang;
    }

    /** Flag indicating whether to print a ';' after certain statements.
     * This is used when pretty-printing for loops. */
    public boolean appendSemicolon() {
        return appendSemicolon;
    }

    /** Set a flag indicating whether to print a ';' after certain statements.
     * This is used when pretty-printing for loops. */
    public boolean appendSemicolon(boolean a) {
        boolean old = this.appendSemicolon;
        this.appendSemicolon = a;
        return old;
    }

    /** Flag indicating whether to print the type in a local declaration.
     * This is used when pretty-printing for loops. */
    public boolean printType() {
        return printType;
    }

    /** Set a flag indicating whether to print type type in a local declaration.
     * This is used when pretty-printing for loops. */
    public boolean printType(boolean a) {
        boolean old = this.printType;
        this.printType = a;
        return old;
    }

    /** Print an AST node using the given code writer.  The
     * {@code CodeWriter.flush()} method must be called after this method
     * to ensure code is output.  Use {@code printAst} rather than this
     * method to print the entire AST; this method should only be called by
     * nodes to print their children.
     */
    public void print(Node parent, Node child, CodeWriter w) {
        if (child != null) {
            lang().prettyPrint(child, w, this);
        }
    }

    /** Print an AST node using the given code writer.  The code writer
     * is flushed by this method. */
    public void printAst(Node ast, CodeWriter w) {
        print(null, ast, w);

        try {
            w.flush();
        } catch (IOException e) {
        }
    }
}
