package polyglot.pth;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class SourceFileTestCollection
        extends TestSuite<SourceFileTest> {
    protected String testCommand = null;
    protected String[] extraArgs;

    protected List<String> mainExtraArgs;
    protected String uniqueId;
    protected List<String> cmdLineHdr;
    protected TestDriver td;

    protected Map<String, String> pathFlags;
    protected Map<String, String> flagMap;

    protected Set<String> undefinedEnvVars = new HashSet<>();

    public SourceFileTestCollection(String testCommand, String name,
            String testDir, String args, List<SourceFileTest> tests) {
        super(testCommand + (name == null ? "" : " (" + name + ")"), tests);
        this.testCommand = testCommand;

        if (args != null) extraArgs = breakString(args);

        char pathSep = File.pathSeparatorChar;
        if (Main.options.extraArgs != null) {
            mainExtraArgs = new LinkedList<>();
            for (String element : breakString(Main.options.extraArgs)) {
                String sas = element;
                if (pathSep != ':' && sas.indexOf(':') >= 0)
                    sas = replacePathSep(sas, pathSep);
                mainExtraArgs.add(sas);
            }
        }

        {
            StringBuffer sb = new StringBuffer();
            sb.append(getName());
            sb.append("::");
            sb.append(testCommand);
            if (extraArgs != null) for (String extraArg : extraArgs) {
                sb.append("::");
                sb.append(extraArg);
            }
            uniqueId = sb.toString();
        }

        pathFlags = new HashMap<>();
        populatePathFlags();

        flagMap = new HashMap<>();
        flagMap.put("compilerpath", Main.options.compilerpath);
        flagMap.put("refpath", Main.options.refpath);
        flagMap.put("testpath",
                    Main.options.testpath == null
                            ? testDir == null ? null : testDir
                            : testDir == null
                                    ? Main.options.testpath
                                    : Main.options.testpath + testDir);
        flagMap.put("workpath", Main.options.workpath);

        cmdLineHdr = buildCmdLine();

        // Create driver to run tests.
        td = createTestDriver();
    }

    protected void populatePathFlags() {
    }

    protected abstract TestDriver createTestDriver();

    protected static String[] breakString(String s) {
        List<String> l = new LinkedList<>();
        int i = 0;
        String token = "";
        // if endChar != 0, then we are parsing a long token that may
        // include whitespace
        char endChar = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (endChar != 0 && c == endChar)
                // we have finished reading the long token.
                endChar = 0;
            else if (c == '\\') {
                // a literal character
                c = s.charAt(++i);
                token += c;
            }
            else if (c == '\"' || c == '\'')
                // a quoted string, treat as a single token
                endChar = c;
            else if (endChar == 0 && Character.isWhitespace(c)) {
                if (token.length() > 0) l.add(token);
                token = "";
            }
            else token += c;
            i++;
        }
        if (token.length() > 0) l.add(token);

        return l.toArray(new String[l.size()]);
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public String getPathFromFlagMap(String... keys) {
        StringBuffer sb = new StringBuffer();
        for (String key : keys) {
            String path = flagMap.get(key);
            if (path == null)
                continue;
            else if (!path.isEmpty() && !path.endsWith(File.separator))
                path += File.separator;
            sb.append(path);
        }
        if (sb.length() == 0) return "." + File.separator;
        return sb.toString();
    }

    @Override
    public boolean haltOnFailure() {
        return td.haltOnFailure();
    }

    @Override
    protected boolean matchFilter() {
        if (Main.options.testCollectionFilters.isEmpty()) return true;
        for (String testCollectionFilter : Main.options.testCollectionFilters)
            if (Pattern.matches(testCollectionFilter, getName())) return true;
        return false;
    }

    @Override
    protected boolean runTest() {
        if (!td.preTest(this)) return false;
        boolean okay = true;

        // Create work directory if not exists.
        boolean workDirCreated = false;
        String workDirname = td.getPathFromFlagMap("workpath");
        File workDir = new File(workDirname);
        if (!workDir.exists()) {
            workDir.mkdirs();
            workDirCreated = true;
        }

        if (getTestSuiteResult() == null) setTestResult(createTestResult(null));

        Map<String, TestResult> testResults = getTestSuiteResult().testResults;
        Map<String, TestResult> oldTestResults = new HashMap<>(testResults);
        Map<String, TestResult> newResults = new HashMap<>();

        boolean shouldExecute = true;
        for (SourceFileTest t : tests) {
            t.setTestDriver(td);
            TestResult tr = oldTestResults.get(t.getUniqueId());
            if (shouldExecute && t.shouldExecute(tr)) {
                if (tr != null) t.setTestResult(tr);
                t.setCommandLineHeader(cmdLineHdr);

                boolean result = t.run();
                okay = okay && result;

                tr = t.getTestResult();

                executedTests += t.getExecutedTestCount();
                successfulTests += t.getSuccessfulTestCount();
                postIndividualTest();
                if (!result && (t.haltOnFailure() || haltOnFirstFailure))
                    shouldExecute = false;
            }
            newResults.put(t.getUniqueId(), tr);
        }
        testResults.clear();
        testResults.putAll(newResults);

        // Remove work directory if it was created.
        if (workDirCreated) {
            if (!workDir.delete()) {
                appendFailureMessage("Cannot delete directory " + workDirname);
                return false;
            }
        }

        return okay;
    }

    public String getSummary() {
        StringBuffer sb = new StringBuffer(getName());
        sb.append(": ");
        sb.append(getSuccessfulTestCount());
        sb.append(" out of ");
        sb.append(getExecutedTestCount());
        sb.append(" tests succeeded.");
        td.getSummary(sb);
        return sb.toString();
    }

    protected List<String> buildCmdLine() {
        List<String> args = new LinkedList<>();

        if (mainExtraArgs != null) args.addAll(mainExtraArgs);

        if (extraArgs != null) {
            String flagName = null;
            for (String element : extraArgs) {
                if (pathFlags.containsKey(element))
                    flagName = pathFlags.get(element);
                else if (flagName != null) {
                    char pathSep = File.pathSeparatorChar;
                    if (pathSep != ':' && element.indexOf(':') >= 0)
                        element = replacePathSep(element, pathSep);
                    element = replaceEnvVariables(element);
                    flagMap.put(flagName, element);
                    flagName = null;
                }
                args.add(element);
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
        // replace path separator ':' with appropriate
        // system specific one
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < sas.length(); j++)
            if (sas.charAt(j) == '\\' && j + 1 < sas.length()
                    && sas.charAt(j + 1) == ':') {
                // escaped ':'
                j++;
                sb.append(':');
            }
            else if (sas.charAt(j) == ':')
                sb.append(pathSep);
            else sb.append(sas.charAt(j));
        return sb.toString();
    }

    private String replaceEnvVariables(String sas) {
        int start;
        while ((start = sas.indexOf('$')) >= 0) {
            // we have an environment variable
            int end = start + 1;
            while (end < sas.length() && (Character
                                                   .isUnicodeIdentifierStart(sas.charAt(end))
                    || Character.isUnicodeIdentifierPart(sas.charAt(end))))
                end++;

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

    protected String getAdditionalClasspath() {
        return Main.options.classpath;
    }
}
