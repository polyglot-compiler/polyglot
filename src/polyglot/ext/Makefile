SOURCE = ../..
PACKAGE = jltools/ext

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
op: jl FORCE
	$(MAKE) -C $@ all
carray: jl FORCE
	$(MAKE) -C $@ all
covarRet: jl FORCE
	$(MAKE) -C $@ all
carcov: carray covarRet FORCE
	$(MAKE) -C $@ all
mixj: polyj FORCE
	$(MAKE) -C $@ all
covop: op covarRet FORCE
	$(MAKE) -C $@ all
covpolyj: polyj covarRet FORCE
	$(MAKE) -C $@ all

FORCE:
