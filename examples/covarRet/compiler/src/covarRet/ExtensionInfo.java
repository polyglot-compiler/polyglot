package polyglot.ext.covarRet;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;

import java.util.*;
import java.io.*;

public class ExtensionInfo extends polyglot.ext.jl.ExtensionInfo {
    public String defaultFileExtension() {
	return "jl";
    }

    protected TypeSystem createTypeSystem() {
	return new CovarRetTypeSystem();
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
