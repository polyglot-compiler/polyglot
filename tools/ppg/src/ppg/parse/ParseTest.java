package jltools.util.jlgen.parse;

import java.io.*;
import jltools.util.jlgen.*;
import jltools.util.jlgen.lex.*;
import jltools.util.jlgen.spec.*;
import jltools.util.jlgen.util.*;

public class ParseTest
{
	private static final String HEADER = "jlgen [parsetest]: ";
	
	private ParseTest() {}

	public static void main(String args[]) {
		FileInputStream fileInput;

		String filename = null;
		try {
			filename = args[0];
			fileInput = new FileInputStream(filename);
		}
		catch (FileNotFoundException e) {
			System.out.println("Error: "+filename+" is not found.");
			return;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(HEADER+"Error: No file name given.");
			return;
		}

		File f = new File(filename);
		String simpleName = f.getName();
		Lexer lex = new Lexer(fileInput, simpleName);
		
		Parser parser = new Parser(filename, lex);
		try {
			parser.parse();
		} catch (Exception e) {
			System.out.println(HEADER+"Exception: "+e.getMessage());
			return;
		}
		Spec spec = (Spec)parser.getProgramNode();
					
		CodeWriter cw = new CodeWriter(System.out, 72); 
		try {
			spec.unparse(cw);
			cw.flush();
			fileInput.close();
		} catch (IOException e) {
			System.out.println(HEADER+"exception: "+e.getMessage());
			return;
		} catch (ParserError e) {
			System.out.println(HEADER+"parser error: "+e.getMessage());
			return;
		}
	}

}