package jltools.frontend;

import jltools.ast.*;
import jltools.parse.*;
import jltools.lex.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;

import java.io.*;
import java.util.*;

/**
 * A <code>Job</code> encapsulates work done by the compiler on behalf of
 * one target.  It includes all information carried between phases of the
 * compiler.
 */
public abstract class Job
{
  /* Stage defining constants. */
  /**
   * The first stage of the compiler. During this stage, lexical and syntatic
   * analysis are performed on the input source. Successful completion of this
   * stage indicates valid lexical and syntactic structure. The AST will be
   * well formed, but may contain ambiguities.
   */
  public static final int PARSED           = 0x01;

  /**
   * The second stage of the compiler. Here, the visible interface of all
   * classes (including inner classes) are read into a table. This includes
   * all fields and methods (including return, argument, and exception types).
   * A ClassResolver is available (after completion of this stage) which maps 
   * the name of each class found in this file to a ClassType. These ClassType 
   * objects, however, may contain ambiguous types. The result of "getResolver"
   * is a ClassResolver that is in this very state. (The AST will still contain
   * ambiguities after this stage.)
   */
  public static final int READ             = 0x02;

  /**
   * The third stage of a the compiler. In this stage, ambiguities are removed
   * from the ClassResolver for this source file. That is, for each class
   * defined in this file, the types associated with fields and methods 
   * (include return, argument and exception types) will be disambiguated,
   * and replaced with actual ClassType definitions. In addition, upon 
   * successful completion of this stage, ALL super classes of any class that 
   * is defined in this file will also be in this state. (The AST of this 
   * source file will continue to contain ambiguities.) 
   */
  public static final int CLEANED          = 0x04;

  /**
   * The fourth stage of the compiler. During this stage, ambiguities are 
   * removed from the AST. Ambiguous dotted expressions, such as "a.b.c", are
   * resolved and replaced with the appropriate nodes. Also, after completion
   * of this stage any AmbiguousTypes referenced in the AST will be replaced 
   * with concrete ClassTypes. Note, however, that these ClassTypes themselves 
   * may contain ambiguities. (The source files cooresponding to these
   * ClassTypes may only be at the READ stage.)
   */
  public static final int DISAMBIGUATED    = 0x08;

  /**
   * The fifth stage of the compiler. This stage represents the type and flow 
   * checking of a source file. Note that all dependencies of this file must be
   * in the CLEANED state before this file can be type checked. To ensure this,
   * the compiler will attempt to bring ALL source in the work list up to (and 
   * through) the CLEANED stage. If the compiler is unable to do so, then
   * it will exits with errors. All sources files which successfully complete
   * this stage are semantically valid.
   */
  public static final int CHECKED          = 0x10;

  /**
   * The sixth (and final) stage of the compiler. During this stage, the 
   * translated version of the source file is written out to the output file. 
   */
  public static final int TRANSLATED       = 0x20;

  /**
   * This is not a stage of translation, but is used to keep track of which
   * targets are currently being used. Notice that is has 
   * <code>protected</code> visibility.
   */
  protected static final int IN_USE        = 0x40;

  protected int status;
  protected Target t;
  protected ImportTable it;
  protected TableClassResolver cr;
  protected Compiler compiler;

  public Job(Compiler compiler, Target t)
  {
    this.t = t;
    it = null;
    this.compiler = compiler;
    cr = new TableClassResolver(compiler);
    status = 0;
  }

  public boolean isParsed() { return (status & PARSED) != 0; }
  public boolean isRead() { return (status & READ) != 0; }
  public boolean isCleaned() { return (status & CLEANED) != 0; }
  public boolean isDisambiguated() { return (status & DISAMBIGUATED) != 0; }
  public boolean isChecked() { return (status & CHECKED) != 0; }
  public boolean isTranslated() { return (status & TRANSLATED) != 0; }
  public boolean isInUse() { return (status & IN_USE) != 0; }

  public void setParsed() { status |= PARSED; }
  public void setRead() { status |= READ; }
  public void setCleaned() { status |= CLEANED; }
  public void setDisambiguated() { status |= DISAMBIGUATED; }
  public void setChecked() { status |= CHECKED; }
  public void setTranslated() { status |= TRANSLATED; }
  public void setInUse() { status |= IN_USE; }
  public void clearInUse() { status &= ~IN_USE; }


  public abstract void parse();
  public abstract void read();
  public abstract void clean();
  public abstract void disambiguate();
  public abstract void check();
  public abstract void translate();
  public abstract void dump(CodeWriter cw) throws IOException;

  public Target getTarget() { return t; }
  public TableClassResolver getClassResolver() { return cr; }
  public ImportTable getImportTable() { return it; }

  public boolean equals( Object o) {
    if( o instanceof Job) {
      return t.equals( ((Job)o).t);
    }
    else {
      return false;
    }
  }
}
