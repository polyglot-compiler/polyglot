package jltools.types;

import java.util.*;

public class Types
{
    static Collection topics = new ArrayList();

    static {
	topics.add("types");
    }

    public static void report(int level, String msg) {
        jltools.main.Report.report(topics, level, msg);
    }
}
