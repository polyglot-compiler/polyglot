/*
 * Author : Stephen Chong
 * Created: Nov 21, 2003
 */
package polyglot.pth;

import java.io.File;
import java.util.*;

import polyglot.main.Main;
import polyglot.util.ErrorInfo;
import polyglot.util.SilentErrorQueue;

/**
 * 
 */
public class SourceFileTest extends AbstractTest {
    protected final List sourceFilenames;
    protected String extensionClassname = null;
    protected String[] extraArgs;
    protected final SilentErrorQueue eq;
    
    protected Set expectedFailures;
        
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
    
    public void setExpectedFailures(Set expectedFailures) {
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
        return checkErrorQueue(eq);
    }
    
    protected boolean checkErrorQueue(SilentErrorQueue eq) {
        List errors = new ArrayList(eq.getErrors());
        
        for (Iterator i = expectedFailures.iterator(); i.hasNext(); ) {
            ExpectedFailure f = (ExpectedFailure)i.next();
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
        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (Iterator iter = errors.iterator(); iter.hasNext(); ) {
                ErrorInfo err = (ErrorInfo)iter.next();                        
                sb.append(err.getMessage());
                sb.append(" (");
                sb.append(err.getPosition());
                sb.append(")");
                if (iter.hasNext()) sb.append("; ");
            }
            setFailureMessage(sb.toString());
        }            
        return errors.isEmpty();
    }
    
    protected String[] getSourceFileNames() {
        return (String[])sourceFilenames.toArray(new String[0]);
    }
    
    protected void invokePolyglot(String[] files) 
        throws polyglot.main.Main.TerminationException 
    {
        String[] cmdLine = buildCmdLine(files);
        Main polyglotMain = new polyglot.main.Main();
        polyglotMain.start(cmdLine, eq);
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

        if ((sa = getExtraCmdLineArgs()) != null) {
            for (int i = 0; i < sa.length; i++) {
                args.add(sa[i]);
            }
        }
        
        args.addAll(Arrays.asList(files));
        
        return (String[])args.toArray(new String[0]);
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
    protected void setExtraCmdLineArgs(String args) {
        StringTokenizer st = new StringTokenizer(args);
        ArrayList l = new ArrayList(st.countTokens());
        while (st.hasMoreTokens()) {
            l.add(st.nextToken());
        }
        
        this.extraArgs = (String[])l.toArray(new String[0]); 
    }
    protected String getAdditionalClasspath() {
        return null;
    }
    protected String getDestDir() {
        return null;
    }
    protected String getSourceDir() {
        return null;
    }

}
