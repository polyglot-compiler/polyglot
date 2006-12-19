/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.lex;

import java.io.IOException;

/**
 * The interface "Lexer" describes lexers produced by JFlex for
 * Polyglot.
 */
public interface Lexer {

  /** This character denotes the end of file */
  final public static int YYEOF = -1;

  /**
   * The file being scanned, for use in constructing diagnostic
   * messages.
   */
  public String file();

  /**
   * The path to the file being scanned, for use in constructing diagnostic
   * messages.
   */
  public String path();

  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   IOException  if any I/O-Error occurs
   */
  public Token nextToken() throws IOException;
}
