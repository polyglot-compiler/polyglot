
Parser.java: out.cup
	java -cp ~/research/jltools/java_cup.jar java_cup.Main -parser Parser -symbols Constant < out.cup

out.cup:	multi.jlg multi.cup
	~/research/jltools/bin/jlgen multi.jlg > out.cup

all:	Parser.java out.cup
