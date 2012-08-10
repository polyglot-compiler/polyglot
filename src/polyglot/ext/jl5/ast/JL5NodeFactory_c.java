package polyglot.ext.jl5.ast;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.ArrayInit;
import polyglot.ast.Binary;
import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Case;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassLit;
import polyglot.ast.ClassMember;
import polyglot.ast.Conditional;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.Disamb;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Import;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.NewArray;
import polyglot.ast.NodeFactory_c;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.Switch;
import polyglot.ast.SwitchElement;
import polyglot.ast.TypeNode;
import polyglot.ast.Unary;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * NodeFactory for jl5 extension.
 */
public class JL5NodeFactory_c extends NodeFactory_c implements JL5NodeFactory {
    public JL5NodeFactory_c() {
        super(new JL5ExtFactory_c(), new JL5DelFactory_c());
    }

    public JL5NodeFactory_c(JL5ExtFactory extFactory) {
        super(extFactory, new JL5DelFactory_c());
    }

    public JL5NodeFactory_c(JL5ExtFactory extFactory, JL5DelFactory delFactory) {
        super(extFactory, delFactory);
    }

    @Override
    public JL5ExtFactory extFactory() {
        return (JL5ExtFactory) super.extFactory();
    }

    @Override
    public JL5DelFactory delFactory() {
        return (JL5DelFactory) super.delFactory();
    }

