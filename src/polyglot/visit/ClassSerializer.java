package jltools.visit;

import jltools.ast.*;
import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;


public class ClassSerializer extends NodeVisitor
{
  protected TypeEncoder te;
  protected ErrorQueue eq;
  protected Date date;
  protected Type string_t, long_t;

  public ClassSerializer( TypeSystem ts, Date date, ErrorQueue eq)
  {
    this.te = new TypeEncoder( ts);
    this.eq = eq;
    this.date = date;
      
    long_t = ts.getLong();

    try
    {
      string_t = ts.getTypeWithName( "java.lang.String");
    }
    catch( SemanticException e)
    {
      throw new InternalCompilerError( e.toString());
    }
  }

  public Node leave( Node old, Node n, NodeVisitor v)
  {
    if( n instanceof ClassNode)
    {
      try
      {
        ClassNode cn = (ClassNode)n;
        ParsedClassType clazz;
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;
        byte[] b;
        
        AccessFlags af = new AccessFlags();
        
        af.setPublic( true);
        af.setStatic( true);
        af.setFinal( true);

        List interfaces = copyIteratorToList( cn.interfaces());
        List members = copyIteratorToList( cn.members());
        List decls;
                
        VariableDeclarationStatement vds;
        VariableDeclarationStatement.Declarator decl;
        FieldNode fn;
        
        /* Add the compiler version number. */
        decl = new VariableDeclarationStatement.Declarator( null,
                      "jlc$CompilerVersion", 0, new StringLiteral( 
                         Compiler.VERSION_MAJOR + "." + Compiler.VERSION_MINOR
                         + "." + Compiler.VERSION_PATCHLEVEL));
        decls = new LinkedList();
        decls.add( decl);
        vds = new VariableDeclarationStatement( af, new TypeNode( string_t), 
                                                decls);
        fn = new FieldNode( af, vds);
        members.add( fn);
               
        /* Add the date of the last source file modification. */
        decl = new VariableDeclarationStatement.Declarator( null,
                      "jlc$SourceLastModified", 0, new IntLiteral( 
                                                       (long)date.getTime()));
        decls = new LinkedList();
        decls.add( decl);
        vds = new VariableDeclarationStatement( af, new TypeNode( long_t), 
                                                decls);
        fn = new FieldNode( af, vds);
        members.add( fn);

        /* Add the class type info. */
        clazz = cn.type;        
        decl = new VariableDeclarationStatement.Declarator( null,
           "jlc$ClassType", 0, new StringLiteral( te.encode( clazz)));

        decls = new LinkedList();
        decls.add( decl);
        vds = new VariableDeclarationStatement( af, new TypeNode( string_t),
                                                decls);
        fn = new FieldNode( af, vds);
        members.add( fn);
        
        return cn.reconstruct( cn.getAccessFlags(), cn.getName(),
                              cn.getSuperClass(), interfaces, members);
      }
      catch( IOException e)
      {
        e.printStackTrace();
        eq.enqueue( ErrorInfo.IO_ERROR, 
                    "Unable to serialize class information.");
        return n;
      }
    }
    else {
      return n;
    }
  }

  private final List copyIteratorToList( Iterator iter)
  {
    List l = new LinkedList();
    for( ; iter.hasNext(); )
    {
      l.add( iter.next());
    }
    return l;
  }
}
