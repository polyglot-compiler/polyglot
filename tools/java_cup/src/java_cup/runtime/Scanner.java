package java_cup.runtime;

/**
 * Defines the Scanner interface, which CUP uses in the default
 * implementation of {@code lr_parser.scan()}.  Integration
 * of scanners implementing {@code Scanner} is facilitated.
 *
 * @version last updated 23-Jul-1999
 * @author David MacMahon <davidm@smartsc.com>
 */

/* *************************************************
  Interface Scanner
  
  Declares the next_token() method that should be
  implemented by scanners.  This method is typically
  called by lr_parser.scan().  End-of-file can be
  indicated either by returning
  {@code new Symbol(lr_parser.EOF_sym())} or
  {@code null}.
 ***************************************************/
public interface Scanner {
    /** Return the next token, or {@code null} on end-of-file. */
    public Symbol next_token() throws java.lang.Exception;
}
