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

package polyglot.visit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.LocalInstance;
import polyglot.util.InternalCompilerError;
import polyglot.util.UniqueID;

/**
 * The <code>AlphaRenamer</code> runs over the AST and alpha-renames any local
 * variable declarations that it encounters.
 **/
public class AlphaRenamer extends NodeVisitor {

    protected NodeFactory nf;

    // Each set in this stack tracks the set of local decls in a block that
    // we're traversing.
    protected Stack<Set<String>> setStack;

    protected Map<String, String> renamingMap;

    // Tracks the set of variables known to be fresh.
    protected Set<String> freshVars;

    /**
     * Should we also alpha-rename catch formals?
     */
    protected boolean renameCatchFormals;

    /**
     * Creates a visitor for alpha-renaming locals.
     *
     * @param nf  The node factory to be used when generating new nodes.
     **/
    public AlphaRenamer(NodeFactory nf) {
        this(nf, false);
    }

    public AlphaRenamer(NodeFactory nf, boolean renameCatchFormals) {
        this.nf = nf;

        this.setStack = new Stack<Set<String>>();
        this.setStack.push(new HashSet<String>());

        this.renamingMap = new HashMap<String, String>();
        this.freshVars = new HashSet<String>();

        this.renameCatchFormals = renameCatchFormals;
    }

    @Override
    public NodeVisitor enter(Node n) {
        if (n instanceof Block) {
            // Push a new, empty set onto the stack.
            setStack.push(new HashSet<String>());
        }

        if (this.renameCatchFormals && n instanceof Catch) {
            Catch c = (Catch) n;
            addToRenamingMap(c.formal().name());
        }
        if (n instanceof LocalDecl) {
            LocalDecl l = (LocalDecl) n;
            addToRenamingMap(l.name());
        }

        return this;
    }

    protected void addToRenamingMap(String name) {
        if (!freshVars.contains(name)) {
            // Add a new entry to the current renaming map.
            String name_ = UniqueID.newID(name);

            freshVars.add(name_);

            setStack.peek().add(name);
            renamingMap.put(name, name_);
        }
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof Block) {
            // Pop the current name set off the stack and remove the corresponding
            // entries from the renaming map.
            Set<String> s = setStack.pop();
            renamingMap.keySet().removeAll(s);
            return n;
        }

        if (n instanceof Local) {
            // Rename the local if its name is in the renaming map.
            Local l = (Local) n;
            String name = l.name();

            if (!renamingMap.containsKey(name)) {
                return n;
            }

            // Update the local instance as necessary.
            String newName = renamingMap.get(name);
            LocalInstance li = l.localInstance();
            if (li != null) li.setName(newName);

            return l.name(newName);
        }

        if (n instanceof LocalDecl) {
            // Rename the local decl.
            LocalDecl l = (LocalDecl) n;
            String name = l.name();

            if (freshVars.contains(name)) {
                return n;
            }

            if (!renamingMap.containsKey(name)) {
                throw new InternalCompilerError("Unexpected error encountered while "
                        + "alpha-renaming.");
            }

            // Update the local instance as necessary.
            String newName = renamingMap.get(name);
            LocalInstance li = l.localInstance();
            if (li != null) li.setName(newName);

            return l.name(newName);
        }

        if (n instanceof Catch && this.renameCatchFormals) {
            // Rename the catch formal.
            Catch c = (Catch) n;
            String name = c.formal().name();

            if (freshVars.contains(name)) {
                return n;
            }

            if (!renamingMap.containsKey(name)) {
                throw new InternalCompilerError("Unexpected error encountered while "
                        + "alpha-renaming.");
            }

            // Update the local instance as necessary.
            String newName = renamingMap.get(name);
            LocalInstance li = c.formal().localInstance();
            if (li != null) li.setName(newName);

            return c.formal(c.formal().name(newName));
        }

        return n;
    }
}
