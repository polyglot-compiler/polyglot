SOURCE = ../../..
PACKAGE = jltools/types/reflect
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
	GenericAttribute.java \
	InnerClasses.java \
	JLCInfo.java \
	Method.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
