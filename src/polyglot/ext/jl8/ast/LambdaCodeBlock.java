package polyglot.ext.jl8.ast;

import java.util.ArrayList;
import java.util.List;
import polyglot.ast.Block;
import polyglot.ast.CodeNode;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.CodeInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;

public class LambdaCodeBlock extends Term_c implements CodeNode {

    protected Block block;
    protected ReferenceType targetType = null;
    // Single Abstract Method
    private transient MethodInstance sam = null;

    public LambdaCodeBlock(Block block) {
        super(block.position());
        this.block = block;
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

    public LambdaCodeBlock block(Block block) {
        return block(this, block);
    }

    MethodInstance getSAM(TypeSystem ts) {
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

    void setTargetType(Type targetType, JL8TypeSystem jl8TypeSystem)
            throws SemanticException {
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

    protected <N extends LambdaCodeBlock> N block(N n, Block block) {
        if (n.block == block) return n;
        n = copyIfNeeded(n);
        n.block = block;
        return n;
    }

    @Override
    public Term firstChild() {
        return block;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        return block(visitChild(block, v));
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(block, this, EXIT);
        return succs;
    }

    @Override
    public String toString() {
        return block.toString();
    }
}
