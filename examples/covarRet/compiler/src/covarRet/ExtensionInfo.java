package jltools.ext.covarRet;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.frontend.*;

import java.util.*;
import java.io.*;

public class ExtensionInfo extends jltools.ext.jl.ExtensionInfo {
    public String defaultFileExtension() {
	return "jl";
    }

    protected NodeFactory createNodeFactory() {
	return new CovarRetNodeFactory();
    }

    public static final Pass.ID COVAR_RET_CAST_REWRITE = new Pass.ID("covariantReturnCasts");
    public List passes(Job job) {
        List l = super.passes(job);
        beforePass(l, Pass.PRE_OUTPUT_ALL,
                  new VisitorPass(COVAR_RET_CAST_REWRITE,
                                  job, new CovarRetRewriter(job, ts, nf)));
        return l;
    }
}
