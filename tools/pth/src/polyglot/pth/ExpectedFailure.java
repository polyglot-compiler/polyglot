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
package polyglot.pth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import polyglot.util.ErrorInfo;

/**
 *
 */
public class ExpectedFailure {
    public ExpectedFailure(int kind) {
        this(kind, null);
    }

    public ExpectedFailure(String errMsg) {
        this(null, errMsg);
    }

    public ExpectedFailure(Integer kind, String errMsg) {
        this.kind = kind;
        errMsgRegExp = errMsg;
    }

    final Integer kind;
    final String errMsgRegExp;

    public boolean matches(ErrorInfo e) {
        if (kind != null && kind.intValue() != e.getErrorKind()) return false;
        if (errMsgRegExp != null) {
            Matcher m = Pattern.compile(errMsgRegExp).matcher(e.getMessage());
            return m.find();
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExpectedFailure) {
            ExpectedFailure that = (ExpectedFailure) o;
            return (that.kind == kind
                    || that.kind != null && that.kind.equals(kind))
                    && (that.errMsgRegExp == errMsgRegExp
                            || that.errMsgRegExp != null
                                    && that.errMsgRegExp.equals(errMsgRegExp));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (errMsgRegExp == null ? -323 : errMsgRegExp.hashCode())
                + (kind == null ? 41 : kind.hashCode());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (kind != null)
            sb.append(ErrorInfo.getErrorString(kind.intValue()));
        else sb.append("error");
        if (errMsgRegExp != null) {
            sb.append(" matching the regular expression '");
            sb.append(errMsgRegExp);
            sb.append('\'');
        }
        return sb.toString();
    }
}
