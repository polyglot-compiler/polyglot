package polyglot.translate.ext;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public class ToExtFactory_c extends AbstractExtFactory_c {

    public ToExtFactory_c() {
        super();
    }

    public ToExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extIdImpl() {
        return new IdToExt_c();
    }

    @Override
    protected Ext extArrayAccessImpl() {
        return new ArrayAccessToExt_c();
    }

    @Override
    protected Ext extArrayInitImpl() {
        return new ArrayInitToExt_c();
    }

    @Override
    protected Ext extLocalAssignImpl() {
        return new LocalAssignToExt_c();
    }

    @Override
    protected Ext extFieldAssignImpl() {
        return new FieldAssignToExt_c();
    }

    @Override
    protected Ext extArrayAccessAssignImpl() {
        return new ArrayAccessAssignToExt_c();
    }

    @Override
    protected Ext extBinaryImpl() {
        return new BinaryToExt_c();
    }

    @Override
    protected Ext extBlockImpl() {
        return new BlockToExt_c();
    }

    @Override
    protected Ext extBranchImpl() {
        return new BranchToExt_c();
    }

    @Override
    protected Ext extCallImpl() {
        return new CallToExt_c();
    }

    @Override
    protected Ext extCanonicalTypeNodeImpl() {
        return new CanonicalTypeNodeToExt_c();
    }

    @Override
    protected Ext extCaseImpl() {
        return new CaseToExt_c();
    }

    @Override
    protected Ext extCastImpl() {
        return new CastToExt_c();
    }

    @Override
    protected Ext extCatchImpl() {
        return new CatchToExt_c();
    }

    @Override
    protected Ext extClassBodyImpl() {
        return new ClassBodyToExt_c();
    }

    @Override
    protected Ext extClassDeclImpl() {
        return new ClassDeclToExt_c();
    }

    @Override
    protected Ext extConditionalImpl() {
        return new ConditionalToExt_c();
    }

    @Override
    protected Ext extConstructorCallImpl() {
        return new ConstructorCallToExt_c();
    }

    @Override
    protected Ext extConstructorDeclImpl() {
        return new ConstructorDeclToExt_c();
    }

    @Override
    protected Ext extDoImpl() {
        return new DoToExt_c();
    }

    @Override
    protected Ext extEmptyImpl() {
        return new EmptyToExt_c();
    }

    @Override
    protected Ext extEvalImpl() {
        return new EvalToExt_c();
    }

    @Override
    protected Ext extFieldImpl() {
        return new FieldToExt_c();
    }

    @Override
    protected Ext extFieldDeclImpl() {
        return new FieldDeclToExt_c();
    }

    @Override
    protected Ext extForImpl() {
        return new ForToExt_c();
    }

    @Override
    protected Ext extFormalImpl() {
        return new FormalToExt_c();
    }

    @Override
    protected Ext extIfImpl() {
        return new IfToExt_c();
    }

    @Override
    protected Ext extImportImpl() {
        return new ImportToExt_c();
    }

    @Override
    protected Ext extInitializerImpl() {
        return new InitializerToExt_c();
    }

    @Override
    protected Ext extInstanceofImpl() {
        return new InstanceOfToExt_c();
    }

    @Override
    protected Ext extLabeledImpl() {
        return new LabeledToExt_c();
    }

    @Override
    protected Ext extLitImpl() {
        return new LitToExt_c();
    }

    @Override
    protected Ext extLocalImpl() {
        return new LocalToExt_c();
    }

    @Override
    protected Ext extLocalDeclImpl() {
        return new LocalDeclToExt_c();
    }

    @Override
    protected Ext extMethodDeclImpl() {
        return new MethodDeclToExt_c();
    }

    @Override
    protected Ext extNewArrayImpl() {
        return new NewArrayToExt_c();
    }

    @Override
    protected Ext extNewImpl() {
        return new NewToExt_c();
    }

    @Override
    protected Ext extPackageNodeImpl() {
        return new PackageNodeToExt_c();
    }

    @Override
    protected Ext extReturnImpl() {
        return new ReturnToExt_c();
    }

    @Override
    protected Ext extSourceFileImpl() {
        return new SourceFileToExt_c();
    }

    @Override
    protected Ext extSpecialImpl() {
        return new SpecialToExt_c();
    }

    @Override
    protected Ext extSwitchBlockImpl() {
        return new SwitchBlockToExt_c();
    }

    @Override
    protected Ext extSwitchImpl() {
        return new SwitchToExt_c();
    }

    @Override
    protected Ext extSynchronizedImpl() {
        return new SynchronizedToExt_c();
    }

    @Override
    protected Ext extThrowImpl() {
        return new ThrowToExt_c();
    }

    @Override
    protected Ext extTryImpl() {
        return new TryToExt_c();
    }

    @Override
    protected Ext extUnaryImpl() {
        return new UnaryToExt_c();
    }

    @Override
    protected Ext extWhileImpl() {
        return new WhileToExt_c();
    }

}
