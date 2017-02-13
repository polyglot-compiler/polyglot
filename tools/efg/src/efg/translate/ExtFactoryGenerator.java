package efg.translate;

import static efg.ExtensionInfo.ABSTRACT_EXT_FACTORY_BASENAME;
import static efg.ExtensionInfo.EFG_INFO;
import static efg.ExtensionInfo.EXT_FACTORY_BASENAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import efg.ExtensionInfo;
import efg.config.ast.Name;
import efg.util.EfgClassInfo;
import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5MethodDeclExt;
import polyglot.ext.jl5.qq.QQ;
import polyglot.ext.jl7.JL7ExtensionInfo;
import polyglot.ext.jl7.ast.JL7NodeFactory;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * Utility class for generating extension factories.
 */
public class ExtFactoryGenerator {
    /**
     * Fully qualified name of {@link Ext}.
     */
    protected static final String EXT_NAME = Ext.class.getName();

    /**
     * Fully qualified name of {@link ExtFactory}.
     */
    protected static final String EXT_FACTORY_NAME = ExtFactory.class.getName();

    /**
     * Fully qualified name of {@link AbstractExtFactory_c}.
     */
    protected static final String ABSTRACT_EXT_FACTORY_NAME =
            AbstractExtFactory_c.class.getName();

    protected ExtensionInfo efgExtInfo;

    /**
     * Node factory for the output language.
     */
    protected final JL7NodeFactory nf;

    /**
     * Quasiquoter for the output language.
     */
    protected final QQ qq;

    /**
     * Type system for the output language.
     */
    protected final TypeSystem ts;

    /**
     * {@link ExtFactory} in the output type system.
     */
    protected final ClassType baseExtFactoryCT;

    /**
     * {@link Node} in the source type system.
     */
    protected final ClassType nodeCT;

    /**
     * The ExtFactory interface being extended (in the source type system).
     */
    protected final ClassType superExtFactoryCT;

    public ExtFactoryGenerator(ExtensionInfo efgExtInfo,
            JL7ExtensionInfo outExtInfo) throws SemanticException {
        this.efgExtInfo = efgExtInfo;
        nf = (JL7NodeFactory) outExtInfo.nodeFactory();
        qq = new QQ(outExtInfo);
        ts = outExtInfo.typeSystem();
        baseExtFactoryCT = ts.typeForName(EXT_FACTORY_NAME).toClass();
        nodeCT = efgExtInfo.typeSystem().Node();
        superExtFactoryCT =
                efgExtInfo.typeSystem()
                          .typeForName(EFG_INFO.superInterface())
                          .toClass();
    }

    /**
     * @return the simple name of the ExtFactory interface being generated.
     */
    public String extFactorySimpleName() {
        return EFG_INFO.lang() + EXT_FACTORY_BASENAME;
    }

    /**
     * @return the fully qualified name of the ExtFactory interface being
     *         generated.
     */
    public String extFactoryFQName() {
        return EFG_INFO.packageName() + "." + extFactorySimpleName();
    }

    /**
     * @return the simple name of the AbstractExtFactory class being generated.
     */
    public String abstractExtFactorySimpleName() {
        return EFG_INFO.lang() + ABSTRACT_EXT_FACTORY_BASENAME;
    }

