package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import java.io.*;
import java.util.*;
import jltools.main.UsageError;
import jltools.main.Options;

public interface ExtensionInfo {
    /** The extension that source files are expected to have */
    String fileExtension();

    /** The name of the compiler for usage messages */
    String compilerName();

    /** Options accepted by the extension. Newline terminated if non-empty */
    String options();

    /** Produce a type system for this language extension. */
    TypeSystem getTypeSystem();

    /** Produce an extension factory for this language extension. */
    ExtensionFactory getExtensionFactory();

    /** This method, when given a job and a compiler stage (such as Job.CHECKED)
     * returns a list of NodeVisitors that will be run at the specified
     * stage of compilation.  For example, the standard compiler runs a 
     * ConstantFolder visitor after the AmbiguityRemover during the 
     * Job.DISAMBIGUATE phase, so StandardExtensionInfo returns a list 
     * containing those two visitors when given the stage Job.DISAMBIGUATE.
     * The visitors will eventually be run in the order specified in the list
     */
    List getNodeVisitors(Compiler c, SourceJob job, int stage);

    /** Parse as much of the command line as this extension understands,
     *  up to the first source file. Return the index of the first
     *  switch not understood by the extension, or of the first
     *  source file, or args.length if neither. */
    int parseCommandLine(String args[], int index, Options options)
      throws UsageError;

    /** Return true if all source files should be compiled to 
     *  stage <code>stage</code> before making further progress.
     *  The standard is to wait for all classes to be cleaned before 
     *  typechecking, but the Split extension needs to have all typechecked
     *  before translating.
     */
    boolean compileAllToStage(int stage);

    /** Construct and return a parser for the extended language. */
    java_cup.runtime.lr_parser getParser(Reader reader, ErrorQueue eq);
}
