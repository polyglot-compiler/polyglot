#
# Makefile to build the jltools source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#


# set up some reasonable defaults (for building in CUCS)
JC 			= javac
JAVA			= java
CUP_RUNTIME		= /home/nks/lib
CLASSPATH		= -classpath $(SOURCE):/usr/local/jdk1.2.1/jre/lib/rt.jar:$(CUP_RUNTIME)
JC_FLAGS 		= $(CLASSPATH)

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
all: util types lex parse ast frontend

#include all of our package makefiles. they give us what class files are in each.
include jltools/ast/Makefile 
include jltools/frontend/Makefile
include jltools/lex/Makefile
include jltools/util/Makefile
include jltools/types/Makefile
include jltools/parse/Makefile

#other targets:
util: $(UTIL_TARGET) 

types: util $(TYPES_TARGET)

lex: util types $(LEX_TARGET)

parse: util types lex $(PARSE_TARGET)

ast : util types lex  $(AST_TARGET)

frontend: util types lex parse ast $(FRONTEND_TARGET)

#clean: (just delete the class files)
clean: 
	rm -f jltools/ast/*.class
	rm -f jltools/frontend/*.class
	rm -f jltools/parse/*.class
	rm -f jltools/types/*.class
	rm -f jltools/util/*.class
	rm -f jltools/lex/*.class

# delete class files as well as the grammar files, so that we can regenerate them
clobber superclean: clean
	rm -f jltools/parse/Grm.java
	rm -f jltools/parse/sym.java