    /**
     * Generates the AST for an ExtFactory.
     */
    public SourceFile genExtFactory() {
        // Generate methods for the extension factory interface.
        List<ClassMember> decls = new ArrayList<>();
        for (Map.Entry<ClassType, EfgClassInfo> entry : EFG_INFO.factoryMappings()
                                                                .entrySet()) {
            // The node implementation for which the method is being generated.
            ClassType ct = entry.getKey();

            for (Name basename : entry.getValue().basenames().keySet()) {
                MethodDecl md =
                        (MethodDecl) qq.parseMember(EXT_NAME + " %s ();",
                                                    efgExtInfo.factoryName(basename.name));

                // Create a Javadoc comment for the method decl.
                StringBuilder comment = new StringBuilder();
                comment.append("/**\n");
                comment.append(" * Creates an extension object for {@link "
                        + ct.fullName() + "}.\n");
                comment.append(" */");
                md = (MethodDecl) md.javadoc(nf.Javadoc(Position.compilerGenerated(),
                                                        comment.toString()));
                decls.add(md);
            }
        }

        // Create the interface declaration.
        ClassDecl cd = qq.parseDecl("public interface %s extends "
                + EFG_INFO.superInterface() + " {  }", extFactorySimpleName());
        cd = cd.body(nf.ClassBody(Position.compilerGenerated(), decls));

        StringBuilder sourceCode = new StringBuilder();
        List<Object> subst = new ArrayList<>();
        sourceCode.append("package " + EFG_INFO.packageName() + ";");

        SourceFile ast = qq.parseFile(sourceCode.toString(), subst.toArray());
        return ast.decls(Collections.<TopLevelDecl> singletonList(cd));
    }

    /**
     * Generates the AST for an AbstractExtFactory.
     *
     * @param className the name of the abstract class to generate.
     * @param interfaceName
     *         the name of the ExtFactory interface being implemented.
     */
    public SourceFile genAbstractExtFactory() {
        String className = abstractExtFactorySimpleName();
        List<ClassMember> decls = new ArrayList<>();

        // Generate default constructor.
        decls.add(qq.parseMember("public %s () { super(); }", className));

        // Generate constructor for extending extensions.
        decls.add(qq.parseMember("public %s (" + EXT_FACTORY_NAME
                + " nextExtFactory) { super(nextExtFactory); }", className));

        List<ClassMember> factoryDecls = new ArrayList<>();
        List<ClassMember> implDecls = new ArrayList<>();
        List<ClassMember> postDecls = new ArrayList<>();

        // Generate methods for the abstract extension factory.
        for (Map.Entry<ClassType, EfgClassInfo> entry : EFG_INFO.factoryMappings()
                                                                .entrySet()) {
            ClassType ct = entry.getKey();
            EfgClassInfo classInfo = entry.getValue();
            ClassType baseCT = classInfo.superType();
            Map<Name, Name> basenames = classInfo.basenames();

            for (Map.Entry<Name, Name> bnEntry : basenames.entrySet()) {
                String basename = bnEntry.getKey().name;
                String delegate = bnEntry.getValue().name;

                // Generate the final methods (named extBasename()).
                factoryDecls.add(genFactoryMethod(ct, basename, baseCT));

                // Generate the impl methods (ones named extBasenameImpl()).
                implDecls.add(genImplMethod(basename, delegate));

                // Generate the post-processing methods (ones named postExtBasename()).
                postDecls.add(genPostMethod(basename, delegate));
            }
        }

        decls.addAll(factoryDecls);
        decls.addAll(implDecls);
        decls.addAll(postDecls);

        // Create the class declaration.
        ClassDecl cd = qq.parseDecl("public abstract class %s extends "
                + EFG_INFO.superClass() + " implements " + extFactoryFQName()
                + " {  }", abstractExtFactorySimpleName());
        cd = cd.body(nf.ClassBody(Position.compilerGenerated(), decls));

        StringBuilder sourceCode = new StringBuilder();
        List<Object> subst = new ArrayList<>();
        sourceCode.append("package " + EFG_INFO.packageName() + ";");

        SourceFile ast = qq.parseFile(sourceCode.toString(), subst.toArray());
        return ast.decls(Collections.<TopLevelDecl> singletonList(cd));
    }

