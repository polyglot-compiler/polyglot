SOURCE = ../../../..
PACKAGE = polyglot/ext/coffer/extension
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AssignDel_c.java \
	AssignExt_c.java \
	LocalExt_c.java \
	SpecialExt_c.java \
	ProcedureCallExt_c.java \
	FreeExt_c.java \
	LocalDeclExt_c.java \
	NewExt_c.java \
	ProcedureDeclExt_c.java \
	CofferExt_c.java \
	CofferExt.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = coffer.jar

all: $(CLASSES)

clean clobber: cleanclasses

