SOURCE = ../..
PACKAGE = jltools/ext
SUBDIRS = jl jif split polyj jmatch op

include $(SOURCE)/Rules.mk

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
