package polyglot.ext.jl5.ast;

import polyglot.ast.AbstractDelFactory_c;
import polyglot.ast.JL;

public class JL5DelFactory_c extends AbstractDelFactory_c implements
        JL5DelFactory {

    public JL5DelFactory_c() {
        super();
    }

    public JL5DelFactory_c(JL5DelFactory delFactory) {
        super(delFactory);
    }

    @Override
    public JL5DelFactory nextDelFactory() {
        return (JL5DelFactory) super.nextDelFactory();
    }

    @Override
    public JL delEnumDecl() {
        JL e = delEnumDeclImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delEnumDecl();
            e = composeDels(e, e2);
        }
        return postDelEnumDecl(e);
    }

    @Override
    public JL delExtendedFor() {
        JL e = delExtendedForImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delExtendedFor();
            e = composeDels(e, e2);
        }
        return postDelExtendedFor(e);
    }

    @Override
    public JL delEnumConstantDecl() {
        JL e = delEnumConstantImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delEnumConstant();
            e = composeDels(e, e2);
        }
        return postDelEnumConstant(e);
    }

    @Override
    public JL delEnumConstant() {
        JL e = delEnumConstantImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delEnumConstant();
            e = composeDels(e, e2);
        }
        return postDelEnumConstant(e);
    }

    @Override
    public JL delParamTypeNode() {
        JL e = delParamTypeNodeImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delParamTypeNode();
            e = composeDels(e, e2);
        }
        return postDelParamTypeNode(e);
    }

    @Override
    public JL delAnnotationElemDecl() {
        JL e = delAnnotationElemDeclImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delAnnotationElemDecl();
            e = composeDels(e, e2);
        }
        return postDelAnnotationElemDecl(e);
    }

    @Override
    public JL delNormalAnnotationElem() {
        JL e = delNormalAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delNormalAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelNormalAnnotationElem(e);
    }

    @Override
    public JL delMarkerAnnotationElem() {
        JL e = delMarkerAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delMarkerAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelMarkerAnnotationElem(e);
    }

    @Override
    public JL delSingleElementAnnotationElem() {
        JL e = delSingleElementAnnotationElemImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delSingleElementAnnotationElem();
            e = composeDels(e, e2);
        }
        return postDelSingleElementAnnotationElem(e);
    }

    @Override
    public JL delElementValuePair() {
        JL e = delElementValuePairImpl();

        if (nextDelFactory() != null) {
            JL e2 = nextDelFactory().delElementValuePair();
            e = composeDels(e, e2);
        }
        return postDelElementValuePair(e);
    }

    public JL delEnumDeclImpl() {
        return this.delClassDeclImpl();
    }

    public JL delExtendedForImpl() {
        return this.delLoopImpl();
    }

    public JL delEnumConstantDeclImpl() {
        return this.delClassMemberImpl();
    }

    public JL delEnumConstantImpl() {
        return this.delFieldImpl();
    }

    public JL delParamTypeNodeImpl() {
        return this.delTypeNodeImpl();
    }

    @Override
    protected JL delAssignImpl() {
        return new JL5AssignDel();
    }

    @Override
    protected JL delNodeImpl() {
        return new JL5Del();
    }

    public JL postDelEnumDecl(JL del) {
        return this.postDelClassDecl(del);
    }

    public JL postDelExtendedFor(JL del) {
        return this.postDelLoop(del);
    }

    public JL postDelEnumConstantDecl(JL del) {
        return this.postDelClassMember(del);
    }

    public JL postDelEnumConstant(JL del) {
        return this.postDelField(del);
    }

    public JL postDelParamTypeNode(JL del) {
        return this.postDelTypeNode(del);
    }

    private JL postDelAnnotationElemDecl(JL e) {
        return e;
    }

    private JL delAnnotationElemDeclImpl() {
        return this.delClassMemberImpl();
    }

    private JL delNormalAnnotationElemImpl() {
        return this.delExprImpl();
    }

    private JL delMarkerAnnotationElemImpl() {
        return this.delNormalAnnotationElemImpl();
    }

    private JL delSingleElementAnnotationElemImpl() {
        return this.delNormalAnnotationElemImpl();
    }

    private JL delElementValuePairImpl() {
        return this.delExprImpl();
    }

    private JL postDelNormalAnnotationElem(JL e) {
        return e;
    }

    private JL postDelMarkerAnnotationElem(JL e) {
        return e;
    }

    private JL postDelSingleElementAnnotationElem(JL e) {
        return e;
    }

    private JL postDelElementValuePair(JL e) {
        return e;
    }

}
