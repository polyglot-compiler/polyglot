SOURCE = ../..
PACKAGE = jltools/frontend
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Compiler.java \
	AbstractPass.java \
	BarrierPass.java \
	CupParser.java \
	EmptyPass.java \
	ExtensionInfo.java \
	InnerJob.java \
	JobExt.java \
	Job.java \
	Parser.java \
	ParserPass.java \
	Pass.java \
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
