package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDecl_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.ext.param.types.MuPClass;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>ClassDecl</code> is the definition of a class, abstract class, or
 * interface. It may be a public or other top-level class, or an inner named
 * class, or an anonymous class.
 */
public class JL5ClassDecl_c extends ClassDecl_c implements JL5ClassDecl {

    protected List<ParamTypeNode> paramTypes;
    protected List<AnnotationElem> annotations;

    public JL5ClassDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body) {
        this(pos,
             flags,
             annotations,
             name,
             superClass,
             interfaces,
             body,
             new ArrayList<ParamTypeNode>());
    }

    public JL5ClassDecl_c(Position pos, Flags fl,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes) {
        super(pos, fl, name, superType, interfaces, body);
        if (paramTypes == null) paramTypes = new ArrayList<ParamTypeNode>();
        this.paramTypes = paramTypes;
        if (pos == null) {
            this.position = Position.compilerGenerated();
        }
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
    public List<ParamTypeNode> paramTypes() {
        return this.paramTypes;
    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return this.annotations;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5ClassDecl n = (JL5ClassDecl) super.buildTypes(tb);
        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();
        JL5ParsedClassType ct = (JL5ParsedClassType) n.type();

        MuPClass<TypeVariable, ReferenceType> pc =
                ts.mutablePClass(ct.position());
        ct.setPClass(pc);
        pc.clazz(ct);

        if (paramTypes() != null && !paramTypes().isEmpty()) {
            List<TypeVariable> typeVars =
                    new ArrayList<TypeVariable>(this.paramTypes().size());
            for (ParamTypeNode ptn : this.paramTypes()) {
                TypeVariable tv = (TypeVariable) ptn.type();
                typeVars.add(tv);
                tv.declaringClass(ct);
            }
            ct.setTypeVariables(typeVars);
            pc.formals(new ArrayList<TypeVariable>(typeVars));
        }

        return n;

    }

    public JL5ClassDecl paramTypes(List<ParamTypeNode> types) {
        JL5ClassDecl_c n = (JL5ClassDecl_c) copy();
        n.paramTypes = types;
        return n;
    }

    public JL5ClassDecl annotations(List<AnnotationElem> annotations) {
        JL5ClassDecl_c n = (JL5ClassDecl_c) copy();
        n.annotations = annotations;
        return n;
    }

    protected ClassDecl reconstruct(Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes, List<AnnotationElem> annotations) {
        if (name != this.name || superClass != this.superClass
                || !CollectionUtil.equals(interfaces, this.interfaces)
                || body != this.body
                || !CollectionUtil.equals(paramTypes, this.paramTypes)
                || !CollectionUtil.equals(annotations, this.annotations)) {

            JL5ClassDecl_c n = (JL5ClassDecl_c) copy();
            n.name = name;
            n.superClass = superClass;
            n.interfaces = ListUtil.copy(interfaces, false);
            n.body = body;
            n.paramTypes = paramTypes;
            n.annotations = annotations;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<AnnotationElem> annotations = visitList(this.annotations, v);
        List<ParamTypeNode> paramTypes = visitList(this.paramTypes, v);
        Id name = (Id) visitChild(this.name, v);
        TypeNode superClass = (TypeNode) visitChild(this.superClass, v);
        List<TypeNode> interfaces = visitList(this.interfaces, v);
        ClassBody body = (ClassBody) visitChild(this.body, v);
        return reconstruct(name,
                           superClass,
                           interfaces,
                           body,
                           paramTypes,
                           annotations);
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.ast.NodeOps#enterScope(polyglot.types.Context)
     */
    @Override
    public Context enterChildScope(Node child, Context c) {
        if (child == this.body) {
            TypeSystem ts = c.typeSystem();
            c = c.pushClass(type, ts.staticTarget(type).toClass());
        }
        else {
            // Add this class to the context, but don't push a class scope.
            // This allows us to detect loops in the inheritance
            // hierarchy, but avoids an infinite loop.
            c = ((JL5Context) c).pushExtendsClause(type);
            c.addNamed(this.type);
        }
        for (ParamTypeNode tn : paramTypes) {
            ((JL5Context) c).addTypeVariable((TypeVariable) tn.type());
        }
        return child.del().enterScope(c);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (type().superType() != null
                && JL5Flags.isEnum(type().superType().toClass().flags())) {
            throw new SemanticException("Cannot extend enum type", position());
        }

        if (ts.equals(ts.Object(), type()) && !paramTypes.isEmpty()) {
            throw new SemanticException("Type: " + type()
                    + " cannot declare type variables.", position());
        }

        if (JL5Flags.isAnnotation(flags()) && flags().isPrivate()) {
            throw new SemanticException("Annotation types cannot have explicit private modifier",
                                        this.position());
        }

        ts.checkDuplicateAnnotations(annotations);

        // check not extending java.lang.Throwable (or any of its subclasses)
        // with a generic class
        if (type().superType() != null
                && ts.isSubtype(type().superType(), ts.Throwable())
                && !paramTypes.isEmpty()) {
            // JLS 3rd ed. 8.1.2
            throw new SemanticException("Cannot subclass java.lang.Throwable or any of its subtypes with a generic class",
                                        superClass().position());
        }

        // check duplicate type variable decls
        for (int i = 0; i < paramTypes.size(); i++) {
            TypeNode ti = paramTypes.get(i);
            for (int j = i + 1; j < paramTypes.size(); j++) {
                TypeNode tj = paramTypes.get(j);
                if (ti.name().equals(tj.name())) {
                    throw new SemanticException("Duplicate type variable declaration.",
                                                tj.position());
                }
            }
        }

        // set the retained annotations
        ((JL5ParsedClassType) type()).setRetainedAnnotations(ts.createRetainedAnnotations(this.annotationElems(),
                                                                                          this.position()));

        return super.typeCheck(tc);
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {

        // check proper used of predefined annotations
        JL5TypeSystem ts = (JL5TypeSystem) annoCheck.typeSystem();
        for (AnnotationElem element : annotations) {
            ts.checkAnnotationApplicability(element, this.type());
        }

        // check annotation circularity
        if (JL5Flags.isAnnotation(flags())) {
            JL5ParsedClassType ct = (JL5ParsedClassType) type();
            for (AnnotationTypeElemInstance ai : ct.annotationElems()) {
                if (ai.type() instanceof ClassType
                        && ((ClassType) ((ClassType) ai.type()).superType()).fullName()
                                                                            .equals("java.lang.annotation.Annotation")) {
                    JL5ParsedClassType other = (JL5ParsedClassType) ai.type();
                    for (Object element2 : other.annotationElems()) {
                        AnnotationTypeElemInstance aj =
                                (AnnotationTypeElemInstance) element2;
                        if (aj.type().equals(ct)) {
                            throw new SemanticException("cyclic annotation element type",
                                                        aj.position());
                        }
                    }
                }
            }
        }
        return this;
    }

    public void prettyPrintModifiers(CodeWriter w, PrettyPrinter tr) {
        Flags f = this.flags;
        if (f.isInterface()) {
            f = f.clearInterface().clearAbstract();
        }
        if (JL5Flags.isEnum(f)) {
            f = JL5Flags.clearEnum(f).clearStatic().clearAbstract();
        }
        if (JL5Flags.isAnnotation(f)) {
            f = JL5Flags.clearAnnotation(f);
        }
        w.write(f.translate());

        if (flags.isInterface()) {
            if (JL5Flags.isAnnotation(flags)) {
                w.write("@interface ");
            }
            else {
                w.write("interface ");
            }
        }
        else if (JL5Flags.isEnum(flags)) {
            w.write("enum ");
        }
        else {
            w.write("class ");
        }
    }

    public void prettyPrintName(CodeWriter w, PrettyPrinter tr) {
        w.write(name.id());
    }

    public void prettyPrintHeaderRest(CodeWriter w, PrettyPrinter tr) {
        if (superClass() != null && !JL5Flags.isEnum(type.flags())
                && !JL5Flags.isAnnotation(type.flags())) {
            w.write(" extends ");
            print(superClass(), w, tr);
        }

        if (!interfaces.isEmpty() && !JL5Flags.isAnnotation(type.flags())) {
            if (flags.isInterface()) {
                w.write(" extends ");
            }
            else {
                w.write(" implements ");
            }

            for (Iterator<TypeNode> i = interfaces().iterator(); i.hasNext();) {
                TypeNode tn = i.next();
                print(tn, w, tr);

                if (i.hasNext()) {
                    w.write(", ");
                }
            }
        }

        w.write(" {");
    }

    @Override
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {

        w.begin(0);
        for (AnnotationElem ae : annotations) {
            ae.prettyPrint(w, tr);
            w.newline();
        }
        w.end();

        prettyPrintModifiers(w, tr);
        prettyPrintName(w, tr);
        // print type variables
        boolean printTypeVars = true;
        if (tr instanceof JL5Translator) {
            JL5Translator jl5tr = (JL5Translator) tr;
            printTypeVars = !jl5tr.removeJava5isms();
        }
        if (printTypeVars && !this.paramTypes().isEmpty()) {
            w.write("<");
            for (Iterator<ParamTypeNode> iter = this.paramTypes.iterator(); iter.hasNext();) {
                ParamTypeNode ptn = iter.next();
                ptn.prettyPrint(w, tr);
                if (iter.hasNext()) {
                    w.write(", ");
                }
            }
            w.write(">");
        }
        prettyPrintHeaderRest(w, tr);

    }
}
