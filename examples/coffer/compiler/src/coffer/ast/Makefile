SOURCE = ../../../..
PACKAGE = polyglot/ext/coffer/ast
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	CofferNodeFactory_c.java \
	CofferNodeFactory.java \
	AmbKeySetNode_c.java \
	AmbKeySetNode.java \
	CanonicalKeySetNode_c.java \
	CanonicalKeySetNode.java \
	Free_c.java \
	Free.java \
	KeyNode_c.java \
	KeyNode.java \
	KeySetNode.java \
	ThrowConstraintNode_c.java \
	ThrowConstraintNode.java \
	TrackedTypeNode_c.java \
	TrackedTypeNode.java \
	CofferClassDecl_c.java \
	CofferClassDecl.java \
	CofferConstructorDecl_c.java \
	CofferConstructorDecl.java \
	CofferMethodDecl_c.java \
	CofferMethodDecl.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = coffer.jar

all: $(CLASSES)

clean clobber: cleanclasses

