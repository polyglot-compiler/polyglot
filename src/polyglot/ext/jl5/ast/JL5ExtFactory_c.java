package polyglot.ext.jl5.ast;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;

public class JL5ExtFactory_c extends AbstractExtFactory_c implements
        JL5ExtFactory {

    public JL5ExtFactory_c() {
        super();
    }

    public JL5ExtFactory_c(JL5ExtFactory extFactory) {
        super(extFactory);
    }

    @Override
    public JL5ExtFactory nextExtFactory() {
        return (JL5ExtFactory) super.nextExtFactory();
    }

    @Override
    public Ext extEnumDecl() {
        Ext e = extEnumDeclImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extEnumDecl();
            e = composeExts(e, e2);
        }
        return postExtEnumDecl(e);
    }

    @Override
    public Ext extExtendedFor() {
        Ext e = extExtendedForImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extExtendedFor();
            e = composeExts(e, e2);
        }
        return postExtExtendedFor(e);
    }

    @Override
    public Ext extEnumConstantDecl() {
        Ext e = extEnumConstantDeclImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extEnumConstantDecl();
            e = composeExts(e, e2);
        }
        return postExtEnumConstantDecl(e);
    }

    @Override
    public Ext extEnumConstant() {
        Ext e = extEnumConstantImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extEnumConstant();
            e = composeExts(e, e2);
        }
        return postExtEnumConstant(e);
    }

    @Override
    public Ext extParamTypeNode() {
        Ext e = extParamTypeNodeImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extParamTypeNode();
            e = composeExts(e, e2);
        }
        return postExtParamTypeNode(e);
    }

    @Override
    public Ext extAnnotationElemDecl() {
        Ext e = extAnnotationElemDeclImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extAnnotationElemDecl();
            e = composeExts(e, e2);
        }
        return postExtAnnotationElemDecl(e);
    }

    @Override
    public Ext extNormalAnnotationElem() {
        Ext e = extNormalAnnotationElemImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extNormalAnnotationElem();
            e = composeExts(e, e2);
        }
        return postExtNormalAnnotationElem(e);
    }

    @Override
    public Ext extMarkerAnnotationElem() {
        Ext e = extMarkerAnnotationElemImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extMarkerAnnotationElem();
            e = composeExts(e, e2);
        }
        return postExtMarkerAnnotationElem(e);
    }

    @Override
    public Ext extSingleElementAnnotationElem() {
        Ext e = extSingleElementAnnotationElemImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extSingleElementAnnotationElem();
            e = composeExts(e, e2);
        }
        return postExtSingleElementAnnotationElem(e);
    }

    @Override
    public Ext extElementValuePair() {
        Ext e = extElementValuePairImpl();

        if (nextExtFactory() != null) {
            Ext e2 = nextExtFactory().extElementValuePair();
            e = composeExts(e, e2);
        }
        return postExtElementValuePair(e);
    }

    protected Ext extEnumDeclImpl() {
        return this.extClassDeclImpl();
    }

    protected Ext extExtendedForImpl() {
        return this.extLoopImpl();
    }

    protected Ext extEnumConstantDeclImpl() {
        return this.extClassMemberImpl();
    }

    protected Ext extEnumConstantImpl() {
        return this.extFieldImpl();
    }

    protected Ext extParamTypeNodeImpl() {
        return this.extTypeNodeImpl();
    }

    @Override
    protected Ext extAssignImpl() {
        return new JL5AssignDel();
    }

    @Override
    protected Ext extNodeImpl() {
        return new JL5Del();
    }

    public Ext postExtEnumDecl(Ext ext) {
        return this.postExtClassDecl(ext);
    }

    public Ext postExtExtendedFor(Ext ext) {
        return this.postExtLoop(ext);
    }

    public Ext postExtEnumConstantDecl(Ext ext) {
        return this.postExtClassMember(ext);
    }

    public Ext postExtEnumConstant(Ext ext) {
        return this.postExtField(ext);
    }

    public Ext postExtParamTypeNode(Ext ext) {
        return this.postExtTypeNode(ext);
    }

    private Ext extAnnotationElemDeclImpl() {
        return this.extClassMemberImpl();
    }

    private Ext extNormalAnnotationElemImpl() {
        return this.extExprImpl();
    }

    private Ext extMarkerAnnotationElemImpl() {
        return this.extNormalAnnotationElemImpl();
    }

    private Ext extSingleElementAnnotationElemImpl() {
        return this.extNormalAnnotationElemImpl();
    }

    private Ext extElementValuePairImpl() {
        return this.extExprImpl();
    }

    private Ext postExtAnnotationElemDecl(Ext ext) {
        return ext;
    }

    private Ext postExtNormalAnnotationElem(Ext ext) {
        return ext;
    }

    private Ext postExtMarkerAnnotationElem(Ext ext) {
        return ext;
    }

    private Ext postExtSingleElementAnnotationElem(Ext ext) {
        return ext;
    }

    private Ext postExtElementValuePair(Ext ext) {
        return ext;
    }

}
