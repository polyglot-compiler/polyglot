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

    protected boolean annotationElemsInitialized;
    protected boolean annotationInitialized;
    protected boolean enumConstantsInitialized;

    @Override
    public void initAnnotationElems() {
        if (!annotationElemsInitialized) {
            if (ct.membersAdded()) {
                this.annotationElemsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }

    @Override
    public void initAnnotations() {
        if (!annotationInitialized) {
            if (ct.signaturesResolved()) {
                this.annotationInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.SignaturesResolved(ct));
            }
        }
    }

    @Override
    public void initEnumConstants() {
        if (!enumConstantsInitialized) {
            if (ct.membersAdded()) {
                this.enumConstantsInitialized = true;
            }
            else {
                throw new MissingDependencyException(scheduler.MembersAdded(ct));
            }
        }
    }
}
