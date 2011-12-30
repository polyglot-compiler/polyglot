package polyglot.translate.ext;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;

public class ToExtFactory_c extends AbstractExtFactory_c {

    
    protected Ext extIdImpl() {
        return new IdToExt_c();
    }

    
    protected Ext extArrayAccessImpl() {
        return new ArrayAccessToExt_c();
    }

    
    protected Ext extArrayInitImpl() {
        return new ArrayInitToExt_c();
    }

    
    protected Ext extLocalAssignImpl() {
        return new LocalAssignToExt_c();
    }

    
    protected Ext extFieldAssignImpl() {
        return new FieldAssignToExt_c();
    }

    
    protected Ext extArrayAccessAssignImpl() {
        return new ArrayAccessAssignToExt_c();
    }

    
    protected Ext extBinaryImpl() {
        return new BinaryToExt_c();
    }

    
    protected Ext extBlockImpl() {
        return new BlockToExt_c();
    }

    
    protected Ext extBranchImpl() {
        return new BranchToExt_c();
    }

    
    protected Ext extCallImpl() {
        return new CallToExt_c();
    }

    
    protected Ext extCanonicalTypeNodeImpl() {
        return new CanonicalTypeNodeToExt_c();
    }

    
    protected Ext extCaseImpl() {
        return new CaseToExt_c();
    }

    
    protected Ext extCastImpl() {
        return new CastToExt_c();
    }

    
    protected Ext extCatchImpl() {
        return new CatchToExt_c();
    }

    
    protected Ext extClassBodyImpl() {
        return new ClassBodyToExt_c();
    }

    
    protected Ext extClassDeclImpl() {    
        return new ClassDeclToExt_c();
    }

    
    protected Ext extConditionalImpl() {
        return new ConditionalToExt_c();
    }

    
    protected Ext extConstructorCallImpl() {
        return new ConstructorCallToExt_c();
    }

    
    protected Ext extConstructorDeclImpl() {
        return new ConstructorDeclToExt_c();
    }

    
    protected Ext extDoImpl() {
        return new DoToExt_c();
    }

    
    protected Ext extEmptyImpl() {
        return new EmptyToExt_c();
    }

    
    protected Ext extEvalImpl() {
        return new EvalToExt_c();
    }

    
    protected Ext extFieldImpl() {
        return new FieldToExt_c();
    }

    
    protected Ext extFieldDeclImpl() {  
        return new FieldDeclToExt_c();
    }

    
    protected Ext extForImpl() {
        return new ForToExt_c();
    }

    
    protected Ext extFormalImpl() {
        return new FormalToExt_c();
    }

    
    protected Ext extIfImpl() {
        return new IfToExt_c();
    }

    
    protected Ext extImportImpl() {
        return new ImportToExt_c();
    }

    
    protected Ext extInitializerImpl() {
        return new InitializerToExt_c();
    }

    
    protected Ext extInstanceofImpl() {
        return new InstanceOfToExt_c();
    }

    
    protected Ext extLabeledImpl() {
        return new LabeledToExt_c();
    }

    
    protected Ext extLitImpl() {
        return new LitToExt_c();
    }

    
    protected Ext extLocalImpl() {
        return new LocalToExt_c();
    }

    
    protected Ext extLocalDeclImpl() {
        return new LocalDeclToExt_c();
    }

    
    protected Ext extMethodDeclImpl() {
        return new MethodDeclToExt_c();
    }

    
    protected Ext extNewArrayImpl() {
        return new NewArrayToExt_c();
    }

    
    protected Ext extNewImpl() {
        return new NewToExt_c();
    }

    
    protected Ext extPackageNodeImpl() {
        return new PackageNodeToExt_c();
    }

    
    protected Ext extReturnImpl() {
        return new ReturnToExt_c();
    }

    
    protected Ext extSourceFileImpl() {
        return new SourceFileToExt_c();
    }

    
    protected Ext extSpecialImpl() {
        return new SpecialToExt_c();
    }

    
    protected Ext extSwitchBlockImpl() {
        return new SwitchBlockToExt_c();
    }

    
    protected Ext extSwitchImpl() {
        return new SwitchToExt_c();
    }

    
    protected Ext extSynchronizedImpl() {
        return new SynchronizedToExt_c();
    }

    
    protected Ext extThrowImpl() {  
        return new ThrowToExt_c();
    }

    
    protected Ext extTryImpl() {
        return new TryToExt_c();
    }

    
    protected Ext extUnaryImpl() {
        return new UnaryToExt_c();
    }

    
    protected Ext extWhileImpl() {
        return new WhileToExt_c();
    }

}
