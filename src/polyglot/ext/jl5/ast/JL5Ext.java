package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.JLDel;
import polyglot.ast.Node;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.ConstantChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class JL5Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static JL5Ext ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof JL5Ext)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No JL5 extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (JL5Ext) e;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        return this.superDel().visitChildren(this.node(), v);
    }

    @Override
    public Context enterScope(Context c) {
        return this.superDel().enterScope(this.node(), c);
    }

    @Override
    public Context enterChildScope(JLDel lang, Node child, Context c) {
        return this.superDel().enterChildScope(this.node(), lang, child, c);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return this.superDel().buildTypes(this.node(), tb);
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        return this.superDel().disambiguateOverride(this.node(), parent, ar);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return this.superDel().disambiguate(this.node(), ar);
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        return this.superDel().typeCheckOverride(this.node(), parent, tc);
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        return this.superDel().typeCheckEnter(this.node(), tc);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return this.superDel().typeCheck(this.node(), tc);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        return this.superDel().childExpectedType(this.node(), child, av);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return this.superDel().checkConstants(this.node(), cc);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        this.superDel().prettyPrint(this.node(), w, pp);
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        if (tr instanceof JL5Translator)
            ((JL5Translator) tr).translateNode(this.node(), w);
        else this.superDel().translate(this.node(), w, tr);
    }
}
