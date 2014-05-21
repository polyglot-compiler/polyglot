/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.pth;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;

import polyglot.util.ErrorInfo;
import polyglot.util.SilentErrorQueue;

/**
 * 
 */
public class SourceFileTest extends AbstractTest {
    private static final String JAVAC = "javac";
    private static final JavaCompiler javaCompiler =
            polyglot.main.Main.javaCompiler();
    /**
     * Each compilation unit is a list of source file names.
     */
    protected final List<List<String>> compilationUnits;
    protected String extensionClassname = null;
    protected String[] extraArgs;
    protected List<String> mainExtraArgs;
    protected final SilentErrorQueue eq;
    protected String testDir;
    protected String destDir;

    protected List<ExpectedFailure> expectedFailures;

    protected Set<String> undefinedEnvVars = new HashSet<>();

    public SourceFileTest(List<List<String>> compilationUnits) {
        super(testName(compilationUnits));
        this.compilationUnits = compilationUnits;
        eq = new SilentErrorQueue(100, getName());
    }

    private static String testName(List<List<String>> compilationUnits) {
        if (compilationUnits.size() == 1) {
            List<String> filenames = compilationUnits.get(0);
            if (filenames.size() == 1)
                return new File(filenames.get(0)).getName();
            return filenames.toString();
        }
        return compilationUnits.toString();
    }

