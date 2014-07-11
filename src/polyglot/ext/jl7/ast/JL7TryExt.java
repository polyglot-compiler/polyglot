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
package polyglot.ext.jl7.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.JLang;
import polyglot.ast.Lang;
import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.Node;
import polyglot.ast.Throw;
import polyglot.ast.Try;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.util.SubtypeSet;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;

public class JL7TryExt extends JL7TermExt implements JL7TryOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Try node() {
        return (Try) super.node();
    }

    @Override
    public Block exceptionCheckTryBlock(ExceptionChecker ec)
            throws SemanticException {
        Block b = ec.superLang(lang()).exceptionCheckTryBlock(this.node(), ec);

        ((J7Lang) ec.lang()).checkPreciseRethrows(this.node(), b, ec);

        return b;
    }

    @Override
    public ExceptionChecker constructTryBlockExceptionChecker(
            ExceptionChecker ec) {
        return ec.superLang(lang())
                 .constructTryBlockExceptionChecker(this.node(), ec);
    }

    @Override
    public List<Catch> exceptionCheckCatchBlocks(ExceptionChecker ec)
            throws SemanticException {
        return ec.superLang(lang()).exceptionCheckCatchBlocks(this.node(), ec);
    }

    @Override
    public Block exceptionCheckFinallyBlock(ExceptionChecker ec)
            throws SemanticException {
        return ec.superLang(lang()).exceptionCheckFinallyBlock(this.node(), ec);
    }

    @Override
    public void checkPreciseRethrows(Block tryBlock, ExceptionChecker ec) {
        TypeSystem ts = ec.typeSystem();
        Try n = this.node();

        // For each catch block, identify which exceptions can get to it.
        // First, get the set of all exceptions that the try block can throw
        SubtypeSet thrown =
                new SubtypeSet(ts.Throwable(), ec.lang().throwTypes(tryBlock,
                                                                    ts,
                                                                    ec));

        // Second, go through the catch blocks, and see what exceptions can actually reach them.
        for (Catch cb : n.catchBlocks()) {
            Type catchType = cb.catchType();

            // The exceptions that can reach cb are the exceptions in thrown
            // that may be assignable to catchType.

            ((J7Lang) ec.lang()).preciseRethrowsForCatchBlock(this.node(),
                                                              cb,
                                                              thrown,
                                                              ec);

            thrown.remove(catchType);
        }
    }

    @Override
    public void preciseRethrowsForCatchBlock(Catch cb, SubtypeSet reaching,
            ExceptionChecker ec) {
        List<Type> s = new ArrayList<>();
        for (Type t : reaching) {
            if (cb.catchType().isSubtype(t)) {
                // nope, it's not worth it.
                // No precision to be gained.
                return;
            }
            if (t.isSubtype(cb.catchType())) {
                s.add(t);
            }
        }
        // now, if cb.formal() is final, or effectively final, then
        // set the throwsSet of any rethrow.
        if (isFinalFormal(ec.lang(), ec.superLangMap(), cb)) {
            setThrowsTypes(ec.lang(),
                           ec.superLangMap(),
                           cb.formal().localInstance(),
                           s,
                           cb.body());
        }
    }

    protected boolean isFinalFormal(JLang lang, Map<Lang, Lang> superLangMap,
            Catch cb) {
        if (cb.formal().localInstance().flags().isFinal()
                || cb instanceof MultiCatch) {
            // explcitly final
            return true;
        }
        // Check to see if the local is effectively final.
        EffectivelyFinalVisitor v =
                new EffectivelyFinalVisitor(lang,
                                            superLangMap,
                                            cb.formal().localInstance());
        cb.body().visit(v);

        return v.isEffectivelyFinal();
    }

    public class EffectivelyFinalVisitor extends NodeVisitor {
        boolean isEffectivelyFinal;
        LocalInstance li;

        public EffectivelyFinalVisitor(JLang lang,
                Map<Lang, Lang> superLangMap, LocalInstance li) {
            super(lang, superLangMap);
            this.li = li;
            isEffectivelyFinal = true;
        }

        public boolean isEffectivelyFinal() {
            return isEffectivelyFinal;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof LocalAssign) {
                LocalAssign la = (LocalAssign) n;
                if (la.left().localInstance().equals(li)) {
                    isEffectivelyFinal = false;
                }
            }
            return n;
        }
    }

    protected void setThrowsTypes(JLang lang, Map<Lang, Lang> superLangMap,
            LocalInstance localInstance, List<Type> s, Block b) {
        SetThrowSetVisitor v =
                new SetThrowSetVisitor(lang, superLangMap, localInstance, s);
        b.visit(v);
    }

    public class SetThrowSetVisitor extends NodeVisitor {
        LocalInstance li;
        List<Type> s;

        public SetThrowSetVisitor(JLang lang, Map<Lang, Lang> superLangMap,
                LocalInstance li, List<Type> s) {
            super(lang, superLangMap);
            this.li = li;
            this.s = s;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Throw) {
                Throw t = (Throw) n;
                if (t.expr() instanceof Local) {
                    Local l = (Local) t.expr();
                    if (l.localInstance().equals(li)) {
                        // set the throw set.
                        JL7ThrowExt ext = (JL7ThrowExt) JL7Ext.ext(t);
                        ext.throwSet = s;
                    }
                }
            }
            return n;
        }
    }
}
