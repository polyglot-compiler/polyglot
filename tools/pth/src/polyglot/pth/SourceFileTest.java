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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public abstract class SourceFileTest extends AbstractTest {
    /**
     * Each compilation unit is a list of source file names.
     */
    protected final List<List<String>> compilationUnits;

    protected List<ExpectedFailure> expectedFailures;

    protected TestDriver td;
    protected List<String> commandLineHeader;

    protected StringBuffer sbout;
    protected StringBuffer sberr;

    protected long elapsed;

    public SourceFileTest(List<List<String>> compilationUnits,
            List<ExpectedFailure> expectedFailures) {
        super(testName(compilationUnits));
        this.compilationUnits = compilationUnits;
        this.expectedFailures = expectedFailures;
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
        return getName();
    }

    public List<ExpectedFailure> getExpectedFailures() {
        return expectedFailures;
    }

    @Override
    protected boolean runTest() {
        List<List<String>> sourceFileNames = getSourceFileNames();

        String testpath = td.getPathFromFlagMap("testpath");
        String sourcepath = td.getPathFromFlagMap("workpath", "sourcepath");

        // First, check that all source files exist.
        for (List<String> compilationUnit : sourceFileNames)
            for (String filename : compilationUnit) {
                File sourceFile = new File(testpath + filename);
                if (!sourceFile.exists()) {
                    appendFailureMessage("File " + filename
                            + " does not exist in directory " + testpath);
                    return false;
                }
            }

        // Copy source files from test/path/file
        // to work/path/source/path,
        // unless test/path and work/path/source/path are the same.
        File testDir = new File(testpath);
        File sourceDir = new File(sourcepath);
        List<File> sourceFiles = new LinkedList<>();

        try {
            if (!testDir.getCanonicalPath()
                        .equals(sourceDir.getCanonicalPath())) {
                for (List<String> compilationUnit : sourceFileNames)
                    for (String filename : compilationUnit) {
                        File srcFile = new File(testpath + filename);
                        File dstFile = new File(sourcepath + filename);
                        if (dstFile.exists()) {
                            appendFailureMessage("File " + filename
                                    + " already exists in directory "
                                    + sourcepath);
                            return false;
                        }
                        Files.copy(srcFile.toPath(), dstFile.toPath());
                        sourceFiles.add(dstFile);
                    }
            }

//        // Figure out the output directory.
//        File destDir;
//        boolean addDestDirToCmdLine = false;
//        {
//            String s = getDestDir();
//            if (s != null) {
//                destDir = new File(s);
//                if (!destDir.exists()) destDir.mkdir();
//            }
//            else {
//                destDir = new File("pthOutput");
//
//                for (int i = 1; destDir.exists(); i++)
//                    destDir = new File("pthOutput." + i);
//
//                destDir.mkdir();
//                addDestDirToCmdLine = true;
//            }
//        }
            if (!td.preTest(this)) return false;

            String compilerDirname = td.getPathFromFlagMap("compilerpath");
            List<String> cmdLineHdr = getCommandLineHeader();

            // Next, loop through each compilation unit and compile it.
//            boolean addClassPath = false;
            for (List<String> list : sourceFileNames) {
                List<String> cmdLine = new LinkedList<>(cmdLineHdr);
                cmdLine.addAll(list);

                // TODO
//                if (addDestDirToCmdLine) {
//                    cmdLine.add("-d");
//                    cmdLine.add(prependTestPath(destDir.getName()));
//                }
//
//                // To get separate compilation, add the output directory to the
//                // class path only if we are compiling subsequent sets of files.
//                if (addClassPath) {
//                    cmdLine.add("-cp");
//                    cmdLine.add(prependTestPath(destDir.getName()));
//                }
//                else addClassPath = true;

                // Start timer.
                long start = System.nanoTime();
                // Invoke the compiler on the compilation unit.
                int ret = invokeCompiler(compilerDirname, cmdLineHdr, list);
                // End timer.
                long finish = System.nanoTime();
                elapsed = finish - start;
                if (ret != 0) {
                    if (ret > 0) appendFailureMessage("Failed to compile: "
                            + compilerName() + " exit code " + ret);
                    return false;
                }
            }

            // Check the compilation result.
            boolean okay = true;
            okay = postTest() && okay;
            okay = td.postTest(this) && okay;
            return okay;
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        finally {
            // Remove source files from work/path/source/path,
            // unless test/path and work/path/source/path are the same.
            if (!isSameDirectory(sourceDir, testDir)) {
                for (File dstFile : sourceFiles) {
                    if (dstFile.exists() && !dstFile.delete()) {
                        appendFailureMessage("Cannot delete file "
                                + dstFile.getName() + " in directory "
                                + sourcepath);
                        return false;
                    }
                }
            }
        }
    }

    public boolean postTest() {
        return true;
    }

    @Override
    protected void postRun() {
        super.postRun();

        File saveDir;
        if (Main.options.deleteOutputFiles)
            saveDir = null;
        else {
            // Figure out the output directory.
            String savepath = getWorkPath() + "pthOutput." + getName();
            saveDir = new File(savepath);
            for (int i = 1; saveDir.exists(); i++)
                saveDir = new File(savepath + "." + i);
            saveDir.mkdir();
        }
        td.cleanup(this, saveDir);
    }

    protected boolean isSameDirectory(File dir1, File dir2) {
        try {
            return dir1.getCanonicalPath().equals(dir2.getCanonicalPath());
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return true;
        }
    }

    public String compilerName() {
        return getName();
    }

    public List<List<String>> getSourceFileNames() {
        List<List<String>> result = new ArrayList<>(compilationUnits.size());
        for (List<String> compilationUnit : compilationUnits) {
            List<String> sourceFileNames =
                    new ArrayList<>(compilationUnit.size());
            for (String sourceFile : compilationUnit)
                sourceFileNames.add(sourceFile);
            result.add(sourceFileNames);
        }
        return result;
    }

    public abstract int invokeCompiler(String compilerDirname,
            List<String> cmdLineHdr, List<String> sourceFileNames);

    public void printTestResult(PDFReporter pr) {
        String notice = getNotice();
        if (notice != null) {
            pr.printText(pr.anonymizedName(notice));
            pr.printText("");
        }

        String result;
        if (success())
            result = "OK";
        else {
            String msg = getFailureMessage();
            if (msg == null) msg = "Failed (no message)";
            result = pr.anonymizedName(msg);
        }
        pr.printText(result);
        pr.printText("");

        pr.printHeader("Command line without filenames:");
        pr.printText(cmdLinePrefix());
        pr.printText("");

        pr.printHeader("Content of test case:");
        String testpath = td.getPathFromFlagMap("testpath");
        for (List<String> compilationUnit : getSourceFileNames())
            for (String filename : compilationUnit) {
                String filepath = testpath + filename;
                File srcFile = new File(filepath);
                pr.printCode(srcFile);
            }

        String stdout = getCompilerStdout();
        if (stdout != null) {
            pr.printHeader("Compiler's standard output:");
            pr.printText(pr.anonymizedName(stdout));
            pr.printText("");
        }
        String stderr = getCompilerStderr();
        if (stderr != null) {
            pr.printHeader("Compiler's standard error:");
            pr.printText(pr.anonymizedName(stderr));
            pr.printText("");
        }

        td.printTestResult(this, pr);
    }

    protected String cmdLinePrefix() {
        StringBuffer sb = new StringBuffer(compilerName());
        for (String token : commandLineHeader) {
            sb.append(" ");
            sb.append(token);
        }
        return sb.toString();
    }

    public TestDriver getTestDriver() {
        return td;
    }

    protected void setTestDriver(TestDriver td) {
        this.td = td;
    }

    public String getWorkPath() {
        return td.getPathFromFlagMap("workpath");
    }

    public List<String> getCommandLineHeader() {
        return commandLineHeader;
    }

    protected void setCommandLineHeader(List<String> commandLineHeader) {
        this.commandLineHeader = commandLineHeader;
    }

    protected String getAdditionalClasspath() {
        return Main.options.classpath;
    }

    public String getCompilerStdout() {
        if (sbout != null && sbout.length() > 0) return sbout.toString();
        return null;
    }

    public String getCompilerStderr() {
        if (sberr != null && sberr.length() > 0) return sberr.toString();
        return null;
    }

    public long getCompilationTime() {
        return elapsed;
    }
}
