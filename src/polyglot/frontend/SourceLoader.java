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

import java.io.IOException;

import javax.tools.JavaFileManager.Location;

/**
 * Interface for defining source file loader
 */
public interface SourceLoader {

    /**
     * @param name
     *            A package name
     * @return true if a package {@code name} exists, otherwise false
     */
    boolean packageExists(String name);

    /**
     * @param location
     *            A location to search a package {@code name} in
     * @param name
     *            A package name
     * @return true if a package {@code name} exists, otherwise false
     */
    boolean packageExists(Location location, String name);

    /**
     * Obtains a FileSource for a dependency.
     * 
     * @param fileName
     *            A source file name
     * @return A FileSource if a source file {@code fileName} exists in
     *         source path
     * @throws IOException
     */
    FileSource fileSource(String fileName) throws IOException;

    /**
     * @param fileName
     *            A source file name
     * @param userSpecified
     *            If the user has specified this source file explicitly
     * @return A FileSource if a source file {@code fileName} exists in
     *         source path
     * @throws IOException
     * @deprecated Use {@link #fileSource(String, Source.Kind)} instead.
     */
    @Deprecated
    FileSource fileSource(String fileName, boolean userSpecified)
            throws IOException;

    /**
     * @param fileName
     *            A source file name
     * @param kind
     *            How the source was found
     * @return A FileSource if a source file {@code fileName} exists in
     *         source path
     * @throws IOException
     */
    FileSource fileSource(String fileName, Source.Kind kind) throws IOException;

    /**
     * Obtains a FileSource for a dependency.
     * 
     * @param location
     *            A location to search a source file in
     * @param fileName
     *            A source file name
     * @return A FileSource if a source file {@code fileName} exists
     * @throws IOException
     */
    FileSource fileSource(Location location, String fileName)
            throws IOException;

    /**
     * @param location
     *            A location to search a source file in
     * @param fileName
     *            A source file name
     * @param userSpecified
     *            If the user has specified this source file explicitly
     * @return A FileSource if a source file {@code fileName} exists
     * @throws IOException
     * @deprecated Use {@link #fileSource(Location, String, Source.Kind)} instead. 
     */
    @Deprecated
    FileSource fileSource(Location location, String fileName,
            boolean userSpecified) throws IOException;

    /**
     * @param location
     *            A location to search a source file in
     * @param kind
     *            How the source was found
     * @param userSpecified
     *            If the user has specified this source file explicitly
     * @return A FileSource if a source file {@code fileName} exists
     * @throws IOException
     */
    FileSource fileSource(Location location, String fileName, Source.Kind kind)
            throws IOException;

    /**
     * Load the source file for the given (possibly nested) class name using the
     * source path.
     */
    /**
     * @param className
     *            A class name
     * @return A FileSource containing definition of the class
     *         {@code className} if it exists in source path, otherwise
     *         null
     */
    FileSource classSource(String className);

    /**
     * @param location
     *            A location to search a file in, that contains definition of
     *            the class {@code className}
     * @param className
     *            A class name
     * @return A FileSource containing definition of the class
     *         {@code className} if it exists in {@code location},
     *         otherwise null
     */
    FileSource classSource(Location location, String className);

    /**
     * @return true if the file system is case-insensitive, otherwise false.
     */
    boolean caseInsensitive();
}
