package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

public interface TranslateOverride
{
    public void translate(Node n, LocalContext c, CodeWriter w);
}