    @Override
    public String getUniqueId() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        if (extensionClassname != null) {
            sb.append("::");
            sb.append(extensionClassname);
        }
        if (extraArgs != null) {
            for (String extraArg : extraArgs) {
                sb.append("::");
                sb.append(extraArg);
            }
        }
        return sb.toString();
    }

    public void setExpectedFailures(List<ExpectedFailure> expectedFailures) {
        this.expectedFailures = expectedFailures;
    }

    @Override
    protected boolean runTest() {
        List<List<String>> sourceFileNames = getSourceFileNames();

        // First, check that all source files exist.
        for (List<String> compilationUnit : sourceFileNames) {
            for (String filename : compilationUnit) {
                File sourceFile = new File(filename);
                if (!sourceFile.exists()) {
                    setFailureMessage("File not found.");
                    return false;
                }
            }
        }

        List<String> cmdLineHdr = buildCmdLine();

        // Figure out the output directory.
        File destDir;
        boolean addDestDirToCmdLine = false;
        {
            String s = getDestDir();
            if (s != null)
                destDir = new File(s);
            else {
                destDir = new File("pthOutput");

                for (int i = 1; destDir.exists(); i++)
                    destDir = new File("pthOutput." + i);

                destDir.mkdir();
                addDestDirToCmdLine = true;
            }
        }

        try {
            // Next, loop through each compilation unit and compile it.
            boolean addClassPath = false;
            for (List<String> list : sourceFileNames) {
                List<String> cmdLine = new LinkedList<>(cmdLineHdr);
                cmdLine.addAll(list);

                if (addDestDirToCmdLine) {
                    cmdLine.add("-d");
                    cmdLine.add(prependTestPath(destDir.getName()));
                }

                // To get separate compilation, add the output directory to the
                // class path only if we are compiling subsequent sets of files.
                if (addClassPath) {
                    cmdLine.add("-cp");
                    cmdLine.add(prependTestPath(destDir.getName()));
                }
                else addClassPath = true;

                // Invoke the compiler on the compilation unit.
                try {
                    if (JAVAC.equals(getExtensionClassname())) {
                        // invoke javac on the program
                        invokeJavac(cmdLine);
                    }
                    else {
                        invokePolyglot(cmdLine);
                    }
                }
                catch (polyglot.main.Main.TerminationException e) {
                    if (e.getMessage() != null) {
                        setFailureMessage(e.getMessage());
                        return false;
                    }
                    else {
                        if (!eq.hasErrors()) {
                            setFailureMessage("Failed to compile for unknown reasons: "
                                    + e.toString());
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
            }
        }
        finally {
            if (Main.options.deleteOutputFiles) {
                deleteDir(destDir);
            }
        }

        // invoke the compiler on the file.
        return checkErrorQueue(eq);
    }

    @Override
    protected void postRun() {
        output.finishTest(this, eq);
    }

    protected boolean checkErrorQueue(SilentErrorQueue eq) {
        List<ErrorInfo> errors = new ArrayList<>(eq.getErrors());

        boolean swallowRemainingFailures = false;
        for (ExpectedFailure f : expectedFailures) {
            if (f instanceof AnyExpectedFailure) {
                swallowRemainingFailures = true;
                continue;
            }

            boolean found = false;
            for (Iterator<ErrorInfo> j = errors.iterator(); j.hasNext();) {
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
            for (Iterator<ErrorInfo> iter = errors.iterator(); iter.hasNext();) {
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

    protected List<List<String>> getSourceFileNames() {
        List<List<String>> result = new ArrayList<>(compilationUnits.size());
        for (List<String> compilationUnit : compilationUnits) {
            List<String> sourceFileNames =
                    new ArrayList<>(compilationUnit.size());
            for (String sourceFile : compilationUnit) {
                sourceFileNames.add(prependTestPath(sourceFile));
            }
            result.add(sourceFileNames);
        }
        return result;
    }

    protected void invokePolyglot(List<String> cmdLine)
            throws polyglot.main.Main.TerminationException {
        polyglot.main.Main polyglotMain = new polyglot.main.Main();
        polyglotMain.start(cmdLine.toArray(new String[cmdLine.size()]), eq);
    }

    protected static void invokeJavac(List<String> cmdLine) {
        javaCompiler.run(null,
                         null,
                         null,
                         cmdLine.toArray(new String[cmdLine.size()]));
    }

    protected static void deleteDir(File dir) {
//        System.out.println("Deleting " + dir.toString());
        for (File f : dir.listFiles()) {
//          System.out.println("  containing " + f);
            if (f.isDirectory()) {
                deleteDir(f);
            }
            else {
                if (!f.delete()) {
                    f.deleteOnExit();
//                    System.out.println("Failed to delete " + f);
                }
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
//            System.out.println("Failed to delete " + dir);
        }
    }

    protected List<String> buildCmdLine() {
        List<String> args = new LinkedList<>();

        String s;
        String[] sa;

        if ((s = getExtensionClassname()) != null && !s.equals(JAVAC)) {
            args.add("-extclass");
            args.add(s);
        }

        if ((s = getAdditionalClasspath()) != null) {
            args.add("-cp");
            args.add(prependTestPath(s));
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
            mainExtraArgs = new LinkedList<>();
            sa = breakString(Main.options.extraArgs);
            for (String element : sa) {
                String sas = element;
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
            List<String> appendFlags =
                    Arrays.asList(new String[] { "-d", "-cp", "-classpath",
                            "-sourcepath" });
            boolean appendFlag = false;
            boolean setDestDir = false;
            for (String element : sa) {
                String sas = element;
                if (appendFlag) {
                    sas = prependTestPath(sas);
                    appendFlag = false;
                }
                else if (appendFlags.contains(element)) appendFlag = true;
                if (setDestDir) {
                    setDestDir(sas);
                    setDestDir = false;
                }
                else if (element.equals("-d")) setDestDir = true;
                if (pathSep != ':' && sas.indexOf(':') >= 0) {
                    sas = replacePathSep(sas, pathSep);
                }
                sas = replaceEnvVariables(sas);
                args.add(sas);
            }
        }

        return args;
    }

    /**
     * @param sas
     * @param pathSep
     * @return
     */
    private static String replacePathSep(String sas, char pathSep) {
        // replace path separater ':' with appropriate 
        // system specific one
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < sas.length(); j++) {
            if (sas.charAt(j) == '\\' && j + 1 < sas.length()
                    && sas.charAt(j + 1) == ':') {
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
            int end = start + 1;
            while (end < sas.length()
                    && (Character.isUnicodeIdentifierStart(sas.charAt(end)) || Character.isUnicodeIdentifierPart(sas.charAt(end)))) {
                end++;
            }
            // the identifier is now from start+1 to end-1 inclusive.

            String var = sas.substring(start + 1, end);
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
        extensionClassname = extClassname;
    }

    protected String[] getExtraCmdLineArgs() {
        return extraArgs;
    }

    protected static String[] breakString(String s) {
        List<String> l = new LinkedList<>();
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
            else if (c == '\"' || c == '\'') {
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
            extraArgs = breakString(args);
        }
    }

    protected String getAdditionalClasspath() {
        return Main.options.classpath;
    }

    protected void setDestDir(String dir) {
        destDir = dir;
    }

    protected String getDestDir() {
        return prependTestPath(destDir);
    }

    protected String getSourceDir() {
        return getTestDir();
    }

    protected void setTestDir(String dir) {
        testDir =
                Main.options.testpath == null
                        ? dir == null ? null : dir : dir == null
                                ? Main.options.testpath : Main.options.testpath
                                        + dir;
    }

    protected String getTestDir() {
        return testDir;
    }

    protected String prependTestPath(String filename) {
        String testpath = getTestDir();
        if (testpath != null) {
            if (filename != null) return testpath + filename;
            return testpath;
        }
        return filename;
    }
}
