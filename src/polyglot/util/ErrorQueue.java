/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.util;

import java.io.*;
import java.util.StringTokenizer;

/**
 * A <code>ErrorQueue</code> handles outputing error messages.
 */
public interface ErrorQueue
{
    public void enqueue(int type, String message);
    public void enqueue(int type, String message, Position position);
    public void enqueue(ErrorInfo e);
    public void flush();
    public boolean hasErrors();
    public int errorCount();
}
