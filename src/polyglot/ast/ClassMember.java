package polyglot.ast;

import polyglot.types.Context;
import polyglot.types.MemberInstance;

/**
 * A <code>ClassMember</code> is a method, a constructor, a field, an
 * initializer block, or another class declaration.  It is any node that may
 * occur directly inside a class body.
 */
public interface ClassMember extends Term 
{
    public MemberInstance memberInstance();
}
