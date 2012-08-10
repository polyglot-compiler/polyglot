package polyglot.ext.jl5.ast;

import polyglot.ast.AmbReceiver;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Field_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5Field_c extends Field_c {

    public JL5Field_c(Position pos, Receiver target, Id name) {
        super(pos, target, name);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Field n = (Field) super.typeCheck(tc);
        if (n.fieldInstance() instanceof EnumInstance) {
            // it's an enum, so replace this with the appropriate AST node for enum constants.
            JL5NodeFactory nf = (JL5NodeFactory) tc.nodeFactory();
            EnumConstant ec =
                    nf.EnumConstant(this.position(),
                                    this.target(),
                                    nf.Id(this.position, this.name()));
            ec = (EnumConstant) ec.type(this.type);
            ec = ec.enumInstance((EnumInstance) n.fieldInstance());
            n = ec;
        }
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        if (!targetImplicit) {
            // explicit target.
            if (target instanceof Expr) {
                printSubExpr((Expr) target, w, tr);
            }
            else if (target instanceof TypeNode
                    || target instanceof AmbReceiver) {
                if (tr instanceof JL5Translator) {
                    JL5Translator jltr = (JL5Translator) tr;
                    jltr.printReceiver(target, w);
                }
                else {
                    print(target, w, tr);
                }
            }

            w.write(".");
            w.allowBreak(2, 3, "", 0);
        }
        tr.print(this, name, w);
        w.end();
    }
}
