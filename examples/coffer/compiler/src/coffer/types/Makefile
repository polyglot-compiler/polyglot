SOURCE = ../../../..
PACKAGE = polyglot/ext/coffer/types
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	CofferTypeSystem_c.java \
	CofferTypeSystem.java \
	InstKey_c.java \
	InstKey.java \
	Key_c.java \
	Key.java \
	KeySet_c.java \
	KeySet.java \
	ParamKey_c.java \
	ParamKey.java \
	ThrowConstraint_c.java \
	ThrowConstraint.java \
	UnknownKey_c.java \
	UnknownKey.java \
	CofferClassType.java \
	CofferConstructorInstance_c.java \
	CofferConstructorInstance.java \
	CofferContext_c.java \
	CofferContext.java \
	CofferMethodInstance_c.java \
	CofferMethodInstance.java \
	CofferParsedClassType_c.java \
	CofferParsedClassType.java \
	CofferProcedureInstance.java \
	CofferSubst_c.java \
	CofferSubstClassType_c.java \
	CofferSubst.java \
	CofferSubstType.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = coffer.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

