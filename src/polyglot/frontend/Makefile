SOURCE = ../..
PACKAGE = jltools/frontend
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AbstractPass.java \
	BarrierPass.java \
	Compiler.java \
	CupParser.java \
	EmptyPass.java \
	ExtensionInfo.java \
	InnerJob.java \
	Job.java \
	JobExt.java \
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
