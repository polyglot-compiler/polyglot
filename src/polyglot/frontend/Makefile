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
	Job.java \
	Parser.java \
	ParserPass.java \
	Pass.java \
	Source.java \
	SourceLoader.java \
	TargetFactory.java \
	VisitorPass.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
