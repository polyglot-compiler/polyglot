package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import java.io.*;
import java.util.*;

public interface ExtensionInfo {
    String fileExtension();
	// The extension that source files are expected to have

    TypeSystem getTypeSystem();
    ExtensionFactory getExtensionFactory();
    List getNodeVisitors(SourceJob job, int stage);

    // This method returns true if all source files should be compiled to 
    // stage before any further progress is made.
    // The standard is to wait for all classes to be cleaned before 
    // typechecking, but the Split extension needs to have all typechecked
    // before translating.
    boolean compileAllToStage(int stage);

    java_cup.runtime.lr_parser getParser(Reader reader, ErrorQueue eq);
}
