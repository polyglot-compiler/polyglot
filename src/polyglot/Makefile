SOURCE = ..
PACKAGE = jltools
SUBDIRS = util/jlgen ext/jl lex parse main frontend ast types visit util ext

include $(SOURCE)/Rules.mk

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
