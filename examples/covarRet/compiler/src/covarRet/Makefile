SOURCE = ../../..
PACKAGE = polyglot/ext/covarRet
#SUBDIRS = runtime
JAR_FILE = covarRet.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Version.java \
	CovarRetTypeSystem.java \
	CovarRetRewriter.java \
	CovarRetMethodInstance_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)

release: release_src
