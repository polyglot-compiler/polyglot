package jltools.types;

import jltools.util.*;
import jltools.ast.TypeNode;

import java.io.IOException;
import java.util.*;


public class TableClassResolver implements ClassResolver
{
  protected ClassCleaner cc;
  protected Map table;
  protected List queue;

  public TableClassResolver( ClassCleaner cc)
  {
    this.cc = cc;

    table = new HashMap();
    queue = new LinkedList();
  }
  
  public void addClass( String fullName, ClassType clazz)
  {
    table.put( fullName, clazz);
    queue.add( clazz);
  }

  /**
   * Adds all the classes found in <code>other</code> to the current table.
   * 
   * @post <code>other</code>remains unchanged.
   */
  public void include( TableClassResolver other)
  {
    for( Iterator iter = other.table.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry entry = (Map.Entry)iter.next();
      table.put( entry.getKey(), entry.getValue());
    }
  }

  public Iterator classes() 
  {
    return queue.iterator();
  }

  public ClassType findClass( String name) throws NoClassException
  {
    ClassType clazz = (ClassType)table.get( name);
    if( clazz == null)
      throw new NoClassException( "Class \"" + name + "\" not found.");
    return clazz;
  }

  public void findPackage( String name) throws NoClassException {}

  public void cleanupSignatures( TypeSystem ts, ImportTable it,
                                 ErrorQueue eq)
  {
    ParsedClassType clazz;
 
    /* Clean up all the class types for this source file. */
    while( queue.size() > 0) {
      clazz = (ParsedClassType)queue.get( 0);
      if( !cleanupClassSignatures( clazz, ts, it, eq)) {
        return;
      }
    }
  }

  protected boolean cleanupClassSignatures( ParsedClassType clazz, 
                                            TypeSystem ts, ImportTable it, 
                                            ErrorQueue eq)
  {
    ListIterator iter2, iter3;
    TypeSystem.Context context;
    MethodTypeInstance method;
    FieldInstance field;
    Type type;
    TypeNode typeNode;
    ClassType superClazz, implementsClazz;
    List list1, list2;
    
    context = new TypeSystem.Context( it, (ClassType)ts.getObject(), null);

    type = clazz.getSuperType();

    if( type != null) {
      try {
        superClazz = (ClassType)ts.checkAndResolveType( type, context);
        
        /* Now we must clean all our super classes. But we need to check
         * first to see if the super class is defined in this file. If
         * so then clean here it, but if not, then ask then ask the
         * "ClassCleaner" to so do.
         * Either way do so recursively. Note that if things fail here
         * we're in a lot of trouble so immediately return false. */
        if( superClazz instanceof ParsedClassType) {
          if( table.containsKey( superClazz.getFullName())) {
            if( !cleanupClassSignatures( (ParsedClassType)superClazz, 
                                         ts, it, eq)) { 
              eq.enqueue( ErrorInfo.SEMANTIC_ERROR,
                          "Errors while compiling dependencies of \""
                          + clazz.getShortName() + "\".");
              queue.remove( clazz);
              return false;
            }
          }
          else {
            try {
              if( !cc.cleanClass( superClazz)) {
                eq.enqueue( ErrorInfo.SEMANTIC_ERROR,
                            "Errors while compiling dependencies of \""
                            + clazz.getShortName() + "\".");
                queue.remove( clazz);
                return false;
              }
            }
            catch( IOException e) 
            {
              e.printStackTrace();
              eq.enqueue( ErrorInfo.IO_ERROR, 
                        "Encountered an I/O error while compiling "
                        + "dependencies of \"" + clazz.getShortName() + "\".");
              queue.remove( clazz);
              return false;
            }
          }
        }
          
        clazz.setSuperType( superClazz);
          
      }
      catch( SemanticException e)
      {
        eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage());
        queue.remove( clazz);
        return false;
      }
    }
    else {
      if( !clazz.getAccessFlags().isInterface()) {
        clazz.setSuperType( (ClassType)ts.getObject());
      }
    }

    for (ListIterator i = clazz.getInterfaces().listIterator(); i.hasNext(); )
    {
      type = (Type)i.next();
      try {
        implementsClazz = (ClassType)ts.checkAndResolveType( type, context);
        
        /* Now we must clean all our interfaces. But we need to check
         * first to see if the interface is defined in this file. If
         * so then clean here it, but if not, then ask then ask the
         * "ClassCleaner" to so do.
         * Either way do so recursively. Note that if things fail here
         * we're in a lot of trouble so immediately return false. */
        if( implementsClazz instanceof ParsedClassType) {
          if( table.containsKey( implementsClazz.getFullName())) {
            if( !cleanupClassSignatures( (ParsedClassType)implementsClazz, 
                                         ts, it, eq)) { 
              eq.enqueue( ErrorInfo.SEMANTIC_ERROR,
                          "Errors while compiling dependencies of "
                          + clazz.getShortName() + "\".");
              queue.remove( clazz);
              return false;
            }
          }
          else {
            try {
              if( !cc.cleanClass( implementsClazz)) {
                eq.enqueue( ErrorInfo.SEMANTIC_ERROR,
                            "Errors while compiling dependencies of \""
                            + clazz.getShortName() + "\".");
                queue.remove( clazz);
                return false;
              }
            }
            catch( IOException e) 
            {
              eq.enqueue( ErrorInfo.IO_ERROR, 
                        "Encountered an I/O error while compiling "
                        + "dependencies of \"" + clazz.getShortName() + "\".");
              queue.remove( clazz);
              return false;
            }
          }
        }

        i.set( implementsClazz );          
      }
      catch( SemanticException e)
      {
        eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage());
        queue.remove( clazz);
        return false;
      }
    }


    context = new TypeSystem.Context( it, clazz, null);

    list1 = clazz.getMethods();
    iter2 = list1.listIterator();
    while( iter2.hasNext()) {
      method = (MethodTypeInstance)iter2.next();
      type = method.getReturnType();
      try {
        method.setReturnType( ts.checkAndResolveType( type, context));
      } 
      catch( SemanticException e) {
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
        catch( SemanticException e) {
          eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
                      Annotate.getLineNumber( method));
        }
      }
      
      list2 = method.exceptionTypes();
      iter3 = list2.listIterator();
      while( iter3.hasNext()) {
        type = (Type)iter3.next();
        try {
          iter3.set( ts.checkAndResolveType( type, context));
        } 
        catch( SemanticException e) {
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
         field.setType( ts.checkAndResolveType( type, context));
      } 
      catch( SemanticException e) {
        eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), 
                    Annotate.getLineNumber( field));
      }        
    }

    queue.remove( clazz);
    return !eq.hasErrors();
  }
}
