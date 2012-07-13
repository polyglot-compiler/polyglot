/*
 * Author : Stephen Chong
 * Created: Nov 21, 2003
 */
package polyglot.pth;

import java.io.File;
import java.util.*;

import polyglot.util.ErrorInfo;
import polyglot.util.SilentErrorQueue;

/**
 * 
 */
public class SourceFileTest extends AbstractTest {
    private static final String JAVAC = "javac";
    protected final List<String> sourceFilenames;
    protected String extensionClassname = null;
    protected String[] extraArgs;
    protected List<String> mainExtraArgs;
    protected final SilentErrorQueue eq;
    protected String destDir;
    
    protected List<ExpectedFailure> expectedFailures;
    
    protected Set<String> undefinedEnvVars = new HashSet<String>();
        
    public SourceFileTest(String filename) {
        super(new File(filename).getName());
        this.sourceFilenames = Collections.singletonList(filename);
        this.eq = new SilentErrorQueue(100, this.getName()); 

    }

    public SourceFileTest(List<String> filenames) {
        super(filenames.toString());
        this.sourceFilenames = filenames;
        this.eq = new SilentErrorQueue(100, this.getName()); 
    }

    public SourceFileTest(String[] filenames) {
        this(Arrays.asList(filenames).toString());
    }
    
    
    @Override
    public String getUniqueId() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getName());
        if (this.extensionClassname != null) {
            sb.append("::");
            sb.append(extensionClassname);
        }
        if (this.extraArgs != null) {
            for (int i = 0; i < this.extraArgs.length; i++) {
                sb.append("::");
                sb.append(this.extraArgs[i]);
            }
        }
        return sb.toString();
    }

    
    public void setExpectedFailures(List<ExpectedFailure> expectedFailures) {
        this.expectedFailures = expectedFailures;
    }

    protected boolean runTest() {
        for (String filename : sourceFilenames) {
            File sourceFile = new File(filename);
            if (!sourceFile.exists()) {
                setFailureMessage("File not found.");
                return false;
            }
        }
        
        // invoke the compiler on the file.
        try {
            if (JAVAC.equals(this.getExtensionClassname())) {
                // invoke javac on the program
                invokeJavac(getSourceFileNames());
            }
            else {
                invokePolyglot(getSourceFileNames());
            }
        }
        catch (polyglot.main.Main.TerminationException e) {
            if (e.getMessage() != null) {
                setFailureMessage(e.getMessage());
                return false;
            }
            else {
                if (!eq.hasErrors()) {
                    setFailureMessage("Failed to compile for unknown reasons: " + 
                             e.toString());
                    return false;
                }                    
            }
        }        
        catch (RuntimeException e) {
            if (e.getMessage() != null) {
                setFailureMessage(e.getMessage());
                e.printStackTrace();
                return false;
            }
            else {
                setFailureMessage("Uncaught " + e.getClass().getName());
                e.printStackTrace();
                return false;
            }
        }        
        return checkErrorQueue(eq);
    }
    
    protected void postRun() {
        output.finishTest(this, eq);
    }

    protected boolean checkErrorQueue(SilentErrorQueue eq) {
        List<ErrorInfo> errors = new ArrayList<ErrorInfo>(eq.getErrors());
        
        boolean swallowRemainingFailures = false;
        for (ExpectedFailure f : expectedFailures) {
            if (f instanceof AnyExpectedFailure) {
                swallowRemainingFailures = true;
                continue;
            }
            
            boolean found = false;
            for (Iterator<ErrorInfo> j = errors.iterator(); j.hasNext(); ) {
                ErrorInfo e = j.next();
                if (f.matches(e)) {
                    // this error info has been matched. remove it.
                    found = true;
                    j.remove();
                    break;
                }
            }
            if (!found) {
                setFailureMessage("Expected to see " + f.toString());
                return false;
            }            
        }
        
        // are there any unaccounted for errors?
        if (!errors.isEmpty() && !swallowRemainingFailures) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<ErrorInfo> iter = errors.iterator(); iter.hasNext(); ) {
                ErrorInfo err = iter.next();                        
                sb.append(err.getMessage());
                if (err.getPosition() != null) {
                    sb.append(" (");
                    sb.append(err.getPosition());
                    sb.append(")");
                }
                if (iter.hasNext()) sb.append("; ");
            }
            setFailureMessage(sb.toString());
        }            
        return errors.isEmpty() || swallowRemainingFailures;
    }
    
    protected String[] getSourceFileNames() {
        return sourceFilenames.toArray(new String[0]);
    }
    
    protected void invokePolyglot(String[] files) 
        throws polyglot.main.Main.TerminationException 
    {
        File tmpdir = new File("pthOutput");

        int i = 1;
        while (tmpdir.exists()) {
            tmpdir = new File("pthOutput." + i);
            i++;
        }

        tmpdir.mkdir();

        setDestDir(tmpdir.getPath());
                
        String[] cmdLine = buildCmdLine(files);
        polyglot.main.Main polyglotMain = new polyglot.main.Main();

        try {
            polyglotMain.start(cmdLine, eq);
        }
        finally {
            if (Main.options.deleteOutputFiles) {
                deleteDir(tmpdir);
            }

            setDestDir(null);
        }
    }

    protected void invokeJavac(String[] files) {
        File tmpdir = new File("pthOutput");

        int i = 1;
        while (tmpdir.exists()) {
            tmpdir = new File("pthOutput." + i);
            i++;
        }

        tmpdir.mkdir();

        setDestDir(tmpdir.getPath());

        String[] cmdLine = buildCmdLine(files);
        com.sun.tools.javac.Main compiler = new com.sun.tools.javac.Main();

        try {
            compiler.compile(cmdLine);
        }
        finally {
            if (Main.options.deleteOutputFiles) {
                deleteDir(tmpdir);
            }

            setDestDir(null);
        }
    }

    protected void deleteDir(File dir) {
//        System.out.println("Deleting " + dir.toString());
        File[] list = dir.listFiles();
        for (int i = 0; i < list.length; i++) {
//            System.out.println("  containing " + list[i]);
            if (list[i].isDirectory()) {
                deleteDir(list[i]);
            }
            else {
                if (!list[i].delete()) {
                    list[i].deleteOnExit();
//                    System.out.println("Failed to delete " + list[i]);
                }
                
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
//            System.out.println("Failed to delete " + dir);
        }
    }

    protected String[] buildCmdLine(String[] files) {
        ArrayList<String> args = new ArrayList<String>();
        
        String s;
        String[] sa;
        
        if ((s = getExtensionClassname()) != null && !s.equals(JAVAC)) {
            args.add("-extclass");
            args.add(s);
        }
        
        if ((s = getAdditionalClasspath()) != null) {
            args.add("-cp");
            args.add(s);                        
        }

        if ((s = getDestDir()) != null) {
            args.add("-d");
            args.add(s);                        
        }

        if ((s = getSourceDir()) != null) {
            args.add("-sourcepath");
            args.add(s);                        
        }

        char pathSep = File.pathSeparatorChar;
        
        if (mainExtraArgs == null && (s = Main.options.extraArgs) != null) {
            mainExtraArgs = new ArrayList<String>();
            sa = breakString(Main.options.extraArgs);
            for (int i = 0; i < sa.length; i++) {
                String sas = sa[i];
                if (pathSep != ':' && sas.indexOf(':') >= 0) {
                    sas = replacePathSep(sas, pathSep);
                }
                mainExtraArgs.add(sas);
            }
        }
        if (mainExtraArgs != null) {
            args.addAll(mainExtraArgs);
        }

        if ((sa = getExtraCmdLineArgs()) != null) {
            for (int i = 0; i < sa.length; i++) {
                String sas = sa[i];
                if (pathSep != ':' && sas.indexOf(':') >= 0) {
                    sas = replacePathSep(sas, pathSep);
                }
                sas = replaceEnvVariables(sas);
                args.add(sas);
            }
        }
        
        args.addAll(Arrays.asList(files));
        
        return args.toArray(new String[0]);
    }
        
    /**
     * @param sas
     * @param pathSep
     * @return
     */
    private String replacePathSep(String sas, char pathSep) {
        // replace path separater ':' with appropriate 
        // system specific one
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < sas.length(); j++) {
            if (sas.charAt(j) == '\\' && (j+1) < sas.length() && sas.charAt(j+1) == ':') {
                // escaped ':'
                j++;
                sb.append(':');
            }
            else if (sas.charAt(j) == ':') {
                sb.append(pathSep);
            }
            else {
                sb.append(sas.charAt(j));
            }
        }
        return sb.toString();
    }

    
    private String replaceEnvVariables(String sas) {
        int start;
        while ((start = sas.indexOf('$')) >= 0) {
            // we have an environment variable
            int end = start+1;
            while (end < sas.length() && 
                    (Character.isUnicodeIdentifierStart(sas.charAt(end)) ||
                    Character.isUnicodeIdentifierPart(sas.charAt(end)))) {
                end++;
            }
            // the identifier is now from start+1 to end-1 inclusive.
            
            String var = sas.substring(start+1, end);
            String v = System.getenv(var);
            if (v == null && !undefinedEnvVars.contains(var)) {
                undefinedEnvVars.add(var);
                output.warning("environment variable $" + var + " undefined.");
                v = "";
            }
            sas = sas.substring(0, start) + v + sas.substring(end); 
        }
        return sas;
    }
    protected String getExtensionClassname() {
        return extensionClassname;
    }

    protected void setExtensionClassname(String extClassname) {
        this.extensionClassname = extClassname;
    }
    
    protected String[] getExtraCmdLineArgs() {
        return this.extraArgs;
    }
    
    protected static String[] breakString(String s) {        
        ArrayList<String> l = new ArrayList<String>();
        int i = 0;
        String token = "";
        // if endChar != 0, then we are parsing a long token that may
        // include whitespace
        char endChar = 0;  
        while (i < s.length()) {
            char c = s.charAt(i);
            if (endChar != 0 && c == endChar) {
                // we have finished reading the long token.
                endChar = 0;
            }
            else if (c == '\\') {
                // a literal character
                c = s.charAt(++i);
                token += c;
            }
            else if (c =='\"' || c == '\'') {
                // a quoted string, treat as a single token
                endChar = c;           
            }
            else if (endChar == 0 && Character.isWhitespace(c)) {
                if (token.length() > 0) {
                    l.add(token);                    
                }
                token = "";
            }
            else {
                token += c;
            }
            i++;
        }
        if (token.length() > 0) {
            l.add(token);                    
        }

        return l.toArray(new String[l.size()]);
    }
    protected void setExtraCmdLineArgs(String args) {
        if (args != null) {
            this.extraArgs = breakString(args);
        } 
    }
    protected String getAdditionalClasspath() {
        return Main.options.classpath;
    }
    protected void setDestDir(String dir) {
        this.destDir = dir;
    }
    protected String getDestDir() {
        return destDir;
    }
    protected String getSourceDir() {
        return null;
    }
}
