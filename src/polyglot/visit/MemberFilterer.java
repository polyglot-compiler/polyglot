package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Replaces the body of some methods with `throw new RuntimeException(...)`,
 * and the initializers of some fields with 0 or null.
 * This can be useful for compiling large code libraries when certain
 * methods (unneeded at runtime) would otherwise fail to compile.
 *
 * For example, the JDK seems to rely on some quirks of javac type checking
 * that are not defined in the JLS. By removing the culprit JDK methods
 * entirely, we can work around the issue without having to edit the JDK.
 */
public class MemberFilterer extends NodeVisitor {
    private final TypeSystem ts;
    private final NodeFactory nf;
    private final Pattern filter;

    public MemberFilterer(Job job, TypeSystem ts, NodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
        filter = job.extensionInfo().getOptions().memberFilter;
    }

    protected String signatureFromProcedure(ProcedureDecl pd) {
        // Format: QualifiedClassName#methodName(T1, ..., Tn).
        StringBuilder sb = new StringBuilder();
        sb.append(pd.procedureInstance().container().toClass().fullName());
        sb.append('#');
        sb.append(pd.name());
        sb.append('(');
        int i = 0;
        for (Formal f : pd.formals()) {
            if (i++ > 0) sb.append(", ");
            sb.append(f.type().name());
        }
        sb.append(')');
        return sb.toString();
    }

    protected String signatureFromField(FieldDecl fd) {
        // Format: QualifiedClassName#fieldName.
        return fd.fieldInstance().container().toClass().fullName() + '#' + fd.name();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        Position pos = n.position();

        if (n instanceof ProcedureDecl) {
            ProcedureDecl pd = (ProcedureDecl) n;
            String signature = signatureFromProcedure(pd);

            if (!filter.matcher(signature).matches())
                return super.leave(old, n, v); // Not a filtered method.

            System.out.println("Removing the body of filtered method " + signature);

            // Raise runtime exception in the body if called at runtime.
            String msg =
                    String.format(
                            "%s\nThe method body of %s was removed by Polyglot at "
                                    + "compile time due to the -method-filter flag, "
                                    + "yet here it is being called at runtime.",
                            pos.toString(), signature);
            StringLit msgLit = nf.StringLit(pos, msg);
            List<Expr> exnArgs = Collections.<Expr>singletonList(msgLit);
            TypeNode exnType = nf.CanonicalTypeNode(pos, ts.RuntimeException());
            New exn = nf.New(pos, exnType, exnArgs);
            Block body = nf.Block(pos, nf.Throw(pos, exn));

            return pd.body(body);
        }

        if (n instanceof FieldDecl) {
            FieldDecl fd = (FieldDecl) n;
            String signature = signatureFromField(fd);

            if (!filter.matcher(signature).matches())
                return super.leave(old, n, v); // Not a filtered field.

            if (fd.init() == null) return super.leave(old, n, v); // No initializer.

            System.out.println("Removing the initializer of filtered field " + signature);

            // Replace initializer with zero value.
            Expr init =
                    fd.declType().isReference() ? nf.NullLit(pos) : nf.IntLit(pos, IntLit.INT, 0);
            return fd.init(init);
        }

        return super.leave(old, n, v);
    }
}
