SOURCE = ../..
PACKAGE = jltools/frontend
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	BarrierID.java \
	BarrierPass.java \
	Compiler.java \
	CompoundPass.java \
	CupParser.java \
	EmptyPass.java \
	ExtensionInfo.java \
	Job.java \
	Parser.java \
	ParserPass.java \
	PassID.java \
	Pass.java \
	Scheduler.java \
	Source.java \
	SourceLoader.java \
	StandardExtensionInfo.java \
	TargetFactory.java \
	VisitorPass.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
