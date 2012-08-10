package polyglot.main;

import static java.io.File.pathSeparator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import polyglot.util.InternalCompilerError;

/**
 * An OptFlag represents a command line option. It groups parsing and usage
 * information together so that compilers may easily compose options from
 * multiple extensions.
 * 
 * @param <T> The type of value parsed by this option.
 */
public abstract class OptFlag<T> implements Comparable<OptFlag<T>> {
    /**
     * Remove the flag matching id
     * @param id 
     *          command line flag of the OptFlag to remove
     * @param flags
     *          the list of OptFlags
     * @return
     *        true if the id was found
     */
    public static boolean removeFlag(String id, Set<OptFlag<?>> flags) {
        for (Iterator<OptFlag<?>> it = flags.iterator(); it.hasNext();) {
            OptFlag<?> flag = it.next();
            if (flag.ids.contains(id)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Lookup the flag matching id
     * @param id 
     *          command line flag of the OptFlag
     * @param flags
     *          the list of OptFlags
     * @return
     *        true if the id was found
     */
    public static OptFlag<?> lookupFlag(String id, Set<OptFlag<?>> flags) {
        for (Iterator<OptFlag<?>> it = flags.iterator(); it.hasNext();) {
            OptFlag<?> flag = it.next();
            if (flag.ids.contains(id)) {
                return flag;
            }
        }
        return null;
    }

    public static boolean hasSourceArg(List<Arg<?>> arguments) {
        for (Arg<?> arg : arguments) {
            if (arg.flag == null) return true;
        }
        return false;
    }

    public static List<Arg<?>> sourceArgs(List<Arg<?>> arguments) {
        List<Arg<?>> matches = new ArrayList<Arg<?>>();
        for (Arg<?> arg : arguments) {
            if (arg.flag == null) matches.add(arg);
        }
        return matches;
    }

    public static Arg<?> lookup(String id, List<Arg<?>> args) {
        for (Arg<?> arg : args) {
            if (arg.flag != null && arg.flag.ids.contains(id)) return arg;
        }
        return null;
    }

    public static List<Arg<?>> lookupAll(String id, List<Arg<?>> args) {
        List<Arg<?>> matches = new ArrayList<Arg<?>>();
        for (Arg<?> arg : args) {
            if (arg.flag != null && arg.flag.ids.contains(id))
                matches.add(arg);
        }
        return matches;
    }

    protected final Kind kind;
    protected final Set<String> ids;
    protected final String params;
    protected final String usage;
    protected final String defaultValue;

    @Override
    public String toString() {
        return ids.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OptFlag) {
            OptFlag<?> that = (OptFlag<?>) obj;
            return kind == that.kind && ids.equals(that.ids);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return kind.hashCode() ^ ids.hashCode();
    }

    @Override
    public int compareTo(OptFlag<T> flag) {
        for (String id1 : ids) {
            for (String id2 : flag.ids())
                return id1.compareTo(id2);
        }
        throw new InternalCompilerError("Empty ids!");
    }

    /**
     * @param id
     *            The flag ID. e.g., "--name", "-n", or "-name".
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     */
    public OptFlag(String id, String params, String usage) {
        this(id, params, usage, null);
    }

    /**
     * @param id
     *            The flag ID. e.g., "--name", "-n", or "-name".
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value for the flag parameter, to be printed out
     *            with help info.
     */
    public OptFlag(String id, String params, String usage, int defaultValue) {
        this(id, params, usage, new Integer(defaultValue).toString());
    }

    /**
     * @param id
     *            The flag ID. e.g., "--name", "-n", or "-name".
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value(s) for the flag parameter(s), to be printed
     *            out with help info.
     */
    public OptFlag(String id, String params, String usage, String defaultValue) {
        this(new String[] { id }, params, usage, defaultValue);
    }

    /**
     * @param ids
     *            Flag IDs. e.g., { "--name", "-n", "-name"}. The first one
     *            specified will be the one printed out with help info.
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     */
    public OptFlag(String[] ids, String params, String usage) {
        this(ids, params, usage, null);
    }

    /**
     * @param ids
     *            Flag IDs. e.g., { "--name", "-n", "-name"}. The first one
     *            specified will be the one printed out with help info.
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value for the flag parameter, to be printed out
     *            with help info.
     */
    public OptFlag(String[] ids, String params, String usage, int defaultValue) {
        this(ids, params, usage, new Integer(defaultValue).toString());
    }

    /**
     * @param ids
     *            Flag IDs. e.g., { "--name", "-n", "-name"}. The first one
     *            specified will be the one printed out with help info.
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value(s) for the flag parameter(s), to be printed
     *            out with help info.
     */
    public OptFlag(String[] ids, String params, String usage,
            String defaultValue) {
        this(Kind.MAIN, ids, params, usage, defaultValue);
    }

    /**
     * @param id
     *            The flag ID. e.g., "--name", "-n", or "-name".
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     */
    public OptFlag(Kind kind, String id, String params, String usage) {
        this(kind, id, params, usage, null);
    }

    /**
     * @param id
     *            The flag ID. e.g., "--name", "-n", or "-name".
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value for the flag parameter, to be printed out
     *            with help info.
     */
    public OptFlag(Kind kind, String id, String params, String usage,
            int defaultValue) {
        this(kind, id, params, usage, new Integer(defaultValue).toString());
    }

    /**
     * @param id
     *            The flag ID. e.g., "--name", "-n", or "-name".
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value(s) for the flag parameter(s), to be printed
     *            out with help info.
     */
    public OptFlag(Kind kind, String id, String params, String usage,
            String defaultValue) {
        this(kind, new String[] { id }, params, usage, defaultValue);
    }

    /**
     * @param ids
     *            Flag IDs. e.g., { "--name", "-n", "-name"}. The first one
     *            specified will be the one printed out with help info.
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     */
    public OptFlag(Kind kind, String[] ids, String params, String usage) {
        this(kind, ids, params, usage, null);
    }

    /**
     * @param ids
     *            OptFlag IDs. e.g., { "--name", "-n", "-name"}. The first one
     *            specified will be the one printed out with help info.
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value for the flag parameter, to be printed out
     *            with help info.
     */
    public OptFlag(Kind kind, String[] ids, String params, String usage,
            int defaultValue) {
        this(kind, ids, params, usage, new Integer(defaultValue).toString());
    }

    /**
     * @param ids
     *            OptFlag IDs. e.g., { "--name", "-n", "-name"}. The first one
     *            specified will be the one printed out with help info.
     * @param params
     *            A string representing parameters for the flag, to be printed
     *            out with help info. e.g., "&lt;hostname&gt;"
     * @param usage
     *            Usage information, to be printed out with help info. e.g.,
     *            "The name of the node."
     * @param defaultValue
     *            The default value(s) for the flag parameter(s), to be printed
     *            out with help info.
     */
    public OptFlag(Kind kind, String[] ids, String params, String usage,
            String defaultValue) {
        this.kind = kind;

        this.ids = new LinkedHashSet<String>(ids.length);
        for (String flag : ids)
            this.ids.add(flag);

        this.params = params;
        this.defaultValue = defaultValue;
        if (defaultValue != null) usage += " (default: " + defaultValue + ")";
        this.usage = usage;
    }

    public static enum Kind {
        MAIN, DEBUG, VERSION, HELP, SECRET, SECRET_HELP
    }

    public Kind kind() {
        return kind;
    }

    public Set<String> ids() {
        return ids;
    }

    /**
     * Handles a usage flag.
     * 
     * @param args
     *            Arguments from the command line.
     * @param index
     *            The index of the argument following the usage flag.
     * @return The next index to be processed. e.g., if calling this method
     *         processes two arguments, then the return value should be index+2.
     * @throws UsageError
     *             If an error occurs while handling the usage flag.
     */
    public abstract Arg<T> handle(String[] args, int index) throws UsageError;

    public Arg<T> defaultArg(List<Arg<?>> arguments) {
        return defaultArg();
    }

    public Arg<T> defaultArg() {
        if (defaultValue != null)
            throw new UnsupportedOperationException("Usage for "
                    + ids()
                    + " specifies a default value, but flag does not implement one.");
        return null;
    }

    public Arg<T> createArg(int next, T value) {
        return new Arg<T>(this, next, value);
    }

    public Arg<T> createDefault(T value) {
        return new Arg<T>(this, -1, value);
    }

    /**
     * An argument parsed from the command line.
     *
     * @param <T> The type of the argument's value
     */
    public static class Arg<T> {
        protected final OptFlag<T> flag;
        protected final T value;
        protected final int next;

        /**
         * Allocates a argument without a flag.
         * @param next
         *      the index of the next argument in the command line
         * @param value
         *      the value representing this argument
         */
        public Arg(int next, T value) {
            this(null, next, value);
        }

        /**
         * Allocates a new argument
         * @param flag
         *      the flag this argument was created by 
         * @param next
         *      the index of the next argument in the command line
         * @param value
         *      the value representing this argument
         */
        public Arg(OptFlag<T> flag, int next, T value) {
            this.flag = flag;
            this.next = next;
            this.value = value;
        }

        public OptFlag<T> flag() {
            return flag;
        }

        public int next() {
            return next;
        }

        public T value() {
            return value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (flag == null)
                sb.append(value());
            else {
                sb.append(flag.ids().toString());
                if (flag.params != null) {
                    sb.append(" ");
                    sb.append(value());
                }
                else {
                    if (!(Boolean) value()) {
                        return "";
                    }
                }
            }
            return sb.toString();
        }
    }

    /**
     * The maximum width of a line when printing usage information.
     */
    private static final int USAGE_SCREEN_WIDTH = 76;

    /**
     * The number of spaces from the left that the description for flags will be
     * displayed.
     */
    private static final int USAGE_FLAG_WIDTH = 27;

    /**
     * Outputs this flag and a description of its usage in a nice format.
     * 
     * @param out
     *            output PrintStream
     */
    public void printUsage(PrintStream out) {
        String flagID = ids.iterator().next();
        if (params != null && !params.equals("")) flagID += " " + params;

        out.print("  ");
        out.print(flagID);

        // cur is where the cursor is on the screen.
        int cur = flagID.length() + 2;

        if (cur < USAGE_FLAG_WIDTH) {
            printSpaces(out, USAGE_FLAG_WIDTH - cur);
        }
        else {
            // The flag is long. Get a new line before printing the description.
            out.println();
            printSpaces(out, USAGE_FLAG_WIDTH);
        }

        cur = USAGE_FLAG_WIDTH;

        // Break up the description.
        StringTokenizer st = new StringTokenizer(usage);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (cur + s.length() > USAGE_SCREEN_WIDTH) {
                out.println();
                printSpaces(out, USAGE_FLAG_WIDTH);
                cur = USAGE_FLAG_WIDTH;
            }

            out.print(s);
            cur += s.length();
            if (st.hasMoreTokens()) {
                if (cur + 1 > USAGE_SCREEN_WIDTH) {
                    out.println();
                    printSpaces(out, USAGE_FLAG_WIDTH);
                    cur = USAGE_FLAG_WIDTH;
                }
                else {
                    out.print(" ");
                    cur++;
                }
            }
        }

        out.println();
    }

    private void printSpaces(PrintStream out, int n) {
        while (n-- > 0)
            out.print(' ');
    }

    /**
     * A OptFlag that accepts a string param with entries separated by the path
     * separator.
     * 
     * @param <T>
     */
    public static abstract class PathFlag<T> extends OptFlag<List<T>> {
        /**
         * Handles an individual entry of a path string
         * @param entry
         *      an entry of the path string
         * @return 
         *      the value corresponding to the path entry or <code>null</code> to skip this entry.
         */
        public abstract T handlePathEntry(String entry);

        /**
         * Invokes handlePathEntry on each entry of the path string and creates
         * an Arg with the list of the non-null results.
         */
        @Override
        public Arg<List<T>> handle(String[] args, int index) {
            List<T> path = new ArrayList<T>();
            StringTokenizer st =
                    new StringTokenizer(args[index], pathSeparator);
            while (st.hasMoreTokens()) {
                T next = handlePathEntry(st.nextToken());
                if (next != null) path.add(next);
            }
            return createArg(index + 1, path);
        }

        public PathFlag(Kind kind, String id, String params, String usage,
                int defaultValue) {
            super(kind, id, params, usage, defaultValue);
        }

        public PathFlag(Kind kind, String id, String params, String usage,
                String defaultValue) {
            super(kind, id, params, usage, defaultValue);
        }

        public PathFlag(Kind kind, String id, String params, String usage) {
            super(kind, id, params, usage);
        }

        public PathFlag(Kind kind, String[] ids, String params, String usage,
                int defaultValue) {
            super(kind, ids, params, usage, defaultValue);
        }

        public PathFlag(Kind kind, String[] ids, String params, String usage,
                String defaultValue) {
            super(kind, ids, params, usage, defaultValue);
        }

        public PathFlag(Kind kind, String[] ids, String params, String usage) {
            super(kind, ids, params, usage);
        }

        public PathFlag(String id, String params, String usage, int defaultValue) {
            super(id, params, usage, defaultValue);
        }

        public PathFlag(String id, String params, String usage,
                String defaultValue) {
            super(id, params, usage, defaultValue);
        }

        public PathFlag(String id, String params, String usage) {
            super(id, params, usage);
        }

        public PathFlag(String[] ids, String params, String usage,
                int defaultValue) {
            super(ids, params, usage, defaultValue);
        }

        public PathFlag(String[] ids, String params, String usage,
                String defaultValue) {
            super(ids, params, usage, defaultValue);
        }

        public PathFlag(String[] ids, String params, String usage) {
            super(ids, params, usage);
        }
    }

    /**
     * A Flag that acts as a boolean switch.
     * 
     * @param <T>
     */
    public static class Switch extends OptFlag<Boolean> {
        protected final boolean on;

        @Override
        public Arg<Boolean> handle(String[] args, int index) {
            return createArg(index, on);
        }

        @Override
        public Arg<Boolean> defaultArg() {
            return createDefault(!on);
        }

        public Switch(Kind kind, String id, String usage, boolean on) {
            super(kind, id, null, usage);
            this.on = on;
        }

        public Switch(Kind kind, String id, String usage) {
            this(kind, id, usage, true);
        }

        public Switch(Kind kind, String[] ids, String usage, boolean on) {
            super(kind, ids, null, usage);
            this.on = on;
        }

        public Switch(Kind kind, String[] ids, String usage) {
            this(kind, ids, usage, true);
        }

        public Switch(String id, String usage, boolean on) {
            super(id, null, usage);
            this.on = on;
        }

        public Switch(String id, String usage) {
            this(id, usage, true);
        }

        public Switch(String[] ids, String usage, boolean on) {
            super(ids, null, usage);
            this.on = on;
        }

        public Switch(String[] ids, String usage) {
            this(ids, usage, true);
        }
    }

    /**
     * A Flag that accepts a integer param.
     * 
     * @param <T>
     */
    public static class IntFlag extends OptFlag<Integer> {
        @Override
        public Arg<Integer> handle(String[] args, int index) {
            return createArg(index + 1, Integer.parseInt(args[index]));
        }

        @Override
        public Arg<Integer> defaultArg() {
            if (defaultValue == null) return null;
            return createDefault(new Integer(defaultValue));
        }

        public IntFlag(OptFlag.Kind kind, String id, String params,
                String usage, int defaultValue) {
            super(kind, id, params, usage, defaultValue);
        }

        public IntFlag(OptFlag.Kind kind, String id, String params, String usage) {
            super(kind, id, params, usage);
        }

        public IntFlag(OptFlag.Kind kind, String[] ids, String params,
                String usage, int defaultValue) {
            super(kind, ids, params, usage, defaultValue);
        }

        public IntFlag(OptFlag.Kind kind, String[] ids, String params,
                String usage) {
            super(kind, ids, params, usage);
        }

        public IntFlag(String id, String params, String usage, int defaultValue) {
            super(id, params, usage, defaultValue);
        }

        public IntFlag(String id, String params, String usage) {
            super(id, params, usage);
        }

        public IntFlag(String[] ids, String params, String usage,
                int defaultValue) {
            super(ids, params, usage, defaultValue);
        }

        public IntFlag(String[] ids, String params, String usage) {
            super(ids, params, usage);
        }

    }
}
