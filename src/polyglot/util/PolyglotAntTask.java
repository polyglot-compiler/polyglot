package polyglot.util;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;

/**
 * TODO: Documentation
 */
public class PolyglotAntTask extends MatchingTask {
	protected String extensionName = null;
	protected Class extensionClass = null;
	protected File destDir = null;
	protected Path src = null;
	protected Path bootclasspath = null;
	protected String srcExt = "java";
	protected String outputExt = "java";
	protected File optionsFile = null;
	protected String post = null;
	protected boolean noSerial = false;
	protected boolean noOutput = false;
	protected boolean fqcn = false;
	protected boolean onlyToJava = false;
	protected String additionalArgs = "";
	protected Path compileClasspath = null;
	protected Path compileSourcepath = null;

	public PolyglotAntTask() { }
	
	public void init() { }
	
	public void execute() throws BuildException {
		dump(System.out);
		checkParameters();
		// scan source directories and dest directory to build up
		File[] compileFiles = getCompileFiles();
		
		if (compileFiles.length == 0) {
			// nothing to do. no files to compile
			return;
		}
		
		// build up the argument list
		String[] args = constructArgList(compileFiles);
		
		System.out.println(Arrays.asList(args));
		
		// invoke the polyglot compiler.
		compile(args);
	}
	
	protected File[] getCompileFiles() {
		File[] compileFiles = new File[0];
		String[] srcList = src.list();
		for (int i = 0; i < srcList.length; i++) {
			File srcDir = getProject().resolveFile(srcList[i]);
			if (!srcDir.exists()) {
				throw new BuildException("srcdir \""
										 + srcDir.getPath()
										 + "\" does not exist!", getLocation());
			}

			DirectoryScanner ds = this.getDirectoryScanner(srcDir);
			String[] files = ds.getIncludedFiles();

			compileFiles = scanDir(compileFiles, srcDir, destDir != null ? destDir : srcDir, files);
		}
		return compileFiles;
	}
	
	/**
	 * Scans the directory looking for source files to be compiled.
	 * The results are returned in the class variable compileList
	 */
	protected File[] scanDir(File[] compileFiles, File srcDir, File destDir, String[] files) {
		GlobPatternMapper m = new GlobPatternMapper();
		m.setFrom("*." + srcExt);
		m.setTo("*." + destFileExt());
		SourceFileScanner sfs = new SourceFileScanner(this);
		File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);

