SOURCE = ../..
PACKAGE = jltools/ext
SUBDIRS = jl jif polyj split jmatch op

include $(SOURCE)/Rules.mk

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
