package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.ClassBody;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.NodeFactory;
import polyglot.ast.Receiver;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;

/**
 * NodeFactory for jl5 extension.
 */
public interface JL5NodeFactory extends NodeFactory {
    JL5EnumDecl EnumDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body);

    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args,
            ClassBody body);

    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Expr> args);

    ParamTypeNode ParamTypeNode(Position pos, List<TypeNode> bounds, Id id);

    JL5ClassDecl ClassDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superType,
            List<TypeNode> interfaces, ClassBody body,
            List<ParamTypeNode> paramTypes);

    JL5ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams);

    JL5MethodDecl MethodDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            List<ParamTypeNode> typeParams);

    Formal Formal(Position pos, Flags flags, List<AnnotationElem> annotations,
            TypeNode type, Id name, boolean var_args);

    Formal Formal(Position pos, Flags flags, List<AnnotationElem> annotations,
            TypeNode type, Id name);

    LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name);

    LocalDecl LocalDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init);

    FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name);

    FieldDecl FieldDecl(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init);

    EnumConstant EnumConstant(Position pos, Receiver r, Id name);

    ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr, Stmt stmt);

    AmbTypeInstantiation AmbTypeInstantiation(Position pos, TypeNode base,
            List<TypeNode> typeArguments);

    AmbWildCard AmbWildCard(Position pos);

    AmbWildCard AmbWildCardExtends(Position pos, TypeNode extendsNode);

    AmbWildCard AmbWildCardSuper(Position pos, TypeNode superNode);

    Call Call(Position pos, Receiver target, List<TypeNode> typeArgs, Id name,
            List<Expr> args);

    New New(Position pos, List<TypeNode> typeArgs, TypeNode type,
            List<Expr> args, ClassBody body);

    New New(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body);

    ConstructorCall ConstructorCall(Position pos, Kind kind, Expr outer,
            List<? extends Expr> args, boolean isEnumSuperCall);

    ConstructorCall ThisCall(Position pos, List<TypeNode> typeArgs,
            List<Expr> args);

    ConstructorCall ThisCall(Position pos, List<TypeNode> typeArgs, Expr outer,
            List<Expr> args);

    ConstructorCall SuperCall(Position pos, List<TypeNode> typeArgs,
            List<Expr> args);

    ConstructorCall SuperCall(Position pos, List<TypeNode> typeArgs,
            Expr outer, List<Expr> args);

    ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind,
            List<TypeNode> typeArgs, List<Expr> args);

    ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind,
            List<TypeNode> typeArgs, Expr outer, List<Expr> args);

    AnnotationElemDecl AnnotationElemDecl(Position pos, Flags flags,
            TypeNode type, Id name, Expr def);

    AnnotationElem NormalAnnotationElem(Position pos, TypeNode name,
            List<ElementValuePair> elements);

    AnnotationElem MarkerAnnotationElem(Position pos, TypeNode name);

    AnnotationElem SingleElementAnnotationElem(Position pos, TypeNode name,
            Expr value);

    ElementValuePair ElementValuePair(Position pos, Id name, Expr value);

}
