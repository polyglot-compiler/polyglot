SOURCE = ..
PACKAGE = jltools
SUBDIRS = util ext lex parse main frontend ast types visit ext/jif/runtime

include $(SOURCE)/Rules.mk

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
