#
# Makefile to build the polyglot source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#

SOURCE = .
SUBDIRS = polyglot bin doc
TAG = RELEASE_0_9_0

include Rules.mk

all: init
	$(subdirs)

init: classes lib
	@chmod +x bin/*

classes:
	mkdir classes

lib:
	mkdir lib

clean:
	-rm -rf classes
	$(subdirs)

clobber:
	-rm -rf $(JAVADOC_OUTPUT)
	-rm -f $(JAR_FILE)
	$(subdirs)

javadoc:
	$(javadoc)

jar:
	@rm -f polyglot.jar
	@for i in $(EXT) ; do rm -f $${i}.jar $${i}rt.jar ; done
	$(MAKE) -C polyglot jar

export: new-manifest export-polyglot export-javadoc export-tar

new-manifest:
	rm -f manifest
	$(MAKE) manifest

export-polyglot:
	version=`head -1 README | awk '{print $$NF}' | sed 's%[^0-9\.]%%g'`; \
	rm -rf polyglot-$${version}-src; \
	mkdir polyglot-$${version}-src; \
	tar cf - `cat manifest | sed "s%^$(CURDIR)/%%"` | (cd polyglot-$${version}-src; tar xf -)

export-tar:
	version=`head -1 README | awk '{print $$NF}' | sed 's%[^0-9\.]%%g'`; \
	rm -f polyglot-$${version}-src.tar polyglot-$${version}-src.tar.gz; \
	tar cf polyglot-$${version}-src.tar polyglot-$${version}-src; \
	gzip polyglot-$${version}-src.tar

export-javadoc: javadoc
	version=`head -1 README | awk '{print $$NF}' | sed 's%[^0-9\.]%%g'`; \
	mv javadoc polyglot-$${version}-src

FORCE:

MANIFEST = Makefile LICENSE README Rules.mk configure java_cup.jar jlex.jar
