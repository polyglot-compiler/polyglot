SOURCE = ../..
PACKAGE = jltools/frontend
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Compiler.java \
	ExtensionInfo.java \
	AbstractExtensionInfo.java \
	AbstractPass.java \
	BarrierPass.java \
	CupParser.java \
	EmptyPass.java \
	InnerJob.java \
	JobExt.java \
	Job.java \
	OutputPass.java \
	Parser.java \
	ParserPass.java \
	Pass.java \
	PrettyPrintPass.java \
	PrettyPrintPass.java \
	Source.java \
	SourceJob.java \
	SourceLoader.java \
	SpawnPass.java \
	TargetFactory.java \
	VisitorPass.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
