package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

public interface TranslateBodyOverride
{
    public void translateBody(ClassNode n, LocalContext c, CodeWriter w);
}
