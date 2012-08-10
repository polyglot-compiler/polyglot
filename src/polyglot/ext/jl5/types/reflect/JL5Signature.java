package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JL5Signature extends Attribute {
    protected DataInputStream in;
    protected int index;
    protected ClassFile cls;
    protected JL5TypeSystem ts;
    protected Position position;
    protected ClassSig classSignature;
    protected MethodSig methodSignature;
    protected FieldSig fieldSignature;
    protected List<TypeVariable> typeVars;
    protected ClassType curClass;

    /**
     * Grammar:
     * class_sig = 
     *  formal_type_params_opt super_class_sig super_inter_sig_list_opt
     * formal_type_params_opt =
     *  * empty *
     *  | LEFT_ANGLE formal_type_param_list RIGHT_ANGLE
     * formal_type_param_list =
     *  formal_type_param
     *  | formal_type_param_list formal_type_param
     * formal_type_param =
     *  ID class_bound inter_bound_list_opt
     * class_bound =
     *  COLON field_type_sig_opt
     * inter_bound_list_opt = 
     *  * empty *
     *  | inter_bound_list
     * inter_bound_list = 
     *  inter_bound
     *  inter_bound_list inter_bound
     * inter_bound = 
     *  COLON field_type_sig
     * super_class_sig =
     *  class_type_sig
     * super_inter_sig_list_opt = 
     *  * empty *
     *  | super_inter_sig_list
     * super_inter_sig_list =
     *  super_inter_sig
     *  | super_inter_sig_list | super_inter_sig
     * super_inter_sig =
     *  class_type_sig
     * field_type_sig =
     *  class_type_sig
     *  | array_type_sig
     *  | type_var_sig
     * class_type_sig =
     *  L pack_spec_list_opt simple_class_type_sig 
     *      class_type_sig_suffix_list_opt SEMI_COLON
     * pack_spec_list_opt =
     *  * empty *
     *  | pack_spec_list
     * pack_spec_list =
     *  pack_spec
     *  | pack_spec_list pack_spec
     * pack_spec =
     *  ID SLASH
     * simple_class_type_sig = 
     *  ID type_args_opt
     * class_type_sig_suffix_list_opt =
     *  * empty *
     *  | class_type_sig_suffix_list
     * class_type_sig_suffix_list =
     *  class_type_sig_suffix
     *  | class_type_sig_suffix_list class_type_sig_suffix
     * class_type_sig_suffix =
     *  DOT simple_class_type_sig
     * type_var_sig =
     *  T ID SEMI_COLON
     * type_args =
     *  LEFT_ANGLE type_arg_list RIGHT_ANGLE
     * type_arg_list = 
     *  type_arg
     *  | type_arg_list type_arg
     * type_arg =
     *  wild_card_ind_opt field_type_sig
     *  | STAR
     * wild_card_ind_opt = 
     *  * empty *
     *  | wild_card_ind
     * wild_card_ind = 
     *  PLUS
     *  | MINUS
     * array_type_sig =
     *  LEFT_SQUARE type_sig
     * type_sig =
     *  field_type_sig
     *  | base_type
     * 
     * method_type_sig =
     *  formal_type_params_opt LEFT_BRACE type_sig_list_opt RIGHT_BRACE 
     *      return_type throws_sig_list_opt
     * return_type =
     *  type_sig
     *  | V
     * throws_sig_list_opt = 
     *  * empty *
     *  | throws_sig_list
     * throws_sig_list =
     *  throws_sig 
     *  | throws_sig_list throws_sig
     * throws_sig =
     *  HAT class_type_sig
     *  | HAT type_var_sig
     * 
     * base_type =
     *  B | C | D | F | I | J | S | Z 
     *  
     */

    JL5Signature(ClassFile clazz, DataInputStream in, int nameIndex, int length)
            throws IOException {
        super(nameIndex, length);
        this.index = in.readUnsignedShort();
        this.cls = clazz;
    }

    // tokens
    private final char LEFT_ANGLE = '<';
    private final char RIGHT_ANGLE = '>';
    private final char COLON = ':';
    private final char L = 'L';
    private final char SEMI_COLON = ';';
    private final char SLASH = '/';
    private final char DOT = '.';
    private final char T = 'T';
    private final char STAR = '*';
    private final char PLUS = '+';
    private final char MINUS = '-';
    private final char LEFT_SQUARE = '[';
    private final char LEFT_BRACE = '(';
    private final char RIGHT_BRACE = ')';
    private final char V = 'V';
    private final char HAT = '^';
    private final char B = 'B';
    private final char C = 'C';
    private final char D = 'D';
    private final char F = 'F';
    private final char I = 'I';
    private final char J = 'J';
    private final char S = 'S';
    private final char Z = 'Z';
    private boolean createTypeVars;

    class ClassSig {
        public ClassSig(List<TypeVariable> typeVars, Type superType,
                List<ClassType> interfaces) {
            this.typeVars = typeVars;
            this.superType = superType;
            this.interfaces = interfaces;
        }

        protected List<TypeVariable> typeVars; // list of intersection types

        public List<TypeVariable> typeVars() {
            return typeVars;
        }

        protected Type superType;

        public Type superType() {
            return superType;
        }

        protected List<ClassType> interfaces; // list of types 

        public List<ClassType> interfaces() {
            return interfaces;
        }
    }

    class MethodSig {
        public MethodSig(List<TypeVariable> typeVars, List<Type> formalTypes,
                Type returnType, List<ReferenceType> throwTypes) {
            this.typeVars = typeVars;
            this.formalTypes = formalTypes;
            this.returnType = returnType;
            this.throwTypes = throwTypes;
        }

        protected List<TypeVariable> typeVars; // list of intersection types

        public List<TypeVariable> typeVars() {
            return typeVars;
        }

        protected List<Type> formalTypes; // list of types

        public List<Type> formalTypes() {
            return formalTypes;
        }

        protected Type returnType;

        public Type returnType() {
            return returnType;
        }

        protected List<ReferenceType> throwTypes; // list of types

        public List<ReferenceType> throwTypes() {
            return throwTypes;
        }
    }

    class FieldSig {
        protected Type type;
    }

    class Result<T> {
        public Result(T result, int pos) {
            this.result = result;
            this.pos = pos;
        }

        protected int pos;
        protected T result;

        public int pos() {
            return pos;
        }

        public T result() {
            return result;
        }
    }

    public Result<ClassSig> classSig(String value, int pos) {
        char token = value.charAt(pos);

        Result<List<TypeVariable>> fres = null;
        if (token == LEFT_ANGLE) {
            //fres = formalTypeParamList(value, ++pos);
            fres = useFormalTypeParamList(value, ++pos);
            pos = fres.pos();
            //typeVars = (List)fres.result();
        }
        Result<ClassType> sres = classTypeSig(value, pos);
        List<ClassType> superInterfaces = new ArrayList<ClassType>();
        pos = sres.pos();
        while (pos < value.length()) {
            Result<ClassType> ires = classTypeSig(value, pos);
            pos = ires.pos();
            superInterfaces.add(ires.result());
            //pos++;
        }
        return new Result<ClassSig>(new ClassSig(fres == null ? new ArrayList<TypeVariable>()
                                                         : (List<TypeVariable>) fres.result(),
                                                 sres.result(),
                                                 superInterfaces),
                                    pos);
    }

    public Result<List<TypeVariable>> createFormalTypeParamList(String value,
            int pos) {
        typeVars = null;
        createTypeVars = true;
        //do it twice. second time, bounds will be resolved
        List<TypeVariable> list = new ArrayList<TypeVariable>();
        char token = value.charAt(pos);
        while (token != RIGHT_ANGLE) {
            Result<TypeVariable> fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }
        pos++;
        return new Result<List<TypeVariable>>(list, pos);
    }

    public Result<List<TypeVariable>> useFormalTypeParamList(String value,
            int pos) {
        createTypeVars = false;
        //do it twice. second time, bounds will be resolved
        List<TypeVariable> list = new ArrayList<TypeVariable>();
        char token = value.charAt(pos);
        while (token != RIGHT_ANGLE) {
            Result<TypeVariable> fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }
        pos++;
        return new Result<List<TypeVariable>>(list, pos);
    }

    public Result<List<TypeVariable>> formalTypeParamList(String value, int pos) {
        //System.err.println("### Parsing formal type param list " + value.substring(pos));
        typeVars = null;
        int oldpos = pos;
        createTypeVars = true;
        //do it twice. second time, bounds will be resolved
        List<TypeVariable> list = new ArrayList<TypeVariable>();
        char token = value.charAt(pos);
        while (token != RIGHT_ANGLE) {
            Result<TypeVariable> fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }

        typeVars = list;
        pos = oldpos;
        createTypeVars = false;
        list = new ArrayList<TypeVariable>();
        token = value.charAt(pos);
        while (token != RIGHT_ANGLE) {
            Result<TypeVariable> fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }

        pos++;
        return new Result<List<TypeVariable>>(list, pos);
    }

    // ID classBound interfaceBoundList(opt)
    public Result<TypeVariable> formalTypeParam(String value, int pos) {
        //System.err.println("### Parsing formalTypeParam " + value.substring(pos));
        String id = "";
        char token = value.charAt(pos);
        while (token != COLON) {
            id += token;
            pos++;
            token = value.charAt(pos);
        }
        Result<? extends ReferenceType> cres = classBound(value, pos);
        pos = cres.pos();
        Result<? extends ReferenceType> ires = null;
        List<ReferenceType> bounds = new ArrayList<ReferenceType>();
        token = value.charAt(pos);
        while (token != RIGHT_ANGLE) {
            if (value.charAt(pos) != COLON) break;
            ires = classBound(value, pos);
            pos = ires.pos();
            bounds.add(ires.result());
        }
        if (createTypeVars) {
            //System.err.println("\ncreating type variable " + id + "\n");
            return new Result<TypeVariable>(ts.typeVariable(position,
                                                            id,
                                                            ts.intersectionType(position,
                                                                                bounds)),
                                            pos);
        }
        else {
            TypeVariable tv = findTypeVar(id);
            tv.setUpperBound(ts.intersectionType(position, bounds));
            return new Result<TypeVariable>(tv, pos);
        }
    }

    // : fieldTypeSig
    public Result<? extends ReferenceType> classBound(String value, int pos) {
        return fieldTypeSig(value, ++pos);
    }

    // classTypeSig L...;
    // typeVarSig T...;
    // arrayTypeSig [...;
    public Result<? extends ReferenceType> fieldTypeSig(String value, int pos) {
        //System.err.println("### Parsing field type sig " + value.substring(pos));
        char token = value.charAt(pos);
        switch (token) {
        case L:
            return classTypeSig(value, pos);
        case LEFT_SQUARE:
            return arrayTypeSig(value, pos);
        case T:
            return typeVarSig(value, pos);
        case COLON:
            return new Result<ClassType>(ts.Object(), pos);
        default:
            return null;
        }
    }

    public Result<ClassType> classTypeSig(String value, int pos) {
        char token = value.charAt(pos); // L
        String className = "";
        String id = "";
        Map<String, List<ReferenceType>> classArgsMap =
                new HashMap<String, List<ReferenceType>>();
        pos++;
        token = value.charAt(pos);
        while (token != SEMI_COLON) {
            switch (token) {
            case SLASH: { // id is a package 
                className += id;
                className += ".";
                id = "";
                pos++;
                token = value.charAt(pos);
                break;
            }
            case DOT: { // id is a className
                className += id;
                className += "$";
                id = "";
                pos++;
                token = value.charAt(pos);
                break;
            }
            case LEFT_ANGLE: { // id is a className
                               //System.err.println("Parsing type arg list " + value.substring(pos));
                Result<List<ReferenceType>> tres = typeArgList(value, pos);
                //System.err.println("Got " + tres.result());
                pos = tres.pos();
                classArgsMap.put(id, tres.result());
                //System.err.println("Adding " + tres.result() + " to map for " + id);
                token = value.charAt(pos);
                break;
            }
            default: {
                id += token;
                pos++;
                token = value.charAt(pos);
                break;
            }
            }
        }
        className += id;
        ClassType ct = null;
        try {
            //System.err.println("<><> " + className);
            //ct = (ClassType) ts.typeForName(className);
            //ct = cls.typeForName()
            ct = (ClassType) ts.systemResolver().find(className);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("could not load " + className, e);
        }
        // look up in the map the last part of className, i.e., the part after the last '.'.
        // This means that java.util.Map$Entry will go to 
        String lookupClassName =
                className.substring(className.lastIndexOf('.') + 1);
        //System.err.println("Now looking up " + ct.name() + " with class name '" + className +"'  in classArgsMap " + classArgsMap + "  " + classArgsMap.containsKey(className) + "  " + createTypeVars);        
//        if (!className.equals(ct.name())) {
//            System.err.println("---- uh oh:   " + className + "   " + ct.name() +"   " + lookupClassName +"  " +classArgsMap);
//        }
        if (classArgsMap.containsKey(lookupClassName)) {
            JL5ParsedClassType pct = parsedClassTypeForClass(ct);
            if (!createTypeVars) {
                try {
                    ct =
                            ts.instantiate(position,
                                           pct,
                                           classArgsMap.get(lookupClassName));
                }
                catch (SemanticException e) {
                    throw new InternalCompilerError(e);
                }
            }
        }
        else {
            // Check if ct is a generic class.  If so, then make a raw class
            // since the signature doesn't instantiate any params
            JL5ParsedClassType pct = parsedClassTypeForClass(ct);
            if (!pct.typeVariables().isEmpty())
                ct = ts.rawClass(pct, Position.compilerGenerated());
        }
        ClassType current = ct;
        ClassType outer = current.outer();
        while (outer != null) {
            if (classArgsMap.containsKey(outer.name())) {
                // XXX SC: very unsure if this is correct behavior.
                // This is dealing with instantiating for nested classes, but it's unclear what it should do.
//                System.err.println("Current is " + current);
//                System.err.println("Outer is " + outer.name() + " and classArgsMap is " + classArgsMap + " outer is " + outer.getClass());
//                System.err.println("  outer base is " + ((JL5SubstClassType)outer).base());
//                System.err.println("  outer subst is " + ((JL5SubstClassType)outer).subst());
                JL5ParsedClassType pct = parsedClassTypeForClass(outer);
                try {
                    ClassType pt =
                            ts.instantiate(position,
                                           pct.pclass(),
                                           classArgsMap.get(outer.name()));
                    if (current instanceof JL5ParsedClassType) {
                        ((JL5ParsedClassType) current).outer(pt);
                    }
                }
                catch (SemanticException e) {
                    throw new InternalCompilerError(e);
                }
            }
            if (current == current.outer()) break;
            current = current.outer();
            outer = current.outer();
        }
        pos++;
        return new Result<ClassType>(ct, pos);
    }

    private JL5ParsedClassType parsedClassTypeForClass(ClassType ct) {
        if (ct instanceof JL5ParsedClassType) {
            return (JL5ParsedClassType) ct;
        }
        else if (ct instanceof RawClass) {
            return ((RawClass) ct).base();
        }
        else if (ct instanceof JL5SubstClassType) {
            return ((JL5SubstClassType) ct).base();
        }
        else {
            throw new InternalCompilerError("Don't know how to deal with finding base of class "
                    + ct);
        }
    }

    public Result<TypeVariable> typeVarSig(String value, int pos) {
        //System.err.println("### Parsing type var sig " + value.substring(pos));
        char token = value.charAt(pos);
        switch (token) {
        case T:
            String id = "";
            pos++;
            token = value.charAt(pos);
            while (token != SEMI_COLON) {
                id += token;
                pos++;
                token = value.charAt(pos);
            }
            pos++;
            return new Result<TypeVariable>(findTypeVar(id), pos);
        default:
            return null;
        }
    }

    public Result<List<ReferenceType>> typeArgList(String value, int pos) {
        List<ReferenceType> typeArgs = new ArrayList<ReferenceType>();
        char token = value.charAt(pos++);
        while (token != RIGHT_ANGLE) {
            Result<? extends ReferenceType> tres = typeArg(value, pos);
            pos = tres.pos();
            typeArgs.add(tres.result());
            token = value.charAt(pos);
        }
        pos++;
        return new Result<List<ReferenceType>>(typeArgs, pos);
    }

    public Result<? extends ReferenceType> typeArg(String value, int pos) {
        char token = value.charAt(pos);
        switch (token) {
        case PLUS: {
            Result<? extends Type> fres = fieldTypeSig(value, ++pos);
            return new Result<WildCardType>(ts.wildCardType(position,
                                                            (ReferenceType) fres.result(),
                                                            null),
                                            fres.pos());
        }
        case MINUS: {
            Result<? extends Type> fres = fieldTypeSig(value, ++pos);
            return new Result<WildCardType>(ts.wildCardType(position,
                                                            null,
                                                            (ReferenceType) fres.result()),
                                            fres.pos());
        }
        case STAR:
            pos++;
            return new Result<WildCardType>(ts.wildCardType(position), pos);
        case L:
        case LEFT_SQUARE:
        case T:
            return fieldTypeSig(value, pos);
        default:
            return null;
        }
    }

    public Result<ArrayType> arrayTypeSig(String value, int pos) {
        char token = value.charAt(pos);
        switch (token) {
        case LEFT_SQUARE: {
            pos++;
            Result<? extends Type> tres = typeSig(value, pos);
            Type type = tres.result();
            return new Result<ArrayType>(ts.arrayOf(position, type, 1),
                                         tres.pos());
        }
        default:
            return null;
        }
    }

    public Result<List<Type>> typeSigList(String value, int pos) {
        //System.err.println("### Parsing type sig list " + value.substring(pos));
        List<Type> formals = new ArrayList<Type>();
        char token = value.charAt(pos);
        while (token != RIGHT_BRACE) {
            Result<? extends Type> ares = typeSig(value, pos);
            pos = ares.pos();
            formals.add(ares.result());
            token = value.charAt(pos);
        }
        pos++;
        return new Result<List<Type>>(formals, pos);
    }

    public Result<? extends Type> typeSig(String value, int pos) {
        // System.err.println("### Parsing type sig " + value.substring(pos));
        char token = value.charAt(pos);
        switch (token) {
        case L:
        case LEFT_SQUARE:
        case T:
            return fieldTypeSig(value, pos);
        case B:
        case C:
        case D:
        case F:
        case I:
        case J:
        case S:
        case Z:
            return baseType(value, pos);
        default:
            return null;
        }
    }

    public Result<MethodSig> methodTypeSig(String value, int pos) {
        //System.err.println("### Parsing method type sig " + value.substring(pos));
        char token = value.charAt(pos);
        Result<List<TypeVariable>> fres = null;
        if (token == LEFT_ANGLE) {
            fres = formalTypeParamList(value, ++pos);
            pos = fres.pos();
            typeVars = fres.result();
        }
        Result<List<Type>> ares = null;
        if ((token = value.charAt(pos)) == LEFT_BRACE) {
            ares = typeSigList(value, ++pos);
            pos = ares.pos();
        }
        Result<? extends Type> rres = returnType(value, pos);
        pos = rres.pos();
        Result<List<ReferenceType>> tres = null;
        if ((pos < value.length()) && ((token = value.charAt(pos)) == HAT)) {
            tres = throwsSigList(value, pos);
            pos = tres.pos();
        }
        return new Result<MethodSig>(new MethodSig(fres == null ? new ArrayList<TypeVariable>()
                                                           : fres.result(),
                                                   ares == null ? new ArrayList<Type>()
                                                           : ares.result(),
                                                   rres.result(),
                                                   tres == null ? new ArrayList<ReferenceType>()
                                                           : tres.result()),
                                     pos);
    }

    // returnType used in methodSig
    // starts pointing at char 
    // ends after (may be end of string
    public Result<? extends Type> returnType(String value, int pos) {
        //System.err.println("### Parsing return type sig " + value.substring(pos));
        char token = value.charAt(pos);
        switch (token) {
        case L:
        case LEFT_SQUARE:
        case T:
        case B:
        case C:
        case D:
        case F:
        case I:
        case J:
        case S:
        case Z:
            return typeSig(value, pos);
        case V: {
            pos++;
            return new Result<PrimitiveType>(ts.Void(), pos);
        }
        default:
            return null;
        }
    }

    // list of throwSigs ^L...;^L...;^T...;
    // starts at ^ may advance beyond end of string
    // this is okay as throwsSigList is last part 
    // of methodTypeSig
    public Result<List<ReferenceType>> throwsSigList(String value, int pos) {
        List<ReferenceType> throwsList = new ArrayList<ReferenceType>();
        while (pos < value.length()) {
            Result<? extends ReferenceType> tres = throwsSig(value, pos);
            pos = tres.pos();
            throwsList.add(tres.result());
        }
        return new Result<List<ReferenceType>>(throwsList, pos);
    }

    // throwsSig used in throwsSigList
    // ^L...; or ^T...;
    // starts at ^ and advances past ; 
    public Result<? extends ReferenceType> throwsSig(String value, int pos) {
        char token = value.charAt(pos);
        switch (token) {
        case HAT: {
            token = value.charAt(++pos);
            switch (token) {
            case L:
                return classTypeSig(value, pos);
            case T:
                return typeVarSig(value, pos);
            default:
                return null;
            }
        }
        default:
            return null;
        }
    }

    // baseType used in typeSig one of:
    // B, C, D, F, I, J, S, Z
    // starts pointing to the char and ends
    // advanced to next char
    public Result<PrimitiveType> baseType(String value, int pos) {
        char token = value.charAt(pos);
        switch (token) {
        case B:
            return new Result<PrimitiveType>(ts.Byte(), ++pos);
        case C:
            return new Result<PrimitiveType>(ts.Char(), ++pos);
        case D:
            return new Result<PrimitiveType>(ts.Double(), ++pos);
        case F:
            return new Result<PrimitiveType>(ts.Float(), ++pos);
        case I:
            return new Result<PrimitiveType>(ts.Int(), ++pos);
        case J:
            return new Result<PrimitiveType>(ts.Long(), ++pos);
        case S:
            return new Result<PrimitiveType>(ts.Short(), ++pos);
        case Z:
            return new Result<PrimitiveType>(ts.Boolean(), ++pos);
        default:
            return null;
        }
    }

    public void parseClassSignature(TypeSystem ts, Position pos) {
        this.ts = (JL5TypeSystem) ts;
        this.position = pos;
        String sigValue = (String) cls.getConstants()[index].value();
        classSignature = classSig(sigValue, 0).result();
    }

    public List<TypeVariable> parseClassTypeVariables(TypeSystem ts,
            Position pos) {
        this.ts = (JL5TypeSystem) ts;
        this.position = pos;
        String sigValue = (String) cls.getConstants()[index].value();
        char token = sigValue.charAt(0);
        ;
        List<TypeVariable> results = null;
        if (token == LEFT_ANGLE) {
            results = this.createFormalTypeParamList(sigValue, 1).result();
        }
        this.typeVars = results;
        return results;
    }

    public void parseMethodSignature(TypeSystem ts, Position pos, ClassType ct) {
        this.ts = (JL5TypeSystem) ts;
        this.position = pos;
        this.curClass = ct;
        String sigValue = (String) cls.getConstants()[index].value();
        methodSignature = methodTypeSig(sigValue, 0).result();
    }

    public void parseFieldSignature(TypeSystem ts, Position pos, ClassType ct) {
        this.ts = (JL5TypeSystem) ts;
        this.position = pos;
        this.curClass = ct;
        String sigValue = (String) cls.getConstants()[index].value();
        fieldSignature = new FieldSig();
        fieldSignature.type = fieldTypeSig(sigValue, 0).result();
    }

    private TypeVariable findTypeVar(String next) {
        //System.err.println("### calling findTypeVar " + next + " in " + typeVars);
        if (typeVars != null) {
            for (TypeVariable iType : typeVars) {
                if (iType.name().equals(next)) return iType;
            }
        }
        //System.err.println("curClass " + curClass);
        if (curClass != null && curClass instanceof JL5ParsedClassType) {
            //System.err.println("### curClass is the right type");
            if (((JL5ParsedClassType) curClass).typeVariables() != null) {
                //System.err.println("### typeVariables is not null");
                for (TypeVariable iType : ((JL5ParsedClassType) curClass).typeVariables()) {
                    //System.err.println("### Checking against " + iType.name());
                    if (iType.name().equals(next)) return iType;
                }
            }
        }
        return null;
    }

    /*public List typeVariables(){
        return typeVars;
    }*/

    @Override
    public String toString() {
        return (String) cls.getConstants()[index].value();
    }

}
