
prs_package	= jltools/parse
prs_codebase	= ../..
prs_files 	= Grm.java sym.java

PARSE_TARGET = $(patsubst %.java, $(prs_package)/%.class, $(prs_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) parse
%:
	cd ../.. && $(MAKE) $@
endif

$(prs_package)/Grm.class: $(prs_package)/Grm.java
$(prs_package)/sym.class: $(prs_package)/sym.java

$(prs_package)/Grm.java $(prs_package)/sym.java:  $(prs_package)/java12.cup
	cd $(prs_package) &&  awk 'BEGIN {FS="\n"} {print $$1,"\t // ",s++}' \
		java12.cup | $(JAVA) $(CLASSPATH) java_cup.Main -parser Grm 
