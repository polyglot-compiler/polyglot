package jltools.types;

/**
 * A <code>PrimitiveType</code> represents a type which may not be directly 
 * coerced to java.lang.Object (under the standard Java type system).    
 * <p>
 * This class should never be instantiated directly. Instead, you should
 * use the <code>TypeSystem.get*</code> methods.
 */
public class PrimitiveType extends Type 
{
  static final long serialVersionUID = 3653023899820987464L;

  public static final int VOID    = 1;
  public static final int BOOLEAN = 2;
  public static final int CHAR    = 3;
  public static final int BYTE    = 4;
  public static final int SHORT   = 5;
  public static final int INT     = 6;
  public static final int LONG    = 7;
  public static final int FLOAT   = 8;
  public static final int DOUBLE  = 9;

  public static final int MAX_KIND_USED = DOUBLE;

  protected PrimitiveType()
  { 
    super();
  }
  
  public PrimitiveType(TypeSystem ts, int kind) 
  {
    super(ts);
    this.kind = kind;
  }

  public int getKind() 
  {
    return kind;
  }

  public String translate(LocalContext c) {
    return getTypeString();
  }
  
  public String getTypeString() 
  {
    switch(kind)
    {
      case VOID:
        return "void";
      case BOOLEAN:
        return "boolean";
      case CHAR:
        return "char";
      case BYTE:
        return "byte";
      case SHORT:
        return "short";
      case INT:
        return "int";
      case LONG:
        return "long";
      case FLOAT:
        return "float";
      case DOUBLE:
        return "double";
      default:
        return "???";
    }
  }
  
  public String getWrapperTypeString() {
    switch(kind)
    {
      case BOOLEAN:
        return "Boolean";
      case CHAR:
        return "Char";
      case BYTE:
        return "Byte";
      case SHORT:
        return "Short";
      case INT:
        return "Integer";
      case LONG:
        return "Long";
      case FLOAT:
        return "Float";
      case DOUBLE:
        return "Double";
      default:
        return "???";
    }
  }
  
  public final boolean isPrimitive() {
    return true;
  }
  public final boolean isCanonical() {
    return true;
  }
  public boolean isReferenceType() { return false; }
  public boolean isClassType() { return false; }
  public boolean isArrayType() { return false; }
  public boolean isPackageType() { return false; }
    public PrimitiveType toPrimitiveType() {
	return this;
    }

  public boolean isNumeric() {
    switch(kind) 
      {
      case CHAR:
      case BYTE:
      case SHORT:
      case INT:
      case LONG:
      case FLOAT:
      case DOUBLE:
	return true;
      default:
	return false;
      }
  }

  public boolean isBoolean() {
    return (kind == BOOLEAN);
  }

  public boolean isVoid() {
    return (kind == VOID);
  }

  public boolean equals(Object o) {
    if (! (o instanceof PrimitiveType)) return false;
    PrimitiveType pt = (PrimitiveType) o;

    return (pt.kind == this.kind) && isComparable(pt);
  }

  public int hashCode() {
    return kind;
  }

  public static PrimitiveType binaryPromotion( PrimitiveType t1, 
                                            PrimitiveType t2)
  {
    return new PrimitiveType( t1.ts, Math.max( t1.kind, t2.kind));
  }

  public static PrimitiveType unaryPromotion( PrimitiveType t)
  {
    if( t.kind >= INT) {
      return t;
    }
    else {
      return (PrimitiveType)t.ts.getInt();
    }
  }
      
  private int kind;
}


