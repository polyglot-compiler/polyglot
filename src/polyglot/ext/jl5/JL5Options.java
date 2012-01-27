package polyglot.ext.jl5;

import java.io.PrintStream;
import java.util.Set;

import polyglot.frontend.ExtensionInfo;
import polyglot.main.Main;
import polyglot.main.Options;
import polyglot.main.UsageError;

public class JL5Options extends Options {
        public String enumImplClass;
        
        public JL5Options(ExtensionInfo extension) {
            super(extension);
        }
        
        @Override
        public void setDefaultValues() {
            super.setDefaultValues();
            this.enumImplClass = null;
        }
        
        @Override
        protected int parseCommand(String args[], int index, Set source) throws UsageError, Main.TerminationException {
            if (args[index].equals("-enumImplClass") || args[index].equals("--enumImplClass")) {
                index++;
                this.enumImplClass = args[index++];
                return index;
            }
           
            return super.parseCommand(args, index, source);
        }
        
        @Override
        public void usage(PrintStream out) {
            super.usage(out);
            usageForFlag(out, "-enumImplClass", "Runtime class to implement Enums (if not specified, translate to java.lang.Enum)");
        }
}
