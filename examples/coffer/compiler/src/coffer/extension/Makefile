SOURCE = ../../../..
PACKAGE = polyglot/ext/coffer/extension
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AssignDel_c.java \
	LocalDel_c.java \
	SpecialDel_c.java \
	CallDel_c.java \
	ConstructorCallDel_c.java \
	FreeDel_c.java \
	LocalDeclDel_c.java \
	NewDel_c.java \
	ProcedureDeclDel_c.java \
	CofferDel_c.java \
	CofferDel.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = coffer.jar

all: $(CLASSES)

clean clobber: cleanclasses

