LOG=log

GOOD =  \
	ArrayInit.jl \
	Ctor.jl \
	Dep1.jl \
	Equality.jl \
	Except2.jl \
	Except3.jl \
	Except4.jl \
	Except.jl \
	FieldAccess.jl \
	ForwardRef2.jl \
	ForwardRef3.jl \
	ForwardRef.jl \
	Init1.jl \
	Init2.jl \
	Init3.jl \
	Init4.jl \
	Init5.jl \
	Inner0.jl \
	Inner1.jl \
	Inner2.jl \
	Inner3.jl \
	Inner4.jl \
	Inner5.jl \
	Inner6.jl \
	Inner7.jl \
	Inner8.jl \
	Inner9.jl \
	Prim.jl \
	Return1.jl \
	Return2.jl \
	Return3.jl \
	Return4.jl \
	StaticContext.jl \
	StaticOps.jl \
	Switch1.jl \
	Switch2.jl \
	Switch3.jl \
	xIterator.jl

BAD = \
	BadAbstract.jl \
	BadAlloc.jl \
	BadCtor.jl \
	BadExcept2.jl \
	BadExcept.jl \
	BadExpCC.jl \
	BadExtends.jl \
	BadForwardRef.jl \
	BadInit.jl \
	BadInner2.jl \
	BadInner3.jl \
	BadInner4.jl \
	BadInner5.jl \
	BadInner6.jl \
	BadInner7.jl \
	BadInner.jl \
	BadInterface2.jl \
	BadInterface.jl \
	BadOverride.jl \
	BadPrim.jl \
	BadProt.jl \
	BadStaticContext.jl \
	BadSwitch1.jl \
	BadSwitch2.jl \
	Errors2.jl \
	Errors.jl \
	NoInit10.jl \
	NoInit11.jl \
	NoInit12.jl \
	NoInit1.jl \
	NoInit2.jl \
	NoInit3.jl \
	NoInit4.jl \
	NoInit5.jl \
	NoInit6.jl \
	NoInit7.jl \
	NoInit8.jl \
	NoInit9.jl \
	NoReturn1.jl \
	NoReturn2.jl \
	NoReturn3.jl \
	NoReturn4.jl \
	NoReturn5.jl \
	Protection2.jl \
	Protection.jl \
	Unreachable10.jl \
	Unreachable11.jl \
	Unreachable1.jl \
	Unreachable2.jl \
	Unreachable3.jl \
	Unreachable4.jl \
	Unreachable5.jl \
	Unreachable6.jl \
	Unreachable7.jl \
	Unreachable8.jl \
	Unreachable9.jl

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

MANIFEST = Makefile $(SOURCES)
