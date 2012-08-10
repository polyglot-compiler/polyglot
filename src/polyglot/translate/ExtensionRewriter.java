package polyglot.translate;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceCollection;
import polyglot.ast.SourceFile;
import polyglot.ast.TypeNode;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.qq.QQ;
import polyglot.translate.ext.ToExt;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/** Visitor which performs rewriting on the AST. */
public class ExtensionRewriter extends ContextVisitor {
    /** The ExtensionInfo of the source language */
    protected final ExtensionInfo from_ext;

    /** The ExtensionInfo of the target language */
    protected final ExtensionInfo to_ext;

    /** A quasi-quoter for generating AST node in the target language */
    protected QQ qq;

    public ExtensionRewriter(Job job, ExtensionInfo from_ext,
            ExtensionInfo to_ext) {
        super(job, from_ext.typeSystem(), to_ext.nodeFactory());
        this.job = job;
        this.from_ext = from_ext;
        this.to_ext = to_ext;
        this.qq = new QQ(to_ext);
    }

    @Override
    public NodeVisitor enterCall(Node n) throws SemanticException {
        try {
            ToExt ext = from_ext.getToExt(to_ext, n);
            return ext.toExtEnter(this);
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
            ToExt ext = from_ext.getToExt(to_ext, n);
            return ext.toExt(this);
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

    public TypeNode typeToJava(Type t, Position pos) throws SemanticException {
        NodeFactory nf = this.to_nf();
        TypeSystem ts = this.to_ts();

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
            return nf.TypeNodeFromQualifiedName(pos, t.toClass().fullName());
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
