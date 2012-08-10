/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Output stream for writing unicode.  Non-ASCII Unicode characters
 * are escaped.
 */
public class UnicodeWriter extends FilterWriter {
    public UnicodeWriter(Writer out) {
        super(out);
    }

    @Override
    public void write(int c) throws IOException {
        if (c <= 0xFF) {
            super.write(c);
        }
        else {
            String s = String.valueOf(Integer.toHexString(c));
            super.write('\\');
            super.write('u');
            for (int i = s.length(); i < 4; i++) {
                super.write('0');
            }
            write(s);
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(cbuf[i + off]);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        write(str.toCharArray(), off, len);
    }
}
