
# Environment variables you must set:
#	OSTYPE
#	MAKE_MODE (with cygwin)

# If CODEBASE is not defined then assume that this makefile was 
# invoked directly and define "RECURSIVE" behavior.
ifndef CODEBASE
CODEBASE	= .
RECURSIVE	= jltools
endif

# First include personal Makefile
-include $(CODEBASE)/Make.personal


# Default settings.
ifndef CLASSBASE
CLASSBASE	= $(CODEBASE)/bin
endif

# Platform dependent defaults.
ifeq ($(OSTYPE),'winnt')
ifndef JC
JC		= javac
endif
ifndef CUPBASE
JAVA_CUPBASE	= d:\classes
endif
ifndef ADDCLASSPATH
ADDCLASSPATH	= $(CLASSBASE);$(CODEBASE);$(JAVA_CUPBASE);$(CLASSPATH)
endif

else # OSTYPE != winnt
ifndef JC
JC	 	= javac
endif
ifndef CUPBASE
JAVA_CUPBASE	= /home/spoons/classes
endif
ifndef ADDCLASSPATH
ADDCLASSPATH	= $(CLASSBASE):$(CODEBASE):$(JAVA_CUPBASE):$(CLASSPATH)
endif

endif 

# Platform indepentent defaults.
ifndef JCFLAGS
JCFLAGS		= -d $(CLASSBASE) -classpath $(ADDCLASSPATH)
endif

# End default settings.



# Targets, depending on where we are.
ifdef RECURSIVE
all: recursive

recursive:
	for DIR in $(RECURSIVE); do $(MAKE) -C $$DIR; done

else # !RECURSIVE
ifdef LEAF
all: leaf

leaf: *.java
	$(JC) $(JCFLAGS) *.java


clean:
	rm $(CLASSBASE)/$(PACKAGE)/*.class


endif # End LEAF.
endif # End RECURSIVE.

# End targets.
