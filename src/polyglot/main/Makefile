SOURCE = ../..
PACKAGE = jltools.main
SUBDIRS =

SOURCES = \
	Main.java \
	MainTargetFactory.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber:
	-rm -f *.class
