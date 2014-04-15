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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ArrayType;
import polyglot.types.Context;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class ParamTypeNode_c extends TypeNode_c implements ParamTypeNode {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Id id;

    protected List<TypeNode> bounds;

    public ParamTypeNode_c(Position pos, List<TypeNode> bounds, Id id) {
        super(pos);
        this.id = id;
        this.bounds = bounds;
    }

    @Override
    public ParamTypeNode id(Id id) {
        ParamTypeNode_c n = (ParamTypeNode_c) copy();
        n.id = id;
        return n;
    }

    @Override
    public Id id() {
        return this.id;
    }

    @Override
    public ParamTypeNode bounds(List<TypeNode> l) {
        ParamTypeNode_c n = (ParamTypeNode_c) copy();
        n.bounds = l;
        return n;
    }

    @Override
    public List<TypeNode> bounds() {
        return bounds;
    }

    public ParamTypeNode reconstruct(List<TypeNode> bounds) {
        if (!CollectionUtil.equals(bounds, this.bounds)) {
            ParamTypeNode_c n = (ParamTypeNode_c) copy();
            n.bounds = bounds;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<TypeNode> bounds = visitList(this.bounds, v);
        return reconstruct(bounds);
    }

    @Override
    public Context enterScope(Context c) {
        c = ((JL5Context) c).pushTypeVariable((TypeVariable) type());
        return super.enterScope(c);
    }

    @Override
    public void addDecls(Context c) {
        // we do not need to add the type variable to the scope
        // since that will be taken care of by the parent node.
        super.addDecls(c);
    }

    // nothing needed for buildTypesEnter - not a code block like methods

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        // makes a new TypeVariable with a list of bounds which
        // are unknown types
        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        ArrayList<ReferenceType> typeList =
                new ArrayList<ReferenceType>(bounds.size());
        for (int i = 0; i < bounds.size(); i++) {
            typeList.add(ts.unknownReferenceType(position()));
        }

        ReferenceType intersection = ts.intersectionType(position(), typeList);
        TypeVariable iType = ts.typeVariable(position(), id.id(), intersection);

        return type(iType);

    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        // all of the children (bounds list) will have already been 
        // disambiguated and should there for be actual types
        JL5TypeSystem ts = (JL5TypeSystem) ar.typeSystem();

        boolean ambiguous = false;
        ArrayList<Type> typeList = new ArrayList<Type>();
        for (TypeNode tn : bounds) {
            if (!tn.isDisambiguated()) {
                // not disambiguated yet
                ambiguous = true;
            }
//			if (!tn.type().isReference()) {
//			    throw new SemanticException("Cannot instantiate a type parameter with type " + tn.type());
//			}
            typeList.add(tn.type());
        }
        if (!ambiguous) {
            TypeVariable tv = (TypeVariable) this.type();
            //		System.err.println("paramtypenode_c : dismab and setting bounds of " + tv + " to " + typeList);
            List<ReferenceType> refTypeList = new ArrayList<ReferenceType>();
            for (Type t : typeList)
                refTypeList.add((ReferenceType) t);
            tv.setUpperBound(ts.intersectionType(this.position(), refTypeList));
        }
        return this;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        for (int i = 0; i < bounds.size(); i++) {
            TypeNode ti = bounds.get(i);
            for (int j = i + 1; j < bounds.size(); j++) {
                TypeNode tj = bounds.get(j);
                if (tc.typeSystem().equals(ti.type(), tj.type())) {
                    throw new SemanticException("Duplicate bound in type variable declaration",
                                                tj.position());
                }
            }
        }
        // check no arrays in bounds list
        for (int i = 0; i < bounds.size(); i++) {
            TypeNode ti = bounds.get(i);
            if (ti.type() instanceof ArrayType) {
                throw new SemanticException("Unexpected type bound in type variable declaration",
                                            ti.position());
            }
        }

        // only first bound can be a class otherwise must be interfaces
        for (int i = 0; i < bounds.size(); i++) {
            TypeNode tn = bounds.get(i);
            if (i > 0 && !tn.type().toClass().flags().isInterface()) {
                throw new SemanticException("Interface expected here.",
                                            tn.position());
            }
        }

        // if first bound is a type variable, the type variable must be the only bound
        if (bounds.size() > 1 && bounds.get(0).type() instanceof TypeVariable)
            throw new SemanticException("Cannot specify any additional bound "
                    + "when first bound is a type variable.", bounds.get(1)
                                                                    .position());

        //XXX: are these checks necessary for us?
        //ts.checkIntersectionBounds(tv.bounds(), false);

        return super.typeCheck(tc);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(id.id());
        if (bounds() != null && !bounds().isEmpty()) {
            w.write(" extends ");
            for (Iterator<TypeNode> it = bounds.iterator(); it.hasNext();) {
                TypeNode tn = it.next();
                print(tn, w, tr);
                if (it.hasNext()) {
                    w.write(" & ");
                }
            }
        }
    }

    @Override
    public String name() {
        return id().toString();
    }
}
