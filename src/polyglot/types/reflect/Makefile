SOURCE = ../../..
PACKAGE = polyglot/types/reflect
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Attribute.java \
	ClassFile.java \
	ClassFileLoader.java \
	ClassPathLoader.java \
	Constant.java \
	ConstantValue.java \
	Exceptions.java \
	Field.java \
	InnerClasses.java \
	JLCInfo.java \
	Method.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)

JAR_FILE = polyglot.jar

clean clobber: cleanclasses

