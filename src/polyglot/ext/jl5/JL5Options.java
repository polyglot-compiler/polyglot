package polyglot.ext.jl5;

import java.io.PrintStream;
import java.util.Set;

import polyglot.frontend.ExtensionInfo;
import polyglot.main.Main;
import polyglot.main.Options;
import polyglot.main.UsageError;

public class JL5Options extends Options {
        public String enumImplClass;
        public boolean removeJava5isms;
        
        public JL5Options(ExtensionInfo extension) {
            super(extension);
        }
        
        @Override
        public void setDefaultValues() {
            super.setDefaultValues();
            this.enumImplClass = null;
            this.removeJava5isms = false;
            this.assertions = true;
        }
        
        @Override
        protected int parseCommand(String args[], int index, Set source) throws UsageError, Main.TerminationException {
            if (args[index].equals("-enumImplClass") || args[index].equals("--enumImplClass")) {
                index++;
                this.enumImplClass = args[index++];
                return index;
            }
           
            if (args[index].equals("-removeJava5isms") || args[index].equals("--removeJava5isms")) {
                index++;
                this.removeJava5isms = true;
                return index;
            }
           
            return super.parseCommand(args, index, source);
        }
        
        @Override
        public void usage(PrintStream out) {
            super.usage(out);
            usageForFlag(out, "-removeJava5isms", "Translate Java 5 language features to Java 1.4 features");
            usageForFlag(out, "-enumImplClass", "Runtime class to implement Enums (if not specified, translate to java.lang.Enum)");
        }
}
