package polyglot.ext.jl8.ast;

import java.util.List;
import polyglot.ast.Block;
import polyglot.ast.Expr_c;
import polyglot.ast.Ext;
import polyglot.ast.Formal;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Precedence;
import polyglot.ast.Term;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.CodeInstance;
import polyglot.types.Context;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class Lambda_c extends Expr_c implements Lambda {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Formal> formals;
    protected LambdaCodeBlock block;

    //    @Deprecated
    Lambda_c(Position pos, List<Formal> formals, Block block) {
        this(pos, formals, block, null);
    }

    Lambda_c(Position pos, List<Formal> formals, Block block, Ext ext) {
        super(pos, ext);
        this.formals = formals;
        this.block = new LambdaCodeBlock(block);
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
    public LambdaCodeBlock block() {
        return block;
    }

    @Override
    public Lambda block(LambdaCodeBlock block) {
        return block(this, block);
    }

    private MethodInstance getSAM(TypeSystem ts) {
        return block.getSAM(ts);
    }

    protected <N extends Lambda_c> N formals(N n, List<Formal> formals) {
        if (n.formals == formals) return n;
        n = copyIfNeeded(n);
        n.formals = formals;
        return n;
    }

    protected <N extends Lambda_c> N block(N n, LambdaCodeBlock block) {
        if (n.block == block) return n;
        n = copyIfNeeded(n);
        n.block = block;
        return n;
    }

    /**
     * Reconstruct the expression.
     */
    protected <N extends Lambda_c> N reconstruct(N n, List<Formal> formals, LambdaCodeBlock block) {
        n = formals(n, formals);
        n = block(n, block);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Formal> formals = visitList(this.formals, v);
        LambdaCodeBlock block = visitChild(this.block, v);
        return reconstruct(this, formals, block);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushCode(getSAM(c.typeSystem()));
    }

    @Override
    public Node overrideContextVisit(Node parent, ContextVisitor visitor) throws SemanticException {
        if (parent instanceof LocalDecl) {
            LocalDecl localDecl = (LocalDecl) parent;
            Type type = localDecl.declType();
            if (type.isCanonical()) {
                this.block.setTargetType(type, (JL8TypeSystem) visitor.context().typeSystem());
            }
        }
        return super.overrideContextVisit(parent, visitor);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(this.block.targetType);
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
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        JL8NodeFactory jl8NodeFactory = (JL8NodeFactory) nf;
        return jl8NodeFactory.Lambda(this.position, this.formals, this.block.block);
    }
}
