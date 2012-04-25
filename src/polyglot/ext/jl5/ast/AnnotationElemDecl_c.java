package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.*;
import polyglot.ext.jl5.types.*;
import polyglot.types.*;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.*;

public class AnnotationElemDecl_c extends Term_c implements AnnotationElemDecl {

    protected TypeNode type;
    protected Flags flags;
    protected Expr defaultVal;
    protected Id name;
    protected AnnotationElemInstance ai;
    
    public AnnotationElemDecl_c(Position pos, Flags flags, TypeNode type, Id name, Expr defaultVal){
        super(pos);
        this.type = type;
        this.flags = flags;
        this.defaultVal = defaultVal;
        this.name = name;
    }
    
    public AnnotationElemDecl type(TypeNode type){
        if (!type.equals(this.type)){ 
            AnnotationElemDecl_c n = (AnnotationElemDecl_c) copy();
            n.type = type;
            return n;
        }
        return this;
    }
    
    public TypeNode type(){
        return type;
    }
    
    public AnnotationElemDecl flags(Flags flags){
        if (!flags.equals(this.flags)){
            AnnotationElemDecl_c n = (AnnotationElemDecl_c) copy();
            n.flags = flags;
            return n;
        }
        return this;
    }
    
    public Flags flags(){
        return flags;
    }

    public AnnotationElemDecl defaultVal(Expr def){
        if (!def.equals(this.defaultVal)){
            AnnotationElemDecl_c n = (AnnotationElemDecl_c) copy();
            n.defaultVal = def;
            return n;
        }
        return this;
    }
    
    public Expr defaultVal(){
        return defaultVal;
    }

    public AnnotationElemDecl name(String name){
        if (!name.equals(this.name())){
            AnnotationElemDecl_c n = (AnnotationElemDecl_c) copy();
            n.name = this.name.id(name);
            return n;
        }
        return this;
    }
    
    public String name(){
        return this.name.id();
    }

    public AnnotationElemDecl annotationElemInstance(AnnotationElemInstance ai){
        AnnotationElemDecl_c n = (AnnotationElemDecl_c) copy();
        n.ai = ai;
        return n;
    }

    public AnnotationElemInstance annotationElemInstance(){
        return ai;
    }
    
    protected AnnotationElemDecl_c reconstruct(TypeNode type, Expr defaultVal) {
        if (!type.equals(this.type) || this.defaultVal != defaultVal){
            AnnotationElemDecl_c n = (AnnotationElemDecl_c) copy();
            n.type = type;
            n.defaultVal = defaultVal;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v){
        TypeNode type = (TypeNode) visitChild( this.type, v);
        Expr defVal = (Expr)visitChild(this.defaultVal, v);
        return reconstruct(type, defVal);
    }

    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        // this may not be neccessary - I think this is for scopes for
        // symbol checking? - in fields and meths there many anon inner 
        // classes and thus a scope is needed - but in annots there 
        // cannot be ???
        return tb.pushCode();
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem)tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return this;
        }

        Flags f = this.flags;
        f = f.Public().Abstract();

        AnnotationElemInstance ai = ts.annotationElemInstance(position(), ct, f, ts.unknownType(position()), this.name(), defaultVal != null);
        ct.addMethod(ai);
        return annotationElemInstance(ai);
    }
    
    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (this.ai.isCanonical()) {
            // already done
            return this;
        }

        if (! returnType().isDisambiguated()) {
            return this;
        }

        ai.setReturnType(returnType().type());

        return this;
    }

    
    public Node typeCheck(TypeChecker tc) throws SemanticException {
    
        JL5TypeSystem ts = (JL5TypeSystem)tc.typeSystem();
        
        // check type - must be one of primitive, String, Class, 
        // enum, annotation or array or one of these
        if (!ts.isValidAnnotationValueType(type().type())){
            throw new SemanticException("The type: "+this.type()+" for the annotation element declaration "+this.name()+" must be a primitive, String, Class, enum type, annotation type or an array of one of these.", type().position());
        }
  
        // an annotation element cannot have the same type as the 
        // type it is declared in - direct
        // also need to check indirect cycles
        if (type().type().equals(tc.context().currentClass())){
            throw new SemanticException("Cyclic annotation element type: "+type(), type().position());
        }

        // check default value matches type
        if (defaultVal != null){
            if (defaultVal instanceof ArrayInit){
                ((ArrayInit)defaultVal).typeCheckElements(type.type());
            }
            else {
                boolean intConversion = false;
                if (! ts.isImplicitCastValid(defaultVal.type(), type.type()) &&
                    ! ts.equals(defaultVal.type(), type.type()) &&
                    ! ts.numericConversionValid(type.type(), defaultVal.constantValue()) &&
                    ! ts.isBaseCastValid(defaultVal.type(), type.type()) &&
                    ! ts.numericConversionBaseValid(type.type(), defaultVal.constantValue())){
                    throw new SemanticException("The type of the default value: "+defaultVal+" does not match the annotation element type: "+type.type()+" .", defaultVal.position());
                }
            }
        }

        if (flags.contains(Flags.NATIVE) ){
            throw new SemanticException("Modifier native is not allowed here", position());
        }
        if (flags.contains(Flags.PRIVATE) ){
            throw new SemanticException("Modifier private is not allowed here", position());
        }

        if (defaultVal != null) ts.checkAnnotationValueConstant(defaultVal);
        return this;
    }
    

    
    public List acceptCFG(CFGBuilder v, List succs){
        if (defaultVal != null) {
            v.visitCFG(defaultVal, this, EXIT);
        }
        return succs;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        
        Flags f = flags();
        f = f.clearPublic();
        f = f.clearAbstract();
        
        w.write(f.translate());
        print(type, w, tr);
        w.write(" "+name.id()+"( )");
        if (defaultVal != null){
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
    public List formals() {
        return Collections.emptyList();
    }

    @Override
    public MethodDecl formals(List formals) {
        if (!formals.isEmpty()) {
            throw new InternalCompilerError("Shouldn't have an Annotation Elem with formals");
        }
        return this;
    }

    @Override
    public List throwTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MethodDecl throwTypes(List throwTypes) {
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
        return this.annotationElemInstance((AnnotationElemInstance)mi);
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
    public Id id() {
        return this.name;
    }

    @Override
    public MethodDecl id(Id name) {
        if (this.name != name) {
            AnnotationElemDecl_c n = (AnnotationElemDecl_c) copy();
            n.name = name;
            return n;
        }
        return this;
    }
}
