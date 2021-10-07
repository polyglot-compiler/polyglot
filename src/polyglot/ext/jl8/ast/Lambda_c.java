package polyglot.ext.jl8.ast;

import java.util.List;
import polyglot.ast.Expr_c;
import polyglot.ast.Ext;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Precedence;
import polyglot.ast.Term;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class Lambda_c extends Expr_c implements Lambda {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LambdaFunctionDeclaration declaration;

    //    @Deprecated
    Lambda_c(LambdaFunctionDeclaration declaration) {
        this(declaration, null);
    }

    Lambda_c(LambdaFunctionDeclaration declaration, Ext ext) {
        super(declaration.position(), ext);
        this.declaration = declaration;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LAMBDA;
    }

    @Override
    public LambdaFunctionDeclaration declaration() {
        return this.declaration;
    }

    @Override
    public Lambda declaration(LambdaFunctionDeclaration declaration) {
        return reconstruct(this, declaration);
    }

    protected <N extends Lambda_c> N reconstruct(N n, LambdaFunctionDeclaration declaration) {
        if (n.declaration == declaration) return n;
        n = copyIfNeeded(n);
        n.declaration = declaration;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LambdaFunctionDeclaration declaration = visitChild(this.declaration, v);
        return reconstruct(this, declaration);
    }

    @Override
    public Node overrideContextVisit(Node parent, ContextVisitor visitor) throws SemanticException {
        if (parent instanceof LocalDecl) {
            LocalDecl localDecl = (LocalDecl) parent;
            Type type = localDecl.declType();
            if (type.isCanonical()) {
                this.declaration.setTargetType(
                        type,
                        (JL8TypeSystem) visitor.context().typeSystem(),
                        visitor.nodeFactory());
            }
        }
        return super.overrideContextVisit(parent, visitor);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(this.declaration.targetType);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        this.declaration.prettyPrint(w, pp);
    }

    @Override
    public String toString() {
        return this.declaration.toString();
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
        return jl8NodeFactory.Lambda(this.declaration);
    }
}
