package polyglot.frontend;

import polyglot.main.Options;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

import java.io.*;
import java.util.*;

/** A <code>TargetFactory</code> is responsible for opening output files. */
public class TargetFactory
{
    File outputDirectory;
    String outputExtension;
    boolean outputStdout;

    public TargetFactory(File outDir, String outExt, boolean so) {
	outputDirectory = outDir;
	outputExtension = outExt;
	outputStdout = so;
    }

    /** Open a writer to the output file for the class in the given package. */
    public Writer outputWriter(String packageName, String className,
	    Source source) throws IOException 
    {
	return outputWriter(outputFile(packageName, className, source));
    }

    /** Open a writer to the output file. */
    public Writer outputWriter(File outputFile) throws IOException {
	if (Report.should_report(Report.frontend, 2))
	    Report.report(2, "Opening " + outputFile + " for output.");

	if (outputStdout) {
	    return new UnicodeWriter(new PrintWriter(System.out));
	}

	if (! outputFile.getParentFile().exists()) {
	    File parent = outputFile.getParentFile();
	    parent.mkdirs();
	}

	return new UnicodeWriter(new FileWriter(outputFile));
    }

    /** Return a file object for the output of the source file in the given package. */
    public File outputFile(String packageName, Source source) {
	String name;
	name = new File(source.name()).getName();
	name = name.substring(0, name.lastIndexOf('.'));
	return outputFile(packageName, name, source);
    }

    /** Return a file object for the output of the class in the given package. */
    public File outputFile(String packageName, String className, Source source)
    {
	if (outputDirectory == null) {
	      throw new InternalCompilerError("Output directory not set.");
	}

	if (packageName == null) {
	    packageName = "";
	}

	File outputFile = new File(outputDirectory,
				   packageName.replace('.', File.separatorChar)
				   + File.separatorChar
				   + className
				   + "." + outputExtension);

        if (source != null && outputFile.getPath().equals(source.path())) {
	    throw new InternalCompilerError("The output file is the same as the source file");
	}
	
	return outputFile;
    }
}
