
package jltools.types;

import java.io.IOException;


public interface ClassCleaner
{
  public abstract boolean cleanClass( ClassType clazz) throws IOException;
}