    /**
     * Creates a factory method (ones named extBasename()).
     *
     * @param ct
     *         the node class type for which the factory method is to be
     *         generated
     * @param basename the basename for the factory method.
     * @param baseCT
     *         an ancestor class of ct for which there is an extension factory
     *         method in the Polyglot base language.
     */
    protected MethodDecl genFactoryMethod(ClassType ct, String basename,
            ClassType baseCT) {
        StringBuilder methodDecl = new StringBuilder();
        List<Object> subst = new ArrayList<>();

        methodDecl.append("public final " + EXT_NAME + " %s () { ");
        subst.add(efgExtInfo.factoryName(basename));
        {
            // Call the actual implementation for this language.
            methodDecl.append(EXT_NAME + " e = %s (); ");
            subst.add(implName(basename));

            // Call the next extension's factory, if any.
            methodDecl.append("if (nextExtFactory() != null) { ");
            {
                // Need to do a dynamic type check.
                methodDecl.append(EXT_NAME + " e2; ");
                methodDecl.append("if (nextExtFactory() instanceof "
                        + extFactoryFQName() + ") { ");
                {
                    methodDecl.append("e2 = ((" + extFactoryFQName()
                            + ") nextExtFactory()). %s (); ");
                    subst.add(efgExtInfo.factoryName(basename));
                }
                methodDecl.append("} else { ");
                {
                    methodDecl.append("e2 = nextExtFactory(). %s (); ");
                    subst.add(efgExtInfo.defaultFactoryBasename(baseCT));
                }
                methodDecl.append("} ");

                // Compose the extensions.
                methodDecl.append("e = composeExts(e, e2); ");
            }
            methodDecl.append("} ");

            // Post-process.
            methodDecl.append("return %s(e); ");
            subst.add(postName(basename));
        }
        methodDecl.append("} ");

        // Actual method decl.
        MethodDecl md = (MethodDecl) qq.parseMember(methodDecl.toString(),
                                                    subst.toArray());

        // Annotate with @Override annotation.
        AnnotationElem overrideAnnotation =
                nf.MarkerAnnotationElem(Position.compilerGenerated(),
                                        qq.parseType(Override.class.getName()));
        List<AnnotationElem> annotationElems =
                Collections.singletonList(overrideAnnotation);
        JL5MethodDeclExt mdExt = (JL5MethodDeclExt) JL5Ext.ext(md);
        return (MethodDecl) mdExt.annotationElems(annotationElems);
    }

    /**
     * Creates an impl method (ones named extBasenameImpl()).
     */
    protected MethodDecl genImplMethod(String basename, String delegate) {
        StringBuilder methodDecl = new StringBuilder();
        List<Object> subst = new ArrayList<>();

        methodDecl.append("protected " + EXT_NAME + " %s () { ");
        subst.add(implName(basename));
        {
            methodDecl.append("return %s (); ");
            subst.add(implName(delegate));
        }

        methodDecl.append("}");

        // Actual method decl.
        return (MethodDecl) qq.parseMember(methodDecl.toString(),
                                           subst.toArray());
    }

    /**
     * Creates a post-processing method (ones named postExtBasename()).
     */
    protected MethodDecl genPostMethod(String basename, String delegate) {
        StringBuilder methodDecl = new StringBuilder();
        List<Object> subst = new ArrayList<>();

        methodDecl.append("protected " + EXT_NAME + " %s (" + EXT_NAME
                + " ext) { ");
        subst.add(postName(basename));
        {
            methodDecl.append("return %s (ext); ");
            subst.add(postName(delegate));
        }

        methodDecl.append("}");

        // Actual method decl.
        return (MethodDecl) qq.parseMember(methodDecl.toString(),
                                           subst.toArray());
    }

    /**
     * @return the name of the impl factory method for the given basename.
     */
    protected String implName(String basename) {
        return efgExtInfo.factoryName(basename) + "Impl";
    }

    /**
     * @return the name of the post-factory method for the given basename.
     */
    protected String postName(String basename) {
        return "post"
                + efgExtInfo.capitalizeFirst(efgExtInfo.factoryName(basename));
    }
}
