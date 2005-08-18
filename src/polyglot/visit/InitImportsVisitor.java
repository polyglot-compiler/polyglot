package polyglot.visit;

import java.util.*;
import java.util.HashSet;
import java.util.Stack;

import polyglot.ast.*;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.Job;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.TypeExists;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.*;

/** Visitor which traverses the AST constructing type objects. */
public class InitImportsVisitor extends ErrorHandlingVisitor
{
    protected ImportTable importTable;
    
    public InitImportsVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }
    
    public NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof SourceFile) {
            SourceFile sf = (SourceFile) n;
            
            PackageNode pn = sf.package_();
            
            ImportTable it;
            
            if (pn != null) {
                it = ts.importTable(sf.source().name(), pn.package_());
            }
            else {
                it = ts.importTable(sf.source().name(), null);
            }
            
            InitImportsVisitor v = (InitImportsVisitor) copy();
            v.importTable = it;
            return v;
        }
        
        return this;
    }
    
    public Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof SourceFile) {
            SourceFile sf = (SourceFile) n;
            InitImportsVisitor v_ = (InitImportsVisitor) v;
            ImportTable it = v_.importTable;
            return sf.importTable(it);
        }
        if (n instanceof Import) {
            Import im = (Import) n;
            
            if (im.kind() == Import.CLASS) {
                this.importTable.addClassImport(im.name());
            }
            else if (im.kind() == Import.PACKAGE) {
                this.importTable.addPackageImport(im.name());
            }
        }

        return n;
    }
}
