package polyglot.ext.jl8.ast;

import java.util.List;
import polyglot.ast.Block;
import polyglot.ast.Expr_c;
import polyglot.ast.Ext;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Precedence;
import polyglot.ast.Term;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class Lambda_c extends Expr_c implements Lambda {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Formal> formals;
    protected Block block;
    private transient Type targetType = null;

    //    @Deprecated
    Lambda_c(Position pos, List<Formal> formals, Block block) {
        this(pos, formals, block, null);
    }

    Lambda_c(Position pos, List<Formal> formals, Block block, Ext ext) {
        super(pos, ext);
        this.formals = formals;
        this.block = block;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LAMBDA;
    }

    @Override
    public List<Formal> formals() {
        return formals;
    }

    @Override
    public Lambda formals(List<Formal> formals) {
        return formals(this, formals);
    }

    @Override
    public Block block() {
        return block;
    }

    @Override
    public Lambda block(Block block) {
        return block(this, block);
    }

    protected <N extends Lambda_c> N formals(N n, List<Formal> formals) {
        if (n.formals == formals) return n;
        n = copyIfNeeded(n);
        n.formals = formals;
        return n;
    }

    protected <N extends Lambda_c> N block(N n, Block block) {
        if (n.block == block) return n;
        n = copyIfNeeded(n);
        n.block = block;
        return n;
    }

    /**
     * Reconstruct the expression.
     */
    protected <N extends Lambda_c> N reconstruct(N n, List<Formal> formals, Block block) {
        n = formals(n, formals);
        n = block(n, block);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Formal> formals = visitList(this.formals, v);
        Block block = visitChild(this.block, v);
        return reconstruct(this, formals, block);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        throw new Error("TODO: not implemented.");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        throw new Error("TODO: not implemented.");
    }

    @Override
    public String toString() {
        return "(" + formals + ") -> " + block;
    }

    @Override
    public Term firstChild() {
        return block;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        throw new Error("TODO: not implemented.");
    }

    @Override
    public Node copy(NodeFactory nf) {
        JL8NodeFactory jl8NodeFactory = (JL8NodeFactory) nf;
        return jl8NodeFactory.Lambda(this.position, this.formals, this.block);
    }
}
