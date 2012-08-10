package polyglot.ext.jl5;

import java.util.Set;

import polyglot.frontend.ExtensionInfo;
import polyglot.main.OptFlag;
import polyglot.main.OptFlag.Arg;
import polyglot.main.OptFlag.Switch;
import polyglot.main.Options;
import polyglot.main.UsageError;

public class JL5Options extends Options {
    public boolean translateEnums;
    public String enumImplClass;
    public boolean removeJava5isms;
    public boolean morePermissiveInference;

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
                return createDefault("java.lang.Enum");
            }
        });
        flags.add(new Switch(new String[] { "-removeJava5isms",
                                     "--removeJava5isms" },
                             "Translate Java 5 language features to Java 1.4 features"));
        flags.add(new Switch(new String[] { "-morepermissiveinference",
                                     "--morepermissiveinference" },
                             "Use a more permissive algorithm for type inference. (Experimental)"));
    }

    @Override
    protected void handleArg(Arg<?> arg) throws UsageError {
        if (arg.flag().ids().contains("-enumImplClass")) {
            this.enumImplClass = (String) arg.value();
            // if anything other than java.lang.Enum, we may need to
            // translate Enums to normal Java classes
            translateEnums = enumImplClass.equals("java.lang.Enum");
        }
        else if (arg.flag().ids().contains("-removeJava5isms")) {
            this.removeJava5isms = (Boolean) arg.value();
        }
        else if (arg.flag().ids().contains("-morepermissiveinference")) {
            this.morePermissiveInference = (Boolean) arg.value();
        }
        else super.handleArg(arg);
    }
}
