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
    protected final List sourceFilenames;
    protected String extensionClassname = null;
    protected String[] extraArgs;
    protected List mainExtraArgs;
    protected final SilentErrorQueue eq;
    protected String destDir;
    
    protected List expectedFailures;
    
    protected Set undefinedEnvVars = new HashSet();
        
    public SourceFileTest(String filename) {
        super(new File(filename).getName());
        this.sourceFilenames = Collections.singletonList(filename);
        this.eq = new SilentErrorQueue(100, this.getName()); 

    }

    public SourceFileTest(List filenames) {
        super(filenames.toString());
        this.sourceFilenames = filenames;
        this.eq = new SilentErrorQueue(100, this.getName()); 
    }

    public SourceFileTest(String[] filenames) {
        this(Arrays.asList(filenames).toString());
    }
    
    public void setExpectedFailures(List expectedFailures) {
        this.expectedFailures = expectedFailures;
    }

    protected boolean runTest() {
        for (Iterator i = sourceFilenames.iterator(); i.hasNext(); ) {
            File sourceFile = new File((String)i.next());
            if (!sourceFile.exists()) {
                setFailureMessage("File not found.");
                return false;
            }
        }
        
        // invoke the compiler on the file.
        try {
            invokePolyglot(getSourceFileNames());
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
        List errors = new ArrayList(eq.getErrors());
        
        boolean swallowRemainingFailures = false;
        for (Iterator i = expectedFailures.iterator(); i.hasNext(); ) {
            ExpectedFailure f = (ExpectedFailure)i.next();
            if (f instanceof AnyExpectedFailure) {
                swallowRemainingFailures = true;
                continue;
            }
            
            boolean found = false;
            for (Iterator j = errors.iterator(); j.hasNext(); ) {
                ErrorInfo e =(ErrorInfo)j.next();
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
            for (Iterator iter = errors.iterator(); iter.hasNext(); ) {
                ErrorInfo err = (ErrorInfo)iter.next();                        
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
        return (String[])sourceFilenames.toArray(new String[0]);
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
        ArrayList args = new ArrayList();
        
        String s;
        String[] sa;
        
        if ((s = getExtensionClassname()) != null) {
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
            mainExtraArgs = new ArrayList();
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
        
        return (String[])args.toArray(new String[0]);
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
        ArrayList l = new ArrayList();
        int i = 0;
        String token = "";
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                c = s.charAt(++i);
                token += c;
            }
            else if (Character.isWhitespace(c)) {
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
        return (String[])l.toArray(new String[l.size()]);
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
