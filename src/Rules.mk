#
# Makefile to build the jltools source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#

# set up some reasonable defaults (for building in CUCS)
include $(SOURCE)/Defs.mk

.SUFFIXES: .class .java

JC_FLAGS 		= -g -d $(OUTPUT) $(JAVAC_PATHS)
RMIC_FLAGS		= -g -d $(OUTPUT) -classpath $(CLASSPATH)

JAR_FILE		= jltools.jar
JAR_FLAGS		= cf 

JAVADOC_MAIN		= com.sun.tools.javadoc.Main
JAVADOC_DOCLET		= iContract.doclet.Standard
JAVADOC_OUTPUT		= $(SOURCE)/javadoc
JAVADOC_FLAGS		= -mx40m -ms40m -classpath "$(JAVADOC_CLASSPATH)"

SOURCEPATH		= $(SOURCE)
BIN 			= $(SOURCE)
PACKAGEPATH		= $(SOURCE)/classes/$(PACKAGE)
VPATH			= $(PACKAGEPATH)

all clean clobber javadoc:

$(PACKAGEPATH)/%.class: %.java
	$(JC) $(JC_FLAGS) $<

cleanclasses:
	-rm -f $(PACKAGEPATH)/*.class

define subdirs
@for i in $(SUBDIRS) ""; do \
    if [ "x$$i" != "x" ]; then $(MAKE) -C $$i $@ || exit 1; fi; \
done
endef

define javadoc
-rm -rf $(JAVADOC_OUTPUT)
-mkdir -p $(JAVADOC_OUTPUT)
"$(JAVA)" "$(JAVADOC_FLAGS)" $(JAVADOC_MAIN) \
	-d $(JAVADOC_OUTPUT) \
	-doclet $(JAVADOC_DOCLET) \
	-sourcepath $(SOURCEPATH) \
	-classpath "$(CLASSPATH)" $(PACKAGES)
endef

define yacc
awk 'BEGIN {FS = "\n"; s = 1} {print $$1, "\t // ", s++}' $< | \
	"$(JAVA)" -classpath "$(CLASSPATH)" java_cup.Main -parser Grm
endef
