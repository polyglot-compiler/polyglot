#
# Makefile to build the jltools source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#

# set up some reasonable defaults (for building in CUCS)
include $(SOURCE)/Defs.mk

.SUFFIXES: .class .java

CC			= gcc
JC_FLAGS 		= -g -d $(OUTPUT) $(JAVAC_PATHS)
RMIC_FLAGS		= -d $(OUTPUT) -classpath $(CLASSPATH)

JAR_FILE		= jltools.jar
JAR_FLAGS		= cf 

JAVADOC_MAIN		= com.sun.tools.javadoc.Main
JAVADOC_DOCLET		= iContract.doclet.Standard
JAVADOC_OUTPUT		= $(SOURCE)/javadoc
JAVADOC_FLAGS		= -mx40m -ms40m -classpath "$(JAVADOC_CLASSPATH)"

BIN 			= $(SOURCE)/bin
JIF			= $(BIN)/jifc
JIF_FLAGS		= -d $(OUTPUT) -sourcepath $(BUILDPATH)

SOURCEPATH		= $(SOURCE)
PACKAGEPATH		= $(SOURCE)/classes/$(PACKAGE)
VPATH			= $(PACKAGEPATH)
RELPATH			= $(SOURCE)/release/jif-1.0
REL_DOC			= $(RELPATH)/doc
REL_IMG			= $(RELPATH)/images
REL_LIB			= $(RELPATH)/lib

REL_SOURCES		= $(SOURCES)

# To avoid repeated slashes
DIR_ = $(DIR)
PACKAGE_ = $(PACKAGE)
DEMO_DIR_ = $(DEMO_DIR)
ifneq ($(DIR)foo, foo) 
DIR_ = /$(DIR)
endif
ifneq ($(PACKAGE)foo, foo)
PACKAGE_ = /$(PACKAGE)
endif
ifneq ($(DEMO_DIR)foo, foo)
DEMO_DIR_ = /$(DEMO_DIR)
endif
REL_SRC = $(RELPATH)/src$(DIR_)$(PACKAGE_)
REL_DEMO = $(RELPATH)/demo$(DEMO_DIR_)$(PACKAGE_)

all clean clobber javadoc release:

$(PACKAGEPATH)/%.class: %.java
	$(JC) $(JC_FLAGS) $<

cleanclasses:
	-rm -f $(PACKAGEPATH)/*.class

clean_java_output:
	-rm -f $(PACKAGEPATH)/*.java*

classpath:
	@echo "setenv CLASSPATH $(CLASSPATH)"

release_src:
	mkdir -p $(REL_SRC)
	@cp -f $(REL_SOURCES) Makefile $(REL_SRC)
	@if [ -f package.html ]; then cp package.html $(REL_SRC); fi

release_demo:
	mkdir -p $(REL_DEMO)
	@if [ -n "$(DEMOS)" ]; then cp -f $(DEMOS) $(REL_DEMO); fi
	@if [ -f package.html ]; then cp package.html $(REL_DEMO); fi

define subdirs
@for i in $(SUBDIRS) ""; do \
    if [ "x$$i" != "x" ]; then $(MAKE) -C $$i $@ || exit 1; fi; \
done
endef

# define javadoc
# -rm -rf $(JAVADOC_OUTPUT)
# -mkdir -p $(JAVADOC_OUTPUT)
# "$(JAVA)" "$(JAVADOC_FLAGS)" $(JAVADOC_MAIN) \
# 	-d $(JAVADOC_OUTPUT) \
# 	-doclet $(JAVADOC_DOCLET) \
# 	-sourcepath $(SOURCEPATH) \
# 	-classpath "$(CLASSPATH)" $(PACKAGES)
# endef

define javadoc
-rm -rf $(JAVADOC_OUTPUT)
-mkdir -p $(JAVADOC_OUTPUT)
"$(JAVADOC)" -d $(JAVADOC_OUTPUT) \
	-sourcepath $(SOURCEPATH) \
	-classpath "$(CLASSPATH)" \
	$(PACKAGES)
endef
#	-doclet $(JAVADOC_DOCLET) \
#	-docletpath "$(JAVADOC_CLASSPATH)" \

define yacc
awk 'BEGIN {FS = "\n"; s = 1} {print $$1, "\t // ", s++}' $< | \
	"$(JAVA)" -classpath "$(CLASSPATH)" java_cup.Main -parser Grm
endef

define flex
	"$(JAVA)" -classpath "$(CLASSPATH)" JFlex.Main $<
endef


