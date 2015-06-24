/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.filemanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Scanner;

import javax.tools.SimpleJavaFileObject;

/**
 * This class represents a source object to be kept in memory.
 */
public class ExtFileObject extends SimpleJavaFileObject {

    final protected ByteArrayOutputStream baos;

    public ExtFileObject(URI u, Kind k) {
        super(u, k);
        baos = new ByteArrayOutputStream();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        Reader r;
        try {
            r = openReader(ignoreEncodingErrors);
            try (Scanner s = new Scanner(r)) {
                // Read until end of stream (NB: \\A is the beginning of
                // input marker, so it won't ever be found)
                return s.useDelimiter("\\A").next();
            }
            catch (java.util.NoSuchElementException e) {
                return "";
            }
        }
        catch (IOException e) {
            return null;
        }
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        baos.reset();
        return baos;
    }

    @Override
    public Reader openReader(boolean arg0) throws IOException {
        return new InputStreamReader(openInputStream());
    }

    @Override
    public Writer openWriter() throws IOException {
        return new OutputStreamWriter(openOutputStream());
    }
}
