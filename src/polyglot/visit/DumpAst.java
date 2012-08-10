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

package polyglot.visit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import polyglot.ast.Node;
import polyglot.frontend.Compiler;
import polyglot.util.CodeWriter;

/** Visitor which dumps the AST to a file. */
public class DumpAst extends NodeVisitor {
    protected PrintWriter fw;
    protected CodeWriter w;

    /** @deprecated Use the other constructor. */
    @Deprecated
    public DumpAst(String name, int width) throws IOException {
        this.fw = new PrintWriter(new FileWriter(name));
        this.w = Compiler.createCodeWriter(fw, width);
    }

    public DumpAst(CodeWriter w) {
        this.w = w;
    }

    /** 
     * Visit each node before traversal of children. Call <code>dump</code> for
     * that node. Then we begin a new <code>CodeWriter</code> block and traverse
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
     * we must end the <code>CodeWriter</code> block that was begun in 
     * <code>enter</code>.
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
