package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import java.io.*;
import java.util.*;
import jltools.main.UsageError;

public interface ExtensionInfo {
    /** The extension that source files are expected to have */
    String fileExtension();

    /** The name of the compiler for usage messages */
    String compilerName();

    /** Options accepted by the extension. Newline terminated if non-empty */
    String options();

    TypeSystem getTypeSystem();
    ExtensionFactory getExtensionFactory();

    /** This method, when given a job and a compiler stage (e.g. Job.CHECKED)
     * returns a list of NodeVisitors that will be run at the specified
     * stage of compilation.  For example, the standard compiler runs a 
     * ConstantFolder visitor after the AmbiguityRemover during the 
     * Job.DISAMBIGUATE phase, so StandardExtensionInfo returns a list 
     * containing those two visitors when given the stage Job.DISAMBIGUATE.
     * The visitors will eventually be run in the order specified in the list
     */
    List getNodeVisitors(SourceJob job, int stage);

    /** Parse the rest of the command line after the "-ext" option
     *  up to the first source file. Return the index of the first
     *  source file (or args.length if none) */
    int parseCommandLine(String args[], int index, Map options) throws UsageError;

    // This method returns true if all source files should be compiled to 
    // stage before any further progress is made.
    // The standard is to wait for all classes to be cleaned before 
    // typechecking, but the Split extension needs to have all typechecked
    // before translating.
    boolean compileAllToStage(int stage);

    java_cup.runtime.lr_parser getParser(Reader reader, ErrorQueue eq);
}
