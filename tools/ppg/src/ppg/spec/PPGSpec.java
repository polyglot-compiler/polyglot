package jltools.util.jlgen.spec;

import java.io.*;
import java.util.*;
import jltools.util.jlgen.*;
import jltools.util.jlgen.cmds.*;
import jltools.util.jlgen.lex.*;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.util.*;

public class JLgenSpec extends Spec
{
	private String include;
	private Vector commands;
	private Spec parent;
	private final String HEADER = "jlgen [spec]: ";
	
	/**
	 * JLgen spec
	 */
	public JLgenSpec(String incFile, Vector cmds)
	{
		include = incFile;
		commands = cmds;
		parent = null;
	}
	
	/**
	 * Parse the chain of inheritance via include files
	 */
	public void parseChain() {
		if (include == null)
			return;
		
		try {
			FileInputStream fileInput = new FileInputStream(include);
			File f = new File(include);
			String simpleName = f.getName();
			Lexer lex = new Lexer(fileInput, simpleName);
			Parser parser = new Parser(simpleName, lex);
			JLgen.DEBUG("parsing "+simpleName);
			parser.parse();
			parent = (Spec)parser.getProgramNode();
			fileInput.close();
		} catch (Exception e) {
			System.out.println(HEADER+"Exception: "+e.getMessage());
			return;
		}
		parent.parseChain();
	}

	public void unparse(CodeWriter cw) throws ParserError {
		cw.begin(0);
		if (include != null) {
			cw.write(include+"\n");
		}
		if (commands != null) {
			for (int i=0; i < commands.size(); i++) {
				((Command)commands.elementAt(i)).unparse(cw);
			}
		}
		cw.end();
	}
}
