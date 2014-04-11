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
package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.ImportTable;
import polyglot.types.MemberInstance;
import polyglot.types.Named;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.StringUtil;

public class JL5ImportTable extends ImportTable {

    protected ArrayList<String> singleStaticImports;
    protected ArrayList<String> staticOnDemandImports;

    public int id = counter++;
    private static int counter = 0;

    public JL5ImportTable(TypeSystem ts, polyglot.types.Package pkg, String src) {
        super(ts, pkg, src);
        this.singleStaticImports = new ArrayList<>();
        this.staticOnDemandImports = new ArrayList<>();
    }

    public JL5ImportTable(TypeSystem ts, polyglot.types.Package pkg) {
        this(ts, pkg, null);
    }

    public void addSingleStaticImport(String member, Position pos) {
        singleStaticImports.add(member);
    }

    public void addStaticOnDemandImport(String className, Position pos) {
        staticOnDemandImports.add(className);
    }

    public List<String> singleStaticImports() {
        return singleStaticImports;
    }

    public List<String> staticOnDemandImports() {
        return staticOnDemandImports;
    }

    @Override
    public Named find(String name) throws SemanticException {
        Named result = null;
        // may be member in static import
        for (String next : singleStaticImports) {
            String id = StringUtil.getShortNameComponent(next);
            if (name.equals(id)) {
                String className = StringUtil.getPackageComponent(next);
                Named nt = ts.forName(className);
                if (nt instanceof Type) {
                    Type t = (Type) nt;
                    try {
                        result = ts.findMemberClass(t.toClass(), name);
                    }
                    catch (SemanticException e) {
                    }
                    if (result != null
                            && ((ClassType) result).flags().isStatic())
                        return result;
                }
            }
        }

        for (String next : staticOnDemandImports) {
            Named nt = ts.forName(next);

            if (nt instanceof Type) {
                Type t = (Type) nt;
                try {
                    result = ts.findMemberClass(t.toClass(), name);
                }
                catch (SemanticException e) {
                }
                if (result != null && ((ClassType) result).flags().isStatic())
                    return result;
            }
        }

        return super.find(name);
    }

    public List<ReferenceType> findTypesContainingMethodOrField(String name)
            throws SemanticException {
        List<ReferenceType> containingTypes = new ArrayList<>();
        for (String next : singleStaticImports) {
            String id = StringUtil.getShortNameComponent(next);
            if (name.equals(id)) {
                // it's a match
                String className = StringUtil.getPackageComponent(next);
                Named nt = ts.forName(className);
                if (nt instanceof ReferenceType) {
                    ReferenceType t = (ReferenceType) nt;
                    if (hasStatic(t.methodsNamed(name))
                            || isStatic(t.fieldNamed(name))) {
                        containingTypes.add(t);
                    }
                }
            }
        }

        for (String next : staticOnDemandImports) {
            Named nt = ts.forName(next);

            if (nt instanceof ReferenceType) {
                ReferenceType t = (ReferenceType) nt;
                if (hasStatic(t.methodsNamed(name))
                        || isStatic(t.fieldNamed(name))) {
                    containingTypes.add(t);
                }
            }
        }
        return containingTypes;
    }

    private static boolean hasStatic(List<? extends MemberInstance> members) {
        for (MemberInstance mi : members) {
            if (isStatic(mi)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStatic(MemberInstance mi) {
        return mi != null && mi.flags().isStatic();
    }

}
