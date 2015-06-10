/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl5.ast;

import java.util.Collections;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.CodeBlock;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Javadoc;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.CodeInstance;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ProcedureInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class AnnotationElemDecl_c extends Term_c implements AnnotationElemDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode type;
    protected Flags flags;
    protected Term defaultVal;
    protected Id name;
    protected AnnotationTypeElemInstance ai;
    protected Javadoc javadoc;

    public AnnotationElemDecl_c(Position pos, Flags flags, TypeNode type,
            Id name, Term defaultVal, Javadoc javadoc) {
        super(pos);
        this.type = type;
        this.flags = flags;
        this.defaultVal = defaultVal;
        this.name = name;
        this.javadoc = javadoc;
    }

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public AnnotationElemDecl_c(Position pos, Flags flags, TypeNode type,
            Id name, Term defaultVal) {
        this(pos, flags, type, name, defaultVal, null);
    }

    @Override
    public TypeNode type() {
        return type;
    }

    @Override
    public AnnotationElemDecl type(TypeNode type) {
        return type(this, type);
    }

    protected <N extends AnnotationElemDecl_c> N type(N n, TypeNode type) {
        if (n.type.equals(type)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.type = type;
        return n;
    }

    @Override
    public Flags flags() {
        return flags;
    }

    @Override
    public AnnotationElemDecl flags(Flags flags) {
        return flags(this, flags);
    }

    protected <N extends AnnotationElemDecl_c> N flags(N n, Flags flags) {
        if (n.flags.equals(flags)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.flags = flags;
        return n;
    }

    @Override
    public Term defaultVal() {
        return defaultVal;
    }

    @Override
    public AnnotationElemDecl defaultVal(Term def) {
        return defaultVal(this, def);
    }

    protected <N extends AnnotationElemDecl_c> N defaultVal(N n, Term defaultVal) {
        if (n.defaultVal == defaultVal) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.defaultVal = defaultVal;
        return n;
    }

    @Override
    public Id id() {
        return this.name;
    }

    @Override
    public AnnotationElemDecl id(Id name) {
        return id(this, name);
    }

    protected <N extends AnnotationElemDecl_c> N id(N n, Id name) {
        if (n.name.equals(name)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return this.name.id();
    }

    @Override
    public AnnotationElemDecl name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public AnnotationTypeElemInstance annotationElemInstance() {
        return ai;
    }

    @Override
    public AnnotationElemDecl annotationElemInstance(
            AnnotationTypeElemInstance ai) {
        return annotationElemInstance(this, ai);
    }

    protected <N extends AnnotationElemDecl_c> N annotationElemInstance(N n,
            AnnotationTypeElemInstance ai) {
        if (n.ai == ai) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.ai = ai;
        return n;
    }

    protected AnnotationElemDecl_c reconstruct(TypeNode type, Term defaultVal) {
        AnnotationElemDecl_c n = this;
        n = type(n, type);
        n = defaultVal(n, defaultVal);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = visitChild(this.type, v);
        Term defVal = visitChild(this.defaultVal, v);
        return reconstruct(type, defVal);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        // this may not be necessary - I think this is for scopes for
        // symbol checking? - in fields and methods there many anon inner 
        // classes and thus a scope is needed - but in annots there 
        // cannot be ???
        return tb.pushCode();
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        JL5ParsedClassType ct = (JL5ParsedClassType) tb.currentClass();

        if (ct == null) {
            return this;
        }

        Flags f = this.flags;
        f = f.Public().Abstract();

        AnnotationTypeElemInstance ai =
                ts.annotationElemInstance(position(),
                                          ct,
                                          f,
                                          ts.unknownType(position()),
                                          this.name(),
                                          defaultVal != null);
        ct.addAnnotationElem(ai);

        return annotationElemInstance(ai);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (this.ai.isCanonical()) {
            // already done
            return this;
        }

        if (!returnType().isDisambiguated()) {
            return this;
        }

        ai.setReturnType(returnType().type());

        return this;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {

        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        // check type - must be one of primitive, String, Class, 
        // enum, annotation or array or one of these
        if (!ts.isValidAnnotationValueType(type().type())) {
            throw new SemanticException("The type: "
                                                + this.type()
                                                + " for the annotation element declaration "
                                                + this.name()
                                                + " must be a primitive, String, Class, enum type, annotation type or an array of one of these.",
                                        type().position());
        }

        // an annotation element cannot have the same type as the 
        // type it is declared in - direct
        // also need to check indirect cycles
        if (type().type().equals(tc.context().currentClass())) {
            throw new SemanticException("Cyclic annotation element type: "
                    + type(), type().position());
        }

        // check default value matches type
        if (defaultVal != null) {
            Type defaultValType;
            if (defaultVal instanceof Expr) {
                defaultValType = ((Expr) defaultVal).type();
            }
            else if (defaultVal instanceof ElementValueArrayInit) {
                ElementValueArrayInit evai = (ElementValueArrayInit) defaultVal;
                defaultValType = evai.type();
            }
            else if (defaultVal instanceof AnnotationElem) {
                defaultValType =
                        ((AnnotationElem) defaultVal).typeName().type();
            }
            else {
                throw new InternalCompilerError("Don't know how to deal with default value ("
                                                        + defaultVal
                                                        + ") of kind "
                                                        + defaultVal.getClass(),
                                                defaultVal.position());
            }
            if (defaultVal instanceof ElementValueArrayInit) {
                ((ElementValueArrayInit) defaultVal).typeCheckElements(tc,
                                                                       type.type());
            }
            else {
                if (!ts.isImplicitCastValid(defaultValType, type.type())
                        && !ts.equals(defaultValType, type.type())
                        && !(defaultVal instanceof Expr && ts.numericConversionValid(type.type(),
                                                                                     tc.lang()
                                                                                       .constantValue((Expr) defaultVal,
                                                                                                      tc.lang())))
                        && !ts.isBaseCastValid(defaultValType, type.type())
                        && !(defaultVal instanceof Expr && ts.numericConversionBaseValid(type.type(),
                                                                                         tc.lang()
                                                                                           .constantValue((Expr) defaultVal,
                                                                                                          tc.lang())))) {
                    throw new SemanticException("The type of the default value: "
                                                        + defaultVal
                                                        + " does not match the annotation element type: "
                                                        + type.type() + " .",
                                                defaultVal.position());
                }
            }
        }

        if (flags.contains(Flags.NATIVE)) {
            throw new SemanticException("Modifier native is not allowed here",
                                        position());
        }
        if (flags.contains(Flags.PRIVATE)) {
            throw new SemanticException("Modifier private is not allowed here",
                                        position());
        }

        if (defaultVal != null) ts.checkAnnotationValueConstant(defaultVal);
        return this;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (defaultVal != null) {
            v.visitCFG(defaultVal, this, EXIT);
        }
        return succs;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);

        Flags f = flags();
        f = f.clearPublic();
        f = f.clearAbstract();

        w.write(f.translate());
        print(type, w, tr);
        w.write(" " + name.id() + "( )");
        if (defaultVal != null) {
            w.write(" default ");
            print(defaultVal, w, tr);
        }
        w.write(";");
        w.end();
    }

    @Override
    public String toString() {
        return flags.translate() + type + " " + name.id() + "()";
    }

    @Override
    public MemberInstance memberInstance() {
        return this.annotationElemInstance();
    }

    @Override
    public Term firstChild() {
        return this.type;
    }

    @Override
    public TypeNode returnType() {
        return this.type();
    }

    @Override
    public MethodDecl returnType(TypeNode returnType) {
        return this.type(returnType);
    }

    @Override
    public List<Formal> formals() {
        return Collections.emptyList();
    }

    @Override
    public MethodDecl formals(List<Formal> formals) {
        if (!formals.isEmpty()) {
            throw new InternalCompilerError("Shouldn't have an Annotation Elem with formals");
        }
        return this;
    }

    @Override
    public List<TypeNode> throwTypes() {
        return Collections.emptyList();
    }

    @Override
    public MethodDecl throwTypes(List<TypeNode> throwTypes) {
        if (!throwTypes.isEmpty()) {
            throw new InternalCompilerError("Shouldn't have an Annotation Elem with throw types");
        }
        return this;
    }

    @Override
    public MethodInstance methodInstance() {
        return this.annotationElemInstance();
    }

    @Override
    public MethodDecl methodInstance(MethodInstance mi) {
        return this.annotationElemInstance((AnnotationTypeElemInstance) mi);
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return this.annotationElemInstance();
    }

    @Override
    public Block body() {
        return null;
    }

    @Override
    public CodeBlock body(Block body) {
        if (body != null) {
            throw new InternalCompilerError("Shouldn't have an Annotation Elem with a body");
        }
        return this;
    }

    @Override
    public Term codeBody() {
        return null;
    }

    @Override
    public CodeInstance codeInstance() {
        return this.annotationElemInstance();
    }

    @Override
    public AnnotationElemDecl javadoc(Javadoc javadoc) {
        return javadoc(this, javadoc);
    }

    protected <N extends AnnotationElemDecl_c> N javadoc(N n, Javadoc javadoc) {
        if (n.javadoc == javadoc) return n;
        n = copyIfNeeded(n);
        n.javadoc = javadoc;
        return n;
    }

    @Override
    public Javadoc javadoc() {
        return javadoc;
    }
}
