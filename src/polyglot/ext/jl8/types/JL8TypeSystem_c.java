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
package polyglot.ext.jl8.types;

import java.util.ArrayList;
import java.util.List;
import polyglot.ext.jl7.types.JL7TypeSystem_c;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;

public class JL8TypeSystem_c extends JL7TypeSystem_c implements JL8TypeSystem {
    @Override
    public List<MethodInstance> nonObjectPublicAbstractMethods(ReferenceType referenceType) {
        List<MethodInstance> objectPublicMethods = this.objectPublicMethods();
        List<MethodInstance> nonObjectPublicAbstractMethods = new ArrayList<>();
        for (MethodInstance m : referenceType.methods()) {
            Flags flags = m.flags();
            if (flags.isPublic() && flags.isAbstract()) {
                boolean isObjectMethod = false;
                for (MethodInstance objectMethod : objectPublicMethods) {
                    if (m.isSameMethod(objectMethod)) {
                        isObjectMethod = true;
                        break;
                    }
                }
                if (!isObjectMethod) nonObjectPublicAbstractMethods.add(m);
            }
        }
        return nonObjectPublicAbstractMethods;
    }

    private List<MethodInstance> objectPublicMethods() {
        List<MethodInstance> objectMethods = new ArrayList<>();
        for (MethodInstance i : Object().methods()) {
            if (i.flags().isPublic()) {
                objectMethods.add(i);
            }
        }
        return objectMethods;
    }
}
