SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/runtime
SUBDIRS =
JAR_FILE = pao.jar paort.jar

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

all: $(CLASSES)

clean clobber: cleanclasses