		if (newFiles.length > 0) {
			File[] newCompileList = new File[compileFiles.length + 
				newFiles.length];
			System.arraycopy(compileFiles, 0, newCompileList, 0,
				compileFiles.length);
			System.arraycopy(newFiles, 0, newCompileList,
				compileFiles.length, newFiles.length);
			compileFiles = newCompileList;
		}
		return compileFiles;
	}

	protected String destFileExt() {
		return onlyToJava ? outputExt : "class";
	}
	
	protected void checkParameters() throws BuildException {
		// at most one of extenstionClass and extensionName is set.
		if (extensionClass != null && extensionName != null) {
			throw new BuildException("At most one of extclass and extname can be set.");
		}
		
		// other checks?
	}

	protected String[] constructArgList(File[] files) {
		List argList = new ArrayList();
		
		// set up command line args
		if (optionsFile != null) {
			argList.add("@" + optionsFile.getPath());
		}
		if (extensionName != null) {
			argList.add("-ext");
			argList.add(extensionName);
		}
		if (extensionClass != null) {
			argList.add("-extclass");
			argList.add(extensionClass.getName());
		}
		if (noSerial) {
			argList.add("-noserial");
		}
		if (noOutput) {
			argList.add("-nooutput");
		}
		if (fqcn) {
			argList.add("-fqcn");
		}
		if (onlyToJava) {
			argList.add("-c");
		}
		if (post != null) {
			argList.add("-post");
			argList.add(post);
		}
		if (srcExt != null) {
			argList.add("-sx");
			argList.add(srcExt);
		}
		if (outputExt != null) {
			argList.add("-ox");
			argList.add(outputExt);
		}
		if (destDir != null) {
			argList.add("-d");
			argList.add(destDir.getPath());
		}
		if (compileSourcepath != null) {
			argList.add("-sourcepath");
			argList.add(compileSourcepath.toString());
		}
		else if (src != null) {
			argList.add("-sourcepath");
			argList.add(src.toString());
		}
		if (bootclasspath != null) {
			argList.add("-bootclasspath");
			argList.add(bootclasspath.toString());
		}
		if (compileClasspath != null) {
			argList.add("-classpath");
			argList.add(compileClasspath.toString());
		}
		if (additionalArgs != null) {
			StringTokenizer st = new StringTokenizer(additionalArgs, " ");
			while (st.hasMoreTokens()) {
				argList.add(st.nextToken());
			}
		}
		
		
		// add the files to compile.
		int argListSize = argList.size();
		String[] args = new String[argListSize + files.length];
		argList.toArray(args);
		
		for (int i = 0; i < files.length; ++i) {
			args[argListSize + i] = files[i].getPath();
		}
		
		return args;
	}
	protected void compile(String[] args) {
		polyglot.main.Main.main(args);
	}
	   
	protected void dump(PrintStream out) {
		out.println("extensionName = " + extensionName);
		out.println("extensionClass = " + extensionClass);
		out.println("destdir = " + destDir);
		out.println("src = " + src);
		out.println("bootclasspath = " + bootclasspath);
		out.println("srcExt = " + srcExt);
		out.println("outputExt = " + outputExt);
		out.println("optionsFile = " + optionsFile);
		out.println("post = " + post);
		out.println("noSerial = " + noSerial);
		out.println("noOutput = " + noOutput);
		out.println("fqcn = " + fqcn);
		out.println("onlyToJava = " + onlyToJava);
		out.println("additionalArgs = " + additionalArgs);
		out.println("compileClasspath = " + compileClasspath);
		out.println("compileSourcepath = " + compileSourcepath);
		out.println("fileset = " + Arrays.asList(this.getCompileFiles()));

	}
	public void setExtname(String extensionName) {
        this.extensionName = extensionName;
    }

    public void setExtclass(Class extensionClass) {
        this.extensionClass = extensionClass;
    }

    public void setDestdir(File destdir) {
        this.destDir = destdir;
    }

	public Path createSrc() {
		if (src == null) {
			src = new Path(getProject());
		}
		return src.createPath();
	}
	public void setSrcdir(Path srcDir) {
		if (src == null) {
			src = srcDir;
		} else {
			src.append(srcDir);
		}
	}

	public void setBootclasspath(Path bootclasspath) {
		if (this.bootclasspath == null) {
			this.bootclasspath = bootclasspath;
		} else {
			this.bootclasspath.append(bootclasspath);
		}
	}
	
	public Path createBootclasspath() {
		if (bootclasspath == null) {
			bootclasspath = new Path(getProject());
		}
		return bootclasspath.createPath();
	}
	
	/**
	 * Adds a reference to a classpath defined elsewhere.
	 */
	public void setBootClasspathRef(Reference r) {
		createBootclasspath().setRefid(r);
	}

	public void setSrcext(String srcExt) {
        this.srcExt = srcExt;
    }

    public void setDestext(String destExt) {
        this.outputExt = destExt;
    }
    public void setOptions(File optionsFile) {
        this.optionsFile = optionsFile;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public void setNoserial(boolean noSerial) {
        this.noSerial = noSerial;
    }

    public void setNooutput(boolean noOutput) {
        this.noOutput = noOutput;
    }

    public void setFqcn(boolean fqcn) {
        this.fqcn = fqcn;
    }

    public void setOnlytojava(boolean onlyToJava) {
        this.onlyToJava = onlyToJava;
    }
    
	public void setClasspath(Path classpath) {
		if (compileClasspath == null) {
			compileClasspath = classpath;
		} else {
			compileClasspath.append(classpath) ; 
		}
	}

	/**
	 * Adds a path to the classpath.
	 */
	public Path createClasspath() {
		if (compileClasspath == null) {
			compileClasspath = new Path(getProject());
		}
		return compileClasspath.createPath();
	}

	/**
	 * Adds a reference to a classpath defined elsewhere.
	 */
	public void setClasspathRef(Reference r) {
		createClasspath().setRefid(r);
	}

	/**
	 * Set the sourcepath to be used for this compilation.
	 */
	public void setSourcepath(Path sourcepath) {
		if (compileSourcepath == null) {
			compileSourcepath = sourcepath;
		} else {
			compileSourcepath.append(sourcepath);
		}
	}

	/** Gets the sourcepath to be used for this compilation. */
	public Path getSourcepath() {
		return compileSourcepath;
	}

	/**
	 * Adds a path to sourcepath.
	 */
	public Path createSourcepath() {
		if (compileSourcepath == null) {
			compileSourcepath = new Path(getProject());
		}
		return compileSourcepath.createPath();
	}

	/**
	 * Adds a reference to a source path defined elsewhere.
	 */
	public void setSourcepathRef(Reference r) {
		createSourcepath().setRefid(r);
	}

}
