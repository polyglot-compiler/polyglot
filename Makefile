#
# Makefile to build the jltools source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#


# set up some reasonable defaults (for building in CUCS)
JC 			= javac
JAVA			= java
JAR			= jar
JAVADOC			= javadoc

CUP_RUNTIME		= /home/nks/lib
CLASSPATH		= $(SOURCE):/usr/local/jdk1.2.1/jre/lib/rt.jar:$(CUP_RUNTIME)
CLASSPATHFLAG		= -classpath $(CLASSPATH)
JC_FLAGS 		= $(CLASSPATHFLAG)

JAR_FILE		= jltools.jar
JAR_FLAGS		= cf 

JAVADOC_OUTPUT		= ./javadoc
JAVADOC_FLAGS		= $(CLASSPATHFLAG) 

SOURCE			= .
PERSONAL_MAKEFILE	= Makefile.personal

# allow users to overload the above by overriding the above settings.
-include $(PERSONAL_MAKEFILE)

# let the included makefiles know that we called them. otherwise, they'll try to 
# call here.
CALLED_FROM_PARENT 	= true

#implicit rule to build class files
%.class: %.java
	$(JC) $(JC_FLAGS) $<

#everything:
all: util types lex parse ast frontend visit runtime 

#include all of our package makefiles. they give us what class files are in each.
include jltools/ast/Makefile 
include jltools/frontend/Makefile
include jltools/lex/Makefile
include jltools/util/Makefile
include jltools/types/Makefile
include jltools/parse/Makefile
include jltools/visit/Makefile
include jltools/runtime/Makefile

#other targets:
util: $(UTIL_TARGET) 

types: util runtime $(TYPES_TARGET)

lex: util types $(LEX_TARGET)

parse: util types lex $(PARSE_TARGET)

ast: util types lex runtime $(AST_TARGET)

visit: util types ast runtime $(VISIT_TARGET)

frontend: util types lex parse ast visit runtime $(FRONTEND_TARGET)

runtime: $(RUNTIME_TARGET)

#clean: (just delete the class files)
clean: 
	rm -f jltools/ast/*.class
	rm -f jltools/frontend/*.class
	rm -f jltools/parse/*.class
	rm -f jltools/types/*.class
	rm -f jltools/util/*.class
	rm -f jltools/lex/*.class
	rm -f jltools/visit/*.class
	rm -f jltools/runtime/*.class
	rm -f jltools/ast/*.html
	rm -f jltools/frontend/*.html
	rm -f jltools/parse/*.html
	rm -f jltools/types/*.html
	rm -f jltools/util/*.html
	rm -f jltools/lex/*.html
	rm -f jltools/visit/*.html
	rm -f jltools/runtime/*.html


# delete class files as well as the grammar files, so that we can regenerate them
# also delete the javadoc & jar file, if they exist
clobber superclean: clean
	rm -f jltools/parse/Grm.java
	rm -f jltools/parse/sym.java
	rm -f $(JAR_FILE)
	rm -rf $(JAVADOC_OUTPUT)

# create a jar file
jar: all
	$(JAR) $(JAR_FLAGS) $(JAR_FILE) jltools/*/*.class

docs:
	-mkdir -p $(JAVADOC_OUTPUT)
	$(JAVADOC) $(JAVADOC_FLAGS) -d $(JAVADOC_OUTPUT) jltools.ast jltools.frontend jltools.parse jltools.util jltools.types jltools.visit jltools.runtime

classpath: # type "eval `make classpath`" to set classpath
	@echo setenv CLASSPATH "$(CLASSPATH)"
