package polyglot.ext.coffer.ast;

import polyglot.ast.*;
import java.util.*;

public interface AmbKeySetNode extends KeySetNode
{
    public List keyNodes();
    public AmbKeySetNode keyNodes(List keyNodes);
}
