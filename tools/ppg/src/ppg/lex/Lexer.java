package jltools.util.jlgen.lex;
import java.io.InputStream;
import jltools.util.jlgen.parse.*;


public class Lexer {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

	private int lastId = -1;
	private String filename = "";
	private String lineSeparator;
	public Lexer(InputStream in, String filename) {
		this(in);
		this.filename = filename;
	}
	private void error(String message) throws LexicalError {
		throw new LexicalError(filename, yyline+1, message);
	}
	private Token t(int id, Object value) {
		lastId = id;
		return new Token(id, filename, yyline + 1, yychar, yychar + yylength(), value);
	}
	private Token t(int id) {
		return t(id, yytext());
	}
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yychar;
	private int yyline;
	private boolean yy_at_bol;
	private int yy_lexical_state;

	public Lexer (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	public Lexer (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Lexer () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yychar = 0;
		yyline = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;

    lineSeparator = System.getProperty("line.separator", "\n");
	}

	private boolean yy_eof_done = false;
	private final int CODE = 3;
	private final int STRING = 2;
	private final int YYINITIAL = 0;
	private final int COMMENT = 1;
	private final int yy_state_dtrans[] = {
		0,
		56,
		44,
		72
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
				++yyline;
			}
			if ('\r' == yy_buffer[i]) {
				++yyline;
				yy_last_was_cr=true;
			} else yy_last_was_cr=false;
		}
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NO_ANCHOR,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NO_ANCHOR,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NO_ANCHOR,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NO_ANCHOR,
		/* 50 */ YY_NOT_ACCEPT,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NO_ANCHOR,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NOT_ACCEPT,
		/* 57 */ YY_NO_ANCHOR,
		/* 58 */ YY_NOT_ACCEPT,
		/* 59 */ YY_NO_ANCHOR,
		/* 60 */ YY_NOT_ACCEPT,
		/* 61 */ YY_NO_ANCHOR,
		/* 62 */ YY_NOT_ACCEPT,
		/* 63 */ YY_NO_ANCHOR,
		/* 64 */ YY_NOT_ACCEPT,
		/* 65 */ YY_NO_ANCHOR,
		/* 66 */ YY_NOT_ACCEPT,
		/* 67 */ YY_NO_ANCHOR,
		/* 68 */ YY_NOT_ACCEPT,
		/* 69 */ YY_NO_ANCHOR,
		/* 70 */ YY_NOT_ACCEPT,
		/* 71 */ YY_NO_ANCHOR,
		/* 72 */ YY_NOT_ACCEPT,
		/* 73 */ YY_NO_ANCHOR,
		/* 74 */ YY_NOT_ACCEPT,
		/* 75 */ YY_NO_ANCHOR,
		/* 76 */ YY_NOT_ACCEPT,
		/* 77 */ YY_NO_ANCHOR,
		/* 78 */ YY_NOT_ACCEPT,
		/* 79 */ YY_NO_ANCHOR,
		/* 80 */ YY_NO_ANCHOR,
		/* 81 */ YY_NO_ANCHOR,
		/* 82 */ YY_NO_ANCHOR,
		/* 83 */ YY_NO_ANCHOR,
		/* 84 */ YY_NO_ANCHOR,
		/* 85 */ YY_NO_ANCHOR,
		/* 86 */ YY_NO_ANCHOR,
		/* 87 */ YY_NO_ANCHOR,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NO_ANCHOR,
		/* 91 */ YY_NO_ANCHOR,
		/* 92 */ YY_NO_ANCHOR,
		/* 93 */ YY_NO_ANCHOR,
		/* 94 */ YY_NO_ANCHOR,
		/* 95 */ YY_NO_ANCHOR,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NO_ANCHOR,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NO_ANCHOR,
		/* 100 */ YY_NO_ANCHOR,
		/* 101 */ YY_NO_ANCHOR,
		/* 102 */ YY_NO_ANCHOR,
		/* 103 */ YY_NO_ANCHOR,
		/* 104 */ YY_NO_ANCHOR,
		/* 105 */ YY_NO_ANCHOR,
		/* 106 */ YY_NO_ANCHOR,
		/* 107 */ YY_NO_ANCHOR,
		/* 108 */ YY_NO_ANCHOR,
		/* 109 */ YY_NO_ANCHOR,
		/* 110 */ YY_NO_ANCHOR,
		/* 111 */ YY_NO_ANCHOR,
		/* 112 */ YY_NO_ANCHOR,
		/* 113 */ YY_NO_ANCHOR,
		/* 114 */ YY_NO_ANCHOR,
		/* 115 */ YY_NO_ANCHOR,
		/* 116 */ YY_NO_ANCHOR,
		/* 117 */ YY_NO_ANCHOR,
		/* 118 */ YY_NO_ANCHOR,
		/* 119 */ YY_NO_ANCHOR,
		/* 120 */ YY_NO_ANCHOR,
		/* 121 */ YY_NO_ANCHOR,
		/* 122 */ YY_NO_ANCHOR,
		/* 123 */ YY_NO_ANCHOR,
		/* 124 */ YY_NO_ANCHOR,
		/* 125 */ YY_NO_ANCHOR,
		/* 126 */ YY_NO_ANCHOR,
		/* 127 */ YY_NO_ANCHOR,
		/* 128 */ YY_NO_ANCHOR,
		/* 129 */ YY_NO_ANCHOR,
		/* 130 */ YY_NO_ANCHOR,
		/* 131 */ YY_NO_ANCHOR,
		/* 132 */ YY_NO_ANCHOR,
		/* 133 */ YY_NO_ANCHOR,
		/* 134 */ YY_NO_ANCHOR,
		/* 135 */ YY_NO_ANCHOR,
		/* 136 */ YY_NO_ANCHOR,
		/* 137 */ YY_NO_ANCHOR,
		/* 138 */ YY_NO_ANCHOR,
		/* 139 */ YY_NO_ANCHOR,
		/* 140 */ YY_NO_ANCHOR,
		/* 141 */ YY_NO_ANCHOR,
		/* 142 */ YY_NO_ANCHOR,
		/* 143 */ YY_NO_ANCHOR,
		/* 144 */ YY_NO_ANCHOR,
		/* 145 */ YY_NO_ANCHOR,
		/* 146 */ YY_NO_ANCHOR,
		/* 147 */ YY_NO_ANCHOR,
		/* 148 */ YY_NO_ANCHOR,
		/* 149 */ YY_NO_ANCHOR,
		/* 150 */ YY_NO_ANCHOR,
		/* 151 */ YY_NO_ANCHOR,
		/* 152 */ YY_NO_ANCHOR,
		/* 153 */ YY_NO_ANCHOR,
		/* 154 */ YY_NO_ANCHOR,
		/* 155 */ YY_NO_ANCHOR,
		/* 156 */ YY_NO_ANCHOR,
		/* 157 */ YY_NO_ANCHOR,
		/* 158 */ YY_NO_ANCHOR,
		/* 159 */ YY_NO_ANCHOR,
		/* 160 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,130,
"44:9,45,42,44,45,42,44:18,43,36,35,36:7,32,36,26,36,25,46,34:10,22,24,36,23" +
",36:2,40,33:13,38,33:12,29,37,30,39,41,40,14,33,3,6,7,16,18,21,1,33,17,4,19" +
",2,11,12,33,10,15,9,5,13,20,8,33:2,27,31,28,40,44,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,161,
"0,1,2,3,1:4,4,1:6,5,6,1:2,7,8,1,6:20,9,1,10,1,11,1,12,1,13,14,15,16,17,18,1" +
"9,20,9,21,16,22,23,24,25,26,27,28,29,30,31,32,33,34,12,35,18,36,37,38,39,40" +
",41,42,43,44,45,46,47,48,49,50,6,51,52,53,54,55,56,57,58,59,60,61,62,63,64," +
"65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89," +
"90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,6,110" +
",111,112,113,114,115,116,117")[0];

	private int yy_nxt[][] = unpackFromString(118,47,
"1,2,89,140,148,152,153,154,152,51,155,156,157,152,158,159,152:4,160,152,3,4" +
",5,6,7,8,9,10,11,12,13,152,4,14,4:2,152,4:3,15:2,4,15,52,-1:48,152,90,152:1" +
"6,91,152:2,-1:11,152,92,-1:3,152,-1:2,92,-1:27,50,-1:46,17,-1:66,15:2,-1,15" +
",-1:2,152:21,-1:11,152,92,-1:3,152,-1:2,92,-1:6,19:41,-1,19:4,-1,152:8,117," +
"152:4,145,152:7,-1:11,152,92,-1:3,152,-1:2,92,-1:6,58:31,60,58:14,1,54:34,4" +
"5,54,46,54:4,-1,54,47:2,54,-1:2,54,-1:6,54,-1:24,64,54,-1,54:2,66,-1:2,68:2" +
",-1,68,-1:2,74:21,76,74:24,-1:23,21,-1:24,152:6,97,152:2,98,16,152:10,-1:11" +
",152,92,-1:3,152,-1:2,92,-1:37,18,-1:13,19,-1,62:31,60,62:13,43,-1,54:34,45" +
",54,46,54:4,-1,54,-1:2,54,-1,78:21,76,78:5,49,78:18,1,42:31,53,42:9,58,42:4" +
",-1,152,20,152:19,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:8,22,152:12,-1:11," +
"152,92,-1:3,152,-1:2,92,-1:6,152:6,23,152:14,-1:11,152,92,-1:3,152,-1:2,92," +
"-1:6,62:31,60,62:14,-1,152:8,24,152:12,-1:11,152,92,-1:3,152,-1:2,92,-1:39," +
"70,-1:13,152:11,25,152:9,-1:11,152,92,-1:3,152,-1:2,92,-1:6,54:21,-1:5,54:5" +
",-1,54,-1:3,54:5,-1:6,152,26,152:19,-1:11,152,92,-1:3,152,-1:2,92,-1:42,54," +
"-1:4,68:2,-1,68,-1:2,152:20,27,-1:11,152,92,-1:3,152,-1:2,92,-1:39,54,-1:13" +
",152:8,28,152:12,-1:11,152,92,-1:3,152,-1:2,92,-1:5,1,48:21,55,48:19,74,48:" +
"4,-1,152:8,29,152:12,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:8,30,152:12,-1:" +
"11,152,92,-1:3,152,-1:2,92,-1:6,152:5,31,152:15,-1:11,152,92,-1:3,152,-1:2," +
"92,-1:6,78:21,76,78:24,-1,152:9,32,152:11,-1:11,152,92,-1:3,152,-1:2,92,-1:" +
"6,152,33,152:19,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:6,34,152:14,-1:11,15" +
"2,92,-1:3,152,-1:2,92,-1:6,152:6,35,152:14,-1:11,152,92,-1:3,152,-1:2,92,-1" +
":6,152:2,36,152:18,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:3,37,152:17,-1:11" +
",152,92,-1:3,152,-1:2,92,-1:6,152:9,38,152:11,-1:11,152,92,-1:3,152,-1:2,92" +
",-1:6,152:6,39,152:14,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:6,40,152:14,-1" +
":11,152,92,-1:3,152,-1:2,92,-1:6,152:3,41,152:17,-1:11,152,92,-1:3,152,-1:2" +
",92,-1:6,152:10,57,152:10,-1:11,152,92,-1:3,152,-1:2,92,-1:6,59,152,104,152" +
":18,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:11,105,152:9,-1:11,152,92,-1:3,1" +
"52,-1:2,92,-1:6,152:5,61,152:15,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:15,6" +
"3,152:5,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:10,65,152:10,-1:11,152,92,-1" +
":3,152,-1:2,92,-1:6,152:8,149,152:12,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152" +
":9,106,152:11,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:13,107,152:7,-1:11,152" +
",92,-1:3,152,-1:2,92,-1:6,152:17,108,152:3,-1:11,152,92,-1:3,152,-1:2,92,-1" +
":6,152:6,109,152:14,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:2,111,152:6,112," +
"152:11,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:13,67,152:7,-1:11,152,92,-1:3" +
",152,-1:2,92,-1:6,152:8,69,152:12,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:3," +
"115,152:17,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:10,116,152:10,-1:11,152,9" +
"2,-1:3,152,-1:2,92,-1:6,152:18,146,152:2,-1:11,152,92,-1:3,152,-1:2,92,-1:6" +
",152,151,152:19,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:20,71,-1:11,152,92,-" +
"1:3,152,-1:2,92,-1:6,152:9,144,152:11,-1:11,152,92,-1:3,152,-1:2,92,-1:6,15" +
"2:2,119,152:18,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:16,120,152:4,-1:11,15" +
"2,92,-1:3,152,-1:2,92,-1:6,152:14,121,152:6,-1:11,152,92,-1:3,152,-1:2,92,-" +
"1:6,122,152:20,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:9,73,152:11,-1:11,152" +
",92,-1:3,152,-1:2,92,-1:6,152:4,123,152:16,-1:11,152,92,-1:3,152,-1:2,92,-1" +
":6,152:9,75,152:11,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:6,150,152:14,-1:1" +
"1,152,92,-1:3,152,-1:2,92,-1:6,152,77,152:19,-1:11,152,92,-1:3,152,-1:2,92," +
"-1:6,152:6,128,152:14,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:13,129,152:7,-" +
"1:11,152,92,-1:3,152,-1:2,92,-1:6,152:6,79,152:14,-1:11,152,92,-1:3,152,-1:" +
"2,92,-1:6,152:10,80,152:10,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:5,81,152:" +
"15,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:14,131,152:6,-1:11,152,92,-1:3,15" +
"2,-1:2,92,-1:6,152,132,152:19,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:15,133" +
",152:5,-1:11,152,92,-1:3,152,-1:2,92,-1:6,134,152:20,-1:11,152,92,-1:3,152," +
"-1:2,92,-1:6,152:5,135,152:15,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:17,82," +
"152:3,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:18,136,152:2,-1:11,152,92,-1:3" +
",152,-1:2,92,-1:6,152:10,83,152:10,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:1" +
"3,84,152:7,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:6,85,152:14,-1:11,152,92," +
"-1:3,152,-1:2,92,-1:6,152:5,86,152:15,-1:11,152,92,-1:3,152,-1:2,92,-1:6,15" +
"2:6,137,152:14,-1:11,152,92,-1:3,152,-1:2,92,-1:6,147,152:20,-1:11,152,92,-" +
"1:3,152,-1:2,92,-1:6,152,138,152:19,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:" +
"2,87,152:18,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:13,88,152:7,-1:11,152,92" +
",-1:3,152,-1:2,92,-1:6,152:10,93,152:10,-1:11,152,92,-1:3,152,-1:2,92,-1:6," +
"152:8,113,152:12,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:13,114,152:7,-1:11," +
"152,92,-1:3,152,-1:2,92,-1:6,152:6,110,152:14,-1:11,152,92,-1:3,152,-1:2,92" +
",-1:6,152:9,127,152:11,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:14,124,152:6," +
"-1:11,152,92,-1:3,152,-1:2,92,-1:6,125,152:20,-1:11,152,92,-1:3,152,-1:2,92" +
",-1:6,152,139,152:19,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:6,94,152:14,-1:" +
"11,152,92,-1:3,152,-1:2,92,-1:6,152:6,118,152:14,-1:11,152,92,-1:3,152,-1:2" +
",92,-1:6,152:9,130,152:11,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:14,126,152" +
":6,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:9,95,152:11,-1:11,152,92,-1:3,152" +
",-1:2,92,-1:6,152:7,96,152:13,-1:11,152,92,-1:3,152,-1:2,92,-1:6,99,152:20," +
"-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:12,100,152:8,-1:11,152,92,-1:3,152,-" +
"1:2,92,-1:6,152:9,143,152:3,101,152:7,-1:11,152,92,-1:3,152,-1:2,92,-1:6,15" +
"2:2,141,152:18,-1:11,152,92,-1:3,152,-1:2,92,-1:6,152:2,102,152:5,142,152:1" +
"2,-1:11,152,92,-1:3,152,-1:2,92,-1:6,103,152:20,-1:11,152,92,-1:3,152,-1:2," +
"92,-1:5");

	public Token getToken ()
		throws java.io.IOException, 
	LexicalError

		{
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

    return t(Constant.EOF, "EOF");
    //return Constant.EOF;
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:
						
					case -2:
						break;
					case 2:
						{ return t(Constant.ID, yytext().intern()); }
					case -3:
						break;
					case 3:
						{ return t(Constant.COLON); }
					case -4:
						break;
					case 4:
						{ 
	error("Invalid character: " + yytext());
}
					case -5:
						break;
					case 5:
						{ return t(Constant.SEMI); }
					case -6:
						break;
					case 6:
						{ return t(Constant.DOT); }
					case -7:
						break;
					case 7:
						{ return t(Constant.COMMA); }
					case -8:
						break;
					case 8:
						{ return t(Constant.LBRACE); }
					case -9:
						break;
					case 9:
						{ return t(Constant.RBRACE); }
					case -10:
						break;
					case 10:
						{ return t(Constant.LBRACK); }
					case -11:
						break;
					case 11:
						{ return t(Constant.RBRACK); }
					case -12:
						break;
					case 12:
						{ return t(Constant.BAR); }
					case -13:
						break;
					case 13:
						{ return t(Constant.STAR); }
					case -14:
						break;
					case 14:
						{yybegin(STRING);}
					case -15:
						break;
					case 15:
						{}
					case -16:
						break;
					case 16:
						{ return t(Constant.TO); }
					case -17:
						break;
					case 17:
						{ yybegin (CODE); }
					case -18:
						break;
					case 18:
						{ yybegin (COMMENT); }
					case -19:
						break;
					case 19:
						{}
					case -20:
						break;
					case 20:
						{ return t(Constant.NON); }
					case -21:
						break;
					case 21:
						{ return t(Constant.COLON_COLON_EQUALS); }
					case -22:
						break;
					case 22:
						{ return t(Constant.INIT); }
					case -23:
						break;
					case 23:
						{ return t(Constant.CODE); }
					case -24:
						break;
					case 24:
						{ return t(Constant.LEFT); }
					case -25:
						break;
					case 25:
						{ return t(Constant.DROP); }
					case -26:
						break;
					case 26:
						{ return t(Constant.SCAN); }
					case -27:
						break;
					case 27:
						{ return t(Constant.WITH); }
					case -28:
						break;
					case 28:
						{ return t(Constant.RIGHT); }
					case -29:
						break;
					case 29:
						{ return t(Constant.START); }
					case -30:
						break;
					case 30:
						{ return t(Constant.IMPORT); }
					case -31:
						break;
					case 31:
						{ return t(Constant.EXTEND); }
					case -32:
						break;
					case 32:
						{ return t(Constant.PARSER); }
					case -33:
						break;
					case 33:
						{ return t(Constant.ACTION); }
					case -34:
						break;
					case 34:
						{ return t(Constant.INCLUDE); }
					case -35:
						break;
					case 35:
						{ return t(Constant.PACKAGE); }
					case -36:
						break;
					case 36:
						{ return t(Constant.NONASSOC); }
					case -37:
						break;
					case 37:
						{ return t(Constant.TERMINAL); }
					case -38:
						break;
					case 38:
						{ return t(Constant.TRANSFER); }
					case -39:
						break;
					case 39:
						{ return t(Constant.OVERRIDE); }
					case -40:
						break;
					case 40:
						{ return t(Constant.PRECEDENCE); }
					case -41:
						break;
					case 41:
						{ return t(Constant.NONTERMINAL); }
					case -42:
						break;
					case 42:
						{
	error("Illegal comment");
}
					case -43:
						break;
					case 43:
						{ yybegin (YYINITIAL); }
					case -44:
						break;
					case 44:
						{
	error("Unclosed string literal");
}
					case -45:
						break;
					case 45:
						{ 
	yybegin(YYINITIAL);
	String literal = yytext();
	return t(Constant.STRING_CONST, literal.substring(0, literal.length()-1));
}
					case -46:
						break;
					case 46:
						{
	error("Illegal escape character");
}
					case -47:
						break;
					case 47:
						{
	error("Illegal character in string literal: " + yytext());
}
					case -48:
						break;
					case 48:
						{
	error("Invalid character in code block: '" + yytext() + "'");
}
					case -49:
						break;
					case 49:
						{
	yybegin(YYINITIAL);
	String codeStr = yytext();
	// cut off ":}" from the end of the code string
	return t(Constant.CODE_STR, codeStr.substring(0, codeStr.length()-2));
}
					case -50:
						break;
					case 51:
						{ return t(Constant.ID, yytext().intern()); }
					case -51:
						break;
					case 52:
						{ 
	error("Invalid character: " + yytext());
}
					case -52:
						break;
					case 53:
						{
	error("Illegal comment");
}
					case -53:
						break;
					case 54:
						{
	error("Unclosed string literal");
}
					case -54:
						break;
					case 55:
						{
	error("Invalid character in code block: '" + yytext() + "'");
}
					case -55:
						break;
					case 57:
						{ return t(Constant.ID, yytext().intern()); }
					case -56:
						break;
					case 59:
						{ return t(Constant.ID, yytext().intern()); }
					case -57:
						break;
					case 61:
						{ return t(Constant.ID, yytext().intern()); }
					case -58:
						break;
					case 63:
						{ return t(Constant.ID, yytext().intern()); }
					case -59:
						break;
					case 65:
						{ return t(Constant.ID, yytext().intern()); }
					case -60:
						break;
					case 67:
						{ return t(Constant.ID, yytext().intern()); }
					case -61:
						break;
					case 69:
						{ return t(Constant.ID, yytext().intern()); }
					case -62:
						break;
					case 71:
						{ return t(Constant.ID, yytext().intern()); }
					case -63:
						break;
					case 73:
						{ return t(Constant.ID, yytext().intern()); }
					case -64:
						break;
					case 75:
						{ return t(Constant.ID, yytext().intern()); }
					case -65:
						break;
					case 77:
						{ return t(Constant.ID, yytext().intern()); }
					case -66:
						break;
					case 79:
						{ return t(Constant.ID, yytext().intern()); }
					case -67:
						break;
					case 80:
						{ return t(Constant.ID, yytext().intern()); }
					case -68:
						break;
					case 81:
						{ return t(Constant.ID, yytext().intern()); }
					case -69:
						break;
					case 82:
						{ return t(Constant.ID, yytext().intern()); }
					case -70:
						break;
					case 83:
						{ return t(Constant.ID, yytext().intern()); }
					case -71:
						break;
					case 84:
						{ return t(Constant.ID, yytext().intern()); }
					case -72:
						break;
					case 85:
						{ return t(Constant.ID, yytext().intern()); }
					case -73:
						break;
					case 86:
						{ return t(Constant.ID, yytext().intern()); }
					case -74:
						break;
					case 87:
						{ return t(Constant.ID, yytext().intern()); }
					case -75:
						break;
					case 88:
						{ return t(Constant.ID, yytext().intern()); }
					case -76:
						break;
					case 89:
						{ return t(Constant.ID, yytext().intern()); }
					case -77:
						break;
					case 90:
						{ return t(Constant.ID, yytext().intern()); }
					case -78:
						break;
					case 91:
						{ return t(Constant.ID, yytext().intern()); }
					case -79:
						break;
					case 92:
						{ return t(Constant.ID, yytext().intern()); }
					case -80:
						break;
					case 93:
						{ return t(Constant.ID, yytext().intern()); }
					case -81:
						break;
					case 94:
						{ return t(Constant.ID, yytext().intern()); }
					case -82:
						break;
					case 95:
						{ return t(Constant.ID, yytext().intern()); }
					case -83:
						break;
					case 96:
						{ return t(Constant.ID, yytext().intern()); }
					case -84:
						break;
					case 97:
						{ return t(Constant.ID, yytext().intern()); }
					case -85:
						break;
					case 98:
						{ return t(Constant.ID, yytext().intern()); }
					case -86:
						break;
					case 99:
						{ return t(Constant.ID, yytext().intern()); }
					case -87:
						break;
					case 100:
						{ return t(Constant.ID, yytext().intern()); }
					case -88:
						break;
					case 101:
						{ return t(Constant.ID, yytext().intern()); }
					case -89:
						break;
					case 102:
						{ return t(Constant.ID, yytext().intern()); }
					case -90:
						break;
					case 103:
						{ return t(Constant.ID, yytext().intern()); }
					case -91:
						break;
					case 104:
						{ return t(Constant.ID, yytext().intern()); }
					case -92:
						break;
					case 105:
						{ return t(Constant.ID, yytext().intern()); }
					case -93:
						break;
					case 106:
						{ return t(Constant.ID, yytext().intern()); }
					case -94:
						break;
					case 107:
						{ return t(Constant.ID, yytext().intern()); }
					case -95:
						break;
					case 108:
						{ return t(Constant.ID, yytext().intern()); }
					case -96:
						break;
					case 109:
						{ return t(Constant.ID, yytext().intern()); }
					case -97:
						break;
					case 110:
						{ return t(Constant.ID, yytext().intern()); }
					case -98:
						break;
					case 111:
						{ return t(Constant.ID, yytext().intern()); }
					case -99:
						break;
					case 112:
						{ return t(Constant.ID, yytext().intern()); }
					case -100:
						break;
					case 113:
						{ return t(Constant.ID, yytext().intern()); }
					case -101:
						break;
					case 114:
						{ return t(Constant.ID, yytext().intern()); }
					case -102:
						break;
					case 115:
						{ return t(Constant.ID, yytext().intern()); }
					case -103:
						break;
					case 116:
						{ return t(Constant.ID, yytext().intern()); }
					case -104:
						break;
					case 117:
						{ return t(Constant.ID, yytext().intern()); }
					case -105:
						break;
					case 118:
						{ return t(Constant.ID, yytext().intern()); }
					case -106:
						break;
					case 119:
						{ return t(Constant.ID, yytext().intern()); }
					case -107:
						break;
					case 120:
						{ return t(Constant.ID, yytext().intern()); }
					case -108:
						break;
					case 121:
						{ return t(Constant.ID, yytext().intern()); }
					case -109:
						break;
					case 122:
						{ return t(Constant.ID, yytext().intern()); }
					case -110:
						break;
					case 123:
						{ return t(Constant.ID, yytext().intern()); }
					case -111:
						break;
					case 124:
						{ return t(Constant.ID, yytext().intern()); }
					case -112:
						break;
					case 125:
						{ return t(Constant.ID, yytext().intern()); }
					case -113:
						break;
					case 126:
						{ return t(Constant.ID, yytext().intern()); }
					case -114:
						break;
					case 127:
						{ return t(Constant.ID, yytext().intern()); }
					case -115:
						break;
					case 128:
						{ return t(Constant.ID, yytext().intern()); }
					case -116:
						break;
					case 129:
						{ return t(Constant.ID, yytext().intern()); }
					case -117:
						break;
					case 130:
						{ return t(Constant.ID, yytext().intern()); }
					case -118:
						break;
					case 131:
						{ return t(Constant.ID, yytext().intern()); }
					case -119:
						break;
					case 132:
						{ return t(Constant.ID, yytext().intern()); }
					case -120:
						break;
					case 133:
						{ return t(Constant.ID, yytext().intern()); }
					case -121:
						break;
					case 134:
						{ return t(Constant.ID, yytext().intern()); }
					case -122:
						break;
					case 135:
						{ return t(Constant.ID, yytext().intern()); }
					case -123:
						break;
					case 136:
						{ return t(Constant.ID, yytext().intern()); }
					case -124:
						break;
					case 137:
						{ return t(Constant.ID, yytext().intern()); }
					case -125:
						break;
					case 138:
						{ return t(Constant.ID, yytext().intern()); }
					case -126:
						break;
					case 139:
						{ return t(Constant.ID, yytext().intern()); }
					case -127:
						break;
					case 140:
						{ return t(Constant.ID, yytext().intern()); }
					case -128:
						break;
					case 141:
						{ return t(Constant.ID, yytext().intern()); }
					case -129:
						break;
					case 142:
						{ return t(Constant.ID, yytext().intern()); }
					case -130:
						break;
					case 143:
						{ return t(Constant.ID, yytext().intern()); }
					case -131:
						break;
					case 144:
						{ return t(Constant.ID, yytext().intern()); }
					case -132:
						break;
					case 145:
						{ return t(Constant.ID, yytext().intern()); }
					case -133:
						break;
					case 146:
						{ return t(Constant.ID, yytext().intern()); }
					case -134:
						break;
					case 147:
						{ return t(Constant.ID, yytext().intern()); }
					case -135:
						break;
					case 148:
						{ return t(Constant.ID, yytext().intern()); }
					case -136:
						break;
					case 149:
						{ return t(Constant.ID, yytext().intern()); }
					case -137:
						break;
					case 150:
						{ return t(Constant.ID, yytext().intern()); }
					case -138:
						break;
					case 151:
						{ return t(Constant.ID, yytext().intern()); }
					case -139:
						break;
					case 152:
						{ return t(Constant.ID, yytext().intern()); }
					case -140:
						break;
					case 153:
						{ return t(Constant.ID, yytext().intern()); }
					case -141:
						break;
					case 154:
						{ return t(Constant.ID, yytext().intern()); }
					case -142:
						break;
					case 155:
						{ return t(Constant.ID, yytext().intern()); }
					case -143:
						break;
					case 156:
						{ return t(Constant.ID, yytext().intern()); }
					case -144:
						break;
					case 157:
						{ return t(Constant.ID, yytext().intern()); }
					case -145:
						break;
					case 158:
						{ return t(Constant.ID, yytext().intern()); }
					case -146:
						break;
					case 159:
						{ return t(Constant.ID, yytext().intern()); }
					case -147:
						break;
					case 160:
						{ return t(Constant.ID, yytext().intern()); }
					case -148:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
