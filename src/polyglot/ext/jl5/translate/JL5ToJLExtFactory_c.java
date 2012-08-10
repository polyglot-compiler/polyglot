package polyglot.ext.jl5.translate;

import polyglot.ast.Ext;

public class JL5ToJLExtFactory_c extends JL5ToExtFactory_c {

    @Override
    protected Ext extClassDeclImpl() {
        return new JL5ClassDeclToJL_c();
    }

    @Override
    protected Ext extMethodDeclImpl() {
        return new JL5MethodDeclToJL_c();
    }

    @Override
    protected Ext extConstructorDeclImpl() {
        return new JL5ConstructorDeclToJL_c();
    }

    @Override
    protected Ext extCanonicalTypeNodeImpl() {
        return new JL5TypeNodeToJL_c();
    }

    @Override
    protected Ext extParamTypeNodeImpl() {
        return new JL5TypeNodeToJL_c();
    }

    @Override
    protected Ext extEnumConstantImpl() {
        return new EnumConstantToJL_c();
    }

    // The below nodes should have been removed 
    // by the time the ExtensionRewriter is called.
    @Override
    protected Ext extEnumDeclImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extExtendedForImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extEnumConstantDeclImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extAnnotationElemDeclImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extNormalAnnotationElemImpl() {
        return new CannotToExt_c();
    }

    @Override
    protected Ext extElementValuePairImpl() {
        return new CannotToExt_c();
    }

}
