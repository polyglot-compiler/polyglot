SOURCE = ..
PACKAGE = polyglot
SUBDIRS = util/ppg ext/jl lex parse main frontend ast types visit util ext

include $(SOURCE)/Rules.mk

all clean clobber jar:
	$(subdirs)

release: release_src
	$(subdirs)