    @Override
    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (!type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical "
                    + "type node for a non-canonical type.");
        }

        CanonicalTypeNode n = new JL5CanonicalTypeNode_c(pos, type);
        n = (CanonicalTypeNode) n.ext(extFactory().extCanonicalTypeNode());
        n = (CanonicalTypeNode) n.del(delFactory().delCanonicalTypeNode());
        return n;
    }

    @Override
    public JL5EnumDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body) {
        JL5EnumDecl n =
                new JL5EnumDecl_c(pos,
                                  flags,
                                  annotations,
                                  name,
                                  superType,
                                  interfaces,
                                  body);
        n = (JL5EnumDecl) n.ext(extFactory().extEnumDecl());
        n = (JL5EnumDecl) n.del(delFactory().delEnumDecl());
        return n;

    }

    @Override
    public AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Expr defaultValue) {
        AnnotationElemDecl n =
                new AnnotationElemDecl_c(pos, flags, type, name, defaultValue);
        n = (AnnotationElemDecl) n.ext(extFactory().extAnnotationElemDecl());
        n = (AnnotationElemDecl) n.del(delFactory().delAnnotationElemDecl());
        return n;
    }

    @Override
    public AnnotationElem NormalAnnotationElem(Position pos, TypeNode name,
            List<ElementValuePair> elements) {
        AnnotationElem n = new AnnotationElem_c(pos, name, elements);
        n = (AnnotationElem) n.ext(extFactory().extNormalAnnotationElem());
        n = (AnnotationElem) n.del(delFactory().delNormalAnnotationElem());
        return n;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnnotationElem MarkerAnnotationElem(Position pos, TypeNode name) {
        return NormalAnnotationElem(pos, name, Collections.EMPTY_LIST);
    }

    @Override
    public AnnotationElem SingleElementAnnotationElem(Position pos,
            TypeNode name, Expr value) {
        List<ElementValuePair> l = new LinkedList<ElementValuePair>();
        l.add(ElementValuePair(pos, this.Id(pos, "value"), value));
        return NormalAnnotationElem(pos, name, l);
    }

    @Override
    public ElementValuePair ElementValuePair(Position pos, Id name, Expr value) {
        ElementValuePair n = new ElementValuePair_c(pos, name, value);
        n = (ElementValuePair) n.ext(extFactory().extElementValuePair());
        n = (ElementValuePair) n.del(delFactory().delElementValuePair());
        return n;
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        return ClassDecl(pos,
                         flags,
                         Collections.<AnnotationElem> emptyList(),
                         name,
                         superClass,
                         interfaces,
                         body,
                         Collections.<ParamTypeNode> emptyList());
    }

    @Override
    public JL5ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes) {
        JL5ClassDecl n =
                new JL5ClassDecl_c(pos,
                                   flags,
                                   annotations,
                                   name,
                                   superType,
                                   interfaces,
                                   body,
                                   paramTypes);
        n = (JL5ClassDecl) n.ext(extFactory().extClassDecl());
        n = (JL5ClassDecl) n.del(delFactory().delClassDecl());
        return n;
    }

    @Override
    public ClassBody ClassBody(Position pos, List<ClassMember> members) {
        ClassBody n =
                new JL5ClassBody_c(pos, CollectionUtil.nonNullList(members));
        n = (ClassBody) n.ext(extFactory().extClassBody());
        n = (ClassBody) n.del(delFactory().delClassBody());
        return n;
    }

    @Override
    public ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr,
            Stmt stmt) {
        ExtendedFor n = new ExtendedFor_c(pos, decl, expr, stmt);
        n = (ExtendedFor) n.ext(extFactory().extExtendedFor());
        n = (ExtendedFor) n.del(delFactory().delExtendedFor());
        return n;
    }

    @Override
    public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body) {
        EnumConstantDecl n =
                new EnumConstantDecl_c(pos,
                                       flags,
                                       annotations,
                                       name,
                                       args,
                                       body);
        n = (EnumConstantDecl) n.ext(extFactory().extEnumConstantDecl());
        n = (EnumConstantDecl) n.del(delFactory().delEnumConstantDecl());
        return n;
    }

    @Override
    public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args) {
        EnumConstantDecl n =
                new EnumConstantDecl_c(pos,
                                       flags,
                                       annotations,
                                       name,
                                       args,
                                       null);
        n = (EnumConstantDecl) n.ext(extFactory().extEnumConstantDecl());
        n = (EnumConstantDecl) n.del(delFactory().delEnumConstantDecl());
        return n;
    }

    @Override
    public EnumConstant EnumConstant(Position pos, Receiver target, Id name) {
        EnumConstant n = new EnumConstant_c(pos, target, name);
        n = (EnumConstant) n.ext(extFactory().extEnumConstant());
        n = (EnumConstant) n.del(delFactory().delEnumConstant());
        return n;
    }

    @Override
    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Binary n = new JL5Binary_c(pos, left, op, right);
        n = (Binary) n.ext(extFactory().extBinary());
        n = (Binary) n.del(delFactory().delBinary());
        return n;
    }

    @Override
    public ClassLit ClassLit(Position pos, TypeNode typeNode) {
        ClassLit n = new JL5ClassLit_c(pos, typeNode);
        n = (ClassLit) n.ext(extFactory().extClassLit());
        n = (ClassLit) n.del(delFactory().delClassLit());
        return n;
    }

    @Override
    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Unary n = new JL5Unary_c(pos, op, expr);
        n = (Unary) n.ext(extFactory().extUnary());
        n = (Unary) n.del(delFactory().delUnary());
        return n;
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind, Expr outer,
            List<Expr> args) {
        return ConstructorCall(pos, kind, outer, args, false);
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind, Expr outer,
            List<? extends Expr> args, boolean isEnumConstructorCall) {
        return ConstructorCall(pos,
                               kind,
                               null,
                               outer,
                               args,
                               isEnumConstructorCall);
    }

    protected ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<? extends Expr> args,
            boolean isEnumConstructorCall) {

        ConstructorCall n =
                new JL5ConstructorCall_c(pos,
                                         kind,
                                         typeArgs,
                                         outer,
                                         args,
                                         isEnumConstructorCall);
        n = (ConstructorCall) n.ext(extFactory().extConstructorCall());
        n = (ConstructorCall) n.del(delFactory().delConstructorCall());
        return n;
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args) {
        return ConstructorCall(pos, kind, typeArgs, outer, args, false);
    }

    @Override
    public JL5ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            Id name, List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        JL5ConstructorDecl n =
                ConstructorDecl(pos,
                                flags,
                                null,
                                name,
                                formals,
                                throwTypes,
                                body,
                                null);
        return n;
    }

    @Override
    public JL5ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        JL5ConstructorDecl n;
        if (typeParams == null) {
            n =
                    new JL5ConstructorDecl_c(pos,
                                             flags,
                                             annotations,
                                             name,
                                             formals,
                                             throwTypes,
                                             body);
        }
        else {
            n =
                    new JL5ConstructorDecl_c(pos,
                                             flags,
                                             annotations,
                                             name,
                                             formals,
                                             throwTypes,
                                             body,
                                             typeParams);
        }
        n = (JL5ConstructorDecl) n.ext(extFactory().extConstructorDecl());
        n = (JL5ConstructorDecl) n.del(delFactory().delConstructorDecl());
        return n;
    }

    @Override
    public Disamb disamb() {
        Disamb n = new JL5Disamb_c();
        return n;
    }

    @Override
    public JL5MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        JL5MethodDecl n =
                MethodDecl(pos,
                           flags,
                           null,
                           returnType,
                           name,
                           formals,
                           throwTypes,
                           body,
                           null);
        return n;
    }

    @Override
    public JL5MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams) {
        JL5MethodDecl n;
        if (typeParams == null) {
            n =
                    new JL5MethodDecl_c(pos,
                                        flags,
                                        annotations,
                                        returnType,
                                        name,
                                        formals,
                                        throwTypes,
                                        body);
        }
        else {
            n =
                    new JL5MethodDecl_c(pos,
                                        flags,
                                        annotations,
                                        returnType,
                                        name,
                                        formals,
                                        throwTypes,
                                        body,
                                        typeParams);
        }
        n = (JL5MethodDecl) n.ext(extFactory().extMethodDecl());
        n = (JL5MethodDecl) n.del(delFactory().delMethodDecl());
        return n;
    }

    @Override
    public ParamTypeNode ParamTypeNode(Position pos, List<TypeNode> bounds,
            Id id) {
        ParamTypeNode n = new ParamTypeNode_c(pos, bounds, id);
        n = (ParamTypeNode) n.ext(extFactory().extParamTypeNode());
        n = (ParamTypeNode) n.del(delFactory().delParamTypeNode());
        return n;
    }

    @Override
    public AmbTypeInstantiation AmbTypeInstantiation(Position pos,
            TypeNode base, List<TypeNode> typeArguments) {
        AmbTypeInstantiation n =
                new AmbTypeInstantiation(pos, base, typeArguments);
        return n;
    }

    @Override
    public JL5Import Import(Position pos, Import.Kind kind, String name) {
        JL5Import n = new JL5Import_c(pos, kind, name);
        n = (JL5Import) n.ext(extFactory().extImport());
        n = (JL5Import) n.del(delFactory().delImport());
        return n;
    }

    @Override
    public Formal Formal(Position pos, Flags flags, TypeNode type, Id name) {
        return Formal(pos, flags, null, type, name);
    }

    @Override
    public Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        Formal n = Formal(pos, flags, annotations, type, name, false);
        n = (Formal) n.ext(extFactory().extFormal());
        n = (Formal) n.del(delFactory().delFormal());
        return n;
    }

    @Override
    public Formal Formal(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean varArgs) {
        Formal n =
                new JL5Formal_c(pos, flags, annotations, type, name, varArgs);
        n = (Formal) n.ext(extFactory().extFormal());
        n = (Formal) n.del(delFactory().delFormal());
        return n;
    }

    @Override
    public Switch Switch(Position pos, Expr expr, List<SwitchElement> elements) {
        Switch n = new JL5Switch_c(pos, expr, elements);
        n = (Switch) n.ext(extFactory().extSwitch());
        n = (Switch) n.del(delFactory().delSwitch());
        return n;
    }

    @Override
    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Special n = new JL5Special_c(pos, kind, outer);
        n = (Special) n.ext(extFactory().extSpecial());
        n = (Special) n.del(delFactory().delSpecial());
        return n;
    }

    @Override
    public Conditional Conditional(Position pos, Expr cond, Expr consequent,
            Expr alternative) {
        Conditional n =
                new JL5Conditional_c(pos, cond, consequent, alternative);
        n = (Conditional) n.ext(extFactory().extConditional());
        n = (Conditional) n.del(delFactory().delConditional());
        return n;
    }

    @Override
    public Case Case(Position pos, Expr expr) {
        Case n = new JL5Case_c(pos, expr);
        n = (Case) n.ext(extFactory().extCase());
        n = (Case) n.del(delFactory().delCase());
        return n;
    }

    @Override
    public AmbWildCard AmbWildCard(Position pos) {
        AmbWildCard n = new AmbWildCard(pos);
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardExtends(Position pos, TypeNode extendsNode) {
        AmbWildCard n = new AmbWildCard(pos, extendsNode, true);
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardSuper(Position pos, TypeNode superNode) {
        AmbWildCard n = new AmbWildCard(pos, superNode, false);
        return n;
    }

    @Override
    public Call Call(Position pos, Receiver target, Id name, List<Expr> args) {
        Call n = Call(pos, target, null, name, args);
        n = (Call) n.ext(extFactory().extCall());
        n = (Call) n.del(delFactory().delCall());
        return n;
    }

    @Override
    public Call Call(Position pos, Receiver target, List<TypeNode> typeArgs,
            Id name, List<Expr> args) {
        Call n =
                new JL5Call_c(pos,
                              target,
                              CollectionUtil.nonNullList(typeArgs),
                              name,
                              CollectionUtil.nonNullList(args));
        n = (Call) n.ext(extFactory().extCall());
        n = (Call) n.del(delFactory().delCall());
        return n;
    }

    @Override
    public New New(Position pos, List<TypeNode> typeArgs, TypeNode type,
            List<Expr> args, ClassBody body) {
        New n = this.New(pos, null, typeArgs, type, args, body);
        n = (New) n.ext(extFactory().extNew());
        n = (New) n.del(delFactory().delNew());
        return n;
    }

    @Override
    public New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body) {
        New n =
                new JL5New_c(pos,
                             outer,
                             CollectionUtil.nonNullList(typeArgs),
                             objectType,
                             CollectionUtil.nonNullList(args),
                             body);
        n = (New) n.ext(extFactory().extNew());
        n = (New) n.del(delFactory().delNew());
        return n;
    }

    @Override
    public New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args, ClassBody body) {
        New n = this.New(pos, outer, null, objectType, args, body);
        n = (New) n.ext(extFactory().extNew());
        n = (New) n.del(delFactory().delNew());
        return n;
    }

    @Override
    public NewArray NewArray(Position pos, TypeNode base, List<Expr> dims,
            int addDims, ArrayInit init) {
        NewArray n =
                new JL5NewArray_c(pos,
                                  base,
                                  CollectionUtil.nonNullList(dims),
                                  addDims,
                                  init);
        n = (NewArray) n.ext(extFactory().extNewArray());
        n = (NewArray) n.del(delFactory().delNewArray());
        return n;
    }

    @Override
    public Field Field(Position pos, Receiver target, Id name) {
        Field n = new JL5Field_c(pos, target, name);
        n = (Field) n.ext(extFactory().extField());
        n = (Field) n.del(delFactory().delField());
        return n;
    }

    @Override
    public ConstructorCall ThisCall(Position pos, List<TypeNode> typeArgs,
            List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.THIS, typeArgs, null, args);
    }

    @Override
    public ConstructorCall ThisCall(Position pos, List<TypeNode> typeArgs,
            Expr outer, List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.THIS, typeArgs, outer, args);
    }

    @Override
    public ConstructorCall SuperCall(Position pos, List<TypeNode> typeArgs,
            List<Expr> args) {
        return ConstructorCall(pos, ConstructorCall.SUPER, typeArgs, null, args);
    }

    @Override
    public ConstructorCall SuperCall(Position pos, List<TypeNode> typeArgs,
            Expr outer, List<Expr> args) {
        return ConstructorCall(pos,
                               ConstructorCall.SUPER,
                               typeArgs,
                               outer,
                               args);
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind,
            List<TypeNode> typeArgs, List<Expr> args) {
        return ConstructorCall(pos, kind, typeArgs, null, args);
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        return LocalDecl(pos, flags, annotations, type, name, null);
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        LocalDecl n =
                new JL5LocalDecl_c(pos, flags, annotations, type, name, init);
        n = (LocalDecl) n.ext(extFactory().extLocalDecl());
        n = (LocalDecl) n.del(delFactory().delLocalDecl());
        return n;
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        return LocalDecl(pos, flags, null, type, name, init);
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        return FieldDecl(pos, flags, annotations, type, name, null);
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        return FieldDecl(pos, flags, null, type, name, init);
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        FieldDecl n =
                new JL5FieldDecl_c(pos, flags, annotations, type, name, init);
        n = (FieldDecl) n.ext(extFactory().extFieldDecl());
        n = (FieldDecl) n.del(delFactory().delFieldDecl());
        return n;
    }

}
