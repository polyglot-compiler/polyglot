SOURCE = ../..
PACKAGE = jltools/ext

include $(SOURCE)/Rules.mk

SUBDIRS = jl ${EXT}

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
