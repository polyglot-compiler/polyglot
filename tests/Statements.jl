
package test;

import jltools.types.*;

public class Statements
{
   public static final void main(String args[])
   {
      int i, n = 10, j = n;
      boolean b;
     
      System.out.println("Hello World!");
      for( i = 0, b = false; i < n; i++, j++)
      {
        if(i > n/2)
          break;
           
        n++;
      }
      
      for( int x = 0, y = 0; x < y * y; x++, y++)
        System.out.println( "x: " + x + " y: " + y);

      TypeSystem ts = new StandardTypeSystem( null);
      Statements s = new Statements();
      Constants c = new Constants();

   }
   
   void foo()
   {
LOOP:while( true)
     {
       if( halts(this)) {
        break LOOP;       
      }
     }
   }
   
   boolean halts(Object o, boolean b) throws Exception
   {
     Object p = new Object(o);
     if( halts(o, b))
       return true;
     else
       return false;
   }
}
