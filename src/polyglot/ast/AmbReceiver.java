package jltools.ast;

/**
 * An <code>AmbReceiver</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers.  It must resolve to a receiver.
 */
public interface AmbReceiver extends Ambiguous, Receiver
{
    Prefix prefix();
    String name();
}
