SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/parse
SUBDIRS = 
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	Parser.java \
	Constant.java \
	ParserError.java \
	Unparse.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

Parser.java Constant.java: ppg.cup
	java -cp $(CUP_RUNTIME) java_cup.Main -parser Parser -symbols Constant < ppg.cup

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	-rm -f Constant.java Parser.java
	$(subdirs)

