package polyglot.ext.jl5;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import javax.tools.FileObject;

import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5NodeFactory_c;
import polyglot.ext.jl5.parse.Grm;
import polyglot.ext.jl5.parse.Lexer_c;
import polyglot.ext.jl5.types.JL5TypeSystem_c;
import polyglot.ext.jl5.types.reflect.JL5ClassFile;
import polyglot.frontend.*;
import polyglot.main.Options;
import polyglot.qq.QQ;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyglot.util.ErrorQueue;

/**
 * Extension information for jl5 extension.
 */
public class ExtensionInfo extends JLExtensionInfo {

	@Override
	public String defaultFileExtension() {
		return "jl5";
	}

	@Override
	public String[] defaultFileExtensions() {
        String ext = defaultFileExtension();
        return new String[] { ext, "java" };
    }
	
	@Override
	public String compilerName() {
		return "jl5c";
	}

	@Override
	protected NodeFactory createNodeFactory() {
		return new JL5NodeFactory_c(new QQ(this));
	}

	@Override
	protected TypeSystem createTypeSystem() {
		return new JL5TypeSystem_c();
	}

	@Override
	public Scheduler createScheduler() {
		return new JL5Scheduler(this);
	}
	
	@Override
	protected Options createOptions() {
        return new JL5Options(this);
    }
	
	@Override
	public ClassFile createClassFile(FileObject classFileSource, byte[] code) throws IOException {
        return new JL5ClassFile(classFileSource, code, this);
    }

	/**
	 * Return a parser for <code>source</code> using the given
	 * <code>reader</code>.
	 */
	public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
		reader = new polyglot.lex.EscapedUnicodeReader(reader);

		polyglot.lex.Lexer lexer = new Lexer_c(reader, source, eq);
		polyglot.parse.BaseParser parser = new Grm(lexer, ts, nf, eq);

		return new CupParser(parser, source, eq);
	}

}
