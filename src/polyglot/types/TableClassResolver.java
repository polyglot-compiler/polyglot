package jltools.types;

import jltools.util.*;
import jltools.ast.TypeNode;

import java.util.*;


public class TableClassResolver implements ClassResolver
{
  protected Map table;
  protected List queue;

  public TableClassResolver()
  {
    table = new HashMap();
    queue = new LinkedList();
  }
  
  public void addClass( String fullName, ClassType clazz)
  {
    table.put( fullName, clazz);
    queue.add( clazz);
  }

  public Iterator classes() 
  {
    return queue.iterator();
  }

  public ClassType findClass( String name) throws NoClassException
  {
    ClassType clazz = (ClassType)table.get( name);
    if( clazz == null)
      throw new NoClassException( "Class " + name + " not found.");
    return clazz;
  }

  public void findPackage( String name) throws NoClassException {}

  public void cleanupSignatures( TypeSystem ts, ImportTable it,
                                 ErrorQueue eq)
  {
    Iterator iter1;
    ListIterator iter2, iter3;
    TypeSystem.Context context;
    ParsedClassType clazz;
    MethodTypeInstance method;
    FieldInstance field;
    Type type;
    List list1, list2;

    /* First clean up all the class types for this source file. */
    iter1 = classes();

    while( iter1.hasNext()) {
      clazz = (ParsedClassType)iter1.next();
      //      System.out.println( "clean: working on " + clazz.getTypeString());
      context = new TypeSystem.Context( it, (ClassType)ts.getObject(), null);

      type = clazz.getSuperType();
      if( type != null) {
        try {
          //       System.out.println( "cleaning super: "+ type.getTypeString());
          clazz.setSuperType( (ClassType)ts.checkAndResolveType( type, 
                                                                 context));
          //System.out.println( "cleaned: " + clazz.getSuperType() );
        }
        catch( TypeCheckException e)
        {
          eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage());
        }
      }
      else {
        if( !clazz.getAccessFlags().isInterface()) {
          clazz.setSuperType( (ClassType)ts.getObject());
        }
      }

      //      ((ClassTypeImpl)clazz).dump();
      //      it.dump();
      
      context = new TypeSystem.Context( it, clazz, null);

      list1 = clazz.getMethods();
      iter2 = list1.listIterator();
      while( iter2.hasNext()) {
        method = (MethodTypeInstance)iter2.next();
        type = method.getReturnType();
        try {
          method.setReturnType( ts.checkAndResolveType( type, context));
        } 
        catch( TypeCheckException e) {
          eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
                      Annotate.getLineNumber( method));
        }

        list2 = method.argumentTypes();
        iter3 = list2.listIterator();
        while( iter3.hasNext()) {
          type = (Type)iter3.next();
          try {
            iter3.set( ts.checkAndResolveType( type, context));
          }
          catch( TypeCheckException e) {
            eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
                        Annotate.getLineNumber( method));
          }
        }

        list2 = method.exceptionTypes();
        iter3 = list2.listIterator();
        while( iter3.hasNext()) {
          type = ((TypeNode)iter3.next()).getType();
          try {
            iter3.set( ts.checkAndResolveType( type, context));
          } 
          catch( TypeCheckException e) {
            eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
                        Annotate.getLineNumber( method));
          }
        }
      }

      list1 = clazz.getFields();
      iter2 = list1.listIterator();
      while( iter2.hasNext()) {
        field = (FieldInstance)iter2.next();
        type = field.getType();

        try {
          //          System.out.println( "field: " + type.getTypeString() + " ("
          //                  + type.getClass().getName() + ")");
 
          field.setType( ts.checkAndResolveType( type, context));
        } 
        catch( TypeCheckException e) {
          eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                      Annotate.getLineNumber( field));
        }        
      }
    }
  }
}
