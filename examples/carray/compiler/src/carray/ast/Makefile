SOURCE = ../../../..
PACKAGE = polyglot/ext/carray/ast
SUBDIRS =
JAR_FILE = carray.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	CarrayAssign_c.java \
	CarrayNodeFactory.java \
	CarrayNodeFactory_c.java \
	ConstArrayTypeNode.java \
	ConstArrayTypeNode_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

release: release_src
