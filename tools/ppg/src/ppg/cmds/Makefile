SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/cmds
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Command.java \
	DropCmd.java \
	ExtendCmd.java \
	NewProdCmd.java \
	OverrideCmd.java \
	TransferCmd.java \

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
