package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.io.*;
import java.util.*;

public class StandardExtensionInfo implements ExtensionInfo {
    protected TypeSystem ts = null;
    protected ExtensionFactory ef = null;

    protected TypeSystem createTypeSystem() {
	return new StandardTypeSystem();
    }

    public TypeSystem getTypeSystem() {
	if (ts == null) {
	  ts = createTypeSystem();
	}
	return ts;
    }

    protected ExtensionFactory createExtensionFactory() {
	return new StandardExtensionFactory();
    }

    public ExtensionFactory getExtensionFactory() {
	if (ef == null) {
	  ef = createExtensionFactory();
	}
	return ef;
    }

    public List getNodeVisitors(SourceJob job, int goal) {
	LinkedList l = new LinkedList();

	Target t = job.getTarget();
	TableClassResolver cr = job.getClassResolver();
	ImportTable it = job.getImportTable();
        Compiler compiler = Compiler.getCompiler();
        int outputWidth = Compiler.getOutputWidth();
	TargetFactory tf = Compiler.getTargetFactory();

	ErrorQueue eq;

        try {
	  eq = t.getErrorQueue();
	}
	catch (IOException e) {
	  throw new InternalCompilerError("Couldn't get error queue");
	}

	switch (goal) {
	    case Job.PARSED:
		break;
	    case Job.READ:
		l.add(new SymbolReader(it, cr, t, tf, ts, eq));
		break;
	    case Job.CLEANED:
		l.add(new SignatureCleaner(ef, ts, it, cr, eq, compiler));
		break;
	    case Job.DISAMBIGUATED:
		l.add(new AmbiguityRemover(ef, ts, it, eq));
		l.add(new ConstantFolder(ef));
		break;
	    case Job.CHECKED:
		l.add(new TypeChecker(ef, ts, it, eq));
		l.add(new ExceptionChecker(eq));
		break;
	    case Job.TRANSLATED:
		if (Compiler.serializeClassInfo()) {
		    l.add(new ClassSerializer(ts, t.getLastModifiedDate(), eq));
		}
		l.add(new TranslationVisitor(ef, it, t, ts, eq, outputWidth));
		break;
	    default:
		throw new InternalCompilerError("Invalid compiler stage: " +
		    goal);
	}

	return l;
    }

    public java_cup.runtime.lr_parser getParser(Reader reader, ErrorQueue eq)
    {
	jltools.lex.Lexer lexer = new jltools.lex.Lexer(reader, eq);
	return new jltools.parse.Grm( lexer, ts, eq);
    }
}
