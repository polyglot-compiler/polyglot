package polyglot.ext.jl.ast;

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

/**
 * This abstract implementation of <code>ExtFactory</code> provides
 * a way of chaining together ExtFactories, and default implementations
 * of factory methods for each node.
 */
public abstract class AbstractExtFactory_c implements ExtFactory
{
    protected AbstractExtFactory_c() {
        this(null);
    }
    
    protected AbstractExtFactory_c(ExtFactory nextExtFactory) {
        this.nextExtFactory = nextExtFactory;
    }
    
    /**
     * The next extFactory in the chain. Whenever an extension is instantiated,
     * the next extFactory should be called to see if it also has an extension,
     * and if so, the extensions should be joined together using the method
     * <code>composeExts</code>
     */
    private ExtFactory nextExtFactory;

    public ExtFactory nextExtFactory() {
        return nextExtFactory;
    }
    /**
     * Compose two extensions together. Order is important: e1 gets added
     * at the end of e2's chain of extensions.
     */
    protected Ext composeExts(Ext e1, Ext e2) {
        if (e1 == null) return e2;        
        if (e2 == null) return e1;        
        // add e1 as e2's last extension, by recursing...
        return e2.ext(composeExts(e1, e2.ext()));
    }
    
    // ******************************************
    // Final methods that call the Impl methods to construct 
    // extensions, and then check with nextExtFactory to see if it
    // also has an extension. Finally, call an appropriate post method,
    // to allow subclasses to perform operations on the construction Exts
    // ******************************************
    public final Ext extAmbAssign() {
        Ext e = extAmbAssignImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAmbAssign();
            e = composeExts(e, e2);
        }
        return postExtAmbAssign(e);
    }

    public final Ext extAmbExpr() {
        Ext e = extAmbExprImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAmbExpr();
            e = composeExts(e, e2);
        }
        return postExtAmbExpr(e);
    }

    public final Ext extAmbPrefix() {
        Ext e = extAmbPrefixImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAmbPrefix();
            e = composeExts(e, e2);
        }
        return postExtAmbPrefix(e);
    }

    public final Ext extAmbQualifierNode() {
        Ext e = extAmbQualifierNodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAmbQualifierNode();
            e = composeExts(e, e2);
        }
        return postExtAmbQualifierNode(e);
    }

    public final Ext extAmbReceiver() {
        Ext e = extAmbReceiverImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAmbReceiver();
            e = composeExts(e, e2);
        }
        return postExtAmbReceiver(e);
    }

    public final Ext extAmbTypeNode() {
        Ext e = extAmbTypeNodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAmbTypeNode();
            e = composeExts(e, e2);
        }
        return postExtAmbTypeNode(e);
    }

    public final Ext extArrayAccess() {
        Ext e = extArrayAccessImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extArrayAccess();
            e = composeExts(e, e2);
        }
        return postExtArrayAccess(e);
    }

    public final Ext extArrayInit() {
        Ext e = extArrayInitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extArrayInit();
            e = composeExts(e, e2);
        }
        return postExtArrayInit(e);
    }

    public final Ext extArrayTypeNode() {
        Ext e = extArrayTypeNodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extArrayTypeNode();
            e = composeExts(e, e2);
        }
        return postExtArrayTypeNode(e);
    }

    public final Ext extAssert() {
        Ext e = extAssertImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAssert();
            e = composeExts(e, e2);
        }
        return postExtAssert(e);
    }

    public final Ext extAssign() {
        Ext e = extAssignImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAssign();
            e = composeExts(e, e2);
        }
        return postExtAssign(e);
    }

    public final Ext extLocalAssign() {
        Ext e = extLocalAssignImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extLocalAssign();
            e = composeExts(e, e2);
        }
        return postExtLocalAssign(e);
    }

    public final Ext extFieldAssign() {
        Ext e = extFieldAssignImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extFieldAssign();
            e = composeExts(e, e2);
        }
        return postExtFieldAssign(e);
    }

    public final Ext extArrayAccessAssign() {
        Ext e = extArrayAccessAssignImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extArrayAccessAssign();
            e = composeExts(e, e2);
        }
        return postExtArrayAccessAssign(e);
    }

    public final Ext extBinary() {
        Ext e = extBinaryImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extBinary();
            e = composeExts(e, e2);
        }
        return postExtBinary(e);
    }

    public final Ext extBlock() {
        Ext e = extBlockImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extBlock();
            e = composeExts(e, e2);
        }
        return postExtBlock(e);
    }

    public final Ext extBooleanLit() {
        Ext e = extBooleanLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extBooleanLit();
            e = composeExts(e, e2);
        }
        return postExtBooleanLit(e);
    }

    public final Ext extBranch() {
        Ext e = extBranchImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extBranch();
            e = composeExts(e, e2);
        }
        return postExtBranch(e);
    }

    public final Ext extCall() {
        Ext e = extCallImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCall();
            e = composeExts(e, e2);
        }
        return postExtCall(e);
    }

    public final Ext extCanonicalTypeNode() {
        Ext e = extCanonicalTypeNodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCanonicalTypeNode();
            e = composeExts(e, e2);
        }
        return postExtCanonicalTypeNode(e);
    }

    public final Ext extCase() {
        Ext e = extCaseImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCase();
            e = composeExts(e, e2);
        }
        return postExtCase(e);
    }

    public final Ext extCast() {
        Ext e = extCastImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCast();
            e = composeExts(e, e2);
        }
        return postExtCast(e);
    }

    public final Ext extCatch() {
        Ext e = extCatchImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCatch();
            e = composeExts(e, e2);
        }
        return postExtCatch(e);
    }

    public final Ext extCharLit() {
        Ext e = extCharLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCharLit();
            e = composeExts(e, e2);
        }
        return postExtCharLit(e);
    }

    public final Ext extClassBody() {
        Ext e = extClassBodyImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extClassBody();
            e = composeExts(e, e2);
        }
        return postExtClassBody(e);
    }

    public final Ext extClassDecl() {
        Ext e = extClassDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extClassDecl();
            e = composeExts(e, e2);
        }
        return postExtClassDecl(e);
    }

    public final Ext extClassMember() {
        Ext e = extClassMemberImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extClassMember();
            e = composeExts(e, e2);
        }
        return postExtClassMember(e);
    }

    public final Ext extCodeDecl() {
        Ext e = extCodeDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCodeDecl();
            e = composeExts(e, e2);
        }
        return postExtCodeDecl(e);
    }

    public final Ext extConditional() {
        Ext e = extConditionalImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extConditional();
            e = composeExts(e, e2);
        }
        return postExtConditional(e);
    }

    public final Ext extConstructorCall() {
        Ext e = extConstructorCallImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extConstructorCall();
            e = composeExts(e, e2);
        }
        return postExtConstructorCall(e);
    }

    public final Ext extConstructorDecl() {
        Ext e = extConstructorDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extConstructorDecl();
            e = composeExts(e, e2);
        }
        return postExtConstructorDecl(e);
    }

    public final Ext extDo() {
        Ext e = extDoImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDo();
            e = composeExts(e, e2);
        }
        return postExtDo(e);
    }

    public final Ext extEmpty() {
        Ext e = extEmptyImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extEmpty();
            e = composeExts(e, e2);
        }
        return postExtEmpty(e);
    }

    public final Ext extEval() {
        Ext e = extEvalImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extEval();
            e = composeExts(e, e2);
        }
        return postExtEval(e);
    }

    public final Ext extExpr() {
        Ext e = extExprImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extExpr();
            e = composeExts(e, e2);
        }
        return postExtExpr(e);
    }

    public final Ext extField() {
        Ext e = extFieldImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extField();
            e = composeExts(e, e2);
        }
        return postExtField(e);
    }

    public final Ext extFieldDecl() {
        Ext e = extFieldDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extFieldDecl();
            e = composeExts(e, e2);
        }
        return postExtFieldDecl(e);
    }

    public final Ext extFloatLit() {
        Ext e = extFloatLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extFloatLit();
            e = composeExts(e, e2);
        }
        return postExtFloatLit(e);
    }

    public final Ext extFor() {
        Ext e = extForImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extFor();
            e = composeExts(e, e2);
        }
        return postExtFor(e);
    }

    public final Ext extFormal() {
        Ext e = extFormalImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extFormal();
            e = composeExts(e, e2);
        }
        return postExtFormal(e);
    }

    public final Ext extIf() {
        Ext e = extIfImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extIf();
            e = composeExts(e, e2);
        }
        return postExtIf(e);
    }

    public final Ext extImport() {
        Ext e = extImportImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extImport();
            e = composeExts(e, e2);
        }
        return postExtImport(e);
    }

    public final Ext extInitializer() {
        Ext e = extInitializerImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extInitializer();
            e = composeExts(e, e2);
        }
        return postExtInitializer(e);
    }

    public final Ext extInstanceof() {
        Ext e = extInstanceofImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extInstanceof();
            e = composeExts(e, e2);
        }
        return postExtInstanceof(e);
    }

    public final Ext extIntLit() {
        Ext e = extIntLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extIntLit();
            e = composeExts(e, e2);
        }
        return postExtIntLit(e);
    }

    public final Ext extLabeled() {
        Ext e = extLabeledImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extLabeled();
            e = composeExts(e, e2);
        }
        return postExtLabeled(e);
    }

    public final Ext extLit() {
        Ext e = extLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extLit();
            e = composeExts(e, e2);
        }
        return postExtLit(e);
    }

    public final Ext extLocal() {
        Ext e = extLocalImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extLocal();
            e = composeExts(e, e2);
        }
        return postExtLocal(e);
    }

    public final Ext extLocalClassDecl() {
        Ext e = extLocalClassDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extLocalClassDecl();
            e = composeExts(e, e2);
        }
        return postExtLocalClassDecl(e);
    }

    public final Ext extLocalDecl() {
        Ext e = extLocalDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extLocalDecl();
            e = composeExts(e, e2);
        }
        return postExtLocalDecl(e);
    }

    public final Ext extLoop() {
        Ext e = extLoopImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extLoop();
            e = composeExts(e, e2);
        }
        return postExtLoop(e);
    }

    public final Ext extMethodDecl() {
        Ext e = extMethodDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extMethodDecl();
            e = composeExts(e, e2);
        }
        return postExtMethodDecl(e);
    }

    public final Ext extNewArray() {
        Ext e = extNewArrayImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extNewArray();
            e = composeExts(e, e2);
        }
        return postExtNewArray(e);
    }

    public final Ext extNode() {
        Ext e = extNodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extNode();
            e = composeExts(e, e2);
        }
        return postExtNode(e);
    }

    public final Ext extNew() {
        Ext e = extNewImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extNew();
            e = composeExts(e, e2);
        }
        return postExtNew(e);
    }

    public final Ext extNullLit() {
        Ext e = extNullLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extNullLit();
            e = composeExts(e, e2);
        }
        return postExtNullLit(e);
    }

    public final Ext extNumLit() {
        Ext e = extNumLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extNumLit();
            e = composeExts(e, e2);
        }
        return postExtNumLit(e);
    }

    public final Ext extPackageNode() {
        Ext e = extPackageNodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPackageNode();
            e = composeExts(e, e2);
        }
        return postExtPackageNode(e);
    }

    public final Ext extProcedureDecl() {
        Ext e = extProcedureDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extProcedureDecl();
            e = composeExts(e, e2);
        }
        return postExtProcedureDecl(e);
    }

    public final Ext extReturn() {
        Ext e = extReturnImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extReturn();
            e = composeExts(e, e2);
        }
        return postExtReturn(e);
    }

    public final Ext extSourceCollection() {
        Ext e = extSourceCollectionImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSourceCollection();
            e = composeExts(e, e2);
        }
        return postExtSourceCollection(e);
    }

    public final Ext extSourceFile() {
        Ext e = extSourceFileImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSourceFile();
            e = composeExts(e, e2);
        }
        return postExtSourceFile(e);
    }

    public final Ext extSpecial() {
        Ext e = extSpecialImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSpecial();
            e = composeExts(e, e2);
        }
        return postExtSpecial(e);
    }

    public final Ext extStmt() {
        Ext e = extStmtImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extStmt();
            e = composeExts(e, e2);
        }
        return postExtStmt(e);
    }

    public final Ext extStringLit() {
        Ext e = extStringLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extStringLit();
            e = composeExts(e, e2);
        }
        return postExtStringLit(e);
    }

    public final Ext extSwitchBlock() {
        Ext e = extSwitchBlockImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSwitchBlock();
            e = composeExts(e, e2);
        }
        return postExtSwitchBlock(e);
    }

    public final Ext extSwitchElement() {
        Ext e = extSwitchElementImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSwitchElement();
            e = composeExts(e, e2);
        }
        return postExtSwitchElement(e);
    }

    public final Ext extSwitch() {
        Ext e = extSwitchImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSwitch();
            e = composeExts(e, e2);
        }
        return postExtSwitch(e);
    }

    public final Ext extSynchronized() {
        Ext e = extSynchronizedImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSynchronized();
            e = composeExts(e, e2);
        }
        return postExtSynchronized(e);
    }

    public final Ext extTerm() {
        Ext e = extTermImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTerm();
            e = composeExts(e, e2);
        }
        return postExtTerm(e);
    }

    public final Ext extThrow() {
        Ext e = extThrowImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extThrow();
            e = composeExts(e, e2);
        }
        return postExtThrow(e);
    }

    public final Ext extTry() {
        Ext e = extTryImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTry();
            e = composeExts(e, e2);
        }
        return postExtTry(e);
    }

    public final Ext extTypeNode() {
        Ext e = extTypeNodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTypeNode();
            e = composeExts(e, e2);
        }
        return postExtTypeNode(e);
    }

    public final Ext extUnary() {
        Ext e = extUnaryImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extUnary();
            e = composeExts(e, e2);
        }
        return postExtUnary(e);
    }

    public final Ext extWhile() {
        Ext e = extWhileImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extWhile();
            e = composeExts(e, e2);
        }
        return postExtWhile(e);
    }

    // ********************************************
    // Impl methods
    // ********************************************
    
    protected Ext extAmbAssignImpl() {
        return extAssignImpl();
    }

    protected Ext extAmbExprImpl() {
        return extExprImpl();
    }

    protected Ext extAmbPrefixImpl() {
        return extNodeImpl();
    }

    protected Ext extAmbQualifierNodeImpl() {
        return extNodeImpl();
    }

    protected Ext extAmbReceiverImpl() {
        return extNodeImpl();
    }

    protected Ext extAmbTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    protected Ext extArrayAccessImpl() {
        return extExprImpl();
    }

    protected Ext extArrayInitImpl() {
        return extExprImpl();
    }

    protected Ext extArrayTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    protected Ext extAssertImpl() {
        return extStmtImpl();
    }

    protected Ext extAssignImpl() {
        return extExprImpl();
    }

    protected Ext extLocalAssignImpl() {
        return extAssignImpl();
    }

    protected Ext extFieldAssignImpl() {
        return extAssignImpl();
    }

    protected Ext extArrayAccessAssignImpl() {
        return extAssignImpl();
    }
    protected Ext extBinaryImpl() {
        return extExprImpl();
    }

    protected Ext extBlockImpl() {
        return extStmtImpl();
    }

    protected Ext extBooleanLitImpl() {
        return extLitImpl();
    }

    protected Ext extBranchImpl() {
        return extStmtImpl();
    }

    protected Ext extCallImpl() {
        return extExprImpl();
    }

    protected Ext extCanonicalTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    protected Ext extCaseImpl() {
        return extSwitchElementImpl();
    }

    protected Ext extCastImpl() {
        return extExprImpl();
    }

    protected Ext extCatchImpl() {
        return extStmtImpl();
    }

    protected Ext extCharLitImpl() {
        return extNumLitImpl();
    }

    protected Ext extClassBodyImpl() {
        return extTermImpl();
    }

    protected Ext extClassDeclImpl() {
        return extTermImpl();
    }

    protected Ext extClassMemberImpl() {
        return extNodeImpl();
    }

    protected Ext extCodeDeclImpl() {
        return extClassMemberImpl();
    }

    protected Ext extConditionalImpl() {
        return extExprImpl();
    }

    protected Ext extConstructorCallImpl() {
        return extStmtImpl();
    }

    protected Ext extConstructorDeclImpl() {
        return extProcedureDeclImpl();
    }

    protected Ext extDoImpl() {
        return extLoopImpl();
    }

    protected Ext extEmptyImpl() {
        return extStmtImpl();
    }

    protected Ext extEvalImpl() {
        return extStmtImpl();
    }

    protected Ext extExprImpl() {
        return extTermImpl();
    }

    protected Ext extFieldImpl() {
        return extExprImpl();
    }

    protected Ext extFieldDeclImpl() {
        return extClassMemberImpl();
    }

    protected Ext extFloatLitImpl() {
        return extLitImpl();
    }

    protected Ext extForImpl() {
        return extLoopImpl();
    }

    protected Ext extFormalImpl() {
        return extNodeImpl();
    }

    protected Ext extIfImpl() {
        return extStmtImpl();
    }

    protected Ext extImportImpl() {
        return extNodeImpl();
    }

    protected Ext extInitializerImpl() {
        return extCodeDeclImpl();
    }

    protected Ext extInstanceofImpl() {
        return extExprImpl();
    }

    protected Ext extIntLitImpl() {
        return extNumLitImpl();
    }

    protected Ext extLabeledImpl() {
        return extStmtImpl();
    }

    protected Ext extLitImpl() {
        return extExprImpl();
    }

    protected Ext extLocalImpl() {
        return extExprImpl();
    }

    protected Ext extLocalClassDeclImpl() {
        return extStmtImpl();
    }

    protected Ext extLocalDeclImpl() {
        return extNodeImpl();
    }

    protected Ext extLoopImpl() {
        return extStmtImpl();
    }

    protected Ext extMethodDeclImpl() {
        return extProcedureDeclImpl();
    }

    protected Ext extNewArrayImpl() {
        return extExprImpl();
    }

    protected Ext extNodeImpl() {
        return null;
    }

    protected Ext extNewImpl() {
        return extExprImpl();
    }

    protected Ext extNullLitImpl() {
        return extLitImpl();
    }

    protected Ext extNumLitImpl() {
        return extLitImpl();
    }

    protected Ext extPackageNodeImpl() {
        return extNodeImpl();
    }

    protected Ext extProcedureDeclImpl() {
        return extCodeDeclImpl();
    }

    protected Ext extReturnImpl() {
        return extStmtImpl();
    }

    protected Ext extSourceCollectionImpl() {
        return extNodeImpl();
    }

    protected Ext extSourceFileImpl() {
        return extNodeImpl();
    }

    protected Ext extSpecialImpl() {
        return extExprImpl();
    }

    protected Ext extStmtImpl() {
        return extTermImpl();
    }

    protected Ext extStringLitImpl() {
        return extLitImpl();
    }

    protected Ext extSwitchBlockImpl() {
        return extSwitchElementImpl();
    }

    protected Ext extSwitchElementImpl() {
        return extStmtImpl();
    }

    protected Ext extSwitchImpl() {
        return extStmtImpl();
    }

    protected Ext extSynchronizedImpl() {
        return extStmtImpl();
    }

    protected Ext extTermImpl() {
        return extNodeImpl();
    }

    protected Ext extThrowImpl() {
        return extStmtImpl();
    }

    protected Ext extTryImpl() {
        return extStmtImpl();
    }

    protected Ext extTypeNodeImpl() {
        return extNodeImpl();
    }

    protected Ext extUnaryImpl() {
        return extExprImpl();
    }

    protected Ext extWhileImpl() {
        return extLoopImpl();
    }


    // ********************************************
    // Post methods
    // ********************************************

    protected Ext postExtAmbAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtAmbExpr(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtAmbPrefix(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbQualifierNode(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbReceiver(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbTypeNode(Ext ext) {
        return postExtTypeNode(ext);
    }

    protected Ext postExtArrayAccess(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtArrayInit(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtArrayTypeNode(Ext ext) {
        return postExtTypeNode(ext);
    }

    protected Ext postExtAssert(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtAssign(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtLocalAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtFieldAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtArrayAccessAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtBinary(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtBlock(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtBooleanLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtBranch(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtCall(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtCanonicalTypeNode(Ext ext) {
        return postExtTypeNode(ext);
    }

    protected Ext postExtCase(Ext ext) {
        return postExtSwitchElement(ext);
    }

    protected Ext postExtCast(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtCatch(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtCharLit(Ext ext) {
        return postExtNumLit(ext);
    }

    protected Ext postExtClassBody(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtClassDecl(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtClassMember(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtCodeDecl(Ext ext) {
        return postExtClassMember(ext);
    }

    protected Ext postExtConditional(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtConstructorCall(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtConstructorDecl(Ext ext) {
        return postExtProcedureDecl(ext);
    }

    protected Ext postExtDo(Ext ext) {
        return postExtLoop(ext);
    }

    protected Ext postExtEmpty(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtEval(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtExpr(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtField(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtFieldDecl(Ext ext) {
        return postExtClassMember(ext);
    }

    protected Ext postExtFloatLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtFor(Ext ext) {
        return postExtLoop(ext);
    }

    protected Ext postExtFormal(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtIf(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtImport(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtInitializer(Ext ext) {
        return postExtCodeDecl(ext);
    }

    protected Ext postExtInstanceof(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtIntLit(Ext ext) {
        return postExtNumLit(ext);
    }

    protected Ext postExtLabeled(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtLit(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtLocal(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtLocalClassDecl(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtLocalDecl(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtLoop(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtMethodDecl(Ext ext) {
        return postExtProcedureDecl(ext);
    }

    protected Ext postExtNewArray(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtNode(Ext ext) {
        return ext;
    }

    protected Ext postExtNew(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtNullLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtNumLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtPackageNode(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtProcedureDecl(Ext ext) {
        return postExtCodeDecl(ext);
    }

    protected Ext postExtReturn(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtSourceCollection(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtSourceFile(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtSpecial(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtStmt(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtStringLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtSwitchBlock(Ext ext) {
        return postExtSwitchElement(ext);
    }

    protected Ext postExtSwitchElement(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtSwitch(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtSynchronized(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtTerm(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtThrow(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtTry(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtTypeNode(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtUnary(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtWhile(Ext ext) {
        return postExtLoop(ext);
    }

}
