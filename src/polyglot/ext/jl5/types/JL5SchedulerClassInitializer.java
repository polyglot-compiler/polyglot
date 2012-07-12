package polyglot.ext.jl5.types;

import polyglot.ext.jl5.types.reflect.JL5LazyClassInitializer;
import polyglot.frontend.MissingDependencyException;
import polyglot.types.SchedulerClassInitializer;
import polyglot.types.TypeSystem;

public class JL5SchedulerClassInitializer extends SchedulerClassInitializer
        implements JL5LazyClassInitializer {

    public JL5SchedulerClassInitializer(TypeSystem ts) {
        super(ts);
    }

    protected boolean annotationsInitialized;

    @Override
    public void initAnnotationElems() {
        if (!annotationsInitialized) {
            if (ct.membersAdded()) {
                this.annotationsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }

}
