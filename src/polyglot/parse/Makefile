
prs_package	= jltools/parse
prs_codebase	= ../..
prs_files 	= Grm.java sym.java

PARSE_TARGET = $(patsubst %.java, $(prs_package)/%.class, $(prs_files))

$(prs_package)/Grm.class: $(prs_package)/Grm.java
$(prs_package)/sym.class: $(prs_package)/sym.java

$(prs_package)/Grm.java $(prs_package)/sym.java:  $(prs_package)/java12.cup
	cd $(prs_package) && $(JAVA) $(CLASSPATH) java_cup.Main -parser Grm < java12.cup

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) parse
%:
	cd ../.. && $(MAKE) $@
endif
