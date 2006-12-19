/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ext.param.types;

import polyglot.types.*;
import java.util.List;

/**
 * A parameterized type instantiated on actual arguments.
 */
public interface InstType extends Type
{
    PClass instantiatedFrom();
    List actuals();
}
