@echo off

rem LEXER
java -cp \michael\research\jltools\jlex.jar JFlex.Main multi.flex

rem PARSER
java -cp \michael\research\jltools\java_cup.jar;\michael\research\jltools\ jltools.util.jlgen.JLgen multi.jlg > out.cup
java -cp \michael\research\jltools\java_cup.jar java_cup.Main -parser Parser -symbols Constant < out.cup

rem COMPILE
javac -classpath \michael\research\jltools\java_cup.jar *.java

rem RUN
java -classpath .;\michael\research\jltools\java_cup.jar Tester expr.txt