SOURCE = ../..
PACKAGE = jltools/ext
SUBDIRS = jl polyj jif split jmatch

include $(SOURCE)/Rules.mk

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
