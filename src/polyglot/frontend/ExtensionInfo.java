package jltools.frontend;

import jltools.types.*;
import jltools.util.*;
import java.io.*;
import java.util.*;

public interface ExtensionInfo {
    TypeSystem getTypeSystem();
    List getNodeVisitors(SourceJob job, int stage);
    java_cup.runtime.lr_parser getParser(Reader reader, ErrorQueue eq);
}
