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
package polyglot.ext.jl5;

import java.util.Set;

import polyglot.frontend.ExtensionInfo;
import polyglot.main.OptFlag;
import polyglot.main.OptFlag.Arg;
import polyglot.main.OptFlag.Kind;
import polyglot.main.OptFlag.Switch;
import polyglot.main.Options;
import polyglot.main.UsageError;

public class JL5Options extends Options {
    public boolean translateEnums;
    public String enumImplClass;
    public String enumSetImplClass;
    public boolean removeJava5isms;
    public boolean morePermissiveInference;
    public boolean morePermissiveCasts;
    public boolean skip524checks;
    public boolean leaveCovariantReturns;

    public JL5Options(ExtensionInfo extension) {
        super(extension);
    }

    @Override
    protected void populateFlags(Set<OptFlag<?>> flags) {
        super.populateFlags(flags);

        flags.add(new OptFlag<String>(new String[] { "-enumImplClass",
                                              "--enumImplClass" },
                                      "<classname>",
                                      "Runtime class to implement Enums",
                                      "java.lang.Enum") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }

            @Override
            public Arg<String> defaultArg() {
                return createDefault(this.defaultValue);
            }
        });
        flags.add(new OptFlag<String>(new String[] { "-enumSetImplClass",
                                              "--enumSetImplClass" },
                                      "<classname>",
                                      "Runtime class to implement EnumSet",
                                      "java.util.EnumSet") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }

            @Override
            public Arg<String> defaultArg() {
                return createDefault(this.defaultValue);
            }
        });
        flags.add(new Switch(new String[] { "-removeJava5isms",
                                     "--removeJava5isms" },
                             "Translate Java 5 language features to Java 1.4 features"));
        flags.add(new Switch(Kind.SECRET,
                             new String[] { "-skip524checks", "--skip524checks" },
                             "Don't type check the result of removeJava5isms"));
        flags.add(new Switch(Kind.SECRET,
                             new String[] { "-leaveCovariantReturns",
                                     "--leaveCovariantReturns" },
                             "With removeJava5isms, does not translate away covariant returns"));
        flags.add(new Switch(new String[] { "-morepermissiveinference",
                                     "--morepermissiveinference" },
                             "Use a more permissive algorithm for type inference. (Experimental)"));
        flags.add(new Switch(new String[] { "-morepermissivecasts",
                                     "--morepermissivecasts" },
                             "Allow allow more permissive casts to and from numeric wrapper types. (Experimental)"));
    }

    @Override
    protected void handleArg(Arg<?> arg) throws UsageError {
        if (arg.flag().ids().contains("-enumImplClass")) {
            this.enumImplClass = (String) arg.value();
            // if anything other than java.lang.Enum, we may need to
            // translate Enums to normal Java classes
            translateEnums = enumImplClass.equals("java.lang.Enum");
        }
        else if (arg.flag().ids().contains("-enumSetImplClass")) {
            this.enumSetImplClass = (String) arg.value();
        }
        else if (arg.flag().ids().contains("-removeJava5isms")) {
            this.removeJava5isms = (Boolean) arg.value();
        }
        else if (arg.flag().ids().contains("-morepermissiveinference")) {
            this.morePermissiveInference = (Boolean) arg.value();
        }
        else if (arg.flag().ids().contains("-morepermissivecasts")) {
            this.morePermissiveCasts = (Boolean) arg.value();
        }
        else if (arg.flag().ids().contains("-skip524checks")) {
            this.skip524checks = (Boolean) arg.value();
        }
        else if (arg.flag().ids().contains("-leaveCovariantReturns")) {
            this.leaveCovariantReturns = (Boolean) arg.value();
        }
        else super.handleArg(arg);
    }
}
