import java.io.*;

public class Tester {

	public Tester() {}

	public static void main (String args[]) {
		FileInputStream fileInput;
		String filename="";
		try {
			filename = args[0];
			fileInput = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			System.out.println("Error: "+filename+" is not found.");
			return;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Error: No file name given.");
			return;
		}

		File file = new File(filename);
		String simpleName = file.getName(); 
		Lexer lex = new Lexer(fileInput, simpleName);
		
		Parser parser = new Parser(filename, lex);
		Expr e1 = null, e2 = null;
		try {
			e1 = (Expr) parser.parse_expr();
			//e2 = (Expr) parser.parse_expr();
		} catch (Exception e) {
			System.out.println("Exception: "+e.getMessage());
			return;
		}

		System.out.println("e1: "+e1.toString());
		System.out.println("e2: "+e2.toString());
	}

}