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
package polyglot.ext.jl5.ast;

import java.util.HashMap;
import java.util.Map;

import polyglot.ast.ClassDecl;
import polyglot.ast.Import;
import polyglot.ast.Import.Kind;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.types.Named;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class JL5SourceFileExt extends JL5Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        SourceFile n = (SourceFile) superLang().typeCheck(node(), tc);
        Map<String, Named> declaredTypes = new HashMap<>();

        for (TopLevelDecl d : n.decls()) {
            String s = d.name();
            declaredTypes.put(s, ((ClassDecl) d).type());
        }

        TypeSystem ts = tc.typeSystem();
        Map<String, Named> importedTypes = new HashMap<>();
        Map<String, Named> staticImportedTypes = new HashMap<>();

        for (Import i : n.imports()) {
            Kind kind = i.kind();
            if (kind == Import.SINGLE_TYPE) {
                String s = i.name();
                Named named = ts.forName(s);
                String name = named.name();
                importedTypes.put(name, named);
            }
            if (kind != JL5Import.SINGLE_STATIC_MEMBER) continue;

            String s = i.name();
            Named named;
            try {
                named = ts.forName(s);
            }
            catch (SemanticException e) {
                // static import is not a type; further checks unnecessary.
                continue;
            }
            String name = named.name();

            // See JLS 3rd Ed. | 7.5.3.

            // If a compilation unit contains both a single-static-import
            // declaration that imports a type whose simple name is n, and a
            // single-type-import declaration that imports a type whose simple
            // name is n, a compile-time error occurs. 
            if (importedTypes.containsKey(name)) {
                Named importedType = importedTypes.get(name);
                throw new SemanticException(name
                                                    + " is already defined in a single-type import as type "
                                                    + importedType + ".",
                                            i.position());
            }
            else staticImportedTypes.put(name, named);

            // If a single-static-import declaration imports a type whose simple
            // name is n, and the compilation unit also declares a top level
            // type whose simple name is n, a compile-time error occurs.
            if (declaredTypes.containsKey(name)) {
                Named declaredType = declaredTypes.get(name);
                throw new SemanticException("The static import " + s
                        + " conflicts with type " + declaredType
                        + " defined in the same file.", i.position());
            }

        }

        return n;
    }
}
