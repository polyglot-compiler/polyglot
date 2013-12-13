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
        return this.superDel().NodeOps(this.node()).visitChildren(v);
    }

    @Override
    public Context enterScope(Context c) {
        return this.superDel().NodeOps(this.node()).enterScope(c);
    }

    @Override
    public Context enterChildScope(JLDel lang, Node child, Context c) {
        return this.superDel()
                   .NodeOps(this.node())
                   .enterChildScope(lang, child, c);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return this.superDel().NodeOps(this.node()).buildTypes(tb);
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        return this.superDel()
                   .NodeOps(this.node())
                   .disambiguateOverride(parent, ar);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return this.superDel().NodeOps(this.node()).disambiguate(ar);
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        return this.superDel()
                   .NodeOps(this.node())
                   .typeCheckOverride(parent, tc);
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        return this.superDel().NodeOps(this.node()).typeCheckEnter(tc);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return this.superDel().NodeOps(this.node()).typeCheck(tc);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        return this.superDel()
                   .NodeOps(this.node())
                   .childExpectedType(child, av);
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return this.superDel().NodeOps(this.node()).checkConstants(cc);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        this.superDel().NodeOps(this.node()).prettyPrint(w, pp);
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        if (tr instanceof JL5Translator)
            ((JL5Translator) tr).translateNode(this.node(), w);
        else this.superDel().NodeOps(this.node()).translate(w, tr);
    }
}
