package polyglot.ext.jl8.ast;

import java.util.ArrayList;
import java.util.List;
import polyglot.ast.Block;
import polyglot.ast.CodeNode;
import polyglot.ast.Ext;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.CodeInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class LambdaFunctionDeclaration extends Term_c implements CodeNode {

    protected List<Formal> formals;
    protected Block block;
    protected ReferenceType targetType = null;
    // Single Abstract Method
    private transient MethodInstance sam = null;

    //    @Deprecated
    LambdaFunctionDeclaration(Position pos, List<Formal> formals, Block block) {
        this(pos, formals, block, null);
    }

    public LambdaFunctionDeclaration(Position pos, List<Formal> formals, Block block, Ext ext) {
        super(pos, ext);
        this.formals = formals;
        this.block = block;
    }

    public List<Formal> formals() {
        return formals;
    }

    public LambdaFunctionDeclaration formals(List<Formal> formals) {
        return formals(this, formals);
    }

    public Block block() {
        return block;
    }

    @Override
    public Term codeBody() {
        return block();
    }

    @Override
    public CodeInstance codeInstance() {
        return sam;
    }

    public LambdaFunctionDeclaration block(Block block) {
        return block(this, block);
    }

    private MethodInstance getSAM(TypeSystem ts) {
        if (sam != null) return sam;
        return ts.methodInstance(
                position(),
                targetType,
                Flags.NONE,
                ts.unknownType(position()),
                "",
                new ArrayList<Type>(),
                new ArrayList<Type>());
    }

    void setTargetType(Type targetType, JL8TypeSystem jl8TypeSystem) throws SemanticException {
        if (targetType.isReference()) {
            ReferenceType targetReferenceType = targetType.toReference();
            List<MethodInstance> methods =
                    jl8TypeSystem.nonObjectPublicAbstractMethods(targetReferenceType);
            if (methods.size() == 1) {
                this.targetType = targetReferenceType;
                this.sam = methods.get(0);
                return;
            }
        }
        throw new SemanticException(targetType + " is not a functional interface.");
    }

    protected <N extends LambdaFunctionDeclaration> N formals(N n, List<Formal> formals) {
        if (n.formals == formals) return n;
        n = copyIfNeeded(n);
        n.formals = formals;
        return n;
    }

    protected <N extends LambdaFunctionDeclaration> N block(N n, Block block) {
        if (n.block == block) return n;
        n = copyIfNeeded(n);
        n.block = block;
        return n;
    }

    @Override
    public Term firstChild() {
        return listChild(formals(), block());
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushCode(getSAM(c.typeSystem()));
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        throw new Error("TODO: not implemented.");
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Formal> formals = visitList(formals(), v);
        Block block = visitChild(block(), v);
        return block(formals(this, formals), block);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(formals(), block(), ENTRY);
        v.visitCFG(block(), this, EXIT);
        return succs;
    }

    @Override
    public String toString() {
        return "(" + formals + ") -> " + block;
    }
}
