SOURCE = ../../..
PACKAGE = jltools/ext/covarRet
#SUBDIRS = runtime

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Version.java \
	CovarRetNodeFactory.java \
	CovarRetRewriter.java \
	CovarRetClassBody_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)

release: release_src
