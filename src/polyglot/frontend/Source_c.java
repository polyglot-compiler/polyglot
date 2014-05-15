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
package polyglot.frontend;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.tools.FileObject;
import javax.tools.ForwardingFileObject;

import polyglot.util.InternalCompilerError;

public class Source_c extends ForwardingFileObject<FileObject> implements
        FileSource {

    protected Kind kind;

    /**
     * @Deprecated Use {@link #Source_c(FileObject, Kind)} instead.
     */
    @Deprecated
    protected Source_c(FileObject f, boolean userSpecified) {
        this(f, userSpecified ? Kind.USER_SPECIFIED : Kind.DEPENDENCY);
    }

    protected Source_c(FileObject f, Kind kind) {
        super(f);
        this.kind = kind;
    }

    @Deprecated
    @Override
    public void setUserSpecified(boolean userSpecified) {
        if (userSpecified) {
            setKind(Kind.USER_SPECIFIED);
        }
        else if (userSpecified()) {
            setKind(Kind.DEPENDENCY);
        }
    }

    @Override
    public boolean userSpecified() {
        return kind == Kind.USER_SPECIFIED;
    }

    @Override
    public boolean compilerGenerated() {
        return kind == Kind.COMPILER_GENERATED;
    }

    @Override
    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public String path() {
        try {
            return new File(toUri().getPath()).getCanonicalPath();
        }
        catch (IOException e) {
            throw new InternalCompilerError(e);
        }
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader(openInputStream());
    }

    @Override
    public String toString() {
        return path();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileObject) {

            FileObject fo = (FileObject) o;
            return toUri().equals(fo.toUri());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toUri().hashCode();
    }
}
