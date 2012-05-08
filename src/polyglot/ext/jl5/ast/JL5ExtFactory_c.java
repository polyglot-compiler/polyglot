package polyglot.ext.jl5.ast;

import polyglot.ast.*;

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

	public Ext extEnumDeclImpl() {
		return this.extClassDeclImpl();
	}

	public Ext extExtendedForImpl() {
		return this.extLoopImpl();
	}

	public Ext extEnumConstantDeclImpl() {
		return this.extClassMemberImpl();
	}

	public Ext extEnumConstantImpl() {
		return this.extFieldImpl();
	}

	public Ext extParamTypeNodeImpl() {
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

}
