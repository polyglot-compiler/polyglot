SOURCE = ../..
PACKAGE = jltools.frontend
SUBDIRS =

SOURCES = \
	ClassTypeJob.java \
	Compiler.java \
	ExtensionInfo.java \
	Job.java \
	SourceJob.java \
	StandardExtensionInfo.java \
	Target.java \
	TargetFactory.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber: cleanclasses
