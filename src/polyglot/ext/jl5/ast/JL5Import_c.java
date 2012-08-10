package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Import;
import polyglot.ast.Import_c;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.StringUtil;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5Import_c extends Import_c implements JL5Import {

    public JL5Import_c(Position pos, Import.Kind kind, String name) {
        super(pos, kind, name);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // check class is exists and is accessible
        if (kind() == JL5Import.SINGLE_STATIC_MEMBER) {
            Type nt =
                    (Type) tc.typeSystem()
                             .forName(StringUtil.getPackageComponent(name));
            String id = StringUtil.getShortNameComponent(name);
            if (!isIdStaticMember(nt.toClass(),
                                  id,
                                  (JL5TypeSystem) tc.typeSystem(),
                                  tc.context().package_())) {
                throw new SemanticException("Cannot find static member " + id
                        + " in class: " + nt, position());
            }
            // check whether there is a top level type called id.
            if (tc.job().ast() instanceof SourceFile) {
                SourceFile sf = (SourceFile) tc.job().ast();
                List<TopLevelDecl> decls = sf.decls();
                for (TopLevelDecl tld : decls) {
                    if (id.equals(tld.name())) {
                        throw new SemanticException("Static import conflicts with top level declaration "
                                                            + id,
                                                    position());
                    }
                }
            }
            return this;
        }
        else {
            return super.typeCheck(tc);
        }
    }

    private boolean isIdStaticMember(ClassType t, String id, JL5TypeSystem ts,
            Package package_) {
        if (!ts.classAccessibleFromPackage(t.toClass(), package_)) {
            return false;
        }

        try {
            FieldInstance fi = ts.findField(t, id);
            if (fi != null
                    && fi.flags().isStatic()
                    && ts.accessibleFromPackage(fi.flags(),
                                                t.package_(),
                                                package_)) {
                return true;
            }
        }
        catch (SemanticException e) {
        }

        if (ts.hasMethodNamed(t, id)) {
            List<? extends MethodInstance> meths = t.methodsNamed(id);
            boolean anyAccessible = false;
            for (MethodInstance mi : meths) {
                if (ts.accessibleFromPackage(mi.flags(), t.package_(), package_)) {
                    anyAccessible = true;
                    break;
                }
            }
            return anyAccessible;
        }

        try {
            ClassType ct = ts.findMemberClass(t, id);
            if (ct != null
                    && ct.flags().isStatic()
                    && ts.accessibleFromPackage(ct.flags(),
                                                t.package_(),
                                                package_)) {
                return true;
            }
        }
        catch (SemanticException e) {
        }

        return false;
    }

    @Override
    public String toString() {
        if (kind() == SINGLE_STATIC_MEMBER || kind() == STATIC_ON_DEMAND) {
            return "import static " + name
                    + (kind() == STATIC_ON_DEMAND ? ".*" : "");
        }
        else return super.toString();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (kind() == SINGLE_STATIC_MEMBER || kind() == STATIC_ON_DEMAND) {
            w.write("import static ");
            w.write(name);

            if (kind() == STATIC_ON_DEMAND) {
                w.write(".*");
            }

            w.write(";");
            w.newline(0);
        }
        else super.prettyPrint(w, tr);

    }
}
