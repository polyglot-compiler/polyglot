/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.*;
import polyglot.ext.param.types.*;

public interface CofferParsedClassType extends CofferClassType, ParsedClassType {
    void setKey(Key key);
    void setInstantiatedFrom(PClass pc);
}
