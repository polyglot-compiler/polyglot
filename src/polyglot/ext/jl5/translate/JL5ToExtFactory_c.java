package polyglot.ext.jl5.translate;

import polyglot.ast.Ext;
import polyglot.ext.jl5.ast.JL5AssignDel;
import polyglot.ext.jl5.ast.JL5Del;
import polyglot.ext.jl5.ast.JL5ExtFactory;
import polyglot.translate.ext.ToExtFactory_c;

public class JL5ToExtFactory_c extends ToExtFactory_c implements JL5ExtFactory {

    public JL5ToExtFactory_c() {
        super();
    }

    public JL5ToExtFactory_c(JL5ExtFactory nextExtFactory) {
        super(nextExtFactory);
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
        return new EnumDeclToExt_c();
    }

    protected Ext extExtendedForImpl() {
        return new ExtendedForToExt_c();
    }

    protected Ext extEnumConstantDeclImpl() {
        return new EnumConstantDeclToExt_c();
    }

    protected Ext extEnumConstantImpl() {
        return new EnumConstantToExt_c();
    }

    protected Ext extParamTypeNodeImpl() {
        return new ParamTypeNodeToExt_c();
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

    protected Ext extAnnotationElemDeclImpl() {
        return this.extClassMemberImpl();
    }

    protected Ext extNormalAnnotationElemImpl() {
        return this.extExprImpl();
    }

    protected Ext extMarkerAnnotationElemImpl() {
        return this.extNormalAnnotationElemImpl();
    }

    protected Ext extSingleElementAnnotationElemImpl() {
        return this.extNormalAnnotationElemImpl();
    }

    protected Ext extElementValuePairImpl() {
        return this.extExprImpl();
    }

    protected Ext postExtAnnotationElemDecl(Ext ext) {
        return ext;
    }

    protected Ext postExtNormalAnnotationElem(Ext ext) {
        return ext;
    }

    protected Ext postExtMarkerAnnotationElem(Ext ext) {
        return ext;
    }

    protected Ext postExtSingleElementAnnotationElem(Ext ext) {
        return ext;
    }

    protected Ext postExtElementValuePair(Ext ext) {
        return ext;
    }
}
