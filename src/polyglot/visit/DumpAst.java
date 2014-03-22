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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import polyglot.ast.JLangToJLDel;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.frontend.Compiler;
import polyglot.util.CodeWriter;

/** Visitor which dumps the AST to a file. */
public class DumpAst extends NodeVisitor {
    protected PrintWriter fw;
    protected CodeWriter w;

    @Deprecated
    public DumpAst(String name, int width) throws IOException {
        this(JLangToJLDel.instance, name, width);
    }

    /** @deprecated Use the other constructor. */
    @Deprecated
    public DumpAst(Lang lang, String name, int width) throws IOException {
        super(lang);
        this.fw = new PrintWriter(new FileWriter(name));
        this.w = Compiler.createCodeWriter(fw, width);
    }

    @Deprecated
    public DumpAst(CodeWriter w) {
        this(JLangToJLDel.instance, w);
    }

    public DumpAst(Lang lang, CodeWriter w) {
        super(lang);
        this.w = w;
    }

    /** 
     * Visit each node before traversal of children. Call {@code dump} for
     * that node. Then we begin a new {@code CodeWriter} block and traverse
     * the children.
     */
    @Override
    public NodeVisitor enter(Node n) {
        w.write("(");
        n.dump(w);
        w.allowBreak(4);
        w.begin(0);
        return this;
    }

    /**
     * This method is called only after normal traversal of the children. Thus
     * we must end the {@code CodeWriter} block that was begun in 
     * {@code enter}.
     */
    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        w.end();
        w.write(")");
        w.allowBreak(0);
        return n;
    }

    @Override
    public void finish() {
        try {
            w.flush();

            if (fw != null) {
                fw.flush();
                fw.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
