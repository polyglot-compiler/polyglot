SOURCE = ..
PACKAGE = jltools
SUBDIRS = util/jlgen ext lex parse main frontend ast types visit util ext/jif/runtime

include $(SOURCE)/Rules.mk

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
