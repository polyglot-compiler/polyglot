package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.*;
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
    protected List typeVars;
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
    
    JL5Signature(ClassFile clazz, DataInputStream in, int nameIndex, int length) throws IOException{
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
        public ClassSig(List typeVars, Type superType, List interfaces){
            this.typeVars = typeVars;
            this. superType = superType;
            this.interfaces = interfaces;
        }
        protected List typeVars;   // list of intersection types
        public List typeVars(){
            return typeVars;
        }
        protected Type superType;
        public Type superType(){
            return superType;
        }
        protected List interfaces; // list of types 
        public List interfaces(){
            return interfaces;
        }
    }
    
    class MethodSig {
        public MethodSig(List typeVars, List formalTypes, Type returnType, List throwTypes){
            this.typeVars = typeVars;
            this.formalTypes = formalTypes;
            this.returnType = returnType;
            this.throwTypes = throwTypes;
        }
        protected List typeVars;     // list of intersection types
        public List typeVars(){
            return typeVars;
        }
        protected List formalTypes;  // list of types
        public List formalTypes(){
            return formalTypes;
        }
        protected Type returnType; 
        public Type returnType(){
            return returnType;
        }
        protected List throwTypes;   // list of types
        public List throwTypes(){
            return throwTypes;
        }
    }

    class FieldSig {
        protected Type type;
    }

    class Result {
        public Result(Object result, int pos){
            this.result = result;
            this.pos = pos;
        }
        protected int pos;
        protected Object result;
        public int pos(){
            return pos;
        }
        public Object result(){
            return result;
        }
    }
    
    public Result classSig(String value, int pos){
        char token = value.charAt(pos);
        
        Result fres = null;
        if (token == LEFT_ANGLE){
            //fres = formalTypeParamList(value, ++pos);
            fres = useFormalTypeParamList(value, ++pos);
            pos = fres.pos();
            //typeVars = (List)fres.result();
        }
        Result sres = classTypeSig(value, pos);
        List superInterfaces = new ArrayList();
        pos = sres.pos();
        while (pos < value.length()){
            Result ires = classTypeSig(value, pos);
            pos = ires.pos();
            superInterfaces.add(ires.result());
            //pos++;
        }
        return new Result(new ClassSig(fres == null ? new ArrayList(): (List)fres.result(), (Type)sres.result(), superInterfaces), pos);
    }

    public Result createFormalTypeParamList(String value, int pos) {
        this.ts = ts;
        typeVars = null;
        int oldpos = pos;
        createTypeVars = true;
        //do it twice. second time, bounds will be resolved
        List list = new ArrayList();
        char token = value.charAt(pos);
        while (token != RIGHT_ANGLE){
            Result fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }
        pos++;
        return new Result(list, pos);
    }
    
    public Result useFormalTypeParamList(String value, int pos) {
        this.ts = ts;
        int oldpos = pos;
        createTypeVars = false;
        //do it twice. second time, bounds will be resolved
        List list = new ArrayList();
        char token = value.charAt(pos);
        while (token != RIGHT_ANGLE){
            Result fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }
        pos++;
        return new Result(list, pos);
    }
    
    public Result formalTypeParamList(String value, int pos){
        //System.err.println("### Parsing formal type param list " + value.substring(pos));
        typeVars = null;
        int oldpos = pos;
        createTypeVars = true;
        //do it twice. second time, bounds will be resolved
        List list = new ArrayList();
        char token = value.charAt(pos);
        while (token != RIGHT_ANGLE){
            Result fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }
        
        typeVars = list;
        pos = oldpos;
        createTypeVars = false;
        list = new ArrayList();
        token = value.charAt(pos);
        while (token != RIGHT_ANGLE){
            Result fres = formalTypeParam(value, pos);
            list.add(fres.result());
            pos = fres.pos();
            token = value.charAt(pos);
        }
        
        pos++;
        return new Result(list, pos);
    }
    
    // ID classBound interfaceBoundList(opt)
    public Result formalTypeParam(String value, int pos){
        //System.err.println("### Parsing formalTypeParam " + value.substring(pos));
        String id = "";
        char token = value.charAt(pos);
        while (token != COLON){
            id += token;
            pos++;
            token = value.charAt(pos);
        }
        Result cres = classBound(value, pos);
        pos = cres.pos();
        Result ires = null;
        List bounds = new ArrayList();
        token = value.charAt(pos);
        while (token != RIGHT_ANGLE){
            if (value.charAt(pos) != COLON) break;
            ires = classBound(value, pos);
            pos = ires.pos();
            bounds.add(ires.result());
        }
        if (createTypeVars) {
            //System.err.println("\ncreating type variable " + id + "\n");
            return new Result(ts.typeVariable(position, id, ts.intersectionType(position, bounds)), pos);
        }
        else {
            TypeVariable tv = findTypeVar(id);
            tv.setUpperBound(ts.intersectionType(position, bounds));
            return new Result(tv, pos);
        }
    }

    // : fieldTypeSig
    public Result classBound(String value, int pos){
        return fieldTypeSig(value, ++pos);
    }

    // classTypeSig L...;
    // typeVarSig T...;
    // arrayTypeSig [...;
    public Result fieldTypeSig(String value, int pos){
        //System.err.println("### Parsing field type sig " + value.substring(pos));
        Result res = null;
        char token = value.charAt(pos);
        switch(token){
            case L: { res = classTypeSig(value, pos); break; }
            case LEFT_SQUARE: { res = arrayTypeSig(value, pos); break; }
            case T: { res = typeVarSig(value, pos); break; }                  
            case COLON: { res = new Result(ts.Object(), pos); break; }
        }
        return res;
    }

    public Result classTypeSig(String value, int pos){
        char token = value.charAt(pos); // L
        String className = "";
        String id = "";
        Map classArgsMap = new HashMap();
        pos++;
        token = value.charAt(pos);   
        while (token != SEMI_COLON){
            switch(token){
                case SLASH: { // id is a package 
                              className += id;
                              className += "."; 
                              id = "";
                              pos++;
                              token = value.charAt(pos);
                              break; }
                case DOT: { // id is a className
                              className += id;
                              className += "$"; 
                              id = "";
                              pos++;
                              token = value.charAt(pos);
                              break; }
                case LEFT_ANGLE: { // id is a className
                                   //System.err.println("Parsing type arg list " + value.substring(pos));
                                   Result tres = typeArgList(value, pos);
                                   //System.err.println("Got " + tres.result());
                                   pos = tres.pos();
                                   classArgsMap.put(id, tres.result());
                                   //System.err.println("Adding " + tres.result() + " to map for " + id);
                                   token = value.charAt(pos);
                                   break;}          
                default: { id += token; 
                           pos++;
                           token = value.charAt(pos);
                           break; }          
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
        String lookupClassName = className.substring(className.lastIndexOf('.')+1);
        //System.err.println("Now looking up " + ct.name() + " with class name '" + className +"'  in classArgsMap " + classArgsMap + "  " + classArgsMap.containsKey(className) + "  " + createTypeVars);        
//        if (!className.equals(ct.name())) {
//            System.err.println("---- uh oh:   " + className + "   " + ct.name() +"   " + lookupClassName +"  " +classArgsMap);
//        }
        if (classArgsMap.containsKey(lookupClassName)){            
            JL5ParsedClassType pct = (JL5ParsedClassType) ct;
            if (!createTypeVars) {
                try {
                    ct = ts.instantiate(position, pct, (List<Type>)classArgsMap.get(lookupClassName));
                } catch (SemanticException e) {
                    throw new InternalCompilerError(e);
                }
            }
        }
        ClassType current = ct;
        ClassType outer = current.outer();
        while (outer != null) {
            if (classArgsMap.containsKey(outer.name())){
                // XXX SC: very unsure if this is correct behavior.
                // This is dealing with instantiating for nested classes, but it's unclear what it should do.
//                System.err.println("Current is " + current);
//                System.err.println("Outer is " + outer.name() + " and classArgsMap is " + classArgsMap + " outer is " + outer.getClass());
//                System.err.println("  outer base is " + ((JL5SubstClassType)outer).base());
//                System.err.println("  outer subst is " + ((JL5SubstClassType)outer).subst());
                JL5ParsedClassType pct = null;
                if (outer instanceof JL5ParsedClassType) {
                    pct = (JL5ParsedClassType) outer;
                }
                else {
                    JL5SubstClassType sct = (JL5SubstClassType) outer;
                    pct = sct.base();
                }
                try {
                    ClassType pt = ts.instantiate(position, pct.pclass(), (List)classArgsMap.get(outer.name()));
                    if (current instanceof JL5ParsedClassType) {
                        ((JL5ParsedClassType)current).outer(pt);
                    }
                } catch (SemanticException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (current == current.outer()) break;
            current = current.outer();
            outer = current.outer();
        }
        pos++;
        return new Result(ct, pos);
    }
    
    public Result typeVarSig(String value, int pos){
        //System.err.println("### Parsing type var sig " + value.substring(pos));
        Result res = null;
        char token = value.charAt(pos);
        switch(token){
            case T: { String id = "";
                      pos++;
                      token = value.charAt(pos);    
                      while (token != SEMI_COLON){
                        id += token;
                        pos++;
                        token = value.charAt(pos);
                      }
                      pos++;
                      res = new Result(findTypeVar(id), pos);
                    }
        }
        return res;
    }

    public Result typeArgList(String value, int pos){
        List typeArgs = new ArrayList();
        char token = value.charAt(pos++);
        while (token != RIGHT_ANGLE){
            Result tres = typeArg(value, pos);
            pos = tres.pos();
            typeArgs.add(tres.result());
            token = value.charAt(pos);
        }
        pos++;
        return new Result(typeArgs, pos);
    }   
    
    public Result typeArg(String value, int pos){
        Result res = null;
        char token = value.charAt(pos);
        switch(token){
            case PLUS: { Result fres = fieldTypeSig(value, ++pos);
                         res = new Result(ts.wildCardType(position, (ReferenceType)fres.result(), null), fres.pos());
                         break;
                       }
            case MINUS: { Result fres = fieldTypeSig(value, ++pos);
                          res = new Result(ts.wildCardType(position, null, (ReferenceType)fres.result()), fres.pos());
                          break;
                   }
            case STAR: { pos++;
                         res = new Result(ts.wildCardType(position), pos);
                         break;  
                       }
            case L:
            case LEFT_SQUARE:
            case T: { res = fieldTypeSig(value, pos);  
                      break;}
        }
        return res;
    }

    public Result arrayTypeSig(String value, int pos){
        Result res = null;
        char token = value.charAt(pos);
        switch(token){
            case LEFT_SQUARE : {pos++;
                                Result tres = typeSig(value, pos);
                                Type type = (Type)tres.result();
                                res = new Result(ts.arrayOf(position, type, 1), tres.pos());
                                break;
                               }
        }
        return res;
    }
  
    public Result typeSigList(String value, int pos){
        //System.err.println("### Parsing type sig list " + value.substring(pos));
        List formals = new ArrayList();
        char token = value.charAt(pos);
        while (token != RIGHT_BRACE){
            Result ares = typeSig(value, pos);
            pos = ares.pos();
            formals.add(ares.result());
            token = value.charAt(pos);
        }
        pos++;
        return new Result(formals, pos);
    }
    
    public Result typeSig(String value, int pos){
        //System.err.println("### Parsing type sig " + value.substring(pos));
        Result res = null;
        char token = value.charAt(pos);
        switch(token) {
            case L: 
            case LEFT_SQUARE:
            case T: { res = fieldTypeSig(value, pos);
                      break;
                    }
            case B:
            case C:
            case D:
            case F:
            case I:
            case J:
            case S:
            case Z: { res = baseType(value, pos);
                      break;
                    }
        }
        return res;
    }

    public Result methodTypeSig(String value, int pos){
        //System.err.println("### Parsing method type sig " + value.substring(pos));
        char token = value.charAt(pos);
        Result fres = null;
        if (token == LEFT_ANGLE){
            fres = formalTypeParamList(value, ++pos);
            pos = fres.pos();
            typeVars = (List)fres.result();
        }
        Result ares = null;
        if ((token = value.charAt(pos)) == LEFT_BRACE){
            ares = typeSigList(value, ++pos);
            pos = ares.pos();
        }
        Result rres = returnType(value, pos);
        pos = rres.pos();
        Type retType = (Type) rres.result;
        Result tres = null;
        if ((pos < value.length()) && ((token = value.charAt(pos)) == HAT)){
            tres = throwsSigList(value, pos);
            pos = tres.pos();
        }
        return new Result(new MethodSig(fres == null ? new ArrayList() : (List)fres.result(), ares == null ? new ArrayList() : (List)ares.result(), (Type)rres.result(), tres == null ? new ArrayList() : (List)tres.result()), pos);
    }

    // returnType used in methodSig
    // starts pointing at char 
    // ends after (may be end of string
    public Result returnType(String value, int pos){
        //System.err.println("### Parsing return type sig " + value.substring(pos));
        Result res = null;
        char token = value.charAt(pos);
        switch(token){
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
            case Z: { res = typeSig(value, pos);
                      break;
                    }
            case V: { pos++;
                      res = new Result(ts.Void(), pos); break;}
        }
        return res;
    }
    
    // list of throwSigs ^L...;^L...;^T...;
    // starts at ^ may advance beyond end of string
    // this is okay as throwsSigList is last part 
    // of methodTypeSig
    public Result throwsSigList(String value, int pos){
        List throwsList = new ArrayList();
        char token;
        while (pos < value.length()){
            Result tres = throwsSig(value, pos);
            pos = tres.pos();
            throwsList.add(tres.result());
        }
        return new Result(throwsList, pos);    
    }

    // throwsSig used in throwsSigList
    // ^L...; or ^T...;
    // starts at ^ and advances past ; 
    public Result throwsSig(String value, int pos){
        Result res = null;
        char token = value.charAt(pos);
        switch(token){
            case HAT: { token = value.charAt(++pos);
                        switch(token){
                            case L: { res = classTypeSig(value, pos); 
                                    }
                            case T: { res = typeVarSig(value, pos);                                                 }
                        }
                      }
        }
        return res;
    }
  
    // baseType used in typeSig one of:
    // B, C, D, F, I, J, S, Z
    // starts pointing to the char and ends
    // advanced to next char
    public Result baseType(String value, int pos){
        Result res = null;
        char token = value.charAt(pos);
        switch(token) {
            case B: { res = new Result(ts.Byte(), ++pos); 
                      break;
                    }
            case C: { res = new Result(ts.Char(), ++pos); 
                      break;
                    }
            case D: { res = new Result(ts.Double(), ++pos); 
                      break;
                    }
            case F: { res = new Result(ts.Float(), ++pos); 
                      break;
                    }
            case I: { res = new Result(ts.Int(), ++pos); 
                      break;
                    }
            case J: { res = new Result(ts.Long(), ++pos); 
                      break;
                    }
            case S: { res = new Result(ts.Short(), ++pos); 
                      break;
                    }
            case Z: { res = new Result(ts.Boolean(), ++pos); 
                      break;
                    }
        
        }
        return res;
    }
    
    public void parseClassSignature(TypeSystem ts, Position pos) throws IOException, SemanticException{
        this.ts = (JL5TypeSystem)ts;
        this.position = pos;
        String sigValue = (String)cls.getConstants()[index].value();
        classSignature = (ClassSig)classSig(sigValue, 0).result();
    }
    
    public List<TypeVariable> parseClassTypeVariables(TypeSystem ts, Position pos) throws IOException, SemanticException{
        this.ts = (JL5TypeSystem)ts;
        this.position = pos;
        String sigValue = (String)cls.getConstants()[index].value();
        char token = sigValue.charAt(0);;
        List<TypeVariable> results = null;
        if (token == LEFT_ANGLE){
            results = (List<TypeVariable>) this.createFormalTypeParamList(sigValue, 1).result();
        }
        this.typeVars = results;
        return results;
    }

    public void parseMethodSignature(TypeSystem ts, Position pos, ClassType ct) throws IOException, SemanticException{
        this.ts = (JL5TypeSystem)ts;
        this.position = pos;
        this.curClass = ct;
        String sigValue = (String)cls.getConstants()[index].value();
        methodSignature = (MethodSig)methodTypeSig(sigValue, 0).result();
    }

    public void parseFieldSignature(TypeSystem ts, Position pos, ClassType ct) throws IOException, SemanticException{
        this.ts = (JL5TypeSystem)ts;
        this.position = pos;
        this.curClass = ct;
        String sigValue = (String)cls.getConstants()[index].value();
        fieldSignature = new FieldSig();
        fieldSignature.type = (Type)fieldTypeSig(sigValue, 0).result();
    }

    private TypeVariable findTypeVar(String next){
        //System.err.println("### calling findTypeVar " + next + " in " + typeVars);
        if (typeVars != null){
            for (Iterator it = typeVars.iterator(); it.hasNext(); ){
                TypeVariable iType = (TypeVariable)it.next();
                if (iType.name().equals(next)) return iType;
            }
        }
        //System.err.println("curClass " + curClass);
        if (curClass != null && curClass instanceof JL5ParsedClassType){
            //System.err.println("### curClass is the right type");
            if (((JL5ParsedClassType)curClass).typeVariables() != null){
                //System.err.println("### typeVariables is not null");
                for (Iterator it = ((JL5ParsedClassType)curClass).typeVariables().iterator(); it.hasNext(); ){
                    TypeVariable iType = (TypeVariable)it.next();
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

    public String toString(){
        return (String)cls.getConstants()[index].value();
    }
    
}
