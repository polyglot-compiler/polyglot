SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/runtime
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Byte.java \
	Primitive.java \
	Boolean.java \
	Character.java \
	Double.java \
	Float.java \
	Integer.java \
	Long.java \
	Short.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = pao.jar paort.jar

all: $(CLASSES)

clean clobber: cleanclasses

