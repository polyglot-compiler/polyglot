SOURCE = ..
PACKAGE = jltools
SUBDIRS = ext lex parse main frontend ast types util visit ext/jif/tests

include $(SOURCE)/Rules.mk

all clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)
