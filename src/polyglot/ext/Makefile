SOURCE = ../..
PACKAGE = polyglot/ext
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SUBDIRS = jl ${EXT}

all: $(SUBDIRS)
	@chmod +x newext

clean clobber:
	$(subdirs)

release: release_src
	$(subdirs)

jl: FORCE
	$(MAKE) -C $@ all
jif: jl FORCE
	$(MAKE) -C $@ all
split: jif FORCE
	$(MAKE) -C $@ all
polyj: jl FORCE
	$(MAKE) -C $@ all
jmatch: jl FORCE
	$(MAKE) -C $@ all
pao: jl FORCE
	$(MAKE) -C $@ all
carray: jl FORCE
	$(MAKE) -C $@ all
covarRet: jl FORCE
	$(MAKE) -C $@ all
carcov: carray covarRet FORCE
	$(MAKE) -C $@ all
mixj: polyj FORCE
	$(MAKE) -C $@ all
covop: pao covarRet FORCE
	$(MAKE) -C $@ all
covpolyj: polyj covarRet FORCE
	$(MAKE) -C $@ all

FORCE:
