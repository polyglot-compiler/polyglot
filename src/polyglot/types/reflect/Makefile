SOURCE = ../../..
PACKAGE = polyglot/types/reflect
SUBDIRS =
JAR_FILE = polyglot.jar

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

all: $(CLASSES)

JAR_FILE = polyglot.jar

jar: all
	$(jar)

release: release_src

clean clobber: cleanclasses
