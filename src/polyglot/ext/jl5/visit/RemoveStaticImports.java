package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Call;
import polyglot.ast.Field;
import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceFile;
import polyglot.ext.jl5.ast.JL5Import;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Remove static imports
 */
public class RemoveStaticImports extends ContextVisitor {
    public RemoveStaticImports(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (n instanceof SourceFile) {
            // remove the static imports
            SourceFile sf = (SourceFile) n;
            List<Import> imports = new ArrayList<Import>(sf.imports());
            boolean changed = false;
            for (Iterator<Import> iter = imports.iterator(); iter.hasNext();) {
                Import imp = iter.next();
                if (imp.kind() == JL5Import.SINGLE_STATIC_MEMBER
                        || imp.kind() == JL5Import.STATIC_ON_DEMAND) {
                    // a static import!
                    iter.remove();
                    changed = true;
                }
            }
            if (changed) {
                n = sf.imports(imports);
            }
        }

        if (n instanceof Field) {
            Field f = (Field) n;
            if (f.flags().isStatic() && f.isTargetImplicit()) {
                // check if we need to make the target explicit
                FieldInstance fi = f.fieldInstance();
                ClassType currClass = this.context().currentClass();
                // just use a reasonable approximation to figure out whether we need an explicit target
                if (currClass == null || !currClass.isSubtype(fi.container())) {
                    n = f.targetImplicit(false);
                }
            }
        }
        if (n instanceof Call) {
            Call c = (Call) n;
            MethodInstance mi = c.methodInstance();
            if (mi.flags().isStatic() && c.isTargetImplicit()) {
                // check if we need to make the target explicit
                ClassType currClass = this.context().currentClass();
                // just use a reasonable approximation to figure out whether we need an explicit target
                if (currClass == null || !currClass.isSubtype(mi.container())) {
                    n = c.targetImplicit(false);
                }
            }
        }
        return n;
    }
}
