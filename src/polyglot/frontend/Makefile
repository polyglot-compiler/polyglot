SOURCE = ../..
PACKAGE = jltools/frontend
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	ClassTypeJob.java \
	Compiler.java \
	ExtensionInfo.java \
	Job.java \
	SourceJob.java \
	StandardExtensionInfo.java \
	Target.java \
	TargetFactory.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
