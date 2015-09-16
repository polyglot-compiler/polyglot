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
package polyglot.translate;

import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceCollection;
import polyglot.ast.SourceFile;
import polyglot.ast.TypeNode;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.qq.QQ;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * The ExtensionRewriter translates AST nodes created by one extension's
 * NodeFactory to nodes created by a target extension's NodeFactory.
 * Extensions using this visitor should add the extension factory
 * ToExtFactory_c to their node factory.
 *
 * After the AST of a source file is rewritten, a new job for the
 * translated AST is enqueued in the scheduler of the target extension.
 * Since this job has not been parsed from a source file, the usual
 * pattern is to subclass the target extension's scheduler in order to
 * substitute an empty pass for the Parsed compiler goal.
 * @see JLOutputExtensionInfo for an example.
 */
public class ExtensionRewriter extends ContextVisitor {
    /** The ExtensionInfo of the source language */
    protected final ExtensionInfo from_ext;

    /** The ExtensionInfo of the target language */
    protected final ExtensionInfo to_ext;

    /** A quasi-quoter for generating AST node in the target language */
    protected QQ qq;

    /** The language dispatcher for the AST */
    private Lang lang;

    public ExtensionRewriter(Job job, ExtensionInfo from_ext,
            ExtensionInfo to_ext) {
        super(job, from_ext.typeSystem(), to_ext.nodeFactory());
        this.job = job;
        this.from_ext = from_ext;
        this.to_ext = to_ext;
        qq = new QQ(to_ext);
        rethrowMissingDependencies = true;
        lang = from_ext.nodeFactory().lang();
    }

    @Override
    public Lang lang() {
        return lang;
    }

    @Override
    public Node override(Node parent, Node n) {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::override " + n);

        Node m = lang().extRewriteOverride(n, this);

        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::override " + n + " -> " + m);
        if (m == null) {
            return super.override(parent, n);
        }
        else {
            return m;
        }
    }

    @Override
    public NodeVisitor enterCall(Node n) throws SemanticException {
        try {
            return lang().extRewriteEnter(n, this);
        }
        catch (SemanticException e) {
            Position position = e.position();

            if (position == null) {
                position = n.position();
            }

            errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
                                 e.getMessage(),
                                 position);

            return this;
        }
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        try {
            return lang().extRewrite(n, this);
        }
        catch (SemanticException e) {
            Position position = e.position();

            if (position == null) {
                position = n.position();
            }

            errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
                                 e.getMessage(),
                                 position);

            return n;
        }
    }

    @Override
    protected void addDecls(Node old, Node n) {
        // Use the old node to add the declarations, as the new node, n, doesn't
        // have type information.
        addDecls(old);
    }

    @Override
    public void finish(Node ast) {
        if (ast instanceof SourceCollection) {
            SourceCollection c = (SourceCollection) ast;
            for (SourceFile sf : c.sources()) {
                to_ext.scheduler().addJob(sf.source(), sf);
            }
        }
        else {
            to_ext.scheduler().addJob(job.source(), ast);
        }
        lang = to_ext.nodeFactory().lang();
    }

    public ExtensionInfo from_ext() {
        return from_ext;
    }

    public TypeSystem from_ts() {
        return from_ext.typeSystem();
    }

    public NodeFactory from_nf() {
        return from_ext.nodeFactory();
    }

    public ExtensionInfo to_ext() {
        return to_ext;
    }

    public TypeSystem to_ts() {
        return to_ext.typeSystem();
    }

    public NodeFactory to_nf() {
        return to_ext.nodeFactory();
    }

    @Override
    public ErrorQueue errorQueue() {
        return job.compiler().errorQueue();
    }

    public TypeNode typeToJava(Type t, Position pos) {
        NodeFactory nf = to_nf();
        TypeSystem ts = to_ts();

        if (t.isNull()) return canonical(nf, ts.Null(), pos);
        if (t.isVoid()) return canonical(nf, ts.Void(), pos);
        if (t.isBoolean()) return canonical(nf, ts.Boolean(), pos);
        if (t.isByte()) return canonical(nf, ts.Byte(), pos);
        if (t.isChar()) return canonical(nf, ts.Char(), pos);
        if (t.isShort()) return canonical(nf, ts.Short(), pos);
        if (t.isInt()) return canonical(nf, ts.Int(), pos);
        if (t.isLong()) return canonical(nf, ts.Long(), pos);
        if (t.isFloat()) return canonical(nf, ts.Float(), pos);
        if (t.isDouble()) return canonical(nf, ts.Double(), pos);

        if (t.isArray()) {
            return nf.ArrayTypeNode(pos, typeToJava(t.toArray().base(), pos));
        }

        if (t.isClass()) {
            return nf.TypeNodeFromQualifiedName(pos, t.translate(context));
        }

        throw new InternalCompilerError("Cannot translate type " + t + ".");
    }

    protected TypeNode canonical(NodeFactory nf, Type t, Position pos) {
        return nf.CanonicalTypeNode(pos, t);
    }

    public QQ qq() {
        return qq;
    }

}
