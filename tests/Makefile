LOG=log

GOOD =  \
	ArrayInit.jl \
	Ctor.jl \
	Dep1.jl \
	Equality.jl \
	Except.jl \
	Except2.jl \
	FieldAccess.jl \
	ForwardRef.jl \
	ForwardRef2.jl \
	Inner0.jl \
	Inner1.jl \
	Inner2.jl \
	Inner3.jl \
	Inner4.jl \
	Inner5.jl \
	Inner6.jl \
	Inner7.jl \
	Inner8.jl \
	Prim.jl \
	StaticContext.jl \
	StaticOps.jl \
	xIterator.jl

BAD = \
	Errors.jl \
	Errors2.jl \
	Except3.jl \
	Except4.jl \
	NoInit.jl \
	NoReturns.jl \
	Unreachable.jl \
	BadAbstract.jl \
	BadAlloc.jl \
	BadCtor.jl \
	BadExcept.jl \
	BadExcept2.jl \
	BadExpCC.jl \
	BadExtends.jl \
	BadInit.jl \
	BadInner.jl \
	BadInner2.jl \
	BadInner3.jl \
	BadInner4.jl \
	BadInner5.jl \
	BadInner6.jl \
	BadInner7.jl \
	BadInterface.jl \
	BadInterface2.jl \
	BadOverride.jl \
	BadPrim.jl \
	BadProt.jl \
	Protection.jl \
	Protection2.jl \
	BadStaticContext.jl

.SUFFIXES: .jl .java

GOOD_OUTPUT = $(patsubst %.jl,%.java,$(GOOD)) Dep2.java Dep3.java
BAD_OUTPUT = $(patsubst %.jl,%.java,$(BAD))

all: good bad

good: $(GOOD)
	-rm -f $(GOOD_OUTPUT) good.log
	cat /dev/null > good.log
	$(MAKE) LOG=good.log $(GOOD_OUTPUT)
	@if cmp -s good.log.in good.log; then \
	    echo "LOG UNCHANGED"; \
	    diff good.log.in good.log; \
	else \
	    echo "LOG CHANGED"; \
	    false; \
	fi

bad: $(BAD)
	-rm -f $(BAD_OUTPUT) bad.log
	cat /dev/null > bad.log
	$(MAKE) LOG=bad.log $(BAD_OUTPUT)
	@if cmp -s bad.log.in bad.log; then \
	    echo "LOG UNCHANGED"; \
	    diff bad.log.in bad.log; \
	else \
	    echo "LOG CHANGED"; \
	    false; \
	fi

clean:
	-rm -f $(GOOD_OUTPUT)
	-rm -f $(BAD_OUTPUT)
	-rm -f *.class
	-rm -f good.log bad.log

.jl.java:
	@-rm -f *.class
	@-echo jlc $<
	@-jlc $< 2>&1 | sed 's%^.*/\([A-Za-z0-9_]*\.java:\)%\1%g' | tee -a $(LOG)
