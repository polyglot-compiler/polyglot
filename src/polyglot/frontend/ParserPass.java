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

package polyglot.frontend;

import java.io.IOException;
import java.io.Reader;

import polyglot.ast.Node;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;

/**
 * A pass which runs a parser. After parsing it stores the AST in the Job. so it
 * can be accessed by later passes.
 */
public class ParserPass extends AbstractPass {
    protected Compiler compiler;

    public ParserPass(Compiler compiler, Goal goal) {
        super(goal);
        this.compiler = compiler;
    }

    @Override
    public boolean run() {
        ErrorQueue eq = compiler.errorQueue();

        FileSource source = (FileSource) goal.job().source();

        try {
            Reader reader = source.openReader(false);

            Parser p = goal.job().extensionInfo().parser(reader, source, eq);

            if (Report.should_report(Report.frontend, 2))
                Report.report(2, "Using parser " + p);

            Node ast = p.parse();

            reader.close();

            if (ast != null) {
                goal.job().ast(ast);
                return true;
            }

            return false;
        }
        catch (IOException e) {
            eq.enqueue(ErrorInfo.IO_ERROR,
                       e.getMessage(),
                       new Position(goal.job().source().path(),
                                    goal.job().source().name(),
                                    1,
                                    1,
                                    1,
                                    1));

            return false;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "(" + goal.job().source() + ")";
    }
}
